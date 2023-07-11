package org.chaostocosmos.leap.manager;

import java.net.MalformedURLException;
import java.util.List;

import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.resource.ClassUtils;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * SpringJPAConfiguration 
 * 
 * @author 9ins
 */
public class SpringJPAManager {

    /**
     * Spring annotation context object
     */
    AnnotationConfigApplicationContext jpaContext;

    /**
     * Leap class loader
     */
    ClassLoader classLoader;

    /**
     * Spring JPA Manager object
     */
    private static SpringJPAManager springJpaManager = null;

    /**
     * Create with HostsManager, ClassLoader
     * @param classLoader
     */
    private SpringJPAManager(ClassLoader classLoader) {
        jpaContext = new AnnotationConfigApplicationContext();
        jpaContext.setClassLoader(classLoader);
        jpaContext.scan(Context.get().server().<List<String>>getSpringJPAPackage().toArray(new String[0]));
        jpaContext.refresh();  
    }

    /**
     * Get SpringJPAManager instance
     * @return
     * @throws MalformedURLException
     */
    public static SpringJPAManager get() {
        if(springJpaManager == null) {
            springJpaManager = new SpringJPAManager(ClassUtils.getClassLoader());
        }
        return springJpaManager;
    }

    /**
     * Get spring AnnotationConfigApplicationContext
     * @return
     */
    public AnnotationConfigApplicationContext getSpringContext() {
        return jpaContext;
    }

    /**
     * Get bean by name
     * @param beanName
     * @param args
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(String beanName, Object ... args) {
        return (T)jpaContext.getBean(beanName, args);
    }

    /**
     * Get Bean by class object
     * @param clazz
     * @param args
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<?> clazz, Object ... args) {
        return (T)jpaContext.getBean(clazz, args);
    }    

    /**
     * Inject custom created bean's @Autowired to be injected to it's field.
     * @param <T>
     * @param bean
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T injectToAutoWired(T bean) {
        AutowireCapableBeanFactory factory = this.jpaContext.getAutowireCapableBeanFactory();
        factory.autowireBean(bean);
        return (T) factory.initializeBean(bean, bean.getClass().getCanonicalName());
    }
}
