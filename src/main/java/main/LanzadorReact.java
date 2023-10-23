package main;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class LanzadorReact {
        private final static String htemele = "web\\pito.html";
        public static void main(String[] args) throws IOException {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost",8080),0);
            ExecutorService ex = Executors.newCachedThreadPool();
            server.setExecutor(ex);

            server.createContext("/projektAEMEKU", new HTMLReturner(htemele));
            server.createContext("/ejemplo", new HTMLReturner("web\\pito2.html"));
            server.start();

            System.out.println("Pulsa enter para parar el servicio");
            new Scanner(System.in).nextLine();
            System.out.println("Parando...");
            server.stop(0);
            ex.shutdown();
        }
}

class HTMLReturner implements HttpHandler {
    private final String htemelePath;
    public HTMLReturner(String htemele) { htemelePath = htemele; }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        BufferedReader fInput = new BufferedReader(new FileReader(htemelePath));
        StringBuilder bob = new StringBuilder();
        String str;
        while ((str = fInput.readLine())!=null) {
            bob.append(str);
        }
        fInput.close();
        exchange.sendResponseHeaders(200, bob.length());
        BufferedWriter pito = new BufferedWriter( new OutputStreamWriter(exchange.getResponseBody()));
        pito.write(bob.toString());
        pito.flush();
        pito.close();
        System.out.println(exchange.getRequestMethod());
        exchange.getResponseHeaders().forEach((a,b) -> {
            System.out.println(a + " -- " + b);
        });
        exchange.close();
    }
}