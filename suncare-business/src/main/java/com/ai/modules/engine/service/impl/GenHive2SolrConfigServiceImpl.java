package com.ai.modules.engine.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ai.modules.engine.service.GenHive2SolrConfigService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class GenHive2SolrConfigServiceImpl implements GenHive2SolrConfigService {
	private String user = "work";
	private String password = "";
	private String driver = "org.apache.hive.jdbc.HiveDriver";
	private String url = "jdbc:hive2://10.63.80.104:10000/medical";

	// configset文件夹
	@Value("${solr.solrConfigPath:home/web/data}")
	String configSetPath;
	@Autowired
	JdbcTemplate jdbcTemplate;

	/**
	 * 将通过DB规则引擎生成的数据，导入到SOLR中
	 */
	public void genSolrConfig(String name, String pk) {

		Connection conn = null;
		Statement stmt = null;
        ResultSet rs = null;
		try {
			log.info("driver={}; url={}; user={}; password={}", driver, url, user, password);
			Class.forName(driver);
	        conn = DriverManager.getConnection(url, user, password);
	        stmt = conn.createStatement();
	        
			String sql = "desc " + name;
			log.info(sql);			
			
			StringBuilder schemaSb = new StringBuilder(schemaPreText + "\n  <!-- 表字段 -->\n");
			StringBuilder importSb = new StringBuilder();
			// data-import 头
			importSb.append("<dataConfig>\n\t<dataSource type=\"JdbcDataSource\" driver=\"").append(driver)
					.append("\" url=\"").append(url).append("\" user=\"").append(user).append("\" password=\"")
					.append(password).append("\"/>\n").append("\t<document>\n").append("\t\t<entity name=\"")
					.append(name);
			
			StringBuilder importFieldsSb = new StringBuilder();
			// 拼接SQL语句
			importSb.append("\" query=\"SELECT ");
			importSb.append("\n\t\t").append(pk).append(" ID,");
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String col_name = rs.getString("col_name").toUpperCase();
				String data_type = rs.getString("data_type");
				String comment = rs.getString("comment");
				
				if("id".equalsIgnoreCase(col_name)) {
					continue;
				}
				if(StringUtils.isBlank(col_name)) {
					break;
				}
				importSb.append("\n\t\t").append(col_name).append(",");
				String solrFieldType = "string";
				if("int".equalsIgnoreCase(data_type)
						|| "bigint".equalsIgnoreCase(data_type)
						|| "tinyint".equalsIgnoreCase(data_type)
						|| "smallint".equalsIgnoreCase(data_type)) {
					solrFieldType = "int";
					if("dwb_master_info".equalsIgnoreCase(name)) {
						solrFieldType = "double";
					}
				} else if("double".equalsIgnoreCase(data_type)) {
					solrFieldType = "double";
				} else if("float".equalsIgnoreCase(data_type)) {
					solrFieldType = "float";
				}
				// 字段注释
				if (comment != null) {
					schemaSb.append("  <!-- ").append(comment).append(" -->\n");
					importFieldsSb.append("\t\t\t<!-- ").append(comment).append(" -->\n");
				}				
				// 字段，字段类型
				schemaSb.append("  <field name=\"").append(col_name).append("\" type=\"").append(solrFieldType)
						.append("\" multiValued=\"false\" indexed=\"true\" stored=\"false\"/>\n");
			
				importFieldsSb.append("\t\t\t<field name=\"").append(col_name).append("\" column=\"").append(col_name)
						.append("\"/>\n");
			}
						
			importSb.deleteCharAt(importSb.length()-1);
			importSb.append("\n\t\tFROM ").append(name).append(" t\">\n");
			
			importSb.append(importFieldsSb);
			
			//dwb_master_info与dws层的关联字段
			if("dwb_master_info".equalsIgnoreCase(name)) {
				InputStream is = this.getClass().getClassLoader().getResourceAsStream("templates/dwb_to_dws");
				InputStreamReader inputReader = new InputStreamReader(is,"UTF-8");
				BufferedReader bf = new BufferedReader(inputReader);
				// 按行读取字符串
		        String line;       
		        while ((line = bf.readLine()) != null) {        
		            schemaSb.append("  ").append(line).append("\n");
		        }
		        bf.close();
		        inputReader.close();
				
			}
			
			// managed-schema
			schemaSb.append("  <!-- 系统自带 -->\n"
					+ "  <field name=\"id\" type=\"string\" multiValued=\"false\" indexed=\"true\" required=\"true\" stored=\"true\"/>\n"
					+ "  <field name=\"_root_\" type=\"string\" docValues=\"false\" indexed=\"true\" stored=\"false\"/>\n"
					+ "  <field name=\"_text_\" type=\"text_general\" multiValued=\"true\" indexed=\"true\" stored=\"false\"/>\n"
					+ "  <field name=\"_version_\" type=\"long\" indexed=\"false\" stored=\"false\"/>\n");
			schemaSb.append(schemaAfterText);
			
			// data-import
			importSb.append("\t\t\t<!-- 系统字段 -->\n" + "\t\t\t<field name=\"id\" column=\"id\" />\n");
			// data-import 尾
			importSb.append("\t\t</entity>\n" + "\t</document>\n" + "</dataConfig>");
			
			String collectionConfigPath = configSetPath + name + "/conf";
			File file = new File(collectionConfigPath);
			
			if (!file.exists()) {
				boolean isMk = file.mkdirs();
				log.error("文件夹不存在，创建文件夹" + (isMk ? "成功" : "失败") + "，路径：" + collectionConfigPath);
			} else {
				log.error("文件夹已存在，路径：" + collectionConfigPath);
			}
			
			String defConfigPath = configSetPath+"/ywjzxx/conf";
			
			String cmd = "cp -rf " + defConfigPath + "/* " + collectionConfigPath + "/";
			
			log.info("cmd:" + cmd);
			
			String[] commands = { "/bin/sh", "-c", cmd };
			
			Process process = Runtime.getRuntime().exec(commands);
			process.waitFor();
			
			BufferedWriter importWriter = initOutFileWriter(collectionConfigPath + "/data-config.xml");
			importWriter.write(importSb.toString());
			importWriter.close();
			
			BufferedWriter schemaWriter = initOutFileWriter(collectionConfigPath + "/managed-schema");
			schemaWriter.write(schemaSb.toString());
			schemaWriter.close();
			
			log.error("生成完毕，路径：" + collectionConfigPath);

		} catch (Exception e) {
			log.error("", e);
		} finally {
			if ( rs != null) {
	            try {
					rs.close();
				} catch (SQLException e) {}
	        }
	        if (stmt != null) {
	            try {
					stmt.close();
				} catch (SQLException e) {}
	        }
	        if (conn != null) {
	            try {
					conn.close();
				} catch (SQLException e) {}
	        }
		}
	}

	private BufferedWriter initOutFileWriter(String path) throws IOException {
		// 输出到XML文件
		File outFile = new File(path);
		if (outFile.exists()) {
			outFile.delete();
		}

		// 创建xml父级文件夹
		if (!outFile.getParentFile().exists()) {
			outFile.getParentFile().mkdirs();
		}
		BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(FileUtils.openOutputStream(outFile), Charset.forName("utf8")));

		return writer;

	}

	private static String schemaPreText = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<!-- Solr managed schema - automatically generated - DO NOT EDIT -->\n"
			+ "<schema name=\"example-basic\" version=\"1.6\">\n" + "  <uniqueKey>id</uniqueKey>\n"
			+ "  <fieldType name=\"ancestor_path\" class=\"solr.TextField\">\n" + "    <analyzer type=\"index\">\n"
			+ "      <tokenizer class=\"solr.KeywordTokenizerFactory\"/>\n" + "    </analyzer>\n"
			+ "    <analyzer type=\"query\">\n"
			+ "      <tokenizer class=\"solr.PathHierarchyTokenizerFactory\" delimiter=\"/\"/>\n" + "    </analyzer>\n"
			+ "  </fieldType>\n" + "  <fieldType name=\"binary\" class=\"solr.BinaryField\"/>\n"
			+ "  <fieldType name=\"boolean\" class=\"solr.BoolField\" sortMissingLast=\"true\"/>\n"
			+ "  <fieldType name=\"booleans\" class=\"solr.BoolField\" sortMissingLast=\"true\" multiValued=\"true\"/>\n"
			+ "  <fieldType name=\"currency\" class=\"solr.CurrencyField\" currencyConfig=\"currency.xml\" defaultCurrency=\"USD\" precisionStep=\"8\"/>\n"
			+ "  <fieldType name=\"date\" class=\"solr.TrieDateField\" positionIncrementGap=\"0\" docValues=\"true\" precisionStep=\"0\"/>\n"
			+ "  <fieldType name=\"dates\" class=\"solr.TrieDateField\" positionIncrementGap=\"0\" docValues=\"true\" multiValued=\"true\" precisionStep=\"0\"/>\n"
			+ "  <fieldType name=\"descendent_path\" class=\"solr.TextField\">\n" + "    <analyzer type=\"index\">\n"
			+ "      <tokenizer class=\"solr.PathHierarchyTokenizerFactory\" delimiter=\"/\"/>\n" + "    </analyzer>\n"
			+ "    <analyzer type=\"query\">\n" + "      <tokenizer class=\"solr.KeywordTokenizerFactory\"/>\n"
			+ "    </analyzer>\n" + "  </fieldType>\n"
			+ "  <fieldType name=\"double\" class=\"solr.TrieDoubleField\" positionIncrementGap=\"0\" docValues=\"true\" precisionStep=\"0\"/>\n"
			+ "  <fieldType name=\"doubles\" class=\"solr.TrieDoubleField\" positionIncrementGap=\"0\" docValues=\"true\" multiValued=\"true\" precisionStep=\"0\"/>\n"
			+ "  <fieldType name=\"float\" class=\"solr.TrieFloatField\" positionIncrementGap=\"0\" docValues=\"true\" precisionStep=\"0\"/>\n"
			+ "  <fieldType name=\"floats\" class=\"solr.TrieFloatField\" positionIncrementGap=\"0\" docValues=\"true\" multiValued=\"true\" precisionStep=\"0\"/>\n"
			+ "  <fieldType name=\"ignored\" class=\"solr.StrField\" indexed=\"false\" stored=\"false\" docValues=\"false\" multiValued=\"true\"/>\n"
			+ "  <fieldType name=\"int\" class=\"solr.TrieIntField\" positionIncrementGap=\"0\" docValues=\"true\" precisionStep=\"0\"/>\n"
			+ "  <fieldType name=\"ints\" class=\"solr.TrieIntField\" positionIncrementGap=\"0\" docValues=\"true\" multiValued=\"true\" precisionStep=\"0\"/>\n"
			+ "  <fieldType name=\"location\" class=\"solr.LatLonType\" subFieldSuffix=\"_coordinate\"/>\n"
			+ "  <fieldType name=\"location_rpt\" class=\"solr.SpatialRecursivePrefixTreeFieldType\" geo=\"true\" maxDistErr=\"0.001\" distErrPct=\"0.025\" distanceUnits=\"kilometers\"/>\n"
			+ "  <fieldType name=\"long\" class=\"solr.TrieLongField\" positionIncrementGap=\"0\" docValues=\"true\" precisionStep=\"0\"/>\n"
			+ "  <fieldType name=\"longs\" class=\"solr.TrieLongField\" positionIncrementGap=\"0\" docValues=\"true\" multiValued=\"true\" precisionStep=\"0\"/>\n"
			+ "  <fieldType name=\"lowercase\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.KeywordTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n" + "    </analyzer>\n" + "  </fieldType>\n"
			+ "  <fieldType name=\"phonetic_en\" class=\"solr.TextField\" indexed=\"true\" stored=\"false\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.DoubleMetaphoneFilterFactory\" inject=\"false\"/>\n" + "    </analyzer>\n"
			+ "  </fieldType>\n"
			+ "  <fieldType name=\"point\" class=\"solr.PointType\" subFieldSuffix=\"_d\" dimension=\"2\"/>\n"
			+ "  <fieldType name=\"random\" class=\"solr.RandomSortField\" indexed=\"true\"/>\n"
			+ "  <fieldType name=\"string\" class=\"solr.StrField\" sortMissingLast=\"true\" docValues=\"true\"/>\n"
			+ "  <fieldType name=\"strings\" class=\"solr.StrField\" sortMissingLast=\"true\" docValues=\"true\" multiValued=\"true\"/>\n"
			+ "  <fieldType name=\"tdate\" class=\"solr.TrieDateField\" positionIncrementGap=\"0\" docValues=\"true\" precisionStep=\"6\"/>\n"
			+ "  <fieldType name=\"tdates\" class=\"solr.TrieDateField\" positionIncrementGap=\"0\" docValues=\"true\" multiValued=\"true\" precisionStep=\"6\"/>\n"
			+ "  <fieldType name=\"tdouble\" class=\"solr.TrieDoubleField\" positionIncrementGap=\"0\" docValues=\"true\" precisionStep=\"8\"/>\n"
			+ "  <fieldType name=\"tdoubles\" class=\"solr.TrieDoubleField\" positionIncrementGap=\"0\" docValues=\"true\" multiValued=\"true\" precisionStep=\"8\"/>\n"
			+ "  <fieldType name=\"text_ar\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"lang/stopwords_ar.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.ArabicNormalizationFilterFactory\"/>\n"
			+ "      <filter class=\"solr.ArabicStemFilterFactory\"/>\n" + "    </analyzer>\n" + "  </fieldType>\n"
			+ "  <fieldType name=\"text_bg\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"lang/stopwords_bg.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.BulgarianStemFilterFactory\"/>\n" + "    </analyzer>\n" + "  </fieldType>\n"
			+ "  <fieldType name=\"text_ca\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.ElisionFilterFactory\" articles=\"lang/contractions_ca.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"lang/stopwords_ca.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.SnowballPorterFilterFactory\" language=\"Catalan\"/>\n" + "    </analyzer>\n"
			+ "  </fieldType>\n"
			+ "  <fieldType name=\"text_cjk\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.CJKWidthFilterFactory\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.CJKBigramFilterFactory\"/>\n" + "    </analyzer>\n" + "  </fieldType>\n"
			+ "  <fieldType name=\"text_cz\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"lang/stopwords_cz.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.CzechStemFilterFactory\"/>\n" + "    </analyzer>\n" + "  </fieldType>\n"
			+ "  <fieldType name=\"text_da\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" format=\"snowball\" words=\"lang/stopwords_da.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.SnowballPorterFilterFactory\" language=\"Danish\"/>\n" + "    </analyzer>\n"
			+ "  </fieldType>\n"
			+ "  <fieldType name=\"text_de\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" format=\"snowball\" words=\"lang/stopwords_de.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.GermanNormalizationFilterFactory\"/>\n"
			+ "      <filter class=\"solr.GermanLightStemFilterFactory\"/>\n" + "    </analyzer>\n" + "  </fieldType>\n"
			+ "  <fieldType name=\"text_el\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.GreekLowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"lang/stopwords_el.txt\" ignoreCase=\"false\"/>\n"
			+ "      <filter class=\"solr.GreekStemFilterFactory\"/>\n" + "    </analyzer>\n" + "  </fieldType>\n"
			+ "  <fieldType name=\"text_en\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer type=\"index\">\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"lang/stopwords_en.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.EnglishPossessiveFilterFactory\"/>\n"
			+ "      <filter class=\"solr.KeywordMarkerFilterFactory\" protected=\"protwords.txt\"/>\n"
			+ "      <filter class=\"solr.PorterStemFilterFactory\"/>\n" + "    </analyzer>\n"
			+ "    <analyzer type=\"query\">\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.SynonymFilterFactory\" expand=\"true\" ignoreCase=\"true\" synonyms=\"synonyms.txt\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"lang/stopwords_en.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.EnglishPossessiveFilterFactory\"/>\n"
			+ "      <filter class=\"solr.KeywordMarkerFilterFactory\" protected=\"protwords.txt\"/>\n"
			+ "      <filter class=\"solr.PorterStemFilterFactory\"/>\n" + "    </analyzer>\n" + "  </fieldType>\n"
			+ "  <fieldType name=\"text_en_splitting\" class=\"solr.TextField\" autoGeneratePhraseQueries=\"true\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer type=\"index\">\n" + "      <tokenizer class=\"solr.WhitespaceTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"lang/stopwords_en.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.WordDelimiterFilterFactory\" catenateNumbers=\"1\" generateNumberParts=\"1\" splitOnCaseChange=\"1\" generateWordParts=\"1\" catenateAll=\"0\" catenateWords=\"1\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.KeywordMarkerFilterFactory\" protected=\"protwords.txt\"/>\n"
			+ "      <filter class=\"solr.PorterStemFilterFactory\"/>\n" + "    </analyzer>\n"
			+ "    <analyzer type=\"query\">\n" + "      <tokenizer class=\"solr.WhitespaceTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.SynonymFilterFactory\" expand=\"true\" ignoreCase=\"true\" synonyms=\"synonyms.txt\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"lang/stopwords_en.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.WordDelimiterFilterFactory\" catenateNumbers=\"0\" generateNumberParts=\"1\" splitOnCaseChange=\"1\" generateWordParts=\"1\" catenateAll=\"0\" catenateWords=\"0\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.KeywordMarkerFilterFactory\" protected=\"protwords.txt\"/>\n"
			+ "      <filter class=\"solr.PorterStemFilterFactory\"/>\n" + "    </analyzer>\n" + "  </fieldType>\n"
			+ "  <fieldType name=\"text_en_splitting_tight\" class=\"solr.TextField\" autoGeneratePhraseQueries=\"true\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.WhitespaceTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.SynonymFilterFactory\" expand=\"false\" ignoreCase=\"true\" synonyms=\"synonyms.txt\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"lang/stopwords_en.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.WordDelimiterFilterFactory\" catenateNumbers=\"1\" generateNumberParts=\"0\" generateWordParts=\"0\" catenateAll=\"0\" catenateWords=\"1\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.KeywordMarkerFilterFactory\" protected=\"protwords.txt\"/>\n"
			+ "      <filter class=\"solr.EnglishMinimalStemFilterFactory\"/>\n"
			+ "      <filter class=\"solr.RemoveDuplicatesTokenFilterFactory\"/>\n" + "    </analyzer>\n"
			+ "  </fieldType>\n"
			+ "  <fieldType name=\"text_es\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" format=\"snowball\" words=\"lang/stopwords_es.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.SpanishLightStemFilterFactory\"/>\n" + "    </analyzer>\n"
			+ "  </fieldType>\n"
			+ "  <fieldType name=\"text_eu\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"lang/stopwords_eu.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.SnowballPorterFilterFactory\" language=\"Basque\"/>\n" + "    </analyzer>\n"
			+ "  </fieldType>\n"
			+ "  <fieldType name=\"text_fa\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <charFilter class=\"solr.PersianCharFilterFactory\"/>\n"
			+ "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.ArabicNormalizationFilterFactory\"/>\n"
			+ "      <filter class=\"solr.PersianNormalizationFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"lang/stopwords_fa.txt\" ignoreCase=\"true\"/>\n"
			+ "    </analyzer>\n" + "  </fieldType>\n"
			+ "  <fieldType name=\"text_fi\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" format=\"snowball\" words=\"lang/stopwords_fi.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.SnowballPorterFilterFactory\" language=\"Finnish\"/>\n" + "    </analyzer>\n"
			+ "  </fieldType>\n"
			+ "  <fieldType name=\"text_fr\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.ElisionFilterFactory\" articles=\"lang/contractions_fr.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" format=\"snowball\" words=\"lang/stopwords_fr.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.FrenchLightStemFilterFactory\"/>\n" + "    </analyzer>\n" + "  </fieldType>\n"
			+ "  <fieldType name=\"text_ga\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.ElisionFilterFactory\" articles=\"lang/contractions_ga.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"lang/hyphenations_ga.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.IrishLowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"lang/stopwords_ga.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.SnowballPorterFilterFactory\" language=\"Irish\"/>\n" + "    </analyzer>\n"
			+ "  </fieldType>\n"
			+ "  <fieldType name=\"text_general\" class=\"solr.TextField\" positionIncrementGap=\"100\" multiValued=\"true\">\n"
			+ "    <analyzer type=\"index\">\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"stopwords.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n" + "    </analyzer>\n"
			+ "    <analyzer type=\"query\">\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"stopwords.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.SynonymFilterFactory\" expand=\"true\" ignoreCase=\"true\" synonyms=\"synonyms.txt\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n" + "    </analyzer>\n" + "  </fieldType>\n"
			+ "  <fieldType name=\"text_general_rev\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer type=\"index\">\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"stopwords.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.ReversedWildcardFilterFactory\" maxPosQuestion=\"2\" maxFractionAsterisk=\"0.33\" maxPosAsterisk=\"3\" withOriginal=\"true\"/>\n"
			+ "    </analyzer>\n" + "    <analyzer type=\"query\">\n"
			+ "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.SynonymFilterFactory\" expand=\"true\" ignoreCase=\"true\" synonyms=\"synonyms.txt\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"stopwords.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n" + "    </analyzer>\n" + "  </fieldType>\n"
			+ "  <fieldType name=\"text_gl\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"lang/stopwords_gl.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.GalicianStemFilterFactory\"/>\n" + "    </analyzer>\n" + "  </fieldType>\n"
			+ "  <fieldType name=\"text_hi\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.IndicNormalizationFilterFactory\"/>\n"
			+ "      <filter class=\"solr.HindiNormalizationFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"lang/stopwords_hi.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.HindiStemFilterFactory\"/>\n" + "    </analyzer>\n" + "  </fieldType>\n"
			+ "  <fieldType name=\"text_hu\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" format=\"snowball\" words=\"lang/stopwords_hu.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.SnowballPorterFilterFactory\" language=\"Hungarian\"/>\n"
			+ "    </analyzer>\n" + "  </fieldType>\n"
			+ "  <fieldType name=\"text_hy\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"lang/stopwords_hy.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.SnowballPorterFilterFactory\" language=\"Armenian\"/>\n" + "    </analyzer>\n"
			+ "  </fieldType>\n"
			+ "  <fieldType name=\"text_id\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"lang/stopwords_id.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.IndonesianStemFilterFactory\" stemDerivational=\"true\"/>\n"
			+ "    </analyzer>\n" + "  </fieldType>\n"
			+ "  <fieldType name=\"text_it\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.ElisionFilterFactory\" articles=\"lang/contractions_it.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" format=\"snowball\" words=\"lang/stopwords_it.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.ItalianLightStemFilterFactory\"/>\n" + "    </analyzer>\n"
			+ "  </fieldType>\n"
			+ "  <fieldType name=\"text_ja\" class=\"solr.TextField\" autoGeneratePhraseQueries=\"false\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.JapaneseTokenizerFactory\" mode=\"search\"/>\n"
			+ "      <filter class=\"solr.JapaneseBaseFormFilterFactory\"/>\n"
			+ "      <filter class=\"solr.JapanesePartOfSpeechStopFilterFactory\" tags=\"lang/stoptags_ja.txt\"/>\n"
			+ "      <filter class=\"solr.CJKWidthFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"lang/stopwords_ja.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.JapaneseKatakanaStemFilterFactory\" minimumLength=\"4\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n" + "    </analyzer>\n" + "  </fieldType>\n"
			+ "  <fieldType name=\"text_lv\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"lang/stopwords_lv.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.LatvianStemFilterFactory\"/>\n" + "    </analyzer>\n" + "  </fieldType>\n"
			+ "  <fieldType name=\"text_nl\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" format=\"snowball\" words=\"lang/stopwords_nl.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.StemmerOverrideFilterFactory\" dictionary=\"lang/stemdict_nl.txt\" ignoreCase=\"false\"/>\n"
			+ "      <filter class=\"solr.SnowballPorterFilterFactory\" language=\"Dutch\"/>\n" + "    </analyzer>\n"
			+ "  </fieldType>\n"
			+ "  <fieldType name=\"text_no\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" format=\"snowball\" words=\"lang/stopwords_no.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.SnowballPorterFilterFactory\" language=\"Norwegian\"/>\n"
			+ "    </analyzer>\n" + "  </fieldType>\n"
			+ "  <fieldType name=\"text_pt\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" format=\"snowball\" words=\"lang/stopwords_pt.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.PortugueseLightStemFilterFactory\"/>\n" + "    </analyzer>\n"
			+ "  </fieldType>\n"
			+ "  <fieldType name=\"text_ro\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"lang/stopwords_ro.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.SnowballPorterFilterFactory\" language=\"Romanian\"/>\n" + "    </analyzer>\n"
			+ "  </fieldType>\n"
			+ "  <fieldType name=\"text_ru\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" format=\"snowball\" words=\"lang/stopwords_ru.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.SnowballPorterFilterFactory\" language=\"Russian\"/>\n" + "    </analyzer>\n"
			+ "  </fieldType>\n"
			+ "  <fieldType name=\"text_sv\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" format=\"snowball\" words=\"lang/stopwords_sv.txt\" ignoreCase=\"true\"/>\n"
			+ "      <filter class=\"solr.SnowballPorterFilterFactory\" language=\"Swedish\"/>\n" + "    </analyzer>\n"
			+ "  </fieldType>\n"
			+ "  <fieldType name=\"text_th\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.ThaiTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.LowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"lang/stopwords_th.txt\" ignoreCase=\"true\"/>\n"
			+ "    </analyzer>\n" + "  </fieldType>\n"
			+ "  <fieldType name=\"text_tr\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n"
			+ "      <filter class=\"solr.TurkishLowerCaseFilterFactory\"/>\n"
			+ "      <filter class=\"solr.StopFilterFactory\" words=\"lang/stopwords_tr.txt\" ignoreCase=\"false\"/>\n"
			+ "      <filter class=\"solr.SnowballPorterFilterFactory\" language=\"Turkish\"/>\n" + "    </analyzer>\n"
			+ "  </fieldType>\n"
			+ "  <fieldType name=\"text_ws\" class=\"solr.TextField\" positionIncrementGap=\"100\">\n"
			+ "    <analyzer>\n" + "      <tokenizer class=\"solr.WhitespaceTokenizerFactory\"/>\n"
			+ "    </analyzer>\n" + "  </fieldType>\n"
			+ "  <fieldType name=\"tfloat\" class=\"solr.TrieFloatField\" positionIncrementGap=\"0\" docValues=\"true\" precisionStep=\"8\"/>\n"
			+ "  <fieldType name=\"tfloats\" class=\"solr.TrieFloatField\" positionIncrementGap=\"0\" docValues=\"true\" multiValued=\"true\" precisionStep=\"8\"/>\n"
			+ "  <fieldType name=\"tint\" class=\"solr.TrieIntField\" positionIncrementGap=\"0\" docValues=\"true\" precisionStep=\"8\"/>\n"
			+ "  <fieldType name=\"tints\" class=\"solr.TrieIntField\" positionIncrementGap=\"0\" docValues=\"true\" multiValued=\"true\" precisionStep=\"8\"/>\n"
			+ "  <fieldType name=\"tlong\" class=\"solr.TrieLongField\" positionIncrementGap=\"0\" docValues=\"true\" precisionStep=\"8\"/>\n"
			+ "  <fieldType name=\"tlongs\" class=\"solr.TrieLongField\" positionIncrementGap=\"0\" docValues=\"true\" multiValued=\"true\" precisionStep=\"8\"/>\n";

	private static String schemaAfterText = "\n  <dynamicField name=\"*_txt_en_split_tight\" type=\"text_en_splitting_tight\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_descendent_path\" type=\"descendent_path\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_ancestor_path\" type=\"ancestor_path\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_en_split\" type=\"text_en_splitting\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_coordinate\" type=\"tdouble\" indexed=\"true\" stored=\"false\" useDocValuesAsStored=\"false\"/>\n"
			+ "  <dynamicField name=\"ignored_*\" type=\"ignored\" multiValued=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_rev\" type=\"text_general_rev\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_phon_en\" type=\"phonetic_en\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_s_lower\" type=\"lowercase\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_cjk\" type=\"text_cjk\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"random_*\" type=\"random\"/>\n"
			+ "  <dynamicField name=\"*_txt_en\" type=\"text_en\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_ar\" type=\"text_ar\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_bg\" type=\"text_bg\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_ca\" type=\"text_ca\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_cz\" type=\"text_cz\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_da\" type=\"text_da\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_de\" type=\"text_de\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_el\" type=\"text_el\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_es\" type=\"text_es\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_eu\" type=\"text_eu\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_fa\" type=\"text_fa\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_fi\" type=\"text_fi\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_fr\" type=\"text_fr\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_ga\" type=\"text_ga\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_gl\" type=\"text_gl\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_hi\" type=\"text_hi\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_hu\" type=\"text_hu\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_hy\" type=\"text_hy\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_id\" type=\"text_id\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_it\" type=\"text_it\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_ja\" type=\"text_ja\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_lv\" type=\"text_lv\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_nl\" type=\"text_nl\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_no\" type=\"text_no\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_pt\" type=\"text_pt\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_ro\" type=\"text_ro\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_ru\" type=\"text_ru\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_sv\" type=\"text_sv\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_th\" type=\"text_th\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt_tr\" type=\"text_tr\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_point\" type=\"point\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_srpt\" type=\"location_rpt\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_tdts\" type=\"tdates\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"attr_*\" type=\"text_general\" multiValued=\"true\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_txt\" type=\"text_general\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_dts\" type=\"date\" multiValued=\"true\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_tis\" type=\"tints\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_tls\" type=\"tlongs\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_tfs\" type=\"tfloats\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_tds\" type=\"tdoubles\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_tdt\" type=\"tdate\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_is\" type=\"ints\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_ss\" type=\"strings\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_ls\" type=\"longs\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_bs\" type=\"booleans\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_fs\" type=\"floats\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_ds\" type=\"doubles\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_dt\" type=\"date\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_ti\" type=\"tint\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_tl\" type=\"tlong\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_tf\" type=\"tfloat\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_td\" type=\"tdouble\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_ws\" type=\"text_ws\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_i\" type=\"int\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_s\" type=\"string\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_l\" type=\"long\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_t\" type=\"text_general\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_b\" type=\"boolean\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_f\" type=\"float\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_d\" type=\"double\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_p\" type=\"location\" indexed=\"true\" stored=\"true\"/>\n"
			+ "  <dynamicField name=\"*_c\" type=\"currency\" indexed=\"true\" stored=\"true\"/>\n" + "</schema>";
}
