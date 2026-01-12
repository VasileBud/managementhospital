package view;

import controller.HospitalController;
import controller.HospitalServer;
import model.common.ChatIF;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;

public class ServerConsole implements ChatIF {

    public static final int DEFAULT_PORT = 5555;

    private final HospitalController hospitalController;
    private HospitalServer server;

    public ServerConsole(int port, HospitalController hospitalController) {
        this.hospitalController = Objects.requireNonNull(hospitalController, "hospitalController");

        try {
            this.server = new HospitalServer(port, this, hospitalController);
            server.listen();
        } catch (Exception exception) {
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

    public void display(String message) {
        System.out.println("> " + message);
    }
}
