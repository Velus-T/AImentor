package com.example.aimentor.science.data.local;

public interface TextCipher {
    String encrypt(String plainText);
    String decrypt(String storedText);
}
