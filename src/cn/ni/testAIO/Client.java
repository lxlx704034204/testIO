package cn.ni.testAIO;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;


//public class Client implements Runnable{
public class Client extends Thread{
    final static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private AsynchronousSocketChannel channel;
    private Helper helper;
    private CountDownLatch latch;
    private final Queue<ByteBuffer> queue = new LinkedList<ByteBuffer>();
    private boolean writing = false;

    public Client(AsynchronousChannelGroup channelGroup, CountDownLatch latch) throws IOException, InterruptedException{
        this.latch = latch;
        helper = new Helper();
        initChannel(channelGroup);
    }

    private void initChannel(AsynchronousChannelGroup channelGroup) throws IOException {
        //在默认channel group下创建一个socket channel
        channel = AsynchronousSocketChannel.open(channelGroup);
        //设置Socket选项
        channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
        channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int sleepTime = Integer.parseInt("10");
        Helper.sleep(sleepTime);

        /**
         * AsynchronousChannelGroup其内部其实是一个(一些)线程池来进行实质工作的；而他们干的活就是等待IO事件，处理数据，分发各个注册的completion handlers
         AsynchronousChannelGroup可以理解为一个JVM中对于Socket相关操作的一些公共资源的代表。
         一个ChannelGroup和一个(或2个)thread pool关联。
         */
        AsynchronousChannelGroup channelGroup = AsynchronousChannelGroup.withFixedThreadPool(Runtime.getRuntime().availableProcessors(), Executors.defaultThreadFactory());
        //只能跑一个线程，第二个线程connect会挂住，暂时不明原因
        //试过同时跑多个客户端，同一时间也只能一个连接成功，其他的连接都被挂起，但channel都是打开的。
        //服务器端只响应已经连接的线程发送的请求，并且每个请求一个线程，这符合aio的设计方式。已连接但未请求的线程，服务器不新建线程。
        final int THREAD_NUM = 2;
        CountDownLatch latch = new CountDownLatch(THREAD_NUM);

        //创建个多线程模拟多个客户端，模拟失败，无效
        //只能通过命令行同时运行多个进程来模拟多个客户端
        for(int i=0; i<THREAD_NUM; i++){
            Client c = new Client(channelGroup, latch);
            c.start();
            System.out.println(c.getName() + "---start");
//            Thread t = new Thread(c);
//            System.out.println(t.getName() + "---start");
//            t.start();
            //让主线程等待子线程处理再退出, 这对于异步调用无效
            //t.join();

        }

        latch.await();

        if(channelGroup !=null){
            channelGroup.shutdown();
        }

        System.out.println("#####################我是main函数结束all work done at "+sdf.format(new Date())+","+System.currentTimeMillis());
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + "---run,我开始跑了"+System.currentTimeMillis());


        /**
         * 在 Java 8 之前，因为没有 Lambda 支持只能应用内部类的方式。JDK 提供了以下异步 Channel 来实现异步操作
         AsynchronousFileChannel
         AsynchronousSocketChannel
         AsynchronousServerSocketChannel
         AsynchronousDatagramChannel
         获取异步结果的方式有 Future 和  CompletionHandler
         */
        //连接服务器
        channel.connect(new InetSocketAddress("localhost", 8383), null, new CompletionHandler<Void, Void>(){
//            final ByteBuffer readBuffer = ByteBuffer.allocateDirect(1024);
            ByteBuffer readBuffer = ByteBuffer.allocateDirect(1024);

            @Override
            public void completed(Void result, Void attachment) {
                //连接成功后, 异步调用OS向服务器写一条消息
                try {
                    //channel.write(CharsetHelper.encode(CharBuffer.wrap(helper.getWord())));
                    System.out.println(Thread.currentThread().getName() + "向服务器异步写消息---" + new Date());
                    writeStringMessage(helper.getWord());
                } catch (CharacterCodingException e) {
                    e.printStackTrace();
                }

                //helper.sleep();//等待写异步调用完成
                readBuffer.clear();
                //异步调用OS读取服务器发送的消息
                channel.read(readBuffer, null, new CompletionHandler<Integer, Object>() {

                    @Override
                    public void completed(Integer result, Object attachment) {
                        try {
                            //异步读取完成后处理
                            if (result > 0) {
                                /**
                                 * flip的作用有两个：
                                 1. 把limit设置为当前的position值
                                 2. 把position设置为0
                                 然后处理的数据就是从position到limit直接的数据，也就是你刚刚读取过来的数据
                                 */
                                readBuffer.flip();//反转
                                CharBuffer charBuffer = CharsetHelper.decode(readBuffer);
                                String answer = charBuffer.toString();
                                System.out.println(Thread.currentThread().getName() +"-"+ new Date()+ "---" + answer );
                                readBuffer.clear();

                                String word = helper.getWord();
                                if (word != null) {
                                    //异步写
                                    //channel.write(CharsetHelper.encode(CharBuffer.wrap(word)));
                                    writeStringMessage(word);
                                    //helper.sleep();//等待异步操作
                                    channel.read(readBuffer, null, this);
                                } else {
                                    //不想发消息了，主动关闭channel
                                    shutdown();
                                    System.out.println(Thread.currentThread().getName() + "---hi,我执行完通道关闭了。" + new Date());
                                }
                            } else {
                                //对方已经关闭channel，自己被动关闭，避免空循环
                                shutdown();
                                System.out.println(Thread.currentThread().getName() + "---通道被动关闭。" + new Date());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    /**
                     * 读取失败处理
                     * @param exc
                     * @param attachment
                     */
                    @Override
                    public void failed(Throwable exc, Object attachment) {
                        System.out.println("client read failed: " + exc);
                        try {
                            shutdown();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                });
            }

            /**
             * 连接失败处理
             * @param exc
             * @param attachment
             */
            @Override
            public void failed(Throwable exc, Void attachment) {
                System.out.println("client connect to server failed: " + exc);

                try {
                    shutdown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //各线程自己跑自己的任务
        for(int i = 0;i< 1000; i++){
            //通道未关闭我就一直自己跑自己的
            if(!channel.isOpen()){
                System.out.println(Thread.currentThread().getName() + "---通道关闭了，我不用跑了，耶！"+i+"-"+System.currentTimeMillis());
                break;
            }
            System.out.println(Thread.currentThread().getName() + "---我跑我的"+i+"-"+System.currentTimeMillis());
        }

        try {
            shutdown();
            System.out.println(Thread.currentThread().getName() + "---我执行完强制关闭通道。" + new Date());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + "---我结束了，真的。"+System.currentTimeMillis());
    }

    private void shutdown() throws IOException {

        if(channel != null){
            channel.close();
        }

        latch.countDown();
    }

    /**
     * Enqueues a write of the buffer to the channel.
     * The call is asynchronous so the buffer is not safe to modify after
     * passing the buffer here.
     *
     * @param buffer the buffer to send to the channel
     */
    private void writeMessage(final ByteBuffer buffer) {
        boolean threadShouldWrite = false;

        synchronized(queue) {
            queue.add(buffer);
            // Currently no thread writing, make this thread dispatch a write
            if (!writing) {
                writing = true;
                threadShouldWrite = true;
            }
        }

        if (threadShouldWrite) {
            writeFromQueue();
        }
    }

    private void writeFromQueue() {
        ByteBuffer buffer;

        synchronized (queue) {
            buffer = queue.poll();
            if (buffer == null) {
                writing = false;
            }
        }

        // No new data in buffer to write
        if (writing) {
            writeBuffer(buffer);
        }
    }

    private void writeBuffer(ByteBuffer buffer) {
        channel.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer buffer) {
                if (buffer.hasRemaining()) {
                    channel.write(buffer, buffer, this);
                } else {
                    // Go back and check if there is new data to write
                    writeFromQueue();
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
            }
        });
    }

    /**
     * Sends a message
     * @param msg the message
     * @throws CharacterCodingException
     */
    public void writeStringMessage(String msg) throws CharacterCodingException {
        writeMessage(Charset.forName("UTF-8").newEncoder().encode(CharBuffer.wrap(msg)));
    }
}