package org.chaostocosmos.http.servlet;

import java.util.List;

/**
 * Servlet info bean object
 */
public class ServletBean {
    /**
     * servlet name
     */
    String servletName;
    /**
     * mapping context
     */
    String mappingContext;
    /**
     * servlet full qualified name
     */
    String servletClass;
    /**
     * servlet filter quailfied class names
     */
    List<String> servletFilterClassNames;
    /**
     * Constructor with attributes
     * 
     * @param servletName
     * @param mappingContext
     * @param servletClass
     * @param servletMethods
     */
    public ServletBean(String servletName, String mappingContext, String servletClass, List<String> servletFilterClassNames) {
        this.servletName = servletName;
        this.mappingContext = mappingContext;
        this.servletClass = servletClass;
        this.servletFilterClassNames =servletFilterClassNames;
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
    @Override
    public String toString() {
        return "{" +
            " servletName='" + servletName + "'" +
            ", servletClass='" + servletClass + "'" +
            ", servletFilterClassNames='" + servletFilterClassNames + "'" +
            "}";
    }
}

