import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {
    private final Socket socket;
    private final ClientReadHandler readHandler;
    private final ClientWriteHandler writeHandler;
    private final ClientHandlerCallback clientHandlerCallback;
    private final String clientInfo;

    public ClientHandler(Socket socket,ClientHandlerCallback clientHandlerCallback) throws IOException {
        this.socket = socket;
        this.readHandler = new ClientReadHandler(socket.getInputStream());
        this.writeHandler = new ClientWriteHandler(socket.getOutputStream());
        this.clientHandlerCallback = clientHandlerCallback;
        this.clientInfo = "A["+socket.getInetAddress().getHostAddress()+"] P["+socket.getPort()+"]";
        System.out.println("新客户端连接："+clientInfo);
    }

    public String getClientInfo(){
        return clientInfo;
    }

    public void exit(){
        readHandler.exit();
        writeHandler.exit();
        CloseUtils.close(socket);
        System.out.println("客户端已退出："+socket.getInetAddress()+"P:"+socket.getPort());
    }

    public void send(String str){
        writeHandler.send(str);
    }

    public void readToPrint(){
        readHandler.start();
    }

    private void exitBySelf(){
        exit();
        clientHandlerCallback.onSelfClosed(this);
    }

    public interface ClientHandlerCallback{
        void onSelfClosed(ClientHandler handler);

        void onNewMessageArrived(ClientHandler handler,String msg);
    }

    class ClientReadHandler extends Thread{
        private boolean done = false;
        private final InputStream inputStream;

        ClientReadHandler(InputStream inputStream){
            this.inputStream = inputStream;
        }

        @Override
        public void run(){
            super.run();
            try {
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream));

                do{
                    String str = socketInput.readLine();
                    if (str == null){
                        System.out.println("客户端已无法读取数据！");
                        ClientHandler.this.exitBySelf();
                        break;
                    }

                    clientHandlerCallback.onNewMessageArrived(ClientHandler.this,str);
                }while (!done);
            }catch (Exception e){
                if(!done){
                    System.out.println("连接异常断开");
                    ClientHandler.this.exitBySelf();
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

    class ClientWriteHandler{
        private boolean done = false;
        private final PrintStream printStream;
        private final ExecutorService executorService;

        ClientWriteHandler(OutputStream outputStream){
            this.printStream = new PrintStream(outputStream);
            this.executorService = Executors.newSingleThreadExecutor();
        }

        void exit(){
            done = true;
            CloseUtils.close(printStream);
            executorService.shutdownNow();
        }

        void send(String str){
            if(done){
                return;
            }
            executorService.execute(new WriteRunnable(str));
        }

        class WriteRunnable implements Runnable{
            private final String msg;

            WriteRunnable(String msg){
                this.msg = msg;
            }

            @Override
            public void run(){
                if (ClientWriteHandler.this.done){
                    return;
                }

                try {
                    ClientWriteHandler.this.printStream.println(msg);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

}
