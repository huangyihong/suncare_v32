package com.ai.modules.action.service.impl;

import com.ai.common.utils.StringUtil;
import com.ai.modules.action.entity.MedicalBreakDrugAction;
import com.ai.modules.action.mapper.MedicalBreakDrugActionMapper;
import com.ai.modules.action.service.IMedicalBreakDrugActionService;
import com.ai.modules.engine.util.EngineUtil;
import com.ai.modules.engine.util.SolrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.SqlInjectionUtil;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: 不合规结果
 * @Author: jeecg-boot
 * @Date:   2020-01-19
 * @Version: V1.0
 */
@Service
public class MedicalBreakDrugActionServiceImpl extends ServiceImpl<MedicalBreakDrugActionMapper, MedicalBreakDrugAction> implements IMedicalBreakDrugActionService {

    @Override
    public IPage<MedicalBreakDrugAction> pageSolr(Page<MedicalBreakDrugAction> page, MedicalBreakDrugAction medicalBreakDrugAction, HttpServletRequest req) throws Exception {

        SolrQuery query = new SolrQuery();
        // 设定查询字段
        String q = "*:*";
        query.add("q", q);
        query.addFilterQuery(getSolrFq(medicalBreakDrugAction).toArray(new String[0]));
        query.set("sort", StringUtil.join(getSolrOrder(medicalBreakDrugAction,req.getParameterMap()),","));
        query.setStart((int)page.offset());
        query.setRows((int)page.getSize());
        QueryResponse queryResponse = SolrUtil.call(query, EngineUtil.MEDICAL_UNREASONABLE_DRUG_ACTION);
        SolrDocumentList documents = queryResponse.getResults();
        List<MedicalBreakDrugAction> result = new ArrayList<>();
        for (SolrDocument doc : documents) {
            MedicalBreakDrugAction bean = SolrUtil.solrDocumentToPojo(doc, MedicalBreakDrugAction.class);
            result.add(bean);
        }
        page.setRecords(result);
        page.setTotal(documents.getNumFound());
        return page;
    }

    public List<String> getSolrFq(Object obj) throws IllegalAccessException {
        List<String> list = new ArrayList<>();
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field f : fields) {
            Object value = f.get(obj);
            if (value != null && String.valueOf(value).length() > 0) {
                list.add(oConvertUtils.camelToUnderline(f.getName()) + ":" + value);
            }
        }
        return list;
    }

    public List<String> getSolrOrder(Object searchObj, Map<String, String[]> parameterMap) throws IllegalAccessException {
        String column=null,order=null;
        List<String> list = new ArrayList<>();
        if(parameterMap!=null&& parameterMap.containsKey(QueryGenerator.ORDER_COLUMN)) {
            column = parameterMap.get(QueryGenerator.ORDER_COLUMN)[0];
            if("createTime".equals(column)){
                try {
                    searchObj.getClass().getDeclaredField("createTime");
                }catch (NoSuchFieldException e) {
                    column = null;
                }
            }
        }
        if(parameterMap!=null&& parameterMap.containsKey(QueryGenerator.ORDER_TYPE)) {
            order = parameterMap.get(QueryGenerator.ORDER_TYPE)[0];
        }
        log.debug("排序规则>>列:"+column+",排序方式:"+order);

        if (oConvertUtils.isNotEmpty(column) && oConvertUtils.isNotEmpty(order)) {
            String[] columns = column.split(",");
            String[] orders = order.split(",");
            for(int i = 0, len = columns.length; i < len; i++){
                String col = columns[i];
                //字典字段，去掉字典翻译文本后缀
                if(col.endsWith(CommonConstant.DICT_TEXT_SUFFIX)) {
                    col = col.substring(0, col.lastIndexOf(CommonConstant.DICT_TEXT_SUFFIX));
                }
                //SQL注入check
//                SqlInjectionUtil.filterContent(col);

                if (orders.length <= i || orders[i].toUpperCase().contains(QueryGenerator.ORDER_TYPE_ASC)) {
                    list.add(oConvertUtils.camelToUnderline(col) + " asc" );
                } else {
                    list.add(oConvertUtils.camelToUnderline(col) + " desc" );
                }
            }

        }
        return list;
    }
}
