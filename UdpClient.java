/*
*Eric Kannampuzha
*Project 3
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
            Socket socket = new Socket("18.221.102.182", 38003);
            System.out.println("Connected to server.");
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            OutputStream os = socket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);
            

            
            for(int i = 1; i <= 12; i++) {
                
                ByteBuffer ipv4 = ByteBuffer.allocate((int)(20 + Math.pow(2,i)));
                
                ipv4.put((byte)0x45);        // version + HLen
                ipv4.put((byte)0x00);        // TOS
                ipv4.putShort((short)(20 + Math.pow(2,i))); // total length
                ipv4.putShort((short)0x00);        // ident
                ipv4.put((byte)0x40);        // Flags
                ipv4.put((byte)0x00);        // Offset
                ipv4.put((byte)50);          // TTL
                ipv4.put((byte)0x06);        // Protocol
                
                ByteBuffer noChecksumIpv4 = ipv4.duplicate();     // temp bytebuffer for checksum
                noChecksumIpv4.put((byte)0x0);        //  temp checksum
                noChecksumIpv4.put((byte)0x0);        //  temp checksum
                noChecksumIpv4.putInt((int)0x0);      // source address for temp
                noChecksumIpv4.put((byte)18);         // destination address for temp
                noChecksumIpv4.put((byte)221);        // .
                noChecksumIpv4.put((byte)102);        // .
                noChecksumIpv4.put((byte)182);        // .
                
                ipv4.putShort((short)checksum(noChecksumIpv4.array()));    // Checksum inserted
                ipv4.putInt((int)0x0);      // source address for packet
                ipv4.put((byte)18);         // destination address for packet
                ipv4.put((byte)221);        // .
                ipv4.put((byte)102);        // .
                ipv4.put((byte)182);        // .
                
                for(int j = 0; j < Math.pow(2,i); j++) // data is just zeroes
                    ipv4.put((byte)0x0);
                
                System.out.println("data length: " + (int)Math.pow(2,i));
                dos.write(ipv4.array());
                String check = br.readLine();
                System.out.println(check);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
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
