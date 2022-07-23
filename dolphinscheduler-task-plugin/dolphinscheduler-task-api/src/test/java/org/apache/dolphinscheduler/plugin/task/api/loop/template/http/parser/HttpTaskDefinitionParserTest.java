package org.apache.dolphinscheduler.plugin.task.api.loop.template.http.parser;

import org.apache.dolphinscheduler.plugin.task.api.loop.template.LoopTaskYamlDefinition;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class HttpTaskDefinitionParserTest {

    private static final String yamlFile = HttpTaskDefinitionParserTest.class.getResource("/mock_loop_task.yaml")
        .getFile();

    @Test
    public void parse() {
    }

    @Test
    public void parseYamlConfigFile() throws IOException {
        LoopTaskYamlDefinition loopTaskYamlDefinition = new HttpTaskDefinitionParser().parseYamlConfigFile(yamlFile);
        Assert.assertNotNull(loopTaskYamlDefinition);
        Assert.assertNotNull(loopTaskYamlDefinition.getService());
        LoopTaskYamlDefinition.LoopTaskServiceYamlDefinition service = loopTaskYamlDefinition.getService();
        Assert.assertEquals("FlinkX", service.getName());
        Assert.assertNotNull(service.getApi());
    }

    @Test
    public void validateYamlDefinition() throws IOException {
        HttpTaskDefinitionParser httpTaskDefinitionParser = new HttpTaskDefinitionParser();
        LoopTaskYamlDefinition loopTaskYamlDefinition = httpTaskDefinitionParser.parseYamlConfigFile(yamlFile);
        httpTaskDefinitionParser.validateYamlDefinition(loopTaskYamlDefinition);
        // if no exception assert true
        Assert.assertTrue(true);

    }
}