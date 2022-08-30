package org.chaostocosmos.leap.http.common;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Filtering objects 
 * 
 * @author 9ins
 */
public class Filtering {

    /**
     * Filter list
     */
    List<Object> filters;

    /**
     * Creates with filters
     * @param filters
     */
    public Filtering(Object filters) {
        this.filters = ((List<?>)filters).stream().filter(o -> o != null && !o.equals("")).collect(Collectors.toList());
    }

    /**
     * Add filter pattern
     * @param pattern
     */
    public void addFilter(String pattern) {
        this.filters.add(pattern);
    }

    /**
     * Filtering resourceName
     * @param resourceName
     * @return
     */
    public boolean filter(String resourceName) {
        for(Object keyword : this.filters) {
            String regex = Arrays.asList(keyword.toString().split(Pattern.quote("*"))).stream().map(s -> s.equals("") ? "" : Pattern.quote(s)).collect(Collectors.joining(".*"))+".*";
            if(resourceName.matches(regex)) {
                return true;
            }   
        }
        return false;
    }

    /**
     * Include filtering
     * @param resourceName
     * @return
     */
    public boolean include(String resourceName) {                
        return filter(resourceName);
    }

    /**
     * Exclude filtering
     * @param resourceName
     * @return
     */
    public boolean exclude(String resourceName) {
        return !filter(resourceName);
    }

    /**
     * Get filters
     * @return
     */
    public List<Object> getFilters() {
        return this.filters;
    }    
}
