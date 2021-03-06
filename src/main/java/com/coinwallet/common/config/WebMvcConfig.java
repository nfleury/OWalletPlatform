package com.coinwallet.common.config;

import com.coinwallet.interceptor.ContextRootInterceptor;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by liuhuan on 2018/3/27.
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {


    @Bean
    public ContextRootInterceptor recordApiLog(){
        return new ContextRootInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(recordApiLog());
        super.addInterceptors(registry);
    }
}
