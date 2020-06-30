import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer implements ClientHandler.ClientHandlerCallback{
    private final int port;
    private ClientListener mListener;
    private List<ClientHandler> clientHandlerList = new ArrayList<>();
    private final ExecutorService forwardingThreadPoolExecutor;

    public TCPServer(int port){
        this.port = port;
        this.forwardingThreadPoolExecutor = Executors.newSingleThreadExecutor();
    }

    public boolean start(){
        try {
            ClientListener listener = new ClientListener(port);
            mListener = listener;
            listener.start();
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void stop(){
        if (mListener!=null){
            mListener.exit();
        }
        synchronized (TCPServer.this){
            for (ClientHandler clientHandler:clientHandlerList){
                clientHandler.exit();
            }
            clientHandlerList.clear();
        }
        forwardingThreadPoolExecutor.shutdownNow();
    }

    public synchronized void broadcast(String str){
        for(ClientHandler clientHandler:clientHandlerList){
            clientHandler.send(str);
        }
    }

    @Override
    public void onSelfClosed(ClientHandler handler) {
        clientHandlerList.remove(handler);
    }

    @Override
    public void onNewMessageArrived(ClientHandler handler, String msg) {
        System.out.println("Received-"+handler.getClientInfo()+":"+msg);

        forwardingThreadPoolExecutor.execute(()->{
            synchronized (TCPServer.this){
                for (ClientHandler clientHandler:clientHandlerList){
                    if (clientHandler.equals(handler)){
                        continue;
                    }
                    clientHandler.send(msg);
                }
            }
        });
    }

    private class ClientListener extends Thread{
        private ServerSocket server;
        private boolean done = false;

        private ClientListener(int port) throws IOException{
            server = new ServerSocket(port);
            System.out.println("服务器信息："+server.getInetAddress()+"P:"+server.getLocalPort());
        }

        @Override
        public void run(){
            super.run();

            System.out.println("服务器准备就绪~");
            do {
                Socket client;
                try {
                    client = server.accept();
                }catch (IOException e){
                    continue;
                }
                try {
                    ClientHandler clientHandler = new ClientHandler(client,TCPServer.this);
                    clientHandler.readToPrint();
                    synchronized (TCPServer.this){
                        clientHandlerList.add(clientHandler);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                    System.out.println("客户端连接异常："+e.getMessage());
                }
            }while (!done);

            System.out.println("服务器已关闭！");
        }

        void exit(){
            done = true;
            try {
                server.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
