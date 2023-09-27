package com.ai.modules.drg.handle.model;

import lombok.Data;

/**
 * @author : zhangly
 * @date : 2023/3/31 14:13
 */
@Data
public class TaskCatalogModel {
    /**DRG目录*/
    private String drgCatalogId = "";
    /**MDC目录*/
    private String mdcCatalogId = "";
    /**ADRG目录*/
    private String adrgCatalogId = "";
    /**MDC诊断目录*/
    private String mdcDiagCatalogId = "";
    /**ADRG诊断目录*/
    private String adrgDiagCatalogId = "";
    /**MCC目录*/
    private String mccCatalogId = "";
    /**CC目录*/
    private String ccCatalogId = "";
    /**手术目录*/
    private String surgeryCatalogId = "";
    /**排除目录*/
    private String excludeCatalogId = "";

    @Override
    public String toString() {
        return "TaskCatalogModel{" +
                "drgCatalogId='" + drgCatalogId + '\'' +
                ", mdcCatalogId='" + mdcCatalogId + '\'' +
                ", adrgCatalogId='" + adrgCatalogId + '\'' +
                ", mdcDiagCatalogId='" + mdcDiagCatalogId + '\'' +
                ", adrgDiagCatalogId='" + adrgDiagCatalogId + '\'' +
                ", mccCatalogId='" + mccCatalogId + '\'' +
                ", ccCatalogId='" + ccCatalogId + '\'' +
                ", surgeryCatalogId='" + surgeryCatalogId + '\'' +
                ", excludeCatalogId='" + excludeCatalogId + '\'' +
                '}';
    }
}
