package main;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Random;

public class Tests {
    public static void main(String[] args) throws Exception {
        NegociaClaveNonce();
    }
    public static void NegociaClaveSingleStep() {
        try {
            PublicKey yuanPublico = RSAsco.loadPublicKey();

            var klave = AEStalkear.generateKey();
            var init = AEStalkear.generateIV();

            byte[] laBrea = klave.getEncoded();

            byte[] elJaleo = RSAsco.encrypt(laBrea, yuanPublico);

            Socket s = new Socket("192.168.22.192",7778);
            System.out.println("Sokete");

            s.getOutputStream().write(elJaleo);
            s.getOutputStream().flush();
            s.shutdownOutput();

            System.out.println("Corrido a oskitar");

            byte[] response = s.getInputStream().readAllBytes();
            s.close();
            System.out.println("Corrido de oskitar");
            String solved = AEStalkear.decrypt(new String(response, StandardCharsets.UTF_8));
            System.out.println(solved);

        } catch (InvalidKeySpecException | IOException | BadPaddingException | IllegalBlockSizeException |
                 InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }
    static byte[] IntToByteArray( int data ) {
        byte[] result = new byte[4];
        result[0] = (byte) ((data & 0xFF000000) >> 24);
        result[1] = (byte) ((data & 0x00FF0000) >> 16);
        result[2] = (byte) ((data & 0x0000FF00) >> 8);
        result[3] = (byte) ((data & 0x000000FF));
        return result;
    }

    public static void NegociaClaveNonce() throws Exception {
        //<editor-fold desc="URLs y Streams">
        URL u1 = new URL("http://localhost:8080/publicKey");
        URL u2 = new URL("http://localhost:8080/setNonce");
        URL u3 = new URL("http://localhost:8080/data");
        InputStream input;
        OutputStream output;
        //</editor-fold>

        //<editor-fold desc="Pedir yuan publico"
        HttpURLConnection pedirYuan = (HttpURLConnection) u1.openConnection();

        input = pedirYuan.getInputStream();
        PublicKey yuanPublico = RSAsco.loadPublicKey(input.readAllBytes());
        input.close();

        System.out.println("Clave Publica RSA");
        System.out.println(new String(yuanPublico.getEncoded()));

        pedirYuan.disconnect();
        //</editor-fold>

        //<editor-fold desc="Pedir ACK para clave y nonce">
        Random r = new Random(123);

        // ID
        int id = r.nextInt();
        byte[] idBytes = IntToByteArray(id);
        System.out.println("ID: " + id);

        // Nonce
        byte[] xd = new byte[1];
        r.nextBytes(xd);
        byte nonce = xd[0]; // xd?
        System.out.println("Nonce: " + nonce);

        // SecretKey
        var aesKey = AEStalkear.generateKey("random1", "random2");
        byte[] aesKeyBytes = aesKey.getEncoded();
        System.out.println("AES key");
        System.out.println(new String(aesKeyBytes));

        // Encrypt RSA
        ByteBuffer bb = ByteBuffer.allocate(idBytes.length + 1 + aesKeyBytes.length).put(idBytes).put(idBytes.length, nonce).put(idBytes.length+1,aesKeyBytes);
        var breaCriptada = RSAsco.encrypt(bb.array(), yuanPublico);
        System.out.println("RSA data");
        System.out.println(new String(breaCriptada));

        // Con
        HttpURLConnection noticeMeSenpai = (HttpURLConnection) u2.openConnection();

        noticeMeSenpai.setDoOutput(true);

        output = noticeMeSenpai.getOutputStream();
        output.write(breaCriptada);
        output.flush();
        output.close();

        noticeMeSenpai.getInputStream().close();

        noticeMeSenpai.disconnect();

        System.out.println("NONCE INICIAL: "+nonce);

        //</editor-fold>

        //<editor-fold desc="Es hora de robar">

        // Encriptar cuerpo
        byte[] reqBody = "El dato: ".getBytes(StandardCharsets.UTF_8);
        byte[] laBrea = AEStalkear.encrypt(reqBody, aesKey, AEStalkear.generateIV(nonce));
        assert laBrea != null;
        System.out.println("Request: \"El dato: \"");
        System.out.println("Request encriptado");
        System.out.println(new String(laBrea));

        // Conectar
        HttpURLConnection robarDatos = (HttpURLConnection) u3.openConnection();
        robarDatos.setRequestProperty("clientID", String.valueOf(id));
        robarDatos.setDoOutput(true);

        // Escribir data
        output = robarDatos.getOutputStream();
        output.write(laBrea);
        output.close();

        // Robar data (con permiso y confianza)
        input = robarDatos.getInputStream();
        int nonceUpdate = input.read();
        byte[] breaRespondida = input.readAllBytes();
        input.close();
        System.out.println("Respuesta encriptada");
        System.out.println(new String(breaRespondida));

        // Desbrear data robada educadamente
        byte[] response = AEStalkear.decrypt(breaRespondida, aesKey, AEStalkear.generateIV(nonce));
        assert response != null;
        System.out.println("Respuesta");
        System.out.println(new String(response, StandardCharsets.UTF_8));

        // Terminar
        robarDatos.disconnect();
        nonce += nonceUpdate;
        System.out.println("NONCE FINAL: "+nonce);
        //</editor-fold>

        //<editor-fold desc="Es hora de robar he dicho">
        reqBody = "El dato2: ".getBytes(StandardCharsets.UTF_8);
        laBrea = AEStalkear.encrypt(reqBody, aesKey, AEStalkear.generateIV(nonce));
        assert laBrea != null;

        robarDatos = (HttpURLConnection) u3.openConnection();

        robarDatos.setRequestProperty("clientID", String.valueOf(id));

        robarDatos.setDoOutput(true);
        output = robarDatos.getOutputStream();
        output.write(laBrea);
        output.close();

        input = robarDatos.getInputStream();
        nonceUpdate = input.read();
        breaRespondida = input.readAllBytes();
        input.close();
        response = AEStalkear.decrypt(breaRespondida, aesKey, AEStalkear.generateIV(nonce));
        assert response != null;
        System.out.println(new String(response, StandardCharsets.UTF_8));

        robarDatos.disconnect();

        nonce += nonceUpdate;

        System.out.println("NONCE FINAL: "+nonce);
        //</editor-fold>

    }
}
