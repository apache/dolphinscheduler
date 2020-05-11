package org.apache.dolphinscheduler.server.worker.task.ssh;

import com.jcraft.jsch.Session;

public class SSHClient {

  private Session session;
  private boolean isIdle;

  public SSHClient(Session session, boolean isIdle) {
    this.session = session;
    this.isIdle = isIdle;
  }

  public Session getSession() {
    return session;
  }

  public void setSession(Session session) {
    this.session = session;
  }

  public boolean isIdle() {
    return isIdle;
  }

  public void setIdle(boolean idle) {
    isIdle = idle;
  }

}
