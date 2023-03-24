package org.chaostocosmos.leap.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.chaostocosmos.leap.security.IAuthenticate;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthenticateMapper {
    /**
     * Class specifying for authentication
     */
    Class<? extends IAuthenticate> authClass(); 
}
