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

# Cluster monitoring

The production page is `/monitor/cluster`. It queries `/monitor/API`, `/monitor/MASTER`, `/monitor/WORKER`, and `/monitor/ALERT_SERVER` using the standard authenticated API client. It contains no mock data or separate preview entry.

`cluster-data.ts` decodes heartbeat snapshots, converts utilization ratios to percentages, and keeps failed or malformed metadata unknown. API HTTP addresses come from `httpPort`; their registered RPC addresses remain available in node details. HA roles are never inferred from health, and missing role data stays unknown.

Worker groups use the `workerGroup` reported by each Worker. Workers without group metadata are shown under the unavailable-group label rather than being assigned to `default`.

Running instances use the existing workflow/task executor endpoints and retain administrator-only actions. The server continues enforcing its existing permissions. Instance links resolve through the application router, including its production base URL. Request failures remain distinct from empty results.

API metadata RPC listens on `api.rpc-port` (default `12346`), using the existing RPC authentication settings. See the [monitoring guide](../../../../../docs/docs/en/guide/monitor.md) for deployment requirements.
