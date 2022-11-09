package org.chaostocosmos.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
/**
 * Config mapping for field
 * 
 * @author 9ins
 */
public @interface MetaField {

    /**
     * Metadata path expression
     * @return
     */
    String expr();      
}
