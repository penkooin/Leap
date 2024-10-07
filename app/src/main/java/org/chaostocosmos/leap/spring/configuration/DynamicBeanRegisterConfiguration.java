package org.chaostocosmos.leap.spring.configuration;

import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.chaostocosmos.leap.datasource.DataSourceManager;
import org.chaostocosmos.leap.spring.SpringJPAManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.lang.NonNull;

/**
 * Dynamic bean registration object
 * 
 * @author 9ins
 */
public class DynamicBeanRegisterConfiguration implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
        AnnotationConfigApplicationContext applicationContext = SpringJPAManager.get().getApplicationContext();
        Map<String, GenericBeanDefinition> dynamicBeanDefinitions = createDynamicDataSourceBeanDefinitions();        
        for(Map.Entry<String, GenericBeanDefinition> entry : dynamicBeanDefinitions.entrySet()) {
            applicationContext.registerBeanDefinition(entry.getKey(), entry.getValue());
        }
        applicationContext.refresh();
    }

    /**
     * Create dynamic DataSource beans 
     * @return
     */
    public Map<String, GenericBeanDefinition> createDynamicDataSourceBeanDefinitions() {
        return DataSourceManager.get().getDataSources().entrySet().stream().map(e -> {
            DataSource dataSource = e.getValue();            
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(dataSource.getClass());
            return new Object[] {e.getKey().name(), beanDefinition};
        }).collect(Collectors.toMap(k -> (String) k[0], v -> (GenericBeanDefinition) v[1]));
    }        
    
}
