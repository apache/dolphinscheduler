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

output "s3_address" {
  value = module.s3_bucket.s3_bucket_bucket_domain_name
  description = "S3 address"
}

output "s3_access_key" {
  value = aws_iam_access_key.s3.id
  description = "S3 access key"
}

output "s3_secret" {
  value     = aws_iam_access_key.s3.secret
  sensitive = true
  description = "S3 access secret"
}

output "s3_bucket" {
  value = module.s3_bucket.s3_bucket_id
  description = "S3 bucket name"
}

output "s3_regional_domain_name" {
  value = module.s3_bucket.s3_bucket_bucket_regional_domain_name
  description = "S3 regional domain name"
}
