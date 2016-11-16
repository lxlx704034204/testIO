package testNIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.logging.Logger;

/**
 * NIO客户端
 *
 * @author shirdrn
 */
public class NioTcpClient {

    private static final Logger log = Logger.getLogger(NioTcpClient.class.getName());
    private InetSocketAddress inetSocketAddress;

    public NioTcpClient(String hostname, int port) {
        inetSocketAddress = new InetSocketAddress(hostname, port);
    }
    private static int pos = 0;
    /**
     * 发送请求数据
     * @param requestData
     */
    public void send(String requestData) {
        try {
            SocketChannel socketChannel = SocketChannel.open(inetSocketAddress);
            socketChannel.configureBlocking(false);
            ByteBuffer byteBuffer = ByteBuffer.allocate(512);
            socketChannel.write(ByteBuffer.wrap(requestData.getBytes()));
            //用户进程需要时不时的询问IO操作是否就绪，这就要求用户进程不停的去询问。
            while (true) {
                byteBuffer.clear();
                int readBytes = socketChannel.read(byteBuffer);
                if (readBytes > 0) {
                    byteBuffer.flip();
                    log.info(Thread.currentThread().getName() + "---Client: readBytes = " + readBytes + "Client: data = " + new String(byteBuffer.array(), 0, readBytes) + "---" + System.currentTimeMillis());
                    socketChannel.close();
                    log.info(Thread.currentThread().getName() + "---我发完关闭通道了---" + System.currentTimeMillis());
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String hostname = "localhost";
        String requestData = "Actions speak louder than words!";
        int port = 1000;
//        new NioTcpClient(hostname, port).send(requestData);

        int n = 2;
        StringBuffer data = new StringBuffer();
        Date start = new Date();
        for(int i=0; i<n; i++) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    data.delete(0, data.length());
                    data.append("I am the client ").append(++pos).append(".");
                    NioTcpClient client = new NioTcpClient(hostname, port);
                    client.send(requestData +"---" + Thread.currentThread().getName());
                    for(int i=0;i<100;i++){
                        System.out.println(Thread.currentThread().getName() +"---"+i+"我跑我的"+System.currentTimeMillis());
                    }
                    System.out.println(Thread.currentThread().getName() + "---我跑完了" + System.currentTimeMillis());
                }
            });
            t.start();
        }
        Date end = new Date();
        long cost = end.getTime() - start.getTime();
        System.out.println(Thread.currentThread().getName() + "---"+n + " requests cost " + cost + " ms.");
    }
}
