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

variable "aws_access_key" {
  type        = string
  description = "AWS access key"
}

variable "aws_secret_key" {
  type        = string
  description = "AWS secret key"
}

variable "aws_region" {
  type        = string
  description = "AWS region"
  default     = "cn-north-1"
}

variable "ds_tar" {
  type        = string
  description = "DolphinScheduler tar file location"
}

variable "ds_ami_name" {
  type        = string
  description = "Name of DolphinScheduler AMI"
  default     = "dolphinscheduler-ami"
}

packer {
  required_plugins {
    amazon = {
      version = ">= 0.0.1"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

source "amazon-ebs" "linux" {
  access_key    = var.aws_access_key
  secret_key    = var.aws_secret_key
  region        = var.aws_region
  ami_name      = var.ds_ami_name
  instance_type = "t2.micro"
  source_ami_filter {
    filters = {
      name                = "al2022-ami-*"
      root-device-type    = "ebs"
      virtualization-type = "hvm"
      architecture        = "x86_64"
    }
    most_recent = true
    owners      = ["amazon"]
  }
  ssh_username = "ec2-user"
}

build {
  name    = "dolphinscheduler-ami"
  sources = ["source.amazon-ebs.linux"]

  provisioner "file" {
    source      = var.ds_tar
    destination = "~/dolphinscheduler.tar.gz"
  }

  provisioner "shell" {
    inline = [
      "sudo yum remove -y java",
      "sudo yum install -y java-1.8.0-amazon-corretto.x86_64",
      "echo 'export JAVA_HOME=/etc/alternatives/jre' | sudo tee /etc/profile.d/java_home.sh",
      "sudo mkdir -p /opt/dolphinscheduler",
      "sudo tar zxvf /home/ec2-user/dolphinscheduler.tar.gz --strip-components 1 -C /opt/dolphinscheduler",
      "sudo find /opt/dolphinscheduler/ -name start.sh | xargs -I{} sudo chmod +x {}",
    ]
  }

}
