package org.chaostocosmos.leap.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.chaostocosmos.leap.enums.REQUEST;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodMapper {
    /**
     * Request type
     * @return
     */
    REQUEST method();    

    /**
     * Servelt mapped path of uri
     * @return
     */
    String mappingPath() default "";

    /**
     * Path of authenticated request
     */
    String[] autheticated() default {"/*"};

    /**
     * Path array for allowed request
     * @return
     */
    String[] allowed() default {};

    /**
     * Path array for forbidden request
     * @return
     */
    String[] forbidden() default {};

}

