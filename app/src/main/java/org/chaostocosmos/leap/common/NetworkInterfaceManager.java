package org.chaostocosmos.leap.common;

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
public class NetworkInterfaceManager {

    /**
     * Get all network interfaces
     * @return
     * @throws SocketException
     */
    public static List<NetworkInterface> getAllNetworkInterfaces() throws SocketException {
        return NetworkInterface.networkInterfaces().collect(Collectors.toList());                
    }

    /**
     * Get InetAddress by host name
     * @param host
     * @return
     * @throws SocketException 
     */
    public static InetAddress getInetAddressByHost(String host) throws SocketException {
        return getAllNetworkAddresses().stream().filter(i -> i.getHostName().equals(host)).findAny().orElse(null);
    }

    /**
     * Whether matching with existing mac addresses
     * @param mac
     * @return
     * @throws SocketException
     */
    public static boolean isExistMacAddress(String mac) throws SocketException {
        return getAllMacAddress().stream().anyMatch(m -> m.equals(mac));
    }

    /**
     * Get MAC address of network interface
     * @param ip
     * @return
     * @throws SocketException
     * @throws UnknownHostException
     */
    public static String getMacAddressByIp(String ip) throws SocketException, UnknownHostException {
        NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getByName(ip));
        if(ni != null) {
            byte[] mac = ni.getHardwareAddress();   
            if(mac != null) {
                StringBuilder macAddress = new StringBuilder();
                for (int i = 0; i < mac.length; i++) {
                    macAddress.append(String.format("%02X", mac[i]));
                    if (i < mac.length - 1) {
                        macAddress.append("-");
                    }
                }
                return macAddress.toString();
            } else {
                throw new RuntimeException("MAC address is not found!!!");
            }
        } else {
            throw new RuntimeException("Network interface is not found!!!");
        }
    }

    /**
     * Get all MAC address
     * @return
     * @throws SocketException 
     */
    public static List<String> getAllMacAddress() throws SocketException {
        return getAllNetworkInterfaces().stream()
                                        .map(ni -> {
                                            try {
                                                return ni.getHardwareAddress();
                                            } catch (SocketException e) {
                                                throw new RuntimeException(e);
                                            }
                                        })
                                        .filter(b -> b != null)
                                        .map(mac -> {
                                            StringBuilder macAddress = new StringBuilder();
                                            for (int i = 0; i < mac.length; i++) {
                                                macAddress.append(String.format("%02X", mac[i]));
                                                if (i < mac.length - 1) {
                                                    macAddress.append("-");
                                                }
                                            }
                                            return macAddress.toString();                                            
                                        })
                                        .collect(Collectors.toList());
    }

    /**
     * Get all system InetAddresses
     * @return
     * @throws SocketException
     */
    public static List<InetAddress> getAllNetworkAddresses() throws SocketException {
        return getAllNetworkInterfaces().stream().flatMap(n -> n.inetAddresses()).collect(Collectors.toList());
    }    

    /**
     * Get whether of the host be in NetworkInterfaces
     * @param host
     * @return
     * @throws SocketException
     * @throws UnknownHostException
     */
    public static boolean isNetworkInterfaces(String host) throws SocketException, UnknownHostException {
        InetAddress inet = InetAddress.getByName(host);
        NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inet);
        if(inet.isLoopbackAddress() && networkInterface != null) {
            return true;
        } else {
            return false;
        }
    }
}
