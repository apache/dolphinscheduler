# DolphinScheduler Expansion and Reduction

## Expansion

This article describes how to add a new master service or worker service to an existing DolphinScheduler cluster.

```
Attention: There cannot be more than one master service process or worker service process on a physical machine.
      If the physical machine which locate the expansion master or worker node has already installed the scheduled service, check the [1.4 Modify configuration] and edit the configuration file `conf/config/install_config.conf` on ** all ** nodes, add masters or workers parameter, and restart the scheduling cluster.
```

### Basic software installation

* [required] [JDK](https://www.oracle.com/technetwork/java/javase/downloads/index.html) (version 1.8+): must install, install and configure `JAVA_HOME` and `PATH` variables under `/etc/profile`
* [optional] If the expansion is a worker node, you need to consider whether to install an external client, such as Hadoop, Hive, Spark Client.

```markdown
Attention: DolphinScheduler itself does not depend on Hadoop, Hive, Spark, but will only call their Client for the corresponding task submission.
```

### Get Installation Package

- Check the version of DolphinScheduler used in your existing environment, and get the installation package of the corresponding version, if the versions are different, there may be compatibility problems.
- Confirm the unified installation directory of other nodes, this article assumes that DolphinScheduler is installed in `/opt/` directory, and the full path is `/opt/dolphinscheduler`.
- Please download the corresponding version of the installation package to the server installation directory, uncompress it and rename it to `dolphinscheduler` and store it in the `/opt` directory.
- Add database dependency package, this document uses Mysql database, add `mysql-connector-java` driver package to `/opt/dolphinscheduler/lib` directory.

```shell
# create the installation directory, please do not create the installation directory in /root, /home and other high privilege directories 
mkdir -p /opt
cd /opt
# decompress
tar -zxvf apache-dolphinscheduler-<version>-bin.tar.gz -C /opt 
cd /opt
mv apache-dolphinscheduler-<version>-bin  dolphinscheduler
```

```markdown
Attention: You can copy the installation package directly from an existing environment to an expanded physical machine.
```

### Create Deployment Users

- Create deployment user on **all** expansion machines, and make sure to configure sudo-free. If we plan to deploy scheduling on four expansion machines, ds1, ds2, ds3, and ds4, create deployment users on each machine is prerequisite.

```shell
# to create a user, you need to log in with root and set the deployment user name, modify it by yourself, the following take `dolphinscheduler` as an example:
useradd dolphinscheduler;

# set the user password, please change it by yourself, the following take `dolphinscheduler123` as an example
echo "dolphinscheduler123" | passwd --stdin dolphinscheduler

# configure sudo password-free
echo 'dolphinscheduler  ALL=(ALL)  NOPASSWD: NOPASSWD: ALL' >> /etc/sudoers
sed -i 's/Defaults    requirett/#Defaults    requirett/g' /etc/sudoers

```

```markdown
Attention:
- Since it is `sudo -u {linux-user}` to switch between different Linux users to run multi-tenant jobs, the deploying user needs to have sudo privileges and be password free.
- If you find the line `Default requiretty` in the `/etc/sudoers` file, please also comment it out.
- If have needs to use resource uploads, you also need to assign read and write permissions to the deployment user on `HDFS or MinIO`.
```

### Modify Configuration

- From an existing node such as `Master/Worker`, copy the configuration directory directly to replace the configuration directory in the new node. After finishing the file copy, check whether the configuration items are correct.

  ```markdown
  Highlights:
  datasource.properties: database connection information 
  zookeeper.properties: information for connecting zk 
  common.properties: Configuration information about the resource store (if hadoop is set up, please check if the core-site.xml and hdfs-site.xml configuration files exist).
  dolphinscheduler_env.sh: environment Variables
  ```
- Modify the `dolphinscheduler_env.sh` environment variable in the `bin/env/dolphinscheduler_env.sh` directory according to the machine configuration (the following is the example that all the used software install under `/opt/soft`)

  ```shell
      export HADOOP_HOME=/opt/soft/hadoop
      export HADOOP_CONF_DIR=/opt/soft/hadoop/etc/hadoop
      export SPARK_HOME=/opt/soft/spark
      export PYTHON_HOME=/opt/soft/python
      export JAVA_HOME=/opt/soft/jav
      export HIVE_HOME=/opt/soft/hive
      export FLINK_HOME=/opt/soft/flink
      export DATAX_HOME=/opt/soft/datax/bin/datax.py
      export PATH=$HADOOP_HOME/bin:$SPARK_HOME/bin:$PYTHON_HOME:$JAVA_HOME/bin:$HIVE_HOME/bin:$PATH:$FLINK_HOME/bin:$DATAX_HOME:$PATH

  ```

  `Attention: This step is very important, such as `JAVA_HOME` and `PATH` is necessary to configure if haven not used just ignore or comment out`

- Soft link the `JDK` to `/usr/bin/java` (still using `JAVA_HOME=/opt/soft/java` as an example)

  ```shell
  sudo ln -s /opt/soft/java/bin/java /usr/bin/java
  ```
- Modify the configuration file `conf/config/install_config.conf` on the **all** nodes, synchronizing the following configuration.
  * To add a new master node, you need to modify the IPs and masters parameters.
  * To add a new worker node, modify the IPs and workers parameters.

```shell
# which machines to deploy DS services on, separated by commas between multiple physical machines
ips="ds1,ds2,ds3,ds4"

# ssh port,default 22
sshPort="22"

# which machine the master service is deployed on
masters="existing master01,existing master02,ds1,ds2"

# the worker service is deployed on which machine, and specify the worker belongs to which worker group, the following example of "default" is the group name
workers="existing worker01:default,existing worker02:default,ds3:default,ds4:default"

```

- If the expansion is for worker nodes, you need to set the worker group, refer to the security of the [Worker grouping](./security.md)

- On all new nodes, change the directory permissions so that the deployment user has access to the DolphinScheduler directory

```shell
sudo chown -R dolphinscheduler:dolphinscheduler dolphinscheduler
```

### Restart the Cluster and Verify

- Restart the cluster

```shell
# stop command:

bin/stop-all.sh # stop all services

bash bin/dolphinscheduler-daemon.sh stop master-server  # stop master service
bash bin/dolphinscheduler-daemon.sh stop worker-server  # stop worker service
bash bin/dolphinscheduler-daemon.sh stop api-server     # stop api    service
bash bin/dolphinscheduler-daemon.sh stop alert-server   # stop alert  service


# start command::
bin/start-all.sh # start all services

bash bin/dolphinscheduler-daemon.sh start master-server  # start master service
bash bin/dolphinscheduler-daemon.sh start worker-server  # start worker service
bash bin/dolphinscheduler-daemon.sh start api-server     # start api    service
bash bin/dolphinscheduler-daemon.sh start alert-server   # start alert  service

```

```
Attention: When using `stop-all.sh` or `stop-all.sh`, if the physical machine execute the command is not configured to be ssh-free on all machines, it will prompt to enter the password
```

- After completing the script, use the `jps` command to see if every node service is started (`jps` comes with the `Java JDK`)

```
MasterServer         ----- master service
WorkerServer         ----- worker service
ApiApplicationServer ----- api    service
AlertServer          ----- alert  service
```

After successful startup, you can view the logs, which are stored in the `logs` folder.

```Log Path
logs/
   ├── dolphinscheduler-alert-server.log
   ├── dolphinscheduler-master-server.log
   ├── dolphinscheduler-worker-server.log
   ├── dolphinscheduler-api-server.log
```

If the above services start normally and the scheduling system page is normal, check whether there is an expanded Master or Worker service in the [Monitor] of the web system. If it exists, the expansion is complete.

-----------------------------------------------------------------------------

## Reduction

The reduction is to reduce the master or worker services for the existing DolphinScheduler cluster.
There are two steps for shrinking. After performing the following two steps, the shrinking operation can be completed.

### Stop the Service on the Scaled-Down Node

* If you are scaling down the master node, identify the physical machine where the master service is located, and stop the master service on the physical machine.
* If scale down the worker node, determine the physical machine where the worker service scale down and stop the worker services on the physical machine.

```shell
# stop command:
bin/stop-all.sh # stop all services

bash bin/dolphinscheduler-daemon.sh stop master-server  # stop master service
bash bin/dolphinscheduler-daemon.sh stop worker-server  # stop worker service
bash bin/dolphinscheduler-daemon.sh stop api-server     # stop api    service
bash bin/dolphinscheduler-daemon.sh stop alert-server   # stop alert  service


# start command:
bin/start-all.sh # start all services

bash bin/dolphinscheduler-daemon.sh start master-server # start master service
bash bin/dolphinscheduler-daemon.sh start worker-server # start worker service
bash bin/dolphinscheduler-daemon.sh start api-server    # start api    service
bash bin/dolphinscheduler-daemon.sh start alert-server  # start alert  service

```

```
Attention: When using `stop-all.sh` or `stop-all.sh`, if the machine without the command is not configured to be ssh-free for all machines, it will prompt to enter the password
```

- After the script is completed, use the `jps` command to see if every node service was successfully shut down (`jps` comes with the `Java JDK`)

```
MasterServer         ----- master service
WorkerServer         ----- worker service
ApiApplicationServer ----- api    service
AlertServer          ----- alert  service
```

If the corresponding master service or worker service does not exist, then the master or worker service is successfully shut down.

### Modify the Configuration File

- modify the configuration file `conf/config/install_config.conf` on the **all** nodes, synchronizing the following configuration.
  * to scale down the master node, modify the IPs and masters parameters.
  * to scale down worker nodes, modify the IPs and workers parameters.

```shell
# which machines to deploy DS services on, "localhost" for this machine
ips="ds1,ds2,ds3,ds4"

# ssh port,default: 22
sshPort="22"

# which machine the master service is deployed on
masters="existing master01,existing master02,ds1,ds2"

# The worker service is deployed on which machine, and specify which worker group this worker belongs to, the following example of "default" is the group name
workers="existing worker01:default,existing worker02:default,ds3:default,ds4:default"

```

