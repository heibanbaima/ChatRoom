import java.io.File;
import java.io.IOException;

import constants.TCPConstants;

public class Server {
    public static void main(String[] args) throws IOException{
        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER);
        boolean isSucceed = tcpServer.start();
        if (!isSucceed){
            System.out.println("Start TCP server failed!");
            return;
        }

        UDP
    }
}
