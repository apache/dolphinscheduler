/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.common.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;

/**
 * encryption utils
 */
@Slf4j
public class EncryptionUtils {

    private static final int AES_KEY_LEN = 16; // 128 bit
    private static final int ITERATIONS = 130_000;

    public static final String ENC_PREFIX = "ENC('";
    public static final String ENC_SUBFIX = "')";

    private EncryptionUtils() {
        throw new UnsupportedOperationException("Construct EncryptionUtils");
    }

    /**
     * @param rawStr raw string
     * @return md5(rawStr)
     */
    public static String getMd5(String rawStr) {
        return DigestUtils.md5Hex(null == rawStr ? StringUtils.EMPTY : rawStr);
    }

    public static boolean isEncrypted(String value) {
        return StringUtils.isNotEmpty(value) && value.startsWith(ENC_PREFIX) && value.endsWith(ENC_SUBFIX);
    }

    private static String encrypt(String strToEncrypt, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            final SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.encodeBase64String(cipher.doFinal(strToEncrypt.getBytes(Charset.defaultCharset())));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static String decrypt(String strToDecrypt, String passwordEncryptKey) {
        if (StringUtils.isEmpty(passwordEncryptKey)) {
            throw new RuntimeException("No encryption key found in config");
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            final SecretKeySpec secretKey = new SecretKeySpec(Base64.decodeBase64(passwordEncryptKey), "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.decodeBase64(strToDecrypt)), Charset.defaultCharset());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static String decryptPassword(String value, String passwordEncryptKey) {
        return decrypt(value.substring(ENC_PREFIX.length(), value.length() - ENC_SUBFIX.length()), passwordEncryptKey);
    }

    private static String normalizeKey(String password) {
        try {
            byte[] salt = new byte[8];
            new SecureRandom().nextBytes(salt);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, AES_KEY_LEN * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] keyBytes = skf.generateSecret(spec).getEncoded();
            return Base64.encodeBase64String(keyBytes);
        } catch (Exception e) {
            throw new RuntimeException("Key derivation failed", e);
        }
    }
    private static String colorize(String text) {
        return "\u001B[31m" + text + "\u001B[0m";
    }
    public static void main(String[] args) {
        String out = "Encrypted Password is [%s], Encrypted Key is [%s]";
        if (args.length != 2) {
            System.out.println("Usage: sh encrypt-password.sh [plain-password] [plain-key]");
        } else {
            String password = args[0];
            String key = args[1];
            String normalizedKey = normalizeKey(key);
            System.out.printf((out) + "%n", colorize(encrypt(password, Base64.decodeBase64(normalizedKey))),
                    colorize(normalizedKey));
        }
    }
}
