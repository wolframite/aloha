package com.zalora.aloha.config;

import java.util.List;
import javax.annotation.PostConstruct;
import org.jmxtrans.embedded.spring.EmbeddedJmxTransFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
public class JmxTransConfig {

    private List<String> configUrls;
    private EmbeddedJmxTransFactory embeddedJmxTransFactory;

    public void init() {
        EmbeddedJmxTransFactory factory = new EmbeddedJmxTransFactory();
//        factory.setConfigurationUrls(configUrls);

        embeddedJmxTransFactory = factory;
    }

    public EmbeddedJmxTransFactory getJmxTransFactory() {
        return embeddedJmxTransFactory;
    }
}
