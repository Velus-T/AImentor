package com.example.aimentor.science.data.local;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public final class AndroidKeystoreTextCipher implements TextCipher {
    private static final String KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "ai_mentor_science_history_key";
    private static final String PREFIX = "enc:v1:";

    @Override
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) return plainText == null ? "" : plainText;
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] iv = cipher.getIV();
            ByteBuffer buffer = ByteBuffer.allocate(4 + iv.length + encrypted.length);
            buffer.putInt(iv.length);
            buffer.put(iv);
            buffer.put(encrypted);
            return PREFIX + Base64.encodeToString(buffer.array(), Base64.NO_WRAP);
        } catch (Exception error) {
            throw new IllegalStateException("Unable to encrypt Science data.", error);
        }
    }

    @Override
    public String decrypt(String storedText) {
        if (storedText == null || storedText.isEmpty() || !storedText.startsWith(PREFIX)) {
            // Supports rows created before encryption was introduced.
            return storedText == null ? "" : storedText;
        }
        try {
            byte[] payload = Base64.decode(storedText.substring(PREFIX.length()), Base64.NO_WRAP);
            ByteBuffer buffer = ByteBuffer.wrap(payload);
            int ivLength = buffer.getInt();
            if (ivLength < 12 || ivLength > 32 || buffer.remaining() <= ivLength) {
                throw new IllegalArgumentException("Invalid encrypted payload.");
            }
            byte[] iv = new byte[ivLength];
            buffer.get(iv);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception error) {
            return "[Encrypted content could not be read on this device]";
        }
    }

    private SecretKey getOrCreateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE);
        keyStore.load(null);
        if (keyStore.containsAlias(KEY_ALIAS)) {
            return ((KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null)).getSecretKey();
        }
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE);
        generator.init(new KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build());
        return generator.generateKey();
    }
}
