package com.aluminate.aluminate_organization_backend.config.util;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import javax.crypto.Cipher;
import java.io.StringWriter;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAEncryptionUtil {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    // Key Generation
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    // Key Serialization
    public static String publicKeyToPem(PublicKey publicKey) throws Exception {
        StringWriter writer = new StringWriter();
        try (PemWriter pemWriter = new PemWriter(writer)) {
            pemWriter.writeObject(new PemObject("PUBLIC KEY", publicKey.getEncoded()));
        }
        return writer.toString();
    }

    public static String privateKeyToPem(PrivateKey privateKey) throws Exception {
        StringWriter writer = new StringWriter();
        try (PemWriter pemWriter = new PemWriter(writer)) {
            pemWriter.writeObject(new PemObject("PRIVATE KEY", privateKey.getEncoded()));
        }
        return writer.toString();
    }

    // Key Deserialization
    public static PublicKey publicKeyFromPem(String pem) throws Exception {
        byte[] content = parsePem(pem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(content);
        KeyFactory factory = KeyFactory.getInstance("RSA", "BC");
        return factory.generatePublic(spec);
    }

    public static PrivateKey privateKeyFromPem(String pem) throws Exception {
        byte[] content = parsePem(pem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(content);
        KeyFactory factory = KeyFactory.getInstance("RSA", "BC");
        return factory.generatePrivate(spec);
    }

    // Encryption/Decryption
    public static String encrypt(String plaintext, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(plaintext.getBytes()));
    }

    public static String decrypt(String ciphertext, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(ciphertext)));
    }

    private static byte[] parsePem(String pem) {
        String content = pem.replaceAll("-----BEGIN [^-]+-----", "")
                .replaceAll("-----END [^-]+-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(content);
    }
}