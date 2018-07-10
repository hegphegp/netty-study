package com.hegp.netty.basic.example03.common.domain;

public class MessageEntity {
    private Header header;
    private Object body;

    public MessageEntity() {
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}
