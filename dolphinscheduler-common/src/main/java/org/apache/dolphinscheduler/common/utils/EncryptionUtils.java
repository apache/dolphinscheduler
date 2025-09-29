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

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;

/**
 * encryption utils
 */
@Slf4j
public class EncryptionUtils {

    private static final byte[] defaultKey =
            {0x72, 0x38, 0x61, 0x73, 0x49, 0x73, 0x41, 0x52, 0x22, 0x11, 0x72, 0x65, 0x74,
                    0x6c, 0x61, 0x49};

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

    private static byte[] getKeyFromConfig() {
        try {
            String keyStr = System.getProperty("datasource.encryption.key");
            if (StringUtils.isEmpty(keyStr)) {
                return getDefaultKey();
            }
            return Base64.decodeBase64(keyStr);
        } catch (Exception e) {
            log.warn("Failed to load encryption key from config, using default key");
            return getDefaultKey();
        }
    }
    private static byte[] getDefaultKey() {
        return defaultKey;
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

    private static String decrypt(String strToDecrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            final SecretKeySpec secretKey = new SecretKeySpec(getKeyFromConfig(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.decodeBase64(strToDecrypt)), Charset.defaultCharset());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static String decryptPass(String value) {
        return decrypt(value.substring(ENC_PREFIX.length(), value.length() - ENC_SUBFIX.length()));
    }

    public static String getDecryptedValue(String value) {
        try {
            if (isEncrypted(value)) {
                return decryptPass(value);
            }
            return value;
        } catch (Exception e) {
            log.error("Get decrypted value failed, {}", value, e);
            return null;
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.exit(1);
        }
        String password = args[0];
        String key = args[1];
        System.out.println(EncryptionUtils.encrypt(password, Base64.decodeBase64(key)));
    }

}
