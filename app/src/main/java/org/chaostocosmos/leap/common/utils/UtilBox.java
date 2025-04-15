package org.chaostocosmos.leap.common.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.common.constant.Constants;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.exception.LeapException;

import ch.qos.logback.classic.Level;

/**
 * Utility functionalities to be provided for other classes.
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

    /**
     * Get log level list
     * @param levels
     * @return
     */
    public static List<Level> getLogLevels(String levels) {
        return getLogLevels(levels, Constants.PROPERTY_SEPARATOR);
    }

    /**
     * Get log level list
     * @param levels
     * @param separator 
     * @return
     */
    public static List<Level> getLogLevels(String levels, String separator) {
        return Arrays.asList(levels.split(separator)).stream().map(l -> Level.toLevel(l.trim())).collect(Collectors.toList());
    }

    /**
     * Get contents type of resource
     * @param resourcePath
     * @return
     * @throws LeapException
     */
    public static String probeContentType(Path resourcePath) throws LeapException {
        try {
            return Files.probeContentType(resourcePath);
        } catch (IOException e) {
            throw new LeapException(HTTP.RES500, e);
        }
    }        

    /**
     * Get arguments from function expression
     * @param resourcePath
     * @return
     * @throws LeapException
     */
    public static List<String> extractArgument(String functionExpr) {
        // Example function expression: "functionName(arg1, arg2, (arg3))"
        // Regex pattern to match the function name and its arguments
        String regex = "^[a-zA-Z_][a-zA-Z0-9_]*\\s*\\(([^)]+)\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(functionExpr);
        if (matcher.find()) {
            String args = matcher.group(1);
            // Split the arguments by comma, taking care of nested parentheses
            return Arrays.stream(args.split(","))
                    .map(String::trim)
                    .filter(arg -> !arg.isEmpty())
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("Invalid function expression: " + functionExpr);
        }
    }

    /**
     * Check if the string is a function expression
     * @param functionExpr
     * @return
     */
    public static boolean isFunctionExpression(String functionExpr) {
        // Example function expression: "functionName(arg1, arg2, (arg3))"
        // Regex pattern to match the function name and its arguments
        String regex = "^[a-zA-Z_][a-zA-Z0-9_]*\\s*\\(([^)]+)\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(functionExpr);
        return matcher.find();
    }
}
