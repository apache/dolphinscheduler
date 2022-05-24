import com.google.auto.service.AutoService;
import org.apache.dolphinscheduler.plugin.task.api.TaskChannel;
import org.apache.dolphinscheduler.plugin.task.api.TaskChannelFactory;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;

import java.util.List;

@AutoService(TaskChannelFactory.class)
public class JavaTaskChannelFactory implements TaskChannelFactory {
    @Override
    public TaskChannel create() {
        return new JavaTaskChannel();
    }

    @Override
    public String getName() {
        return "JAVA";
    }

    @Override
    public List<PluginParams> getParams() {
        return null;
    }
}
