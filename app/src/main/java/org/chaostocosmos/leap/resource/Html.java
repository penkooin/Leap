package org.chaostocosmos.leap.resource;

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.context.Context;

/**
 * Html handing
 * 
 * @author 9ins
 */
public class Html {

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
            "Server", "Leap?/"+Context.get().server().getLeapVersion()+"("+System.getProperty("os.name")+") java/"+System.getProperty("java.version"),
            "Refresh", seconds+"; URL="+url,
            "Connection", "close",
            "Content-Type", "text/html; charset=iso-8859-1",
            "Content-Length", "0"
            );
    }
}


