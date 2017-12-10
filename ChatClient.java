package epn;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatClient {

	BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(8, 40);
    
    //Construye el cliente diseñando la GUI y registrando un oyente con el campo de texto de
    //manera que presionar Volver en el oyente envíe los contenidos del campo de texto al servidor. 
    //Sin embargo, tenga en cuenta que el campo de texto inicialmente NO es editable, y solo se 
    //puede editar DESPUÉS de que el cliente reciba el mensaje NAMEACCEPTED del servidor.
    
    public ChatClient() {

        // Layout GUI
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, "North");
        frame.getContentPane().add(new JScrollPane(messageArea), "Center");
        frame.pack();

        // Add Listeners
        textField.addActionListener(new ActionListener() {
        	
        	/*Responde a presionar la tecla Intro en el campo de texto enviando el contenido 
        	 * del campo de texto al servidor. Luego borre el área de texto en preparación para el
        	 *  próximo mensaje.
        	 */
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });
    }
    
    /**
     * Solicitar y devolver la dirección del servidor.
     */
    private String getServerAddress() {
        return JOptionPane.showInputDialog(
            frame,
            "Enter IP Address of the Server:",
            "Welcome to the Chatter",
            JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Solicite y devuelva el nombre de pantalla deseado.
     */
    private String getName() {
        return JOptionPane.showInputDialog(
            frame,
            "Choose a screen name:",
            "Screen name selection",
            JOptionPane.PLAIN_MESSAGE);
    }
    
    /**
     *  Se conecta al servidor y luego ingresa al ciclo de procesamiento.
     */
    private void run() throws IOException {

        // Make connection and initialize streams
        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 9001);
        in = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        //Procesa todos los mensajes del servidor de acuerdo al protocolo
        
        
        
        /**En esta parte se realiza el broadcasting de los datos al servidor
        * */
        while (true) {
            String line = in.readLine();         
            if (line.startsWith("SUBMITNAME")) {
                out.println(getName());     //Si la cadena empieza con SUBMITNAME,obtiene un dato nombre
            } else if (line.startsWith("NAMEACCEPTED")) {
                textField.setEditable(true); //si la cadena empieza con NAMEACCEPTED establece un campo detexto
            } else if (line.startsWith("MESSAGE")) {
                messageArea.append(line.substring(8) + "\n");  //Si la cadena empieza con MESSAGE, se espera la entrada de un mensaje
            }
        }
    }
    
    /**
     * Runs the client as an application with a closeable frame.
     */
    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
    
}