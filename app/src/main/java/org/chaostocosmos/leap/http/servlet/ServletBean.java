package org.chaostocosmos.leap.http.servlet;

import java.util.List;
import java.util.Map;

/**
 * Servlet info bean object
 */
public class ServletBean {
    /**
     * servlet name
     */
    protected String servletName;

    /**
     * mapping context
     */
    protected String mappingContext;

    /**
     * servlet full qualified name
     */
    protected String servletClass;

    /**
     * servlet filter quailfied class names
     */
    protected List<String> servletFilterClassNames;

    /**
     * servlet methods context mapping
     */
    protected Map<String, String> servletMethodsMap;

    /**
     * Constructor with attributes
     * 
     * @param servletName
     * @param mappingContext
     * @param servletClass
     * @param servletFilterClassNames
     * @param servletMethodsMap
     */
    public ServletBean(String servletName, String mappingContext, String servletClass, List<String> servletFilterClassNames, Map<String, String> servletMethodsMap) {
        this.servletName = servletName;
        this.mappingContext = mappingContext;
        this.servletClass = servletClass;
        this.servletFilterClassNames = servletFilterClassNames;
        this.servletMethodsMap = servletMethodsMap;
    }

    /**
     * Get servlet name
     * @return
     */
    public String getServletName() {
        return this.servletName;
    }

    /**
     * Set servlet name
     * @param servletName
     */
    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    /**
     * Get mapping context
     * @return
     */
    public String getMappingContext() {
        return this.mappingContext;
    }

    /**
     * Set mapping context
     * @param mappingContext
     */
    public void setMappingContext(String mappingContext) {
        this.mappingContext = mappingContext;
    }

    /**
     * Get servlet full qualified name
     * @return
     */
    public String getServletClass() {
        return this.servletClass;
    }

    /**
     * Set servlet full qualified name
     * @param servletClass
     */
    public void setServletClass(String servletClass) {
        this.servletClass = servletClass;
    }

    /**
     * Get servlet filter class names
     * @return
     */
    public List<String> getServletFilterClassNames() {
        return this.servletFilterClassNames;
    }

    /**
     * Set servlet filter class names
     * @param servletFilterClassNames
     */
    public void setServletFilterClassNames(List<String> servletFilterClassNames) {
        this.servletFilterClassNames = servletFilterClassNames;
    }

    /**
     * Get servlet methods Map
     * @return
     */
    public Map<String, String> getServletMethodsMap() {
        return this.servletMethodsMap;
    }

    /**
     * Set servlet methods Map
     * @param servletMethodsMap
     */
    public void setServletMethodsMap(Map<String, String> servletMethodsMap) {
        this.servletMethodsMap = servletMethodsMap;
    }

    @Override
    public String toString() {
        return "{" +
            ", servletName='" + getServletName() + "'" +
            ", mappingContext='" + getMappingContext() + "'" +
            ", servletClass='" + getServletClass() + "'" +
            ", servletFilterClassNames='" + getServletFilterClassNames() + "'" +
            ", servletMethodsMap='" + getServletMethodsMap() + "'" +
            "}";
    }    
}

