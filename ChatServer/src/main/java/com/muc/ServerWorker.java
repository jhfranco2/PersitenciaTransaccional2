package com.muc;


import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Grupo 7.
 */
//Clase encargada de los controladores de conexion
public class ServerWorker extends Thread {

    //Variable para enviar la información entre cliente y servidor.
    private final Socket clientSocket;
    //Variable que define el servidor.
    private final Server server;
    //Variable para iniciar sesión.
    private String login = null;

    //Variable para conectarse a el cliente,y dependiendo de la información ingresada manda una respuesta desde el cliente.
    private OutputStream outputStream;

   //Contiene un conjunto de objetos, pero de una manera que le permite determinar fácil y rápidamente si un objeto ya está en el conjunto o no.
    private HashSet<String> topicSet = new HashSet<>();

    //Constructor que recibe un objeto Server y un socket, permite comunicarse con la clase server.
    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    //Sirve para correr
    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    //Sirve para indicarle que hacer en los comandos de "login","join","logoff","quit","leave","msg"
    private void handleClientSocket() throws IOException, InterruptedException {
        //Recibe la informacion del cliente a el servidor
        InputStream inputStream = clientSocket.getInputStream();
        //Envia la informacion del chat.
        this.outputStream = clientSocket.getOutputStream();
        //Sirve para leer texto
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        //Este bucle lee lineas del cliente y luego estamos entregando diferentes lineas de codigo dependiendo de lo que se quiere hacer.
        while ( (line = reader.readLine()) != null) {
            //Guarda mediante tokens los chats de el o los clientes.
            String[] tokens = StringUtils.split(line);
            //Si no hay mensajes en la consola
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];
                //Comando para salirse de el servidor
                if ("logoff".equals(cmd) || "quit".equalsIgnoreCase(cmd)) {
                    handleLogoff();
                    break;
                    //Comando para conectarse.
                } else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(outputStream, tokens);
                    //Comando para enviar mensajes a otro usuario.
                } else if("msg".equalsIgnoreCase(cmd)){
                    String[] tokensMsg= StringUtils.split(line,null, 3);
                    handleMessage(tokensMsg);
                    //Sirve para crear temas de conversación
                } else if ("join".equalsIgnoreCase(cmd)) {
                    handleJoin(tokens);
                    //Sirve para abandonar el tema de conversación.
                } else if ("leave".equalsIgnoreCase(cmd)) {
                    heandleLeave(tokens);
                    //Si no es ninguno de los anteriores comandos es desconocido y lo muestra en el cliente.
                } else {

                    String msg = "desconocido " + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }

        clientSocket.close();
    }

    //Remueve el usuario de la conexión,recibe un Arreglo de String.
    private void heandleLeave(String[] tokens) {
        if(tokens.length >1){
            String topic = tokens[1];
            topicSet.remove(topic);


        }
    }

    //Mira si eres miembro de el tema, recibe un String y devuelve si es parte de el tema.
    public boolean isMenberOfTopic(String topic){

        return topicSet.contains(topic);
    }
    //Para hacer grupos

    //Agrega un tema de conversación, recibe un arreglo de String.
    private void handleJoin(String[] tokens) {
        if(tokens.length >1){
            String topic = tokens[1];
            topicSet.add(topic);


        }
    }

    //formato : "msg" "login" body...
    //formato: "msg" "topic" body
    //Recibe los mensajes mediante un arreglo de String y los envia

    private void handleMessage(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String body = tokens[2];

        boolean isTopic = sendTo.charAt(0)== '#';

        List<ServerWorker> workerList = server.getWorkerList();
        for(ServerWorker worker : workerList) {
            //Si es  un tema lo envia a los miembros de ese tema.
            if (isTopic) {
                if(worker.isMenberOfTopic(sendTo)){

                    String outMsg = "msg " + sendTo+":"+login + " " + body + "\n";
                    worker.send(outMsg);
                }
            } else {
                //Si se es parte de un tema no se envia.
                if (sendTo.equalsIgnoreCase(worker.getLogin())) {
                    String outMsg = "msg " + login + " " + body + "\n";
                    worker.send(outMsg);
                }
            }
        }

    }

    //Sirve para cuando se quiere desloguearse
    private void handleLogoff() throws IOException {
        //Lo elimina de la lista de conexiones
        server.removeWorker(this);
        //Obtiene la lista actual de los clientes logueados.
        List<ServerWorker> workerList = server.getWorkerList();

        // envia a otros usuarios en línea el estado actual del usuario
        String onlineMsg = "offline " + login + "\n";
        //Recorre los clientes del  server
        for(ServerWorker worker : workerList) {
            //Envia el mensaje de el usuario actual.
            if (!login.equals(worker.getLogin())) {
                worker.send(onlineMsg);
            }
        }
        clientSocket.close();
    }
//retorna el login.
    public String getLogin() {

        return login;
    }

    //Metodo para iniciar sesion, recibe un OutputStream y un arreglo de strings y devuelve si el usuario puede loguearse y lo guarda mediante hilos en el servidor.
    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 3) {
            String login = tokens[1];
            String password = tokens[2];

            //Si alguno de los  usuarios tiene perrmiso lo deja ingresar a el servidor.
            if ((login.equals("poli01") && password.equals("poli01")) || (login.equals("poli02") && password.equals("poli02")) || (login.equals("poli03") && password.equals("poli03")) || (login.equals("poli04") && password.equals("poli04"))) {
                String msg = "inicio correcto\n";
                //Muestra mensaje en el cliente.
                outputStream.write(msg.getBytes());
                this.login = login;
                //Mensaje si el usuario se loguea correctamente
                System.out.println("Usuario logueado satisfactoriamente: " + login);
                //Obtiene la lista de el servidor.
                List<ServerWorker> workerList = server.getWorkerList();

                // Envia el usuario actual a todos los usuarios conectados.
                for(ServerWorker worker : workerList) {
                    if (worker.getLogin() != null) {
                        //Si no hay usuarios repetidos indica que esta en linea.
                        if (!login.equals(worker.getLogin())) {
                            String msg2 = "En linea" + worker.getLogin() + "\n";
                            send(msg2);
                        }
                    }
                }

                // envia el mensaje por si otro usuario esta en linea
                String onlineMsg = "En linea " + login + "\n";
                for(ServerWorker worker : workerList) {
                    if (!login.equals(worker.getLogin())) {
                        worker.send(onlineMsg);
                    }
                }
            } else {
                String msg = "Error al logearse\n";
                outputStream.write(msg.getBytes());
            }
        }
    }

    //Envia mensajes a el cliente,recibe un String.
    private void send(String msg) throws IOException {
        if (login != null) {
            outputStream.write(msg.getBytes());
        }
    }
}
