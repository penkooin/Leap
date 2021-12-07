package org.chaostocosmos.leap.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * UtilBox
 * @author Kooin-Shin
 * @since 2021.09.15
 */
public class UtilBox {
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
     * Compare two file is same
     * @param file
     * @param file1
     * @return
     */
    public static boolean isFileSame(File file, File file1) {
        return isFileSame(file.lastModified(), file1.lastModified());
    } 

    /**
     * Compare two modified millis
     * @param modi
     * @param modi1
     * @return
     */
    public static boolean isFileSame(long modi, long modi1) {
        return modi == modi1;
    }
}
