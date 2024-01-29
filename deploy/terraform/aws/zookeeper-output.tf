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

output "zookeeper_server_instance_id" {
  value = [for vm in aws_instance.zookeeper : vm.id]
  description = "Instance IDs of zookeeper instances"
}
output "zookeeper_server_instance_private_ip" {
  value = [for vm in aws_instance.zookeeper : vm.private_ip]
  description = "Private IPs of zookeeper instances"
}
output "zookeeper_server_instance_public_dns" {
  value = [for vm in aws_instance.zookeeper : vm.public_dns]
  description = "Public domain names of zookeeper instances"
}
output "zookeeper_server_instance_public_ip" {
  value = [for vm in aws_instance.zookeeper : vm.public_ip]
  description = "Public IPs of zookeeper instances"
}
