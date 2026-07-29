# DolphinScheduler API Server OOM POC

This POC demonstrates CVE-18441: TransporterDecoder OOM via crafted bodyLength.

## Vulnerability

`TransporterDecoder.decode()` at line 61 does `new byte[bodyLength]` without any
size validation. A malicious or buggy peer can send `bodyLength = Integer.MAX_VALUE`
(~2GB), causing immediate `OutOfMemoryError` on all services (Master, Worker, API,
Alert) that share this decoder.

## Attack Vector

```
User → HTTP GET /log/detail?limit=2147483647
  → API Server → Netty RPC → Worker
  → Worker reads entire log file into memory
  → Worker returns huge RPC response
  → API Server TransporterDecoder: new byte[hugeBodyLength] → OOM
```

## Files

- `OomPoc.java` — Self-contained POC that simulates both pre-fix and post-fix behavior

## Run

```bash
# Compile and run against the project
cd dolphinscheduler-poc
mvn compile exec:java -Dexec.mainClass=OomPoc
# Or simply:
java -cp <classpath> OomPoc
```
