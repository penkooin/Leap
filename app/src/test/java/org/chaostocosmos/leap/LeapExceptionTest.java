package org.chaostocosmos.leap;

import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.exception.LeapException;
import org.junit.jupiter.api.Test;

public class LeapExceptionTest {
    @Test
    public void testCode() throws InterruptedException {
        int code = new LeapException(HTTP.RES100).code();
        System.out.println(code);
        Context.get().stopMetaWatcher();
    }

    @Test
    public void testGetResCode() throws InterruptedException {
        HTTP http = new LeapException(HTTP.LEAP900).getResCode();        
        System.out.println(http.name());
        Context.get().stopMetaWatcher();
    }

    @Test
    public void testGetStackTraceMessage() throws InterruptedException {
        LeapException e = new LeapException(HTTP.LEAP900, "aaa", "bbb");
        String msg = e.getStackTraceMessage();
        System.out.println(msg);    
        Context.get().stopMetaWatcher();
    }

    @Test
    public void testGetStatus() throws InterruptedException {
        String status = new LeapException(HTTP.RES428).getStatus();
        System.out.println(status);
        Context.get().stopMetaWatcher();
    }

    public static void main(String[] args) throws InterruptedException {
        new LeapExceptionTest().testGetStackTraceMessage();;
    }
}
