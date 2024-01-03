package org.apache.dolphinscheduler.e2e.pages.project.workflow.task;

import org.apache.dolphinscheduler.e2e.pages.common.CodeEditor;
import org.apache.dolphinscheduler.e2e.pages.common.HttpInput;
import org.apache.dolphinscheduler.e2e.pages.project.workflow.WorkflowForm;
import org.openqa.selenium.WebDriver;

public class HttpTaskForm extends TaskNodeForm{
    private WebDriver driver;

    private HttpInput httpInput;


    public HttpTaskForm(WorkflowForm parent) {
        super(parent);
        this.httpInput = new HttpInput(parent.driver());
        this.driver = parent.driver();
    }

    public HttpTaskForm url(String script) {
        httpInput.content(script);
        return this;
    }
}
