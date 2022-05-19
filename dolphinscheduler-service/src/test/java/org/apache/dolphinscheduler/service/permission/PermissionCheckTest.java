package org.apache.dolphinscheduler.service.permission;

import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.service.exceptions.ServiceException;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import static org.powermock.api.mockito.PowerMockito.when;

public class PermissionCheckTest {

    private ProcessService processService;
    private static final Logger logger = LoggerFactory.getLogger(PermissionCheckTest.class);

    @Before
    public void before() throws Exception {
        processService = PowerMockito.mock(ProcessService.class);
    }

    @Test(expected = ServiceException.class)
    public void testCheckPermissionExceptionNullUser() throws ServiceException {
        Integer[] arr = {1,2};
        PermissionCheck<Integer> permissionCheck = new PermissionCheck(AuthorizationType.RESOURCE_FILE_ID, processService, arr, 1, logger);
        when(processService.getUserById(1)).thenReturn(null);
        permissionCheck.checkPermission();
    }
}
