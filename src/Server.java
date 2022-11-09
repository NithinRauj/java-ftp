import java.io.*;
import java.util.HashMap;
import java.net.Socket;
import java.net.ServerSocket;

public class Server {
    private static final int PORT = 5000;
    private static DataInputStream inputStream = null;
    private static DataOutputStream outputStream = null;
    private static FTPAuthentication authenticator = null;

    public static void main(String[] args) {
        System.out.println("---FTP Server---");
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Listening on port 5000");
            Socket clienSocket = serverSocket.accept();
            inputStream = new DataInputStream(clienSocket.getInputStream());
            outputStream = new DataOutputStream(clienSocket.getOutputStream());
            boolean loggedIn = handleAuth();
            outputStream.writeBoolean(loggedIn);
            if (loggedIn) {
                receiveFile();
            } else {
                System.out.println("Error logging in user");
            }
            inputStream.close();
            outputStream.close();
            clienSocket.close();
            serverSocket.close();

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
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            int bytes = 0;
            byte[] buffer = new byte[4 * 1024];
            while (fileSize > 0
                    && (bytes = inputStream.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                fileOutputStream.write(buffer, 0, bytes);
                fileSize -= bytes;
                System.out.println("Writing file");
            }
            System.out.println("File uploaded to server");
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

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
            System.out.println("User has signed up and logged in");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    boolean signIn(String username, String password) {
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

    private void fetchCredentials() {
        try {
            dbReader = new FileReader("creds.txt");
            BufferedReader reader = new BufferedReader(dbReader);
            creds = new HashMap<String, String>();
            String cred;
            while ((cred = reader.readLine()) != null) {
                String[] values = cred.split(":");
                creds.put(values[0], values[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
