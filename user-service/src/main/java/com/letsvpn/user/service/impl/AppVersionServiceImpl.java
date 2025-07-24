package com.letsvpn.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.letsvpn.user.entity.AppVersion;
import com.letsvpn.user.mapper.AppVersionMapper;
import com.letsvpn.user.service.AppVersionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppVersionServiceImpl implements AppVersionService {

    private static final Logger log = LoggerFactory.getLogger(AppVersionServiceImpl.class);

    @Autowired
    private AppVersionMapper appVersionMapper;

    @Override
    public AppVersion getLatest(String platform) {
        QueryWrapper<AppVersion> queryWrapper = new QueryWrapper<>();
        if (platform != null && !platform.trim().isEmpty()) {
            queryWrapper.eq("platform", platform);
        }
        queryWrapper.orderByDesc("created_at").last("LIMIT 1");
        
        AppVersion latest = appVersionMapper.selectOne(queryWrapper);
        
        if (latest == null) {
            log.warn("未找到平台 {} 的版本信息", platform);
        }
        
        return latest;
    }
}