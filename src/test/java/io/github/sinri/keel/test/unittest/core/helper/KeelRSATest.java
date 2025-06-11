package io.github.sinri.keel.test.unittest.core.helper;

import io.github.sinri.keel.core.helper.encryption.rsa.KeelRSA;
import io.github.sinri.keel.core.helper.encryption.rsa.KeelRSAKeyPair;
import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

class KeelRSATest extends KeelUnitTest {
    public String raw;
    private final KeelRSA rsa;
    private final String path;

    public KeelRSATest() {
        try {
            path = "/Users/sinri/code/keel/src/test/resources/runtime";
            KeelRSAKeyPair.generateKeyPairToDir(path);
            rsa = new KeelRSA();
            rsa.loadPublicKeyByKeyStoreFile(path + "/publicKey.keystore");
            rsa.loadPrivateKeyByKeyStoreFile(path + "/privateKey.keystore");
        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

        raw = "看上去成功了吗😁23333Hahaha";
    }

    @Test
    public void test1() {
        try {
            byte[] encryptedByPrivateKey = rsa.encryptWithPrivateKey(raw.getBytes());
            byte[] decryptedByPublicKey = rsa.decryptWithPublicKey(encryptedByPrivateKey);
            getUnitTestLogger().info("encryptedByPrivateKey and decryptedByPublicKey: " + new String
                    (decryptedByPublicKey));

            byte[] encryptedByPublicKey = rsa.encryptWithPublicKey(raw.getBytes());
            byte[] decryptedByPrivateKey = rsa.decryptWithPrivateKey(encryptedByPublicKey);
            getUnitTestLogger().info("encryptedByPublicKey and decryptedByPrivateKey: " + new String
                    (decryptedByPrivateKey));

            String sign = rsa.signWithPrivateKey(raw.getBytes());
            getUnitTestLogger().info("sign: " + sign);
            boolean verified = rsa.verifySignWithPublicKey(raw.getBytes(), sign);
            getUnitTestLogger().info("verified: " + verified);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}