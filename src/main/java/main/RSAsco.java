package main;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAsco {
    public static void main(String[] args) {
        try {
            PublicKey yuanPublico = loadPublicKey();

            String laBrea = "Puterio homosexual en los frios montes de alberite";

            System.out.println(laBrea);

            var elJaleo = encrypt(laBrea.getBytes(), yuanPublico);

            System.out.println(new String(elJaleo));

            PrivateKey yuanPrivado = loadPrivateKey();

            var Jaleont = decrypt(elJaleo, yuanPrivado);

            System.out.println(new String(Jaleont));


        } catch (InvalidKeySpecException | IOException | BadPaddingException | IllegalBlockSizeException |
                 InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    //<editor-fold desc="RSA Strings">
    public static String encrypt(String data, Key publicKey) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] bytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return new String(Base64.getEncoder().encode(bytes));
    }
    public static String decrypt(String data, Key privateKey) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] bytes = cipher.doFinal(Base64.getDecoder().decode(data));
        return new String(bytes);
    }
    //</editor-fold>

    //<editor-fold desc="RSA Bytes">
    public static byte[] encrypt(byte[] data, Key key) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] bytes = cipher.doFinal(data);
        return Base64.getEncoder().encode(bytes);
    }
    public static byte[] decrypt(byte[] data, Key key) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, key);

        return cipher.doFinal(Base64.getDecoder().decode(data));
    }
    //</editor-fold>

    //<editor-fold desc="Claves">
    public static PublicKey loadPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        Path path = Paths.get("elhombRedelSAco.pub");
        byte[] bytes = Files.readAllBytes(path);

        X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(ks);
    }

    public static PublicKey loadPublicKey(byte[] data) throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec ks = new X509EncodedKeySpec(data);
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

    public static void genKey(){
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");

            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();

            Key pub = kp.getPublic();
            Key pvt = kp.getPrivate();

            String outFile = "elhombRedelSAco";
            var out = new FileOutputStream(outFile + ".key");
            out.write(pvt.getEncoded());
            out.close();

            out = new FileOutputStream(outFile + ".pub");
            out.write(pub.getEncoded());
            out.close();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("algot",e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("fichero", e);
        } catch (IOException e) {
            throw new RuntimeException("io", e);
        }
    }
    //</editor-fold>
}
