package com.brightpath.science.data.local;

public interface TextCipher {
        /*
 * Responsibility: Performs the `encrypt` operation for TextCipher within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
String encrypt(String plainText);
        /*
 * Responsibility: Performs the `decrypt` operation for TextCipher within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
String decrypt(String storedText);
}
