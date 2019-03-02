package com.pipple;

import com.netty.NettyClient;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * ChannelInboundHandler:从字面意思也可以猜到，他是处理读数据的逻辑，比如，我们在一端读到一段数据，
 * 首先要解析这段数据，然后对这些数据做一系列逻辑处理，最终把响应写到对端， 在开始组装响应之前的所有的逻辑，
 * 都可以放置在 ChannelInboundHandler 里处理，它的一个最重要的方法就是 channelRead()。
 * 可以将 ChannelInboundHandler 的逻辑处理过程与 TCP 的七层协议的解析联系起来，
 * 收到的数据一层层从物理层上升到我们的应用层。
 *
 * ChannelOutBoundHandler:是处理写数据的逻辑，它是定义我们一端在组装完响应之后，把数据写到对端的逻辑，
 * 比如，我们封装好一个 response 对象，接下来我们有可能对这个 response 做一些其他的特殊逻辑，
 * 然后，再编码成 ByteBuf，最终写到对端，它里面最核心的一个方法就是 write()，
 * 读者可以将 ChannelOutBoundHandler 的逻辑处理过程与 TCP 的七层协议的封装过程联系起来，
 * 我们在应用层组装响应之后，通过层层协议的封装，直到最底层的物理层。
 */
public class PippleServer {

    public static void main(String[] args) {


        ServerBootstrap bootstrap = new ServerBootstrap();

        NioEventLoopGroup group = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        bootstrap.group(group,worker)
                 .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        //ouBound类和inBound类之间的顺序一般没有限制，通常情况下，同一种类型的 handler 的添加顺序需要注意
                        socketChannel.pipeline().addLast(new InA());

                        socketChannel.pipeline().addLast(new OuD());

                        socketChannel.pipeline().addLast(new InB());
                        socketChannel.pipeline().addLast(new InC());


                        socketChannel.pipeline().addLast(new OuE());
                        socketChannel.pipeline().addLast(new OuF());

                    }
                });

        bind(bootstrap,8080);
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


class InA extends ChannelInboundHandlerAdapter{

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("处理器A:"+msg);
        super.channelRead(ctx, msg+"AAAA");
    }
}



class InB extends ChannelInboundHandlerAdapter{

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("处理器B:"+msg);
        super.channelRead(ctx, msg+"BBBB");
    }
}


class InC extends ChannelInboundHandlerAdapter{

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("处理器C:"+msg);
//        super.channelRead(ctx, msg);
        //如果把InBoundHandlerC中的ctx.channel().writeAndFlush(msg)改为ctx.writeAndFlush(msg)，则会跳过所有的OutBoundHandler而直接写入到底层。
        //
        ctx.channel().writeAndFlush(msg+"CCCC");//进行写事件才会触发out调用链
    }
}


class OuD extends ChannelOutboundHandlerAdapter{

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        System.out.println("处理器D:"+msg);
        super.write(ctx, msg, promise);

    }
}


/**
 * 在 write() 方法里面，我们打印当前 handler 的信息，然后调用父类的 write() 方法，
 * 而这里父类的 write() 方法会自动调用到下一个 outBoundHandler 的 write() 方法，
 * 并且会把当前 outBoundHandler 里处理完毕的对象传递到下一个 outBoundHandler。
 */
class OuE extends ChannelOutboundHandlerAdapter{

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        System.out.println("处理器E:"+msg);
        super.write(ctx, msg, promise);

    }
}

class OuF extends ChannelOutboundHandlerAdapter{

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        System.out.println("处理器F:"+msg);
        super.write(ctx, msg, promise);

    }
}