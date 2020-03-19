package org.apache.dolphinscheduler.api.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.dolphinscheduler.dao.entity.UserAlertGroup;
import org.apache.dolphinscheduler.dao.mapper.UserAlertGroupMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created on 2020/3/19.
 *
 * @author flowkr90@gmail.com
 */
@Service
public class UserAlertGroupService extends ServiceImpl<UserAlertGroupMapper, UserAlertGroup> {

	@Autowired
	private UserAlertGroupMapper userAlertGroupMapper;

	boolean deleteByAlertGroupId(Integer groupId) {
		return userAlertGroupMapper.deleteByAlertgroupId(groupId) >= 1;
	}

}
