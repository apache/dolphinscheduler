#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

while getopts t:c:p: opts; do
    case $opts in
        t) t=$OPTARG ;;
        c) c=$OPTARG ;;
        p) p=$OPTARG ;;
        ?) ;;
    esac
done


# Write your specific logic here

# Set the exit code according to your execution result, and alert needs to use it to judge the status of this alarm result


if  [ "$t" = "error msg title" ]
   then
     exit 12
fi
exit 0
exit 0