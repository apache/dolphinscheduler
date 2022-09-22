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

import hashlib

import pytest

from pydolphinscheduler.models import User


def md5(str):
    """MD5 a string."""
    hl = hashlib.md5()
    hl.update(str.encode(encoding="utf-8"))
    return hl.hexdigest()


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
    user = User(
        name=name,
        password=password,
        email=email,
        phone=phone,
        tenant=tenant,
        queue=queue,
        status=status,
    )
    user.create_if_not_exists()
    return user


def test_create_user():
    """Test weather client could connect java gate way or not."""
    user = User(
        name="test-name",
        password="test-password",
        email="test-email@abc.com",
        phone="17366637777",
        tenant="test-tenant",
        queue="test-queue",
        status=1,
    )
    user.create_if_not_exists()
    assert user.user_id is not None


def test_get_user():
    """Test get user from java gateway."""
    user = get_user()
    user_ = User.get_user(user.user_id)
    assert user_.password == md5(user.password)
    assert user_.email == user.email
    assert user_.phone == user.phone
    assert user_.status == user.status


def test_update_user():
    """Test update user from java gateway."""
    user = get_user()
    user.update(
        password="test-password-",
        email="test-email-updated@abc.com",
        phone="17366637766",
        tenant="test-tenant-updated",
        queue="test-queue-updated",
        status=2,
    )
    user_ = User.get_user(user.user_id)
    assert user_.password == md5("test-password-")
    assert user_.email == "test-email-updated@abc.com"
    assert user_.phone == "17366637766"
    assert user_.status == 2


def test_delete_user():
    """Test delete user from java gateway."""
    user = get_user()
    user.delete()
    with pytest.raises(AttributeError) as excinfo:
        _ = user.user_id

    assert excinfo.type == AttributeError
