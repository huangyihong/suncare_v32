package com.ai.modules.ybFj.service.impl;

import cn.hutool.core.util.StrUtil;
import com.ai.common.utils.IdUtils;
import com.ai.modules.ybFj.entity.YbFjTemplateExport;
import com.ai.modules.ybFj.mapper.YbFjTemplateExportMapper;
import com.ai.modules.ybFj.service.IYbFjTemplateExportService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description: 文件模板信息
 * @Author: jeecg-boot
 * @Date:   2023-02-03
 * @Version: V1.0
 */
@Service
public class YbFjTemplateExportServiceImpl extends ServiceImpl<YbFjTemplateExportMapper, YbFjTemplateExport> implements IYbFjTemplateExportService {

    @Override
    public void add(YbFjTemplateExport ybFjTemplate) {
        //获取templateId
        QueryWrapper<YbFjTemplateExport> queryWrapper = new QueryWrapper<YbFjTemplateExport>();
        queryWrapper.select("IFNULL(max(TEMPLATE_CODE),0) as TEMPLATE_CODE_MAX");
        List<Map<String, Object>> map = this.baseMapper.selectMaps(queryWrapper);
        int templateIdMax = Integer.parseInt(String.valueOf(map.get(0).get("TEMPLATE_CODE_MAX")));
        ybFjTemplate.setTemplateCode(templateIdMax+1);
        //获取versionNum
        ybFjTemplate.setVersionNum(1.00f);
        ybFjTemplate.setUseStatus("1");
        this.baseMapper.insert(ybFjTemplate);
    }

    @Override
    @Transactional
    public void edit(YbFjTemplateExport ybFjTemplate) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        //历史版本
        YbFjTemplateExport oldBean = this.baseMapper.selectById(ybFjTemplate.getId());
        oldBean = this.setUpdateUseStatus(oldBean,user);
        this.baseMapper.updateById(oldBean);

        //修改 版本号加0.01
        ybFjTemplate.setId(IdUtils.uuid());
        ybFjTemplate.setVersionNum(oldBean.getVersionNum()+0.01f);
        ybFjTemplate.setUpdateTime(new Date());
        ybFjTemplate.setUpdateUser(user.getUsername());
        ybFjTemplate.setUpdateUsername(user.getRealname());
        this.baseMapper.insert(ybFjTemplate);
    }

    @Override
    public void delete(String id) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        YbFjTemplateExport oldBean = this.baseMapper.selectById(id);
        oldBean = this.setUpdateUseStatus(oldBean,user);
        this.baseMapper.updateById(oldBean);
    }

    @Override
    public void deleteBatch(String ids) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<YbFjTemplateExport> list = this.baseMapper.selectBatchIds(Arrays.asList(ids.split(",")));
        for(YbFjTemplateExport oldBean:list){
            this.setUpdateUseStatus(oldBean,user);
        }
        this.updateBatchById(list);
    }

    private YbFjTemplateExport setUpdateUseStatus(YbFjTemplateExport oldBean, LoginUser user){
        oldBean.setUseStatus("0");
        oldBean.setUpdateTime(new Date());
        oldBean.setUpdateUser(user.getUsername());
        oldBean.setUpdateUsername(user.getRealname());
        return oldBean;
    }


    @Override
    public String upload(MultipartFile mf, String bizPath) throws IOException {
        File file = new File("/home/web/python_src");
        if (!file.exists()) {
            file.mkdirs();// 创建文件根目录
        }
        // 获取文件名
        String originalFilename = mf.getOriginalFilename();
        if(StrUtil.isNotEmpty(originalFilename)){
            //获取文件扩展名
            String tempFileName = originalFilename.trim().toLowerCase();
            int index = tempFileName.lastIndexOf(".");

            //如果没有扩展名则返回false
            if(index >0) {
                String fileType = tempFileName.substring(index+1);
                if(!fileType.equalsIgnoreCase("py")){
                    throw new IOException("文件扩展名不符合要求!");
                }
            }

        }

        String path = file.getPath() + File.separator + originalFilename;
        File savefile = new File(path);
        FileCopyUtils.copy(mf.getBytes(), savefile);

        return path;
    }
}
