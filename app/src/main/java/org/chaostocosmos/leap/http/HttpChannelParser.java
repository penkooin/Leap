package org.chaostocosmos.leap.http;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.http.common.ChannelUtils;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;

/**
 * HttpChannelParser
 * 
 * @author 9ins
 */
public class HttpChannelParser {
    /**
     * Http request parser
     */
    private static ChannelRequestParser requestParser;

    /**
     * Http response parser
     */
    private static ChannelResponseParser responseParser; 

    /**
     * Get request parser object
     * @return
     * @throws IOException
     */
    public static ChannelRequestParser buildRequestParser() {
        if(requestParser == null) {
            requestParser = new ChannelRequestParser();
        }
        return requestParser;
    }

    /**
     * Get response parser object
     * @return
     */
    public static ChannelResponseParser buildResponseParser() {
        if(responseParser == null) {
            responseParser = new ChannelResponseParser();
        }
        return responseParser;
    }

    /**
     * Check request method
     * @param requestType
     * @return
     */
    public static boolean isValidType(String requestType) {
        if(REQUEST_TYPE.GET.name().equals(requestType) || REQUEST_TYPE.POST.name().equals(requestType) || REQUEST_TYPE.PUT.name().equals(requestType) || REQUEST_TYPE.DELETE.name().equals(requestType)) {
            return true;
        }
        return false; 
    }

    /**
     * Request parser inner class
     * @author 9ins
     */
    public static class ChannelRequestParser {
        /**
         * Parse request
         * @throws IOException
         */
        public Request parseRequest(Socket socket) throws IOException {
            SocketChannel channel = socket.getChannel();
            Map<String, List<String>> lines = ChannelUtils.readHeaders(channel, ByteBuffer.allocate(1024));             
            return null;
        }
    }

    /**
     * Response parser inner class
     */
    public static class ChannelResponseParser {
        /**
         * parse response
         * @return
         * @throws HTTPException
         */
        public Response buildResponse(final Request request, 
                                                    final int statusCode, 
                                                    final Object body, 
                                                    final Map<String, List<String>> headers) {
            return new Response(request, statusCode, body, headers);
        }
    }       
}
