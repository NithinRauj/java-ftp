import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;

public class Server {
    private static DataInputStream inputStream = null;

    public static void main(String[] args) {
        System.out.println("---FTP Server---");
        try {
            ServerSocket serverSocket = new ServerSocket(5000);
            System.out.println("Listening on port 5000");
            Socket clienSocket = serverSocket.accept();
            inputStream = new DataInputStream(clienSocket.getInputStream());

            receiveFile("output.png");
            inputStream.close();
            clienSocket.close();
            serverSocket.close();

        } catch (Exception e) {
            System.out.println("Error" + e.toString());
        }
    }

    private static void receiveFile(String path) {
        try {
            long fileSize = inputStream.readLong();
            String outputFile = inputStream.readUTF();
            File file = new File(outputFile);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            int bytes = 0;
            byte[] buffer = new byte[4 * 1024];
            while (fileSize > 0
                    && (bytes = inputStream.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                fileOutputStream.write(buffer, 0, bytes);
                fileSize -= bytes;
                System.out.println("Writing file...");
            }
            System.out.println("File uploaded to server");
            fileOutputStream.close();
        } catch (Exception e) {
            System.out.println("Error in receiving file" + e.toString());
        }
    }
}
