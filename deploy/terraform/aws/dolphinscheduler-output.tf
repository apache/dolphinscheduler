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

output "vm_server_instance_id" {
  value = [for vm in aws_instance.standalone_server : vm.id]
  description = "Instance IDs of standalone instances"
}
output "vm_server_instance_private_ip" {
  value = [for vm in aws_instance.standalone_server : vm.private_ip]
  description = "Private IPs of standalone instances"
}
output "vm_server_instance_public_dns" {
  value = [for vm in aws_instance.standalone_server : vm.public_dns]
  description = "Public domain names of standalone instances"
}
output "vm_server_instance_public_ip" {
  value = [for vm in aws_instance.standalone_server : vm.public_ip]
  description = "Public IPs of standalone instances"
}

output "master_server_instance_id" {
  value = [for vm in aws_instance.master : vm.id]
  description = "Instance IDs of master instances"
}
output "master_server_instance_private_ip" {
  value = [for vm in aws_instance.master : vm.private_ip]
  description = "Private IPs of master instances"
}
output "master_server_instance_public_dns" {
  value = [for vm in aws_instance.master : vm.public_dns]
  description = "Public domain names of master instances"
}
output "master_server_instance_public_ip" {
  value = [for vm in aws_instance.master : vm.public_ip]
  description = "Public IPs of master instances"
}

output "worker_server_instance_id" {
  value = [for vm in aws_instance.worker : vm.id]
  description = "Instance IDs of worker instances"
}
output "worker_server_instance_private_ip" {
  value = [for vm in aws_instance.worker : vm.private_ip]
  description = "Private IPs of worker instances"
}
output "worker_server_instance_public_dns" {
  value = [for vm in aws_instance.worker : vm.public_dns]
  description = "Public domain names of worker instances"
}
output "worker_server_instance_public_ip" {
  value = [for vm in aws_instance.worker : vm.public_ip]
  description = "Public IPs of worker instances"
}

output "api_server_instance_id" {
  value = [for vm in aws_instance.api : vm.id]
  description = "Instance IDs of api instances"
}
output "api_server_instance_private_ip" {
  value = [for vm in aws_instance.api : vm.private_ip]
  description = "Private IPs of api instances"
}
output "api_server_instance_public_dns" {
  value = [for vm in aws_instance.api : vm.public_dns]
  description = "Public domain names of api instances"
}
output "api_server_instance_public_ip" {
  value = [for vm in aws_instance.api : vm.public_ip]
  description = "Public IPs of api instances"
}

output "alert_server_instance_id" {
  value = [for vm in aws_instance.alert : vm.id]
  description = "Instance IDs of alert instances"
}
output "alert_server_instance_private_ip" {
  value = [for vm in aws_instance.alert : vm.private_ip]
  description = "Private IPs of alert instances"
}
output "alert_server_instance_public_dns" {
  value = [for vm in aws_instance.alert : vm.public_dns]
  description = "Public domain names of alert instances"
}
output "alert_server_instance_public_ip" {
  value = [for vm in aws_instance.alert : vm.public_ip]
  description = "Public IPs of alert instances"
}
