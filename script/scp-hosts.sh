#!/bin/sh

workDir=`dirname $0`
workDir=`cd ${workDir};pwd`
source $workDir/../conf/config/run_config.conf
source $workDir/../conf/config/install_config.conf

hostsArr=(${ips//,/ })
for host in ${hostsArr[@]}
do

    if ! ssh $host test -e $installPath; then
      ssh $host "sudo mkdir -p $installPath; sudo chown -R $deployUser:$deployUser $installPath"
    fi

	ssh $host  "cd $installPath/; rm -rf bin/ conf/ lib/ script/ sql/"
	scp -r $workDir/../bin  $host:$installPath
	scp -r $workDir/../conf  $host:$installPath
	scp -r $workDir/../lib   $host:$installPath
	scp -r $workDir/../script  $host:$installPath
	scp -r $workDir/../sql  $host:$installPath
	scp  $workDir/../install.sh  $host:$installPath
done
