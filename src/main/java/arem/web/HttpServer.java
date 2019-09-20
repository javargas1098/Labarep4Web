package arem.web;

import java.net.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.*;
import java.lang.reflect.InvocationTargetException;


/**
 * La clase HttpServer se comporta como un servidor web, el cual recibe
 * peticiones por medio del protocolo http y responde con recursos html,png y
 * jpg.
 * 
 * @author Javier Vargas
 */
public class HttpServer {

    public static void main(String[] args) {
        int i = 0;
        ExecutorService es = Executors.newFixedThreadPool(10);
        while (true) {
            i++;
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(getPort());
            } catch (IOException e) {
                System.err.println("Could not listen on port: " + getPort());
                System.exit(1);
            }
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ... " + i);
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            Request ar = new Request(clientSocket);
            es.execute(ar);
            try {
                serverSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static int getPort() {
        if (System.getenv("PORT") != null) {
            return Integer.parseInt(System.getenv("PORT"));
        }
        return 4567; //returns default port if heroku-port isn't set (i.e.on localhost)
    }

}