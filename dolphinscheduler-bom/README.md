# Introduction

The `dolphinscheduler-bom` module is used to manage the version of third part dependencies. If you want to import
`dolphinscheduler-xx` to your project, you need to import `dolphinscheduler-bom` together by below way,
this can help you to manage the version.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.apache.dolphinscheduler</groupId>
            <artifactId>dolphinscheduler-bom</artifactId>
            <version>${dolphinscheduler.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

If you want to override the version defined in `dolphinscheduler-bom` you can directly add the version at your
module's `dependencyManagement`.
