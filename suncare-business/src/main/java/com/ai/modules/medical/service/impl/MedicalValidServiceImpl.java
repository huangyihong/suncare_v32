package com.ai.modules.medical.service.impl;

import com.ai.modules.config.mapper.MedicalDrugMapper;
import com.ai.modules.config.mapper.MedicalEquipmentMapper;
import com.ai.modules.config.mapper.MedicalStdAtcMapper;
import com.ai.modules.config.mapper.MedicalTreatProjectMapper;
import com.ai.modules.config.service.IMedicalDictService;
import com.ai.modules.config.service.IMedicalOtherDictService;
import com.ai.modules.config.vo.MedicalCodeNameVO;
import com.ai.modules.medical.mapper.MedicalClinicalMapper;
import com.ai.modules.medical.service.IMedicalValidService;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Auther: zhangpeng
 * @Date: 2020/12/22 10
 * @Description:
 */
@Service
public class MedicalValidServiceImpl implements IMedicalValidService {

    @Autowired
    MedicalClinicalMapper medicalClinicalMapper;

    @Autowired
    MedicalStdAtcMapper medicalStdAtcMapper;

    @Autowired
    MedicalDrugMapper medicalDrugMapper;

    @Autowired
    MedicalTreatProjectMapper medicalTreatProjectMapper;

    @Autowired
    MedicalEquipmentMapper medicalEquipmentMapper;

    @Autowired
    IMedicalDictService medicalDictService;

    @Autowired
    IMedicalOtherDictService medicalOtherDictService;

    @Override
    public void validDiseaseGroupCodes(String[] codes, String title) throws Exception {
        if(codes.length == 0){
            return;
        }
        // 排序，跟数据库取的对应
        List<String> notExistCode = this.getGroupNoExistCodes(codes, "5");
        if (notExistCode.size() > 0) {
            throw new Exception(title + "编码不存在：[" +
                    StringUtils.join(notExistCode, ",") + "]");
        }

    }

    @Override
    public List<String> invalidDiseaseGroupCodes(String[] codes) throws Exception {
        if(codes.length == 0){
            return new ArrayList<>();
        }
        // 排序，跟数据库取的对应
        List<String> notExistCode = this.getGroupNoExistCodes(codes, "5");
        return notExistCode;

    }

    @Override
    public void validTreatGroupCodes(String[] codes, String title) throws Exception {
        if(codes.length == 0){
            return;
        }
        // 排序，跟数据库取的对应
        List<String> notExistCode = this.getGroupNoExistCodes(codes, "1");
        if (notExistCode.size() > 0) {
            throw new Exception(title + "编码不存在：[" +
                    StringUtils.join(notExistCode, ",") + "]");
        }

    }

    @Override
    public List<String> invalidDrugGroupCodes(String[] codes) throws Exception {
        if(codes.length == 0){
            return new ArrayList<>();
        }
        // 排序，跟数据库取的对应
        List<String> notExistCode = this.getGroupNoExistCodes(codes, "7");
        return notExistCode;

    }

    @Override
    public void validDrugGroupCodes(String[] codes, String title) throws Exception {
        if(codes.length == 0){
            return;
        }
        // 排序，跟数据库取的对应
        List<String> notExistCode = this.getGroupNoExistCodes(codes, "7");
        if (notExistCode.size() > 0) {
            throw new Exception(title + "编码不存在：[" +
                    StringUtils.join(notExistCode, ",") + "]");
        }
    }

    @Override
    public List<String> invalidTreatGroupCodes(String[] codes) throws Exception {
        if(codes.length == 0){
            return new ArrayList<>();
        }
        // 排序，跟数据库取的对应
        List<String> notExistCode = this.getGroupNoExistCodes(codes, "1");
        return notExistCode;
    }

    @Override
    public void validTreatmentCodes(String[] codes, String title) throws Exception {
        // 排序，跟数据库取的对应
        List<String> notExistCode = this.getItemNoExistCodes(codes, "1");
        if (notExistCode.size() > 0) {
            throw new Exception(title + "编码不存在：[" +
                    StringUtils.join(notExistCode, ",") + "]");
        }
    }

    @Override
    public void validStdAtcCodes(String[] codes, String title) throws Exception {
        // 排序，跟数据库取的对应
        List<String> notExistCode = this.getItemNoExistCodes(codes, codes2 ->
                this.medicalStdAtcMapper.queryCodeNameIdInCodes(codes2));
        if (notExistCode.size() > 0) {
            throw new Exception(title + "编码不存在：[" +
                    StringUtils.join(notExistCode, ",") + "]");
        }
    }

    @Override
    public void validDrugCodes(String[] codes, String title) throws Exception {
        // 排序，跟数据库取的对应
        List<String> notExistCode = this.getItemNoExistCodes(codes, codes2 ->
                this.medicalDrugMapper.queryCodeNameIdInCodes(codes2));
        if (notExistCode.size() > 0) {
            throw new Exception(title + "编码不存在：[" +
                    StringUtils.join(notExistCode, ",") + "]");
        }
    }

    @Override
    public void validDrugAndStdAtcCodes(String[] codes, String title) throws Exception {
        // 排序，跟数据库取的对应
        List<String> notExistCode1 = this.getItemNoExistCodes(codes, codes2 ->
                this.medicalStdAtcMapper.queryCodeNameIdInCodes(codes2));

        List<String> notExistCode2 = this.getItemNoExistCodes(codes, codes2 ->
                this.medicalDrugMapper.queryCodeNameIdInCodes(codes2));
        List<String> notExistCode = notExistCode1.stream().filter(notExistCode2::contains).collect(Collectors.toList());
        if (notExistCode.size() > 0) {
            throw new Exception(title + "编码不存在：[" +
                    StringUtils.join(notExistCode, ",") + "]");
        }
    }

    @Override
    public void validTreatProjectAndEquipmentCodes(String[] codes, String title) throws Exception {
        // 排序，跟数据库取的对应
        List<String> notExistCode1 = this.getItemNoExistCodes(codes, codes2 ->
                this.medicalTreatProjectMapper.queryCodeNameIdInCodes(codes2));

        List<String> notExistCode2 = this.getItemNoExistCodes(codes, codes2 ->
                this.medicalEquipmentMapper.queryCodeNameIdInCodes(codes2));
        List<String> notExistCode = notExistCode1.stream().filter(notExistCode2::contains).collect(Collectors.toList());
        if (notExistCode.size() > 0) {
            throw new Exception(title + "编码不存在：[" +
                    StringUtils.join(notExistCode, ",") + "]");
        }
    }

    @Override
    public void validTreatProjectAndEquipmentCodesAndGroupCodes(String[] codes, String title) throws Exception {
        // 排序，跟数据库取的对应
        List<String> notExistCode1 = this.getItemNoExistCodes(codes, codes2 ->
                this.medicalTreatProjectMapper.queryCodeNameIdInCodes(codes2));

        List<String> notExistCode2 = this.getItemNoExistCodes(codes, codes2 ->
                this.medicalEquipmentMapper.queryCodeNameIdInCodes(codes2));

        List<String> notExistCode3 = this.getGroupNoExistCodes(codes, "1");

        List<String> notExistCode = notExistCode1.stream().filter(notExistCode2::contains).filter(notExistCode3::contains).collect(Collectors.toList());
        if (notExistCode.size() > 0) {
            throw new Exception(title + "编码不存在：[" +
                    StringUtils.join(notExistCode, ",") + "]");
        }
    }

    @Override
    public List<String> invalidTreatmentCodes(String[] codes) throws Exception {
        if(codes.length == 0){
            return new ArrayList<>();
        }
        // 排序，跟数据库取的对应
        List<String> notExistCode = this.getItemNoExistCodes(codes, "1");
        return notExistCode;
    }

    @Override
    public List<String> invalidDrugCodes(String[] codes) {
        if(codes.length == 0){
            return new ArrayList<>();
        }
        // 排序，跟数据库取的对应
        List<String> notExistCode = this.getItemNoExistCodes(codes, "7");
        return notExistCode;
    }

    @Override
    public String transDiseaseGroupCodes(String codesStr, String[] splits) {
        return this.transGroupCodes(codesStr, "5", splits);
    }

    @Override
    public String transTreatGroupCodes(String codesStr, String[] splits) {
        return this.transGroupCodes(codesStr, "1", splits);
    }

    @Override
    public List<String> transTreatmentCodes(List<String> codesStrs, String[] splits) {
        return this.transItemCodes(codesStrs, "1", splits);
    }

    @Override
    public List<String> transDiseaseGroupCodes(List<String> codesStrs, String[] splits) {
        return this.transGroupCodes(codesStrs, "5", splits);
    }

    @Override
    public List<String> transTreatGroupCodes(List<String> codesStrs, String[] splits) {
        return this.transGroupCodes(codesStrs, "1", splits);
    }

    @Override
    public List<String> transDrugGroupCodes(List<String> codesStrs, String[] splits) {
        return this.transGroupCodes(codesStrs, "7", splits);
    }

    @Override
    public String transTreatmentCodes(String codesStr, String[] splits) {
        return this.transItemCodes(codesStr, "1", splits);
    }

    @Override
    public List<String> transStdAtcCodes(List<String> codesStrs, String[] splits) {
        return this.transItemCodes(codesStrs, splits, codes ->
                this.medicalStdAtcMapper.queryCodeNameIdInCodes(codes));
    }

    @Override
    public List<String> invalidAtcCodes(String[] codes) throws Exception {
        if(codes.length == 0){
            return new ArrayList<>();
        }
        // 排序，跟数据库取的对应
        return this.getItemNoExistCodes(codes, codes2 ->
                this.medicalStdAtcMapper.queryCodeNameIdInCodes(codes2));
    }

    @Override
    public List<String> invalidTreatOrEquipmentCodes(String[] codes) throws Exception {
        if(codes.length == 0){
            return new ArrayList<>();
        }
        // 排序，跟数据库取的对应
        return this.getItemNoExistCodes(codes, codes2 ->
                this.medicalClinicalMapper.queryTreatOrEquipmentCodeIdInCodes(codes2));
    }

    @Override
    public List<String> transMedicalDictCodes(List<String> codesStrs, String[] splits, String dictCode) {

        List<SplitNode> roots = codesStrs.stream().map(codesStr -> StringUtils.isBlank(codesStr)?null:initNode(codesStr, splits, 0)).collect(Collectors.toList());

        Map<String, String> codeNameMap = medicalDictService.queryMapByType(dictCode);

        return roots.stream().map(root -> root == null?null:getTransData(root, codeNameMap)).collect(Collectors.toList());
    }

    @Override
    public String transMedicalDictCodes(String codesStr, String[] splits, String dictCode) {

        SplitNode root = initNode(codesStr, splits, 0);

        Map<String, String> codeNameMap = medicalDictService.queryMapByType(dictCode);
        return getTransData(root, codeNameMap);
    }

    @Override
    public List<String> transMedicalOtherDictCodes(List<String> codesStrs, String[] splits, String dictCode) {
        List<SplitNode> roots = codesStrs.stream().map(codesStr -> StringUtils.isBlank(codesStr)?null:initNode(codesStr, splits, 0)).collect(Collectors.toList());

        return roots.stream().map(root -> root == null?
                null: getTransData(root, code -> medicalOtherDictService.getValueByCode(dictCode, code)))
                .collect(Collectors.toList());
    }

    @Override
    public String transMedicalDictNames(String codesStr, String[] splits, String dictCode) throws Exception {
        SplitNode root = initNode(codesStr, splits, 0);

        Map<String, String> codeNameMap = medicalDictService.queryNameMapByType(dictCode);
        Set<String> noExistCodes = new HashSet<>();
        String value = getTransData(root, code -> {
            String name = codeNameMap.get(code);
            if(name == null){
                noExistCodes.add(code);
            }
            return name;
        });
        if(noExistCodes.size() > 0){
            throw new Exception("医疗字典-" + dictCode + "中不存在：" + Arrays.toString(noExistCodes.toArray(new String[0])));
        }
        return value;
    }

    @Override
    public String transMedicalOtherDictCodes(String codesStr, String[] splits, String dictCode) {

        SplitNode root = initNode(codesStr, splits, 0);

        return getTransData(root, code -> medicalOtherDictService.getValueByCode(dictCode, code));
    }

    @Override
    public String transMedicalOtherDictNames(String codesStr, String[] splits, String dictCode) throws Exception {
        SplitNode root = initNode(codesStr, splits, 0);

        Set<String> noExistCodes = new HashSet<>();
        String value = getTransData(root, code -> {
            String name = medicalOtherDictService.getCodeByValue(dictCode, code);
            if(name == null){
                noExistCodes.add(code);
            }
            return name;
        });
        if(noExistCodes.size() > 0){
            throw new Exception("其他字典-" + dictCode + "中不存在：" + Arrays.toString(noExistCodes.toArray(new String[0])));
        }
        return value;
    }

    private List<String> getItemNoExistCodes(String[] codes, String kind){
        return this.getItemNoExistCodes(codes, codes2 ->
                this.medicalClinicalMapper.queryItemCodeIdInCodes(codes2, kind));
    }

    private List<String> getGroupNoExistCodes(String[] codes, String kind){
        return this.getItemNoExistCodes(codes, codes2 ->
                this.medicalClinicalMapper.queryGroupCodeIdInCodes(codes2, kind));
    }

    private List<String> getItemNoExistCodes(String[] codes, Function<String[], List<MedicalCodeNameVO>> func){
        Arrays.sort(codes);
        List<String> notExistCode = new ArrayList<>();
        List<MedicalCodeNameVO> groupMaps = new ArrayList<>();
        for(int i = 0,j; i < codes.length; i = j){
            j = i + 1000;
            if(j > codes.length){
                j = codes.length;
            }
            groupMaps.addAll(func.apply(Arrays.copyOfRange(codes, i, j)));
        }

        int index = 0;
        String oldCode = "";
        for (MedicalCodeNameVO map : groupMaps) {
//                String id = (String) map.get("ID");
            if(index == codes.length){
                break;
            }
            String code = codes[index];
            String mapCode = map.getCode();
            if(oldCode.equals(mapCode) && !code.equals(mapCode)){
                continue;
            }
            index++;
            while(!mapCode.equals(code)){
                notExistCode.add(code);
                code = codes[index++];
            }
            oldCode = code;
//                treatmentMap.put(mapCode,(String) map.get("NAME"));
        }
        for(;index < codes.length; index++){
            notExistCode.add(codes[index]);
        }
        return notExistCode;
    }

    @Data
    private class SplitNode {
        private String data;
        private String splitChar;
        private SplitNode[] nodes;
        private boolean isLeaf;

    }


    private List<String> getLeafData(SplitNode splitNode){

        if(splitNode.isLeaf){
            return Collections.singletonList(splitNode.data);
        } else {
            List<String> list = new ArrayList<>();
            for(SplitNode node: splitNode.getNodes()){
                list.addAll(getLeafData(node));
            }
            return list;
        }

    }

    private String getTransData(SplitNode splitNode, Map<String, String> map){
        return this.getTransData(splitNode, code -> map.getOrDefault(code, code));
    }

    private String getTransData(SplitNode splitNode, Function<String, String> func){

        if(splitNode.isLeaf){
            String data = splitNode.data;
            return func.apply(data);
        } else {
            List<String> list = new ArrayList<>();
            for(SplitNode node: splitNode.getNodes()){
                list.add(getTransData(node, func));
            }
            return StringUtils.join(list, splitNode.getSplitChar());
        }

    }


    private SplitNode initNode(String codesStr, String[] splits, int splitIndex){

        SplitNode node = new SplitNode();
        node.setData("null".equals(codesStr)?"":codesStr);

        if(splitIndex < splits.length){

            String splitChar = splits[splitIndex];
            node.setSplitChar(splitChar);
            String[] codes = this.split(codesStr, splitChar);
            SplitNode[] nodes = new SplitNode[codes.length];

            for(int i = 0, len = codes.length; i < len; i++){
                nodes[i] = initNode(codes[i], splits, splitIndex + 1);
            }

            node.setNodes(nodes);
        } else {
            node.setLeaf(true);
        }


        return node;

    }

    private String transGroupCodes(String codesStr, String kind, String[] splits){
        if(StringUtils.isBlank(codesStr)){
            return null;
        }

        SplitNode root = initNode(codesStr, splits, 0);
        String[] codes = getLeafData(root).stream().filter(StringUtils::isNotBlank).distinct().toArray(String[]::new);

        List<MedicalCodeNameVO> groupMaps = new ArrayList<>();
        for(int i = 0,j; i < codes.length; i = j){
            j = i + 1000;
            if(j > codes.length){
                j = codes.length;
            }
            groupMaps.addAll(
                    this.medicalClinicalMapper.queryGroupCodeIdInCodes(Arrays.copyOfRange(codes, i, j), kind)
            );
        }

        Map<String, String> codeNameMap = new HashMap<>();
        for (MedicalCodeNameVO map : groupMaps) {
            codeNameMap.put(map.getCode(), map.getName());
        }
        String transStr = getTransData(root, codeNameMap);
        return transStr;
    }

    private List<String> transGroupCodes(List<String> codesStrs, String kind, String[] splits){

        List<SplitNode> roots = codesStrs.stream().map(codesStr -> StringUtils.isBlank(codesStr)?null:initNode(codesStr, splits, 0)).collect(Collectors.toList());
        String[] codes = roots.stream().filter(Objects::nonNull).map(this::getLeafData).flatMap(Collection::stream).filter(Objects::nonNull).distinct().toArray(String[]::new);

        List<MedicalCodeNameVO> groupMaps = new ArrayList<>();
        for(int i = 0,j; i < codes.length; i = j){
            j = i + 1000;
            if(j > codes.length){
                j = codes.length;
            }
            groupMaps.addAll(
                    this.medicalClinicalMapper.queryGroupCodeIdInCodes(Arrays.copyOfRange(codes, i, j), kind)
            );
        }

        Map<String, String> codeNameMap = new HashMap<>();
        for (MedicalCodeNameVO map : groupMaps) {
            codeNameMap.put(map.getCode(), map.getName());
        }
        List<String> transStrs = roots.stream().map(root -> root == null?null:getTransData(root, codeNameMap)).collect(Collectors.toList());
        return transStrs;
    }

    private String transItemCodes(String codesStr, String kind, String[] splits){
        if(StringUtils.isBlank(codesStr)){
            return null;
        }
        SplitNode root = initNode(codesStr, splits, 0);
        String[] codes = getLeafData(root).stream().filter(Objects::nonNull).distinct().toArray(String[]::new);

        List<MedicalCodeNameVO> groupMaps = new ArrayList<>();
        for(int i = 0,j; i < codes.length; i = j){
            j = i + 1000;
            if(j > codes.length){
                j = codes.length;
            }
            groupMaps.addAll(
                    this.medicalClinicalMapper.queryItemCodeIdInCodes(Arrays.copyOfRange(codes, i, j), kind)
            );
        }

        Map<String, String> codeNameMap = new HashMap<>();
        for (MedicalCodeNameVO map : groupMaps) {
            codeNameMap.put(map.getCode(), map.getName());
        }
        String transStr = getTransData(root, codeNameMap);
        return transStr;
    }

    private List<String> transItemCodes(List<String> codesStrs, String kind, String[] splits){
        return this.transItemCodes(codesStrs, splits, codes ->
                this.medicalClinicalMapper.queryItemCodeIdInCodes(codes, kind));
    }

    private List<String> transItemCodes(List<String> codesStrs, String[] splits, Function<String[], List<MedicalCodeNameVO>> func){

        List<SplitNode> roots = codesStrs.stream().map(codesStr -> StringUtils.isBlank(codesStr)?null:initNode(codesStr, splits, 0)).collect(Collectors.toList());
        String[] codes = roots.stream().filter(Objects::nonNull).map(this::getLeafData).flatMap(Collection::stream).filter(Objects::nonNull).distinct().toArray(String[]::new);

        List<MedicalCodeNameVO> groupMaps = new ArrayList<>();
        for(int i = 0,j; i < codes.length; i = j){
            j = i + 1000;
            if(j > codes.length){
                j = codes.length;
            }
            groupMaps.addAll(func.apply(Arrays.copyOfRange(codes, i, j)));
        }

        Map<String, String> codeNameMap = new HashMap<>();
        for (MedicalCodeNameVO map : groupMaps) {
            codeNameMap.put(map.getCode(), map.getName());
        }
        List<String> transStrs = roots.stream().map(root -> root == null?null:getTransData(root, codeNameMap)).collect(Collectors.toList());
        return transStrs;
    }

    private String[] split(String str, String splitChar){
        if("|".equals(splitChar)){
            splitChar = "\\|";
        }
        return str.split(splitChar);

    }
}
