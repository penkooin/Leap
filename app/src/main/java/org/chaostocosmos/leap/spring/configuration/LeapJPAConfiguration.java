package org.chaostocosmos.leap.spring.configuration;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.enums.DATASOURCE;
import org.chaostocosmos.leap.spring.datasource.LeapDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableJpaRepositories( basePackages = {"org.chaostocosmos.leap.service.repository"})
public class LeapJPAConfiguration {    

    @Bean
    @Qualifier("primaryDataSource")
    public LeapDataSource getDataSource() {
        return (LeapDataSource) routingDataSource().getResolvedDefaultDataSource();
    }

    @Bean
    @Qualifier("dataSources")
    public LeapDataSource[] getDataSources() {
        return routingDataSource().getResolvedDataSources().values().toArray(LeapDataSource[]::new);
    }

    @Bean
    public AbstractRoutingDataSource routingDataSource() {
        RoutingDataSource dynamicRoutingDataSource = new RoutingDataSource();
        Map<Object, Object> targetDataSources = Context.get().server().getLeapDataSources().stream().map(d -> new Object[]{DATASOURCE.valueOf(d.getId()), d}).collect(Collectors.toMap(k -> (DATASOURCE)k[0], v -> (LeapDataSource)v[1]));        
        dynamicRoutingDataSource.setTargetDataSources(targetDataSources);
        dynamicRoutingDataSource.setDefaultTargetDataSource(targetDataSources.values().iterator().next());
        dynamicRoutingDataSource.afterPropertiesSet();
        return dynamicRoutingDataSource;
    }    

    @Bean
    public JdbcTemplate jdbcTemplate(AbstractRoutingDataSource routingDataSource) {
        return new JdbcTemplate(routingDataSource);
    }    

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(AbstractRoutingDataSource dataSource) {  
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);  
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();        
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan(Context.get().server().getSpringJPAPackages().toArray(new String[0]));
        factory.setDataSource(dataSource);
        return factory;
    }
 
    @Bean
    public PersistenceAnnotationBeanPostProcessor persistenceAnnotationBeanPostProcessor() {
        PersistenceAnnotationBeanPostProcessor postProcessor = new PersistenceAnnotationBeanPostProcessor();
        postProcessor.setPersistenceUnits(Collections.emptyMap());
        return postProcessor;
    }
    
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {  
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory);
        return txManager;
    }

}