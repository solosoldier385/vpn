package com.letsvpn.user.service;

import com.letsvpn.user.entity.AppVersion;

public interface AppVersionService {

    /**
     * 获取指定平台的最新版本信息
     * @param platform 平台名称，如 "android", "ios", "windows", "mac"
     * @return 最新版本信息
     */
    AppVersion getLatest(String platform);
}