package org.apache.dolphinscheduler.common.utils;

import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.task.sql.SqlParameters;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class DependUnionKeyUtilsTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testClearIllegalChars() {
        Assert.assertEquals("abc,", DependUnionKeyUtils.clearIllegalChars("a b \t c \n，"));
        Assert.assertEquals(null, DependUnionKeyUtils.clearIllegalChars(null));
    }

    @Test
    public void testRemoveMarkWord() {
        Assert.assertEquals("127.0.0.1:db1:table1", DependUnionKeyUtils.removeMarkWord("127.0.0.1:db1:table1#depend"));
        Assert.assertEquals("127.0.0.1:db1:table2", DependUnionKeyUtils.removeMarkWord("127.0.0.1:db1:table2#target"));
        Assert.assertEquals(null, DependUnionKeyUtils.removeMarkWord(null));
    }

    @Test
    public void testBuildDependTableUnionKey1() {
        Assert.assertEquals("127.0.0.1:db1:table1#depend", DependUnionKeyUtils.buildDependTableUnionKey("127.0.0.1", "db1", "table1"));
    }

    @Test
    public void testBuildDependTableUnionKey2() {
        Assert.assertEquals("127.0.0.1:db1:table1#depend", DependUnionKeyUtils.buildDependTableUnionKey("127.0.0.1", null, "db1.table1"));
    }

    @Test
    public void testBuildDependTableUnionKey3() {
        Assert.assertEquals("127.0.0.1:db1:table1#depend,127.0.0.1:db1:table2#depend", DependUnionKeyUtils.buildDependTableUnionKey("127.0.0.1", "db1", Arrays.asList("table1", "table2")));

        List<String> list = null;
        Assert.assertEquals(null, DependUnionKeyUtils.buildDependTableUnionKey("127.0.0.1", "db1", list));
        Assert.assertEquals("127.0.0.1:db1:table1#depend,", DependUnionKeyUtils.buildDependTableUnionKey("127.0.0.1", "db1", Arrays.asList("table1", null)));
    }

    @Test
    public void testBuildTargetTableUnionKey1() {
        Assert.assertEquals("127.0.0.1:db1:table1#target", DependUnionKeyUtils.buildTargetTableUnionKey("127.0.0.1", "db1", "table1"));
    }

    @Test
    public void testBuildTargetTableUnionKey2() {
        Assert.assertEquals("127.0.0.1:db1:table1#target", DependUnionKeyUtils.buildTargetTableUnionKey("127.0.0.1", null, "db1.table1"));
    }

    @Test
    public void testBuildTargetTableUnionKey3() {
        Assert.assertEquals("127.0.0.1:db1:table1#target,127.0.0.1:db1:table2#target", DependUnionKeyUtils.buildTargetTableUnionKey("127.0.0.1", "db1", Arrays.asList("table1", "table2")));
    }

    @Test
    public void testBuildTargetTableUnionKey4() {
        Assert.assertEquals("127.0.0.1:db1:table1#target,127.0.0.1:db1:table2#target", DependUnionKeyUtils.buildTargetTableUnionKey("127.0.0.1", "db1", new String[]{"table1", "table2"}));
    }

    @Test
    public void testExistDependRelation1() {
        TaskNode taskNode = new TaskNode();
        taskNode.setType(TaskType.SQL.toString());
        SqlParameters sqlParameters = new SqlParameters();
        sqlParameters.setTargetNodeKeys("127.0.0.1:db1:table1#target");
        taskNode.setParams(JSONUtils.toJsonString(sqlParameters));
        Assert.assertTrue(DependUnionKeyUtils.existDependRelation(taskNode, new String[]{"127.0.0.1:db1:table1#depend"}));

        Assert.assertTrue(!DependUnionKeyUtils.existDependRelation(taskNode, null));
    }

    @Test
    public void testExistDependRelation2() {
        Assert.assertTrue(DependUnionKeyUtils.existDependRelation("127.0.0.1:db1:table1#target", new String[]{"127.0.0.1:db1:table1#depend"}));
        Assert.assertTrue(!DependUnionKeyUtils.existDependRelation("127.0.0.1:db1:table2#target", new String[]{"127.0.0.1:db1:table1#depend"}));
        Assert.assertTrue(!DependUnionKeyUtils.existDependRelation("127.0.0.1:db1:table2#target", null));
    }

    @Test
    public void testReplaceMarkWordToTarget1() {
        Assert.assertArrayEquals(new String[]{"127.0.0.1:db1:table1#target"}, DependUnionKeyUtils.replaceMarkWordToTarget(new String[]{"127.0.0.1:db1:table1#depend"}));

        String[] array = null;
        Assert.assertArrayEquals(null, DependUnionKeyUtils.replaceMarkWordToTarget(array));
    }

    @Test
    public void testReplaceMarkWordToTarget2() {
        Assert.assertEquals("127.0.0.1:db1:table1#target", DependUnionKeyUtils.replaceMarkWordToTarget("127.0.0.1:db1:table1#depend"));
    }
}