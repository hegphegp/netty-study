package com.hegp.netty.basic.example03.client;

import com.hegp.netty.basic.example03.common.domain.MessageEntity;

public class ClientApplication {
    public static void main(String[] args) throws InterruptedException {
        new SocketClient("192.168.1.169", 8877).startup();
        for (int i = 0; i < 10000; i++) {
            MessageEntity entity = new MessageEntity();
        }
    }
}
