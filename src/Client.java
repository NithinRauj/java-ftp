import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final int PORT = 5000;
    private static Socket socket = null;
    private static DataOutputStream outputStream = null;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("---FTP Client---");
        try {
            System.out.println("Enter 1 to signin and 2 to sign up");
            String option = scanner.nextLine();
            if (option.equals("1")) {
                signIn();
            }
            if (option.equals("2")) {
                signUp();
            }

        } catch (Exception e) {
            System.out.println("Error" + e.toString());
        }
    }

    private static void signIn() {
        try {
            System.out.println("Enter username:");
            String username = scanner.nextLine();
            System.out.println("Enter password:");
            String password = scanner.nextLine();

            socket = new Socket("localhost", PORT);
            System.out.println("Connected to server on port " + PORT);
            outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeUTF("1:" + username + ":" + password);
            proceedWithFileTransfer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void signUp() {
        try {
            System.out.println("Enter username:");
            String username = scanner.nextLine();
            System.out.println("Enter password:");
            String password = scanner.nextLine();

            socket = new Socket("localhost", PORT);
            System.out.println("Connected to server on port " + PORT);
            outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeUTF("2:" + username + ":" + password);
            proceedWithFileTransfer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void proceedWithFileTransfer() throws Exception {
        System.out.println("Enter path of file to upload:");
        String path = scanner.nextLine();
        System.out.println("Enter output file name:(output.png)");
        String outputFile = scanner.nextLine();
        sendFile(path, outputFile);

        outputStream.close();
        socket.close();
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
                System.out.println("File written to stream");
            }
            fileInputStream.close();
        } catch (Exception e) {
            System.out.println("Error in sending file" + e.toString());
        }

    }
}
