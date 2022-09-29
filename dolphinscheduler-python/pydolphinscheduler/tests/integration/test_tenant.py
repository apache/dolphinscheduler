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

"""Test pydolphinscheduler tenant."""
import pytest

from pydolphinscheduler.models import Tenant, User


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


def get_tenant(
    name="test-name-1",
    queue="test-queue-1",
    description="test-description",
    tenant_code="test-tenant-code",
    user_name=None,
):
    """Get a test tenant."""
    tenant = Tenant(name, queue, description, code=tenant_code, user_name=user_name)
    tenant.create_if_not_exists(name)
    return tenant


def test_create_tenant():
    """Test create tenant from java gateway."""
    tenant = get_tenant()
    assert tenant.tenant_id is not None


def test_get_tenant():
    """Test get tenant from java gateway."""
    tenant = get_tenant()
    tenant_ = Tenant.get_tenant(tenant.code)
    assert tenant_.tenant_id == tenant.tenant_id


def test_update_tenant():
    """Test update tenant from java gateway."""
    tenant = get_tenant(user_name="admin")
    tenant.update(
        user="admin",
        code="test-code-updated",
        queue_id=1,
        description="test-description-updated",
    )
    tenant_ = Tenant.get_tenant(code=tenant.code)
    assert tenant_.code == "test-code-updated"
    assert tenant_.queue == 1


def test_delete_tenant():
    """Test delete tenant from java gateway."""
    tenant = get_tenant(user_name="admin")
    tenant.delete()
    with pytest.raises(AttributeError) as excinfo:
        _ = tenant.tenant_id

    assert excinfo.type == AttributeError
