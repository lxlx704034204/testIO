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
            while (true) {
                byteBuffer.clear();
                int readBytes = socketChannel.read(byteBuffer);
                if (readBytes > 0) {
                    byteBuffer.flip();
                    log.info("Client: readBytes = " + readBytes);
                    log.info("Client: data = " + new String(byteBuffer.array(), 0, readBytes));
                    socketChannel.close();
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

        int n = 3;
        StringBuffer data = new StringBuffer();
        Date start = new Date();
        for(int i=0; i<n; i++) {
            data.delete(0, data.length());
            data.append("I am the client ").append(++pos).append(".");
            NioTcpClient client = new NioTcpClient(hostname, port);
            client.send(requestData+i);
        }
        Date end = new Date();
        long cost = end.getTime() - start.getTime();
        System.out.println(n + " requests cost " + cost + " ms.");
    }
}
