# netty粘包和丢包的解决方案

* 大数据包的流量攻击，直接关掉客户端的连接Socket
* 将消息分为消息头和消息体，消息头中包含消息长度的字段，通常设计思路为消息头的第一个字段使用int32来表示消息的总长。