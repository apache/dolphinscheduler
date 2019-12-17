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
package org.apache.dolphinscheduler.api.security;

import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiApplicationServer.class)
public class JsonWebTokenAuthenticatorTest {
    private static Logger logger = LoggerFactory.getLogger(JsonWebTokenAuthenticatorTest.class);

    @Test
    public void getPublicKey() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        writePemToTempDir();
        JsonWebTokenAuthenticator authenticator = new JsonWebTokenAuthenticator();
        PublicKey publicKey = authenticator.getPublicKey("/tmp/test_rsa_public_key.pem");
        Assert.assertNotNull(publicKey);
        logger.info(publicKey.toString());
    }

    private void writePemToTempDir() throws IOException {
        String keyContents = "-----BEGIN PUBLIC KEY-----\n" +
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC2jR/uUOkUvoqIAAm6e9oMcvEA\n" +
                "h7s4FjnKorQJ6FYcHm5Iq9bd7UNQ+JrkLa4aEk2zJ+eIdZHlAd1x7Sf4EoN9f5pb\n" +
                "X6jZS1+seC94kdPPzMrSUTsu9d2AfL2YBbHX0On8/etF1F6f6x0ARfHuUKHT/Pgu\n" +
                "MrBcV1c+0iiZrarwcQIDAQAB\n" +
                "-----END PUBLIC KEY-----";
        File file = new File("/tmp/test_rsa_public_key.pem");
        file.delete();
        file.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(keyContents);
        writer.flush();
        writer.close();
    }
}
