package org.chaostocosmos.leap.http.context;

/**
 * Mime
 * 
 * @author 9ins
 */
public class Mime <M> extends Metadata <M> {

    M mimeMap;

    /**
     * Constructor
     * @param mimeMap
     */
    public Mime(M mimeMap) {
        super(mimeMap);
    }

    /**
     * Get mime type value
     * @param expr
     * @return
     */
    public String getMime(String mimeKey) {
        return super.<String>getValue("mime."+mimeKey);
    }
}
