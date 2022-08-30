package org.apache.dolphinscheduler.remote.command.log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAppIdRequestCommand implements Serializable {

    private String logPath;

    public Command convert2Command() {
        Command command = new Command();
        command.setType(CommandType.GET_APP_ID_REQUEST);
        byte[] body = JSONUtils.toJsonByteArray(this);
        command.setBody(body);
        return command;
    }

}
