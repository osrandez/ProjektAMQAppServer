package main;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

class AEStalkear {
    //<editor-fold desc="Tests">
    public static void testBytes() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String puterio = "Aqui se podruce jaleo en bytes :)";
        var crypted = encrypt(puterio.getBytes(StandardCharsets.UTF_8), generateKey(), generateIV());
        assert crypted != null;
        System.out.println(new String(crypted));
        var puterioDeshacido = decrypt(crypted, generateKey(), generateIV());
        assert puterioDeshacido != null;
        System.out.println(new String(puterioDeshacido));
    }
    public static void testString() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String puterio = "Aqui se podruce jaleo en string :)";
        var crypted = encrypt(puterio, generateKey(), generateIV());
        System.out.println(crypted);
        var puterioDeshacido = decrypt(crypted, generateKey(), generateIV());
        System.out.println(puterioDeshacido);
    }
    //</editor-fold>

    private static final String PASSPHRASE = "Esta es una passphrase decente mi pana";
    private static final String SALT = "cosas indecentes";

    //<editor-fold desc="Strings con clave e IV default"
    public static String encrypt(String str) {
        return encrypt(str, generateKey(), generateIV());
    }
    public static String decrypt(String str) {
        return encrypt(str, generateKey(), generateIV());
    }
    //</editor-fold>

    //<editor-fold desc="Strings con clave e IV genericos"
    public static String encrypt(String str, SecretKeySpec secretKey, IvParameterSpec ivspec) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            return Base64.getEncoder()
                    .encodeToString(cipher.doFinal(str.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e);
        }
        return null;
    }
    public static String decrypt(String strToDecrypt, SecretKeySpec secretKey, IvParameterSpec ivspec) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e);
        }
        return null;
    }
    //</editor-fold>

    //<editor-fold desc="Bytes con clave e IV genericos">
    public static byte[] encrypt(byte[] data, SecretKeySpec secretKey, IvParameterSpec ivspec) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            return Base64.getEncoder()
                    .encode(cipher.doFinal(data));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e);
        }
        return null;
    }
    public static byte[] decrypt(byte[] data, SecretKeySpec secretKey, IvParameterSpec ivspec) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            return cipher.doFinal(Base64.getDecoder().decode(data));
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e);
        }
        return null;
    }
    //</editor-fold>

    //<editor-fold desc="Claves">
    public static IvParameterSpec generateIV() {
        byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        return new IvParameterSpec(iv);
    }
    public static SecretKeySpec generateKey() {
        return generateKey(PASSPHRASE, SALT);
    }
    public static SecretKeySpec generateKey(byte[] data) {
        return new SecretKeySpec(data, "AES");
    }
    public static SecretKeySpec generateKey(String passPhrase, String salt) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(passPhrase.toCharArray(), salt.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            return new SecretKeySpec(tmp.getEncoded(), "AES");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
    //</editor-fold>
}
