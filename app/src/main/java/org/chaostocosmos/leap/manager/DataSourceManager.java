package org.chaostocosmos.leap.manager;

public class DataSourceManager {
    
    private static DataSourceManager dataSourceManager = null;    

    public DataSourceManager() {
        initialize();
    }

    public static DataSourceManager get() {
        if(dataSourceManager == null) {
            dataSourceManager = new DataSourceManager();
        }
        return dataSourceManager;
    }

    private void initialize() {

    }

}
