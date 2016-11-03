package com.zalora.aloha.config;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Component
public class PropertyConfigurator {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.jpa.show-sql}")
    private String showSql;

    @Value("${spring.jpa.hbm2ddl-auto}")
    private String autoGenerate;

    @Value("${infinispan.cache.readthrough.entityClass}")
    private String entityClass;

    @Value("${spring.jpa.properties.hibernate.dialect}")
    private String hibernateDialect;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @PostConstruct
    public void init() {
        System.setProperty("spring.datasource.url", dbUrl);
        System.setProperty("spring.datasource.username", dbUsername);
        System.setProperty("spring.datasource.password", dbPassword);
        System.setProperty("spring.datasource.driver-class-name", driverClassName);

        System.setProperty("spring.jpa.show-sql", showSql);
        System.setProperty("spring.jpa.hbm2ddl-auto", autoGenerate);
        System.setProperty("infinispan.cache.readthrough.entityClass", entityClass);
        System.setProperty("spring.jpa.properties.hibernate.dialect", hibernateDialect);
    }

}
