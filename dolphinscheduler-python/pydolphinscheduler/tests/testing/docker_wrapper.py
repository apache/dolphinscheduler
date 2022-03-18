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

"""Wrap docker commands for easier create docker container."""

import time
from typing import Optional

import docker
from docker.errors import ImageNotFound
from docker.models.containers import Container


class DockerWrapper:
    """Wrap docker commands for easier create docker container.

    :param image: The image to create docker container.
    """

    def __init__(self, image: str, container_name: str):
        self._client = docker.from_env()
        self.image = image
        self.container_name = container_name

    def run(self, *args, **kwargs) -> Container:
        """Create and run a new container.

        This method would return immediately after the container started, if you wish it return container
        object when specific service start, you could see :func:`run_until_log` which return container
        object when specific output log appear in docker.
        """
        if not self.images_exists:
            raise ValueError("Docker image named %s do not exists.", self.image)
        return self._client.containers.run(
            image=self.image, name=self.container_name, detach=True, *args, **kwargs
        )

    def run_until_log(
        self, log: str, remove_exists: Optional[bool] = True, *args, **kwargs
    ) -> Container:
        """Create and run a new container, return when specific log appear.

        It will call :func:`run` inside this method. And after container started, it would not
        return it immediately but run command `docker logs` to see whether specific log appear.
        It will raise `RuntimeError` when 10 minutes after but specific log do not appear.
        """
        if remove_exists:
            self.remove_container()

        log_byte = str.encode(log)
        container = self.run(*args, **kwargs)

        timeout_threshold = 10 * 60
        start_time = time.time()
        while time.time() <= start_time + timeout_threshold:
            if log_byte in container.logs(tail=1000):
                break
            time.sleep(2)
        # Stop container and raise error when reach timeout threshold but do not appear specific log output
        else:
            container.remove(force=True)
            raise RuntimeError(
                "Can not capture specific log `%s` in %d seconds, remove container.",
                (log, timeout_threshold),
            )
        return container

    def remove_container(self):
        """Remove container which already running."""
        containers = self._client.containers.list(
            all=True, filters={"name": self.container_name}
        )
        if containers:
            for container in containers:
                container.remove(force=True)

    @property
    def images_exists(self) -> bool:
        """Check whether the image exists in local docker repository or not."""
        try:
            self._client.images.get(self.image)
            return True
        except ImageNotFound:
            return False
