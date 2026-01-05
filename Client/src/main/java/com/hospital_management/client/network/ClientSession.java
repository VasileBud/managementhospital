package com.hospital_management.client.network;

import shared.dto.UserDTO;

public class ClientSession {
    private static ClientSession instance;
    private HospitalClient client;
    private UserDTO loggedUser;

    private ClientSession() {
        try {
            // Ne conectăm la localhost, port 5555 (unde rulează ServerConsole)
            client = new HospitalClient("localhost", 5555);
        } catch (Exception e) {
            System.err.println("Nu ma pot conecta la server! Verifică dacă ServerConsole rulează.");
        }
    }

    public static synchronized ClientSession getInstance() {
        if (instance == null) {
            instance = new ClientSession();
        }
        return instance;
    }

    public HospitalClient getClient() {
        return client;
    }

    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    public synchronized boolean ensureConnected() {
        if (isConnected()) {
            return true;
        }
        try {
            client = new HospitalClient("localhost", 5555);
            return true;
        } catch (Exception e) {
            System.err.println("Nu ma pot conecta la server! Verifica daca ServerConsole ruleaza.");
            return false;
        }
    }

    public UserDTO getLoggedUser() { return loggedUser; }
    public void setLoggedUser(UserDTO loggedUser) { this.loggedUser = loggedUser; }
}
