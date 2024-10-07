package org.chaostocosmos.leap.spring.datasource;

import java.util.Map;

import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.enums.DATASOURCE;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Leap data source object
 * 
 * @author 9ins
 */
public class LeapDataSource extends DriverManagerDataSource {
    /**
     * Data source id
     */
    DATASOURCE dataSourceId;

    /**
     * Default constructor
     */
    public LeapDataSource() {
        this(Context.get().server().getDataSources().get(0));
    }

    /**
     * Constructs with data source id
     * @param dataSourceId
     */
    public LeapDataSource(String dataSourceId) {
        this(Context.get().server().getDataSource(dataSourceId));
    }

    /**
     * Constructs with data source Map
     * @param dataSourceMap
     */
    public LeapDataSource(Map<String, String> dataSourceMap) {
        this(dataSourceMap.get("id"), dataSourceMap.get("schema"), dataSourceMap.get("driver-class"), dataSourceMap.get("url"), dataSourceMap.get("user"), dataSourceMap.get("password"));
    }

    /**
     * Constructs with data source id, schema, driver class name, url, user and password
     * @param dataSourceId
     * @param schema
     * @param driverClassName
     * @param url
     * @param user
     * @param password
     */
    public LeapDataSource(String dataSourceId, String schema, String driverClassName, String url, String user, String password) {
        this(DATASOURCE.valueOf(dataSourceId), schema, driverClassName, url, user, password);
    }

    /**
     * Constructs with data source id object, schema, driver class name, url, user and password
     * @param dataSourceId
     * @param schema
     * @param driverClassName
     * @param url
     * @param user
     * @param password
     */
    public LeapDataSource(DATASOURCE dataSourceId, String schema, String driverClassName, String url, String user, String password) {
        super(url, user, password);
        super.setSchema(schema);
        super.setDriverClassName(driverClassName);
        this.dataSourceId = dataSourceId;
    }   

    /**
     * Get data source id
     * @return
     */
    public String getId() {
        return this.dataSourceId.name();
    }
    
    /**
     * Data source id object
     * @return
     */
    public DATASOURCE getDATASOURCE() {
        return this.dataSourceId;
    }
}
