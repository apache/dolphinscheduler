#!/bin/bash
start=$1
size=$2
for i in $(seq $start $size)
do
echo $i
python3 src/pydolphinscheduler/examples/tutorial.py $i
sleep 1s
done
