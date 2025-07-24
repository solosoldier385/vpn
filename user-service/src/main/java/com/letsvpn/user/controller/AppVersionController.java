package com.letsvpn.user.controller;

import com.letsvpn.common.core.response.R;
import com.letsvpn.user.entity.AppVersion;
import com.letsvpn.user.service.AppVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "06. 应用版本管理 (App Version)", description = "客户端版本更新检测")
@RestController
@RequestMapping("/user/app")
public class AppVersionController {

    @Autowired
    private AppVersionService appVersionService;

    @Operation(
            summary = "获取最新版本信息",
            description = "检查指定平台的最新版本信息，用于客户端版本更新检测",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取版本信息"),
                    @ApiResponse(responseCode = "404", description = "未找到指定平台的版本信息", content = @Content)
            }
    )
    @GetMapping("/version")
    public R<Map<String, Object>> getLatestVersion(
            @Parameter(description = "平台类型", example = "android")
            @RequestParam(required = false, defaultValue = "android") String platform,
            @Parameter(description = "当前客户端版本", example = "1.0.0")
            @RequestParam(required = false) String currentVersion) {

        AppVersion latest = appVersionService.getLatest(platform);
        
        if (latest == null) {
            return R.fail("未找到指定平台的版本信息");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("version", latest.getVersion());
        result.put("downloadUrl", latest.getDownloadUrl());
        result.put("forceUpdate", latest.getForceUpdate() != null ? latest.getForceUpdate() : false);
        result.put("updateNote", latest.getUpdateNote());
        result.put("platform", latest.getPlatform());

        return R.success(result);
    }
}