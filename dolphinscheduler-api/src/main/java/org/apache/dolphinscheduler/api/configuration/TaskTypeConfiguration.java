package org.apache.dolphinscheduler.api.configuration;

import lombok.Getter;
import lombok.Setter;
import org.apache.dolphinscheduler.api.dto.FavDto;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.config.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@EnableConfigurationProperties
@PropertySource(value = {"classpath:task-type-config.yaml"}, factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "task")
@Getter
@Setter
public class TaskTypeConfiguration {

    private List<String> universal;
    private List<String> cloud;
    private List<String> logic;
    private List<String> di;
    private List<String> dq;
    private List<String> other;

    public Set<FavDto> getDefaultTaskTypes() {
        Set<FavDto> defaultTaskTypes = new HashSet<>();
        if (defaultTaskTypes.size() <= 0) {
            universal.forEach(task -> defaultTaskTypes.add(new FavDto(task, false, Constants.TYPE_UNIVERSAL)));
            cloud.forEach(task -> defaultTaskTypes.add(new FavDto(task, false, Constants.TYPE_CLOUD)));
            logic.forEach(task -> defaultTaskTypes.add(new FavDto(task, false, Constants.TYPE_LOGIC)));
            di.forEach(task -> defaultTaskTypes.add(new FavDto(task, false, Constants.TYPE_DI)));
            dq.forEach(task -> defaultTaskTypes.add(new FavDto(task, false, Constants.TYPE_DQ)));
            other.forEach(task -> defaultTaskTypes.add(new FavDto(task, false, Constants.TYPE_OTHER)));
        }

        return defaultTaskTypes;
    }
}
