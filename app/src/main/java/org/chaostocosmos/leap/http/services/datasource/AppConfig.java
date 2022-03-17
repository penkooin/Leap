package org.chaostocosmos.leap.http.services.datasource;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = {"org.chaostocosmos.leap.http.services", 
"org.chaostocosmos.leap.http.services.datasource",
"org.chaostocosmos.leap.http.services.entiry",
"org.chaostocosmos.leap.http.services.repository"})
public class AppConfig {

    @Bean
    public DataSource getDataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.mariadb.jdbc.Driver");
        dataSourceBuilder.url("jdbc:mariadb://localhost:3306/leap");
        dataSourceBuilder.username("root");
        dataSourceBuilder.password("9292");
        return dataSourceBuilder.build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {  
      HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
      vendorAdapter.setGenerateDdl(true);  
      LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
      factory.setPersistenceUnitName("leap");
      factory.setJpaVendorAdapter(vendorAdapter);
      factory.setPackagesToScan("org.chaostocosmos.leap.http.services", 
      "org.chaostocosmos.leap.http.services.datasource",
      "org.chaostocosmos.leap.http.services.entiry",
      "org.chaostocosmos.leap.http.services.repository");
      factory.setDataSource(getDataSource());
      return factory;
    }
  
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {  
      JpaTransactionManager txManager = new JpaTransactionManager();
      txManager.setEntityManagerFactory(entityManagerFactory);
      return txManager;
    }
}