# Apache DolphinScheduler: Advanced Business Date Scheduling & Conditional Triggering Design

## 1. Understanding Summary
- **What is being built**: Enhancement of DS core capabilities to support advanced calendar-driven scheduling, business date offset (T+/-N), mixed-condition triggering (calendar time, external events, cross-workflow dependencies), and an "earliest execution time" constraint.
- **Why it exists**: To meet complex enterprise requirements (especially in finance, banking, data warehousing) where physical execution time is completely decoupled from logical business execution dates, and where complex dependency chains govern step execution.
- **Who it is for**: Data engineers, scheduling platform administrators, maintenance engineers requiring high observability.
- **Key Constraints**: Enhance existing `Process Definition` and `Schedule` entities (no new top-level framework); 10,000+ active schedule scaling; <= 50ms compute per scan; persistent high-observability wait states; smooth recovery on Master restart.
- **Explicit Non-Goals**: No new top-level Execution Plan object isolated from `Process Definition`. Existing Cron functionality remains unaffected.

## 2. Assumptions
- Business date (`business_date`) is injected into the Process Instance global variables and becomes the execution context for all tasks.
- A new Master scanner (`CommandDependencyEvaluator`) will decouple command generation from command execution.
- Missing/failing calendars will gracefully hang commands (`WAITING_CALENDAR_SERVICE`) rather than failing instances.
- Wait states are persisted to DB to survive Master restarts.

## 3. Decision Log
1. **Decision**: Extend existing entities (`Process Definition` and `Schedule`) instead of creating a parallel abstraction.
   - *Alternatives considered*: A new wrapper/Facade macro-scheduler, or adding complex Gatekeeper tasks to DAGs.
   - *Reason*: Aligns best with the DS mental model, reduces system complexity, and guarantees backward compatibility for existing scheduler features (YAGNI).
2. **Decision**: Use a "Command Queue Expansion Strategy" with explicit waiting states in `t_ds_command`.
   - *Alternatives considered*: Let commands generate instances that hang infinitely until conditions pass.
   - *Reason*: Preserves Master/Worker thread pool health and offers a unified dashboard view of pending commands with exact blockage reasons (`WAITING_TIME`, `WAITING_UPSTREAM`, `WAITING_SIGNAL`).
3. **Decision**: Keep internal condition nodes as-is.
   - *Alternatives considered*: Build new Condition node structures.
   - *Reason*: Making `business_date` globally accessible allows existing DS expressions to route tasks dynamically without engine modification.

## 4. Final Architecture Design
### Data Model Changes
- `t_ds_calendar`, `t_ds_calendar_date`: Define custom calendars (trading days, holidays).
- `t_ds_schedules`: Adding `calendar_id`, `business_date_offset` (T+N, T-N), `cutover_time`, `earliest_exec_time` configuration options.
- `t_ds_command`: Adding `business_date`, `earliest_timeout_time`, and highly granular `command_state` / `wait_reason` field (`CHECK_PENDING`, `WAITING_TIME`, `WAITING_SIGNAL`, `WAITING_UPSTREAM`, `WAITING_CALENDAR_SERVICE`, `READY`).

### Command Generation & Scanner Subsystem
- **Generation**: Quartz schedules generate `command` records into `t_ds_command` initialized to `CHECK_PENDING` with pre-calculated `business_date` context based on the Custom Calendar offset.
- **Evaluator**: A new Master asynchronous thread pool (`CommandDependencyEvaluator`) sweeps `CHECK_PENDING` or `WAITING_X` commands to evaluate cascade conditions:
  1. Has `earliest_timeout_time` been reached?
  2. Have custom external signals arrived (API/Message queue check)?
  3. Are cross-workflow upstreams (with matching `business_date`) successful?
- **Dispatch**: Once all barriers clear, the command state flips to `READY`. The traditional `MasterSchedulerService` scoops these up and transforms them into active `ProcessInstance` records.

### Edge Cases
- **Manual Triggers**: Optional overriding of `business_date` via UI payload; otherwise determined dynamically.
- **Backfill/Complement**: Batch-generates instructions that are automatically intersected against the valid days in the target Calendar to prevent meaningless run occurrences on skip-dates (e.g. weekends).
