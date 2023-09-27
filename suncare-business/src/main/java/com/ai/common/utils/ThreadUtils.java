package com.ai.common.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.ai.modules.api.util.ApiTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.modules.engine.runnable.AbsEngineRunnable;
import com.ai.modules.engine.runnable.EngineRejectedExecutionHandler;

/**
 * Created by Nearlyz on 2018/4/18 0018.
 * Describe:
 */

@Component
public class ThreadUtils {
//  public static int threadNum = 0;

    private static ExecutorService executor;

    public static void add(Runnable runnable) {
        executor.submit(runnable);
    }

    public static void create() {
        executor = Executors.newCachedThreadPool();
    }

    public static void shutDown() {
        executor.shutdown();
    }


    /**
     * 执行solr请求操作线程池
     */

    public static FixPool THREAD_SOLR_REQUEST_POOL = new FixPool(10);

    /**
     * 模型计算引擎线程池
     */
    public static FixPool THREAD_CASE_POOL = new FixPool(5);

    /**
     * 药品、收费合规批次计算引擎线程池
     */
    public static FixPool THREAD_DRUG_POOL = new FixPool(5);

    /**
     * 药品、收费合规规则计算引擎线程池
     */
    public static FixPool THREAD_DRUGITEM_POOL = new FixPool(4);

    /**
     * 试算引擎线程池
     */
    public static FixPool THREAD_TRAIL_POOL = new FixPool(5);

    public static ThreadPool THREAD_DRG_POOL = new ThreadPool(5);

    /**
     * solr数据源线程本地变量
     */
    private static ThreadLocal<String> THREAD_LOCAL_DATASOURCE = new ThreadLocal<>();

    private static ThreadLocal<String> THREAD_LOCAL_TOKEN = new ThreadLocal<>();

    public static void setDatasource(String datasource) {
    	THREAD_LOCAL_DATASOURCE.set(datasource);
    }

    public static String getDatasource() {
    	return THREAD_LOCAL_DATASOURCE.get();
    }

    public static void setToken(String token){
        THREAD_LOCAL_TOKEN.set(token);
    }

    public static String getToken() {
        return THREAD_LOCAL_TOKEN.get();
    }

    public static void removeToken() {
        THREAD_LOCAL_TOKEN.remove();
    }

    public static void setTokenDef(){
        setToken(ApiTokenUtil.DEFAULT_TOKEN);
    }

    public static void removeDatasource() {
    	THREAD_LOCAL_DATASOURCE.remove();
    }

    /**
     * 导出文件线程池
     */
    public static ThreadExportPool EXPORT_POOL;

    @Autowired
    public void setExportPoll(ThreadExportPool thread) {
        EXPORT_POOL = thread;
    }

    /**
     * 异步操作线程池
     */
    public static ThreadAsyncPool ASYNC_POOL;

    @Autowired
    public void setAsyncPool(ThreadAsyncPool thread) {
        ASYNC_POOL = thread;
    }


    public static class FixPool {
    	ThreadPoolExecutor executor;

        public void add(AbsEngineRunnable runnable) {
            executor.execute(runnable);
        }

        public void removeAll() {
            executor.shutdownNow();
        }

        public FixPool(){

        }

        public FixPool(int num) {
            //executor = new ThreadPoolExecutor(num, num, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        	// 有界队列
			BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(20);
			// 线程池
			executor = new ThreadPoolExecutor(num, num, 10, TimeUnit.MINUTES, queue);
			executor.setRejectedExecutionHandler(new EngineRejectedExecutionHandler());
        }

        public ThreadPoolExecutor getExecutor() {
            return executor;
        }
    }

    public static class ThreadPool {
        ThreadPoolExecutor executor;

        public void add(Runnable runnable) {
            executor.execute(runnable);
        }

        public void removeAll() {
            executor.shutdownNow();
        }

        public ThreadPool(){

        }

        public ThreadPool(int num) {
            // 有界队列
            BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(20);
            // 线程池
            executor = new ThreadPoolExecutor(num, num, 10, TimeUnit.MINUTES, queue);
            executor.setRejectedExecutionHandler(new EngineRejectedExecutionHandler());
        }

        public ThreadPoolExecutor getExecutor() {
            return executor;
        }
    }
}
