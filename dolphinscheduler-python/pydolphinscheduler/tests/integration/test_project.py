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

from pydolphinscheduler.models import Project


def get_project(name="test-name-1",
                description="test-description",
                code="test-project-code"):
    """Get a test project."""
    project = Project(name, description, code=code)
    project.create_if_not_exists(name)
    return project


def test_create_project():
    """Test create project from java gateway."""
    project = get_project()
    assert project.code is not None


def test_get_project():
    """Test get project from java gateway."""
    project = get_project()
    project_ = Project()
    project_.get_project_by_name(name=project.name)
    assert project_.name == project.name
    assert project_.description == project.description
    assert project_.code == project.code


def test_update_project():
    """Test update project from java gateway."""
    project = get_project()
    project.update(project_code=project.code, project_name="test-name-updated", description="test-description-updated")
    project_ = Project()
    project_.get_project_by_name(name="test-name-updated")
    assert project_.description == "test-description-updated"
    assert project_.name == "test-name-updated"


def test_delete_project():
    """Test delete project from java gateway."""
    project = get_project()
    project.delete()
    project_ = Project()
    project_.get_project_by_name(name=project.name)
    assert project_.code is None