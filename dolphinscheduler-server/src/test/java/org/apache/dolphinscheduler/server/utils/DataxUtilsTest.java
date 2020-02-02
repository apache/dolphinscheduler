package org.apache.dolphinscheduler.server.utils;

import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * DataxUtils Tester.
 *
 * @author wenhemin
 * @since <pre>一月 31, 2020</pre>
 * @version 1.0
 */
public class DataxUtilsTest {

    /**
     *
     * Method: getReaderPluginName(DbType dbType)
     *
     */
    @Test
    public void testGetReaderPluginName() {
        assertEquals(DataxUtils.DATAX_READER_PLUGIN_MYSQL, DataxUtils.getReaderPluginName(DbType.MYSQL));
    }

    /**
     *
     * Method: getWriterPluginName(DbType dbType)
     *
     */
    @Test
    public void testGetWriterPluginName() {
        assertEquals(DataxUtils.DATAX_WRITER_PLUGIN_MYSQL, DataxUtils.getWriterPluginName(DbType.MYSQL));
    }

    /**
     *
     * Method: getSqlStatementParser(DbType dbType, String sql)
     *
     */
    @Test
    public void testGetSqlStatementParser() throws Exception {
        assertTrue(DataxUtils.getSqlStatementParser(DbType.MYSQL, "select 1") instanceof MySqlStatementParser);
    }

    /**
     *
     * Method: convertKeywordsColumns(DbType dbType, String[] columns)
     *
     */
    @Test
    public void testConvertKeywordsColumns() throws Exception {
        String[] fromColumns = new String[]{"`select`", "from", "\"where\"", " table "};
        String[] targetColumns = new String[]{"`select`", "`from`", "`where`", "`table`"};

        String[] toColumns = DataxUtils.convertKeywordsColumns(DbType.MYSQL, fromColumns);

        assertTrue(fromColumns.length == toColumns.length);

        for (int i = 0; i < toColumns.length; i++) {
            assertEquals(toColumns[i], targetColumns[i]);
        }
    }

    /**
     *
     * Method: doConvertKeywordsColumn(DbType dbType, String column)
     *
     */
    @Test
    public void testDoConvertKeywordsColumn() throws Exception {
        String fromColumn = " \"`select`\" ";
        String targetColumn = "`select`";

        assertEquals(DataxUtils.doConvertKeywordsColumn(DbType.MYSQL, fromColumn), targetColumn);
    }
}
