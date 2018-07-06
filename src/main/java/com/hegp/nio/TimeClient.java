package com.hegp.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class TimeClient implements Runnable {

    public static void main(String[] args) {
        new Thread(new TimeClient("192.168.1.169", 8080), "TimeClient-001").start();
    }

    private Selector selector;
    private SocketChannel socketChannel;
    private volatile boolean stop;

    public TimeClient(String host, int port) {
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            // 如果直接连接成功，则注册到多路复用器上，发送请求消息，读应答
            // SocketChannel的connect()操作进行判断。如果连接是成功，则将SocketChannel注册到多路复用器Selector上，注册SelectionKey.OP_READ；
            // 如果没有注册成功，则说明服务端没有返回TCP握手应答信息，但不代表连接失败。我们需要将SocketChannel注册到多路复用器Selector上，注册SelectionKey.OP_CONNECT，
            // 当服务端返回TCPsyn-ack消息后，Selector就能够轮询到这个SocketChannel处于连接就绪状态
            // 个人猜测：因为NIO是多路复用器Selector轮询channel的，不是一有连接过来服务器就马上启动一个线程处理的，所以客户端请求连接时，客户端不能马上得到服务应答(生产环境肯定会出现服务器不能及时响应的情况，该实例的服务器有睡眠秒钟的设置selector.sleep(1000))，所以客户端先注册socketChannel.register(selector, SelectionKey.OP_CONNECT);，不阻塞等待服务器的返回
            if (socketChannel.connect(new InetSocketAddress(host, port))) {
                socketChannel.register(selector, SelectionKey.OP_READ);
                doWrite(socketChannel);
            } else {
                socketChannel.register(selector, SelectionKey.OP_CONNECT);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                selector.select(1000);
                //如果客户端想随时随地通过socketChannel发信息怎么办？
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectedKeys.iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();
                    try {
                        handleInput(key);
                    } catch (Exception e) {
                        if (key != null) {
                            key.cancel();
                            if (key.channel() != null)
                                key.channel().close();
                        }
                    }
                }
                doWrite(socketChannel);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        // 多路复用器关闭后，所有注册在上面的Channel和Pipe等资源都会被自动去注册并关闭，所以不需要重复释放资源
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            // 判断是否连接成功
            // 通过SelectionKey进行判断，看它处于什么状态。
            // 如果是处于连接状态，说明服务端已经返回ACK应答消息。这时我们需要对连接结果进行判断，调用SocketChannel的finishConnect()方法。如果返回值为true，说明客户端连接成功；如果返回值为false或者直接抛出IOException，说明连接失败。
            // 在本例过程中，返回值为true，说明连接成功。将SocketChannel注册到多路复用器上，注册SelectionKey.OP_READ操作位，监听网络读操作，然后发送请求信息给服务器
            SocketChannel sc = (SocketChannel) key.channel();
            // 通过 if(sc==this.socketChannel) 发现 sc与this.socketChannel的地址是一样的
            if (key.isConnectable()) {
                if (sc.finishConnect()) {
                    sc.register(selector, SelectionKey.OP_READ);
                    doWrite(sc);
                } else
                    System.exit(1);// 连接失败，进程退出
            }
            if (key.isReadable()) {
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = sc.read(readBuffer);
                if (readBytes > 0) {
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String body = new String(bytes, "UTF-8");
                    System.out.println("Now is : " + body);
//                    this.stop = true;
                } else if (readBytes < 0) {
                    //测试多次发现，一直没有触发if (readBytes < 0)的判断
                    System.out.println("0000000000000000000000");
                    key.cancel(); // 对端链路关闭
                    sc.close();
                }
            }
        }
    }

    private void doWrite(SocketChannel sc) throws IOException {
        byte[] req = "QUERY TIME ORDER".getBytes();
        ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
        writeBuffer.put(req);
        writeBuffer.flip();
        sc.write(writeBuffer);
        if (!writeBuffer.hasRemaining())
            System.out.println("Send order 2 server succeed.");
    }
}