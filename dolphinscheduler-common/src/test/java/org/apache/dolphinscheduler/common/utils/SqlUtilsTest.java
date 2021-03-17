/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dolphinscheduler.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;


public class SqlUtilsTest {

    @Test
    public void sqlWithComments() {
        String sql = " -- start comment line \n" +
                "select * from test.abc;" +
                "select * from test.bcd;";
        List<String> sqlStatementsExpect = new ArrayList<>();
        sqlStatementsExpect.add((" -- start comment line \n" +
                "select * from test.abc").trim());
        sqlStatementsExpect.add("select * from test.bcd".trim());

        List<String> sqlStatements = SqlUtils.splitStatements(sql);
        Assert.assertEquals(sqlStatementsExpect.size(), sqlStatements.size());
        Assert.assertTrue(CollectionUtils.equalLists(sqlStatementsExpect, sqlStatements));
    }

    @Test
    public void sqlWithSingleQuotesAndSemicolons() {
        String sql = "SELECT CONCAT('11',';','33'); select concat('', ';', '');\n" + "select concat('1000', ';', '1000');";
        List<String> sqlStatementsExpect = new ArrayList<>();
        sqlStatementsExpect.add("SELECT CONCAT('11',';','33')");
        sqlStatementsExpect.add("select concat('', ';', '')");
        sqlStatementsExpect.add("select concat('1000', ';', '1000')");

        List<String> sqlStatements = SqlUtils.splitStatements(sql);
        Assert.assertEquals(sqlStatementsExpect.size(), sqlStatements.size());
        Assert.assertTrue(CollectionUtils.equalLists(sqlStatementsExpect, sqlStatements));

    }

    @Test
    public void sqlWithMixQuotesAndSemicolons() {
        String sql = "SELECT CONCAT('11',\";'\",'33'); select concat(\"'\", ';', \"'\");\n" + "select concat('1000', \"';\", '1000')";
        ;
        List<String> sqlStatementsExpect = new ArrayList<>();
        sqlStatementsExpect.add("SELECT CONCAT('11',\";'\",'33')");
        sqlStatementsExpect.add("select concat(\"'\", ';', \"'\")");
        sqlStatementsExpect.add("select concat('1000', \"';\", '1000')");

        List<String> sqlStatements = SqlUtils.splitStatements(sql);
        Assert.assertEquals(sqlStatementsExpect.size(), sqlStatements.size());
        Assert.assertTrue(CollectionUtils.equalLists(sqlStatementsExpect, sqlStatements));
    }

    @Test
    public void sqlMixSimple() {
        String sql = "-- sql 1: \n SELECT CONCAT('11',\";'\",'33'); -- sql 2: \n select concat(\"'\", ';', \"'\");\n" + " -- sql 3: \n select concat('1000', \"';\", '1000')";
        List<String> sqlStatementsExpect = new ArrayList<>();
        sqlStatementsExpect.add("-- sql 1: \n SELECT CONCAT('11',\";'\",'33')");
        sqlStatementsExpect.add("-- sql 2: \n select concat(\"'\", ';', \"'\")");
        sqlStatementsExpect.add("-- sql 3: \n select concat('1000', \"';\", '1000')");

        List<String> sqlStatements = SqlUtils.splitStatements(sql);
        Assert.assertEquals(sqlStatementsExpect.size(), sqlStatements.size());
        Assert.assertTrue(CollectionUtils.equalLists(sqlStatementsExpect, sqlStatements));
    }


    @Test
    public void sqlMixComplex() {
        String sql = "select concat(\"';\", \"c\", 'a') ;; select * from b ; ; " +
                "SELECT \n" +
                "\tcountry.country_name_eng,\n" +
                "\tSUM(CASE WHEN call.id IS NOT NULL THEN 1 ELSE 0 END) AS calls,\n" +
                "\tAVG(ISNULL(DATEDIFF(SECOND, call.start_time, call.end_time),0)) AS avg_difference\n" +
                "FROM country \n" +
                "LEFT JOIN city ON city.country_id = country.id\n" +
                "LEFT JOIN customer ON city.id = customer.city_id\n" +
                "LEFT JOIN call ON call.customer_id = customer.id\n" +
                "GROUP BY \n" +
                "\tcountry.id,\n" +
                "\tcountry.country_name_eng\n" +
                "HAVING AVG(ISNULL(DATEDIFF(SECOND, call.start_time, call.end_time),0)) > (SELECT AVG(DATEDIFF(SECOND, call.start_time, call.end_time)) FROM call)\n" +
                "ORDER BY calls DESC, country.id ASC;";
        List<String> sqlStatementsExpect = new ArrayList<>();
        sqlStatementsExpect.add("select concat(\"';\", \"c\", 'a')");
        sqlStatementsExpect.add("select * from b");
        sqlStatementsExpect.add("SELECT \n" +
                "\tcountry.country_name_eng,\n" +
                "\tSUM(CASE WHEN call.id IS NOT NULL THEN 1 ELSE 0 END) AS calls,\n" +
                "\tAVG(ISNULL(DATEDIFF(SECOND, call.start_time, call.end_time),0)) AS avg_difference\n" +
                "FROM country \n" +
                "LEFT JOIN city ON city.country_id = country.id\n" +
                "LEFT JOIN customer ON city.id = customer.city_id\n" +
                "LEFT JOIN call ON call.customer_id = customer.id\n" +
                "GROUP BY \n" +
                "\tcountry.id,\n" +
                "\tcountry.country_name_eng\n" +
                "HAVING AVG(ISNULL(DATEDIFF(SECOND, call.start_time, call.end_time),0)) > (SELECT AVG(DATEDIFF(SECOND, call.start_time, call.end_time)) FROM call)\n" +
                "ORDER BY calls DESC, country.id ASC");

        List<String> sqlStatements = SqlUtils.splitStatements(sql);
        Assert.assertEquals(sqlStatementsExpect.size(), sqlStatements.size());
        Assert.assertTrue(CollectionUtils.equalLists(sqlStatementsExpect, sqlStatements));
    }


}
