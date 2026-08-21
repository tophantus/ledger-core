package com.example.ledgercore.common.lock;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    String[] keys();

    String prefix() default "lock:";

    long waitTime() default 3;

    TimeUnit unit() default TimeUnit.SECONDS;
}