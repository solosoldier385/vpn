package com.letsvpn.common.core.response;

import lombok.Data;
import lombok.NoArgsConstructor; // 可以添加一个无参构造函数
import lombok.AllArgsConstructor; // 可以添加一个全参构造函数以便更灵活地创建实例

@Data
@NoArgsConstructor // 添加无参构造，Lombok会生成它
// @AllArgsConstructor // 如果需要，也可以添加全参构造
public class R<T> {
    private Integer code;
    private String msg;
    private T data;

    // 私有构造函数，强制使用静态工厂方法
    private R(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // --- Success Methods ---
    public static <T> R<T> success() {
        return new R<>(200, "success", null);
    }

    public static <T> R<T> success(T data) {
        return new R<>(200, "success", data);
    }

    public static <T> R<T> success(String msg, T data) {
        return new R<>(200, msg, data);
    }


    // --- Fail Methods ---
    public static <T> R<T> fail() {
        return new R<>(500, "fail", null);
    }

    public static <T> R<T> fail(String msg) {
        return new R<>(500, msg, null);
    }

    /**
     * 返回失败结果，可以指定状态码和消息
     * @param code 状态码
     * @param msg 错误消息
     * @return R
     * @param <T>
     */
    public static <T> R<T> fail(Integer code, String msg) {
        return new R<>(code, msg, null);
    }

    /**
     * 返回失败结果，可以指定状态码、消息和数据（尽管失败时通常不带数据）
     * @param code 状态码
     * @param msg 错误消息
     * @param data 附带的数据
     * @return R
     * @param <T>
     */
    public static <T> R<T> fail(Integer code, String msg, T data) {
        return new R<>(code, msg, data);
    }

    public static boolean isSuccess(Integer code) {
        return code == 200;
    }
}