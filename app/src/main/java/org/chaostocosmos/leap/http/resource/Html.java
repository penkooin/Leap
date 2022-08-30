package org.chaostocosmos.leap.http.resource;

import java.util.Date;

import org.chaostocosmos.leap.http.context.Context;

/**
 * Html handing
 * 
 * @author 9ins
 */
public class Html {

    /**
     * Make redirect page
     * @param seconds
     * @param url
     * @return
     */
    public static String makeRedirectHtml(String protocol, int seconds, String url) {
        return protocol+" 200 Leap Load-Balance redirect.\r\n"
                +"Date: "+new Date()+"\r\n"
                +"Server: Leap?/"+Context.getServer().getLeapVersion()+"("+System.getProperty("os.name")+") java/"+System.getProperty("java.version")+"\r\n"
                +"Content-Type: text/html; charset=iso-8859-1\r\n"
                +"\r\n"
                +"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">"
                +"<html><head><title>Leap Load-Balance redirect!!!</title>"
                +"<meta http-equiv = \"refresh\" content = \""+seconds+"; url = '"+url+"'\" />"
                +"</head><body></body></html>";
    }

    /**
     * Make load-balance redirect
     * @param protocol
     * @param url
     * @return
     */
    public static String makeRedirect(String protocol, int seconds, String url) {
        return protocol+" 307 Leap Load-Balance redirect.\r\n"
               +"Date: "+new Date()+"\r\n"
               +"Server: Leap?/"+Context.getServer().getLeapVersion()+"("+System.getProperty("os.name")+") java/"+System.getProperty("java.version")+"\r\n"
               +"Refresh: "+seconds+"; URL="+url+"\r\n"
               +"Connection: close"+"\r\n"
               +"Content-Type: text/html; charset=iso-8859-1\r\n\r\n";
    }
}


