#!/bin/bash
# --- (BEGIN) 用户需要根据实际情况修改这里的配置 ---
# WireGuard 节点的唯一标识符，必须与 user-service 中的 nodeId 一致。
# 建议通过环境变量 WG_NODE_ID 传入，例如：WG_NODE_ID=0 ./wg-keypool-sync.sh
# 如果环境变量未设置，则使用下面的默认值。
NODE_ID="${WG_NODE_ID:-0}"

# User Service API 服务器地址
USER_SERVICE_URL="http://localhost:8080" # 例如: http://192.168.1.100:8080

# User Service API 路径
API_PATH="/v1/internal/wireguard-key-pool/config/${NODE_ID}"

# WireGuard 网络接口名称
WG_INTERFACE_NAME="wg0" # 例如: wg0, wg1

# WireGuard 配置文件在节点上的完整路径
WG_CONF_FILE="/etc/wireguard/wg0.conf"

# 同步间隔 (秒) - 24小时 = 86400秒
SYNC_INTERVAL_SECONDS=86400

# 存储上一次成功应用的配置内容的 MD5，用于比较配置是否变化
LAST_CONFIG_MD5_FILE="/tmp/wg_keypool_sync_${WG_INTERFACE_NAME}.last_md5"

# 日志文件路径
LOG_FILE="/var/log/wg-keypool-sync.log"
# --- (END) 用户配置区域 ---

# 日志函数
log() {
    local message="$(date '+%Y-%m-%d %H:%M:%S') [KeyPool Sync Node ${NODE_ID}] - $1"
    echo "$message" >&2
    echo "$message" >> "$LOG_FILE"
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

# 从 User Service API 获取配置内容
# 返回: 配置内容字符串。如果失败或内容为空，则返回空字符串并打印错误日志。
get_keypool_config_content() {
    local api_url="${USER_SERVICE_URL}${API_PATH}"
    log "正在从 User Service API 获取配置: ${api_url}"

    local connect_timeout=10  # 连接超时设置为10秒
    local max_time=30        # 总操作最大允许时间设置为30秒

    local response_content
    response_content=$(curl --connect-timeout ${connect_timeout} --max-time ${max_time} -s -f -L GET "$api_url")
    local curl_exit_code=$?

    log "DEBUG: curl 命令已执行完毕. URL: ${api_url}"
    log "DEBUG: curl 的退出码是: ${curl_exit_code}"

    if [ $curl_exit_code -ne 0 ]; then
        log "错误: curl 从 User Service API 获取配置失败 (退出码: ${curl_exit_code})."
        if [ $curl_exit_code -eq 6 ]; then
            log "详细原因: 无法解析主机。请检查 User Service 服务器地址是否正确以及DNS是否正常。"
        elif [ $curl_exit_code -eq 7 ]; then
            log "详细原因: 无法连接到主机。请检查网络连通性以及目标主机和端口是否可访问。"
        elif [ $curl_exit_code -eq 22 ]; then
            log "详细原因: HTTP 服务器返回错误状态码。可能是 404 (配置不存在) 或 5xx (服务器内部错误)。"
            log "尝试进行一次不带 -f 的 curl 以查看服务器具体响应..."
            local detailed_response
            detailed_response=$(curl --connect-timeout ${connect_timeout} --max-time ${max_time} -s -L -i GET "$api_url")
            log "不带 -f 的 curl 响应详情:\n${detailed_response}"
        elif [ $curl_exit_code -eq 28 ]; then
            log "详细原因: 操作超时。连接超时设置: ${connect_timeout}s, 总操作超时设置: ${max_time}s。"
        else
            log "详细原因: 其他 curl 错误，具体退出码已记录。"
        fi
        return 1
    fi

    if [ -z "$response_content" ]; then
        log "警告: 从 User Service API 获取到的配置内容为空 (HTTP 200)."
    fi
    
    echo "$response_content"
    return 0
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

    if [ "$current_config_md5" == "$last_md5" ] && [ -f "$WG_CONF_FILE" ]; then
        log "User Service API 中的配置与本地已应用配置一致 (MD5: ${current_config_md5}). 无需更新."
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
    if sudo mv "$tmp_conf_file" "$WG_CONF_FILE"; then
        log "配置文件 ${WG_CONF_FILE} 已成功更新."
        sudo chmod 600 "$WG_CONF_FILE"

        # 5. 使用 wg syncconf 应用配置
        log "准备使用 'wg syncconf' 应用配置到接口 ${WG_INTERFACE_NAME}..."
        if sudo wg syncconf "$WG_INTERFACE_NAME" "$WG_CONF_FILE"; then
            log "'wg syncconf' 成功应用配置到接口 ${WG_INTERFACE_NAME}."
            
            # 重启 wg-quick 服务以确保所有网络规则生效
            log "配置已通过 'wg syncconf' 应用。现在将重启 wg-quick@${WG_INTERFACE_NAME} 服务以确保所有网络规则生效..."
            if sudo systemctl restart "wg-quick@${WG_INTERFACE_NAME}"; then
                log "wg-quick@${WG_INTERFACE_NAME} 服务已成功重启."
                # 成功应用并重启后，更新 MD5 记录
                echo "$current_config_md5" > "$LAST_CONFIG_MD5_FILE"
            else
                log "错误: 重启 wg-quick@${WG_INTERFACE_NAME} 服务失败."
                log "尽管 'wg syncconf' 可能已部分应用配置，但服务重启失败可能导致网络功能不完整。"
                return 1
            fi
        else
            log "错误: 'wg syncconf' 应用配置到接口 ${WG_INTERFACE_NAME} 失败."
            log "请检查 WireGuard 服务状态和日志，以及 ${WG_CONF_FILE} 的内容和格式。"
            return 1
        fi
    else
        log "错误: 移动临时文件 ${tmp_conf_file} 到 ${WG_CONF_FILE} 失败."
        if [ -f "$tmp_conf_file" ]; then
            rm -f "$tmp_conf_file"
        fi
        return 1
    fi
    return 0
}

# 初始化日志文件
init_log() {
    local log_dir
    log_dir=$(dirname "$LOG_FILE")
    if [ ! -d "$log_dir" ]; then
        sudo mkdir -p "$log_dir"
        sudo chmod 755 "$log_dir"
    fi
    touch "$LOG_FILE"
    chmod 644 "$LOG_FILE"
}

# --- Agent 主逻辑 ---
init_log
check_dependencies
log "启动 WireGuard KeyPool 同步服务..."
log "节点ID (NODE_ID): ${NODE_ID}"
log "监控接口 (WG_INTERFACE_NAME): ${WG_INTERFACE_NAME}"
log "User Service API: ${USER_SERVICE_URL}${API_PATH}"
log "本地 WireGuard 配置文件: ${WG_CONF_FILE}"
log "同步间隔: ${SYNC_INTERVAL_SECONDS} 秒 (${SYNC_INTERVAL_SECONDS}/3600 小时)"

# 首次运行时，尝试获取并应用配置
log "首次运行，尝试获取并应用初始配置..."
initial_config_content=$(get_keypool_config_content)
if [ $? -eq 0 ]; then
    if [ -n "$initial_config_content" ]; then
        apply_config "$initial_config_content"
    else
        log "User Service API 中初始配置为空或未找到。如果这是预期之外的，请检查 API 配置。"
        if [ -f "$LAST_CONFIG_MD5_FILE" ]; then
            log "检测到API配置为空，但本地存在旧的MD5记录。考虑是否需要清空本地配置或采取其他操作。"
        fi
    fi
else
    log "获取初始配置失败。服务将在下一轮同步中重试。"
fi

# 进入主循环，定期同步
while true; do
    log "等待 ${SYNC_INTERVAL_SECONDS} 秒后进行下一次同步..."
    sleep "$SYNC_INTERVAL_SECONDS"

    log "开始同步 KeyPool 配置..."
    current_config_content=$(get_keypool_config_content)
    if [ $? -eq 0 ]; then
        apply_config "$current_config_content"
    else
        log "本次同步获取配置失败。将在下一轮重试。"
    fi
done 