package com.maya;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class Ex3Client {

    private static final String HOST_IP = "18.221.102.182";
    private static final int PORT = 38103;

    public static void main(String[] args) {
        Ex3Client client = new Ex3Client();
        client.main();
    }

    public short checksum(byte[] b){
        int sum = 0;
        for (int i = 0; i < b.length; i+=2){
            //get first 2 halfs of values
            int firstHalf = b[i] << 8;
            firstHalf &= 0xFF00;
            int secondHalf = b[i+1] & 0xFF;

            sum += firstHalf + secondHalf;

            if ((sum & 0xFFFF0000) != 0){
                sum &= 0xFFFF;
                sum++;
            }
        }
        System.out.println(sum);
        return (short)(~(sum & 0xFFFF));
    }

    public void main(){
        try (Socket socket = new Socket(HOST_IP, PORT)) {
            //Read from server
            InputStream is = socket.getInputStream();

            //Write to server
            OutputStream os = socket.getOutputStream();
            ByteBuffer bb = ByteBuffer.allocate(2);

            //find size of byte array
            int size = is.read() & 0xFF;
            int arraySize = (size % 2 == 0) ? size: size+1;
            byte[] val = new byte[arraySize];
            System.out.printf("Reading %d bytes\n", arraySize);

            //read in values to byte array
            for(int i = 0; i < size; i++) {
                val[i] = (byte) is.read();
            }

            //print byteArray val result
            StringBuilder builder = new StringBuilder();
            builder.append("Value: ");
            for (byte b : val){
                builder.append(String.format("%02x", b));
            }
            System.out.println(builder.toString());

            //get checkSum
            short checkSum = checksum(val);
            ByteBuffer bt = ByteBuffer.allocate(2);
            bt.putShort(checkSum);

            //write to server
            os.write(bt.array());

            //print whether or not guess is correct
            String determineCorrect = (is.read() == 1) ? "correct": "incorrect";
            System.out.println("Input is " + determineCorrect);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
