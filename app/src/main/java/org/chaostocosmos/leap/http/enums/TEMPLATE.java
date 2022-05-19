package org.chaostocosmos.leap.http.enums;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.context.Context;

/**
 * TEMPLATE
 * @author 9ins
 */
public enum TEMPLATE {

    ERROR(Context.getTemplatePath().resolve("error.html")), 
    RESOURCE(Context.getTemplatePath().resolve("resource.html")),
    RESPONSE(Context.getTemplatePath().resolve("response.html"));

    Map<TEMPLATE, byte[]> templateMap = new HashMap<>();
    Path path;

    TEMPLATE(Path path) {
        this.path = path;
        if(!templateMap.containsKey(this)) {
            try {
                byte[] bytes = Files.readAllBytes(path);
                templateMap.put(this, bytes);
            } catch (IOException e) {
                throw new WASException(MSG_TYPE.ERROR, 0, e.getMessage());
            }
        }
    }

    /**
     * Get template page with charset
     * @param template
     * @param charset
     * @return
     */
    public String getTemplatePage(TEMPLATE template, Charset charset) {
        return new String(templateMap.get(template), charset);
    }
    
    /**
     * Get template bytes
     * @param template
     * @return
     */
    public byte[] getTemplateBytes(TEMPLATE template) {
        return templateMap.get(template);
    }
}
