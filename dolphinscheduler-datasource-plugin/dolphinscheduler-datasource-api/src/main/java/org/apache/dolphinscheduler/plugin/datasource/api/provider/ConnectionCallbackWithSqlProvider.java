package org.apache.dolphinscheduler.plugin.datasource.api.provider;

import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.SqlProvider;

public interface ConnectionCallbackWithSqlProvider<T> extends ConnectionCallback<T>, SqlProvider {
}
