/**
 * EngineMain.java	  V1.0   2019年12月25日 下午5:45:50
 *
 * Copyright (c) 2019 AsiaInfo, All rights reserved.
 *
 * Modification history(By    Time    Reason):
 *
 * Description:
 */

package org.jeecg.test;

import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.ai.modules.his.entity.HisMedicalFormalBusi;
import com.ai.modules.his.entity.HisMedicalFormalCaseBusi;
import com.ai.modules.his.service.IHisMedicalFormalBusiService;
import com.ai.modules.his.service.IHisMedicalFormalCaseBusiService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jxl.Workbook;
import jxl.write.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.modules.engine.EngineSpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.Boolean;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@SpringBootApplication(scanBasePackages = {"org.jeecg","com.ai"})
public class testMainBusi {

	public static void main(String[] args) {
		//SpringApplication.run(EngineMain.class, args);
		LocalDateTime startTime = LocalDateTime.now();
		try {
			new EngineSpringApplication(null, new Class<?>[] { testMainBusi.class }).run(args);
			ApplicationContext context = SpringContextUtils.getApplicationContext();
			IHisMedicalFormalCaseBusiService caseBusiService = context.getBean(IHisMedicalFormalCaseBusiService.class);
			IHisMedicalFormalBusiService busiService = context.getBean(IHisMedicalFormalBusiService.class);

			String batchId = "0a15fe20853b9b7686f41661f6f79c7f";

			Map<String, List<String>> busiCaseListMap = new HashMap<>();

			List<HisMedicalFormalCaseBusi> caseBusiList =  caseBusiService.list(new QueryWrapper<HisMedicalFormalCaseBusi>().eq("BATCH_ID", batchId));
			List<HisMedicalFormalBusi> busiList = busiService.list(new QueryWrapper<HisMedicalFormalBusi>().eq("BATCH_ID",batchId));

			for(HisMedicalFormalBusi bean: busiList){
				busiCaseListMap.put(bean.getBusiId(),new ArrayList<>());
			}

			for(HisMedicalFormalCaseBusi bean: caseBusiList){
				List<String> caseIdList = busiCaseListMap.get(bean.getBusiId());
				caseIdList.add(bean.getCaseId());
			}

			List<String> exprList = new ArrayList<>();
			for(HisMedicalFormalBusi bean: busiList){
				List<String> caseIdList = busiCaseListMap.get(bean.getBusiId());
				if (caseIdList.size() > 0){
					String q = "BATCH_ID:" + batchId + " AND CASE_ID:(\"" + StringUtils.join(caseIdList, "\",\"") + "\")";
					String timeJoin = "{!join from=VISITID fromIndex=DWB_MASTER_INFO to=VISITID}VISITDATE:2019*";
					String expr = "stats(" + EngineUtil.MEDICAL_UNREASONABLE_ACTION + ",q=\"" + q + "\",fq=\"" + timeJoin +"\"" +
							",count(*), sum(TOTALFEE))";
					exprList.add(expr);
				}

			}


			String path = "E:/ASIAProject/suncare_v3/excel/2019业务组结果统计-病例" + batchId +".xls";
			File file = new File(path);
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}

			String[] titles = {"业务名称","病例数","涉及总金额"};
			String[] fields = {"", "count(*)", "sum(TOTALFEE)"};
//        String[] titles = {"医疗机构编码","医疗机构名称", "出现次数", "涉及总金额"};
//        String[] fields = {"ORGID","ORGNAME", "count(*)", "sum(FEE)"};

			WritableCellFormat[] formats = {new WritableCellFormat(NumberFormats.TEXT),new WritableCellFormat(NumberFormats.TEXT),
					new WritableCellFormat(NumberFormats.FLOAT), new WritableCellFormat(NumberFormats.FLOAT)};
			WritableCellFormat textFormat = new WritableCellFormat(NumberFormats.TEXT);
			FileOutputStream os = new FileOutputStream(path);
			WritableWorkbook wwb = Workbook.createWorkbook(os);
			WritableSheet sheet = wwb.createSheet("sheet1", 0);
			AtomicInteger startHang = new AtomicInteger();
			for(int i = 0, len = titles.length; i < len; i++){
				sheet.addCell(new Label(i, startHang.get(), titles[i]));
			}

			int len = fields.length;

			String expr = "list(" + StringUtils.join(exprList, ",") +")";

			System.out.println("expr:" + expr);
			SolrUtil.stream(expr, (map, index) -> {
				Boolean eof = (Boolean)map.get("EOF");
				if(eof != null && eof){
					return;
				}
				HisMedicalFormalBusi bean = busiList.get(index);

				startHang.incrementAndGet();
				for(int i = 0; i < len; i++){
					Object val = i ==0?bean.getBusiName():map.get(fields[i]);
					if(val != null) {
						if(val instanceof Double || val instanceof Float){
							val = new BigDecimal(val.toString()).setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue();  ;
						}
						Label label =  new Label(i,startHang.get(), val.toString(),
								textFormat);
						try {
							sheet.addCell(label);
						} catch (WriteException e) {
							e.printStackTrace();
						}
					}

				}
			});

			wwb.write();
			wwb.close();

			os.close();
			System.exit(0);


		} catch(Exception e) {
			log.error("", e);
		} finally {
			LocalDateTime endTime = LocalDateTime.now();
			Duration duration = Duration.between(startTime, endTime);
			long seconds = duration.toMillis() / 1000;//相差毫秒数
			long minutes = seconds / 60;
			System.out.println("运行时长： "+minutes +"分钟，"+ seconds % 60+"秒 。");

			System.exit(0);
		}
	}

}
