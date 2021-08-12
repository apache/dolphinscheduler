package org.apache.dolphinscheduler.graphql.datafetcher.service;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.graphql.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

@Service
public class UserArgumentService {

    @Autowired
    private UserMapper userMapper;

    public Result getUserFromArgument(LinkedHashMap<String, String> loginUserMap) {
        int id = Integer.parseInt(loginUserMap.get("id"));
        Result result = new Result();

        User user = userMapper.selectById(id);

        if (user == null) {
            ResultUtil.putStatus(result, Status.USER_NOT_EXIST, id);
            return result;
        }

        result.setData(user);
        ResultUtil.putStatus(result, Status.SUCCESS);
        return result;
    }



}
