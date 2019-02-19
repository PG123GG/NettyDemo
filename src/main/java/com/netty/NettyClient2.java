package com.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.Charset;
import java.util.Date;

public class NettyClient2 {

    public static void main(String[] args) {
        Bootstrap bootstrap = new Bootstrap();

        NioEventLoopGroup group = new NioEventLoopGroup();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        //socketChannel.pipeline() 返回的是和这条连接相关的逻辑处理链，采用了责任链模式，这里不理解没关系，后面会讲到
                        //然后再调用 addLast() 方法 添加一个逻辑处理器，这个逻辑处理器为的就是在客户端建立连接成功之后，向服务端写数据，下面是这个逻辑处理器相关的代码
                        socketChannel.pipeline().addLast(new FirstClientHandler());
                    }
                });

//        bootstrap.handler((ChannelInitializer) (ch)->{
//            ch.pipeline().addLast(new StringEncoder());
//        });

        NettyClient.connect(bootstrap,"127.0.0.1",8000,5);


    }



}

class FirstClientHandler extends ChannelInboundHandlerAdapter{

    /*1、这个逻辑处理器继承自 ChannelInboundHandlerAdapter，然后覆盖了 channelActive()方法，这个方法会在客户端连接建立成功之后被调用
    *
    * 2、客户端连接建立成功之后，调用到 channelActive() 方法，在这个方法里面，我们编写向服务端写数据的逻辑
    *
    * 3、写数据的逻辑分为两步：首先我们需要获取一个 netty 对二进制数据的抽象 ByteBuf，上面代码中,
    * ctx.alloc() 获取到一个 ByteBuf 的内存管理器，这个 内存管理器的作用就是分配一个 ByteBuf，
    * 然后我们把字符串的二进制数据填充到 ByteBuf，这样我们就获取到了 Netty 需要的一个数据格式，
    * 最后我们调用 ctx.channel().writeAndFlush() 把数据写到服务端
    */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(new Date() + ": 客户端写出数据");

        //1、获取数据
        ByteBuf byteBuf = getByteBuf(ctx);

        //2、写数据
//        ctx.channel().write(byteBuf);//使用write方法不会立即写入数据
        ctx.channel().writeAndFlush(byteBuf);
//        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf)msg;

        System.out.println(new Date() + ": 客户端读到数据 -> " + byteBuf.toString(Charset.forName("utf-8")));
    }

    private ByteBuf getByteBuf(ChannelHandlerContext ctx) {
        // 1. 获取二进制抽象 ByteBuf
        ByteBuf buffer = ctx.alloc().buffer();

        // 2. 准备数据，指定字符串的字符集为 utf-8
        byte[] bytes = "你好，PG!".getBytes(Charset.forName("utf-8"));

        // 3. 填充数据到 ByteBuf
        buffer.writeBytes(bytes);

        return buffer;
    }
}
