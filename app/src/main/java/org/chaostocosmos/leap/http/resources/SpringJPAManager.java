package org.chaostocosmos.leap.http.resources;

import java.util.List;

import org.chaostocosmos.leap.http.services.SimpleJPAService;
import org.chaostocosmos.leap.http.services.entity.Users;
import org.chaostocosmos.leap.http.services.repository.IUsersRespository;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * SpringJPAConfiguration 
 * 
 * @author 9ins
 */
public class SpringJPAManager <T> implements ApplicationListener {

    /**
     * List of being scanned
     */
    List<String> scanPackages;

    /**
     * Spring annotation context object
     */
    AnnotationConfigApplicationContext jpaContext;

    /**
     * Create object
     * @param scanPackages
     * @param classLoader
     */
    public SpringJPAManager(String[] scanPackages, ClassLoader classLoader) {
        this.jpaContext = new AnnotationConfigApplicationContext();
        this.jpaContext.addApplicationListener(this);
        this.jpaContext.setClassLoader(classLoader);
        this.jpaContext.scan(scanPackages);
        this.jpaContext.refresh();       
    }

    public AnnotationConfigApplicationContext getSpringContext() {
        return this.jpaContext;
    }

    public Object getBean(String name, Object ... params) {
        return this.jpaContext.getBean(name, params);
    }

    public T getBean(Class<T> clazz, Object ... params) {
        return this.jpaContext.getBean(clazz, params);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        System.out.println(event.getSource());
    }

    public static class JPAConfiguration {

        JPAConfiguration(String[] scanPackages) {}
    }

    public static void main(String[] args) {
        String[] packages = {
            "org.chaostocosmos.leap.http.services", 
            "org.chaostocosmos.leap.http.services.datasource",
            "org.chaostocosmos.leap.http.services.entiry",
            "org.chaostocosmos.leap.http.services.repository"
        };
        SpringJPAManager<SimpleJPAService> config = new SpringJPAManager<SimpleJPAService> (packages, SimpleJPAService.class.getClassLoader());
        AnnotationConfigApplicationContext ctx = config.getSpringContext();
        SimpleJPAService usersService = config.getBean(SimpleJPAService.class, new Object[0]);
        usersService.getUsers(null, null);
        IUsersRespository repo = ctx.getBean(IUsersRespository.class, new Object[0]);
        Users users = repo.findByName("Kooin-Shin");
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ "+users.toString());
    }
}
