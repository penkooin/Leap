package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.net.Socket;

/**
 * HttpResponseTransfer
 * 
 * @author 9ins
 */
public class HttpTransferBuilder {
    /**
     * Build HttpTransfer object
     * @param hostId
     * @param client
     * @throws IOException
     * @throws Exception
     */
    public static HttpTransfer buildHttpTransfer(String hostId, Socket client) throws IOException {
        return new HttpTransfer(hostId, client);
    }     
}