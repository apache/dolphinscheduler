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

"""Test pydolphinscheduler project."""
import pytest

from pydolphinscheduler.models import Project, User


def get_user(
    name="test-name",
    password="test-password",
    email="test-email@abc.com",
    phone="17366637777",
    tenant="test-tenant",
    queue="test-queue",
    status=1,
):
    """Get a test user."""
    user = User(name, password, email, phone, tenant, queue, status)
    user.create_if_not_exists()
    return user


def get_project(name="test-name-1", description="test-description", code=1):
    """Get a test project."""
    project = Project(name, description, code=code)
    user = get_user()
    project.create_if_not_exists(user=user.name)
    return project


def test_create_and_get_project():
    """Test create and get project from java gateway."""
    project = get_project()
    project_ = Project.get_project_by_name(user="test-name", name=project.name)
    assert project_.name == project.name
    assert project_.description == project.description


def test_update_project():
    """Test update project from java gateway."""
    project = get_project()
    project = project.get_project_by_name(user="test-name", name=project.name)
    project.update(
        user="test-name",
        project_code=project.code,
        project_name="test-name-updated",
        description="test-description-updated",
    )
    project_ = Project.get_project_by_name(user="test-name", name="test-name-updated")
    assert project_.description == "test-description-updated"


def test_delete_project():
    """Test delete project from java gateway."""
    project = get_project()
    project.get_project_by_name(user="test-name", name=project.name)
    project.delete(user="test-name")

    with pytest.raises(AttributeError) as excinfo:
        _ = project.name

    assert excinfo.type == AttributeError
