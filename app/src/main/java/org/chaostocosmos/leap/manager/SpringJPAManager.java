package org.chaostocosmos.leap.manager;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManagerFactory;

import org.chaostocosmos.leap.common.ClassUtils;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.service.datasource.LeapDataSource;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

/**
 * SpringJPAConfiguration 
 * 
 * @author 9ins
 */
public class SpringJPAManager {
    /**
     * Spring annotation context object
     */
    AnnotationConfigApplicationContext applicationContext;
    /**
     * Leap class loader
     */
    ClassLoader classLoader;
    /**
     * Spring JPA Manager object
     */
    private static SpringJPAManager springJpaManager = null;
    /**
     * Get SpringJPAManager instance
     * @return
     */
    public static SpringJPAManager get() {
        if(springJpaManager == null) {
            springJpaManager = new SpringJPAManager(ClassUtils.getClassLoader());
        }
        return springJpaManager;
    }
    /**
     * Create with HostsManager, ClassLoader
     * @param classLoader
     */
    private SpringJPAManager(ClassLoader classLoader) {
        this.applicationContext = new AnnotationConfigApplicationContext();
        this.applicationContext.setClassLoader(classLoader);
        this.applicationContext.scan(Context.get().server().<List<String>>getSpringJPAPackage().toArray(new String[0]));
        this.applicationContext.refresh();  
    }
    /**
     * Create entity manager factory bean
     * @param dataSource
     * @return
     */
    public LocalContainerEntityManagerFactoryBean createEntityManagerFactoryBean(LeapDataSource dataSource) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        //factory.setPackagesToScan(Context.get().server().<List<String>> getSpringJPAPackage().toArray(new String[0]));
        factory.setPackagesToScan("org.chaostocosmos.leap.service");
        factory.setPersistenceUnitName(dataSource.getId());
        factory.setJpaVendorAdapter(vendorAdapter);
        //factory.afterPropertiesSet();
        return factory;
    }
    /**
     * Create dynamic DataSource beans 
     * @return
     */
    public List<GenericBeanDefinition> createDynamicDataSourceBeanDefinitions(List<LeapDataSource> dataSources) {
        return dataSources.stream().map(ds -> {
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(ds.getClass());
            beanDefinition.setFactoryBeanName(ds.getId());
            return beanDefinition;
        }).collect(Collectors.toList());
    }        
    /**
     * Get spring AnnotationConfigApplicationContext
     * @return
     */
    public AnnotationConfigApplicationContext getApplicationContext() {
        return applicationContext;
    }
    /**
     * Get bean by name
     * @param beanName
     * @param args
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(String beanName, Object ... args) {
        return (T)applicationContext.getBean(beanName, args);
    }
    /**
     * Get Bean by class object
     * @param clazz
     * @param args
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<?> clazz, Object ... args) {
        return (T)applicationContext.getBean(clazz, args);
    }    
    /**
     * Inject custom created bean's @Autowired to be injected to it's field.
     * @param <T>
     * @param bean
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T injectToAutoWired(T bean) {
        AutowireCapableBeanFactory factory = this.applicationContext.getAutowireCapableBeanFactory();
        factory.autowireBean(bean);
        return (T) factory.initializeBean(bean, bean.getClass().getCanonicalName());
    }
    /**
     * Close Spring application context object
     */
    public void close() {
        this.applicationContext.close();
    }
}
