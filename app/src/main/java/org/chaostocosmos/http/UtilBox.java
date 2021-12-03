package org.chaostocosmos.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;

/**
 * UtilBox
 * @author Kooin-Shin
 * @since 2021.09.15
 */
public class UtilBox {
    /**
     * Get contents of Resource
     * @param resourceName
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static String getResourceContent(String resourceName) throws IOException, URISyntaxException {
        InputStream is = UtilBox.class.getResourceAsStream(resourceName);
        return readAllString(is);
    }
    /**
     * Read all
     * @param is
     * @return
     * @throws IOException
     */
    public static String readAllString(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
        String text = "";
        for (String line; (line = br.readLine()) != null;) {
            text += line + System.lineSeparator();
        }
        br.close();
        return text;
    }
    /**
     * Extract environment resources
     * @param docroot
     * @throws IOException
     */
    public static void extractEnvironment(Path docroot) throws IOException {
        Path WEBAPP = docroot.resolve("webapp");
        if(!WEBAPP.toFile().exists()) {
            Files.createDirectories(WEBAPP);
        }
        Path WEBINF = WEBAPP.resolve("WEB-INF");
        if(!WEBINF.toFile().exists()) {
            Files.createDirectories(WEBINF);
        }
        Path STATIC = WEBINF.resolve("static");
        if(!STATIC.toFile().exists()) {
            Files.createDirectories(STATIC);
        }
        if(!WEBAPP.resolve("trademark").toFile().exists()) {
            Files.writeString(WEBAPP.resolve("trademark"), UtilBox.readAllString(UtilBox.class.getResourceAsStream("/webapp/trademark")));
        }
        if(!WEBAPP.resolve("config.yml").toFile().exists()) {
            Files.writeString(WEBAPP.resolve("config.yml"), UtilBox.readAllString(UtilBox.class.getResourceAsStream("/webapp/config.yml")));
        }
        if(!WEBAPP.resolve("logback.xml").toFile().exists()) {
            Files.writeString(WEBAPP.resolve("logback.xml"), UtilsBox.readAllString(UtilBox.class.getResourceAsStream("/webapp/logback.xml")));
        }
        List<String> files = IOUtils.readLines(UtilBox.class.getClassLoader().getResourceAsStream("webapp/WEB-INF/static/"), Charsets.UTF_8);
        for(String s : files) {
            if(!docroot.resolve(STATIC.resolve(s)).toFile().exists()) {
                Files.writeString(docroot.resolve(STATIC.resolve(s)), UtilBox.readAllString(UtilBox.class.getResourceAsStream("/webapp/WEB-INF/static/"+s)));
            }
        }
    }
}
