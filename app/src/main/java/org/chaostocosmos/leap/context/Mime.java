package org.chaostocosmos.leap.context;

/**
 * Mime
 * 
 * @author 9ins
 */
public class Mime <T> extends Metadata <T> {

    /**
     * Constructor
     * @param mimeMap
     */
    public Mime(T mimeMap) {
        super(mimeMap);
    }

    /**
     * Get mime type value
     * @param expr
     * @return
     */
    public <V> V getMime(String mimeKey) {
        return super.getValue("mime."+mimeKey);
    }
}
