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
package org.apache.dolphinscheduler.server.worker.task.sqoop;

import com.alibaba.fastjson.JSONObject;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.server.worker.task.ShellCommandExecutor;
import org.apache.dolphinscheduler.server.worker.task.TaskProps;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.SqoopJobGenerator;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

/**
 * @author simfo
 * @date 2020/2/17 15:05
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class SqoopTaskTest {

    private static final Logger logger = LoggerFactory.getLogger(SqoopTaskTest.class);

    private ProcessService processService;
    private ApplicationContext applicationContext;
    private SqoopTask sqoopTask;

    @Before
    public void before() throws Exception{
        processService = Mockito.mock(ProcessService.class);
        Mockito.when(processService.findDataSourceById(2)).thenReturn(getDataSource());
        applicationContext = Mockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);
        Mockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);

        TaskProps props = new TaskProps();
        props.setTaskDir("/tmp");
        props.setTaskAppId(String.valueOf(System.currentTimeMillis()));
        props.setTaskInstId(1);
        props.setTenantCode("1");
        props.setEnvFile(".dolphinscheduler_env.sh");
        props.setTaskStartTime(new Date());
        props.setTaskTimeout(0);
        props.setTaskParams("{\"concurrency\":1,\"modelType\":\"import\",\"sourceType\":\"MYSQL\",\"targetType\":\"HIVE\",\"sourceParams\":\"{\\\"srcDatasource\\\":2,\\\"srcTable\\\":\\\"person_2\\\",\\\"srcQueryType\\\":\\\"1\\\",\\\"srcQuerySql\\\":\\\"SELECT * FROM person_2\\\",\\\"srcColumnType\\\":\\\"0\\\",\\\"srcColumns\\\":\\\"\\\",\\\"srcConditionList\\\":[],\\\"mapColumnHive\\\":[],\\\"mapColumnJava\\\":[{\\\"prop\\\":\\\"id\\\",\\\"direct\\\":\\\"IN\\\",\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"Integer\\\"}]}\",\"targetParams\":\"{\\\"hiveDatabase\\\":\\\"stg\\\",\\\"hiveTable\\\":\\\"person_internal_2\\\",\\\"createHiveTable\\\":true,\\\"dropDelimiter\\\":false,\\\"hiveOverWrite\\\":true,\\\"replaceDelimiter\\\":\\\"\\\",\\\"hivePartitionKey\\\":\\\"date\\\",\\\"hivePartitionValue\\\":\\\"2020-02-16\\\"}\",\"localParams\":[]}");

        sqoopTask = new SqoopTask(props,logger);
        sqoopTask.init();
    }

    @Test
    public void testGenerator(){
        String data1 = "{\"concurrency\":1,\"modelType\":\"import\",\"sourceType\":\"MYSQL\",\"targetType\":\"HDFS\",\"sourceParams\":\"{\\\"srcDatasource\\\":2,\\\"srcTable\\\":\\\"person_2\\\",\\\"srcQueryType\\\":\\\"0\\\",\\\"srcQuerySql\\\":\\\"\\\",\\\"srcColumnType\\\":\\\"0\\\",\\\"srcColumns\\\":\\\"\\\",\\\"srcConditionList\\\":[],\\\"mapColumnHive\\\":[],\\\"mapColumnJava\\\":[]}\",\"targetParams\":\"{\\\"targetPath\\\":\\\"/ods/tmp/test/person7\\\",\\\"deleteTargetDir\\\":true,\\\"fileType\\\":\\\"--as-textfile\\\",\\\"compressionCodec\\\":\\\"\\\",\\\"fieldsTerminated\\\":\\\"@\\\",\\\"linesTerminated\\\":\\\"\\\\\\\\n\\\"}\",\"localParams\":[]}";
        SqoopParameters sqoopParameters1 = JSONObject.parseObject(data1,SqoopParameters.class);

        SqoopJobGenerator generator = new SqoopJobGenerator();
        String script = generator.generateSqoopJob(sqoopParameters1);
        String expected = "sqoop import -m 1 --connect jdbc:mysql://192.168.0.111:3306/test --username kylo --password 123456 --table person_2 --target-dir /ods/tmp/test/person7 --as-textfile --delete-target-dir --fields-terminated-by '@' --lines-terminated-by '\\n' --null-non-string 'NULL' --null-string 'NULL'";
        Assert.assertEquals(expected, script);

        String data2 = "{\"concurrency\":1,\"modelType\":\"export\",\"sourceType\":\"HDFS\",\"targetType\":\"MYSQL\",\"sourceParams\":\"{\\\"exportDir\\\":\\\"/ods/tmp/test/person7\\\"}\",\"targetParams\":\"{\\\"targetDatasource\\\":2,\\\"targetTable\\\":\\\"person_3\\\",\\\"targetColumns\\\":\\\"id,name,age,sex,create_time\\\",\\\"preQuery\\\":\\\"\\\",\\\"isUpdate\\\":true,\\\"targetUpdateKey\\\":\\\"id\\\",\\\"targetUpdateMode\\\":\\\"allowinsert\\\",\\\"fieldsTerminated\\\":\\\"@\\\",\\\"linesTerminated\\\":\\\"\\\\\\\\n\\\"}\",\"localParams\":[]}";
        SqoopParameters sqoopParameters2 = JSONObject.parseObject(data2,SqoopParameters.class);

        String script2 = generator.generateSqoopJob(sqoopParameters2);
        String expected2 = "sqoop export -m 1 --export-dir /ods/tmp/test/person7 --connect jdbc:mysql://192.168.0.111:3306/test --username kylo --password 123456 --table person_3 --columns id,name,age,sex,create_time --fields-terminated-by '@' --lines-terminated-by '\\n' --update-key id --update-mode allowinsert";
        Assert.assertEquals(expected2, script2);

        String data3 = "{\"concurrency\":1,\"modelType\":\"export\",\"sourceType\":\"HIVE\",\"targetType\":\"MYSQL\",\"sourceParams\":\"{\\\"hiveDatabase\\\":\\\"stg\\\",\\\"hiveTable\\\":\\\"person_internal\\\",\\\"hivePartitionKey\\\":\\\"date\\\",\\\"hivePartitionValue\\\":\\\"2020-02-17\\\"}\",\"targetParams\":\"{\\\"targetDatasource\\\":2,\\\"targetTable\\\":\\\"person_3\\\",\\\"targetColumns\\\":\\\"\\\",\\\"preQuery\\\":\\\"\\\",\\\"isUpdate\\\":false,\\\"targetUpdateKey\\\":\\\"\\\",\\\"targetUpdateMode\\\":\\\"allowinsert\\\",\\\"fieldsTerminated\\\":\\\"@\\\",\\\"linesTerminated\\\":\\\"\\\\\\\\n\\\"}\",\"localParams\":[]}";
        SqoopParameters sqoopParameters3 = JSONObject.parseObject(data3,SqoopParameters.class);

        String script3 = generator.generateSqoopJob(sqoopParameters3);
        String expected3 = "sqoop export -m 1 --hcatalog-database stg --hcatalog-table person_internal --hcatalog-partition-keys date --hcatalog-partition-values 2020-02-17 --connect jdbc:mysql://192.168.0.111:3306/test --username kylo --password 123456 --table person_3 --fields-terminated-by '@' --lines-terminated-by '\\n'";
        Assert.assertEquals(expected3, script3);

        String data4 = "{\"concurrency\":1,\"modelType\":\"import\",\"sourceType\":\"MYSQL\",\"targetType\":\"HIVE\",\"sourceParams\":\"{\\\"srcDatasource\\\":2,\\\"srcTable\\\":\\\"person_2\\\",\\\"srcQueryType\\\":\\\"1\\\",\\\"srcQuerySql\\\":\\\"SELECT * FROM person_2\\\",\\\"srcColumnType\\\":\\\"0\\\",\\\"srcColumns\\\":\\\"\\\",\\\"srcConditionList\\\":[],\\\"mapColumnHive\\\":[],\\\"mapColumnJava\\\":[{\\\"prop\\\":\\\"id\\\",\\\"direct\\\":\\\"IN\\\",\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"Integer\\\"}]}\",\"targetParams\":\"{\\\"hiveDatabase\\\":\\\"stg\\\",\\\"hiveTable\\\":\\\"person_internal_2\\\",\\\"createHiveTable\\\":true,\\\"dropDelimiter\\\":false,\\\"hiveOverWrite\\\":true,\\\"replaceDelimiter\\\":\\\"\\\",\\\"hivePartitionKey\\\":\\\"date\\\",\\\"hivePartitionValue\\\":\\\"2020-02-16\\\"}\",\"localParams\":[]}";
        SqoopParameters sqoopParameters4 = JSONObject.parseObject(data4,SqoopParameters.class);

        String script4 = generator.generateSqoopJob(sqoopParameters4);
        String expected4 = "sqoop import -m 1 --connect jdbc:mysql://192.168.0.111:3306/test --username kylo --password 123456 --query 'SELECT * FROM person_2 WHERE $CONDITIONS' --map-column-java id=Integer --hive-import  --hive-table stg.person_internal_2 --create-hive-table --hive-overwrite -delete-target-dir --hive-partition-key date --hive-partition-value 2020-02-16";
        Assert.assertEquals(expected4, script4);

    }

    private DataSource getDataSource() {
        DataSource dataSource = new DataSource();
        dataSource.setType(DbType.MYSQL);
        dataSource.setConnectionParams(
                "{\"address\":\"jdbc:mysql://192.168.0.111:3306\",\"database\":\"test\",\"jdbcUrl\":\"jdbc:mysql://192.168.0.111:3306/test\",\"user\":\"kylo\",\"password\":\"123456\"}");
        dataSource.setUserId(1);
        return dataSource;
    }

    @Test
    public void testGetParameters() throws Exception {
        Assert.assertNotNull(sqoopTask.getParameters());
    }

    /**
     * Method: init
     */
    @Test
    public void testInit() throws Exception {
        try {
            sqoopTask.init();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    //要测试的包
    private static final String PACKAGE_NAME = "org.apache.dolphinscheduler.common.task.sqoop";
    // 需要过滤的一些特殊的类方法
    private static final List<String> filterClazzMethodList = new ArrayList<String>();
    // 需要过滤的一些特殊的类属性
    private static final List<String> filterClazzFieldList = new ArrayList<String>();
    // 过滤一些特殊类--在类加载阶段就会过滤
    private static final List<String> filterClazzList = new ArrayList<String>();
    static {
        filterClazzFieldList.add("");
        // ==============================================================================分割线

        filterClazzMethodList.add("getClass");
        filterClazzMethodList.add("notify");
        filterClazzMethodList.add("notifyAll");
        filterClazzMethodList.add("wait");
        filterClazzMethodList.add("equals");
        filterClazzMethodList.add("hashCode");
        filterClazzMethodList.add("clone");


        // ================================================================================分割线
        filterClazzList.add("");
    }

    @Test
    public void test() {

        List<Class<?>> allClass = getClasses(PACKAGE_NAME);
        if (null != allClass) {
            for (Class classes : allClass) {// 循环反射执行所有类
                try {

                    boolean isAbstract = Modifier.isAbstract(classes.getModifiers());
                    if (classes.isInterface() || isAbstract) {// 如果是接口或抽象类,跳过
                        continue;
                    }
                    Constructor[] constructorArr = classes.getConstructors();
                    Object clazzObj = newConstructor(constructorArr, classes);

                    fieldTest(classes, clazzObj);

                    methodInvoke(classes, clazzObj);
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (SecurityException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private void fieldTest(Class<?> classes, Object clazzObj)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (null == clazzObj) {
            return;
        }

        String clazzName = classes.getName();
        Field[] fields = classes.getDeclaredFields();
        if (null != fields && fields.length > 0) {
            for (Field field : fields) {
                String fieldName = field.getName();
                if (filterClazzFieldList.contains(clazzName + "." + fieldName)) {
                    continue;
                }
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                Object fieldGetObj = field.get(clazzObj);
                if (!Modifier.isFinal(field.getModifiers()) || null == fieldGetObj) {
                    field.set(clazzObj, adaptorGenObj(field.getType()));
                }
            }
        }
    }

    private void methodInvoke(Class<?> classes, Object clazzObj)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
        String clazzName = classes.getName();
        Method[] methods = classes.getDeclaredMethods();
        if (null != methods && methods.length > 0) {
            for (Method method : methods) {
                String methodName = method.getName();
                String clazzMethodName = clazzName + "." + methodName;
                // 排除无法处理方法
                if (filterClazzMethodList.contains(clazzMethodName)) {
                    continue;
                }
                // 无论如何，先把权限放开
                method.setAccessible(true);
                Class<?>[] paramClassArrs = method.getParameterTypes();

                // 执行getset方法
                if (methodName.startsWith("set") && null != clazzObj) {
                    methodInvokeGetSet(classes, clazzObj, method, paramClassArrs, clazzMethodName, methodName);
                    continue;
                }
                // 如果是静态方法
                if (Modifier.isStatic(method.getModifiers()) && !classes.isEnum()) {
                    if (null == paramClassArrs || paramClassArrs.length == 0) {
                        method.invoke(null, null);
                    } else if (paramClassArrs.length == 1) {
                        System.out.println("clazzMethodName:" + clazzMethodName + "," + classes.isEnum());
                        method.invoke(null, adaptorGenObj(paramClassArrs[0]));
                    } else if (paramClassArrs.length == 2) {
                        method.invoke(null, adaptorGenObj(paramClassArrs[0]), adaptorGenObj(paramClassArrs[1]));
                    } else if (paramClassArrs.length == 3) {
                        method.invoke(null, adaptorGenObj(paramClassArrs[0]), adaptorGenObj(paramClassArrs[1]),
                                adaptorGenObj(paramClassArrs[2]));
                    } else if (paramClassArrs.length == 4) {
                        method.invoke(null, adaptorGenObj(paramClassArrs[0]), adaptorGenObj(paramClassArrs[1]),
                                adaptorGenObj(paramClassArrs[2]), adaptorGenObj(paramClassArrs[3]));
                    }
                    continue;
                }
                if (null == clazzObj) {
                    continue;
                }
                // 如果方法是toString,直接执行
                if ("toString".equals(methodName)) {
                    try {
                        Method toStringMethod = classes.getDeclaredMethod(methodName, null);
                        toStringMethod.invoke(clazzObj, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                // 其他方法
                if (null == paramClassArrs || paramClassArrs.length == 0) {
                    method.invoke(clazzObj, null);
                } else if (paramClassArrs.length == 1) {
                    method.invoke(clazzObj, adaptorGenObj(paramClassArrs[0]));
                } else if (paramClassArrs.length == 2) {
                    method.invoke(clazzObj, adaptorGenObj(paramClassArrs[0]), adaptorGenObj(paramClassArrs[1]));
                } else if (paramClassArrs.length == 3) {
                    method.invoke(clazzObj, adaptorGenObj(paramClassArrs[0]), adaptorGenObj(paramClassArrs[1]),
                            adaptorGenObj(paramClassArrs[2]));
                } else if (paramClassArrs.length == 4) {
                    method.invoke(clazzObj, adaptorGenObj(paramClassArrs[0]), adaptorGenObj(paramClassArrs[1]),
                            adaptorGenObj(paramClassArrs[2]), adaptorGenObj(paramClassArrs[3]));
                }
            }
        }
    }

    private void methodInvokeGetSet(Class<?> classes, Object clazzObj, Method method, Class<?>[] paramClassArrs,
                                    String clazzMethodName, String methodName)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Object getObj = null;
        String methodNameSuffix = methodName.substring(3, methodName.length());
        Method getMethod = null;
        try {
            getMethod = classes.getDeclaredMethod("get" + methodNameSuffix, null);
        } catch (NoSuchMethodException e) {
            // 如果对应的get方法找不到,会有is开头的属性名,其get方法就是其属性名称
            if (null == getMethod) {
                Character firstChar = methodNameSuffix.charAt(0);// 取出第一个字符转小写
                String firstLowerStr = firstChar.toString().toLowerCase();
                try {
                    getMethod = classes.getDeclaredMethod(
                            firstLowerStr + methodNameSuffix.substring(1, methodNameSuffix.length()), null);
                } catch (NoSuchMethodException e2) {
                    // 如果还是空的,就跳过吧
                    if (null == getMethod) {
                        return;
                    }
                }
            }
        }
        // 如果get返回结果和set参数结果一样,才可以执行,否则不可以执行
        Class<?> returnClass = getMethod.getReturnType();
        if (paramClassArrs.length == 1 && paramClassArrs[0].toString().equals(returnClass.toString())) {
            getObj = getMethod.invoke(clazzObj, null);
            method.invoke(clazzObj, getObj);
        }

    }

    @SuppressWarnings("rawtypes")
    private Object newConstructor(Constructor[] constructorArr, Class<?> classes)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (null == constructorArr || constructorArr.length < 1) {
            return null;
        }
        Object clazzObj = null;
        boolean isExitNoParamConstruct = false;
        for (Constructor constructor : constructorArr) {
            Class[] constructParamClazzArr = constructor.getParameterTypes();
            if (null == constructParamClazzArr || constructParamClazzArr.length == 0) {
                constructor.setAccessible(true);
                clazzObj = classes.newInstance();
                isExitNoParamConstruct = true;
                break;
            }
        }
        // 没有无参构造取第一个
        if (!isExitNoParamConstruct) {
            boolean isContinueFor = false;
            Class[] constructParamClazzArr = constructorArr[0].getParameterTypes();
            Object[] construParamObjArr = new Object[constructParamClazzArr.length];
            for (int i = 0; i < constructParamClazzArr.length; i++) {
                Class constructParamClazz = constructParamClazzArr[i];
                construParamObjArr[i] = adaptorGenObj(constructParamClazz);
                if (null == construParamObjArr[i]) {
                    isContinueFor = true;
                }
            }
            if (!isContinueFor) {
                clazzObj = constructorArr[0].newInstance(construParamObjArr);
            }
        }
        return clazzObj;
    }

    private Object adaptorGenObj(Class<?> clazz)
            throws IllegalArgumentException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (null == clazz) {
            return null;
        }
        if ("int".equals(clazz.getName())) {
            return 1;
        } else if ("char".equals(clazz.getName())) {
            return 'x';
        } else if ("boolean".equals(clazz.getName())) {
            return true;
        } else if ("double".equals(clazz.getName())) {
            return 1.0;
        } else if ("float".equals(clazz.getName())) {
            return 1.0f;
        } else if ("long".equals(clazz.getName())) {
            return 1l;
        } else if ("byte".equals(clazz.getName())) {
            return 0xFFFFFFFF;
        } else if ("java.lang.Class".equals(clazz.getName())) {
            return this.getClass();
        } else if ("java.math.BigDecimal".equals(clazz.getName())) {
            return new BigDecimal(1);
        } else if ("java.lang.String".equals(clazz.getName())) {
            return "333";
        } else if ("java.util.Hashtable".equals(clazz.getName())) {
            return new Hashtable();
        } else if ("java.util.Hashtable".equals(clazz.getName())) {
            return new Hashtable();
        } else if ("java.util.List".equals(clazz.getName())) {
            return new ArrayList();
        } else {
            // 如果是接口或抽象类,直接跳过
            boolean paramIsAbstract = Modifier.isAbstract(clazz.getModifiers());
            boolean paramIsInterface = Modifier.isInterface(clazz.getModifiers());
            if (paramIsInterface || paramIsAbstract) {
                return null;
            }
            Constructor<?>[] constructorArrs = clazz.getConstructors();
            return newConstructor(constructorArrs, clazz);
        }
    }

    private List<Class<?>> getClasses(String packageName) {
        // 第一个class类的集合
        List<Class<?>> classes = new ArrayList<Class<?>>();
        // 是否循环迭代
        boolean recursive = true;
        // 获取包的名字 并进行替换
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }

    private void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive,
                                                  List<Class<?>> classes) {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(new FileFilter() {
            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            public boolean accept(File file) {
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        // 循环所有文件
        for (File file : dirfiles) {
            // 如果是目录 则递归继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive,
                        classes);
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                String pakClazzName = packageName + '.' + className;
                if (filterClazzList.contains(pakClazzName)) {
                    continue;
                }
                try {
                    // 添加到集合中去
                    classes.add(Class.forName(pakClazzName));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
