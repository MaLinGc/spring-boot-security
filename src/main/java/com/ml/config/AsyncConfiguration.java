package com.ml.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfiguration implements AsyncConfigurer {

    private final Logger log = LoggerFactory.getLogger(AsyncConfiguration.class);

    private final ApplicationProperties applicationProperties;

    public AsyncConfiguration(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        log.debug("Creating Async Task Executor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(applicationProperties.getAsync().getCorePoolSize());
        executor.setMaxPoolSize(applicationProperties.getAsync().getMaxPoolSize());
        executor.setQueueCapacity(applicationProperties.getAsync().getQueueCapacity());
        executor.setThreadNamePrefix("jhipster-Executor-");
        return new ExceptionHandlingAsyncTaskExecutor(executor);
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }

    private class ExceptionHandlingAsyncTaskExecutor implements AsyncTaskExecutor, InitializingBean, DisposableBean {

        private final Logger log = LoggerFactory.getLogger(ExceptionHandlingAsyncTaskExecutor.class);

        private final AsyncTaskExecutor executor;

        public ExceptionHandlingAsyncTaskExecutor(AsyncTaskExecutor executor) {
            this.executor = executor;
        }

        @Override
        public void execute(Runnable task) {
            this.executor.execute(this.createWrappedRunnable(task));
        }

        @Override
        public void execute(Runnable task, long startTimeout) {
            this.executor.execute(this.createWrappedRunnable(task), startTimeout);
        }

        private <T> Callable<T> createCallable(Callable<T> task) {
            return () -> {
                try {
                    return task.call();
                } catch (Exception var3) {
                    this.handle(var3);
                    throw var3;
                }
            };
        }

        private Runnable createWrappedRunnable(Runnable task) {
            return () -> {
                try {
                    task.run();
                } catch (Exception var3) {
                    this.handle(var3);
                }

            };
        }

        protected void handle(Exception e) {
            this.log.error("Caught async exception", e);
        }

        @Override
        public Future<?> submit(Runnable task) {
            return this.executor.submit(this.createWrappedRunnable(task));
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            return this.executor.submit(this.createCallable(task));
        }

        @Override
        public void destroy() throws Exception {
            if(this.executor instanceof DisposableBean) {
                DisposableBean bean = (DisposableBean)this.executor;
                bean.destroy();
            }

        }

        @Override
        public void afterPropertiesSet() throws Exception {
            if(this.executor instanceof InitializingBean) {
                InitializingBean bean = (InitializingBean)this.executor;
                bean.afterPropertiesSet();
            }

        }
    }
}
