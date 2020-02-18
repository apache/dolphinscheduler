package org.apache.dolphinscheduler.server.worker.task.sqoop.generator;

import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;

public interface ITargetGenerator {

    String generate(SqoopParameters sqoopParameters);
}
