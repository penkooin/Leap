package org.chaostocosmos.leap.http.services.session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.enums.RES_CODE;
import org.chaostocosmos.leap.http.services.entity.Message;

/**
 * WebSocketSession
 * 
 * @author 9ins
 */
public class HttpSocketSession extends HttpSession {

    final Socket socket;
    final InputStream inStream;
    final OutputStream outStream;

    public HttpSocketSession(String sessionId, long creationTime, long lastAccessedTime, int maxInteractiveInteralSecond, Request request, Socket socket) throws IOException {
        super(sessionId, creationTime, lastAccessedTime, maxInteractiveInteralSecond, request);
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
