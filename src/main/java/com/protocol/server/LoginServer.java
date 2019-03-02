package com.protocol.server;

import com.protocol.LoginRequestPacket;
import com.protocol.Packet;
import com.protocol.PacketCodeC;
import com.sun.org.apache.bcel.internal.generic.NEW;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;

import java.nio.charset.Charset;

public class LoginServer {


    public static void main(String[] args) {

        ServerBootstrap bootstrap = new ServerBootstrap();


        NioEventLoopGroup bootGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        bootstrap.group(bootGroup,workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new ServerLoginHandle());
                    }
                });
        bind(bootstrap,8000);
    }

    public static void bind(ServerBootstrap bootstrap,int port){

        bootstrap.bind(port).addListener((future -> {

            if(future.isSuccess()){
                System.out.println("端口"+port+"绑定成功");
            }else {
                System.out.println("端口"+port+"绑定失败");
            }

        }));
    }

}


class ServerLoginHandle extends ChannelInboundHandlerAdapter{

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("开始接受登陆信息");
        ByteBuf byteBuf = (ByteBuf)msg;

        Channel channel = ctx.channel();

        //使用channel的attr方法对某个属性进行赋值,在取某个状态时可以进行动态维护
//        channel.attr(AttributeKey.newInstance("name")).set(true);

        Packet packet = PacketCodeC.decode(byteBuf);

//        if (packet instanceof LoginRequestPacket){
            LoginRequestPacket logMsg = (LoginRequestPacket)packet;

            if(logMsg != null){
                System.out.println("接收到登陆信息,用户名为 ： "+logMsg.getUserName()+",用户密码为："+logMsg.getPassword());


                ByteBuf outBufer = ctx.alloc().buffer();
                outBufer.writeBytes(("你好！用户"+logMsg.getUserName()+"登录成功").getBytes(Charset.forName("UTF-8")));
                channel.writeAndFlush(outBufer);

                System.out.println("服务端响应登陆信息");
            }


//        }

    }
}
