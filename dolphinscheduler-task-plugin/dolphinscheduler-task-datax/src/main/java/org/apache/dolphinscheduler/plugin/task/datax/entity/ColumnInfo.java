package org.apache.dolphinscheduler.plugin.task.datax.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColumnInfo {
    /**
     * index of the columns in DDL
     */
    private int index;

    /**
     * column name
     */
    private String columnName;

    /**
     * sql type
     */
    private String dataType;

    /**
     * whether included in dataX job
     */
    private boolean enable;

    /**
     * json to describe column info
     */
    private String json;

    public ColumnInfo(int index, String columnName, String dataType) {
        this.index = index;
        this.columnName = columnName;
        this.dataType = dataType;
    }
}
