package com.muc;


import java.util.Scanner;

/**
 * Jhoan Mateo Franco Vargas
 */
//Esta clase sirve para inciar el Servidor ,
public class ServerMain {
    public static void main(String[] args) {

        int port = 8818;
        //Objeto de tipo servidor se le envia el puerto .
        Server server = new Server(port);
        //Incia el servidor.
        server.start();
    }
}