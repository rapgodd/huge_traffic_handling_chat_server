package com.giyeon.chat_server.aspect;

import com.giyeon.chat_server.annotation.Sharding;
import com.giyeon.chat_server.entity.ShardTarget;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ShardAspect {

    private static final ThreadLocal<Long> idThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<ShardTarget> shardTargetThreadLocal=new ThreadLocal<>();


    @Around("@annotation(sharding)")
    public Object handleShard(ProceedingJoinPoint joinPoint, Sharding sharding) throws Throwable {

        Object[] args = joinPoint.getArgs();
        Long id = Long.valueOf(String.valueOf(args[0]));

        idThreadLocal.set(id);
        shardTargetThreadLocal.set(sharding.target());

        try{
            return joinPoint.proceed();
        }finally {
            shardTargetThreadLocal.remove();
            idThreadLocal.remove();
        }
    }


    public static Long getCurrentId(){
        return idThreadLocal.get();
    }

}
