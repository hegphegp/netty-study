package com.hegp.netty.basic.example03.common.constant;

import java.nio.charset.Charset;

public class Constants {
    public final static int MAX_MESSAGE_LENGTH = 1<<20;
    public final static Charset CHARSET = Charset.forName("utf-8");
    public final static int MESSAGE_NOT_ZIP_MAX_LENGTH = 1024;
}
