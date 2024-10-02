package org.chaostocosmos.leap.common.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.transaction.NotSupportedException;

import org.chaostocosmos.leap.common.constant.Constants;

public class RegexTest {

   public static String resolvePlaceHolderTest(String htmlPage, Map<String, Object> placeHolderValueMap) {
        String regex = Constants.PLACEHOLDER_REGEX;
        Pattern ptrn = Pattern.compile(regex);
        Matcher matcher = ptrn.matcher(htmlPage);
        while(matcher.find()) {
            if(placeHolderValueMap.containsKey(matcher.group(1))) {
                System.out.println(placeHolderValueMap.get(matcher.group(1)));
                htmlPage = htmlPage.replaceAll("<!--\\s*@\\s*" + matcher.group(1) + "\\s*-->", placeHolderValueMap.get(matcher.group(1)).toString());
            }
        }
        System.out.println(htmlPage);
        return htmlPage;
    }


    public static void main(String[] args) throws NotSupportedException, IOException, URISyntaxException {
        Map<String, Object> map = Map.of("@serverName", "localhost", "@title", "hello");
        System.out.println(ClassLoader.getSystemClassLoader().getResource("webapp/static/").toURI());
        java.nio.file.Path path = java.nio.file.Paths.get(ClassLoader.getSystemClassLoader().getResource("webapp/templates/error.html").toURI());

        String str = java.nio.file.Files.readString(path);
        str = RegexTest.resolvePlaceHolderTest(str, map);
        System.out.println(str);
    }    
}
