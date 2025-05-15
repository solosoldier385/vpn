package com.letsvpn.common.core.exception;

/**
 * 业务逻辑异常类
 * 用于封装业务处理过程中发生的、可预见的错误情况。
 */
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L; // 保持序列化兼容性

    /**
     * 错误码，可以对应HTTP状态码，或者自定义的业务错误码。
     */
    private Integer code;

    /**
     * 默认的业务错误码，例如当只提供错误消息时使用。
     * 您可以根据您的应用规范定义一个合适的默认值，例如500或一个特定的业务错误标识。
     * 这里暂时不设置默认值，code会是null，除非在构造函数中显式赋值。
     */
    // private static final Integer DEFAULT_BIZ_ERROR_CODE = 500; // 示例：默认服务器内部业务错误

    /**
     * 构造函数：只包含错误消息。
     * 错误码可以根据需要设置为null或一个默认值。
     * @param message 错误消息
     */
    public BizException(String message) {
        super(message);
        // this.code = DEFAULT_BIZ_ERROR_CODE; // 如果希望只传message时也有默认code
    }

    /**
     * 构造函数：包含错误消息和根本原因。
     * @param message 错误消息
     * @param cause 根本原因的Throwable对象
     */
    public BizException(String message, Throwable cause) {
        super(message, cause);
        // this.code = DEFAULT_BIZ_ERROR_CODE; // 如果希望默认code
    }

    /**
     * 构造函数：包含错误码和错误消息。
     * 这是为了支持在抛出异常时能明确指定错误类型的场景。
     * @param code 错误码 (例如 HTTPStatus.value())
     * @param message 错误消息
     */
    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造函数：包含错误码、错误消息和根本原因。
     * @param code 错误码
     * @param message 错误消息
     * @param cause 根本原因的Throwable对象
     */
    public BizException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * 获取错误码。
     * @return 错误码 (可能为 null，如果构造时未指定且没有设置默认值)
     */
    public Integer getCode() {
        return code;
    }

    // 通常情况下，异常的属性在构造后不应改变，所以一般不提供setter。
    // public void setCode(Integer code) {
    //     this.code = code;
    // }
}