package com.ai.modules.config.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.util.ExcelToPDF;
import org.jeecg.common.system.util.WordToPDF;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.ai.common.utils.ExcelTool;
import com.ai.common.utils.ExcelUtils;
import com.ai.common.utils.ExcelXUtils;
import com.ai.common.utils.ExportUtils;
import com.ai.common.utils.ExportXUtils;
import com.ai.common.utils.IdUtils;
import com.ai.modules.config.entity.MedicalPolicy;
import com.ai.modules.config.mapper.MedicalPolicyMapper;
import com.ai.modules.config.service.IMedicalOtherDictService;
import com.ai.modules.config.service.IMedicalPolicyService;
import com.ai.modules.config.vo.MedicalPolicyQuery;
import com.ai.modules.engine.util.SolrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * @Description: 新版本政策法规
 * @Author: jeecg-boot
 * @Date:   2021-08-04
 * @Version: V1.0
 */
@Service
public class MedicalPolicyServiceImpl extends ServiceImpl<MedicalPolicyMapper, MedicalPolicy>
implements IMedicalPolicyService {
    @Value("${jeecg.path.upload}")
    String UPLOAD_PATH;

    private final static String ADD_FLAG = "1";
    private final static String UPDATE_FLAG = "0";
    private final static String DEL_FLAG = "2";

    @Autowired
    private IMedicalOtherDictService medicalOtherDictService;

	public void deleteDocFromSolr(String id)throws Exception{
		SolrClient solrClient = SolrUtil.getSolrClient("MEDICAL_POLICY", "default",false);
		solrClient.deleteById(id);
		solrClient.commit();
	}

	//将文件转换成pdf
	private String converFileToPdf(String fileName) throws Exception{

		//如果文件路径为空，则不处理文件相关的内容
		if(StringUtils.isBlank(fileName))  {
			return "";
		}

		String filePath=UPLOAD_PATH + "/" +fileName;

		//如果文档本身就是pdf，则返回本身路径
		if(fileName.toLowerCase().endsWith(".pdf")) {
			return filePath;
		}

		//如果不是word或者excel，则不转换
		String converPdfPath="";

		//如果已经是doc路径，则转换成PDF
		if(filePath.toLowerCase().trim().endsWith(".doc") ||
				filePath.toLowerCase().trim().endsWith(".docx")) {

			int index = filePath.lastIndexOf(".");
			converPdfPath = filePath.substring(0,index) + ".pdf";

			try {
				//如果转换失败，则pdfPath设置成空
				WordToPDF.wordConverterToPdf(filePath, converPdfPath);
			} catch (Exception e) {
				converPdfPath="";
			}
		}
		//如果是文档是EXCEL，则转换成PDF
		else if(filePath.toLowerCase().trim().endsWith(".xls") ||
				filePath.toLowerCase().trim().endsWith(".xlsx")) {

			int index = filePath.lastIndexOf(".");
			converPdfPath = filePath.substring(0,index) + ".pdf";

			try {
				//如果转换失败，则pdfPath设置成空
				ExcelToPDF.excelConverterToPdf(filePath, converPdfPath);
			} catch (Exception e) {
				converPdfPath="";
			}
		}


		return converPdfPath;
	}

	/**
	 * 将政策法规内容存放到SOLR
	 */
	public void saveDocToSolr(MedicalPolicy medicalPolicyBean) throws Exception{
		SolrInputDocument doc = new SolrInputDocument();

		//根据当前的文件名，转换成pdf文件
		String converPdfPath = converFileToPdf(medicalPolicyBean.getFilenames());



		//如果PDF路径不为空，则解析PDF文件内容，同时TEXT_FILENAMES字段上，存放转换后的pdf路径
		if(StringUtils.isNotBlank(converPdfPath) ) {
			//读取PDF文件的内容
			String[]  pdfContents = readAllParagraphFromPdf(converPdfPath);

			if(pdfContents!= null && pdfContents.length>0) {
				for(String pdfContent : pdfContents) {
					doc.addField("FILE_CONTENT", pdfContent);
				}
			}

			String pdfFileName = converPdfPath = converPdfPath.substring(UPLOAD_PATH.length());
			doc.addField("TEXT_FILENAMES",pdfFileName);
			medicalPolicyBean.setTextFilenames(pdfFileName);
		}
		//如果没有转换成pdf，则存放原来附件的内容
		else if(StringUtils.isNotBlank(medicalPolicyBean.getFilenames()) ) {
			doc.addField("TEXT_FILENAMES",medicalPolicyBean.getFilenames());
			medicalPolicyBean.setTextFilenames(medicalPolicyBean.getFilenames());
		}

		doc.addField("id", medicalPolicyBean.getId());

        setDocUpdateFieldValue(doc,"POLICY_CODE",medicalPolicyBean.getPolicyCode());
        setDocUpdateFieldValue(doc,"POLICY_TYPE_CODE",medicalPolicyBean.getPolicyTypeCode());
        setDocUpdateFieldValue(doc,"POLICY_TYPE_NAME",medicalPolicyBean.getPolicyTypeName());
        setDocUpdateFieldValue(doc,"EFFECT_LEVEL_CODE",medicalPolicyBean.getEffectLevelCode());
        setDocUpdateFieldValue(doc,"EFFECT_LEVEL_NAME",medicalPolicyBean.getEffectLevelName());
        setDocUpdateFieldValue(doc,"POLICY_SERVICE_CLASS_CODE",medicalPolicyBean.getPolicyServiceClassCode());
        setDocUpdateFieldValue(doc,"POLICY_SERVICE_CLASS_NAME",medicalPolicyBean.getPolicyServiceClassName());
        setDocUpdateFieldValue(doc,"POLICY_NAME",medicalPolicyBean.getPolicyName());
        setDocUpdateFieldValue(doc,"POLICY_DOC_NUMBER",medicalPolicyBean.getPolicyDocNumber());
        setDocUpdateFieldValue(doc,"POLICY_DISCARD_DOC_NUMBER",medicalPolicyBean.getPolicyDiscardDocNumber());
        setDocUpdateFieldValue(doc,"ISSUING_OFFICE",medicalPolicyBean.getIssuingOffice());
        setDocUpdateFieldValue(doc,"ISSUING_OFFICE_AREA",medicalPolicyBean.getIssuingOfficeArea());


        setDocUpdateFieldValue(doc,"ISSUING_DATE",DateUtils.date2Str(medicalPolicyBean.getIssuingDate() ,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")) );
        setDocUpdateFieldValue(doc,"DOC_ORIGINAL_URL",medicalPolicyBean.getDocOriginalUrl());
        setDocUpdateFieldValue(doc,"APPLY_AREA",medicalPolicyBean.getApplyArea());
        setDocUpdateFieldValue(doc,"APPLY_PEOPLE",medicalPolicyBean.getApplyPeople());

        setDocUpdateFieldValue(doc,"EFFECT_STARTDATE",DateUtils.date2Str(medicalPolicyBean.getEffectStartdate() ,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")));
        setDocUpdateFieldValue(doc,"EFFECT_ENDDATE",DateUtils.date2Str(medicalPolicyBean.getEffectEnddate() ,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")));
        setDocUpdateFieldValue(doc,"DATA_STATUS",medicalPolicyBean.getDataStatus());
        setDocUpdateFieldValue(doc,"REMARK",medicalPolicyBean.getRemark());
        setDocUpdateFieldValue(doc,"CREATE_USER",medicalPolicyBean.getCreateUser());
        setDocUpdateFieldValue(doc,"CREATE_USERNAME",medicalPolicyBean.getCreateUsername());
        setDocUpdateFieldValue(doc,"CREATE_TIME",DateUtils.date2Str(medicalPolicyBean.getCreateTime() ,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")));
        setDocUpdateFieldValue(doc,"UPDATE_USER",medicalPolicyBean.getUpdateUser());
        setDocUpdateFieldValue(doc,"UPDATE_USERNAME",medicalPolicyBean.getUpdateUsername());
        setDocUpdateFieldValue(doc,"UPDATE_TIME",DateUtils.date2Str(medicalPolicyBean.getUpdateTime() ,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")));
        setDocUpdateFieldValue(doc,"FILENAMES",medicalPolicyBean.getFilenames());
        setDocUpdateFieldValue(doc,"APPLY_AREA_ID",medicalPolicyBean.getApplyAreaId());

        if(medicalPolicyBean.getIsOrder() != null) {
            setDocUpdateFieldValue(doc, "IS_ORDER", medicalPolicyBean.getIsOrder());
        }


        //将内容插入SOLR
		SolrClient solrClient = SolrUtil.getSolrClient("MEDICAL_POLICY", "default",false);
		solrClient.add(doc);
		solrClient.commit();
	}

    private void setDocUpdateFieldValue(SolrInputDocument doc ,String fieldName ,Object value){
        String newValue="";
        if (value != null){
            newValue ="" + value;
        }
        HashMap<String ,String> map = new HashMap<String ,String>();
        map.put("set", newValue);

        doc.addField(fieldName, map);
    }


	/**
	 * 从pdf读取每个段落的内容，在段落前面加 4位页码|
	 * @param pdfFilePath
	 * @return
	 * @throws IOException
	 */
    public   String[] readAllParagraphFromPdf(String pdfFilePath)throws IOException {
    	if(pdfFilePath == null || pdfFilePath.equals("")) {
			return new String[0];
		}


    	List<String> resultList = new ArrayList<>();

		PDDocument document = PDDocument.load(new File(pdfFilePath));

		// 获取页码
		int pages = document.getNumberOfPages();
		// 读文本内容
		PDFTextStripper stripper = new PDFTextStripper();

		//按页读取pdf文件内容
		for (int i = 1; i <= pages; i++) {
			stripper.setStartPage(i);
			stripper.setEndPage(i);
			String content = stripper.getText(document);

			if(content == null) {
				continue;
			}

			//将读取的内容分段
			String paragraphContents[] = content.split("\n");

			//获取4位页码，如果不足4位，左补空格
			String pageNo = "0000" + i;
			pageNo= pageNo.substring(pageNo.length()-4);

			//遍历段落内容
			for(String paragraphContent : paragraphContents) {
				if(paragraphContent == null) {
					continue;
				}

				//去空格
				paragraphContent = paragraphContent.trim();

				//忽略空行
				if("".equals(paragraphContent)) {
					continue;
				}

				//在内容前面加4位页码

				resultList.add(pageNo +"|" + paragraphContent);
			}

		}

		return resultList.toArray(new String[0]);
    }



    @Override
    public boolean exportExcel(List<MedicalPolicy> list, OutputStream os, String suffix) throws Exception {
        SimpleDateFormat date_sdf = new SimpleDateFormat("yyyy-MM-dd");
        String titleStr = "编号,信息类型,文件用途分类,效力级别,文件名称,政策文号,发文机关,发文机关所属地区,发布日期,适用开始时间,"+
        			"适用结束时间,适用人群,适用地区,数据状态,相对应废止的政策文号,创建人,创建时间,修改人,修改时间,网址";
        String[] titles = titleStr.split(",");
        String fieldStr = "policyCode,policyTypeName,policyServiceClassName,effectLevelName,policyName,policyDocNumber," +
        		"issuingOffice,issuingOfficeArea,issuingDate,effectStartdate,effectEnddate,applyPeople,applyArea," +
        		"dataStatus,policyDiscardDocNumber,createUsername,createTime,updateUsername,updateTime,docOriginalUrl";
        String[] fields = fieldStr.split(",");
//        List<MedicalPolicyBasisImport> exportList = new ArrayList<MedicalPolicyBasisImport>();
//        for (MedicalPolicy bean : list) {
//            MedicalPolicyBasisImport dataBean = new MedicalPolicyBasisImport();
//            BeanUtils.copyProperties(bean, dataBean);
//
//            //数据时间
//            dataBean.setStartEndDateStr(DateUtils.date2Str(bean.getEffectStartdate(),date_sdf)+"到"+DateUtils.date2Str(bean.getEffectEnddate(),date_sdf));
//            exportList.add(dataBean);
//        }
        if (ExcelTool.OFFICE_EXCEL_2010_POSTFIX.equals(suffix)) {
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            ExportXUtils.exportExl(list, MedicalPolicy.class, titles, fields, workbook, "政策法规");
            workbook.write(os);
            workbook.dispose();
        } else {
            // 创建文件输出流
            WritableWorkbook wwb = Workbook.createWorkbook(os);
            WritableSheet sheet = wwb.createSheet("政策法规", 0);
            ExportUtils.exportExl(list, MedicalPolicy.class, titles, fields, sheet, "");
            wwb.write();
            wwb.close();
        }
        return false;
    }


    @Override
    public Result<?> importExcel(MultipartFile file, LoginUser user) throws Exception {
        String mappingFieldStr = "policyCode,policyTypeName,policyServiceClassName,effectLevelName,policyName,policyDocNumber," +
        		"issuingOffice,issuingOfficeArea,issuingDate,effectStartdate,effectEnddate,applyPeople,applyArea," +
        		"dataStatus,policyDiscardDocNumber,docOriginalUrl,importActionType";//导入的字段
        String[] mappingFields = mappingFieldStr.split(",");

        System.out.println("开始导入时间："+DateUtils.now() );
        SimpleDateFormat date_sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<MedicalPolicyQuery> list = new ArrayList<>();
        String name = file.getOriginalFilename();
        if (name.endsWith(ExcelTool.POINT + ExcelTool.OFFICE_EXCEL_2010_POSTFIX)) {
            list = ExcelXUtils.readSheet(MedicalPolicyQuery.class, mappingFields, 0, 1, file.getInputStream());
        } else {
            list = ExcelUtils.readSheet(MedicalPolicyQuery.class, mappingFields, 0, 1, file.getInputStream());
        }

        if(list.size() == 0) {
            return Result.error("上传文件内容为空");
        }

        String message = "";
        System.out.println("校验开始："+DateUtils.now() );

        //更新标志(1新增0修改2删除)
        String[] importActionTypeArr = {"0","1","2"};

        //字典值检验
        List<MedicalPolicy> addUpdateList = new ArrayList<MedicalPolicy>();
        List<String> deleteList = new ArrayList<String>();//删除id

      //保证添加的政策编号也不能重复
        Set<String> policyCodeSet = new HashSet<String>();

        //循环校验数据
        for (int i = 0; i < list.size(); i++) {
            boolean flag = true;
            MedicalPolicyQuery beanVO = list.get(i);


            if (StringUtils.isBlank(beanVO.getPolicyCode())) {
                message += "导入数据中的第" + (i + 2) + "行数据“政策编号”为空\n";
                flag = false;
                continue;
            }

            //校验政策编码是否符合格式，SD-JN-0001-ZW
            String temp[] = beanVO.getPolicyCode().split("-");
            if(temp.length!=4) {
            	message += "导入的数据中的第" + (i + 2) + "行数据“政策编号”格式不符合要求\n";
                flag = false;
                continue;
            }

            if(policyCodeSet.contains(beanVO.getPolicyCode())) {
    		   message += "导入的数据中第" + (i + 2) + "行数据“政策编号”在文件中重复出现\n";
               flag = false;
               continue;
            }

            policyCodeSet.add(beanVO.getPolicyCode());

            if (StringUtils.isBlank(beanVO.getPolicyName())) {
                message += "导入的数据第" + (i + 2) + "行数据“名称”为空\n";
                flag = false;
                continue;
            }
            if (StringUtils.isBlank(beanVO.getImportActionType())) {
                message += "导入的数据中第" + (i + 2) + "行数据“更新标志”为空\n";
                flag = false;
                continue;
            }

            if (!Arrays.asList(importActionTypeArr).contains(beanVO.getImportActionType())) {
                message += "导入的数据中“更新标志”值不正确，如：第" + (i + 2) + "行数据\n";
                flag = false;
                continue;
            }

            //信息类型
            if(StringUtils.isNotBlank(beanVO.getPolicyTypeName())){
                beanVO.setPolicyTypeCode(medicalOtherDictService.getCodeByValue("rule_sourcetype", beanVO.getPolicyTypeName()));
            }

            //适用地区
            if(StringUtils.isNotBlank(beanVO.getApplyArea())){
                beanVO.setApplyAreaId(medicalOtherDictService.getCodeByValue("region", beanVO.getApplyArea()));
            }

            //文件用途分类 policy_service_class_name
            if(StringUtils.isNotBlank(beanVO.getPolicyServiceClassName())){
                beanVO.setPolicyServiceClassCode(medicalOtherDictService.getCodeByValue("service_class", beanVO.getPolicyServiceClassName()));
            }

            //效力级别 effect_level_name
            if(StringUtils.isNotBlank(beanVO.getEffectLevelName())){
                beanVO.setEffectLevelCode(medicalOtherDictService.getCodeByValue("effectiveness", beanVO.getEffectLevelName()));
            }

            //判断数据是否存在
            MedicalPolicy oldBean = this.getBeanByPolicyCode(beanVO.getPolicyCode());

            //如果数据存在并且是新增，则提示出错
            if (ADD_FLAG.equals(beanVO.getImportActionType()) &&
            		oldBean !=null) {
            	message += "第" + (i + 2) + "行数据的政策编号在库中已经存在！\n";
                flag = false;
            }

            else if (oldBean == null && (UPDATE_FLAG.equals(beanVO.getImportActionType())
            		|| DEL_FLAG.equals(beanVO.getImportActionType()))) {
            	message += "第" + (i + 2) + "行数据的政策编号在库中不存在！\n";
                flag = false;
            }


            if(!flag) {
                continue;
            }


            if (ADD_FLAG.equals(beanVO.getImportActionType())) {//新增
                beanVO.setId(IdUtils.uuid());
                beanVO.setCreateTime(DateUtils.getDate());
                beanVO.setCreateUser(user.getUsername());
                beanVO.setCreateUsername(user.getRealname());

                addUpdateList.add(beanVO);
            }
            else if (UPDATE_FLAG.equals(beanVO.getImportActionType()) ) {//修改
                beanVO.setId(oldBean.getId());
                beanVO.setUpdateTime(DateUtils.getDate());
                beanVO.setCreateUser(user.getUsername());
                beanVO.setCreateUsername(user.getRealname());
                addUpdateList.add(beanVO);
            }
            else if(DEL_FLAG.equals(beanVO.getImportActionType()) )  {//删除
            	 deleteList.add(oldBean.getId());
            }

            MedicalPolicy bean = beanVO;
        }

        if(StringUtils.isNotBlank(message)){
            message +="请核对数据后进行导入。";
            return Result.error(message);
        }

        //删除表，
        if (deleteList.size() > 0) {
            List<HashSet<String>> idSetList = getIdSetList(deleteList,1000);

            //循环删除数据，每1000条删除一次
            for (HashSet<String> idsSet : idSetList) {
                this.baseMapper.delete(new QueryWrapper<MedicalPolicy>().in("ID", idsSet));
            }

            for(String  deleteId :deleteList) {
            	this.deleteDocFromSolr(deleteId);
            }
        }

        //批量新增修改
        if (addUpdateList.size() > 0) {
            this.saveOrUpdateBatch(addUpdateList, 1000);//直接插入

            for(MedicalPolicy tempBean :addUpdateList) {
            	this.saveDocToSolr(tempBean);
            }

        }

        System.out.println("结束导入时间："+DateUtils.now() );
//            message += "导入成功，共导入"+list.size()+"条数据。";
        return Result.ok(message,list.size());

    }

    @Override
    @Transactional
    public void deleteByIds(List<String> idList) {
        List<HashSet<String>> idSetList = getIdSetList(idList,1000);
        if (idSetList.size() > 0) {
            for (HashSet<String> idsSet : idSetList) {
                //删除文件
                List<MedicalPolicy> list = this.baseMapper.selectList(new QueryWrapper<MedicalPolicy>()
                        .in("ID",idsSet));
                for(MedicalPolicy bean:list){
                    deleteFiles(bean);
                }
                this.removeByIds(idsSet);
            }
        }
    }


    private List<HashSet<String>> getIdSetList(List<String> idList, int size) {
        List<HashSet<String>> idSetList = new ArrayList<HashSet<String>>();
        HashSet<String> idSet = new HashSet<String>();

        for (String id : idList) {
            if (idSet.size() >= size) {
                idSetList.add(idSet);
                idSet = new HashSet<String>();
            }
            idSet.add(id);
        }
        if (idSet.size() > 0) {
            idSetList.add(idSet);
        }
        return idSetList;
    }


    //删除文件
    private void deleteFiles(MedicalPolicy bean) {
        if(StringUtils.isNotBlank(bean.getFilenames())){
            String[] filePaths = bean.getFilenames().split(",");
            for(String path: filePaths){
                File file = new File(UPLOAD_PATH+ File.separator +path);
                if(file.exists()) {
                    file.delete();
                }
            }
        }
    }


    @Override
    public boolean isExistName(String name, String id) {
        QueryWrapper<MedicalPolicy> queryWrapper = new QueryWrapper<MedicalPolicy>();
        queryWrapper.eq("NAME", name);
        if(StringUtils.isNotBlank(id)){
            queryWrapper.notIn("ID", id);
        }
        List<MedicalPolicy> list = this.baseMapper.selectList(queryWrapper);
        if(list != null && list.size()>0){
            return true;
        }
        return false;
    }


    @Override
    public MedicalPolicy getBeanByPolicyCode(String policyCode) {
        QueryWrapper<MedicalPolicy> queryWrapper = new QueryWrapper<MedicalPolicy>();
        queryWrapper.eq("POLICY_CODE", policyCode);

        List<MedicalPolicy> list = this.baseMapper.selectList(queryWrapper);
        if(list != null && list.size()>0){
            return list.get(0);
        }
        return null;
    }

    public List<Map<String ,Object>> queryPolicyDocContent(String policyId ,String searchContent ) throws Exception {
    	if(searchContent == null) {
    		return null;
    	}

    	searchContent = searchContent.trim();
    	if("".equals(searchContent)) {
    		return null;
    	}

    	SolrClient solrClient = SolrUtil.getSolrClient("MEDICAL_POLICY", "default",false);

    	SolrQuery solrQuery = new SolrQuery("id:" + policyId +
    			" AND FILE_CONTENT:*" + searchContent + "*");
		// 设定查询字段
		solrQuery.setStart(0);
		solrQuery.setRows(1);
		QueryResponse response = solrClient.query(solrQuery, METHOD.POST);

		//查不到记录则返回
		if( response.getResults().getNumFound()==0) {
			return null;
		}

		SolrDocument doc = response.getResults().iterator().next();

		Collection<Object> lines= doc.getFieldValues("FILE_CONTENT");

		//如果内容为空，则返回空
		if(lines == null || lines.size()==0) {
			return null;
		}

		ArrayList<Map<String ,Object>> resultList = new ArrayList<Map<String ,Object>>();

		Iterator<Object> it =  lines.iterator();
		while(it.hasNext()) {
			String line = it.next().toString();
			int index = line.indexOf(searchContent);
			if(index<0) {
				continue;
			}

			HashMap<String ,Object> map = new HashMap<String ,Object>();

			map.put("content", line.substring(5));
			map.put("page",Integer.parseInt( line.substring(0,4)));

			resultList.add(map);

		}

    	return resultList;
    }


}
