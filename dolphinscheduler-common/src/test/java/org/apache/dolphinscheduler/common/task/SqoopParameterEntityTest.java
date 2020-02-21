package org.apache.dolphinscheduler.common.task;

import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.common.task.sqoop.sources.SourceHdfsParameter;
import org.apache.dolphinscheduler.common.task.sqoop.sources.SourceHiveParameter;
import org.apache.dolphinscheduler.common.task.sqoop.sources.SourceMysqlParameter;
import org.apache.dolphinscheduler.common.task.sqoop.targets.TargetHdfsParameter;
import org.apache.dolphinscheduler.common.task.sqoop.targets.TargetHiveParameter;
import org.apache.dolphinscheduler.common.task.sqoop.targets.TargetMysqlParameter;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author simfo
 * @date 2020/2/21 13:41
 */
public class SqoopParameterEntityTest {

    @Test
    public void testEntity(){
        try {
            List<Class> classList = new ArrayList<>();
            classList.add(SourceMysqlParameter.class);
            classList.add(SourceHiveParameter.class);
            classList.add(SourceHdfsParameter.class);
            classList.add(SqoopParameters.class);
            classList.add(TargetMysqlParameter.class);
            classList.add(TargetHiveParameter.class);
            classList.add(TargetHdfsParameter.class);
            EntityTestUtils.run(classList);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
