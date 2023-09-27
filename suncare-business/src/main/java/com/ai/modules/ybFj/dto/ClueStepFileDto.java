package com.ai.modules.ybFj.dto;

import lombok.Data;

/**
 * @author : zhangly
 * @date : 2023/3/14 9:56
 */
@Data
public class ClueStepFileDto {
    private String projectOrgId;
    private String culeId;
    private String stepGroup;
    private String stepType;
    private boolean mine = false;
}
