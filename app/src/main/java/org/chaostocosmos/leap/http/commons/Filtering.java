package org.chaostocosmos.leap.http.commons;

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
    List<String> filters;

    /**
     * Creates with filters
     * @param filters
     */
    public Filtering(List<String> filters) {
        this.filters = filters;
    }

    /**
     * Filtering resourceName
     * @param resourceName
     * @return
     */
    public boolean filter(String resourceName) {
        for(String keyword : this.filters) {
            String regex = Arrays.asList(keyword.split(Pattern.quote("*"))).stream().map(s -> s.equals("") ? "" : Pattern.quote(s)).collect(Collectors.joining(".*"))+".*";
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
    public List<String> getFilters() {
        return this.filters;
    }    
}
