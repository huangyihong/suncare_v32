/**
 * SimplePostTool.java	  V1.0   2020年9月2日 上午11:00:30
 *
 * Copyright (c) 2020 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 * 
 * Description:
 */

package com.ai.solr;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ai.modules.engine.model.RTimer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimplePostTool {
	private static final int CONNECT_TIME_OUT = 60000;
	private static final int READ_TIME_OUT = 60000;
	private static final String DATA_MODE_FILES = "files";
	private static final String DATA_MODE_ARGS = "args";
	private static final String DATA_MODE_STDIN = "stdin";
	private static final String DATA_MODE_WEB = "web";
	private boolean auto = false;
	private int recursive = 0;
	private int delay = 0;
	private String fileTypes;
	private URL solrUrl;
	private OutputStream out = null;
	private String type;
	private String format;

	private String mode;
	private boolean commit;
	private boolean optimize;
	private String[] args;
	private int currentDepth;
	private static HashMap<String, String> mimeMap;
	private FileFilter fileFilter;
	private List<LinkedHashSet<URL>> backlog = new ArrayList<>();
	private Set<URL> visited = new HashSet<>();

	private static final Set<String> DATA_MODES = new HashSet<>();
	private static boolean mockMode = false;
	private static PageFetcher pageFetcher;

	static {
		DATA_MODES.add(DATA_MODE_FILES);
		DATA_MODES.add(DATA_MODE_ARGS);
		DATA_MODES.add(DATA_MODE_STDIN);
		DATA_MODES.add(DATA_MODE_WEB);

		mimeMap = new HashMap<>();
		mimeMap.put("xml", "application/xml");
		mimeMap.put("csv", "text/csv");
		mimeMap.put("json", "application/json");
		mimeMap.put("jsonl", "application/json");
		mimeMap.put("pdf", "application/pdf");
		mimeMap.put("rtf", "text/rtf");
		mimeMap.put("html", "text/html");
		mimeMap.put("htm", "text/html");
		mimeMap.put("doc", "application/msword");
		mimeMap.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		mimeMap.put("ppt", "application/vnd.ms-powerpoint");
		mimeMap.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
		mimeMap.put("xls", "application/vnd.ms-excel");
		mimeMap.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		mimeMap.put("odt", "application/vnd.oasis.opendocument.text");
		mimeMap.put("ott", "application/vnd.oasis.opendocument.text");
		mimeMap.put("odp", "application/vnd.oasis.opendocument.presentation");
		mimeMap.put("otp", "application/vnd.oasis.opendocument.presentation");
		mimeMap.put("ods", "application/vnd.oasis.opendocument.spreadsheet");
		mimeMap.put("ots", "application/vnd.oasis.opendocument.spreadsheet");
		mimeMap.put("txt", "text/plain");
		mimeMap.put("log", "text/plain");
	}

	public static void execute(String[] args, Properties props) throws Exception{
		info("SimplePostTool version 5.0.0");
		if (0 < args.length && ("-help".equals(args[0]) || "--help".equals(args[0]) || "-h".equals(args[0]))) {
			usage();
			throw new Exception("请按参数说明传参");
		} else {
			SimplePostTool t = parseArgsAndInit(args, props);
			t.execute();
		}
	}

	private void execute() throws Exception {
		RTimer timer = new RTimer();
		if (DATA_MODE_FILES.equals(this.mode) && this.args.length > 0) {
			doFilesMode();
		} else if (DATA_MODE_ARGS.equals(this.mode) && this.args.length > 0) {
			doArgsMode();
		} else if (DATA_MODE_WEB.equals(this.mode) && this.args.length > 0) {
			doWebMode();
		} else if (DATA_MODE_STDIN.equals(this.mode)) {
			doStdinMode();
		} else {
			usageShort();
			return;
		}
		if (this.commit)
			commit();
		if (this.optimize)
			optimize();
		displayTiming((long) timer.getTime());
	}

	private void displayTiming(long millis) {
		SimpleDateFormat df = new SimpleDateFormat("H:mm:ss.SSS", Locale.getDefault());
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		log.info("Time spent: " + df.format(new Date(millis)));
	}

	protected static SimplePostTool parseArgsAndInit(String[] args, Properties props) throws Exception {
		String urlStr = null;

		String mode = props.getProperty("data", "files");
		if (!DATA_MODES.contains(mode)) {
			throwMsg("System Property 'data' is not valid for this tool: " + mode);
		}

		String params = props.getProperty("params", "");

		String host = props.getProperty("host", "localhost");
		String port = props.getProperty("port", "8983");
		String core = props.getProperty("c");

		urlStr = props.getProperty("url");

		if (urlStr == null && core == null) {
			throwMsg("Specifying either url or core/collection is mandatory.\nUsage: java [SystemProperties] -jar post.jar [-h|-] [<file|folder|url|arg> [<file|folder|url|arg>...]]");
		}

		if (urlStr == null) {
			urlStr = String.format(Locale.ROOT, "http://%s:%s/solr/%s/update", new Object[] { host, port, core });
		}
		urlStr = appendParam(urlStr, params);
		URL url = new URL(urlStr);
		String user = null;
		if (url.getUserInfo() != null && url.getUserInfo().trim().length() > 0) {
			user = url.getUserInfo().split(":")[0];
		} else if (props.getProperty("basicauth") != null) {
			user = props.getProperty("basicauth").trim().split(":")[0];
		}
		if (user != null) {
			info("Basic Authentication enabled, user=" + user);
		}
		boolean auto = isOn(props.getProperty("auto", "no"));
		String type = props.getProperty("type");
		String format = props.getProperty("format");

		int recursive = 0;
		String r = props.getProperty("recursive", "0");
		try {
			recursive = Integer.parseInt(r);
		} catch (Exception e) {
			if (isOn(r)) {
				recursive = "web".equals(mode) ? 1 : 999;
			}
		}
		int delay = "web".equals(mode) ? 10 : 0;
		delay = Integer.parseInt(props.getProperty("delay", "" + delay));

		OutputStream out = isOn(props.getProperty("out", "no")) ? System.out : null;
		String fileTypes = props.getProperty("filetypes",
				"xml,json,jsonl,csv,pdf,doc,docx,ppt,pptx,xls,xlsx,odt,odp,ods,ott,otp,ots,rtf,htm,html,txt,log");
		boolean commit = isOn(props.getProperty("commit", "yes"));
		boolean optimize = isOn(props.getProperty("optimize", "no"));

		return new SimplePostTool(mode, url, auto, type, format, recursive, delay, fileTypes, out, commit, optimize, args);
	}

	public SimplePostTool(String mode, URL url, boolean auto, String type, String format, int recursive, int delay,
			String fileTypes, OutputStream out, boolean commit, boolean optimize, String[] args) {
		this.mode = mode;
		this.solrUrl = url;
		this.auto = auto;
		this.type = type;
		this.format = format;
		this.recursive = recursive;
		this.delay = delay;
		this.fileTypes = fileTypes;
		this.fileFilter = getFileFilterFromFileTypes(fileTypes);
		this.out = out;
		this.commit = commit;
		this.optimize = optimize;
		this.args = args;
		pageFetcher = new PageFetcher();
	}

	private void doFilesMode() throws Exception {
		this.currentDepth = 0;

		if (!this.args[0].equals("-")) {
			info("Posting files to [base] url " + this.solrUrl
					+ (!this.auto ? (" using content-type " + ((this.type == null) ? "application/xml" : this.type))
							: "")
					+ "...");
			if (this.auto)
				info("Entering auto mode. File endings considered are " + this.fileTypes);
			if (this.recursive > 0)
				info("Entering recursive mode, max depth=" + this.recursive + ", delay=" + this.delay + "s");
			int numFilesPosted = postFiles(this.args, 0, this.out, this.type);
			info(numFilesPosted + " files indexed.");
		}
	}

	private void doArgsMode() throws Exception {
		info("POSTing args to " + this.solrUrl + "...");
		for (String a : this.args) {
			postData(stringToStream(a), null, this.out, this.type, this.solrUrl);
		}
	}

	private int doWebMode() throws Exception {
		reset();
		int numPagesPosted = 0;
		if (this.type != null) {
			throwMsg("Specifying content-type with \"-Ddata=web\" is not supported");
		}
		if (this.args[0].equals("-")) {
			return 0;
		}

		this.solrUrl = appendUrlPath(this.solrUrl, "/extract");

		info("Posting web pages to Solr url " + this.solrUrl);
		this.auto = true;
		info("Entering auto mode. Indexing pages with content-types corresponding to file endings "
				+ this.fileTypes);
		if (this.recursive > 0) {
			if (this.recursive > 10) {
				this.recursive = 10;
				warn("Too large recursion depth for web mode, limiting to 10...");
			}
			if (this.delay < 10)
				warn("Never crawl an external web site faster than every 10 seconds, your IP will probably be blocked");
			info("Entering recursive mode, depth=" + this.recursive + ", delay=" + this.delay + "s");
		}
		numPagesPosted = postWebPages(this.args, 0, this.out);
		info(numPagesPosted + " web pages indexed.");
		return numPagesPosted;
	}

	private void doStdinMode() throws Exception {
		info("POSTing stdin to " + this.solrUrl + "...");
		postData(System.in, null, this.out, this.type, this.solrUrl);
	}

	private void reset() {
		this.backlog = new ArrayList<>();
		this.visited = new HashSet<>();
	}

	private static void usageShort() {
		log.info(
				"Usage: java [SystemProperties] -jar post.jar [-h|-] [<file|folder|url|arg> [<file|folder|url|arg>...]]\n       Please invoke with -h option for extended usage help.");
	}

	private static void usage() {
		log.info(
				"Usage: java [SystemProperties] -jar post.jar [-h|-] [<file|folder|url|arg> [<file|folder|url|arg>...]]\n\nSupported System Properties and their defaults:\n  -Dc=<core/collection>\n  -Durl=<base Solr update URL> (overrides -Dc option if specified)\n  -Ddata=files|web|args|stdin (default=files)\n  -Dtype=<content-type> (default=application/xml)\n  -Dhost=<host> (default: localhost)\n  -Dport=<port> (default: 8983)\n  -Dbasicauth=<user:pass> (sets Basic Authentication credentials)\n  -Dauto=yes|no (default=no)\n  -Drecursive=yes|no|<depth> (default=0)\n  -Ddelay=<seconds> (default=0 for files, 10 for web)\n  -Dfiletypes=<type>[,<type>,...] (default=xml,json,jsonl,csv,pdf,doc,docx,ppt,pptx,xls,xlsx,odt,odp,ods,ott,otp,ots,rtf,htm,html,txt,log)\n  -Dparams=\"<key>=<value>[&<key>=<value>...]\" (values must be URL-encoded)\n  -Dcommit=yes|no (default=yes)\n  -Doptimize=yes|no (default=no)\n  -Dout=yes|no (default=no)\n\nThis is a simple command line tool for POSTing raw data to a Solr port.\nNOTE: Specifying the url/core/collection name is mandatory.\nData can be read from files specified as commandline args,\nURLs specified as args, as raw commandline arg strings or via STDIN.\nExamples:\n  java -Dc=gettingstarted -jar post.jar *.xml\n  java -Ddata=args -Dc=gettingstarted -jar post.jar '<delete><id>42</id></delete>'\n  java -Ddata=stdin -Dc=gettingstarted -jar post.jar < hd.xml\n  java -Ddata=web -Dc=gettingstarted -jar post.jar http://example.com/\n  java -Dtype=text/csv -Dc=gettingstarted -jar post.jar *.csv\n  java -Dtype=application/json -Dc=gettingstarted -jar post.jar *.json\n  java -Durl=http://localhost:8983/solr/techproducts/update/extract -Dparams=literal.id=pdf1 -jar post.jar solr-word.pdf\n  java -Dauto -Dc=gettingstarted -jar post.jar *\n  java -Dauto -Dc=gettingstarted -Drecursive -jar post.jar afolder\n  java -Dauto -Dc=gettingstarted -Dfiletypes=ppt,html -jar post.jar afolder\nThe options controlled by System Properties include the Solr\nURL to POST to, the Content-Type of the data, whether a commit\nor optimize should be executed, and whether the response should\nbe written to STDOUT. If auto=yes the tool will try to set type\nautomatically from file name. When posting rich documents the\nfile name will be propagated as \"resource.name\" and also used\nas \"literal.id\". You may override these or any other request parameter\nthrough the -Dparams property. To do a commit only, use \"-\" as argument.\nThe web mode is a simple crawler following links within domain, default delay=10s.");
	}

	public int postFiles(String[] args, int startIndexInArgs, OutputStream out, String type) throws Exception {
		reset();
		int filesPosted = 0;
		for (int j = startIndexInArgs; j < args.length; j++) {
			File srcFile = new File(args[j]);
			if (srcFile.isDirectory() && srcFile.canRead()) {
				filesPosted += postDirectory(srcFile, out, type);
			} else if (srcFile.isFile() && srcFile.canRead()) {
				filesPosted += postFiles(new File[] { srcFile }, out, type);
			} else {
				File parent = srcFile.getParentFile();
				if (parent == null)
					parent = new File(".");
				String fileGlob = srcFile.getName();
				GlobFileFilter ff = new GlobFileFilter(fileGlob, false);
				File[] files = parent.listFiles(ff);
				if (files == null || files.length == 0) {
					throwMsg("No files or directories matching " + srcFile);
				} else {

					filesPosted += postFiles(parent.listFiles(ff), out, type);
				}
			}
		}
		return filesPosted;
	}

	private int postFiles(File[] files, int startIndexInArgs, OutputStream out, String type) throws Exception {
		reset();
		int filesPosted = 0;
		for (File srcFile : files) {
			if (srcFile.isDirectory() && srcFile.canRead()) {
				filesPosted += postDirectory(srcFile, out, type);
			} else if (srcFile.isFile() && srcFile.canRead()) {
				filesPosted += postFiles(new File[] { srcFile }, out, type);
			} else {
				File parent = srcFile.getParentFile();
				if (parent == null)
					parent = new File(".");
				String fileGlob = srcFile.getName();
				GlobFileFilter ff = new GlobFileFilter(fileGlob, false);
				File[] fileList = parent.listFiles(ff);
				if (fileList == null || fileList.length == 0) {
					throwMsg("No files or directories matching " + srcFile);
				} else {

					filesPosted += postFiles(fileList, out, type);
				}
			}
		}
		return filesPosted;
	}

	private int postDirectory(File dir, OutputStream out, String type) throws Exception {
		if (dir.isHidden() && !dir.getName().equals("."))
			return 0;
		info("Indexing directory " + dir.getPath() + " (" + (dir.listFiles(this.fileFilter)).length + " files, depth="
				+ this.currentDepth + ")");
		int posted = 0;
		posted += postFiles(dir.listFiles(this.fileFilter), out, type);
		if (this.recursive > this.currentDepth) {
			for (File d : dir.listFiles()) {
				if (d.isDirectory()) {
					this.currentDepth++;
					posted += postDirectory(d, out, type);
					this.currentDepth--;
				}
			}
		}
		return posted;
	}

	int postFiles(File[] files, OutputStream out, String type) throws Exception {
		int filesPosted = 0;
		for (File srcFile : files) {
			if (srcFile.isFile() && !srcFile.isHidden()) {
				postFile(srcFile, out, type);
				Thread.sleep((this.delay * 1000));
				filesPosted++;
			}
		}
		return filesPosted;
	}

	public int postWebPages(String[] args, int startIndexInArgs, OutputStream out) throws Exception {
		reset();
		LinkedHashSet<URL> s = new LinkedHashSet<>();
		for (int j = startIndexInArgs; j < args.length; j++) {
			try {
				URL u = new URL(normalizeUrlEnding(args[j]));
				s.add(u);
			} catch (MalformedURLException e) {
				warn("Skipping malformed input URL: " + args[j]);
			}
		}

		this.backlog.add(s);
		return webCrawl(0, out);
	}

	protected static String normalizeUrlEnding(String link) {
		if (link.indexOf("#") > -1)
			link = link.substring(0, link.indexOf("#"));
		if (link.endsWith("?"))
			link = link.substring(0, link.length() - 1);
		if (link.endsWith("/"))
			link = link.substring(0, link.length() - 1);
		return link;
	}

	protected int webCrawl(int level, OutputStream out) throws Exception {
		int numPages = 0;
		LinkedHashSet<URL> stack = this.backlog.get(level);
		int rawStackSize = stack.size();
		stack.removeAll(this.visited);
		int stackSize = stack.size();
		LinkedHashSet<URL> subStack = new LinkedHashSet<>();
		info("Entering crawl at level " + level + " (" + rawStackSize + " links total, " + stackSize + " new)");
		for (URL u : stack) {
			try {
				this.visited.add(u);
				PageFetcherResult result = pageFetcher.readPageFromUrl(u);
				if (result.httpStatus == 200) {
					u = (result.redirectUrl != null) ? result.redirectUrl : u;
					URL postUrl = new URL(appendParam(this.solrUrl.toString(),
							"literal.id=" + URLEncoder.encode(u.toString(), "UTF-8") + "&literal.url="
									+ URLEncoder.encode(u.toString(), "UTF-8")));
					boolean success = postData(new ByteArrayInputStream(result.content.array(),
							result.content.arrayOffset(), result.content.limit()), null, out, result.contentType,
							postUrl);
					if (success) {
						info("POSTed web resource " + u + " (depth: " + level + ")");
						Thread.sleep((this.delay * 1000));
						numPages++;

						if (this.recursive > level && result.contentType.equals("text/html")) {
							Set<URL> children = pageFetcher
									.getLinksFromWebPage(u,
											new ByteArrayInputStream(result.content.array(),
													result.content.arrayOffset(), result.content.limit()),
											result.contentType, postUrl);
							subStack.addAll(children);
						}
						continue;
					}
					warn("An error occurred while posting " + u);
					continue;
				}
				warn("The URL " + u + " returned a HTTP result status of " + result.httpStatus);
			} catch (IOException e) {
				warn("Caught exception when trying to open connection to " + u + ": " + e.getMessage());
				throw e;
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		if (!subStack.isEmpty()) {
			this.backlog.add(subStack);
			numPages += webCrawl(level + 1, out);
		}
		return numPages;
	}

	public static class BAOS extends ByteArrayOutputStream {
		public ByteBuffer getByteBuffer() {
			return ByteBuffer.wrap(this.buf, 0, this.count);
		}
	}

	private static ByteBuffer inputStreamToByteArray(InputStream is) throws IOException {
		return inputStreamToByteArray(is, 2147483647L);
	}

	private static ByteBuffer inputStreamToByteArray(InputStream is, long maxSize) throws IOException {
		BAOS bos = new BAOS();
		long sz = 0L;
		int next = is.read();
		while (next > -1) {
			if (++sz > maxSize)
				throw new BufferOverflowException();
			bos.write(next);
			next = is.read();
		}
		bos.flush();
		is.close();
		return bos.getByteBuffer();
	}

	protected String computeFullUrl(URL baseUrl, String link) {
		if (link == null || link.length() == 0) {
			return null;
		}
		if (!link.startsWith("http")) {
			if (link.startsWith("/")) {
				link = baseUrl.getProtocol() + "://" + baseUrl.getAuthority() + link;
			} else {
				if (link.contains(":")) {
					return null;
				}
				String path = baseUrl.getPath();
				if (!path.endsWith("/")) {
					int sep = path.lastIndexOf("/");
					String file = path.substring(sep + 1);
					if (file.contains(".") || file.contains("?"))
						path = path.substring(0, sep);
				}
				link = baseUrl.getProtocol() + "://" + baseUrl.getAuthority() + path + "/" + link;
			}
		}
		link = normalizeUrlEnding(link);
		String l = link.toLowerCase(Locale.ROOT);

		if (l.endsWith(".jpg") || l.endsWith(".jpeg") || l.endsWith(".png") || l.endsWith(".gif")) {
			return null;
		}
		return link;
	}

	protected boolean typeSupported(String type) {
		for (String key : mimeMap.keySet()) {
			if (((String) mimeMap.get(key)).equals(type) && this.fileTypes.contains(key)) {
				return true;
			}
		}
		return false;
	}

	protected static boolean isOn(String property) {
		return ("true,on,yes,1".indexOf(property) > -1);
	}

	static void warn(String msg) {
		log.error("SimplePostTool: WARNING: " + msg);
	}

	static void info(String msg) {
		log.info(msg);
	}

	static void fatal(String msg) {
		log.error("SimplePostTool: FATAL: " + msg);
	}
	
	static void throwMsg(String msg) throws Exception {
		throw new Exception(msg);
	}

	private void commit() throws Exception {
		info("COMMITting Solr index changes to " + this.solrUrl + "...");
		doGet(appendParam(this.solrUrl.toString(), "commit=true"));
	}

	private void optimize() throws Exception {
		info("Performing an OPTIMIZE to " + this.solrUrl + "...");
		doGet(appendParam(this.solrUrl.toString(), "optimize=true"));
	}

	private static String appendParam(String url, String param) {
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

	private void postFile(File file, OutputStream output, String type) throws Exception {
		InputStream is = null;
		try {
			URL url = this.solrUrl;
			String suffix = "";
			if (this.auto) {
				if (type == null) {
					type = guessType(file);
				}

				if (type.equals("application/json") && !"solr".equals(this.format)) {
					suffix = "/json/docs";
					String urlStr = appendUrlPath(this.solrUrl, suffix).toString();
					url = new URL(urlStr);
				} else if (!type.equals("application/xml") 
						&& !type.equals("text/csv")
						&& !type.equals("application/json")) {

					suffix = "/extract";
					String urlStr = appendUrlPath(this.solrUrl, suffix).toString();
					if (urlStr.indexOf("resource.name") == -1)
						urlStr = appendParam(urlStr,
								"resource.name=" + URLEncoder.encode(file.getAbsolutePath(), "UTF-8"));
					if (urlStr.indexOf("literal.id") == -1)
						urlStr = appendParam(urlStr,
								"literal.id=" + URLEncoder.encode(file.getAbsolutePath(), "UTF-8"));
					url = new URL(urlStr);
				}
			} else if (type == null) {
				type = "application/xml";
			}

			info("POSTing file " + file.getName() + (this.auto ? (" (" + type + ")") : "") + " to [base]" + suffix);
			is = new FileInputStream(file);
			postData(is, Long.valueOf(file.length()), output, type, url);
		} catch (IOException e) {
			warn("Can't open/read file: " + file);
			throw e;
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
				fatal("IOException while closing file: " + e);
			}
		}
	}

	protected static URL appendUrlPath(URL url, String append) throws MalformedURLException {
		return new URL(url.getProtocol() + "://" + url.getAuthority() + url.getPath() + append
				+ ((url.getQuery() != null) ? ("?" + url.getQuery()) : ""));
	}

	protected static String guessType(File file) {
		String name = file.getName();
		String suffix = name.substring(name.lastIndexOf(".") + 1);
		String type = mimeMap.get(suffix.toLowerCase(Locale.ROOT));
		return (type != null) ? type : "application/octet-stream";
	}

	private static void doGet(String url) throws Exception {
		doGet(new URL(url));		
	}

	private static void doGet(URL url) throws Exception {
		if (mockMode)
			return;
		HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
		urlc.setConnectTimeout(CONNECT_TIME_OUT);
		urlc.setReadTimeout(READ_TIME_OUT);
		basicAuth(urlc);
		urlc.connect();
		checkResponseCode(urlc);
	}

	private boolean postData(InputStream data, Long length, OutputStream output, String type, URL url) throws Exception {
		if (mockMode)
			return true;
		boolean success = false;
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
			pipe(data, out);
			
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
		success = true;
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

	private static boolean checkResponseCode(HttpURLConnection urlc) throws IOException, GeneralSecurityException {
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

	private static InputStream stringToStream(String s) {
		return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
	}

	private static void pipe(InputStream source, OutputStream dest) throws IOException {
		byte[] buf = new byte[1024];
		int read = 0;
		while ((read = source.read(buf)) >= 0) {
			if (null != dest)
				dest.write(buf, 0, read);
		}
		if (null != dest)
			dest.flush();
	}

	private FileFilter getFileFilterFromFileTypes(String fileTypes) {
		String glob;
		if (fileTypes.equals("*")) {
			glob = ".*";
		} else {
			glob = "^.*\\.(" + fileTypes.replace(",", "|") + ")$";
		}
		return new GlobFileFilter(glob, true);
	}

	private static NodeList getNodesFromXP(Node n, String xpath) throws XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xp = factory.newXPath();
		XPathExpression expr = xp.compile(xpath);
		return (NodeList) expr.evaluate(n, XPathConstants.NODESET);
	}

	private static String getXP(Node n, String xpath, boolean concatAll) throws XPathExpressionException {
		NodeList nodes = getNodesFromXP(n, xpath);
		StringBuilder sb = new StringBuilder();
		if (nodes.getLength() > 0) {
			for (int i = 0; i < nodes.getLength(); i++) {
				sb.append(nodes.item(i).getNodeValue() + " ");
				if (!concatAll)
					break;
			}
			return sb.toString().trim();
		}
		return "";
	}

	private static Document makeDom(byte[] in) throws SAXException, IOException, ParserConfigurationException {
		InputStream is = new ByteArrayInputStream(in);

		Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
		return dom;
	}

	public SimplePostTool() {
	}

	class GlobFileFilter implements FileFilter {
		private String _pattern;
		private Pattern p;

		public GlobFileFilter(String pattern, boolean isRegex) {
			this._pattern = pattern;
			if (!isRegex) {
				this._pattern = this._pattern.replace("^", "\\^").replace("$", "\\$").replace(".", "\\.")
								.replace("(", "\\(").replace(")", "\\)").replace("+", "\\+").replace("*", ".*")
								.replace("?", ".");
				this._pattern = "^" + this._pattern + "$";
			}

			try {
				this.p = Pattern.compile(this._pattern, 2);
			} catch (PatternSyntaxException e) {
				SimplePostTool.fatal("Invalid type list " + pattern + ". " + e.getDescription());
			}
		}

		public boolean accept(File file) {
			return this.p.matcher(file.getName()).find();
		}
	}

	class PageFetcher {
		Map<String, List<String>> robotsCache;

		final String DISALLOW = "Disallow:";

		public PageFetcher() {
			this.robotsCache = new HashMap<>();
		}

		public SimplePostTool.PageFetcherResult readPageFromUrl(URL u) throws Exception {
			SimplePostTool.PageFetcherResult res = new SimplePostTool.PageFetcherResult();
			try {
				if (isDisallowedByRobots(u)) {
					SimplePostTool.warn("The URL " + u + " is disallowed by robots.txt and will not be crawled.");
					res.httpStatus = 403;
					SimplePostTool.this.visited.add(u);
					return res;
				}
				res.httpStatus = 404;
				HttpURLConnection conn = (HttpURLConnection) u.openConnection();
				conn.setRequestProperty("User-Agent", "SimplePostTool-crawler/5.0.0 (http://lucene.apache.org/solr/)");
				conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
				conn.connect();
				res.httpStatus = conn.getResponseCode();
				if (!SimplePostTool.normalizeUrlEnding(conn.getURL().toString())
						.equals(SimplePostTool.normalizeUrlEnding(u.toString()))) {
					SimplePostTool.info("The URL " + u + " caused a redirect to " + conn.getURL());
					u = conn.getURL();
					res.redirectUrl = u;
					SimplePostTool.this.visited.add(u);
				}
				if (res.httpStatus == 200) {

					String rawContentType = conn.getContentType();
					String type = rawContentType.split(";")[0];
					if (SimplePostTool.this.typeSupported(type) || "*".equals(SimplePostTool.this.fileTypes)) {
						InputStream is;
						String encoding = conn.getContentEncoding();

						if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
							is = new GZIPInputStream(conn.getInputStream());
						} else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
							is = new InflaterInputStream(conn.getInputStream(), new Inflater(true));
						} else {
							is = conn.getInputStream();
						}

						res.content = SimplePostTool.inputStreamToByteArray(is);
						is.close();
					} else {
						SimplePostTool.warn("Skipping URL with unsupported type " + type);
						res.httpStatus = 415;
					}
				}
			} catch (IOException e) {
				SimplePostTool.warn("IOException when reading page from url " + u + ": " + e.getMessage());
				throw e;
			}
			return res;
		}

		public boolean isDisallowedByRobots(URL url) throws Exception {
			String host = url.getHost();
			String strRobot = url.getProtocol() + "://" + host + "/robots.txt";
			List<String> disallows = this.robotsCache.get(host);
			if (disallows == null) {
				disallows = new ArrayList<>();

				URL urlRobot = new URL(strRobot);
				disallows = parseRobotsTxt(urlRobot.openStream());
			}

			this.robotsCache.put(host, disallows);

			String strURL = url.getFile();
			for (String path : disallows) {
				if (path.equals("/") || strURL.indexOf(path) == 0)
					return true;
			}
			return false;
		}

		protected List<String> parseRobotsTxt(InputStream is) throws IOException {
			List<String> disallows = new ArrayList<>();
			BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			String l;
			while ((l = r.readLine()) != null) {
				String[] arr = l.split("#");
				if (arr.length == 0)
					continue;
				l = arr[0].trim();
				if (l.startsWith("Disallow:")) {
					l = l.substring("Disallow:".length()).trim();
					if (l.length() == 0)
						continue;
					disallows.add(l);
				}
			}
			is.close();
			return disallows;
		}

		protected Set<URL> getLinksFromWebPage(URL u, InputStream is, String type, URL postUrl) throws Exception {
			Set<URL> l = new HashSet<>();
			URL url = null;
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			URL extractUrl = new URL(SimplePostTool.appendParam(postUrl.toString(), "extractOnly=true"));
			boolean success = SimplePostTool.this.postData(is, null, os, type, extractUrl);
			if (success) {
				Document d = SimplePostTool.makeDom(os.toByteArray());
				String innerXml = SimplePostTool.getXP(d, "/response/str/text()[1]", false);
				d = SimplePostTool.makeDom(innerXml.getBytes(StandardCharsets.UTF_8));
				NodeList links = SimplePostTool.getNodesFromXP(d, "/html/body//a/@href");
				for (int i = 0; i < links.getLength(); i++) {
					String link = links.item(i).getTextContent();
					link = SimplePostTool.this.computeFullUrl(u, link);
					if (link != null)

					{
						url = new URL(link);
						if (url.getAuthority() != null && url.getAuthority().equals(u.getAuthority())) {
							l.add(url);
						}
					}
				}
			}
			return l;
		}
	}

	public class PageFetcherResult {
		int httpStatus = 200;
		String contentType = "text/html";
		URL redirectUrl = null;
		ByteBuffer content;
	}
}
