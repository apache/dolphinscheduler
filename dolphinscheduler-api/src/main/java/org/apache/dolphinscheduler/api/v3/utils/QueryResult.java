package org.apache.dolphinscheduler.api.v3.utils;

import lombok.Data;

import java.util.List;

@Data
public class QueryResult<T> {

    List<T> data;

    String nextToken;

    int totalSize;
}
