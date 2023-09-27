package com.ai.solr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Locale;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HivePostSolrTool {
	private static final Logger logger = LoggerFactory.getLogger(HivePostSolrTool.class);
	private static final int CONNECT_TIME_OUT = 60000;
	private static final int READ_TIME_OUT = 60000;
	boolean auto = false;
	String fileTypes;
	URL solrUrl;
	OutputStream out = null;

	String type;
	boolean commit;
	String path;
	String hdfs;

	public static boolean post(Properties props, String dir) throws Exception {	
		HivePostSolrTool tool = parseArgsAndInit(props, dir);
		if(tool==null) {
			return false;
		}
		return tool.execute();
	}

	private boolean execute() throws Exception {
		boolean success = doFilesMode();
		if (success && this.commit) {
			success = commit();
		}
		return success;
	}

	protected static HivePostSolrTool parseArgsAndInit(Properties props, String dir) throws Exception {
		String urlStr = null;
		String host = props.getProperty("host", "localhost");
		String port = props.getProperty("port", "8983");
		String core = props.getProperty("c");

		urlStr = props.getProperty("url");

		if (urlStr == null && core == null) {
			exception("Specifying either url or core/collection is mandatory.\nUsage: java [SystemProperties] -jar post.jar [-h|-] [<file|folder|url|arg> [<file|folder|url|arg>...]]");
		}

		if (urlStr == null) {
			urlStr = String.format(Locale.ROOT, "http://%s:%s/solr/%s/update", new Object[] { host, port, core });
		}
		URL url = new URL(urlStr);

		boolean auto = isOn(props.getProperty("auto", "no"));
		String type = props.getProperty("type");
		String hdfs = props.getProperty("hdfs");
	
		OutputStream out = isOn(props.getProperty("out", "no")) ? System.out : null;
		String fileTypes = props.getProperty("filetypes",
				"xml,json,jsonl,csv,pdf,doc,docx,ppt,pptx,xls,xlsx,odt,odp,ods,ott,otp,ots,rtf,htm,html,txt,log");
		boolean commit = isOn(props.getProperty("commit", "yes"));

		return new HivePostSolrTool(url, auto, type, fileTypes, out, commit, dir, hdfs);
	}

	public HivePostSolrTool(URL url, boolean auto, String type,
			String fileTypes, OutputStream out, boolean commit, String path, String hdfs) {
		this.solrUrl = url;
		this.auto = auto;
		this.type = type;
		this.fileTypes = fileTypes;
		this.out = out;
		this.commit = commit;
		this.path = path;
		this.hdfs = hdfs;
	}

	private boolean doFilesMode() throws Exception {
		info("Posting " + (this.hdfs!=null ? "hdfs " : "") + "files to [base] url " + this.solrUrl
				+ (!this.auto ? (" using content-type " + ((this.type == null) ? "application/xml" : this.type))
						: "")
				+ "...");
		if (this.auto)
			info("Entering auto mode. File endings considered are " + this.fileTypes);
		return postFiles(path, this.out, this.type);
	}

	private boolean postFiles(String path, OutputStream out, String type) throws Exception {
		return postFile(path, out, type);
	}

	protected static boolean isOn(String property) {
		return ("true,on,yes,1".indexOf(property) > -1);
	}

	private void warn(String msg) {
		logger.error("HivePostSolrTool FILE: {}, WARNING: {}", path, msg);
	}

	private void info(String msg) {
		logger.info(msg);
	}

	private void fatal(String msg) {
		logger.error("HivePostSolrTool FILE: {}, FATAL: {}", path, msg);
	}
	
	static void exception(String msg) throws Exception {
		throw new Exception(msg);
	}

	private boolean commit() throws Exception {
		//info("commiting Solr index changes to " + this.solrUrl + "...");
		return doGet(appendParam(this.solrUrl.toString(), "commit=true"));
	}

	private String appendParam(String url, String param) {
		String[] pa = param.split("&");
		for (String p : pa) {
			if (p.trim().length() != 0) {
				String[] kv = p.split("=");
				if (kv.length == 2) {
					url = url + ((url.indexOf('?') > 0) ? "&" : "?") + kv[0] + "=" + kv[1];
				} else {
					warn("Skipping param " + p + " which is not on form key=value");
				}
			}
		}
		return url;
	}

	private boolean postFile(String path, OutputStream output, String type) throws Exception {
		boolean result = true;
		InputStream is = null;
		try {
			URL url = this.solrUrl;
			if (type == null) {
				type = "application/xml";
			}

			//info("POSTing " + (this.hdfs!=null ? "hdfs " : "") + "file " + path + " to [base]" + suffix);
			if(hdfs==null || hdfs.trim().length()==0) {
				File file = new File(path);
				is = new FileInputStream(file);
				result = postData(is, null, output, type, url);
			} else {
				//hdfs文件路径
				Configuration conf = new Configuration();
		        conf.set("fs.default.name", hdfs);
		        FileSystem fileSystem = FileSystem.get(conf);
		        Path p = new Path(path);
				is = fileSystem.open(p);
				result = postData(is, null, output, type, url);
			}
			
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
				//fatal("IOException while closing file: " + e);
			}
		}
		return result;
	}

	private boolean doGet(String url) throws Exception {
		return doGet(new URL(url));
	}

	private boolean doGet(URL url) throws Exception {
		HttpURLConnection urlc = null;
		try {
			urlc = (HttpURLConnection) url.openConnection();
			urlc.setConnectTimeout(CONNECT_TIME_OUT);
			urlc.setReadTimeout(READ_TIME_OUT);
			basicAuth(urlc);
			urlc.connect();
			checkResponseCode(urlc);
        } catch(Exception e) {
        	throw e;
        } finally {
        	try {
				if (urlc != null) {
					urlc.disconnect();
				}
			} catch(Exception e) {}
        }		
		return true;
	}
	
	private boolean postData(InputStream data, Long length, OutputStream output, String type, URL url) throws Exception {
		boolean success = true;
		if (type == null)
			type = "application/xml";
		HttpURLConnection urlc = null;
		try {
			urlc = (HttpURLConnection) url.openConnection();
			urlc.setConnectTimeout(CONNECT_TIME_OUT);
			urlc.setReadTimeout(READ_TIME_OUT);
			urlc.setRequestMethod("POST");
			urlc.setDoOutput(true);
			urlc.setDoInput(true);
			urlc.setUseCaches(false);
			urlc.setAllowUserInteraction(false);
			urlc.setRequestProperty("Content-type", type);
			basicAuth(urlc);
			if (null != length) {
				urlc.setFixedLengthStreamingMode(length.longValue());
			} else {
				urlc.setChunkedStreamingMode(-1);
			}
			urlc.connect();

			OutputStream out = urlc.getOutputStream();
			write(data, out);		

			success &= checkResponseCode(urlc);
			InputStream in = urlc.getInputStream();
			pipe(in, output);
		} catch (IOException e) {
			fatal("Connection error (is Solr running at " + this.solrUrl + " ?): " + e);
			success = false;
			throw e;
		} catch (GeneralSecurityException e) {
			fatal("Looks like Solr is secured and would not let us in. Try with another user in '-u' parameter");
			throw e;
		} catch (Exception e) {
			fatal("POST failed with error " + e.getMessage());
			throw e;
		} finally {
			if (urlc != null)
				urlc.disconnect();
		}
		return success;
	}
	
	private static void basicAuth(HttpURLConnection urlc) throws Exception {
		if (urlc.getURL().getUserInfo() != null) {
			String encoding = Base64.getEncoder()
					.encodeToString(urlc.getURL().getUserInfo().getBytes(StandardCharsets.US_ASCII));
			urlc.setRequestProperty("Authorization", "Basic " + encoding);
		} else if (System.getProperty("basicauth") != null) {
			String basicauth = System.getProperty("basicauth").trim();
			if (!basicauth.contains(":")) {
				throw new Exception("System property 'basicauth' must be of format user:pass");
			}
			urlc.setRequestProperty("Authorization",
					"Basic " + Base64.getEncoder().encodeToString(basicauth.getBytes(StandardCharsets.UTF_8)));
		}
	}	

	private boolean checkResponseCode(HttpURLConnection urlc) throws IOException, GeneralSecurityException {
		if (urlc.getResponseCode() >= 400) {
			warn("Solr returned an error #" + urlc.getResponseCode() + " (" + urlc.getResponseMessage() + ") for url: "
					+ urlc.getURL());
			Charset charset = StandardCharsets.ISO_8859_1;
			String contentType = urlc.getContentType();

			if (contentType != null) {
				int idx = contentType.toLowerCase(Locale.ROOT).indexOf("charset=");
				if (idx > 0) {
					charset = Charset.forName(contentType.substring(idx + "charset=".length()).trim());
				}
			}

			try (InputStream errStream = urlc.getErrorStream()) {
				if (errStream != null) {
					BufferedReader br = new BufferedReader(new InputStreamReader(errStream, charset));
					StringBuilder response = new StringBuilder("Response: ");
					int ch;
					while ((ch = br.read()) != -1) {
						response.append((char) ch);
					}
					warn(response.toString().trim());
				}
			}
			if (urlc.getResponseCode() == 401) {
				throw new GeneralSecurityException(
						"Solr requires authentication (response 401). Please try again with '-u' option");
			}
			if (urlc.getResponseCode() == 403) {
				throw new GeneralSecurityException(
						"You are not authorized to perform this action against Solr. (response 403)");
			}
			return false;
		}
		return true;
	}
	
	public static InputStream stringToStream(String s) {
		return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
	}

	private static void pipe(InputStream source, OutputStream dest) throws IOException {
		byte[] buf = new byte[1024];
		int read = 0;
		if (null != dest) {
			dest.write("[".getBytes());
		}
		while ((read = source.read(buf)) >= 0) {
			if (null != dest)
				dest.write(buf, 0, read);
		}		
		if (null != dest) {
			dest.write("]".getBytes());
			dest.flush();
		}			
	}
	
	private static void write(InputStream source, OutputStream desc) throws IOException {
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			int buffSize = 10*1024*1024; //10M缓冲区缓存
			br = new BufferedReader(new InputStreamReader(source, "utf-8"), buffSize);
			bw = new BufferedWriter(new OutputStreamWriter(desc));
			bw.write("[");
			String line = null;
			while((line = br.readLine()) != null) {
				bw.write(line);
			}
			bw.write("]");
			bw.flush();
		} catch(IOException e) {
			throw e;
		} finally {
			try {
				if(bw!=null) {
					bw.close();
				}
			} catch(Exception e) {
				
			}
			try {
				if(br!=null) {
					br.close();
				}
			} catch(Exception e) {
				
			}
		}
	}
}
