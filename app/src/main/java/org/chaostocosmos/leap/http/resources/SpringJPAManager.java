package org.chaostocosmos.leap.http.resources;

import java.net.MalformedURLException;

import org.chaostocosmos.leap.http.context.Hosts;
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
     * Hosts manager
     */
    Hosts<?> hosts;

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
     * @param hosts
     * @param classLoader
     */
    private SpringJPAManager(Hosts<?> hosts, ClassLoader classLoader) {
        this.hosts = hosts;        
        jpaContext = new AnnotationConfigApplicationContext();
        jpaContext.setClassLoader(classLoader);
        jpaContext.scan(this.hosts.getAllSpringPackages().toArray(new String[0]));
        jpaContext.refresh();  
    }

    /**
     * Get SpringJPAManager instance
     * @return
     * @throws MalformedURLException
     */
    public static SpringJPAManager get() throws MalformedURLException {
        if(springJpaManager == null) {
            //springJpaManager = new SpringJPAManager(HostsManager.get(), ClassUtils.getClassLoader());
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
     * @param host
     * @param beanName
     * @param params
     * @return
     */
    public Object getBean(String host, String beanName, Object ... params) {
        Object bean = jpaContext.getBean(beanName, params);
        if(this.hosts.filteringSpringJPAPackages(host, bean.getClass().getName())) {
            return bean;
        }
        return null;
    }

    /**
     * Get Bean by class object
     * @param host
     * @param clazz
     * @param params
     * @return
     */
    public Object getBean(String host, Class<?> clazz, Object ... params) {
        if(this.hosts.filteringSpringJPAPackages(host, clazz.getName())) {
            return jpaContext.getBean(clazz, params);
        }
        return null;        
    }
}
