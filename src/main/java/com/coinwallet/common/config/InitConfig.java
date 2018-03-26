package com.coinwallet.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 *
 */
@Configuration
public class InitConfig {

    @Value("${aes.deskey}")
    public String deskey;

    @Value("${node.main.url}")
    public String nodeMainUrl;

    @Value("${gas.name}")
    public String gasName;



}
