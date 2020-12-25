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
@echo off

echo "------ dolphinscheduler start - build -------"
set
setlocal enableextensions enabledelayedexpansion

if not defined VERSION (
    echo "set environment variable [VERSION]"
    set first=1
    for /f "tokens=3 delims=<>" %%a in ('findstr "<version>[0-9].*</version>" %cd%\pom.xml') do (
        if !first! EQU 1 (set VERSION=%%a)
        set first=0
    )
)

if not defined DOCKER_REPO (
    echo "set environment variable [DOCKER_REPO]"
    set DOCKER_REPO=dolphinscheduler
)

echo "Version: %VERSION%"
echo "Repo: %DOCKER_REPO%"

echo "Current Directory is %cd%"

:: maven package(Project Directory)
echo "call mvn clean compile package -Prelease"
call mvn clean compile package -Prelease -DskipTests=true
if "%errorlevel%"=="1" goto :mvnFailed

:: move dolphinscheduler-bin.tar.gz file to docker/build directory
echo "move %cd%\dolphinscheduler-dist\target\apache-dolphinscheduler-incubating-%VERSION%-SNAPSHOT-dolphinscheduler-bin.tar.gz %cd%\docker\build\"
move %cd%\dolphinscheduler-dist\target\apache-dolphinscheduler-incubating-%VERSION%-dolphinscheduler-bin.tar.gz %cd%\docker\build\

:: docker build
echo "docker build --build-arg VERSION=%VERSION% -t %DOCKER_REPO%:%VERSION% %cd%\docker\build\"
docker build --build-arg VERSION=%VERSION% -t %DOCKER_REPO%:%VERSION% %cd%\docker\build\
if "%errorlevel%"=="1" goto :dockerBuildFailed

echo "------ dolphinscheduler end - build -------"

:mvnFailed
echo "MAVEN PACKAGE FAILED!"

:dockerBuildFailed
echo "DOCKER BUILD FAILED!"