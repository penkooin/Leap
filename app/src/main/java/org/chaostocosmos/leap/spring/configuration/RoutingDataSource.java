package org.chaostocosmos.leap.spring.configuration;

import org.chaostocosmos.leap.enums.DATASOURCE;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * RoutingDataSource
 * 
 * @author 9ins
 */
public class RoutingDataSource extends AbstractRoutingDataSource {

    /**
     * Data source key
     */
    private static final ThreadLocal<DATASOURCE> DATA_SOURCE_KEY = new ThreadLocal<>();

    /**
     * Set data source key
     * @param dataSourceKey
     */
    public static void setDataSourceKey(DATASOURCE dataSourceKey) {        
        DATA_SOURCE_KEY.set(dataSourceKey);
    }

    /**
     * Get data source key
     */
    public static DATASOURCE getDataSourceKey() {
        return DATA_SOURCE_KEY.get();
    }    

    /**
     * Clear data source key
     */
    public static void clearDataSourceKey() {
        DATA_SOURCE_KEY.remove();
    }        

    @Override
    protected Object determineCurrentLookupKey() {
        return getDataSourceKey();
    }
}