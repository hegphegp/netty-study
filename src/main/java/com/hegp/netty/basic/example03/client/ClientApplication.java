package com.hegp.netty.basic.example03.client;

import com.hegp.netty.basic.example03.common.domain.MessageEntity;

public class ClientApplication {
    public static void main(String[] args) throws InterruptedException {
        SocketClient client = new SocketClient("192.168.1.169", 8877);
        client.startup();

        Thread.sleep(100);
        if(client.channelFuture.channel().isActive()){
            int version = 1;
            for(int i=0; i<100; i++) {
                String body = "Hello world from client:"+ i;
                MessageEntity msg = new MessageEntity(version, (byte) 0XBF, i, body);
                client.write(msg);
            }
        }
    }
}
