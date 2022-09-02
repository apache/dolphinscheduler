package org.apache.dolphinscheduler.common.utils;

import static org.apache.dolphinscheduler.common.Constants.FOLDER_SEPARATOR;
import static org.apache.dolphinscheduler.common.Constants.FORMAT_S_S;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.Bucket;
import com.aliyun.oss.model.PutObjectRequest;

@RunWith(MockitoJUnitRunner.class)
public class OssOperatorTest {

    private static final Logger logger = LoggerFactory.getLogger(OssOperatorTest.class);

    private static final String ACCESS_KEY_ID_MOCK = "ACCESS_KEY_ID_MOCK";
    private static final String ACCESS_KEY_SECRET_MOCK = "ACCESS_KEY_SECRET_MOCK";
    private static final String REGION_MOCK = "REGION_MOCK";
    private static final String BUCKET_NAME_MOCK = "BUCKET_NAME_MOCK";
    private static final String TENANT_CODE_MOCK = "TENANT_CODE_MOCK";
    private static final String DIR_MOCK = "DIR_MOCK";
    private static final String FILE_NAME_MOCK = "FILE_NAME_MOCK";
    private static final String FILE_PATH_MOCK = "FILE_PATH_MOCK";

    @Mock
    private OSS ossClientMock;

    @Mock
    private Bucket bucketMock;

    @Mock
    private PutObjectRequest putObjectRequestMock;

    @Mock
    private PropertyUtilsWrapper propertyUtilsWrapperMock;

    private OssOperator ossOperator;

    @Before
    public void setUp() throws Exception {
        ossOperator = spy(OssOperator.getInstance());
        doReturn(propertyUtilsWrapperMock).when(ossOperator).createPropertyUtilsWrapper();
        doReturn(ossClientMock).when(ossOperator).buildOssClient();
        doReturn(ACCESS_KEY_ID_MOCK).when(propertyUtilsWrapperMock)
                .getString(TaskConstants.ALIBABA_CLOUD_ACCESS_KEY_ID);
        doReturn(ACCESS_KEY_SECRET_MOCK).when(propertyUtilsWrapperMock)
                .getString(TaskConstants.ALIBABA_CLOUD_ACCESS_KEY_SECRET);
        doReturn(REGION_MOCK).when(propertyUtilsWrapperMock).getString(TaskConstants.ALIBABA_CLOUD_REGION);
        doReturn(BUCKET_NAME_MOCK).when(propertyUtilsWrapperMock).getString(Constants.ALIBABA_CLOUD_OSS_BUCKET_NAME);
        // doReturn(END_POINT_MOCK).when(propertyUtilsWrapperMock).getString(ALIBABA_CLOUD_OSS_END_POINT);
        doNothing().when(ossOperator).ensureBucketSuccessfullyCreated(any());

        ossOperator.init();

    }

    @Test
    public void initOssOperator() {
        verify(ossOperator, times(1)).createPropertyUtilsWrapper();
        verify(ossOperator, times(1)).buildOssClient();
        Assert.assertEquals(ACCESS_KEY_ID_MOCK, ossOperator.ACCESS_KEY_ID);
        Assert.assertEquals(ACCESS_KEY_SECRET_MOCK, ossOperator.ACCESS_KEY_SECRET);
        Assert.assertEquals(REGION_MOCK, ossOperator.REGION);
        Assert.assertEquals(BUCKET_NAME_MOCK, ossOperator.BUCKET_NAME);
    }

    @Test
    public void tearDownOssOperator() throws IOException {
        doNothing().when(ossClientMock).shutdown();
        ossOperator.close();
        verify(ossClientMock, times(1)).shutdown();
    }

    @Test
    public void createTenantResAndUdfDir() throws Exception {
        doReturn(DIR_MOCK).when(ossOperator).getOssResDir(TENANT_CODE_MOCK);
        doReturn(DIR_MOCK).when(ossOperator).getOssUdfDir(TENANT_CODE_MOCK);
        doReturn(true).when(ossOperator).mkdir(TENANT_CODE_MOCK, DIR_MOCK);
        ossOperator.createTenantDirIfNotExists(TENANT_CODE_MOCK);
        verify(ossOperator, times(2)).mkdir(TENANT_CODE_MOCK, DIR_MOCK);
    }

    @Test
    public void getResDir() {
        final String expectedResourceDir = String.format("dolphinscheduler/%s/resources/", TENANT_CODE_MOCK);
        final String dir = ossOperator.getResDir(TENANT_CODE_MOCK);
        Assert.assertEquals(expectedResourceDir, dir);
    }

    @Test
    public void getUdfDir() {
        final String expectedUdfDir = String.format("dolphinscheduler/%s/udfs/", TENANT_CODE_MOCK);
        final String dir = ossOperator.getUdfDir(TENANT_CODE_MOCK);
        Assert.assertEquals(expectedUdfDir, dir);
    }

    @Test
    public void mkdirWhenDirExists() {
        boolean isSuccess = false;
        try {
            final String key = DIR_MOCK + FOLDER_SEPARATOR;
            doReturn(true).when(ossClientMock).doesObjectExist(BUCKET_NAME_MOCK, key);
            isSuccess = ossOperator.mkdir(TENANT_CODE_MOCK, DIR_MOCK);
            verify(ossClientMock, times(1)).doesObjectExist(BUCKET_NAME_MOCK, key);

        } catch (IOException e) {
            fail("test failed due to unexpected IO exception");
        }

        Assert.assertEquals(true, isSuccess);
    }

    @Test
    public void mkdirWhenDirNotExists() {
        boolean isSuccess = true;
        try {
            final String key = DIR_MOCK + FOLDER_SEPARATOR;
            doReturn(false).when(ossClientMock).doesObjectExist(BUCKET_NAME_MOCK, key);
            doNothing().when(ossOperator).createOssPrefix(BUCKET_NAME_MOCK, key);
            isSuccess = ossOperator.mkdir(TENANT_CODE_MOCK, DIR_MOCK);
            verify(ossClientMock, times(1)).doesObjectExist(BUCKET_NAME_MOCK, key);
            verify(ossOperator, times(1)).createOssPrefix(BUCKET_NAME_MOCK, key);

        } catch (IOException e) {
            fail("test failed due to unexpected IO exception");
        }

        Assert.assertEquals(true, isSuccess);
    }

    @Test
    public void getResourceFileName() {
        final String expectedResourceFileName =
                String.format("dolphinscheduler/%s/resources/%s", TENANT_CODE_MOCK, FILE_NAME_MOCK);
        final String resourceFileName = ossOperator.getResourceFileName(TENANT_CODE_MOCK, FILE_NAME_MOCK);
        assertEquals(expectedResourceFileName, resourceFileName);
    }

    @Test
    public void getFileName() {
        final String expectedFileName =
                String.format("dolphinscheduler/%s/resources/%s", TENANT_CODE_MOCK, FILE_NAME_MOCK);
        final String fileName = ossOperator.getFileName(ResourceType.FILE, TENANT_CODE_MOCK, FILE_NAME_MOCK);
        assertEquals(expectedFileName, fileName);
    }

    @Test
    public void exists() {
        boolean doesExist = false;
        doReturn(true).when(ossClientMock).doesObjectExist(BUCKET_NAME_MOCK, FILE_NAME_MOCK);
        try {
            doesExist = ossOperator.exists(TENANT_CODE_MOCK, FILE_NAME_MOCK);
        } catch (IOException e) {
            fail("unexpected IO exception in unit test");
        }

        Assert.assertEquals(true, doesExist);
        verify(ossClientMock, times(1)).doesObjectExist(BUCKET_NAME_MOCK, FILE_NAME_MOCK);
    }

    @Test
    public void delete() {
        boolean isDeleted = false;
        doReturn(null).when(ossClientMock).deleteObject(anyString(), anyString());
        try {
            isDeleted = ossOperator.delete(TENANT_CODE_MOCK, FILE_NAME_MOCK, true);
        } catch (IOException e) {
            fail("unexpected IO exception in unit test");
        }

        Assert.assertEquals(true, isDeleted);
        verify(ossClientMock, times(1)).deleteObject(anyString(), anyString());
    }

    @Test
    public void copy() {
        boolean isSuccess = false;
        doReturn(null).when(ossClientMock).copyObject(anyString(), anyString(), anyString(), anyString());
        doReturn(null).when(ossClientMock).deleteObject(anyString(), anyString());
        try {
            isSuccess = ossOperator.copy(FILE_PATH_MOCK, FILE_PATH_MOCK, false, false);
        } catch (IOException e) {
            fail("unexpected IO exception in unit test");
        }

        Assert.assertEquals(true, isSuccess);
        verify(ossClientMock, times(1)).copyObject(anyString(), anyString(), anyString(), anyString());
        verify(ossClientMock, times(1)).deleteObject(anyString(), anyString());
    }

    @Test
    public void deleteTenant() {
        doNothing().when(ossOperator).deleteTenantCode(anyString());
        try {
            ossOperator.deleteTenant(TENANT_CODE_MOCK);
        } catch (Exception e) {
            fail("unexpected exception caught in unit test");
        }

        verify(ossOperator, times(1)).deleteTenantCode(anyString());
    }

    @Test
    public void getOssResDir() {
        final String expectedOssResDir = String.format("dolphinscheduler/%s/resources", TENANT_CODE_MOCK);
        final String ossResDir = ossOperator.getOssResDir(TENANT_CODE_MOCK);
        Assert.assertEquals(expectedOssResDir, ossResDir);
    }

    @Test
    public void getOssUdfDir() {
        final String expectedOssUdfDir = String.format("dolphinscheduler/%s/udfs", TENANT_CODE_MOCK);
        final String ossUdfDir = ossOperator.getOssUdfDir(TENANT_CODE_MOCK);
        Assert.assertEquals(expectedOssUdfDir, ossUdfDir);
    }

    @Test
    public void getOssTenantDir() {
        final String expectedOssTenantDir = String.format(FORMAT_S_S, DIR_MOCK, TENANT_CODE_MOCK);
        doReturn(DIR_MOCK).when(ossOperator).getOssDataBasePath();
        final String ossTenantDir = ossOperator.getOssTenantDir(TENANT_CODE_MOCK);
        Assert.assertEquals(expectedOssTenantDir, ossTenantDir);
    }

    @Test
    public void deleteDir() {
        doReturn(true).when(ossClientMock).doesObjectExist(anyString(), anyString());
        ossOperator.deleteDir(DIR_MOCK);
        verify(ossClientMock, times(1)).deleteObject(anyString(), anyString());
    }
}
