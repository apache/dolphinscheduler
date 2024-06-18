///*
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *    http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.apache.dolphinscheduler.plugin.task.flink;
//
//
//import lombok.extern.slf4j.Slf4j;
//
//import org.apache.flink.configuration.Configuration;
//import org.apache.flink.table.api.DataTypes;
//import org.apache.flink.table.api.EnvironmentSettings;
//import org.apache.flink.table.api.Schema;
//import org.apache.flink.table.api.TableEnvironment;
//import org.apache.flink.table.catalog.CatalogBaseTable;
//import org.apache.flink.table.catalog.CatalogMaterializedTable;
//import org.apache.flink.table.catalog.CatalogStore;
//import org.apache.flink.table.catalog.Column;
//import org.apache.flink.table.catalog.FileCatalogStore;
//import org.apache.flink.table.catalog.IntervalFreshness;
//import org.apache.flink.table.catalog.ObjectPath;
//import org.apache.flink.table.catalog.ResolvedCatalogMaterializedTable;
//import org.apache.flink.table.catalog.ResolvedSchema;
//import org.apache.flink.table.catalog.UniqueConstraint;
//import org.apache.flink.table.file.testutils.catalog.TestFileSystemCatalog;
//import org.apache.http.NameValuePair;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.message.BasicNameValuePair;
//import org.apache.http.util.EntityUtils;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.junit.jupiter.api.io.TempDir;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import static org.apache.flink.connector.file.table.FileSystemConnectorOptions.PATH;
//import static org.apache.flink.table.file.testutils.catalog.TestFileSystemCatalog.DATA_PATH;
//
////@ExtendWith(MockitoExtension.class)
//@Slf4j
//public class FlinkMaterializedTableTaskTest {
//
//   protected static final String TEST_CATALOG = "TestCatalog111";
//   protected static final String TEST_DEFAULT_DATABASE = "TestDb111";
//
//   protected static TestFileSystemCatalog catalog;
//
//   protected static File tempFile;
//
//   private static final Map<String, String> EXPECTED_OPTIONS = new HashMap<>();
//
//   private static final List<Column> CREATE_COLUMNS =
//       Arrays.asList(
//           Column.physical("id", DataTypes.BIGINT()),
//           Column.physical("name", DataTypes.VARCHAR(20)),
//           Column.physical("age", DataTypes.INT()),
//           Column.physical("tss", DataTypes.TIMESTAMP(3)),
//           Column.physical("partition", DataTypes.VARCHAR(10)));
//
//   private static final UniqueConstraint CONSTRAINTS =
//       UniqueConstraint.primaryKey("primary_constraint", Collections.singletonList("id"));
//
//   private static final List<String> PARTITION_KEYS = Collections.singletonList("partition");
//
//   private static final ResolvedSchema CREATE_RESOLVED_SCHEMA =
//       new ResolvedSchema(CREATE_COLUMNS, Collections.emptyList(), CONSTRAINTS);
//
//   private static final Schema CREATE_SCHEMA =
//       Schema.newBuilder().fromResolvedSchema(CREATE_RESOLVED_SCHEMA).build();
//
//   static {
//      EXPECTED_OPTIONS.put("source.monitor-interval", "5S");
//      EXPECTED_OPTIONS.put("auto-compaction", "true");
//   }
//
//   private static final String DEFINITION_QUERY = "SELECT id, region, county FROM T";
//
//   private static final IntervalFreshness FRESHNESS = IntervalFreshness.ofMinute("3");
//
//   private static final ResolvedCatalogMaterializedTable EXPECTED_CATALOG_MATERIALIZED_TABLE =
//       new ResolvedCatalogMaterializedTable(
//           CatalogMaterializedTable.newBuilder()
//               .schema(CREATE_SCHEMA)
//               .comment("test materialized table")
//               .partitionKeys(PARTITION_KEYS)
//               .options(EXPECTED_OPTIONS)
//               .definitionQuery(DEFINITION_QUERY)
//               .freshness(FRESHNESS)
//               .logicalRefreshMode(
//                   CatalogMaterializedTable.LogicalRefreshMode.AUTOMATIC)
//               .refreshMode(CatalogMaterializedTable.RefreshMode.CONTINUOUS)
//               .refreshStatus(CatalogMaterializedTable.RefreshStatus.INITIALIZING)
//               .build(),
//           CREATE_RESOLVED_SCHEMA);
//
//
//   public static void main(String[] args) throws Exception {
////      testHttpRequest();
////      createFlinkMT();
//   }
//
//   private static void testHttpRequest() throws IOException {
//      String result = "";
//      HttpPost post = new HttpPost("http://localhost:8083/v1/sessions");
//
//      // add request parameters or form parameters
////      List<NameValuePair> urlParameters = new ArrayList<>();
////      urlParameters.add(new BasicNameValuePair("username", "abc"));
////      urlParameters.add(new BasicNameValuePair("password", "123"));
////      urlParameters.add(new BasicNameValuePair("custom", "secret"));
////
////      post.setEntity(new UrlEncodedFormEntity(urlParameters));
//
//      try (CloseableHttpClient httpClient = HttpClients.createDefault();
//           CloseableHttpResponse response = httpClient.execute(post)){
//
//         result = EntityUtils.toString(response.getEntity());
//
//         Map<String, String> map = new HashMap<String, String>();
//         ObjectMapper mapper = new ObjectMapper();
//
//         try {
//            //convert JSON string to Map
//            map = mapper.readValue(result, Map.class);
//            final String sessionHandle = map.get("sessionHandle");
//            log.info(sessionHandle);
//         } catch (Exception e) {
//            e.printStackTrace();
//         }
//      }
//   }
//
//   private static void createFlinkMT() throws Exception {
//      tempFile = new File("/Users/alibaba/workplace/metadata");
//      File testDb = new File(tempFile, TEST_DEFAULT_DATABASE);
//      testDb.mkdir();
//
//      String catalogPathStr = tempFile.getAbsolutePath();
//      catalog = new TestFileSystemCatalog(catalogPathStr, TEST_CATALOG, TEST_DEFAULT_DATABASE);
//      catalog.open();
//      List<String> databases = catalog.listDatabases();
//      log.info("list test dbs - {}", databases);
//
//      List<String> tables = catalog.listTables(TEST_DEFAULT_DATABASE);
//      log.info("list test tables - {}", tables);
//
//      ObjectPath tablePath = new ObjectPath(TEST_DEFAULT_DATABASE, "tb2");
//      // test create materialized table
//      catalog.createTable(tablePath, EXPECTED_CATALOG_MATERIALIZED_TABLE, true);
//
//      // test materialized table exist
//      Map<String, String> expectedOptions = new HashMap<>(EXPECTED_OPTIONS);
//      expectedOptions.put(
//          PATH.key(),
//          String.format(
//              "%s/%s/%s/%s",
//              tempFile.getAbsolutePath(),
//              tablePath.getDatabaseName(),
//              tablePath.getObjectName(),
//              DATA_PATH));
//
//      // test get materialized table
//      CatalogBaseTable materializedTable = catalog.getTable(tablePath);
//      log.info("materializedTable - {}", materializedTable);
//
//      catalog.close();
//   }
//
//   private static void createCsvTable() {
//      TableEnvironment tEnv =
//          TableEnvironment.create(EnvironmentSettings.inBatchMode());
//      tEnv.registerCatalog(TEST_CATALOG, catalog);
//      tEnv.useCatalog(TEST_CATALOG);
//
//      tEnv.executeSql(
//          "CREATE TABLE CsvTable (\n"
//              + "  id BIGINT,\n"
//              + "  user_name STRING,\n"
//              + "  message STRING,\n"
//              + "  log_ts STRING\n"
//              + ") WITH (\n"
//              + "  'format' = 'csv'\n"
//              + ")");
//
//      tEnv.getConfig().getConfiguration().setString("parallelism.default", "1");
//      tEnv.executeSql(
//              String.format(
//                  "INSERT INTO %s.%s.CsvTable VALUES\n"
//                      + "(1001, 'user1', 'hello world', '2021-06-10 10:00:00'),\n"
//                      + "(1002, 'user2', 'hi', '2021-06-10 10:01:00'),\n"
//                      + "(1003, 'user3', 'ciao', '2021-06-10 10:02:00'),\n"
//                      + "(1004, 'user4', '你好', '2021-06-10 10:03:00')",
//                  TEST_CATALOG, TEST_DEFAULT_DATABASE))
//          .await();
//
//      CloseableIterator<Row> result =
//          tEnv.executeSql(
//                  String.format(
//                      "SELECT * FROM %s.%s.CsvTable",
//                      TEST_CATALOG, TEST_DEFAULT_DATABASE))
//              .collect();
//
//   }
//
//}
