import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyGenerator {

    public static void main(String[] args) {
        try {
            // Generate key pair
            KeyPair keyPair = generateKeyPair();

            // Save public key to file
            savePublicKey(keyPair.getPublic(), "public_key.pem");

            // Save private key to file
            savePrivateKey(keyPair.getPrivate(), "private_key.pem");

            System.out.println("Keys generated and saved successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // You can adjust the key size as needed
        return keyPairGenerator.generateKeyPair();
    }

    private static void savePublicKey(PublicKey publicKey, String fileName) throws Exception {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
        byte[] publicKeyBytes = x509EncodedKeySpec.getEncoded();
        String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKeyBytes);

        Path path = Paths.get(fileName);
        Files.write(path, publicKeyBase64.getBytes());
    }

    private static void savePrivateKey(PrivateKey privateKey, String fileName) throws Exception {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
        byte[] privateKeyBytes = pkcs8EncodedKeySpec.getEncoded();
        String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKeyBytes);

        Path path = Paths.get(fileName);
        Files.write(path, privateKeyBase64.getBytes());
    }
}
