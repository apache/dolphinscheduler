package org.apache.dolphinscheduler.server.master.controller;

import org.apache.dolphinscheduler.remote.dto.MasterTaskInstanceDispatchingDto;
import org.apache.dolphinscheduler.server.master.service.ExecutingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/task")
public class TaskInstanceExecutionController {

    @Autowired
    private ExecutingService executingService;

    @GetMapping("/listingDispatchingTaskInstances")
    @ResponseStatus(HttpStatus.OK)
    public List<MasterTaskInstanceDispatchingDto> listingDispatchingData() {
        return executingService.listingDispatchingTaskInstance();
    }
}
