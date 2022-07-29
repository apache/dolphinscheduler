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

"""Test pydolphinscheduler user."""

from pydolphinscheduler.models import User


def get_user(name="test-name",
             password="test-password",
             email="test-email",
             phone="test-phone",
             tenant="test-tenant",
             queue="test-queue",
             status=1):
    """Get a test user."""
    user = User(name, password, email, phone, tenant, queue, status)
    user.create_if_not_exists()
    return user


def test_create_user():
    """Test weather client could connect java gate way or not."""
    user = User(name="test-name", password="test-password", email="test-email", phone="test-phone",
                tenant="test-tenant", queue="test-queue", status=1)
    user.create_if_not_exists()
    assert user.user_id is not None


def test_get_user():
    """Test get user from java gateway."""
    user = get_user()
    user_ = User(user.name)
    user_.get_user(user.user_id)
    assert user_.password == user.password
    assert user_.email == user.email
    assert user_.phone == user.phone
    assert user_.tenant == user.tenant
    assert user_.queue == user.queue
    assert user_.status == user.status


def test_update_user():
    """Test update user from java gateway."""
    user = get_user()
    user.update(password="test-password-updated", email="test-email-updated", phone="test-phone-updated",
                tenant="test-tenant-updated", queue="test-queue-updated", status=2)
    user_ = User(user.name)
    User(user.name).get_user(user.user_id)
    assert user_.password == "test-password-updated"
    assert user_.email == "test-email-updated"
    assert user_.phone == "test-phone-updated"
    assert user_.tenant == "test-tenant-updated"
    assert user_.queue == "test-queue-updated"
    assert user_.status == 2


def test_delete_user():
    """Test delete user from java gateway."""
    user = get_user()
    user.delete()
    assert user.user_id is None
