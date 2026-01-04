package network;// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import java.io.*;
import java.nio.charset.StandardCharsets;

import common.ChatIF;
import ocsf.server.*;

/**
 * This class overrides some of the methods in the abstract
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 * @version July 2000
 */
public class EchoServer extends AbstractServer {
    //Class variables *************************************************

    /**
     * The default port to listen on.
     */
    final public static int DEFAULT_PORT = 5555;
    
    //Instance variables **********************************************
    
    /**
     * The interface type variable. It allows the implementation of
     * the display method in the server.
     */
    ChatIF serverUI;

    //Constructors ****************************************************

    /**
     * Constructs an instance of the echo server.
     *
     * @param port The port number to connect on.
     */
    public EchoServer(int port) {
        super(port);
        this.serverUI = null;
    }
    
    /**
     * Constructs an instance of the echo server.
     *
     * @param port The port number to connect on.
     * @param serverUI The interface type variable.
     */
    public EchoServer(int port, ChatIF serverUI) {
        super(port);
        this.serverUI = serverUI;
    }


    //Instance methods ************************************************

    /**
     * This method handles any messages received from the client.
     *
     * @param msg    The message received from the client.
     * @param client The connection from which the message originated.
     */
    public void handleMessageFromClient
    (Object msg, ConnectionToClient client) {
        System.out.println("Message received: " + msg + " from " + client);
        this.sendToAllClients(msg.toString().getBytes(StandardCharsets.UTF_8));
    }


    /**
     * This method handles all data coming from the UI
     *
     * @param message The message from the UI.
     */
    public void handleMessageFromServerUI(String message) {
        if (message.startsWith("#")) {
            String[] command = message.split(" ");

            switch (command[0]) {
                case "#quit":
                    if (serverUI != null) {
                        serverUI.display("Server shutting down...");
                    }
                    try {
                        close();
                    } catch (IOException e) {
                        if (serverUI != null) {
                            serverUI.display("Error closing server: " + e.getMessage());
                        }
                    }
                    System.exit(0);
                    break;

                case "#stop":
                    if (isListening()) {
                        stopListening();
                        if (serverUI != null) {
                            serverUI.display("Server stopped listening for new clients.");
                        }
                    } else {
                        if (serverUI != null) {
                            serverUI.display("Server is not currently listening.");
                        }
                    }
                    break;

                case "#close":
                    if (isListening() || getNumberOfClients() > 0) {
                        try {
                            close();
                            if (serverUI != null) {
                                serverUI.display("Server closed. All clients disconnected.");
                            }
                        } catch (IOException e) {
                            if (serverUI != null) {
                                serverUI.display("Error closing server: " + e.getMessage());
                            }
                        }
                    } else {
                        if (serverUI != null) {
                            serverUI.display("Server is already closed.");
                        }
                    }
                    break;

                case "#setport":
                    if (!isListening() && getNumberOfClients() == 0) {
                        if (command.length > 1) {
                            try {
                                int port = Integer.parseInt(command[1]);
                                setPort(port);
                                if (serverUI != null) {
                                    serverUI.display("Port set to: " + getPort());
                                }
                            } catch (NumberFormatException e) {
                                if (serverUI != null) {
                                    serverUI.display("Error: Port must be an integer.");
                                }
                            }
                        } else {
                            if (serverUI != null) {
                                serverUI.display("Error: Please specify a port number.");
                            }
                        }
                    } else {
                        if (serverUI != null) {
                            serverUI.display("Error: Cannot change port while server is listening or has clients connected.");
                        }
                    }
                    break;

                case "#start":
                    if (!isListening()) {
                        try {
                            listen();
                            if (serverUI != null) {
                                serverUI.display("Server started listening for clients.");
                            }
                        } catch (IOException e) {
                            if (serverUI != null) {
                                serverUI.display("Error: Cannot start server - " + e.getMessage());
                            }
                        }
                    } else {
                        if (serverUI != null) {
                            serverUI.display("Server is already listening for clients.");
                        }
                    }
                    break;

                case "#getport":
                    if (serverUI != null) {
                        serverUI.display("Current port: " + getPort());
                    }
                    break;

                default:
                    if (serverUI != null) {
                        serverUI.display("Unknown command: " + command[0]);
                    }
            }
        } else {
            String serverMessage = "SERVER MSG> " + message;
            if (serverUI != null) {
                serverUI.display(serverMessage);
            }
            this.sendToAllClients(serverMessage);
        }
    }

    /**
     * This method overrides the one in the superclass.  Called
     * when the server starts listening for connections.
     */
    protected void serverStarted() {
        System.out.println
                ("Server listening for connections on port " + getPort());
    }

    /**
     * This method overrides the one in the superclass.  Called
     * when the server stops listening for connections.
     */
    protected void serverStopped() {
        System.out.println
                ("Server has stopped listening for connections.");
    }

    synchronized protected void clientException(
            ConnectionToClient client, Throwable exception) {
        System.out.println("Client exception: " + client);
        exception.printStackTrace();
    }

    protected void clientConnected(ConnectionToClient client) {
        System.out.println("Client connected: " + client);
    }

    synchronized protected void clientDisconnected(
            ConnectionToClient client) {
        System.out.println("Client disconected: " + client);
    }
}
//End of network.EchoServer class