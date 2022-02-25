package org.chaostocosmos.leap.http.commons;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * NetworkInterfaces
 * 
 * @author 9ins
 */
public class NetworkInterfaces {

    private static NetworkInterfaces networkInterfaces;

    private static List<NetworkInterface> networkInterfacesList;
    
    private static List<InetAddress> inetAddresses;

    private NetworkInterfaces() throws SocketException {
        networkInterfacesList = NetworkInterface.networkInterfaces().collect(Collectors.toList());
        inetAddresses = getAllNetworkAddresses();
        //inetAddresses.stream().forEach(i -> System.out.println(i.getHostName()));
    }

    public static NetworkInterfaces get() throws SocketException {
        if(networkInterfaces == null) {
            networkInterfaces = new NetworkInterfaces();
        }
        return networkInterfaces;
    }

    /**
     * Get all system InetAddresses
     * @return
     * @throws SocketException
     */
    public List<InetAddress> getAllNetworkAddresses() throws SocketException {
        return networkInterfacesList.stream().flatMap(n -> n.inetAddresses()).collect(Collectors.toList());
    }    

    /**
     * Get whether of the host be in NetworkInterfaces
     * @param host
     * @return
     * @throws SocketException
     * @throws UnknownHostException
     */
    public boolean isNetworkInterfaces(String host) throws SocketException, UnknownHostException {
        InetAddress inet = InetAddress.getByName(host);
        NetworkInterface.getByInetAddress(inet).inetAddresses().forEach(i -> System.out.println(i.getHostName().toString()));
        return inet.isLoopbackAddress() || inetAddresses.stream().anyMatch(i -> i.getHostName().equals(host));
    }
}
