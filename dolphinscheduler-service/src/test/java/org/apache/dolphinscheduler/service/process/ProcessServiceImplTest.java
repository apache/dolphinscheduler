package org.apache.dolphinscheduler.service.process;

import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ProcessServiceImplTest {

    @InjectMocks
    private ProcessServiceImpl processService;

    @Mock
    private CommandMapper commandMapper;

    @Test
    public void findCommandPageBySlot() {
        List<Command> commands = generateCommands(11);
        Mockito.when(commandMapper.queryCommandPage(Mockito.anyInt(), Mockito.anyInt())).thenReturn(commands);

        List<Command> result = processService.findCommandPageBySlot(10, 0, 2, 1);
        Assert.assertEquals(5, result.size());
    }

    private List<Command> generateCommands(int size) {
        List<Command> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Command command = new Command();
            command.setId(i);
            result.add(command);
        }
        return result;
    }
}
