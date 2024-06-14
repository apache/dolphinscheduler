package org.apache.dolphinscheduler.plugin.task.flink;

public enum JobStatus {

    INITIALIZING(TerminalState.NON_TERMINAL),

    CREATED(TerminalState.NON_TERMINAL),

    RUNNING(TerminalState.NON_TERMINAL),

    FAILING(TerminalState.NON_TERMINAL),

    FAILED(TerminalState.GLOBALLY),

    CANCELLING(TerminalState.NON_TERMINAL),

    CANCELED(TerminalState.GLOBALLY),

    FINISHED(TerminalState.GLOBALLY),

    RESTARTING(TerminalState.NON_TERMINAL),

    SUSPENDED(TerminalState.LOCALLY),

    RECONCILING(TerminalState.NON_TERMINAL);

    private enum TerminalState {
        NON_TERMINAL,
        LOCALLY,
        GLOBALLY
    }

    private final TerminalState terminalState;

    JobStatus(TerminalState terminalState) {
        this.terminalState = terminalState;
    }

    public boolean isGloballyTerminalState() {
        return terminalState == TerminalState.GLOBALLY;
    }

    public boolean isTerminalState() {
        return terminalState != TerminalState.NON_TERMINAL;
    }
}
