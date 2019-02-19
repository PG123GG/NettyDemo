package com.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class NettyClient {

    /**
     * group对应了我们IOClient.java中 main 函数起的线程
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        //引导类
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();

        bootstrap.group(group)//1、指定线程模型
                .channel(NioSocketChannel.class)//2、指定IO类型为NIO
                .handler(new ChannelInitializer<Channel>() {//3、指定NIO的处理逻辑
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new StringEncoder());
                    }
                });

        //4、建立连接
        Channel channel = bootstrap.connect("127.0.0.1", 8000).channel();

        while (true) {
            channel.writeAndFlush(new Date() + ": hello world!");
            Thread.sleep(2000);
        }
    }


    /**
     * 失败重连:把建立连接的逻辑先抽取出来，然后在重连失败的时候，递归调用自身
     * @param bootstrap
     * @param host
     * @param port
     */
    private static void connect(Bootstrap bootstrap, String host, int port) {
        bootstrap.connect(host, port).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("连接成功!");
            } else {
                System.err.println("连接失败，开始重连");
                connect(bootstrap, host, port);
            }
        });
    }


    /**
     * 但是，通常情况下，连接建立失败不会立即重新连接，而是会通过一个指数退避的方式，
     * 比如每隔 1 秒、2 秒、4 秒、8 秒，以 2 的幂次来建立连接，然后到达一定次数之后就放弃连接，
     * 接下来我们就来实现一下这段逻辑，我们默认重试 5 次
     */
    static int MAX_RETRY = 5;
    public static void connect(Bootstrap bootstrap, String host, int port, int retry) {
        bootstrap.connect(host, port).addListener(future -> {
            if (future.isSuccess()) {
                //1、连接成功逻辑
                System.out.println("连接成功!");
            } else if (retry == 0) {
                //2、重连次数用完逻辑
                System.err.println("重试次数已用完，放弃连接！");
            } else {
                //3、重新连接逻辑
                // 第几次重连
                int order = (MAX_RETRY - retry) + 1;
                // 本次重连的间隔
                int delay = 1 << order;
                System.err.println(new Date() + ": 连接失败，第" + order + "次重连……");
                //4.1：bootstrap.config() 这个方法返回的是 BootstrapConfig，他是对 Bootstrap 配置参数的抽象
                //4.2：bootstrap.config().group() 返回的就是我们在一开始的时候配置的线程模型 workerGroup
                //4.3：调 workerGroup 的 schedule 方法即可实现定时任务逻辑。
                bootstrap.config().group().schedule(() -> connect(bootstrap, host, port, retry - 1), delay, TimeUnit
                        .SECONDS);
            }
        });
    }
}


/*
客户端启动其他方法
1、attr() 方法
bootstrap.attr(AttributeKey.newInstance("clientName"), "nettyClient")
attr() 方法可以给客户端 Channel，也就是NioSocketChannel绑定自定义属性，然后我们可以通过channel.attr()取出这个属性

--------------------------------------------------------------------------------------------
2、option() 方法
Bootstrap
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
        .option(ChannelOption.SO_KEEPALIVE, true)
        .option(ChannelOption.TCP_NODELAY, true)

option() 方法可以给连接设置一些 TCP 底层相关的属性，比如上面，我们设置了三种 TCP 属性，其中

ChannelOption.CONNECT_TIMEOUT_MILLIS 表示连接的超时时间，超过这个时间还是建立不上的话则代表连接失败
ChannelOption.SO_KEEPALIVE 表示是否开启 TCP 底层心跳机制，true 为开启
ChannelOption.TCP_NODELAY 表示是否开始 Nagle 算法，true 表示关闭，false 表示开启，通俗地说，如果要求高实时性，有数据发送时就马上发送，就设置为 true 关闭，如果需要减少发送次数减少网络交互，就设置为 false 开启

--------------------------------------------------------------------------------------------
3、
*/
