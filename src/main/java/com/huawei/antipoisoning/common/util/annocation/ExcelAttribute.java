package com.huawei.antipoisoning.common.util.annocation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 公用接口
 *
 * @author ly
 * @since 2021-04-19
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcelAttribute {
    int columnIndex() default 0;

    ExcelColumnType columnType() default ExcelColumnType.STRING;
}
