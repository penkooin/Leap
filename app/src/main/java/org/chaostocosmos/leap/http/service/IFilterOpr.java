package org.chaostocosmos.leap.http.service;

import java.util.List;

import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.filter.IFilter;

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
     * @throws WASException
     */
    public void addFilter(IFilter filter) throws WASException;

    /**
     * Add all filters
     * @param filters
     * @throws WASException
     */
    public void addAllFilters(List<IFilter> filters) throws WASException;

    /**
     * Remove filter
     * @param filter
     * @throws WASException
     */
    public void removeFilter(IFilter filter) throws WASException;

    /**
     * Remove all filters
     * @throws WASException
     */
    public void removeAllFilters() throws WASException;
}
