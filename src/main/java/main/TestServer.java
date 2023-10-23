package main;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static main.TestServer.fromByteArray;
import static main.TestServer.table;

public class TestServer {
    static final PublicKey yuanPublico;
    static final PrivateKey yuanPrivado;
    static HashMap<Integer, Blob> table = new HashMap<>();

    static {
        try {
            yuanPublico = RSAsco.loadPublicKey();
            yuanPrivado = RSAsco.loadPrivateKey();
        } catch (Exception e) {
            throw new RuntimeException("Mal :(",e);
        }
    }
    static int fromByteArray(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8 ) |
                ((bytes[3] & 0xFF));
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost",8080),0);
        ExecutorService ex = Executors.newCachedThreadPool();
        server.setExecutor(ex);

        var yuanGiver = new PublicReturner(yuanPublico.getEncoded());
        var nonceAck = new NonceAck();
        var normal = new CryptedReturner("Datos :)".getBytes(StandardCharsets.UTF_8));

        server.createContext("/publicKey", yuanGiver);
        server.createContext("/setNonce", nonceAck);
        server.createContext("/data", normal);
        server.start();

        new Scanner(System.in).nextLine();
        System.out.println("Parando...");
        server.stop(0);
        ex.shutdown();
    }
}

class PublicReturner implements HttpHandler {
    private static byte[] data;

    public PublicReturner(byte[] data) { PublicReturner.data = data; }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, data.length);
        var output = exchange.getResponseBody();
        output.write(data);
        exchange.close();
    }
}

class NonceAck implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("ACK IN");
        byte[] rsaCrypted = exchange.getRequestBody().readAllBytes();
        byte[] buenArray = null;
        try {
            buenArray = RSAsco.decrypt(rsaCrypted, TestServer.yuanPrivado);
        } catch (Exception e) {
            System.out.println("RSA PUTERO");
            throw new RuntimeException("RSA decrypt: nonceAck", e);
        }

        InputStream input = new ByteArrayInputStream(buenArray);


        byte[] idBytes = input.readNBytes(4);
        int id = fromByteArray(idBytes);
        byte nonce = (byte) input.read();
        byte[] aesKeyData = input.readAllBytes();

        var secretKey = AEStalkear.generateKey(aesKeyData);

        //todo check y retry
        //todo id no puede ser un int para esto
        table.put(id, new Blob(secretKey, nonce));

        exchange.sendResponseHeaders(200, 2);
        exchange.getResponseBody().write(new byte[]{0,0});
        exchange.close();
    }
}

class CryptedReturner implements HttpHandler {
    private final byte[] data;

    public CryptedReturner(byte[] data) { this.data = data; }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {

            var xd = exchange.getRequestHeaders().getFirst("clientID");
            int id = Integer.parseInt(xd);

            Blob meta = table.get(id);
            System.out.println("NONCE inicial: "+ meta.nonce);

            var key = meta.aesKey;
            var iv = AEStalkear.generateIV(meta.nonce);

            byte[] crypted = exchange.getRequestBody().readAllBytes();
            byte[] fino = AEStalkear.decrypt(crypted, key, iv);
            assert fino != null;

            byte nonceUpdate = 5; // TODO RANDOM

            ByteBuffer bb = ByteBuffer.allocate(fino.length+data.length).put(fino).put(fino.length,data);

            byte[] breaCripto = AEStalkear.encrypt(bb.array(), key, iv);
            assert breaCripto != null;

            exchange.sendResponseHeaders(200, 1+breaCripto.length);

            var output = exchange.getResponseBody();
            output.write(nonceUpdate);
            output.write(breaCripto);
            exchange.close();
            meta.nonce += nonceUpdate;
            System.out.println("NONCE final: " + meta.nonce);
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException("PUTABIDA", e);
        }
    }
}

class Blob {
    public Blob(SecretKeySpec key, byte nonce) {
        aesKey = key;
        this.nonce = nonce;
    }

    SecretKeySpec aesKey;
    byte nonce;
}
