package org.chaostocosmos.leap.http.commons;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;

import org.chaostocosmos.leap.common.NetworkInterfaceManager;
import org.junit.Test;    
    
public class NetworkInterfacesTest {

    @Test
    public void test() throws SocketException {
        List<InetAddress> addresses = NetworkInterfaceManager.getAllNetworkAddresses();        
        addresses.stream().forEach(a -> System.out.println(a.getHostAddress()+" ---- "+a.getHostName()));
        System.out.println("--------------------------------------------------------------------------------------");
        addresses.stream().forEach(System.out::println);
    }

    @Test
    public void test1() throws Exception {
        InetAddress inet = InetAddress.getByName("www.leap1.org");
        System.out.println(inet);
    }
}
    