# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

"""Test pydolphinscheduler java gateway."""


from py4j.java_gateway import JavaGateway, java_import


def test_gateway_connect():
    """Test weather client could connect java gate way or not."""
    gateway = JavaGateway()
    app = gateway.entry_point
    assert app.ping() == "PONG"


def test_jvm_simple():
    """Test use JVM build-in object and operator from java gateway."""
    gateway = JavaGateway()
    smallest = gateway.jvm.java.lang.Integer.MIN_VALUE
    biggest = gateway.jvm.java.lang.Integer.MAX_VALUE
    assert smallest is not None and biggest is not None
    assert biggest > smallest


def test_python_client_java_import_single():
    """Test import single class from java gateway."""
    gateway = JavaGateway()
    java_import(gateway.jvm, "org.apache.dolphinscheduler.common.utils.FileUtils")
    assert hasattr(gateway.jvm, "FileUtils")


def test_python_client_java_import_package():
    """Test import package contain multiple class from java gateway."""
    gateway = JavaGateway()
    java_import(gateway.jvm, "org.apache.dolphinscheduler.common.utils.*")
    # test if jvm view have some common utils
    for util in ("FileUtils", "OSUtils", "DateUtils"):
        assert hasattr(gateway.jvm, util)
