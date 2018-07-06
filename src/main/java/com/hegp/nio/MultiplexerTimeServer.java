package com.hegp.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
/**
 步骤一：打开ServerSocketChannel，用于监听客户端的连接，它是所有客户端连接的父管道。代码如下：
 步骤二：绑定监听的网关IP和端口，设置连接为非阻塞模式(ServerSocketChannel既有阻塞模式也有非阻塞模式，应该显式声明比较好)。代码如下：
 步骤三：创建Reactor线程，创建多路复用器并启动线程。代码如下：
 步骤四：将ServerSocketChannel注册到Reactor线程的多路复用Selector上，监听ACCEPT事件。代码如下：
 步骤五：多路复用器在线程run方法的无限循环体内轮询准备就绪的Key。代码如下：
 步骤六：多路复用器监听到有新的客户端接入，处理新的接入请求，完成TCP三次握手，建立物理链路。代码如下：
 步骤七：设置客户端链路为非阻塞模式。代码如下：
 步骤八：将新接入的客户端连接注册到Reactor线程的多路复用器上，监听读操作，用来读取客户端发送的网络消息。代码如下：
 步骤九：异步读取客户端请求消息到缓冲区。代码如下：
 步骤十：对ByteBuffer进行编解码，如果有半包消息指针reset，继续读取很想的报文，将解码成功的消息封装成Task，投递到业务线程池中，进行业务逻辑编排。代码如下：
 步骤十一：将POJO对象encode成ByteBuffer，调用SocketChannel的异步write接口，将信息异步发送到客户端
 注意：如果发送区TCP缓冲区满，会导致写半包，此时，需要注册监听写操作位，循环写，直到整包信息写入TCP缓冲区。
 */
/**
 完全搞不懂Java是怎么定义NIO的API，是怎么使用API的，完全看不懂

 SelectionKey四种取值分别是什么意思？
 为什么服务器端的ServerSocketChannel.register()设置成SelectionKey.OP_ACCEPT，客户端SocketChannel.register()设置成SelectionKey.OP_CONNECT
 为什么服务器端遍历selector.selectedKeys().iterator()的时候，都要判断key.isValid()，然后判断key.isAcceptable()，如果为true，为什么后面要通过SelectionKey一步步反推过来获取
 serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

 selector.selectedKeys();获取的keys是所有注册在selector上面的Key吗？还是获取当前有读写请求的Key？
 亲测答案：获取就绪状态的Key，如果有几十个客户端Socket连接服务器，很多服务器不发信息，只是单纯地连着，System.out.println("===>>>"+selector.selectedKeys().size())打印的不是所有Socket的数目，是打印有请求I/O的Socket的数据量;

 掌握SocketChannel读数据和写数据的方法
*/
public class MultiplexerTimeServer implements Runnable {

    public static void main(String[] args) {
        MultiplexerTimeServer timeServer = new MultiplexerTimeServer("192.168.1.169", 8080);
        new Thread(timeServer, "NIO-MultiplexerTimeServer111-001").start();
    }

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private volatile boolean stop;

    public MultiplexerTimeServer(String host, int port) {
        try {
            serverSocketChannel = ServerSocketChannel.open(); //打开ServerSocketChannel，用于监听客户端的连接，它是所有客户端连接的父管道
            //通过ServerSocketChannel.open()获取的对象是sun.nio.ch.ServerSocketChannelImpl类型的
            serverSocketChannel.configureBlocking(false); //设置连接为非阻塞模式
            //因为电脑有多网卡，如果只指定端口而不指定IP地址去创建InetSocketAddress对象，底层会调用new InetSocketAddress(InetAddress.anyLocalAddress(), port)构造方法创建对象
            serverSocketChannel.socket().bind(new InetSocketAddress(host, port), 1024);
            selector = Selector.open(); //多路复用器在线程run方法的无限循环体内轮询准备就绪的Key
            //通过Selector.open()获取的对象是sun.nio.ch.EPollSelectorImpl类型的
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT); //将ServerSocketChannel注册到Reactor线程的多路复用Selector上，监听ACCEPT事件
            System.out.println("The time server is start in port : " + port);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void stop() {
        this.stop = true;
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                selector.select(1000);//多路复用器有睡眠方法
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    //key是sun.nio.ch.SelectionKeyImpl类型的
                    SelectionKey key = iterator.next();
                    iterator.remove();
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
            } catch (Throwable t) {
                t.printStackTrace();
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
            // 处理新接入的请求消息
            // 处理新接入的容户端请求消息，根据SelectionKey的操作位可获知网络事件的类型，通过ServerSocketChannel的accept接收容户端的连接请求并创建SocketChannel实例，完成上述操作后，相当于完成了TCP的三次握手，TCP物理链路正式建立。注意，设置新创建的SocketChannel为异步非阻塞，同时也可以对其TCP参数进行设置，例如TCP接收和发送缓冲区的大小等
            if (key.isAcceptable()) {
                // 接受新的连接
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                //通过sun.nio.ch.SelectionKeyImpl.channel()获取的对象是sun.nio.ch.ServerSocketChannelImpl类型的
                //通过ServerSocketChannel的accept接收容户端的连接请求并创建SocketChannel实例，完成上述操作后，相当于完成了TCP的三次握手，TCP物理链路正式建立。
                SocketChannel sc = ssc.accept(); //多路复用器监听到有新的客户端接入，处理新的接入请求，完成TCP三次握手，建立物理链路。
                //通过sun.nio.ch.ServerSocketChannel.accept()获取的对象是sun.nio.ch.SocketChannelImpl类型的
                // 设置客户端链路为非阻塞模式。
                // 设置新创建的SocketChannel为异步非阻塞，同时也可以对其TCP参数进行设置，例如TCP接收和发送缓冲区的大小等
                sc.configureBlocking(false);
                sc.socket().setReuseAddress(true);
                // 将新接入的客户端连接注册到Reactor线程的多路复用器上，监听读操作，用来读取客户端发送的网络消息。
                sc.register(selector, SelectionKey.OP_READ);
            }
            //这里用else if(key.isReadable())可以吗？合适吗？
            if (key.isReadable()) {
                // key.channel()可以转为sun.nio.ch.SelectionKeyImpl类型，也可以转为sun.nio.ch.SocketChannelImpl，两者有公共的父类和接口
                SocketChannel sc = (SocketChannel) key.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = sc.read(readBuffer);
                /**
                  SocketChannel设置为异步非阻塞模式后，它的read方法是非阻塞的。使用返回值进行判断，看读取到的字节数，返回值有以下三种可能的结果。
                  1) 返回值大于0: 读到了字节，对字节进行编解码；
                  2) 返回值等于0: 没有读取到字节，属于正常情况，忽略
                  3) 返回值为-1，链路已经关闭，需要关闭SocketChannel，释放资源
                 */
                if (readBytes > 0) {
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String body = new String(bytes, "UTF-8");
                    System.out.println("The time server receive order : " + body);
                    String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new java.util.Date(System.currentTimeMillis()).toString() : "BAD ORDER";
                    doWrite(sc, currentTime);
                } else if (readBytes < 0) {
                    key.cancel(); // 对端链路关闭
                    sc.close();
                }
            }
        }
    }

    private void doWrite(SocketChannel channel, String response) throws IOException {
        if (response != null && response.trim().length() > 0) {
            byte[] bytes = response.getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            writeBuffer.put(bytes);
            writeBuffer.flip();
            channel.write(writeBuffer);
        }
    }
}