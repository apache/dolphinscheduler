package org.apache.dolphinscheduler.server.worker.task.ssh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SSHPool {

  private static Map<String,List<SSHClient>> sshPool = new ConcurrentHashMap<>();

  public static synchronized SSHClient getSSHClient(String host, String user, String password, int port, int timeout) throws Exception {
    List<SSHClient> clients = sshPool.get(host + "-" + user);
    if (clients != null) {
      SSHClient client = getIdleClient(clients);
      if (client != null) {
        return client;
      } else {
        System.out.println("--------&&&&&&&&&&--------" + clients.size());
        if (clients.size() < 5) {
          Session session = createSession(host, user, password, port, timeout);
          client = new SSHClient(session, false);
          clients.add(client);
          return client;
        } else {
          while (true) {
            client = getIdleClient(clients);
            if (client != null) {
              return client;
            }
          }
        }
      }
    } else {
      return putAndGetSSHClient(host, user, password, port, timeout);
    }
  }

  public static SSHClient putAndGetSSHClient(String host, String user, String password, int port, int timeout) throws Exception {
    List<SSHClient> clients = new ArrayList<>();
    Session session = createSession(host, user, password, port, timeout);
    SSHClient client = new SSHClient(session, false);
    clients.add(client);
    sshPool.put(host + "-" + user, clients);
    return client;
  }

  private static Session createSession(String host, String user, String password, int port, int timeout) throws Exception {
    JSch jsch = new JSch();
    Session session = jsch.getSession(user, host, port);
    session.setPassword(password);
    session.setConfig("StrictHostKeyChecking", "no");
    session.connect(timeout);   // making a connection with timeout.
    return session;
  }

  private static SSHClient getIdleClient(List<SSHClient> clients) {
    for (SSHClient client : clients) {
      if (client.isIdle()) {
        client.setIdle(false);
        return client;
      }
    }
    return null;
  }

}
