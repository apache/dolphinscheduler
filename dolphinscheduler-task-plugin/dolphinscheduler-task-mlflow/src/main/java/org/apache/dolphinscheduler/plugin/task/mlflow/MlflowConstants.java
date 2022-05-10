package org.apache.dolphinscheduler.plugin.task.mlflow;

public class MlflowConstants {
    private MlflowConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String PRESET_SKLEARN_PROJECT = "/home/lucky/WhaleOps/mlflow_sklearn_gallery";

    public static final String RUN_PROJECT_SCRIPT = "run_mlflow_project.sh";
}