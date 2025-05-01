package com.giyeon.chat_server.annotation;


import com.giyeon.chat_server.entity.ShardTarget;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Sharding {
    ShardTarget target() default ShardTarget.MESSAGE;
}

