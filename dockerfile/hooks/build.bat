:: Licensed to the Apache Software Foundation (ASF) under one or more
:: contributor license agreements.  See the NOTICE file distributed with
:: this work for additional information regarding copyright ownership.
:: The ASF licenses this file to You under the Apache License, Version 2.0
:: (the "License"); you may not use this file except in compliance with
:: the License.  You may obtain a copy of the License at
::
::     http://www.apache.org/licenses/LICENSE-2.0
::
:: Unless required by applicable law or agreed to in writing, software
:: distributed under the License is distributed on an "AS IS" BASIS,
:: WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
:: See the License for the specific language governing permissions and
:: limitations under the License.
::
echo "------ dolphinscheduler start - build -------"
set

echo "Current Directory is %cd%"

:: maven package(Project Directory)
echo "call mvn clean compile package -Prelease"
call mvn clean compile package -Prelease -DskipTests=true

:: move dolphinscheduler-bin.tar.gz file to dockerfile directory
echo "move %cd%\dolphinscheduler-dist\target\apache-dolphinscheduler-incubating-%VERSION%-SNAPSHOT-dolphinscheduler-bin.tar.gz %cd%\dockerfile\"
move %cd%\dolphinscheduler-dist\target\apache-dolphinscheduler-incubating-%VERSION%-SNAPSHOT-dolphinscheduler-bin.tar.gz %cd%\dockerfile\

:: docker build
echo "docker build --build-arg VERSION=%VERSION% -t %DOCKER_REPO%:%VERSION% %cd%\dockerfile\"
docker build --build-arg VERSION=%VERSION% -t %DOCKER_REPO%:%VERSION% %cd%\dockerfile\

echo "------ dolphinscheduler end   - build -------"