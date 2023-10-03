package main;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Pruebotes implements HttpHandler {
    public void handle(HttpExchange t) throws IOException {

    }

    public static void main(String[] args) throws IOException {

        InetSocketAddress cd = new InetSocketAddress(8080);
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost",8080),0);

        server.createContext("/index", t -> {
            InputStream is = t.getRequestBody();

            InputStreamReader isReader = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(isReader);
            StringBuffer sb = new StringBuffer();
            String str;
            while((str = reader.readLine())!= null){
                sb.append(str);
            }

            System.out.println(sb);

            String response = "pinga du negro moreno";

            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
            server.stop(0);
        });

        server.setExecutor(null); // creates a default executor
        server.start();


    }
}