package org.chaostocosmos.leap.http;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * Constants
 */
public class Constants {
    /**
     * separator for string property value
     */
    public static final String PROPERTY_SEPARATOR = ",";

    /**
     * default leap home path
     */
    public static final Path DEFAULT_HOME_PATH = Paths.get("./");

    /**
     * default buffer size
     */
    public static final int DEFAULT_BUFFER_SIZE = 1024;

    /**
     * body part max size limit
     */
    public static final int BODY_MAX_LIMIT = 1024 * 1000 * 1;

    /**
     * Password vaildation pattern
     */
    public static final Pattern PASSWORD_REGEX = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");
}
