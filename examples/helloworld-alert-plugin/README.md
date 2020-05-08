# How to use an alert plugin

## 1. Write your alert plugin

You can see the code in this repo and learn how to write a `hello world` plugin.

1. Depend on `dolphinscheduler-plugin-api` which contains the plugin API and some useful tools. To do this, update your pom file and add dependencies.

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.dolphinscheduler</groupId>
        <artifactId>dolphinscheduler-plugin-api</artifactId>
    </dependency>
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

2. Package all your code with dependencies using `maven-assembly-plugin` maven plugin.

```xml
<build>
    <plugins>
        <plugin>
            <artifactId>maven-assembly-plugin</artifactId>
            <configuration>
                <descriptorRefs>
                    <descriptorRef>jar-with-dependencies</descriptorRef>
                </descriptorRefs>
                <appendAssemblyId>false</appendAssemblyId>
            </configuration>
            <executions>
                <execution>
                    <id>make-assembly</id>
                    <phase>package</phase>
                    <goals>
                        <goal>single</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

3. Your plugin should be packaged in a jar file

```xml
<packaging>jar</packaging>
```

4. Add your plugin provider class, which will implement `AlertPluginProvider` interface. For example `HelloworldProvider`

```java
public class HelloworldProvider implements AlertPluginProvider {

    @Override
    public AlertPlugin createPlugin() {
        return new HelloworldAlertPlugin();
    }

}
```

5. Implement your plugin class, which will implement `AlertPlugin` interface.

```java
public class HelloworldAlertPlugin implements AlertPlugin {

    private static final Logger logger = LoggerFactory.getLogger(HelloworldAlertPlugin.class);

    private PluginName pluginName;

    public HelloworldAlertPlugin() {
        pluginName = new PluginName();
        pluginName.setEnglish("helloworld")
                .setChinese("你好世界");
    }

    @Override
    public String getId() {
        return "helloworld";
    }

    @Override
    public PluginName getName() {
        return pluginName;
    }

    @Override
    public Map<String, Object> process(AlertInfo info) {
        logger.info("{}", PropertyUtils.getInt("helloworld.int"));
        logger.info(PropertyUtils.getString("helloworld.string"));
        logger.info(info.getAlertData().getTitle());
        return new HashMap<>();
    }
}
```

As shown above, you can use `PropertyUtils` to read your property file. The name of the property file is `plugin.properties` and located in the root path of your jar file.

6. You should also create a file named `org.apache.dolphinscheduler.plugin.spi.AlertPluginProvider` under `resources/META-INF/services` folder. The file tree view shown as below.

```shell
└── [May  8 19:16]  resources
    ├── [May  8 19:16]  META-INF
    │   └── [May  8 19:16]  services
    │       └── [May  8 19:16]  org.apache.dolphinscheduler.plugin.spi.AlertPluginProvider
    └── [May  8 19:16]  plugin.properties
```

7. In `org.apache.dolphinscheduler.plugin.spi.AlertPluginProvider` file, add The **fully-qualified name** of your provider class. For example, here we use `HelloworldProvider`

```properties
# file org.apache.dolphinscheduler.plugin.spi.AlertPluginProvider
org.apache.dolphinscheduler.example.plugin.HelloworldProvider
```

8. Be careful your package name should not contain prefix `org.apache.dolphinscheduler.plugin`.

9. Run `mvn clean package`, which will generate your plugin jar file.

## Put your plugin UNDER plugin folder

1. Put the plugin jar file under the plugin directory. The plugin directory could be set in `alert.properties`

```properties
plugin.dir=/Users/xx/your/path/to/plugin/dir
```

2. Now restart the alert service, it will find the plugin automatically. But it will not run the plugin currently, because it hasn't implement the related logci about how to choose the alert way.



Have fun.