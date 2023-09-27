package com.ai.modules.medical.service;

import java.util.List;

/**
 * @Auther: zhangpeng
 * @Date: 2020/12/22 10
 * @Description:
 */
public interface IMedicalValidService {
    /**
     * 验证编码是否存在于数据库
     * @param codes
     * @param title
     * @throws Exception
     */
    void validDiseaseGroupCodes(String[] codes, String title) throws Exception;

    void validTreatGroupCodes(String[] codes, String title) throws Exception;

    void validDrugGroupCodes(String[] codes, String title) throws Exception;

    void validTreatmentCodes(String[] codes, String title) throws Exception;

    void validStdAtcCodes(String[] codes, String title) throws Exception;

    void validDrugCodes(String[] codes, String title) throws Exception;

    void validDrugAndStdAtcCodes(String[] codes, String title) throws Exception;

    void validTreatProjectAndEquipmentCodes(String[] codes, String title) throws Exception;

    void validTreatProjectAndEquipmentCodesAndGroupCodes(String[] codes, String title) throws Exception;

    /**
     * 查询出不存在的编码
     * @param codes
     * @return
     * @throws Exception
     */
    List<String> invalidTreatGroupCodes(String[] codes) throws Exception;

    List<String> invalidDiseaseGroupCodes(String[] codes) throws Exception;

    List<String> invalidDrugGroupCodes(String[] codes) throws Exception;

    List<String> invalidTreatmentCodes(String[] codes) throws Exception;

    List<String> invalidDrugCodes(String[] codes);
    /**
     * 翻译字符串，多个code连接 -> 多个name连接
     * @param codesStr
     * @param splits
     * @return
     */
    String transDiseaseGroupCodes(String codesStr, String[] splits);

    String transTreatGroupCodes(String codesStr, String[] splits);

    String transTreatmentCodes(String codesStr, String[] splits);

    String transMedicalDictCodes(String codesStr, String[] splits, String dictCode);

    String transMedicalOtherDictCodes(String codesStr, String[] splits, String dictCode);

    /**
     * 翻译字符串，List<多个code连接> -> List<多个name连接>
     * @param codesStrs
     * @param splits
     * @return
     */
    List<String> transTreatmentCodes(List<String> codesStrs, String[] splits);

    List<String> transDiseaseGroupCodes(List<String> codesStrs, String[] splits);

    List<String> transTreatGroupCodes(List<String> codesStrs, String[] splits);

    List<String> transDrugGroupCodes(List<String> drugGroupOldList, String[] strings);

    List<String> transStdAtcCodes(List<String> codesStrs, String[] splits);

    List<String> invalidAtcCodes(String[] codes) throws Exception;

    List<String> invalidTreatOrEquipmentCodes(String[] codes) throws Exception;

    List<String> transMedicalDictCodes(List<String> codesStrs, String[] splits, String dictCode);

    List<String> transMedicalOtherDictCodes(List<String> codesStrs, String[] splits, String dictCode);

    String transMedicalDictNames(String codesStr, String[] splits, String dictCode) throws Exception;


    String transMedicalOtherDictNames(String codesStr, String[] splits, String dictCode) throws Exception;


}
