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

import static main.TestServer.IntFromByteArray;
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

    static int IntFromByteArray(byte[] bytes) {
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
        System.out.println("Mandamos yuanPublico");
    } // Mandar yuanPublico
}

class NonceAck implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        //<editor-fold desc="Desencriptar input">
        byte[] rsaCrypted = exchange.getRequestBody().readAllBytes();
        System.out.println("RSA ENCRIPTADO");
        System.out.println(new String(rsaCrypted));
        byte[] buenArray;
        try {
            buenArray = RSAsco.decrypt(rsaCrypted, TestServer.yuanPrivado);
        } catch (Exception e) {
            System.out.println("RSA PUTERO");
            throw new RuntimeException("RSA decrypt: nonceAck", e);
        }
        System.out.println(new String(buenArray));
        //</editor-fold>

        //<editor-fold desc="Hallar datos"
        InputStream input = new ByteArrayInputStream(buenArray);

        // ID
        byte[] idBytes = input.readNBytes(4);
        int id = IntFromByteArray(idBytes);

        // Nonce
        byte nonce = (byte) input.read();

        // AES Key
        byte[] aesKeyData = input.readAllBytes();
        var secretKey = AEStalkear.generateKey(aesKeyData);
        //</editor-fold>

        //<editor-fold desc="Tablear">
        //todo check y en caso de coincidir, el servidor decide un id libre
        //todo id debería ser algo más que un int
        table.put(id, new Blob(secretKey, nonce));
        //</editor-fold>

        //<editor-fold desc="Devolver">
        //todo Caso servidor indica ID (cifrado con AES, ya tenemos Nonce + clave)
        exchange.sendResponseHeaders(200, 2);
        exchange.getResponseBody().write(new byte[]{0,0}); // dummy data
        exchange.close();
        //</editor-fold>
    }
}

class CryptedReturner implements HttpHandler {
    private final byte[] data;

    public CryptedReturner(byte[] data) { this.data = data; }
    @Override
    public void handle(HttpExchange exchange) {
        try {
            //<editor-fold desc="Cabeceras"
            //todo encriptar / hashear tambien?
            var xd = exchange.getRequestHeaders().getFirst("clientID");
            int id = Integer.parseInt(xd);
            System.out.println("ID: " + id);
            //</editor-fold>

            //<editor-fold desc="Buscar en tabla">
            //todo caso no encontrado (no debería)
            Blob meta = table.get(id);
            System.out.println("NONCE inicial: "+ meta.nonce);

            var key = meta.aesKey;
            var iv = AEStalkear.generateIV(meta.nonce);

            System.out.println("AES Key");
            System.out.println(new String(key.getEncoded()));
            System.out.println("IV");
            System.out.println(Arrays.toString(iv.getIV()));
            //</editor-fold>

            //<editor-fold desc="Brea Jose">
            byte[] crypted = exchange.getRequestBody().readAllBytes();
            byte[] fino = AEStalkear.decrypt(crypted, key, iv);
            assert fino != null;

            System.out.println("Criptado recibido");
            System.out.println(new String(crypted));
            System.out.println("Desencriptado");
            System.out.println(new String(fino));
            //</editor-fold>

            //<editor-fold desc="Updatear Nonce">
            byte nonceUpdate = 5; // TODO RANDOM
            meta.nonce += nonceUpdate;
            System.out.println("NONCE final: " + meta.nonce);
            //</editor-fold>

            //<editor-fold desc="Concatenar y encriptar datos AES">
            ByteBuffer bb = ByteBuffer.allocate(fino.length+data.length).put(fino).put(fino.length,data);

            System.out.println("Respuesta");
            System.out.println(new String(bb.array()));
            byte[] breaCripto = AEStalkear.encrypt(bb.array(), key, iv);
            assert breaCripto != null;
            System.out.println("Respuesta Cripto");
            System.out.println(new String(breaCripto));
            //</editor-fold>

            //<editor-fold desc="Enviar datos">
            exchange.sendResponseHeaders(200, 1+breaCripto.length);
            var output = exchange.getResponseBody();

            // Dato nonceUpdate
            //todo encriptar también?
            output.write(nonceUpdate);

            // Brea importante
            output.write(breaCripto);
            exchange.close();
            //</editor-fold>

        } catch (Exception e) {
            throw new RuntimeException("Si sale esto me tiro por un puente", e);
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
