package com.protocol;


/**
 * 通信协议的设计
 *
 * 协议的格式：
 * [魔数|版本号|序列化算法|指令|数据长度|数据]
 * 4字节 1字节  1字节    1字节  4字节  N字节
 *
 * 协议格式说明：
 * 1、首先，第一个字段是魔数，通常情况下为固定的几个字节，服务端首先取出前面四个字节进行比对，能够在第一时间识别出这个数据包并非是遵循自定义协议的，也就是无效数据包，为了安全考虑可以直接关闭连接以节省资源。
 * 2、接下来一个字节为版本号，通常情况下是预留字段，用于协议升级的时候用到，有点类似 TCP 协议中的一个字段标识是 IPV4 协议还是 IPV6 协议。
 * 3、第三部分，序列化算法表示如何把 Java 对象转换二进制数据以及二进制数据如何转换回 Java 对象，比如 Java 自带的序列化，json，hessian 等序列化方式。
 * 4、第四部分的字段表示指令，关于指令相关的介绍，我们在前面已经讨论过，服务端或者客户端每收到一种指令都会有相应的处理逻辑。
 * 5、接下来的字段为数据部分的长度，占四个字节。
 * 6、最后一个部分为数据内容，每一种指令对应的数据是不一样的，比如登录的时候需要用户名密码，收消息的时候需要用户标识和具体消息内容等等。
 */

/**
 * 抽象的协议类
 */
public abstract class Packet {

    /**
     * 协议版本
     */
    private Byte version = 1;

    /**
     * 指令
     * @return
     */
    public abstract Byte getCommand();


    public abstract Byte getVersion();

}
