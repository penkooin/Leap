package org.chaostocosmos.leap.http.services;

import java.util.List;

import org.chaostocosmos.leap.http.filters.ILeapFilter;

/**
 * Top level of Filter
 * 
 * @author 9ins
 * @since 2021.09.17
 */
public interface IFilterOpr {
    /**
     * Add filter
     * @param filter
     * @throws Exception
     */
    public void addFilter(ILeapFilter filter) throws Exception;

    /**
     * Add all filters
     * @param filters
     * @throws Exception
     */
    public void addAllFilters(List<ILeapFilter> filters) throws Exception;

    /**
     * Remove filter
     * @param filter
     * @throws Exception
     */
    public void removeFilter(ILeapFilter filter) throws Exception;

    /**
     * Remove all filters
     * @throws Exception
     */
    public void removeAllFilters() throws Exception;
}
