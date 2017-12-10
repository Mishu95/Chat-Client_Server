package epn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
	
public class ChatServer {

	/**
     * The port that the server listens on.
     */
    private static final int PORT = 9001;

    /**
     * The set of all names of clients in the chat room.  Maintained
     * so that we can check that new clients are not registering name
     * already in use.
     */
    private static HashSet<String> names = new HashSet<String>();
    
    /**
     * The set of all the print writers for all the clients.  This
     * set is kept so we can easily broadcast messages.
     */
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
    
    /**
     * The appplication main method, which just listens on a port and
     * spawns handler threads.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }
    
    /**
     * A handler thread class.  Handlers are spawned from the listening
     * loop and are responsible for a dealing with a single client
     * and broadcasting its messages.
      */
    
     // Este conjunto se mantiene para que podamos transmitir mensajes fácilmente.
     
    private static class Handler extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        /**
           Construye un hilo manejador, guardando el zócalo.
         * Todo el trabajo interesante se realiza en el método de ejecución
         */
        public Handler(Socket socket) {
            this.socket = socket;
        }
        /**
        Presta servicios al cliente de este subproceso solicitando repetidamente un nombre de pantalla 
        hasta que se haya enviado uno único, luego confirma el nombre y registra el flujo de salida para  
        el cliente en un conjunto global, luego obtiene entradas y las emite repetidamente
         */
        public void run() {
            try {

                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Request a name from this client.  Keep requesting until
                // a name is submitted that is not already used.  Note that
                // checking for the existence of a name and adding the name
                // must be done while locking the set of names.
              
                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {
                        if (!names.contains(name)) {
                            names.add(name);
                            break;
                        }
                    }
                }

                // Now that a successful name has been chosen, add the
                // socket's print writer to the set of all writers so
                // this client can receive broadcast messages.
                out.println("NAMEACCEPTED");
                writers.add(out);

                // Accept messages from this client and broadcast them.
                // Ignore other clients that cannot be broadcasted to.
                
                
                /** Esta es la parte en que se realiza el Broadcasting, porque el servidor espera una entrada de datos
                * para despues enviarla al arreglo de los mensajes e imprimirlo */
                while (true) {
                    String input = in.readLine(); //Lee los datos que el cliente mande
                    if (input == null) { //sale del bucle si el cliente envia una cadena vacia
                        return;
                    }
                    for (PrintWriter writer : writers) {//obtiene un mensaje del arreglo de mensajes
                        writer.println("MESSAGE " + name + ": " + input); //Envia el mensaje enviado con el nombre del Cliente
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                // This client is going down!  Remove its name and its print
                // writer from the sets, and close its socket.
                if (name != null) {
                    names.remove(name);
                }
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}