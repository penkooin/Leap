package org.chaostocosmos.leap.http.session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.enums.RES_CODE;
import org.chaostocosmos.leap.http.service.entity.Message;

/**
 * WebSocketSession
 * 
 * @author 9ins
 */
public class HttpSocketSession extends HttpSession {
    /**
     * Socket object
     */
    final Socket socket;

    /**
     * Client InputStream
     */
    final InputStream inStream;

    /**
     * Client OutputStream
     */
    final OutputStream outStream;

    /**
     * HttpSocketSession
     * @param sessionManager
     * @param sessionId
     * @param creationTime
     * @param lastAccessedTime
     * @param maxInteractiveInteralSecond
     * @param request
     * @param socket
     * @throws IOException
     */
    public HttpSocketSession(SessionManager sessionManager, String sessionId, long creationTime, long lastAccessedTime, int maxInteractiveInteralSecond, Request request, Socket socket) throws IOException {
        super(sessionManager, sessionId, creationTime, lastAccessedTime, maxInteractiveInteralSecond, request);
        this.socket = socket;
        this.inStream = socket.getInputStream();
        this.outStream = socket.getOutputStream();
    }

    @Override
    public void close() {
        Message msg = new Message();
        msg.setContent(Context.getMessages().getHttpMsg(RES_CODE.RES500.code()));
        try {
            this.inStream.close();
            this.outStream.write(msg.getContent().getBytes());
            this.outStream.close();
            this.socket.close();    
        } catch(IOException e) {
            super.getHost().getLogger().error("Error in session closing process: "+super.sessionId, e);
        }
    }
}
