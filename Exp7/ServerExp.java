// Import basic I/O classes for handling streams (DataInputStream, DataOutputStream)
import java.io.*;
import java.net.*;
import java.util.*;

// Main class representing the multi-client chat server
public class ServerExp {
    // Thread-safe dynamic list (Vector) to keep track of all currently connected client handlers
    static Vector<ClientHandler> clients = new Vector<>();

    // Entry point of the server application; throws Exception to simplify error handling on startup
    public static void main(String[] args) throws Exception {
        // Bind the server to port 5000 and start listening for incoming client connection requests
        ServerSocket ss = new ServerSocket(5000);
        // Print a confirmation message to the console once the server is listening
        System.out.println("Server Started...");

        // Infinite loop to continuously accept incoming connections from clients
        while (true) {
            // Blocks execution until a client connects; returns a dedicated Socket for communication
            Socket s = ss.accept();
            // Notify the server console that a new client has successfully connected
            System.out.println("New Client Connected...");

            // Create an input stream to receive binary/primitive data (e.g., UTF strings) from this client
            DataInputStream dis = new DataInputStream(s.getInputStream());
            // Create an output stream to send binary/primitive data (e.g., UTF strings) to this client
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

            // Instantiate a runnable task dedicated to handling this specific client's messages
            ClientHandler ch = new ClientHandler(s, dis, dos);
            // Add the new handler to the shared active clients list
            clients.add(ch);

            // Wrap the ClientHandler instance in a new Thread to handle this client asynchronously
            Thread t = new Thread(ch);
            // Start the thread, which invokes ch.run() in the background without blocking main loop
            t.start();
        }
    }
}

// Runnable task that manages communication with a single connected client in its own thread
class ClientHandler implements Runnable {
    // Reference to the client's network socket
    Socket s;
    // Input stream to read incoming messages sent by this client
    DataInputStream dis;
    // Output stream to send messages directly to this client
    DataOutputStream dos;

    // Constructor to initialize the handler with the client's socket and active I/O streams
    ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos) {
        // Assign the client's socket to the instance variable
        this.s = s;
        // Assign the input stream to the instance variable
        this.dis = dis;
        // Assign the output stream to the instance variable
        this.dos = dos;
    }

    // Execution logic running inside the dedicated client thread
    public void run() {
        try {
            // Loop indefinitely to receive and broadcast messages as long as the client is connected
            while (true) {
                // Blocks until a UTF-8 string is received from this client
                String msg = dis.readUTF();

                // Broadcast the received message to all other active clients
                for (ClientHandler Client : Server.clients) {
                    // Check to avoid echoing the message back to the sender
                    if (Client != this) {
                        // Forward the message to the other client's output stream
                        Client.dos.writeUTF(msg);
                    }
                }
            }
        } catch (Exception ex) {
            // Catches I/O errors or socket disconnects (e.g., client abruptly closes connection)
            try {
                // Remove the disconnected client from the active client list
                Server.clients.remove(this);
                // Close the underlying socket to release system network resources
                s.close();
            } catch (Exception e) {
                // Silently ignore any exception that occurs during socket cleanup
            }
        }
    }
}