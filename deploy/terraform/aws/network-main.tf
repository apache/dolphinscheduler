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

resource "aws_vpc" "_" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  tags = merge(var.tags, {
    "Name" = "${var.name_prefix}-vpc"
  })
}

resource "aws_internet_gateway" "_" {
  vpc_id = aws_vpc._.id
  tags = merge(var.tags, {
    "Name" = "${var.name_prefix}-ig"
  })
}

resource "aws_subnet" "public" {
  count             = var.subnet_count.public
  vpc_id            = aws_vpc._.id
  cidr_block        = var.public_subnet_cidr_blocks[count.index]
  availability_zone = data.aws_availability_zones.available.names[count.index]
  tags = merge(var.tags, {
    "Name" = "${var.name_prefix}-public-subnet-${count.index}"
  })
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc._.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway._.id
  }
  tags = merge(var.tags, {
    "Name" = "${var.name_prefix}-public-rt"
  })
}

resource "aws_route_table_association" "public" {
  count          = var.subnet_count.public
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

resource "aws_subnet" "private" {
  count             = var.subnet_count.private
  vpc_id            = aws_vpc._.id
  cidr_block        = var.private_subnet_cidr_blocks[count.index]
  availability_zone = data.aws_availability_zones.available.names[count.index]
  tags = merge(var.tags, {
    "Name" = "${var.name_prefix}-private-subnet-${count.index}"
  })
}

resource "aws_route_table" "private" {
  vpc_id = aws_vpc._.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway._.id
  }
  tags = merge(var.tags, {
    "Name" = "${var.name_prefix}-private-rt"
  })
}

resource "aws_route_table_association" "private" {
  count          = var.subnet_count.private
  subnet_id      = aws_subnet.private[count.index].id
  route_table_id = aws_route_table.private.id
}
