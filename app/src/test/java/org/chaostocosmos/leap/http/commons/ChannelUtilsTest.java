package org.chaostocosmos.leap.http.commons;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;

import org.chaostocosmos.leap.http.common.ChannelUtils;
import org.junit.Before;
import org.junit.Test;

public class ChannelUtilsTest {

    ServerSocket server;

    @Before
    public void setup() throws Exception {
    }

    @Test
    public void testReadLines() throws IOException {
        ServerSocketChannel serverSocket = null;
        serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress(8080));

        while(true) {
            final SocketChannel client = serverSocket.accept();
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                     try {
                        System.out.println("Client accepted: " + client.toString());               
                        ByteBuffer buffer = ByteBuffer.allocate(10);     
                        Map<String, List<String>> headers = ChannelUtils.readHeaders(client, buffer);
                        System.out.println(headers);
                        if(headers != null && headers.size() > 1) {
                            //String contentType = headers.get("Content-Type").get(0);
                            String boundary = headers.get("Content-Type").get(1);
                            boundary = boundary.substring(boundary.indexOf("=")+1);
                            long length = Long.parseLong(headers.get("Content-Length").get(0));
                            System.out.println("["+boundary+"]");
                            Map<String, byte[]> data = ChannelUtils.getMultiPartContents("localhost", client, buffer, boundary, "utf-8", length, 64);
                            for(Map.Entry<String, byte[]> entry : data.entrySet()) {
                                FileOutputStream out = new FileOutputStream(new File("D:/0.github/Leap/"+entry.getKey()));
                                out.write(entry.getValue());
                                out.close();    
                                System.out.println("&&&&&&&&&&& file saved: "+entry.getKey());
                            }
                            client.close();        
                        }
                     } catch(Exception e) {
                         e.printStackTrace();
                     }
                }
             }).start();
        }
    }

    public void testReadLines1() throws IOException {
        try(ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(8080));
            while(true) {
                Socket client = serverSocket.accept();   
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            System.out.println("start handling......");
                            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
                            String line;
                            while((line = br.readLine()) != null) {
                                System.out.println(line);
                            }
                            br.close();
                            client.close();
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }    
        }
   }

    public static void main(String[] args) throws IOException {
        //new ChannelUtilsTest().testReadLines1();
        //byte[] bytes = Files.readAllBytes(Paths.get("D:\\0.github\\Leap\\home\\webapp\\WEB-INF\\static\\video\\video1.mp4"));
        //System.out.println(bytes.length);
        File file = new File("D:\\1.iq-designer\\designer\\release\\IQD_BD-V7.3.1.20220112.zip");
        byte[] buffer = new byte[1024*1000*5];
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int len;
        while((len=fis.read(buffer)) > 0) {
            bos.write(buffer, 0, len);
        }
        fis.close();
    }

}
