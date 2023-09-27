package com.ai.modules.medical.vo;

import com.ai.modules.medical.entity.MedicalClinicalAccessGroup;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Auther: zhangpeng
 * @Date: 2020/4/27 17
 * @Description:
 */
@Data
public class MedicalClinicalAccessGroupVO extends MedicalClinicalAccessGroup {
    public MedicalClinicalAccessGroupVO(){
        init = false;
        diseaseGroupList = new HashSet<>();
        operationList = new HashSet<>();
        checkItemList = new HashSet<>();
        drugGroupList = new HashSet<>();
        pathologyList = new HashSet<>();
        checkItemsDescList = new ArrayList<>();
    }
    // 是否已赋值基本信息
    private boolean init;

    private Set<String> diseaseGroupList;

    private Set<String> operationList;

    private Set<String> checkItemList;

    private Set<String> drugGroupList;

    private Set<String> pathologyList;

    private List<String> checkItemsDescList;

    public boolean addDiseaseGroup(String str){
        return diseaseGroupList.add(str);
    }
    public boolean addOperation(String str){
        return operationList.add(str);
    }
    public boolean addCheckItem(String str){
        return checkItemList.add(str);
    }

    public boolean addDrugGroup(String str){
        return drugGroupList.add(str);
    }
    public boolean addPathology(String str){
        return pathologyList.add(str);
    }
    public void addCheckItemsDesc(String str){
        checkItemsDescList.add(str);
    }
}
