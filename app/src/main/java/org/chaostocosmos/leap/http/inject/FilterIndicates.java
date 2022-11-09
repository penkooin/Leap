package org.chaostocosmos.leap.http.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.chaostocosmos.leap.http.service.filter.IFilter;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FilterIndicates {
    /**
     * Get pre filter class
     * @return
     */
    Class<? extends IFilter>[] preFilters() default {};

    /**
     * Get post filter class
     * @return
     */
    Class<? extends IFilter>[] postFilters() default {};
}
