package org.chaostocosmos.leap.annotation;

import java.util.Map;

import javax.sql.DataSource;

import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * DATASOURCE enum
 * 
 * @author 9ins
 */
public enum DATASOURCE {
    MYSQL,
    ORACLE,
    POSTGRES,
    MONGODB;

    /**
     * Get DataSource object
     * @param hostId
     * @return
     */
    public DataSource getDataSource(String hostId) {
        return getDataSource(Context.get().host(hostId));
    }

    /**
     * Get DataSource object
     * @param hostId
     * @param dataSourceId
     * @return
     */
    public DataSource getDataSource(Host<?> host) {
        Map<String, String> map = host.getDataSource(super.name());
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(map.get("driver-class"));
        dataSource.setUrl(map.get("url"));
        dataSource.setUsername(map.get("user"));
        dataSource.setPassword(map.get("passwored"));                        
        return dataSource;
    }    
}
