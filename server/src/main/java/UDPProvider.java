import constants.UDPConstants;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPProvider {
    private static Provider

    private static class Provider extends Thread{
        private final byte[] sn;
        private final int port;
        private boolean done = false;
        private DatagramSocket ds = null;
        final byte[] buffer = new byte[128];

        Provider(String sn,int port){
            super();
            this.sn = sn.getBytes();
            this.port = port;
        }

        @Override
        public void run(){
            super.run();

            System.out.println("UDPProvider Started.");

            try {
                ds = new DatagramSocket(UDPConstants.PORT_SERVER);
                DatagramPacket receivePack = new DatagramPacket(buffer,buffer.length);
                while (!done){
                    ds.receive(receivePack);
                    String clientIP = receivePack.getAddress().getHostAddress();
                    int clientPort = receivePack.getPort();
                    int clientDataLen = receivePack.getLength();
                    byte[] clientData = receivePack.getData();
                    boolean isValid = clientData>=(UDPConstants.HEADER.length+2+4)&&
                }
            }
        }
    }
}
