package com.ai.modules.drg.handle.rule;

import com.ai.modules.drg.constants.DrgCatalogConstants;
import com.ai.modules.drg.entity.MedicalVisitDrg;
import com.ai.modules.drg.service.IMedicalVisitDrgService;
import com.ai.modules.drg.vo.DrgTargetDtlVo;
import com.ai.modules.drg.vo.VisitDrgVo;
import org.jeecg.common.util.SpringContextUtils;
import org.springframework.context.ApplicationContext;

/**
 * @author : zhangly
 * @date : 2023/4/10 11:04
 */
public class DrgTargetHandle extends AbsDrgTargetHandle {
    public DrgTargetHandle(MedicalVisitDrg visitDrg) {
        super(visitDrg);
    }

    @Override
    public DrgTargetDtlVo parse() {
        DrgTargetDtlVo vo = new DrgTargetDtlVo();
        vo.setMdcDiagCode(visitDrg.getMdcDiagCode());
        vo.setMdcDiagName(visitDrg.getMdcDiagName());
        vo.setAdrgDiagCode(visitDrg.getAdrgDiagCode());
        vo.setAdrgDiagName(visitDrg.getAdrgDiagName());
        vo.setSurgeryCodes(visitDrg.getDrgSurgeryCode());
        vo.setSurgeryNames(visitDrg.getDrgSurgeryName());
        vo.setDrgDiagCode(visitDrg.getDrgDiagCode());
        vo.setDrgDiagName(visitDrg.getDrgDiagName());
        String mdc = visitDrg.getMdc();
        if(DrgCatalogConstants.MDCA.equals(mdc)) {
            vo.setMdcaFlag(DrgCatalogConstants.YES);
        } else {
            vo.setMdcaFlag(DrgCatalogConstants.NO);
            if(DrgCatalogConstants.MDCP.equals(mdc)) {
                vo.setBabyFlag(DrgCatalogConstants.YES);
            } else {
                vo.setBabyFlag(DrgCatalogConstants.NO);
                if(DrgCatalogConstants.MDCY.equals(mdc)) {
                    vo.setHivFlag(DrgCatalogConstants.YES);
                } else {
                    vo.setHivFlag(DrgCatalogConstants.NO);
                    if(DrgCatalogConstants.MDCZ.equals(mdc)) {
                        vo.setMultWoundFlag(DrgCatalogConstants.YES);
                    } else {
                        vo.setMultWoundFlag(DrgCatalogConstants.NO);
                    }
                }
            }
        }
        String adrgStep = visitDrg.getAdrgStep();
        if("6".equals(adrgStep)) {
            vo.setMeetAdrgFlag(DrgCatalogConstants.YES);
        } else if("7".equals(adrgStep)) {
            vo.setMeetAdrgFlag(DrgCatalogConstants.YES);
        } else if("8".equals(adrgStep)) {
            vo.setMeetAdrgFlag(DrgCatalogConstants.NO);
        } else if("9".equals(adrgStep)) {
            vo.setMeetAdrgFlag(DrgCatalogConstants.NO);
        }
        String drgStep = visitDrg.getDrgStep();
        if("14".equals(drgStep)) {
            vo.setJudgeSecDiagFlag(DrgCatalogConstants.NO);
        } else {
            if("10".equals(drgStep)) {
                vo.setJudgeSecDiagFlag(DrgCatalogConstants.YES);
                vo.setMcc(DrgCatalogConstants.NO);
                vo.setCc(DrgCatalogConstants.NO);
            }
            if("11".equals(drgStep)) {
                vo.setJudgeSecDiagFlag(DrgCatalogConstants.YES);
                vo.setMcc(DrgCatalogConstants.YES);
            }
            if("12".equals(drgStep)) {
                vo.setJudgeSecDiagFlag(DrgCatalogConstants.YES);
                vo.setMcc(DrgCatalogConstants.NO);
                vo.setCc(DrgCatalogConstants.YES);
            }
            if("13".equals(drgStep)) {
                vo.setJudgeSecDiagFlag(DrgCatalogConstants.YES);
                vo.setMcc(DrgCatalogConstants.NO);
                vo.setCc(DrgCatalogConstants.NO);
            }
        }
        ApplicationContext context = SpringContextUtils.getApplicationContext();
        IMedicalVisitDrgService service = context.getBean(IMedicalVisitDrgService.class);

        return vo;
    }
}
