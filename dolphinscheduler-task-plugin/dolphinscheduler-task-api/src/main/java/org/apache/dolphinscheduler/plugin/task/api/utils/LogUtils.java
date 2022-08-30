package org.apache.dolphinscheduler.plugin.task.api.utils;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
@UtilityClass
public class LogUtils {

    private static final Pattern APPLICATION_REGEX = Pattern.compile(TaskConstants.APPLICATION_REGEX);

    public List<String> getAppIdsFromLogFile(@NonNull String logPath) {
        return getAppIdsFromLogFile(logPath, log);
    }

    public List<String> getAppIdsFromLogFile(@NonNull String logPath, Logger logger) {
        File logFile = new File(logPath);
        if (!logFile.exists() || !logFile.isFile()) {
            return Collections.emptyList();
        }
        Set<String> appIds = new HashSet<>();
        try (Stream<String> stream = Files.lines(Paths.get(logPath))) {
            stream.filter(line -> {
                        Matcher matcher = APPLICATION_REGEX.matcher(line);
                        return matcher.find();
                    }
            ).forEach(line -> {
                Matcher matcher = APPLICATION_REGEX.matcher(line);
                if (matcher.find()) {
                    String appId = matcher.group();
                    if (appIds.add(appId)) {
                        logger.info("Find appId: {} from {}", appId, logPath);
                    }
                }
            });
            return new ArrayList<>(appIds);
        } catch (IOException e) {
            logger.error("Get appId from log file erro, logPath: {}", logPath, e);
            return Collections.emptyList();
        }
    }
}
