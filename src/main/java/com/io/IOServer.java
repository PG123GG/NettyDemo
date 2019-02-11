package com.io;

import java.net.ServerSocket;
import java.net.Socket;

public class IOServer {

    /**
     * 传统IO的服务端
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception{

        ServerSocket serverSocket = new ServerSocket(8000);

        // (1) 接收新连接线程
        new Thread(() ->{
            while (true){
                try {
                    // (1) 阻塞方法获取新的连接
                    Socket socket = serverSocket.accept();

                    // (2) 每一个新的连接都创建一个线程，负责读取数据
                    while (true){
                        try {
                            int len = 0;
                            byte[] bytes = new byte[1024];
                            while ((len = socket.getInputStream().read(bytes)) != -1){
                                System.out.println(new String(bytes,0,len));
                            }
                        }catch (Exception e){

                        }
                    }

                }catch (Exception e){

                }
            }
        }).start();
    }


}
