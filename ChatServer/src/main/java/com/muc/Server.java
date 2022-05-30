package com.muc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jim on 4/19/17.
 */
//Clase responsable de crear los hilos para generar multiples clientes que puedan conectarse a el srvidor, tambien incia el servidor.
public class Server extends Thread {
    private final int serverPort;

    //guarda las la lista de el servidor,
    private ArrayList<ServerWorker> workerList = new ArrayList<>();

    //Constructor que recibe el puerto.
    public Server(int serverPort) {
        this.serverPort = serverPort;
    }

    //Obtiene la lista de el servidor.
    public List<ServerWorker> getWorkerList() {
        return workerList;
    }

    //Incia el servidor
    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while(true) {
                System.out.println("Aceptando la conexión de el cliente...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Aceptada la conexión  " + clientSocket);
                ServerWorker worker = new ServerWorker(this, clientSocket);
                workerList.add(worker);
                worker.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Lo elimina de la lista de conexiones.
    public void removeWorker(ServerWorker serverWorker) {
        workerList.remove(serverWorker);
    }
}


