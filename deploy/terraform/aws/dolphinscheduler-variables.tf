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

variable "ds_version" {
  type        = string
  description = "DolphinScheduler Version"
  default     = "3.1.1"
}

variable "ds_ami_name" {
  type        = string
  description = "Name of DolphinScheduler AMI"
  default     = "dolphinscheduler-ami"
}

variable "ds_component_replicas" {
  type        = map(number)
  description = "Replicas of the DolphinScheduler Components"
  default = {
    master            = 1
    worker            = 1
    alert             = 1
    api               = 1
    standalone_server = 0
  }
}

## VM settings

variable "vm_instance_type" {
  type        = map(string)
  description = "EC2 instance type"
  default = {
    master            = "t2.medium"
    worker            = "t2.medium"
    alert             = "t2.micro"
    api               = "t2.small"
    standalone_server = "t2.small"
  }
}

variable "vm_associate_public_ip_address" {
  type        = map(bool)
  description = "Associate a public IP address to the EC2 instance"
  default = {
    master            = true
    worker            = true
    alert             = true
    api               = true
    standalone_server = true
  }
}

variable "vm_root_volume_size" {
  type        = map(number)
  description = "Root Volume size of the EC2 Instance"
  default = {
    master            = 30
    worker            = 30
    alert             = 30
    api               = 30
    standalone_server = 30
  }
}

variable "vm_data_volume_size" {
  type        = map(number)
  description = "Data volume size of the EC2 Instance"
  default = {
    master            = 10
    worker            = 10
    alert             = 10
    api               = 10
    standalone_server = 10
  }
}

variable "vm_root_volume_type" {
  type        = map(string)
  description = "Root volume type of the EC2 Instance"
  default = {
    master            = "gp2"
    worker            = "gp2"
    alert             = "gp2"
    api               = "gp2"
    standalone_server = "gp2"
  }
}

variable "vm_data_volume_type" {
  type        = map(string)
  description = "Data volume type of the EC2 Instance"
  default = {
    master            = "gp2"
    worker            = "gp2"
    alert             = "gp2"
    api               = "gp2"
    standalone_server = "gp2"
  }
}
