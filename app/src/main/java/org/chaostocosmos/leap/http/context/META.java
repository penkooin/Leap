package org.chaostocosmos.leap.http.context;

import java.nio.file.Path;

/**
 * Metadata enum
 * 
 * @author 9ins
 */
public enum META {
    
    SERVER(Context.getHomePath().resolve("server.yml")),
    HOSTS(Context.getHomePath().resolve("hosts.yml")),
    MESSAGES(Context.getHomePath().resolve("messages.yml")),
    MIME(Context.getHomePath().resolve("mime.yml"));

    Path metaPath;
    META(Path metaPath) {
        this.metaPath = metaPath;
    }

    public Path getMetaPath() {
        return metaPath;
    }
}
