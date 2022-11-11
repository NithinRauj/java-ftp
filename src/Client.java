import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.security.MessageDigest;

public class Client {
    private static int port;
    private static String host;
    private static final int KEY = 1217;

    private static Socket socket = null;
    private static DataInputStream inputStream = null;
    private static DataOutputStream outputStream = null;
    private static FileInputStream fileInputStream = null;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("---FTP Client---");
        try {
            System.out.println("Enter hostname");
            host = scanner.nextLine();
            System.out.println("Enter port");
            port = Integer.parseInt(scanner.nextLine());
            System.out.println("Enter 1 to signin and 2 to sign up");
            String option = scanner.nextLine();
            if (option.equals("1")) {
                signIn();
            }
            if (option.equals("2")) {
                signUp();
            }
            outputStream.close();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void signIn() {
        try {
            System.out.println("Enter username:");
            String username = scanner.nextLine().trim();
            System.out.println("Enter password:");
            String password = scanner.nextLine().trim();

            socket = new Socket(host, port);
            System.out.println("Connected to server on port " + port);
            outputStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream.writeUTF("1:" + username + ":" + password);
            boolean loggedIn = inputStream.readBoolean();
            if (loggedIn) {
                System.out.println("Client logged in. Proceeding with file transfer");
                proceedWithFileTransfer();
            } else {
                System.out.println("Client failed to log in");
            }
            inputStream.close();
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

            socket = new Socket(host, port);
            System.out.println("Connected to server on port " + port);
            outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeUTF("2:" + username + ":" + password);
            proceedWithFileTransfer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void proceedWithFileTransfer() {
        System.out.println("Enter path of file to upload:(input/mountain.jpeg)");
        String path = scanner.nextLine();
        System.out.println("Enter output file name:(output.jpeg)");
        String outputFile = scanner.nextLine();
        sendFile(path, outputFile);
    }

    private static void sendFile(String path, String outputFile) {
        try {
            int bytes = 0;
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                fileInputStream = new FileInputStream(file);
                outputStream.writeLong(file.length());
                outputStream.writeUTF(outputFile);
                byte[] buffer = new byte[4 * 1024];
                while ((bytes = fileInputStream.read(buffer)) != -1) {
                    byte[] encryptedData = encrypt(buffer);
                    outputStream.write(encryptedData, 0, bytes);
                    outputStream.flush();
                    System.out.println("Writing encrypted data to stream");
                }
                fileInputStream.close();
                fileInputStream = null;
                fileInputStream = new FileInputStream(file);
                String checksum = getChecksum();
                outputStream.writeUTF(checksum);
                fileInputStream.close();
            } else {
                System.out.println("File not found.Aborting transfer");
                outputStream.writeLong(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Encrypt using XOR operation
    private static byte[] encrypt(byte[] buffer) {
        int i = 0;
        byte[] encryptedData = new byte[4 * 1024];
        i = 0;
        for (byte b : buffer) {
            encryptedData[i] = (byte) (b ^ KEY);
            i++;
        }
        return encryptedData;
    }

    // Calculate MD5 hash for file contents for integrity check
    private static String getChecksum() {
        try {
            MessageDigest mdigest = MessageDigest.getInstance("MD5");
            byte[] byteArray = new byte[1024];
            int bytesCount = 0;

            while ((bytesCount = fileInputStream.read(byteArray)) != -1) {
                mdigest.update(byteArray, 0, bytesCount);
            }
            fileInputStream.close();

            byte[] bytes = mdigest.digest();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer
                        .toString((bytes[i] & 0xff) + 0x100, 16)
                        .substring(1));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
