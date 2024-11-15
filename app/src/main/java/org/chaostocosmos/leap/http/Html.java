package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.common.constant.Constants;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.TEMPLATE;
import org.chaostocosmos.leap.exception.LeapException;

/**
 * Html handing
 * 
 * @author 9ins
 */
public class Html {

    /**
     * Make error response page
     * @param host
     * @param leapException
     * @return
     * @throws IOException
     */
    public static String makeDefaultErrorHtml(Host<?> host, LeapException leapException) throws IOException {
        String stacktrace = "";
        if(host.<Boolean> getValue("logs.details")) {
            stacktrace = "<pre>" + leapException.getStackTraceMessage() + "<pre>";
        }
        Map<String, Object> paramMap = Map.of("@serverName", host.getHost(), 
                                              "@code", leapException.getHTTP().code(), 
                                              "@status", leapException.getHTTP().status(), 
                                              "@message", leapException.getMessage(), 
                                              "@stacktrace", stacktrace);
        return resolvePlaceHolder(TEMPLATE.ERROR.loadTemplatePage(host.getId()), paramMap);
    }

    /**
     * Make redirect html page
     * @param protocol
     * @param protocolVersion
     * @param seconds
     * @param url     
     * @return
     */
    public static String makeRedirectHtml(String protocol, String protocolVersion, int seconds, String url) {
        return  makeRedirect(protocol, protocol, seconds, url)
                +"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">"
                +"<html><head><title>Leap Load-Balance redirect!!!</title>"
                +"<meta http-equiv = \"refresh\" content = \""+seconds+"; url = '"+url+"'\" />"
                +"</head><body></body></html>";
    }

    /**
     * Make redirect headers
     * @param protocol
     * @param protocolVersion
     * @param seconds
     * @param url
     * @return
     */
    public static String makeRedirect(String protocol, String protocolVersion, int seconds, String url) {
        return protocol+"/"+protocolVersion+" 307 Leap Load-Balance redirect.\r\n" 
                + makeRedirectHeader(seconds, url).entrySet().stream().map(e -> e.getKey()+": "+e.getValue()).collect(Collectors.joining("\r\n")) 
                + "\r\n";
    }

    /**
     * Make 
     * @param seconds
     * @param url
     * @return
     */
    public static Map<String, String> makeRedirectHeader(int seconds, String url) {
        return Map.of(
            "Date", new Date().toString(), 
            "Server", "Leap?/"+Context.get().server().<String> getValue("server.version")+"("+System.getProperty("os.name")+") java/"+System.getProperty("java.version"),
            "Refresh", seconds+"; URL="+url,
            "Connection", "close",
            "Content-Type", "text/html; charset=iso-8859-1",
            "Content-Length", "0"
            );
    }

    /**
     * Resolve placeholders in HTML page
     * @param htmlPage
     * @param placeHolderValueMap
     * @return
     */
    public static String resolvePlaceHolder(String htmlPage, Map<String, Object> placeHolderValueMap) {
        String regex = Constants.PLACEHOLDER_REGEX;
        Pattern ptrn = Pattern.compile(regex);
        Matcher matcher = ptrn.matcher(htmlPage);
        placeHolderValueMap = placeHolderValueMap.entrySet().stream().map(e -> {
            String k = e.getKey();
            Object v = e.getValue();
            return new Object[] {k, v != null ? v.toString().replace("\n", "<br>") : v};
        }).collect(Collectors.toMap(k -> (String)k[0], v -> v[1]));
        while(matcher.find()) {
            String match = matcher.group(1).trim();
            String key = match.replace("<!--", "").replace("-->", "").trim();
            if(placeHolderValueMap.containsKey(key)) {
                //Object obj = placeHolderValueMap.get(key);
                htmlPage = htmlPage.substring(0, htmlPage.indexOf(match))
                           + placeHolderValueMap.get(key)
                           + htmlPage.substring(htmlPage.indexOf(match)+match.length());
            }
        }
        return htmlPage;
    }    
}


