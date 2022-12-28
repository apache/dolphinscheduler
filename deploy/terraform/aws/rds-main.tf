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

resource "aws_security_group" "database_sg" {
  name        = "dolphinscheduler-database"
  vpc_id      = aws_vpc._.id
  description = "Allow all inbound for Postgres"
  ingress {
    from_port = 5432
    to_port   = 5432
    protocol  = "tcp"
    security_groups = [
      aws_security_group.master.id,
      aws_security_group.worker.id,
      aws_security_group.alert.id,
      aws_security_group.api.id,
      aws_security_group.standalone.id
    ]
  }
}

resource "aws_db_subnet_group" "database_subnet_group" {
  name       = "dolphinscheduler-database_subnet_group"
  subnet_ids = [for subnet in aws_subnet.private : subnet.id]
}

resource "aws_db_instance" "database" {
  identifier             = "dolphinscheduler"
  db_name                = "dolphinscheduler"
  instance_class         = var.db_instance_class
  allocated_storage      = 5
  engine                 = "postgres"
  engine_version         = "14.5"
  skip_final_snapshot    = true
  db_subnet_group_name   = aws_db_subnet_group.database_subnet_group.id
  publicly_accessible    = true
  vpc_security_group_ids = [aws_security_group.database_sg.id]
  username               = var.db_username
  password               = var.db_password
}
