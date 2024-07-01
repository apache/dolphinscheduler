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

module "s3_bucket" {
  source  = "terraform-aws-modules/s3-bucket/aws"
  version = "~> 3.6"

  bucket_prefix            = var.s3_bucket_prefix
  acl                      = "private"
  control_object_ownership = true
  object_ownership         = "ObjectWriter"
  force_destroy            = true
  attach_policy            = true
  policy                   = data.aws_iam_policy_document.s3.json
}

resource "aws_iam_user" "s3" {
  name = "${var.name_prefix}-s3"
  path = "/dolphinscheduler/"
}

resource "aws_iam_access_key" "s3" {
  user = aws_iam_user.s3.name
}

data "aws_iam_policy_document" "s3" {
  statement {
    principals {
      type        = "AWS"
      identifiers = [aws_iam_user.s3.arn]
    }

    actions = ["s3:*"]

    resources = [
      "${module.s3_bucket.s3_bucket_arn}",
      "${module.s3_bucket.s3_bucket_arn}/*"
    ]
  }
}

resource "aws_iam_user_policy" "s3" {
  name = "${var.name_prefix}-s3"
  user = aws_iam_user.s3.name

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = [
          "s3:*",
        ]
        Effect   = "Allow"
        Resource = "*"
      },
    ]
  })
}
