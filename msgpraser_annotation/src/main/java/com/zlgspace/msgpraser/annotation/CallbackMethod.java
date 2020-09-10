package com.zlgspace.msgpraser.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface CallbackMethod {
    String value() default "";
    boolean intercept() default false;
}
