package com.hegp.netty.basic.example03.client;

import com.hegp.netty.basic.example03.common.domain.Message;

public class ClientApplication {
    public static void main(String[] args) throws InterruptedException {
        SocketClient client = new SocketClient("192.168.1.169", 8877);
        client.startup();

        if(client.channelFuture.channel().isActive()){
            for(int i=0; i<100; i++) {
                String body = "Hello world from client:"+ i;
                Message msg = new Message((byte) 0XAF, (byte) 0XBF, i, body);
                client.write(msg);
            }
        }
    }
}
