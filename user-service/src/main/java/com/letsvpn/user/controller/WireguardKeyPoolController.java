package com.letsvpn.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.letsvpn.common.core.response.R;
import com.letsvpn.user.entity.Node;
import com.letsvpn.user.entity.WireguardKeyPool;
import com.letsvpn.user.mapper.NodeMapper;
import com.letsvpn.user.mapper.WireguardKeyPoolMapper;
import com.letsvpn.user.service.WireGuardConfigService;
import com.letsvpn.user.task.WireguardKeyPoolTask;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "WireGuard密钥池管理", description = "手动管理WireGuard密钥池")
@RestController
@RequestMapping("/user/v1/internal/wireguard-key-pool")
@Slf4j
public class WireguardKeyPoolController {

    @Autowired
    private WireguardKeyPoolTask wireguardKeyPoolTask;

    @Autowired
    private WireGuardConfigService wireGuardConfigService;
    
    @Autowired
    private NodeMapper nodeMapper;
    
    @Autowired
    private WireguardKeyPoolMapper wireguardKeyPoolMapper;


    @Operation(summary = "手动补充密钥池")
    @PostMapping("/replenish")
    public R<Map<String, Object>> replenishKeyPool(
            @Parameter(description = "节点ID，不传则为所有节点生成") @RequestParam(required = false) Long nodeId,
            @Parameter(description = "生成数量，默认50") @RequestParam(defaultValue = "50") int batchSize) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            if (nodeId != null) {
                // 为指定节点生成密钥对
                log.info("开始为节点 {} 生成 {} 个密钥对", nodeId, batchSize);
                
                // 验证节点是否存在
                Node node = nodeMapper.selectById(nodeId);
                if (node == null) {
                    return R.fail("节点不存在: " + nodeId);
                }
                
                int generatedCount = wireGuardConfigService.allocateKeyPairforNode(nodeId, batchSize);


                result.put("nodeId", nodeId);
                result.put("nodeName", node.getName());
                result.put("nodeWgAddress", node.getWgAddress());
                result.put("requestedCount", batchSize);
                result.put("generatedCount", generatedCount);
                result.put("message", String.format("节点 %d 密钥池补充成功，生成 %d 个密钥对", nodeId, generatedCount));
                
                log.info("节点 {} 密钥池补充完成，生成 {} 个密钥对", nodeId, generatedCount);
            } else {
                // 为所有活跃节点生成密钥对
                log.info("开始为所有活跃节点生成密钥对，每个节点 {} 个", batchSize);
                wireguardKeyPoolTask.manualReplenishKeyPool();
                
                result.put("message", "所有节点密钥池补充成功");
                result.put("batchSizePerNode", batchSize);
                
                log.info("所有节点密钥池补充完成");
            }
            
            return R.success(result);
        } catch (Exception e) {
            log.error("手动补充密钥池失败", e);
            return R.fail("密钥池补充失败: " + e.getMessage());
        }
    }

    @Operation(summary = "清理重复的密钥池数据")
    @PostMapping("/clean-duplicates")
    public R<String> cleanDuplicateKeys() {
        try {
            wireGuardConfigService.cleanDuplicateAddresses();
            return R.success("重复数据清理成功");
        } catch (Exception e) {
            log.error("清理重复数据失败", e);
            return R.fail("清理重复数据失败: " + e.getMessage());
        }
    }

    @Operation(summary = "清理指定节点的所有密钥对")
    @PostMapping("/clean-node/{nodeId}")
    public R<String> cleanNodeKeys(
            @Parameter(description = "节点ID") @PathVariable Long nodeId) {
        try {
            wireGuardConfigService.cleanAllKeysByNodeId(nodeId);
            return R.success("节点 " + nodeId + " 密钥对清理成功");
        } catch (Exception e) {
            log.error("清理节点 {} 密钥对失败", nodeId, e);
            return R.fail("清理节点密钥对失败: " + e.getMessage());
        }
    }

    /**
     * 根据节点ID获取完整的 wg0.conf 配置文件内容
     * @param nodeId 节点ID
     * @return 完整的 wg0.conf 配置文件内容
     */
    @GetMapping("/config/{nodeId}")
    public ResponseEntity<String> getWgConfigForNode(@PathVariable Long nodeId) {
        try {
            String configContent = wireGuardConfigService.generateWgConfigForNodeFromKeyPool(nodeId);
            if (configContent != null) {
                return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(configContent);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取节点 {} 的 WireGuard 配置失败: {}", nodeId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("获取配置失败: " + e.getMessage());
        }
    }

    /**
     * 测试生成密钥对时是否避免与节点自身IP冲突
     * @param nodeId 节点ID
     * @param batchSize 生成数量
     * @return 测试结果
     */
    @PostMapping("/test-generate/{nodeId}")
    public ResponseEntity<Map<String, Object>> testGenerateKeyPairs(@PathVariable Long nodeId, 
                                                                   @RequestParam(defaultValue = "5") int batchSize) {
        try {
            // 获取节点信息
            Node node = nodeMapper.selectById(nodeId);
            if (node == null) {
                return ResponseEntity.notFound().build();
            }

            // 获取生成前的占用IP列表
            Set<String> beforeOccupiedIps = wireGuardConfigService.getOccupiedIpsForNode(nodeId);
            
            // 生成密钥对
            wireGuardConfigService.generateKeyPairsForAllNodes(batchSize);
            
            // 获取生成后的占用IP列表
            Set<String> afterOccupiedIps = wireGuardConfigService.getOccupiedIpsForNode(nodeId);
            
            // 获取新生成的密钥对
            QueryWrapper<WireguardKeyPool> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("node_id", nodeId)
                       .eq("status", 0)
                       .orderByDesc("created_at")
                       .last("LIMIT " + batchSize);
            List<WireguardKeyPool> newKeys = wireguardKeyPoolMapper.selectList(queryWrapper);
            
            Map<String, Object> result = new HashMap<>();
            result.put("nodeId", nodeId);
            result.put("nodeWgAddress", node.getWgAddress());
            result.put("nodeServerIp", node.getWgAddress() != null ? node.getWgAddress().split("/")[0] : null);
            result.put("beforeOccupiedIps", new ArrayList<>(beforeOccupiedIps));
            result.put("afterOccupiedIps", new ArrayList<>(afterOccupiedIps));
            result.put("newGeneratedKeys", newKeys.stream()
                .map(key -> Map.of(
                    "id", key.getId(),
                    "address", key.getAddress(),
                    "publicKey", key.getPublicKey(),
                    "createdAt", key.getCreatedAt()
                ))
                .collect(Collectors.toList()));
            result.put("generatedCount", newKeys.size());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("测试生成密钥对失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 测试为指定节点生成大量密钥对
     * @param nodeId 节点ID
     * @param batchSize 生成数量
     * @return 测试结果
     */
    @PostMapping("/test-bulk-generate/{nodeId}")
    public ResponseEntity<Map<String, Object>> testBulkGenerateKeyPairs(@PathVariable Long nodeId, 
                                                                       @RequestParam(defaultValue = "1000") int batchSize) {
        try {
            // 获取节点信息
            Node node = nodeMapper.selectById(nodeId);
            if (node == null) {
                return ResponseEntity.notFound().build();
            }

            // 获取生成前的统计信息
            QueryWrapper<WireguardKeyPool> beforeQuery = new QueryWrapper<>();
            beforeQuery.eq("node_id", nodeId);
            long beforeCount = wireguardKeyPoolMapper.selectCount(beforeQuery);
            
            // 获取生成前的占用IP列表
            Set<String> beforeOccupiedIps = wireGuardConfigService.getOccupiedIpsForNode(nodeId);
            
            long startTime = System.currentTimeMillis();
            
            // 生成密钥对
            int generatedCount = wireGuardConfigService.generateKeyPairsForNode(nodeId, batchSize);
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // 获取生成后的统计信息
            QueryWrapper<WireguardKeyPool> afterQuery = new QueryWrapper<>();
            afterQuery.eq("node_id", nodeId);
            long afterCount = wireguardKeyPoolMapper.selectCount(afterQuery);
            
            // 获取生成后的占用IP列表
            Set<String> afterOccupiedIps = wireGuardConfigService.getOccupiedIpsForNode(nodeId);
            
            // 获取新生成的密钥对样本（前10个）
            QueryWrapper<WireguardKeyPool> sampleQuery = new QueryWrapper<>();
            sampleQuery.eq("node_id", nodeId)
                      .eq("status", 0)
                      .orderByDesc("created_at")
                      .last("LIMIT 10");
            List<WireguardKeyPool> sampleKeys = wireguardKeyPoolMapper.selectList(sampleQuery);
            
            Map<String, Object> result = new HashMap<>();
            result.put("nodeId", nodeId);
            result.put("nodeName", node.getName());
            result.put("nodeWgAddress", node.getWgAddress());
            result.put("requestedCount", batchSize);
            result.put("generatedCount", generatedCount);
            result.put("beforeTotalCount", beforeCount);
            result.put("afterTotalCount", afterCount);
            result.put("durationMs", duration);
            result.put("durationSeconds", duration / 1000.0);
            result.put("generationRate", String.format("%.2f keys/second", generatedCount / (duration / 1000.0)));
            result.put("beforeOccupiedIpsCount", beforeOccupiedIps.size());
            result.put("afterOccupiedIpsCount", afterOccupiedIps.size());
            result.put("sampleGeneratedKeys", sampleKeys.stream()
                .map(key -> Map.of(
                    "id", key.getId(),
                    "address", key.getAddress(),
                    "publicKey", key.getPublicKey().substring(0, 20) + "...",
                    "createdAt", key.getCreatedAt()
                ))
                .collect(Collectors.toList()));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("测试批量生成密钥对失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 根据节点ID生成singboxconfig.json配置
     * @param nodeId 节点ID
     * @return singboxconfig.json配置内容
     */
    @Operation(summary = "生成singboxconfig.json配置")
    @GetMapping(value = "/singbox-config/{nodeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSingboxConfigForNode(@PathVariable Long nodeId) {
        try {
            // 查询节点信息
            Node node = nodeMapper.selectById(nodeId);
            if (node == null) {
                return ResponseEntity.notFound().build();
            }

            // 查询该节点下所有wireguard_key_pool数据，按主键升序排列
            QueryWrapper<WireguardKeyPool> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("node_id", nodeId)
                       .orderByAsc("id");
            List<WireguardKeyPool> keyPools = wireguardKeyPoolMapper.selectList(queryWrapper);

            // 构建完整的singboxconfig.json
            StringBuilder config = new StringBuilder();
            config.append("{\n");
            
            // log部分
            config.append("  \"log\": {\n");
            config.append("    \"level\": \"debug\",\n");
            config.append("    \"timestamp\": true\n");
            config.append("  },\n");
            
            // inbounds部分
            config.append("  \"inbounds\": [\n");
            config.append("    {\n");
            config.append("      \"type\": \"vless\",\n");
            config.append("      \"tag\": \"reality-in\",\n");
            config.append("      \"listen\": \"::\",\n");
            config.append("      \"listen_port\": 443,\n");
            
            // users数组
            config.append("      \"users\": [\n");
            for (int i = 0; i < keyPools.size(); i++) {
                WireguardKeyPool keyPool = keyPools.get(i);
                config.append("        {\n");
                config.append("          \"uuid\": \"").append(keyPool.getUuid()).append("\",\n");
                config.append("          \"flow\": \"xtls-rprx-vision\"\n");
                config.append("        }");
                if (i < keyPools.size() - 1) {
                    config.append(",");
                }
                config.append("\n");
            }
            config.append("      ],\n");
            
            // tls配置
            config.append("      \"tls\": {\n");
            config.append("        \"enabled\": true,\n");
            config.append("        \"server_name\": \"www.cloudflare.com\",\n");
            config.append("        \"reality\": {\n");
            config.append("          \"enabled\": true,\n");
            config.append("          \"handshake\": {\n");
            config.append("            \"server\": \"www.cloudflare.com\",\n");
            config.append("            \"server_port\": 443\n");
            config.append("          },\n");
            config.append("          \"private_key\": \"oI-9YlMxv_6ihHm-Hnju6kMIObZnDyQLs1ov4NRAZWo\",\n");
            config.append("          \"short_id\": [\"4f77d10f\"]\n");
            config.append("        }\n");
            config.append("      }\n");
            config.append("    }\n");
            config.append("  ],\n");

            // outbounds数组
            config.append("  \"outbounds\": [\n");
            for (int i = 0; i < keyPools.size(); i++) {
                WireguardKeyPool keyPool = keyPools.get(i);
                config.append("    {\n");
                config.append("      \"type\": \"wireguard\",\n");
                config.append("      \"tag\": \"wg-out-").append(keyPool.getId()).append("\",\n");
                config.append("      \"private_key\": \"").append(keyPool.getPrivateKey()).append("\",\n");
                config.append("      \"local_address\": [\"").append(keyPool.getAddress()).append("\"],\n");
                config.append("      \"peers\": [\n");
                config.append("        {\n");
                config.append("          \"public_key\": \"").append(node.getWgPublicKey()).append("\",\n");
                config.append("          \"allowed_ips\": [\"0.0.0.0/0\"],\n");
                config.append("          \"server\": \"").append(node.getIp()).append("\",\n");
                config.append("          \"server_port\": ").append(node.getPort()).append("\n");
                config.append("        }\n");
                config.append("      ],\n");
                config.append("      \"mtu\": 1280\n");
                config.append("    }");
                if (i < keyPools.size() - 1) {
                    config.append(",");
                }
                config.append("\n");
            }
            
            // 添加默认的direct outbound
            if (!keyPools.isEmpty()) {
                config.append(",\n");
            }
            config.append("    {\n");
            config.append("      \"type\": \"direct\",\n");
            config.append("      \"tag\": \"default-direct\"\n");
            config.append("    }\n");
            config.append("  ],\n");

            // dns配置
            config.append("  \"dns\": {\n");
            config.append("    \"servers\": [\n");
            config.append("      {\n");
            config.append("        \"address\": \"8.8.8.8\"\n");
            config.append("      }\n");
            config.append("    ]\n");
            config.append("  },\n");

            // route rules数组
            config.append("  \"route\": {\n");
            config.append("    \"rules\": [\n");
            for (int i = 0; i < keyPools.size(); i++) {
                WireguardKeyPool keyPool = keyPools.get(i);
                config.append("      {\n");
                config.append("        \"inbound\": \"reality-in\",\n");
                config.append("        \"user\": \"").append(keyPool.getUuid()).append("\",\n");
                config.append("        \"outbound\": \"wg-out-").append(keyPool.getId()).append("\"\n");
                config.append("      }");
                if (i < keyPools.size() - 1) {
                    config.append(",");
                }
                config.append("\n");
            }
            config.append("    ]\n");
            config.append("  }\n");
            config.append("}");

            String configContent = config.toString();
            log.info("成功生成节点 {} 的singboxconfig.json配置，包含 {} 个用户配置", nodeId, keyPools.size());
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(configContent);
        } catch (Exception e) {
            log.error("生成节点 {} 的singboxconfig.json配置失败: {}", nodeId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"生成配置失败: " + e.getMessage() + "\"}");
        }
    }
} 