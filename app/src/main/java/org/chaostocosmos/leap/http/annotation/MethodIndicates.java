package org.chaostocosmos.leap.http.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodIndicates {
    /**
     * Request type
     * @return
     */
    REQUEST_TYPE mappingMethod();

    /**
     * Servelt mapped path of uri
     * @return
     */
    String path();    
}
