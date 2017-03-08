package testBIO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * 基于BIO的Socket客户端
 *
 * @author shirdrn
 */
public class SimpleBioTcpClient {

    private String ipAddress;
    private int port;
    private static int pos = 0;

    public SimpleBioTcpClient() {}

    public SimpleBioTcpClient(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    /**
     * 连接Socket服务端，并模拟发送请求数据
     * @param data 请求数据
     */
    public void send(byte[] data) {
        Socket socket = null;
        OutputStream out = null;
        InputStream in = null;
        try {
            socket = new Socket(this.ipAddress, this.port); // 连接
            // 发送请求
            out = socket.getOutputStream();
            out.write(data);
            out.flush();
            // 接收响应
            in = socket.getInputStream();
            int totalBytes = 0;
            int receiveBytes = 0;
            byte[] receiveBuffer = new byte[128];
            if((receiveBytes=in.read(receiveBuffer))!=-1) {
                totalBytes += receiveBytes;
            }
            String serverMessage = new String(receiveBuffer, 0, receiveBytes);
            System.out.println("Client: receives serverMessage->" + serverMessage);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 发送请求并接收到响应，通信完成，关闭连接
                out.close();
                in.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        final int n = 2;
        final StringBuffer data = new StringBuffer();
        Date start = new Date();
        for(int i=0; i<n; i++) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    data.delete(0, data.length());
                    data.append("I am the client ").append(++pos).append(".").append("---"+Thread.currentThread().getName() );
                    SimpleBioTcpClient client = new SimpleBioTcpClient("localhost", 1983);
                    System.out.println(Thread.currentThread().getName() +"---我向服务器发消息了"+System.currentTimeMillis());
                    client.send(data.toString().getBytes());
                    for(int i=0;i<100;i++){
                        System.out.println(Thread.currentThread().getName() +"---"+i+"我跑我的"+System.currentTimeMillis());
                    }
                    System.out.println(Thread.currentThread().getName() +"---我跑完了"+System.currentTimeMillis());
                }
            });
            t.start();
        }
        Date end = new Date();
        long cost = end.getTime() - start.getTime();
        System.out.println(n + " requests cost " + cost + " ms.");
    }
}