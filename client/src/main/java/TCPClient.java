import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class TCPClient {
    public static void linkWith(ServerInfo info) throws IOException{
        Socket socket = new Socket();
        socket.setSoTimeout(3000);

        socket.connect(new InetSocketAddress(Inet4Address.getByName(info.getAddress()),info.getPort()),3000);

        System.out.println("已发起服务器连接，并进入后续流程~");
        System.out.println("客户端信息："+socket.getLocalAddress()+"P:"+socket.getLocalPort());
        System.out.println("服务器信息："+socket.getInetAddress()+"P:"+socket.getPort());

        try {
            ReadHandler readHandler = new ReadHandler(socket.getInputStream());
            readHandler.start();

            write(socket);

            readHandler.exit();
        }catch (Exception e){
            System.out.println("客户端已退出~");
        }
    }

    private static void write(Socket client) throws IOException{
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        OutputStream outputStream = client.getOutputStream();
        PrintStream socketPrintStream = new PrintStream(outputStream);

        do {
            String str = input.readLine();
            socketPrintStream.println(str);

            if("00bye00".equalsIgnoreCase(str)){
                break;
            }
        }while (true);

        socketPrintStream.close();
    }

    static class ReadHandler extends Thread{
        private boolean done = false;
        private final InputStream inputStream;

        ReadHandler(InputStream inputStream){
            this.inputStream = inputStream;
        }

        @Override
        public void run(){
            super.run();
            try {
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream));

                do {
                    String str;
                    try {
                        str = socketInput.readLine();
                    }catch (SocketTimeoutException e){
                        continue;
                    }
                    if (str == null){
                        System.out.println("连接已关闭，无法读取数据！");
                        break;
                    }
                    System.out.println(str);
                }while (!done);
            }catch (Exception e){
                if (!done){
                    System.out.println("连接异常断开："+e.getMessage());
                }
            }finally {
                CloseUtils.close(inputStream);
            }
        }

        void exit(){
            done = true;
            CloseUtils.close(inputStream);
        }
    }
}
