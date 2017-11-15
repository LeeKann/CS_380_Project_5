/*
*Eric Kannampuzha
*Project 5
*Class UdpClient.java
*CS 380
*Nima
*/

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.zip.CRC32;
import java.util.Arrays;
import java.nio.ByteBuffer;



public class UdpClient {
     
     public static void main(String[] args) {
        try {
            Socket socket = new Socket("18.221.102.182", 38005);
            System.out.println("Connected to server.");
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            DataInputStream dis = new DataInputStream(is);
            OutputStream os = socket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);
            
            dos.write(ipv4Header(new byte[]{(byte)0xDE,(byte)0xAD,(byte)0xBE,(byte)0xEF}));
            ByteBuffer response = ByteBuffer.allocate(6);
            for(int j = 0; j < 6; j++) {
                response.put(dis.readByte());
            }
            
            System.out.printf("Handshake Response: 0x" + (Integer.toHexString(response.getInt(0))).toUpperCase() + "\n");
            
            int dstPort = (((int)response.getShort(4))&0x0000FFFF);
            System.out.printf("Port number received: " + dstPort + "\n\n");
            
            double rttTotal = 0.0;
            
            for(int i = 1; i <= 12; i++) {
            
                System.out.println("Sending packet with " + (int)Math.pow(2,i) + " bytes of data");
                ByteBuffer udp = ByteBuffer.allocate((int)(8 + Math.pow(2,i)));
                udp.putShort((short)0x0);
                udp.putShort((short)dstPort);
                udp.putShort((short)(8 + Math.pow(2,i)));
                
                
                ByteBuffer noChecksumUdp = ByteBuffer.allocate((int)(20 + Math.pow(2,i)));
                noChecksumUdp.putInt(0x0); //source ipv4
                noChecksumUdp.put((byte)18);               // destination address for temp
                noChecksumUdp.put((byte)221);                      // .
                noChecksumUdp.put((byte)102);                      // .
                noChecksumUdp.put((byte)182);    
                noChecksumUdp.put((byte)0x0);
                noChecksumUdp.put((byte)0x11);
                noChecksumUdp.putShort((short)(8 + Math.pow(2,i)));
                noChecksumUdp.putShort((short)0x0);
                noChecksumUdp.putShort((short)dstPort);
                noChecksumUdp.putShort((short)(8 + Math.pow(2,i)));
                noChecksumUdp.putShort((short)0x0);
                for(int j = 0; j < Math.pow(2,i); j++) { // data is just zeroes
                    noChecksumUdp.put((byte)0x1);
                }
                
                short chk = checksum(noChecksumUdp.array());
                udp.putShort(chk);
                for(int j = 0; j < Math.pow(2,i); j++) { // data is just ones
                    udp.put((byte)0x1);
                }
                byte[] msg = ipv4Header(udp.array());
                
                ByteBuffer reply = ByteBuffer.allocate(4);
                long startTime = System.currentTimeMillis();
                dos.write(msg);
                for(int j = 0; j < 4; j++) {
                    reply.put(dis.readByte());
                }
                long endTime = System.currentTimeMillis();
                long rtt = endTime - startTime;
                rttTotal += rtt;
            
                System.out.printf("Response: 0x" + (Integer.toHexString(reply.getInt(0))).toUpperCase() + "\nRTT: " + rtt + "ms" + "\n\n");
            }
            System.out.printf("Average RTT: %,.2f" + "ms\n", (rttTotal/12.0));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public static byte[] ipv4Header(byte[] data) {
        ByteBuffer ipv4 = ByteBuffer.allocate((int)(20 + data.length));
                
        ipv4.put((byte)0x45);                       // version + HLen
        ipv4.put((byte)0x00);                       // TOS
        ipv4.putShort((short)(20 + data.length));   // total length
        ipv4.putShort((short)0x00);                 // ident
        ipv4.put((byte)0x40);                       // Flags
        ipv4.put((byte)0x00);                       // Offset
        ipv4.put((byte)50);                         // TTL
        ipv4.put((byte)0x11);                       // Protocol = UDP
        
        ByteBuffer noChecksumIpv4 = ipv4.duplicate();       // temp bytebuffer for checksum
        noChecksumIpv4.put((byte)0x0);                      //  temp checksum
        noChecksumIpv4.put((byte)0x0);                      //  temp checksum
        noChecksumIpv4.putInt((int)0x0);                    // source address for temp
        noChecksumIpv4.put((byte)18);                       // destination address for temp
        noChecksumIpv4.put((byte)221);                      // .
        noChecksumIpv4.put((byte)102);                      // .
        noChecksumIpv4.put((byte)182);                      // .
        
        ipv4.putShort((short)checksum(noChecksumIpv4.array()));     // Checksum inserted
        ipv4.putInt((int)0x0);                                      // source address for packet
        ipv4.put((byte)18);                                         // destination address for packet
        ipv4.put((byte)221);                                        // .
        ipv4.put((byte)102);                                        // .
        ipv4.put((byte)182);                                        // .
        
        ipv4.put(data);
        
        System.out.println("data length: " + data.length);
        return ipv4.array();

    }
    
    public static short checksum(byte[] b) {
        int sum = 0;
        
        for(int i = 0; i < b.length; i++) {
            byte upper = b[i++];
            byte lower;
            
            if(i < b.length)
                lower = b[i];
            else
                lower = 0;
            
            int result = 0;
            result = (result | (upper<<0x8 & 0xFF00));
            result = (result | (lower & 0x00FF));

            sum += result;
            if((sum & 0xFFFF0000) != 0) {
                sum = sum & 0xFFFF;
                sum += 1;
            }
        }
        return (short)~(sum & 0xFFFF);
    }
        

}
