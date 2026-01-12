package presenter;

import model.common.Response;
import model.common.Request;
import ocsf.client.AbstractClient;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

public class HospitalClient extends AbstractClient {

    private Consumer<Response> onResponseReceived;
    private final Deque<Consumer<Response>> pendingCallbacks = new ArrayDeque<>();

    public HospitalClient(String host, int port) throws IOException {
        super(host, port);
        openConnection();
        System.out.println("Client conectat la " + host + ":" + port);
    }

    public void setOnResponseReceived(Consumer<Response> callback) {
        this.onResponseReceived = callback;
    }

    @Override
    protected void handleMessageFromServer(Object msg) {
        if (msg instanceof Response response) {
            Consumer<Response> callback = null;
            synchronized (pendingCallbacks) {
                if (!pendingCallbacks.isEmpty()) {
                    callback = pendingCallbacks.pollFirst();
                }
            }
            if (callback != null) {
                callback.accept(response);
            } else if (onResponseReceived != null) {
                onResponseReceived.accept(response);
            } else {
                System.out.println("Raspuns primit fara handler: " + response);
            }
        } else {
            System.out.println("Mesaj necunoscut primit: " + msg);
        }
    }

    public void sendRequest(Request request) {
        sendRequest(request, null);
    }

    public void sendRequest(Request request, Consumer<Response> callback) {
        if (callback != null) {
            synchronized (pendingCallbacks) {
                pendingCallbacks.addLast(callback);
            }
        }
        try {
            sendToServer(request);
        } catch (IOException e) {
            if (callback != null) {
                synchronized (pendingCallbacks) {
                    pendingCallbacks.removeLastOccurrence(callback);
                }
            }
            System.err.println("Eroare trimitere cerere: " + e.getMessage());
        }
    }
}
