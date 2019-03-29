import os

HADOOP_HOME="/opt/soft/hadoop"
SPARK_HOME1="/opt/soft/spark1"
SPARK_HOME2="/opt/soft/spark2"
PYTHON_HOME="/opt/soft/python"
JAVA_HOME="/opt/soft/java"
HIVE_HOME="/opt/soft/hive"
PATH=os.environ['PATH']
PATH="%s/bin:%s/bin:%s/bin:%s/bin:%s/bin:%s/bin:%s"%(HIVE_HOME,HADOOP_HOME,SPARK_HOME1,SPARK_HOME2,JAVA_HOME,PYTHON_HOME,PATH)

os.putenv('PATH','%s'%PATH)