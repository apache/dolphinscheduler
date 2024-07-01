# Project-level Parameter

## Scope

Project-level parameters are valid for all task nodes under the entire project.

## Usage

### Define project-level parameters

On the project page, click Project Parameters and Create Parameters, and fill in the parameter name and parameter value, select the appropriate parameter value type. As shown below:

![project-parameter01](../../../../img/new_ui/dev/parameter/project_parameter01.png)

### Use project-level parameters

Take the shell task as an example, enter `echo ${param}` in the script content, where `param` is the project-level parameter created in the previous step.

![project-parameter02](../../../../img/new_ui/dev/parameter/project_parameter02.png)

Run the shell task. On the task instance page, you can view the task log to verify whether the parameters are valid.

![project-parameter03](../../../../img/new_ui/dev/parameter/project_parameter03.png)
