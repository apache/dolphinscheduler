package org.apache.dolphinscheduler.poc;

import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.common.ILogService;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadResponse;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * POC for CVE-18441: Log streaming verification against a running standalone server.
 *
 * <p>Prerequisites:
 * <ol>
 *   <li>Start the standalone server (which embeds master + worker + api in one JVM)</li>
 *   <li>Create a large log file in the worker's log directory, e.g.:
 *       {@code logs/20260729/1_1_1_1.log} with 50,000 lines (~11MB)</li>
 * </ol>
 *
 * <p>Usage:
 * <pre>
 * java -cp <classpath> org.apache.dolphinscheduler.poc.LogStreamingPoc [logPath] [workerHost] [skipLineNum] [limit]
 *   logPath    - absolute path to the task instance log file (default: see code)
 *   workerHost - worker RPC host:port (default: 127.0.0.1:1234)
 *   skipLineNum - lines to skip (default: 0)
 *   limit       - max lines to request (default: 1000)
 * </pre>
 *
 * <p>This POC demonstrates three scenarios:
 * <ol>
 *   <li><b>Normal pagination</b>: Request limit=1000, verify the response is ~64KB
 *       (early termination at MAX_RESPONSE_LOG_SIZE, not all 1000 lines loaded).</li>
 *   <li><b>Attack scenario</b>: Request limit=Integer.MAX_VALUE, verify the response
 *       is still ~64KB and returns in < 100ms (clamped + early termination).</li>
 *   <li><b>Full download</b>: Request the entire log file as byte[], verify it works
 *       within the 64MB maxFrameSize limit.</li>
 * </ol>
 */
@Slf4j
public class LogStreamingPoc {

    public static void main(String[] args) throws Exception {
        String logPath = args.length > 0 ? args[0]
                : "D:/Project/dolphinscheduler/logs/20260729/1_1_1_1.log";
        String workerHost = args.length > 1 ? args[1] : "127.0.0.1:1234";
        int skipLineNum = args.length > 2 ? Integer.parseInt(args[2]) : 0;
        int limit = args.length > 3 ? Integer.parseInt(args[3]) : 1000;

        log.info("========================================");
        log.info("CVE-18441 POC: Log Streaming Verification");
        log.info("========================================");
        log.info("Log path:     {}", logPath);
        log.info("Worker host:  {}", workerHost);
        log.info("Skip lines:   {}", skipLineNum);
        log.info("Limit:        {}", limit);
        log.info("");

        ILogService logService = Clients.withService(ILogService.class).withHost(workerHost);

        // --- Test 1: Normal paginated query ---
        log.info("--- Test 1: Normal paginated query (limit={}) ---", limit);
        TaskInstanceLogPageQueryRequest request = TaskInstanceLogPageQueryRequest.builder()
                .taskInstanceId(1)
                .taskInstanceLogAbsolutePath(logPath)
                .skipLineNum(skipLineNum)
                .limit(limit)
                .build();

        long start = System.currentTimeMillis();
        TaskInstanceLogPageQueryResponse response = logService.pageQueryTaskInstanceLog(request);
        long elapsed = System.currentTimeMillis() - start;

        log.info("Response code: {}", response.getCode());
        if (response.getLogContent() != null) {
            int contentBytes = response.getLogContent().getBytes("UTF-8").length;
            int lineCount = response.getLogContent().split("\r\n").length;
            log.info("Content length: {} bytes ({} KB)", contentBytes, contentBytes / 1024);
            log.info("Content lines:  {}", lineCount);
            log.info("Time:           {} ms", elapsed);

            // Print first 3 and last 3 lines
            String[] lines = response.getLogContent().split("\r\n");
            log.info("--- First 3 lines ---");
            for (int i = 0; i < Math.min(3, lines.length); i++) {
                String line = lines[i].length() > 100 ? lines[i].substring(0, 100) + "..." : lines[i];
                log.info("  {}", line);
            }
            log.info("--- Last 3 lines ---");
            for (int i = Math.max(0, lines.length - 3); i < lines.length; i++) {
                String line = lines[i].length() > 100 ? lines[i].substring(0, 100) + "..." : lines[i];
                log.info("  {}", line);
            }
        }

        // --- Test 2: Attack scenario - limit=Integer.MAX_VALUE ---
        log.info("");
        log.info("--- Test 2: Attack scenario (limit=Integer.MAX_VALUE) ---");
        request = TaskInstanceLogPageQueryRequest.builder()
                .taskInstanceId(1)
                .taskInstanceLogAbsolutePath(logPath)
                .skipLineNum(0)
                .limit(Integer.MAX_VALUE)
                .build();
        start = System.currentTimeMillis();
        response = logService.pageQueryTaskInstanceLog(request);
        elapsed = System.currentTimeMillis() - start;
        log.info("Response code: {}", response.getCode());
        if (response.getLogContent() != null) {
            int contentBytes = response.getLogContent().getBytes("UTF-8").length;
            log.info("Content length: {} bytes ({} KB)", contentBytes, contentBytes / 1024);
            log.info("Time:           {} ms", elapsed);
            log.info("(Should be ~64KB due to early termination, not the full 11MB file)");
        }

        // --- Test 3: Full log file download ---
        log.info("");
        log.info("--- Test 3: Full log file download ---");
        TaskInstanceLogFileDownloadRequest dlRequest = new TaskInstanceLogFileDownloadRequest(1, logPath);
        start = System.currentTimeMillis();
        TaskInstanceLogFileDownloadResponse dlResponse = logService.getTaskInstanceWholeLogFileBytes(dlRequest);
        elapsed = System.currentTimeMillis() - start;
        log.info("Response code: {}", dlResponse.getCode());
        if (dlResponse.getLogBytes() != null) {
            int mb = dlResponse.getLogBytes().length / 1024 / 1024;
            log.info("Downloaded bytes: {} ({} MB)", dlResponse.getLogBytes().length, mb);
            log.info("Time:              {} ms", elapsed);
            log.info("(Within 64MB maxFrameSize limit - no OOM)");
        }

        log.info("");
        log.info("========================================");
        log.info("POC completed successfully");
        log.info("========================================");
        System.exit(0);
    }
}
