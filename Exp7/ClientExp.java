// Import I/O streams for sending and receiving binary/primitive data over sockets
import java.io.*;
import java.net.*;
import java.util.*;

// Main class representing the chat client application
public class ClientExp {
    // Main entry point; throws Exception to bypass explicit try-catch blocks during setup
    public static void main(String[] args) throws Exception {
        // Connect to the server listening on the local machine ("localhost") at port 5000
        Socket socket = new Socket("localhost", 5000);

        // Input stream to read incoming messages sent by the server
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        // Output stream to write and send messages to the server
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

        // Scanner instance to capture text input from the user's terminal/console
        Scanner sc = new Scanner(System.in);

        // Prompt the user to set a display name for the chat session
        System.out.print("Enter your name: ");
        // Read and store the user's name from standard input
        String name = sc.nextLine();

        // Create a background thread via a lambda to listen for incoming messages asynchronously
        Thread receive = new Thread(() -> {
            try {
                // Keep listening continuously as long as the connection remains active
                while (true) {
                    // Blocks until a UTF string is received from the server, then prints it to the console
                    System.out.println(dis.readUTF());
                }
            } catch (Exception e) {
                // Silently handle socket disconnection or stream closure when the connection drops
            }
        });
        // Start the background listening thread so message reception does not block console input
        receive.start();

        // Main thread loop: continuously read input from the console and forward it to the server
        while (true) {
            // Wait for the user to type a message and press Enter
            String msg = sc.nextLine();
            // Prefix the message with the user's name and send it over the socket to the server
            dos.writeUTF(name + ":" + msg);
        }
    }
}