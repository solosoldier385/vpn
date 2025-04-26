package com.letsvpn.common.core.exception;

import com.letsvpn.common.core.response.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public R<String> handleBizException(BizException e) {
        return R.fail(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public R<String> handleException(Exception e) {
        return R.fail("服务器内部异常");
    }
}