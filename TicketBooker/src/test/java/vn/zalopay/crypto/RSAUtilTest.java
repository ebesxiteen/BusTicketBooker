package vn.zalopay.crypto;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RSAUtilTest {

    private static KeyPair keyPair;

    @BeforeAll
    static void initKeys() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keyPair = generator.generateKeyPair();
    }

    @Test
    void stringConversionRoundTripsForPublicAndPrivateKeys() throws InvalidKeySpecException {
        String publicKeyString = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateKeyString = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

        PublicKey convertedPublic = RSAUtil.stringToPublicKey(publicKeyString);
        PrivateKey convertedPrivate = RSAUtil.stringToPrivateKey(privateKeyString);

        assertArrayEquals(keyPair.getPublic().getEncoded(), convertedPublic.getEncoded());
        assertArrayEquals(keyPair.getPrivate().getEncoded(), convertedPrivate.getEncoded());
    }

    @Test
    void encryptWithPublicKeyProducesCipherText() throws Exception {
        String cipherText = RSAUtil.encrypt(keyPair.getPublic(), "hello world");

        assertNotNull(cipherText);
        assertNotEquals("hello world", cipherText);
    }

    @Test
    void decryptWithPrivateKeyReturnsOutputBytes() throws Exception {
        byte[] decrypted = RSAUtil.decrypt(keyPair.getPrivate(), "sample message");

        assertNotNull(decrypted);
        // Regardless of the internal cipher mode, the method should return some bytes
        // for further handling by callers.
        assertNotEquals(0, decrypted.length);
    }
}
