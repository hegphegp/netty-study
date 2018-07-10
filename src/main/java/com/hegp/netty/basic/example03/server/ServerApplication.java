package com.hegp.netty.basic.example03.server;

public class ServerApplication {
    public static void main(String[] args) throws InterruptedException {
        new SocketServer(8877).startup();
    }
}
