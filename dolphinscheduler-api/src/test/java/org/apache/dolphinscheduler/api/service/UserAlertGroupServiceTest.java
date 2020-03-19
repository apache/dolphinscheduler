package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.dao.mapper.UserAlertGroupMapper;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Created on 2020/3/19.
 *
 * @author flowkr90gmail.com
 */
@RunWith(MockitoJUnitRunner.class)
public class UserAlertGroupServiceTest {

	@InjectMocks
	UserAlertGroupService userAlertGroupService;

	@Mock
	UserAlertGroupMapper userAlertGroupMapper;

	@Test
	public void deleteByAlertGroupId() {

		Integer groupId = 1;
		userAlertGroupService.deleteByAlertGroupId(groupId);
		ArgumentCaptor<Integer> argumentCaptor = ArgumentCaptor.forClass(Integer.class);

		Mockito.verify(userAlertGroupMapper).deleteByAlertgroupId(argumentCaptor.capture());
		assertEquals(argumentCaptor.getValue(), groupId);

	}

}