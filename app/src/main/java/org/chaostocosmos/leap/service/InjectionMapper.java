package org.chaostocosmos.leap.service;

import java.lang.reflect.Field;
import java.net.MalformedURLException;

import org.chaostocosmos.leap.annotation.AutowiredJPA;
import org.chaostocosmos.leap.resource.SpringJPAManager;

/**
 * Annotaion handler
 * 
 * @author 9ins
 */
public class InjectionMapper <I> {
    /**
     * Host
     */
    String host;

    /**
     * Object to be injected
     */
    I handleObject;

    public InjectionMapper(String host, I handleObject) {
        this.host = host;
        this.handleObject = handleObject;
    }

    /**
     * Inject autowired annation field with Spring Bean object
     */
    public void injectToAutowired() throws IllegalArgumentException, IllegalAccessException, MalformedURLException {
        for(Field field : handleObject.getClass().getDeclaredFields()) {
            AutowiredJPA autowired = field.getAnnotation(AutowiredJPA.class);
            if(autowired != null) {                
                field.setAccessible(true);
                field.set(this.handleObject, SpringJPAManager.get().getBean(this.host, field.getType(), null));
            }
        }
    }
}
