// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package client;

import ocsf.client.*;
import common.*;

import java.io.*;

/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the client.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;
 * @author Fran&ccedil;ois B&eacute;langer
 * @version July 2000
 */
public class ChatClient extends AbstractClient {
    //Instance variables **********************************************

    /**
     * The interface type variable.  It allows the implementation of
     * the display method in the client.
     */
    ChatIF clientUI;


    //Constructors ****************************************************

    /**
     * Constructs an instance of the chat client.
     *
     * @param host     The server to connect to.
     * @param port     The port number to connect on.
     * @param clientUI The interface type variable.
     */

    public ChatClient(String host, int port, ChatIF clientUI)
            throws IOException {
        super(host, port); //Call the superclass constructor
        this.clientUI = clientUI;
        openConnection();
    }


    //Instance methods ************************************************

    /**
     * This method handles all data that comes in from the server.
     *
     * @param msg The message from the server.
     */
    public void handleMessageFromServer(Object msg) {
        clientUI.display(msg.toString());
    }

    /**
     * This method handles all data coming from the UI
     *
     * @param message The message from the UI.
     */
    public void handleMessageFromClientUI(String message) {
        if (message.startsWith("#")) {
            String[] command = message.split(" ");

            switch (command[0]) {
                case "#quit":
                    quit();
                    break;

                case "#logoff":
                    try {
                        closeConnection();
                    } catch (IOException e) {
                    }
                    break;

                case "#login":
                    if (!isConnected()) {
                        try {
                            openConnection();
                            clientUI.display("You are now logged in.");
                        } catch (IOException e) {
                            clientUI.display("Login failed: " + e.getMessage());
                        }
                    } else {
                        clientUI.display("Already connected.");
                    }
                    break;

                case "#sethost":
                    if (isConnected()) {
                        clientUI.display("Cannot change host while connected.");
                    } else {
                        setHost(command[1]);
                        clientUI.display("Host set to: " + getHost());
                    }
                    break;

                case "#setport":
                    if (isConnected()) {
                        clientUI.display("Cannot change port while connected.");
                    } else {
                        try {
                            int port = Integer.parseInt(command[1]);
                            setPort(port);
                            clientUI.display("Port set to: " + getPort());
                        } catch (Exception e) {
                            clientUI.display("Port must be an integer.");
                        }
                    }
                    break;

                case "#gethost":
                    clientUI.display("Current host: " + getHost());
                    break;

                case "#getport":
                    clientUI.display("Current port: " + getPort());
                    break;

                default:
                    clientUI.display("Unknown command: " + command);
            }
        } else {
            try {
                sendToServer(message);
            } catch (IOException e) {
                clientUI.display("Could not send message to server. Terminating client.");
                quit();
            }
        }
    }


    /**
     * This method terminates the client.
     */
    public void quit() {
        try {
            closeConnection();
        } catch (IOException e) {
        }
        System.exit(0);
    }


    protected void connectionClosed() {
        clientUI.display("Connection closed.");
    }

    protected void connectionException(Exception exception) {
        clientUI.display("Server has shut down. Terminating client.");
        quit();
    }
}
//End of ChatClient class