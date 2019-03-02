package com.protocol.client;

import com.netty.NettyClient;
import com.protocol.LoginRequestPacket;
import com.protocol.PacketCodeC;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.charset.Charset;

public class LoginClient {

    public static void main(String[] args) {

        Bootstrap bootstrap = new Bootstrap();

        NioEventLoopGroup group = new NioEventLoopGroup();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new LoginHandle());
                    }
                });
//                .bind("127.0.0.1",8080);

        NettyClient.connect(bootstrap,"127.0.0.1",8000,5);
    }

}

class LoginHandle extends ChannelInboundHandlerAdapter{

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("开始登陆");
        LoginRequestPacket packet = new LoginRequestPacket();
        packet.setUserId(1);
        packet.setPassword("public");
        packet.setUserName("张三");

        ByteBuf byteBuf = PacketCodeC.encode(packet);

        ctx.channel().writeAndFlush(byteBuf);
        System.out.println("开始发送登陆消息");



    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        System.out.println("开始接收服务端返回的信息");
        ByteBuf byteBuf = (ByteBuf)msg;

        String s = byteBuf.toString(Charset.forName("utf-8"));
        System.out.println(s);

        super.channelRead(ctx, msg);

        //他的作用就是接收上一个 handler 的输出，这里的 msg 就是上一个 handler 的输出。
        // 大家也可以看到，默认情况下 adapter 会通过 fireChannelRead()
        // 方法直接把上一个 handler 的输出结果传递到下一个 handler。
        ctx.fireChannelRead(msg);
    }


}