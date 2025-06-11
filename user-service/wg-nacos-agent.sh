#!/bin/bash
# --- (BEGIN) 用户需要根据实际情况修改这里的配置 ---
# WireGuard 节点的唯一标识符，必须与 user-service 中发布到 Nacos 时使用的 nodeId 一致。
# 建议通过环境变量 WG_NODE_ID 传入，例如：WG_NODE_ID=0 ./wg-nacos-agent.sh
# 如果环境变量未设置，则使用下面的默认值。
NODE_ID="${WG_NODE_ID:-0}"

# Nacos 服务器地址 (不需要 /nacos 后缀)
NACOS_SERVER_URL="http://96.9.228.173:30848/" # 例如: http://192.168.1.100:8848

# Nacos 命名空间ID (Namespace ID / Tenant ID)
# 如果使用默认的 public 命名空间，请留空。否则填写 Namespace ID。
NACOS_NAMESPACE_ID=""

# Nacos 配置的 Group
NACOS_GROUP="WIREGUARD_CONFIG"

# Nacos 配置的 Data ID 格式 (Agent 会用 NODE_ID 替换 ${NODE_ID})
NACOS_DATA_ID_TEMPLATE="wireguard.node.${NODE_ID}.conf"

# WireGuard 网络接口名称 (Agent 会用 NODE_ID 替换 ${NODE_ID})
WG_INTERFACE_NAME="wg0" # 例如: wg0, wg1

# WireGuard 配置文件在节点上的完整路径
WG_CONF_FILE="/etc/wireguard/wg0.conf"

# 轮询 Nacos 的时间间隔 (秒)
POLL_INTERVAL_SECONDS=5

# 存储上一次成功应用的配置内容的 MD5，用于比较配置是否变化
LAST_CONFIG_MD5_FILE="/tmp/wg_agent_${WG_INTERFACE_NAME}.last_md5"
# --- (END) 用户配置区域 ---

# 构造实际的 Data ID
DATA_ID="${NACOS_DATA_ID_TEMPLATE//\$\{NODE_ID\}/${NODE_ID}}" # Shell中的字符串替换

# 日志函数
log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') [Agent Node ${NODE_ID}] - $1" >&2 # 将 echo 的输出重定向到 stderr
}

# 检查依赖命令
check_dependencies() {
    local missing_deps=0
    for cmd in curl wg mkdir sudo mv echo date cat md5sum awk; do
        if ! command -v "$cmd" &> /dev/null; then
            log "错误: 核心依赖命令 '$cmd' 未找到. 请先安装."
            missing_deps=1
        fi
    done
    if [ "$missing_deps" -eq 1 ]; then
        exit 1
    fi
}

# 从 Nacos 获取配置内容
# 返回: 配置内容字符串。如果失败或内容为空，则返回空字符串并打印错误日志。
get_nacos_config_content() {
     local tenant_param=""
    if [ -n "$NACOS_NAMESPACE_ID" ]; then
        tenant_param="&tenant=${NACOS_NAMESPACE_ID}"
    fi

    # --- 优化 URL 拼接 ---
    local base_url="${NACOS_SERVER_URL%/}" # 移除 NACOS_SERVER_URL 末尾可能存在的斜杠
    local api_url="${base_url}/nacos/v1/cs/configs?dataId=${DATA_ID}&group=${NACOS_GROUP}${tenant_param}"
    # --- 优化结束 ---
    log "正在从 Nacos 获取配置: ${api_url}"

    local connect_timeout=5  # 连接超时设置为5秒
    local max_time=10        # 总操作最大允许时间设置为10秒

    local response_content
    response_content=$(curl --connect-timeout ${connect_timeout} --max-time ${max_time} -s -f -L GET "$api_url")
    local curl_exit_code=$? # 获取 curl 的退出码

    # ******** 新增/修改的关键日志输出 ********
    log "DEBUG: curl 命令已执行完毕. URL: ${api_url}"
    log "DEBUG: curl 的退出码是: ${curl_exit_code}" # 无论成功失败，都打印退出码
    # ******** 关键日志输出结束 ********

    if [ $curl_exit_code -ne 0 ]; then
        # 根据退出码给出更具体的错误提示
        log "错误: curl 从 Nacos 获取配置失败 (已记录退出码如上)." # 主错误信息
        if [ $curl_exit_code -eq 6 ]; then
            log "详细原因: 无法解析主机 (curl exit code 6)。请检查 Nacos 服务器地址是否正确以及DNS是否正常。"
        elif [ $curl_exit_code -eq 7 ]; then
            log "详细原因: 无法连接到主机 (curl exit code 7)。请检查网络连通性以及目标主机和端口是否可访问。"
        elif [ $curl_exit_code -eq 22 ]; then
            # 对于退出码22，可以尝试不用 -f 选项再请求一次以获取服务器返回的实际内容
            log "详细原因: HTTP 服务器返回错误状态码 (curl exit code 22)。可能是 404 (配置不存在) 或 5xx (服务器内部错误)。"
            log "尝试进行一次不带 -f 的 curl 以查看服务器具体响应..."
            local detailed_response
            detailed_response=$(curl --connect-timeout ${connect_timeout} --max-time ${max_time} -s -L -i GET "$api_url") # 使用 -i 查看头部
            log "不带 -f 的 curl 响应详情:\n${detailed_response}"
        elif [ $curl_exit_code -eq 28 ]; then
            log "详细原因: 操作超时 (curl exit code 28)。连接超时设置: ${connect_timeout}s, 总操作超时设置: ${max_time}s。"
        else
            log "详细原因: 其他 curl 错误，具体退出码已记录。"
        fi
        return 1 # 返回失败
    fi

    if [ -z "$response_content" ]; then
        log "警告: 从 Nacos 获取到的配置内容为空 (HTTP 200). DataId: ${DATA_ID}, Group: ${NACOS_GROUP}"
    fi
    
    echo "$response_content" # 将配置内容输出到标准输出
    return 0 # 返回成功
}




# 应用新的 WireGuard 配置
# 参数 $1: 新的配置内容字符串
apply_config() {
    local new_config_content="$1"
    local current_config_md5
    current_config_md5=$(echo -n "$new_config_content" | md5sum | awk '{print $1}')

    # 1. 检查配置是否有变化
    local last_md5=""
    if [ -f "$LAST_CONFIG_MD5_FILE" ]; then
        last_md5=$(cat "$LAST_CONFIG_MD5_FILE")
    fi

    if [ "$current_config_md5" == "$last_md5" ] && [ -f "$WG_CONF_FILE" ]; then # 增加检查WG_CONF_FILE是否存在
        log "Nacos 中的配置与本地已应用配置一致 (MD5: ${current_config_md5}). 无需更新."
        return 0
    fi

    log "检测到配置变更 (新MD5: ${current_config_md5}, 旧MD5: ${last_md5}). 准备应用..."

    # 2. 确保配置目录存在
    local wg_conf_dir
    wg_conf_dir=$(dirname "$WG_CONF_FILE")
    if [ ! -d "$wg_conf_dir" ]; then
        log "WireGuard 配置目录 ${wg_conf_dir} 不存在，尝试创建..."
        if ! sudo mkdir -p "$wg_conf_dir"; then
            log "错误: 创建 WireGuard 配置目录 ${wg_conf_dir} 失败."
            return 1
        fi
        # 设置目录权限 (可选，但推荐)
        sudo chmod 700 "$wg_conf_dir"
    fi

    # 3. 将新配置写入临时文件
    local tmp_conf_file="${WG_CONF_FILE}.tmp"
    echo "$new_config_content" > "$tmp_conf_file"
    if [ $? -ne 0 ]; then
        log "错误: 写入临时配置文件 ${tmp_conf_file} 失败."
        return 1
    fi
    log "新配置已写入临时文件: ${tmp_conf_file}"

    # 4. 原子替换配置文件
    # 使用 sudo 是因为 /etc/wireguard 通常需要 root 权限
    if sudo mv "$tmp_conf_file" "$WG_CONF_FILE"; then
        log "配置文件 ${WG_CONF_FILE} 已成功更新."
        # 设置文件权限 (确保WireGuard能读取，通常root拥有)
        sudo chmod 600 "$WG_CONF_FILE"

        # 5. 使用 wg syncconf 应用配置 (需要 sudo)
        # wg syncconf <interface> <config_file>
        # 这是推荐的非中断式更新方式
        log "准备使用 'wg syncconf' 应用配置到接口 ${WG_INTERFACE_NAME}..."
        if sudo wg syncconf "$WG_INTERFACE_NAME" "$WG_CONF_FILE"; then # L161 in your script
            log "'wg syncconf' 成功应用配置到接口 ${WG_INTERFACE_NAME}."
            # --- 修改开始: 在 wg syncconf 成功后，重启 wg-quick 服务 ---
            log "配置已通过 'wg syncconf' 应用。现在将重启 wg-quick@${WG_INTERFACE_NAME} 服务以确保所有网络规则 (如 PostUp 中的防火墙/NAT规则) 生效..."
            if sudo systemctl restart "wg-quick@${WG_INTERFACE_NAME}"; then # L165 in your script
                log "wg-quick@${WG_INTERFACE_NAME} 服务已成功重启."
                # 成功应用并重启后，更新 MD5 记录
                echo "$current_config_md5" > "$LAST_CONFIG_MD5_FILE"
            else # L169 in your script
                log "错误: 重启 wg-quick@${WG_INTERFACE_NAME} 服务失败."
                log "尽管 'wg syncconf' 可能已部分应用配置，但服务重启失败可能导致网络功能不完整。"
                # 即使重启失败，配置本身已通过syncconf写入，但PostUp/Down可能未正确执行
                # 可以考虑是否回滚，但此处仅记录错误
                return 1 # 标记为失败，因为未能完整执行预期操作
            fi # L175 in your script, closes 'if systemctl restart'
            # --- 修改结束 --- (Original L176)
        else
            # --- 这是为 'wg syncconf' 失败添加的 else 块 ---
            log "错误: 'wg syncconf' 应用配置到接口 ${WG_INTERFACE_NAME} 失败."
            log "请检查 WireGuard 服务状态和日志，以及 ${WG_CONF_FILE} 的内容和格式。"
            return 1
        fi # --- 这是为 'wg syncconf' 添加的 fi ---
    else # Original L177 in your script, 'else' for 'sudo mv'
        log "错误: 移动临时文件 ${tmp_conf_file} 到 ${WG_CONF_FILE} 失败."
        if [ -f "$tmp_conf_file" ]; then
            rm -f "$tmp_conf_file" # 清理失败的临时文件
        fi
        return 1
    fi # Original L183 in your script, closes 'if sudo mv'
    return 0
} # Original L185 in your script, closes 'apply_config' function



# --- Agent 主逻辑 ---
check_dependencies
log "启动 WireGuard Nacos Agent..."
log "节点ID (NODE_ID): ${NODE_ID}"
log "监控接口 (WG_INTERFACE_NAME): ${WG_INTERFACE_NAME}"
log "Nacos Data ID (DATA_ID): ${DATA_ID}"
log "Nacos Group: ${NACOS_GROUP}"
log "Nacos 服务器: ${NACOS_SERVER_URL}"
log "本地 WireGuard 配置文件: ${WG_CONF_FILE}"

# 首次运行时，尝试获取并应用配置
log "首次运行，尝试获取并应用初始配置..."
initial_config_content=$(get_nacos_config_content)
if [ $? -eq 0 ]; then # 检查 get_nacos_config_content 的退出码
    if [ -n "$initial_config_content" ]; then
        apply_config "$initial_config_content"
    else
        # 如果Nacos中确实没有配置，这可能是正常的初始状态
        log "Nacos 中初始配置为空或未找到。如果这是预期之外的，请检查 Nacos 配置。"
        # 如果本地有旧的MD5记录，且Nacos返回空，说明Nacos配置被清空了，可能也需要处理
        if [ -f "$LAST_CONFIG_MD5_FILE" ]; then
            log "检测到Nacos配置为空，但本地存在旧的MD5记录。考虑是否需要清空本地配置或采取其他操作。"
            # 极简版：如果Nacos空，本地WG配置保持不变，除非有明确的空配置下发逻辑
        fi
    fi
else
    log "获取初始配置失败。Agent 将在轮询中重试。"
fi


# 进入主循环，定期轮询
while true; do
    log "等待 ${POLL_INTERVAL_SECONDS} 秒后再次轮询 Nacos..."
    sleep "$POLL_INTERVAL_SECONDS"

    log "轮询 Nacos 获取配置更新..."
    current_nacos_config_content=$(get_nacos_config_content)
    if [ $? -eq 0 ]; then # 检查 get_nacos_config_content 的退出码
         # 即使内容为空字符串，也传递给 apply_config，由 apply_config 决定是否操作
        apply_config "$current_nacos_config_content"
    else
        log "本次轮询获取配置失败。将在下一轮重试。"
    fi
done