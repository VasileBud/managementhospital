package com.hospital_management.client.network;

import common.Response;
import common.Request;
import ocsf.client.AbstractClient;

import java.io.IOException;
import java.util.function.Consumer;

public class HospitalClient extends AbstractClient {

    private Consumer<Response> onResponseReceived;

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
        if (msg instanceof Response) {
            if (onResponseReceived != null) {
                onResponseReceived.accept((Response) msg);
            }
        } else {
            System.out.println("Mesaj necunoscut primit: " + msg);
        }
    }

    public void sendRequest(Request request) {
        try {
            sendToServer(request);
        } catch (IOException e) {
            System.err.println("Eroare trimitere cerere: " + e.getMessage());
        }
    }
}