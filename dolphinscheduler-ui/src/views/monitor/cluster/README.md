<!--
SPDX-License-Identifier: Apache-2.0

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

# Cluster overview preview

This is a UI preview for [#18829](https://github.com/apache/dolphinscheduler/issues/18829), using simulated service metadata and running instances. It does not replace the production monitoring routes or call the monitoring APIs yet.

From `dolphinscheduler-ui`:

```sh
pnpm install --frozen-lockfile
pnpm dev --host 127.0.0.1 --port 5190
```

Open `http://127.0.0.1:5190/cluster-preview.html`. The preview entry loads the same Vue component intended for the cluster overview; the mock data and surrounding preview shell are separate from the production application entry.

The preview includes:

- API → Master → Worker topology with shared connections and expandable Worker groups.
- CPU and memory utilization, unavailable metadata, and independent Alert Server Active/Standby roles.
- Running workflow and task lists for Masters, task lists for Workers, and links to workflow instance detail paths. These links describe production routes; this standalone preview does not provide workflow detail pages.
- Light and dark themes and layouts for narrow windows.

`ClusterOverview` takes service snapshots and a running-instance query callback. Its executor actions require `canQueryExecutors`; production integration must derive that value from the user's permissions and retain the existing server-side authorization checks. Query failures are displayed explicitly rather than as empty lists.

Before this becomes a production feature:

- Implement API server discovery and connect real service metadata, including metadata freshness and HA roles.
- Connect the existing `/monitor/masters/workflow-executors` and `/monitor/workers/task-executors` queries. The existing Master page also supports querying tasks on a Master.
- Add the cluster route and menu, replace the Master/Worker entries, remove the DB entry, and define redirects for existing links.
- Add English and Chinese translations and production integration coverage.
