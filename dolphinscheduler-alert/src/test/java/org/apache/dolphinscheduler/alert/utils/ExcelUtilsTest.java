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

package org.apache.dolphinscheduler.alert.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import static org.junit.Assert.assertTrue;

public class ExcelUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(ExcelUtilsTest.class);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private String rootPath = null;

    @Before
    public void setUp() throws Exception {

        folder.create();
        rootPath = folder.getRoot().getAbsolutePath();
    }

    @After
    public void tearDown() throws Exception {

        folder.delete();
    }

    /**
     * Test GenExcelFile
     */
    @Test
    public void testGenExcelFile() {

        //Define dest file path
        String xlsFilePath = rootPath + System.getProperty("file.separator");
        logger.info("xlsFilePath: "+xlsFilePath);

        //Define correctContent
        String correctContent = "[{\"name\":\"ds name\",\"value\":\"ds value\"}]";

        //Define incorrectContent
        String incorrectContent1 = "{\"name\":\"ds name\",\"value\":\"ds value\"}";

        //Define title
        String title = "test report";

        //Invoke genExcelFile with correctContent
        ExcelUtils.genExcelFile(correctContent, title, xlsFilePath);

        //Test file exists
        File xlsFile = new File(xlsFilePath + Constants.SINGLE_SLASH + title + Constants.EXCEL_SUFFIX_XLS);
        assertTrue(xlsFile.exists());

        //Expected RuntimeException
        expectedException.expect(RuntimeException.class);

        //Expected error message
        expectedException.expectMessage("itemsList is null");

        //Invoke genExcelFile with incorrectContent, will cause RuntimeException
        ExcelUtils.genExcelFile(incorrectContent1, title, xlsFilePath);

    }

    /**
     * Test GenExcelFile (check directory)
     */
    @Test
    public void testGenExcelFileByCheckDir() {
        ExcelUtils.genExcelFile("[{\"a\": \"a\"},{\"a\": \"a\"}]", "t", "/tmp/xls");
        File file = new File("/tmp/xls" + Constants.SINGLE_SLASH + "t" + Constants.EXCEL_SUFFIX_XLS);
        file.delete();
    }
}