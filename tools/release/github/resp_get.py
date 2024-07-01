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

"""Github utils get HTTP response."""

import copy
import json
from typing import Dict, List, Optional

import requests


class RespGet:
    """Get response from GitHub restful API.

    :param url: URL to requests GET method.
    :param headers: headers for HTTP requests.
    :param param:  param for HTTP requests.
    """

    def __init__(self, url: str, headers: dict, param: Optional[dict] = None):
        self.url = url
        self.headers = headers
        self.param = param

    @staticmethod
    def get(url: str, headers: dict, params: Optional[dict] = None) -> Dict:
        """Get single response dict from HTTP requests by given condition."""
        resp = requests.get(url=url, headers=headers, params=params)
        if not resp.ok:
            raise ValueError("Requests error with", resp.reason)
        return json.loads(resp.content)

    def get_single(self) -> Dict:
        """Get single response dict from HTTP requests by given condition."""
        return self.get(url=self.url, headers=self.headers, params=self.param)

    def get_total(self) -> List[Dict]:
        """Get all response dict from HTTP requests by given condition.

        Will change page number until no data return.
        """
        total = []
        curr_param = copy.deepcopy(self.param)
        while True:
            curr_param["page"] = curr_param.setdefault("page", 0) + 1
            content_dict = self.get(
                url=self.url, headers=self.headers, params=curr_param
            )
            data = content_dict.get("items")
            if not data:
                return total
            total.extend(data)
