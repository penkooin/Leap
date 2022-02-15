package org.chaostocosmos.leap.http.services;

import java.util.List;

import org.chaostocosmos.leap.http.WASException;
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
     * @throws WASException
     */
    public void addFilter(ILeapFilter filter) throws WASException;

    /**
     * Add all filters
     * @param filters
     * @throws WASException
     */
    public void addAllFilters(List<ILeapFilter> filters) throws WASException;

    /**
     * Remove filter
     * @param filter
     * @throws WASException
     */
    public void removeFilter(ILeapFilter filter) throws WASException;

    /**
     * Remove all filters
     * @throws WASException
     */
    public void removeAllFilters() throws WASException;
}
