package org.apache.dolphinscheduler.server.worker.task.sqoop;

public final class SqoopConstants {

    //sqoop general param
    public static final String SQOOP = "sqoop";
    public static final String SQOOP_MR_JOB_NAME = "mapred.job.name";
    public static final String SQOOP_PARALLELISM = "-m";
    public static final String FIELDS_TERMINATED_BY = "--fields-terminated-by";
    public static final String LINES_TERMINATED_BY = "--lines-terminated-by";
    public static final String FIELD_NULL_PLACEHOLDER = "--null-non-string 'NULL' --null-string 'NULL'";

    //sqoop db
    public static final String DB_CONNECT = "--connect";
    public static final String DB_USERNAME = "--username";
    public static final String DB_PWD = "--password";
    public static final String TABLE = "--table";
    public static final String COLUMNS = "--columns";
    public static final String QUERY_WHERE = "where";
    public static final String QUERY = "--query";
    public static final String QUERY_CONDITION = "AND \\$CONDITIONS";
    public static final String QUERY_WITHOUT_CONDITION = "WHERE \\$CONDITIONS";
    public static final String QUOTATION_MARKS = "\"";
    public static final String MAP_COLUMN_HIVE = "--map-column-hive";
    public static final String MAP_COLUMN_JAVA = "--map-column-java";

    //sqoop hive source
    public static final String HCATALOG_DATABASE = "--hcatalog-database";
    public static final String HCATALOG_TABLE = "--hcatalog-table";
    public static final String HCATALOG_PARTITION_KEYS = "--hcatalog-partition-keys";
    public static final String HCATALOG_PARTITION_VALUES = "--hcatalog-partition-values";

    //sqoop hdfs
    public static final String HDFS_EXPORT_DIR = "--export-dir";
    public static final String TARGET_DIR = "--target-dir";
    public static final String COMPRESSION_CODEC = "--compression-codec";

    //sqoop hive
    public static final String HIVE_IMPORT = "--hive-import";
    public static final String HIVE_TABLE = "--hive-table";
    public static final String CREATE_HIVE_TABLE = "--create-hive-table";
    public static final String HIVE_DROP_IMPORT_DELIMS = "--hive-drop-import-delims";
    public static final String HIVE_OVERWRITE = "--hive-overwrite";
    public static final String DELETE_TARGET_DIR = "--delete-target-dir";
    public static final String HIVE_DELIMS_REPLACEMENT = "--hive-delims-replacement";
    public static final String HIVE_PARTITION_KEY = "--hive-partition-key";
    public static final String HIVE_PARTITION_VALUE = "--hive-partition-value";

    //sqoop update model
    public static final String UPDATE_KEY = "--update-key";
    public static final String UPDATE_MODE = "--update-mode";



}
