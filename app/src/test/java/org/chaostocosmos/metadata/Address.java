package org.chaostocosmos.metadata;

public class Address {
    
    @MetaField(expr = "hosts[0].resources.streaming-buffer-size")
    int port;
}
