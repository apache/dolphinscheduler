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

from typing import List, Dict, Set


class User:
    """Get users according specific pull requests list"""
    def __init__(self, prs: List[Dict]):
        self.prs = prs

    def contribution_num(self) -> Dict:
        res = dict()
        for pr in self.prs:
            user_id = pr['user']['login']
            res[user_id] = res.setdefault(user_id, 0) + 1
        return res

    def contributors(self) -> Set[str]:
        cn = self.contribution_num()
        return {contributor for contributor in cn}
