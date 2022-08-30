package org.apache.dolphinscheduler.remote.command.log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAppIdResponseCommand implements Serializable {

    private List<String> appIds;

    public Command convert2Command(long opaque) {
        Command command = new Command(opaque);
        command.setType(CommandType.GET_APP_ID_RESPONSE);
        byte[] body = JSONUtils.toJsonByteArray(this);
        command.setBody(body);
        return command;
    }
}
