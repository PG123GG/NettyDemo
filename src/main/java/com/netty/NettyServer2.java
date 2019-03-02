package com.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * 服务端接受信息和回复信息
 */
public class NettyServer2 {

    public static void main(String[] args) {

        NioEventLoopGroup bootGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();

        serverBootstrap.group(bootGroup,workerGroup)
                       .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 1024)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childOption(ChannelOption.TCP_NODELAY, true)
                        //服务端相关的数据处理逻辑是通过 ServerBootstrap 的 childHandler() 方法指定
                        .childHandler(new ChannelInitializer<SocketChannel>() {

                            @Override
                            protected void initChannel(SocketChannel socketChannel) {
                                //在 initChannel() 方法里面给服务端添加一个逻辑处理器，这个处理器的作用就是负责读取客户端来的数据
                                socketChannel.pipeline().addLast(new FirstServerHandler());
                            }
                        });

        bind(serverBootstrap,8000);
    }

    private static void bind(final ServerBootstrap serverBootstrap, final int port) {
        serverBootstrap.bind(port).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("端口[" + port + "]绑定成功!");
            } else {
                System.err.println("端口[" + port + "]绑定失败!");
            }
        });
    }

}


class FirstServerHandler extends ChannelInboundHandlerAdapter{

    //继承自 ChannelInboundHandlerAdapter，
    // 与客户端不同的是，这里覆盖的方法是 channelRead()，这个方法在接收到客户端发来的数据之后被回调。
    //msg 参数指的就是 Netty 里面数据读写的载体
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //注意这里使用的市netty的ByteBuf而不是NIO的ByteBuffer
        ByteBuf byteBuf = (ByteBuf)msg;

//        byteBuf.toString();
        System.out.println(new Date() + ": 服务端读到数据 -> " + byteBuf.toString(Charset.forName("utf-8")));

        //服务端返回给客户端的数据
        ByteBuf serverByteBuf = getServerByteBuf(ctx);
        ctx.channel().writeAndFlush(serverByteBuf);
    }


    private ByteBuf getServerByteBuf(ChannelHandlerContext ctx){
        ByteBuf buffer = ctx.alloc().buffer();

        buffer.writeBytes("你好，我是服务端的大哥!".getBytes(Charset.forName("utf-8")));

        return buffer;
    }
}


