import java.io.*;
import java.util.HashMap;
import java.net.Socket;
import java.security.MessageDigest;
import java.net.ServerSocket;

public class Server {
    private static final int PORT = 5000;
    private static final int KEY = 1217;

    private static DataInputStream inputStream = null;
    private static DataOutputStream outputStream = null;
    private static FileOutputStream fileOutputStream = null;
    private static FileInputStream fileInputStream = null;
    private static FTPAuthentication authenticator = null;

    public static void main(String[] args) {
        System.out.println("---FTP Server---");
        try {
            while (true) {
                ServerSocket serverSocket = new ServerSocket(PORT);
                System.out.println("Listening on port " + PORT);
                Socket clienSocket = serverSocket.accept();
                System.out.println("Client connected");
                inputStream = new DataInputStream(clienSocket.getInputStream());
                outputStream = new DataOutputStream(clienSocket.getOutputStream());
                boolean loggedIn = handleAuth();
                outputStream.writeBoolean(loggedIn);
                if (loggedIn) {
                    System.out.println("User logged in successfully");
                    receiveFile();
                } else {
                    System.out.println("Error logging in user");
                }
                inputStream.close();
                outputStream.close();
                clienSocket.close();
                serverSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean handleAuth() {
        try {
            String creds = inputStream.readUTF();
            String[] credentials = creds.split(":");
            if (credentials.length > 0) {
                authenticator = new FTPAuthentication();
                if (credentials[0].equals("1")) {
                    return authenticator.signIn(credentials[1], credentials[2]);
                } else {
                    return authenticator.signUp(credentials[1], credentials[2]);
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void receiveFile() {
        try {
            long fileSize = inputStream.readLong();
            if (fileSize == 0) {
                System.out.println("File not found. Aborting transfer");
                return;
            }
            String outputFile = inputStream.readUTF();
            File file = new File(outputFile);
            fileOutputStream = new FileOutputStream(file);
            int bytes = 0;
            byte[] buffer = new byte[4 * 1024];
            while (fileSize > 0 && (bytes = inputStream.read(buffer, 0,
                    (int) Math.min(buffer.length, fileSize))) != -1) {
                byte[] decryptedData = decrypt(buffer);
                fileOutputStream.write(decryptedData, 0, bytes);
                fileSize -= bytes;
                System.out.println("Writing decrypted data to file");
            }
            System.out.println("File uploaded to server");

            fileInputStream = new FileInputStream(file);
            boolean isCorrect = checkIntegrity();
            if (isCorrect) {
                System.out.println("Transfered file passed integrity check");
            } else {
                System.out.println("Transfered file failed integrity check");
            }
            fileOutputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Decrypt using XOR operation
    private static byte[] decrypt(byte[] buffer) {
        int i = 0;
        byte[] decryptedData = new byte[4 * 1024];
        i = 0;
        for (byte b : buffer) {
            decryptedData[i] = (byte) (b ^ KEY);
            i++;
        }
        return decryptedData;
    }

    private static boolean checkIntegrity() {
        try {
            String clientChecksum = inputStream.readUTF();
            String serverChecksum = getChecksum();
            return clientChecksum.equals(serverChecksum);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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

// Class handles authentication
class FTPAuthentication {
    private static FileWriter dbWriter = null;
    private static FileReader dbReader = null;
    private static HashMap<String, String> creds = null;

    boolean signUp(String username, String password) {
        try {
            dbWriter = new FileWriter("creds.txt", true);
            BufferedWriter writer = new BufferedWriter(dbWriter);
            writer.write(username);
            writer.write(":");
            writer.write(password);
            writer.newLine();
            writer.close();
            dbWriter.close();
            System.out.println("User has signed up successfully");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    boolean signIn(String username, String password) throws IOException {
        fetchCredentials();
        String fetchedPassword = creds.get(username);
        if (fetchedPassword != null && fetchedPassword.equals(password)) {
            System.out.println("User has been logged in");
            return true;
        } else {
            System.out.println("Invaild credentials");
            return false;
        }
    }

    private void fetchCredentials() throws IOException {
        try {
            dbReader = new FileReader("creds.txt");
            BufferedReader reader = new BufferedReader(dbReader);
            creds = new HashMap<String, String>();
            String cred;
            while ((cred = reader.readLine()) != null) {
                String[] values = cred.split(":");
                creds.put(values[0], values[1]);
            }
        } catch (FileNotFoundException e) {
            File credsFile = new File("creds.txt");
            credsFile.createNewFile();
            fetchCredentials();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
