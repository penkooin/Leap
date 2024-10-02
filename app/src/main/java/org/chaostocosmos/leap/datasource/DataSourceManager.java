package org.chaostocosmos.leap.datasource;

import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.enums.DATASOURCE;
import org.chaostocosmos.leap.spring.datasource.LeapDataSource;

/**
 * DataSourceManager 
 * 
 * @author 9ins
 */
public class DataSourceManager {
    /**
     * DataSourceManager static instance
     */
    private static DataSourceManager dataSourceManager = null;    
    /**
     * Constructor
     */
    private DataSourceManager() {        
    }
    /**
     * Get DataSourceManager instance
     * @return
     */
    public static DataSourceManager get() {
        if(dataSourceManager == null) {
            dataSourceManager = new DataSourceManager();
        }
        return dataSourceManager;
    }
    /**
     * Get data source object by host object
     * @param datasource
     * @return
     */
    public LeapDataSource getDataSource(DATASOURCE datasource) {
        return getDataSources().get(datasource);
    }
    /**
     * Get data source map by host object
     * @return
     */
    public Map<DATASOURCE, LeapDataSource> getDataSources() {
        return Context.get().server().getDataSources().stream().map(dsm -> {
            DATASOURCE ds = DATASOURCE.valueOf(dsm.get("id"));
            LeapDataSource dataSource = ds.getDataSource();
            return new Object[]{ds, dataSource};
        }).collect(Collectors.toMap(k -> (DATASOURCE)k[0], v -> (LeapDataSource)v[1]));
    }
}
