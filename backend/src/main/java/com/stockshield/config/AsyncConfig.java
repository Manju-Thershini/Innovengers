package com.stockshield.config;
import org.springframework.context.annotation.*; import java.util.concurrent.*;
@Configuration public class AsyncConfig { @Bean(destroyMethod="shutdown") public ExecutorService orderExecutor(){ return new ThreadPoolExecutor(20,100,60,TimeUnit.SECONDS,new ArrayBlockingQueue<>(1000),new ThreadPoolExecutor.CallerRunsPolicy()); } }
