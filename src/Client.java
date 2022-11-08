import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final int PORT = 5000;
    private static DataOutputStream outputStream = null;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("---FTP Client---");
        try {
            Socket socket = new Socket("localhost", PORT);
            System.out.println("Connected to server on port 5000");
            outputStream = new DataOutputStream(socket.getOutputStream());
            System.out.println("Enter path of file to upload:");
            String path = scanner.nextLine();
            System.out.println("Enter output file name:(output.png)");
            String outputFile = scanner.nextLine();
            sendFile(path, outputFile);

            outputStream.close();
            socket.close();

        } catch (Exception e) {
            System.out.println("Error" + e.toString());
        }
    }

    private static void sendFile(String path, String outputFile) {
        try {
            int bytes = 0;
            File file = new File(path);
            FileInputStream fileInputStream = new FileInputStream(file);
            outputStream.writeLong(file.length());
            outputStream.writeUTF(outputFile);
            byte[] buffer = new byte[4 * 1024];
            while ((bytes = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytes);
                outputStream.flush();
                System.out.println("File written to stream...");
            }
            fileInputStream.close();
        } catch (Exception e) {
            System.out.println("Error in sending file" + e.toString());
        }

    }
}
