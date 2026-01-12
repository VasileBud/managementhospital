import controller.HospitalController;
import view.ServerConsole;

public class ServerMain {
    public static void main(String[] args)
    {
        int port;

        try{
            port = Integer.parseInt(args[0]) ;
        } catch(ArrayIndexOutOfBoundsException e)
        {
            port = ServerConsole.DEFAULT_PORT;
        }
        HospitalController hospitalController = new HospitalController();
        ServerConsole serverConsole = new ServerConsole(port, hospitalController);
        serverConsole.accept();
    }
}
