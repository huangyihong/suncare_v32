package com.ai.solr;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ai.modules.engine.model.RTimer;

public class Hive2SolrMain {
	private final Logger logger = LoggerFactory.getLogger(Hive2SolrMain.class);
	private final AtomicInteger count = new AtomicInteger(0);
	
	public boolean execute(String path, String collection, boolean multthread) throws Exception {
		count.set(0);
		RTimer runtimer = new RTimer();
		//String path = "/home/gbdp/zly/udf";
		//String collection = "test_dwb_charge_detail";						
		logger.info("Main args: path={}; collection={}, multthread={}", path, collection, multthread);
						
		if (collection == null) {
			throw new Exception("Specifying either collection is mandatory.");
		}
		//是否使用多线程处理，>1启用
		int thread = multthread ? 5 : 1;		
		//处理完文件删除
		boolean frm = true;
		Properties props = new Properties();
		props.setProperty("type", "text/json");
		props.setProperty("url", collection);
		props.setProperty("frm", String.valueOf(frm));
		String hdfs = HiveJDBCUtil.FS_DEFAULT_NAME;
		props.setProperty("hdfs", hdfs);
		
		boolean success = true;
		List<String> filePathList = getFilePathList(path);
		logger.info("需要处理文件个数："+filePathList.size());
		if(filePathList.size()>0) {
			if(thread>1 && filePathList.size()<5) {
				thread = filePathList.size();
			}
			//开始处理文件
			success = execute(props, filePathList, thread, frm);
		}
		
		if(success) {
			//删除目录
			this.removeFile(path);
			displayTiming((long) runtimer.getTime(), "文件处理完成，消耗时长");
		}
		return success;
	}
	
	/**
	 * 获取路径下的文件
	 * @param path
	 * @return
	 * @throws Exception
	 */
	private List<String> getFilePathList(String path) throws Exception {
		List<String> filePathList = new ArrayList<String>();		
		//获取目录下的文件
		String hdfs = HiveJDBCUtil.FS_DEFAULT_NAME;			
		Configuration conf = new Configuration();
        conf.set("fs.default.name", hdfs);
        FileSystem fileSystem = FileSystem.get(conf);
        Path p = new Path(path);
        RemoteIterator<LocatedFileStatus> status = fileSystem.listFiles(p, true);
        while(status.hasNext()) {
            LocatedFileStatus fileStatus = status.next();
            if(fileStatus.getLen()>0) {
            	String fileName = fileStatus.getPath().toString();
	            filePathList.add(fileName);
            }
        }
		return filePathList;
	}
	
	/**
	 * post文件到solr
	 * @param filePathList：文件列表
	 * @param thread：线程数
	 * @param is_hdfs：是否hdfs文件系统
	 * @param frm：处理完成功是否删除文件
	 * @throws Exception
	 */
	private boolean execute(Properties props, List<String> filePathList, int thread, boolean frm) throws Exception {				
		if(thread<=1) {
			//单线程处理
			return post(props, filePathList, frm);
		} else {
			//多线程处理
			return postByThread(props, filePathList, thread, frm);
		}
	}
	
	private boolean post(Properties props, List<String> filePathList, boolean frm) throws Exception {
		for(String filePath : filePathList) {
			boolean success = post(props, filePath);
			if(!success) {
				return false;
			} else {				
				if(frm) {
					//删除文件
					this.removeFile(filePath);
				}
			}
		}	
		return true;
	}
	
	private boolean post(Properties props, String filePath) throws Exception {
		RTimer timer = new RTimer();
		logger.info("正在处理文件：" + filePath);
		boolean success = true;
		try {
			success = HivePostSolrTool.post(props, filePath);
		} catch (Exception e) {
		}
		if(!success) {
			//休眠1分钟，重做一次
			try {
				Thread.sleep(60000L);
			} catch (InterruptedException e) {
			}
			logger.info("开始重做...{}", filePath);
			success = HivePostSolrTool.post(props, filePath);
		}
		if(success) {
			count.incrementAndGet();
			displayTiming((long) timer.getTime(), "处理时长");
			logger.info("已处理文件个数："+count.get());
		}
		return success;
	}
	
	private boolean postByThread(Properties props, List<String> filePathList, int poolSize, final boolean frm) {
		// 有界队列
		BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(20);
		// 线程池
		ThreadPoolExecutor executor = new ThreadPoolExecutor(poolSize, poolSize, 5, TimeUnit.MINUTES, queue);
		executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				try {
					// 核心改造点，由blockingqueue的offer改成put阻塞方法
					executor.getQueue().put(r);
				} catch (InterruptedException e) {
				}
			}
		});
		final AtomicInteger err = new AtomicInteger(0);
		for(final String filePath : filePathList) {
			if(err.get()>0) {
				break;
			}
			final String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
			executor.execute(new Runnable() {
				@Override
				public void run() {
					RTimer timer = new RTimer();
					logger.info("正在处理文件：" + filePath);
					boolean success = true;
					try {
						success = HivePostSolrTool.post(props, filePath);
					} catch (Exception e) {
					}
					if(success==false) {
						//休眠1分钟，重做一次
						try {
							Thread.sleep(60000L);
						} catch (InterruptedException e) {
						}
						logger.info("开始重做...{}", filePath);
						try {
							success = HivePostSolrTool.post(props, filePath);
						} catch (Exception e) {
						}
					}
					if(success==false) {
						err.incrementAndGet();
					} else {
						count.incrementAndGet();						
						displayTiming((long) timer.getTime(), fileName+"处理时长");	
						logger.info("已处理文件个数：{}/{}", count.get(), filePathList.size());
						if(frm) {
							//删除文件
							removeFile(filePath);
						}
					}
				}        		
        	});
		}
		
		executor.shutdown();
		while (true) {
			if (executor.isTerminated()) {
				break;
			}
			try {
				Thread.sleep(10000L);
			} catch (InterruptedException e) {
			}
		}
		return err.get()==0 ? true : false;
	}
	
	/**
	 * 删除本地文件
	 * @param filePath
	 * @param is_hdfs
	 */
	private void removeFile(String filePath) {
		try {
        	String hdfs = HiveJDBCUtil.FS_DEFAULT_NAME;				
			Configuration conf = new Configuration();
	        conf.set("fs.default.name", hdfs);
			FileSystem fileSystem = FileSystem.get(conf);
			Path path = new Path(filePath);
			fileSystem.delete(path, true);
		} catch (IOException e) {				
		}
	}
	
	private void displayTiming(long millis, String title) {
		SimpleDateFormat df = new SimpleDateFormat("H:mm:ss.SSS", Locale.getDefault());
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		logger.info(title + ": " + df.format(new Date(millis)));
	}	
}
