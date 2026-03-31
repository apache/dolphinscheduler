package org.apache.dolphinscheduler.server.master.engine.command;

import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

/**
 * Background thread in Master that evaluates PENDING commands (command_state = 1)
 * Once all conditions (time delays, dependencies, signals) are met,
 * the command is upgraded to READY (command_state = 0) to be picked up by the regular fetcher.
 */
@Component
public class CommandDependencyEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(CommandDependencyEvaluator.class);

    @Autowired
    private CommandMapper commandMapper;
    
    // Command states
    private static final int STATE_READY = 0;
    private static final int STATE_CHECK_PENDING = 1;

    // Wait reasons (Observability mapping)
    private static final int REASON_NONE = 0;
    private static final int REASON_WAIT_TIME = 1;
    private static final int REASON_WAIT_UPSTREAM = 2; // Stub for Phase 5 extension
    private static final int REASON_WAIT_SIGNAL = 3;   // Stub for Phase 5 extension

    @PostConstruct
    public void startEvaluator() {
        Thread evaluatorThread = new Thread(this::evaluateLoop, "Command-Dependency-Evaluator-Thread");
        evaluatorThread.setDaemon(true);
        evaluatorThread.start();
        logger.info("Started Command Dependency Evaluator background scanner.");
    }

    private void evaluateLoop() {
        while (true) {
            try {
                // Hardcoded 5s scan rate for simple responsiveness. Could be configured via properties.
                Thread.sleep(5000); 

                List<Command> pendingCommands = commandMapper.queryPendingCommands(100);
                if (pendingCommands != null && !pendingCommands.isEmpty()) {
                    for (Command cmd : pendingCommands) {
                        try {
                            evaluateCommand(cmd);
                        } catch (Exception e) {
                            logger.error("Error evaluating command [id={}]", cmd.getId(), e);
                        }
                    }
                }
            } catch (InterruptedException e) {
                logger.warn("CommandDependencyEvaluator interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Unexpected error in Evaluator Loop", e);
            }
        }
    }

    private void evaluateCommand(Command cmd) {
        boolean ready = true;
        int newWaitReason = REASON_NONE;
        
        // --- 1. PHYSICAL/LOGICAL TIME CHECK ---
        if (cmd.getEarliestTimeoutTime() != null) {
            if (new Date().before(cmd.getEarliestTimeoutTime())) {
                ready = false;
                newWaitReason = REASON_WAIT_TIME;
            }
        }
        
        // --- 2. (Future) CROSS-DAG DEPENDENCY CHECK ---
        // if (ready && hasUpstreamDependencies(cmd)) { ... newWaitReason = REASON_WAIT_UPSTREAM; ... }
        
        // --- 3. (Future) EXTERNAL SIGNAL CHECK ---
        // if (ready && missingSignalFiles(cmd)) { ... newWaitReason = REASON_WAIT_SIGNAL; ... }

        boolean changed = false;
        
        if (ready) {
            logger.info("Check passed! Command [id={}] is now READY to trigger.", cmd.getId());
            cmd.setCommandState(STATE_READY);
            cmd.setWaitReason(REASON_NONE);
            changed = true;
        } else if (cmd.getWaitReason() == null || cmd.getWaitReason() != newWaitReason) {
            logger.debug("Command [id={}] blocked. Evolving wait_reason to {}", cmd.getId(), newWaitReason);
            cmd.setWaitReason(newWaitReason);
            changed = true;
        }

        if (changed) {
            cmd.setUpdateTime(new Date());
            commandMapper.updateById(cmd);
        }
    }
}
