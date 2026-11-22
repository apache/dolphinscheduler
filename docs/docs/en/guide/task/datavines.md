# Datavines Node

## Overview

[Datavines](https://github.com/datavane/datavines) is a data quality and data observability platform. The Datavines task plugin lets DolphinScheduler trigger an existing Datavines data quality job and wait for its result.

The plugin uses the Datavines OpenAPI:

- `POST /api/v1/openapi/job/execute/{jobId}`
- `GET /api/v1/openapi/job/execution/status/{executionId}`
- `GET /api/v1/openapi/job/execution/result/{executionId}`

## Prerequisites

1. A running Datavines service.
2. A Datavines data quality job. Copy its job ID from the job list.
3. A Datavines OpenAPI token. See the Datavines documentation for how to create one.
4. The DolphinScheduler worker can access the Datavines service address.

## Create a Datavines Task

1. Create or edit a workflow, then drag a Datavines node into the canvas.
2. Fill in the Datavines address, job ID and token.
3. Configure the poll interval, timeout and retry times if needed.
4. Save the workflow and run it.

## Parameters

|     Parameter     | Required |         Default         |                            Description                             |
|-------------------|----------|-------------------------|--------------------------------------------------------------------|
| Datavines Address | Yes      | `http://127.0.0.1:5600` | Datavines service address, must start with `http://` or `https://` |
| Job ID            | Yes      | -                       | ID of the Datavines data quality job                               |
| Token             | Yes      | -                       | Datavines OpenAPI token                                            |
| Poll Interval (s) | No       | `2`                     | Interval for polling the execution status                          |
| Timeout (s)       | No       | `3600`                  | Maximum time to wait for the job to finish                         |
| Retry Times       | No       | `3`                     | Number of retries when an API call fails                           |

## Output Parameters

The task writes the following values to the task variable pool:

|   Parameter   |                          Description                           |
|---------------|----------------------------------------------------------------|
| `executionId` | Datavines execution ID                                         |
| `result`      | Datavines execution result, for example `SUCCESS` or `FAILURE` |

You can use them in downstream tasks:

```text
${executionId}
${result}
```

## Result Handling

- If the Datavines execution status is `SUCCESS`, the plugin fetches the execution result.
- If the result is `SUCCESS`, the DolphinScheduler task succeeds.
- If the status or result is `FAILURE`, `CANCELED` or `KILLED`, the task fails.
- If the timeout is reached, the task fails with a timeout error.
- If the task is killed, the plugin stops polling and marks the task as killed.

## FAQ

### Token is invalid

Check that the token belongs to the Datavines instance specified by the address and that it has not expired.

### Job ID does not exist

Copy the job ID from the Datavines job list. The job must exist before the DolphinScheduler task runs.

### Connection timeout

Check that the DolphinScheduler worker can access the Datavines address, and that the address includes the correct port.

### The task is still running after a long time

Increase the timeout if the Datavines job is expected to run for a long time. The plugin polls the status until the timeout is reached.
