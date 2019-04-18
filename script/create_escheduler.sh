#!/bin/bash
workDir=`dirname $0`
workDir=`cd ${workDir};pwd`
echo "$workDir/lib"

java -Xmx1G -cp "$workDir/../lib/*"  cn.escheduler.dao.upgrade.shell.CreateEscheduler
