
import common.ChatIF;

import java.io.*;


public class ServerConsole implements ChatIF
{

    final public static int DEFAULT_PORT = 5555;
    EchoServer server;

    public ServerConsole(int port)
    {
        try
        {
            server = new EchoServer(port, this);    
            server.listen();
        }
        catch(Exception exception)
        {
            System.out.println("Error: Can't setup server!"
                    + " Terminating server.");
            System.exit(1);
        }
    }

    public void accept()
    {
        try
        {
            BufferedReader fromConsole =
                    new BufferedReader(new InputStreamReader(System.in));
            String message;

            while (true)
            {
                message = fromConsole.readLine();
                server.handleMessageFromServerUI(message);
            }
        }
        catch (Exception ex)
        {
            System.out.println
                    ("Unexpected error while reading from console!");
        }
    }

    public void display(String message)
    {
        System.out.println("> " + message);
    }

    public static void main(String[] args)
    {
        int port;

        try{
            port = Integer.parseInt(args[0]) ;
        } catch(ArrayIndexOutOfBoundsException e)
        {
            port = DEFAULT_PORT;
        }

        ServerConsole serverConsole = new ServerConsole(port);
        serverConsole.accept();
    }
}