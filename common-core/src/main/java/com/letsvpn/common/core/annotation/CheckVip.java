package com.letsvpn.common.core.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckVip {
    // 预留扩展参数，如 minLevel() 也可以加
}
