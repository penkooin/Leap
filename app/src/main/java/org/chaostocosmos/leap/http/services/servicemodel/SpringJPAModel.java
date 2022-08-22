package org.chaostocosmos.leap.http.services.servicemodel;

/**
 * Spring JPA operation model
 * 
 * @author 9ins
 */
public interface SpringJPAModel {

    /**
     * Get bean object by bean name
     * @param <T>
     * @param beanName
     * @param args
     * @return
     */
    public <T> T getBean(String beanName, Object... args) throws Exception;

    /**
     * Get bean object by bean class
     * @param <T>
     * @param beanClass
     * @param args
     * @return
     */
    public <T> T getBean(Class<?> beanClass, Object... args) throws Exception;
}
