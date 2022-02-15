package org.chaostocosmos.leap.http.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.chaostocosmos.leap.http.filters.ILeapFilter;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FilterMapper {
    /**
     * Get pre filter class
     * @return
     */
    Class<? extends ILeapFilter>[] preFilters() default {};

    /**
     * Get post filter class
     * @return
     */
    Class<? extends ILeapFilter>[] postFilters() default {};
}
