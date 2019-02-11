package com.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class NettyServer {

    /**
     * boss 对应 IOServer.java 中的接受新连接线程，主要负责创建新连接
     * worker 对应 IOServer.java 中的负责读取数据的线程，主要用于读取数据以及业务逻辑处理
     * @param args
     */
    public static void main(String[] args) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        NioEventLoopGroup boss = new NioEventLoopGroup();//bossGroup表示监听端口，accept 新连接的线程组
        NioEventLoopGroup worker = new NioEventLoopGroup();//workerGroup表示处理每一条连接的数据读写的线程组
        //1、bossGroup接收完连接，扔给workerGroup去处理

        //2、引导类 ServerBootstrap,这个类将引导我们进行服务端的启动工作
        serverBootstrap
                .group(boss, worker)//3、通过.group(bossGroup, workerGroup)给引导类配置两大线程组
                .channel(NioServerSocketChannel.class)//4、指定我们服务端的 IO 模型为NIO,也可以通过该方法指定其他的IO类型如BIO
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel ch) {//5、给这个引导类创建一个ChannelInitializer，这里主要就是定义后续每条连接的数据读写，业务处理逻辑
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                                System.out.println(msg);
                            }
                        });
                    }
                })
                .bind(8000);//这个bind绑定端口也可以动态实现，因为bind()返回的是一个ChannelFuture对象,可以通过addListener()判断是否连接成功


        //ServerBootstrap除了上述属性还可以通过其他方法设置其他属性
    }

    //实现动态绑定端口的逻辑
    private static void bind(final ServerBootstrap serverBootstrap, final int port) {
        serverBootstrap.bind(port).addListener(new GenericFutureListener<Future<? super Void>>() {
            public void operationComplete(Future<? super Void> future) {
                if (future.isSuccess()) {
                    System.out.println("端口[" + port + "]绑定成功!");
                } else {
                    System.err.println("端口[" + port + "]绑定失败!");
                    bind(serverBootstrap, port + 1);
                }
            }
        });
    }
}
