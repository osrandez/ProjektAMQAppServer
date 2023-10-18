package main;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSATESTO {
    public static void main(String[] args) {
        try {
            PublicKey yuanPublico = loadPublicKey();

            var klave = AEStalkear.generateKey();
            var init = AEStalkear.generateIV();
            byte[] laBrea = klave.getEncoded();

            byte[] elJaleo = encrypt(laBrea, yuanPublico);

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

        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (IOException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException |
                NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }

    }

    public static String decrypt(String data, PrivateKey privateKey) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] bytes = cipher.doFinal(Base64.getDecoder().decode(data));
        return new String(bytes);
    }

    public static byte[] decrypt(byte[] data, PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return cipher.doFinal(Base64.getDecoder().decode(data));
    }
    public static String encrypt(String data, PublicKey publicKey) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] bytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return new String(Base64.getEncoder().encode(bytes));
    }

    // TESTO
    public static byte[] encrypt(byte[] data, PublicKey pk) throws BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pk);

        byte[] bytes = cipher.doFinal(data);
        return Base64.getEncoder().encode(bytes);
    }

    public static PublicKey loadPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        Path path = Paths.get("elhombRedelSAco.pub");
        byte[] bytes = Files.readAllBytes(path);

        X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(ks);
    }

    public static PrivateKey loadPrivateKey() throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        Path path = Paths.get("elhombRedelSAco.key");
        byte[] bytes = Files.readAllBytes(path);

        PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(ks);
    }
}
