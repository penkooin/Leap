package org.chaostocosmos.leap;

import java.net.MalformedURLException;
import java.util.List;

import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.resource.ClassUtils;
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
    public static SpringJPAManager get() throws MalformedURLException {
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
}
