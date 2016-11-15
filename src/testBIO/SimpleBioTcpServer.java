package testBIO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 基于BIO的Socket服务器端
 *
 * @author shirdrn
 */
public class SimpleBioTcpServer extends Thread {

    /** 服务端口号 */
    private int port = 8888;
    /** 为客户端分配编号  */
    private static int sequence = 0;

    public SimpleBioTcpServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        Socket socket = null;
        try {
            ServerSocket serverSocket = new ServerSocket(this.port);
            while(true) {
                socket = serverSocket.accept(); // 监听
                this.handleMessage(socket); // 处理一个连接过来的客户端请求
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理一个客户端socket连接
     * @param socket 客户端socket
     * @throws IOException
     */
    private void handleMessage(Socket socket) throws IOException {
        InputStream in = socket.getInputStream(); // 流：客户端->服务端（读）
        OutputStream out = socket.getOutputStream(); // 流：服务端->客户端（写）
        int receiveBytes;
        byte[] receiveBuffer = new byte[128];
        String clientMessage = "";
        if((receiveBytes=in.read(receiveBuffer))!=-1) {
            clientMessage = new String(receiveBuffer, 0, receiveBytes);
            if(clientMessage.startsWith("I am the client")) {
                String serverResponseWords =
                        "I am the server, and you are the " + (++sequence) + "th client.";
                out.write(serverResponseWords.getBytes());
            }
        }
        out.flush();
        System.out.println("Server: receives clientMessage->" + clientMessage);
    }

    public static void main(String[] args) {
        SimpleBioTcpServer server = new SimpleBioTcpServer(1983);
        server.start();
        System.out.println("BIO Server started!");
    }
}
