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

resource "aws_security_group" "zookeeper_sg" {
  count = var.zookeeper_connect_string != "" ? 0 : 1

  name        = "zookeeper_sg"
  description = "Allow incoming connections"
  vpc_id      = aws_vpc._.id
  ingress {
    from_port = 2181
    to_port   = 2181
    protocol  = "tcp"
    security_groups = [
      aws_security_group.master.id,
      aws_security_group.worker.id,
      aws_security_group.alert.id,
      aws_security_group.api.id,
      aws_security_group.standalone.id
    ]
    description = "Allow incoming HTTP connections"
  }
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow incoming SSH connections (Linux)"
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = merge(var.tags, {
    "Name" = "${var.name_prefix}-zookeeper-sg-${count.index}"
  })
}

data "template_file" "zookeeper_user_data" {
  template = file("templates/zookeeper/cloud-init.yaml")
  vars = {
    "ssh_public_key" = aws_key_pair.key_pair.public_key
  }
}

resource "aws_instance" "zookeeper" {
  count = var.zookeeper_connect_string != "" ? 0 : 1

  ami                         = data.aws_ami.amazon-linux.id
  instance_type               = var.vm_instance_type.standalone_server
  subnet_id                   = aws_subnet.public[0].id
  vpc_security_group_ids      = [aws_security_group.zookeeper_sg[count.index].id]
  source_dest_check           = false
  associate_public_ip_address = var.vm_associate_public_ip_address.standalone_server
  key_name                    = aws_key_pair.key_pair.key_name

  user_data = data.template_file.zookeeper_user_data.rendered

  root_block_device {
    volume_size           = var.vm_root_volume_size.standalone_server
    volume_type           = var.vm_root_volume_type.standalone_server
    delete_on_termination = true
    encrypted             = true
    tags = merge(var.tags, {
      "Name" = "${var.name_prefix}-rbd-zookeeper-${count.index}"
    })
  }

  ebs_block_device {
    device_name           = "/dev/xvda"
    volume_size           = var.vm_data_volume_size.standalone_server
    volume_type           = var.vm_data_volume_type.standalone_server
    encrypted             = true
    delete_on_termination = true
    tags = merge(var.tags, {
      "Name" = "${var.name_prefix}-ebd-zookeeper-${count.index}"
    })
  }

  connection {
    type        = "ssh"
    user        = "ec2-user"
    private_key = tls_private_key.key_pair.private_key_pem
    host        = self.public_ip
    timeout     = "30s"
  }

  provisioner "remote-exec" {
    inline = [
      "cloud-init status --wait",
      "docker run -it --name zookeeper -d -p 2181:2181 zookeeper:3.5"
    ]
  }

  tags = merge(var.tags, {
    "Name" = "${var.name_prefix}-zookeeper-${count.index}"
  })
}
