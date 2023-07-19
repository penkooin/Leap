package org.chaostocosmos.leap.enums;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.service.datasource.LeapDataSource;

/**
 * DATASOURCE enum
 * 
 * @author 9ins
 */
public enum DATASOURCE {
    MYSQL,
    ORACLE,
    MSSQL,
    POSTGRES,
    MONGODB;    
    /**
     * Get LeapDataSource object
     * @return
     */
    public LeapDataSource getDataSource() {
        return getDataSource(this);
    }
    /**
     * Get LeapDataSource object
     * @param dataSourceId
     * @return
     */
    public LeapDataSource getDataSource(DATASOURCE dataSourceId) {
        Map<String, String> map = Context.get().server().getDataSource(dataSourceId.name());
        LeapDataSource dataSource = new LeapDataSource(map);
        return dataSource;
    }  
    /**
     * Get Leap data source list
     * @return
     */
    public List<LeapDataSource> getDataSources() {
        return Context.get().server().getDataSources().stream().map(m -> new LeapDataSource(m)).collect(Collectors.toList());
    }
}
