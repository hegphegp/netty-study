package com.hegp.netty.basic.example03.common.domain;

import java.nio.charset.Charset;

public class MessageEntity {

    private final Charset charset = Charset.forName("utf-8");
    private byte type;      // 消息类型  0xAF表示心跳包, 0xBF表示超时包, 0xCF业务信息包
    private int requestId; // 请求id
    private byte isZip;     // 是否压缩
    private byte priority;  // 消息优先级
    private int length;
    private String body;

    public MessageEntity() { }

    public MessageEntity(byte type, int requestId, byte[] data) {
        this.type = type;
        this.requestId = requestId;
        this.length = data.length;
        this.body = new String(data, charset);
    }

    public MessageEntity(byte type, int requestId, String body) {
        this.type = type;
        this.requestId = requestId;
        this.length = body.getBytes(charset).length;
        this.body = body;
    }

    public Charset getCharset() {
        return charset;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public byte getIsZip() {
        return isZip;
    }

    public void setIsZip(byte isZip) {
        this.isZip = isZip;
    }

    public byte getPriority() {
        return priority;
    }

    public void setPriority(byte priority) {
        this.priority = priority;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "MessageEntity{" +
                "charset=" + charset +
                ", type=" + type +
                ", requestId=" + requestId +
                ", length=" + length +
                ", body='" + body + '\'' +
                '}';
    }
}