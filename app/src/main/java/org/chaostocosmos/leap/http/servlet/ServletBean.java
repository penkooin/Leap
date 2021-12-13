package org.chaostocosmos.leap.http.servlet;

import java.util.List;

/**
 * Servlet info bean object
 */
public class ServletBean {
    /**
     * servlet name
     */
    protected String servletName;

    /**
     * servlet full qualified name
     */
    protected String servletClass;

    /**
     * servlet filter quailfied class names
     */
    protected List<String> servletFilterClassNames;

    /**
     * Constructor with attributes
     * 
     * @param servletName
     * @param servletClass
     * @param servletFilterClassNames
     */
    public ServletBean(String servletName, String servletClass, List<String> servletFilterClassNames) {
        this.servletName = servletName;
        this.servletClass = servletClass;
        this.servletFilterClassNames = servletFilterClassNames;
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
            ", servletName='" + getServletName() + "'" +
            ", servletClass='" + getServletClass() + "'" +
            ", servletFilterClassNames='" + getServletFilterClassNames() + "'" +
            "}";
    }    
}

