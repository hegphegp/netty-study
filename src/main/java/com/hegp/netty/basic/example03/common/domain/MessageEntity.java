package com.hegp.netty.basic.example03.common.domain;

import com.hegp.netty.basic.example03.common.constant.Constants;

public class MessageEntity {

    // version+type+requestId+isZip=int+byte+int+byte+int=4+1+4+1+4=14
    public static final int HEADER_SIZE = 14;

    private int version;    // 版本
    private byte type;      // 消息类型  0xAF表示心跳包, 0xBF表示超时包, 0xCF业务信息包, 订下下的规矩是: 心跳包的内容长度为0
    private int requestId;  // 请求id
    private byte isZip;     // 是否压缩, 0表示不压缩, 1表示压缩
    private int length;
    private String body;

    public MessageEntity() { }

    public MessageEntity(int version, byte type, int requestId, String body) {
        this.version = version;
        this.type = type;
        this.requestId = requestId;
        this.isZip = 0;
        int length = body.getBytes(Constants.CHARSET).length;
        if(Constants.MESSAGE_NOT_ZIP_MAX_LENGTH < length) {
            // 编码逻辑。。。。。。
            // body = 压缩后的body
            // isZip = 1
            // length = 编码后的长度
        }
//        this.length = length;
        this.body = body;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
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
                "version=" + version +
                ", type=" + type +
                ", requestId=" + requestId +
                ", isZip=" + isZip +
                ", length=" + length +
                ", body='" + body + '\'' +
                '}';
    }
}