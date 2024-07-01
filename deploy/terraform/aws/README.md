# Prerequisites

- [Packer](https://developer.hashicorp.com/packer/downloads)
- [Terraform](https://developer.hashicorp.com/terraform/downloads?ajs_aid=e8824c6e-5f6f-480c-bb7d-27f8c97f8d8d&product_intent=terraform)

# Build AMI

Set necessary variables by creating a file `ds-ami.pkrvars.hcl` and adding the following variables according to your own usage.

```shel
cat <<EOF > ds-ami.pkrvars.hcl
aws_access_key = ""
aws_secret_key = ""
aws_region     = "cn-north-1"


ds_ami_name = "my-test-ds-2"

# If you want to use the official distribution tar, just set the `ds_version` to the one you want.
ds_version  = "3.1.1"

# If you want to use a locally built distribution tar, set the `ds_tar` to the tar file location.
ds_tar      = "~/workspace/dolphinscheduler/dolphinscheduler-dist/target/apache-dolphinscheduler-3.1.3-SNAPSHOT-bin.tar.gz"
EOF
```

Then run the following command to initialize and build a custom AMI.

- If you want to use the official distribution tar.

```shell
packer init --var-file=ds-ami.pkrvars.hcl packer/ds-ami-official.pkr.hcl
packer build --var-file=ds-ami.pkrvars.hcl packer/ds-ami-official.pkr.hcl
```

- If you want to use the locally built distribution tar.

```shell
packer init --var-file=ds-ami.pkrvars.hcl packer/ds-ami-local.pkr.hcl
packer build --var-file=ds-ami.pkrvars.hcl packer/ds-ami-local.pkr.hcl
```

# Create resources

Set necessary variables by creating a file `terraform.tfvars` and adding the following variables according to your own usage.

Make sure `ds_ami_name` is the same as the one in `ds-ami.pkrvars.hcl` above.

```tfvars
cat <<EOF > terraform.tfvars
aws_access_key = ""
aws_secret_key = ""
aws_region     = ""

name_prefix = "test-ds-terraform"
ds_ami_name = "my-test-ds"

ds_component_replicas = {
  master            = 1
  worker            = 1
  alert             = 1
  api               = 1
  standalone_server = 0
}
EOF
```

Then run the following commands to apply necessary resources.

```shell
terraform init -var-file=terraform.tfvars
terraform apply -var-file=terraform.tfvars -auto-approve
```

# Open DolphinScheduler UI

```shell
open http://$(terraform output -json api_server_instance_public_dns | jq -r '.[0]'):12345/dolphinscheduler/ui
```

# Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_aws_access_key"></a> [aws\_access\_key](#input\_aws\_access\_key) | AWS access key | `string` | n/a | yes |
| <a name="input_aws_region"></a> [aws\_region](#input\_aws\_region) | AWS region | `string` | `"cn-north-1"` | no |
| <a name="input_aws_secret_key"></a> [aws\_secret\_key](#input\_aws\_secret\_key) | AWS secret key | `string` | n/a | yes |
| <a name="input_db_instance_class"></a> [db\_instance\_class](#input\_db\_instance\_class) | Database instance class | `string` | `"db.t3.micro"` | no |
| <a name="input_db_password"></a> [db\_password](#input\_db\_password) | Database password | `string` | n/a | yes |
| <a name="input_db_username"></a> [db\_username](#input\_db\_username) | Database username | `string` | `"dolphinscheduler"` | no |
| <a name="input_ds_ami_name"></a> [ds\_ami\_name](#input\_ds\_ami\_name) | Name of DolphinScheduler AMI | `string` | `"dolphinscheduler-ami"` | no |
| <a name="input_ds_component_replicas"></a> [ds\_component\_replicas](#input\_ds\_component\_replicas) | Replicas of the DolphinScheduler Components | `map(number)` | <pre>{<br>  "alert": 1,<br>  "api": 1,<br>  "master": 1,<br>  "standalone_server": 0,<br>  "worker": 1<br>}</pre> | no |
| <a name="input_ds_version"></a> [ds\_version](#input\_ds\_version) | DolphinScheduler Version | `string` | `"3.1.1"` | no |
| <a name="input_name_prefix"></a> [name\_prefix](#input\_name\_prefix) | Name prefix for all resources | `string` | `"dolphinscheduler"` | no |
| <a name="input_private_subnet_cidr_blocks"></a> [private\_subnet\_cidr\_blocks](#input\_private\_subnet\_cidr\_blocks) | Available CIDR blocks for private subnets | `list(string)` | <pre>[<br>  "10.0.101.0/24",<br>  "10.0.102.0/24",<br>  "10.0.103.0/24",<br>  "10.0.104.0/24"<br>]</pre> | no |
| <a name="input_public_subnet_cidr_blocks"></a> [public\_subnet\_cidr\_blocks](#input\_public\_subnet\_cidr\_blocks) | CIDR blocks for the public subnets | `list(string)` | <pre>[<br>  "10.0.1.0/24",<br>  "10.0.2.0/24",<br>  "10.0.3.0/24",<br>  "10.0.4.0/24"<br>]</pre> | no |
| <a name="input_s3_bucket_prefix"></a> [s3\_bucket\_prefix](#input\_s3\_bucket\_prefix) | n/a | `string` | `"dolphinscheduler-test-"` | no |
| <a name="input_subnet_count"></a> [subnet\_count](#input\_subnet\_count) | Number of subnets | `map(number)` | <pre>{<br>  "private": 2,<br>  "public": 1<br>}</pre> | no |
| <a name="input_tags"></a> [tags](#input\_tags) | Tags to apply to all resources | `map(string)` | <pre>{<br>  "Deployment": "Test"<br>}</pre> | no |
| <a name="input_vm_associate_public_ip_address"></a> [vm\_associate\_public\_ip\_address](#input\_vm\_associate\_public\_ip\_address) | Associate a public IP address to the EC2 instance | `map(bool)` | <pre>{<br>  "alert": true,<br>  "api": true,<br>  "master": true,<br>  "standalone_server": true,<br>  "worker": true<br>}</pre> | no |
| <a name="input_vm_data_volume_size"></a> [vm\_data\_volume\_size](#input\_vm\_data\_volume\_size) | Data volume size of the EC2 Instance | `map(number)` | <pre>{<br>  "alert": 10,<br>  "api": 10,<br>  "master": 10,<br>  "standalone_server": 10,<br>  "worker": 10<br>}</pre> | no |
| <a name="input_vm_data_volume_type"></a> [vm\_data\_volume\_type](#input\_vm\_data\_volume\_type) | Data volume type of the EC2 Instance | `map(string)` | <pre>{<br>  "alert": "gp2",<br>  "api": "gp2",<br>  "master": "gp2",<br>  "standalone_server": "gp2",<br>  "worker": "gp2"<br>}</pre> | no |
| <a name="input_vm_instance_type"></a> [vm\_instance\_type](#input\_vm\_instance\_type) | EC2 instance type | `map(string)` | <pre>{<br>  "alert": "t2.micro",<br>  "api": "t2.small",<br>  "master": "t2.medium",<br>  "standalone_server": "t2.small",<br>  "worker": "t2.medium"<br>}</pre> | no |
| <a name="input_vm_root_volume_size"></a> [vm\_root\_volume\_size](#input\_vm\_root\_volume\_size) | Root Volume size of the EC2 Instance | `map(number)` | <pre>{<br>  "alert": 30,<br>  "api": 30,<br>  "master": 30,<br>  "standalone_server": 30,<br>  "worker": 30<br>}</pre> | no |
| <a name="input_vm_root_volume_type"></a> [vm\_root\_volume\_type](#input\_vm\_root\_volume\_type) | Root volume type of the EC2 Instance | `map(string)` | <pre>{<br>  "alert": "gp2",<br>  "api": "gp2",<br>  "master": "gp2",<br>  "standalone_server": "gp2",<br>  "worker": "gp2"<br>}</pre> | no |
| <a name="input_vpc_cidr"></a> [vpc\_cidr](#input\_vpc\_cidr) | CIDR for the VPC | `string` | `"10.0.0.0/16"` | no |
| <a name="input_zookeeper_connect_string"></a> [zookeeper\_connect\_string](#input\_zookeeper\_connect\_string) | Zookeeper connect string, if empty, will create a single-node zookeeper for demonstration, don't use this in production | `string` | `""` | no |

# Outputs

| Name | Description |
|------|-------------|
| <a name="output_alert_server_instance_id"></a> [alert\_server\_instance\_id](#output\_alert\_server\_instance\_id) | Instance IDs of alert instances |
| <a name="output_alert_server_instance_private_ip"></a> [alert\_server\_instance\_private\_ip](#output\_alert\_server\_instance\_private\_ip) | Private IPs of alert instances |
| <a name="output_alert_server_instance_public_dns"></a> [alert\_server\_instance\_public\_dns](#output\_alert\_server\_instance\_public\_dns) | Public domain names of alert instances |
| <a name="output_alert_server_instance_public_ip"></a> [alert\_server\_instance\_public\_ip](#output\_alert\_server\_instance\_public\_ip) | Public IPs of alert instances |
| <a name="output_api_server_instance_id"></a> [api\_server\_instance\_id](#output\_api\_server\_instance\_id) | Instance IDs of api instances |
| <a name="output_api_server_instance_private_ip"></a> [api\_server\_instance\_private\_ip](#output\_api\_server\_instance\_private\_ip) | Private IPs of api instances |
| <a name="output_api_server_instance_public_dns"></a> [api\_server\_instance\_public\_dns](#output\_api\_server\_instance\_public\_dns) | Public domain names of api instances |
| <a name="output_api_server_instance_public_ip"></a> [api\_server\_instance\_public\_ip](#output\_api\_server\_instance\_public\_ip) | Public IPs of api instances |
| <a name="output_db_address"></a> [db\_address](#output\_db\_address) | Database address |
| <a name="output_db_name"></a> [db\_name](#output\_db\_name) | Database name |
| <a name="output_db_port"></a> [db\_port](#output\_db\_port) | Database port |
| <a name="output_master_server_instance_id"></a> [master\_server\_instance\_id](#output\_master\_server\_instance\_id) | Instance IDs of master instances |
| <a name="output_master_server_instance_private_ip"></a> [master\_server\_instance\_private\_ip](#output\_master\_server\_instance\_private\_ip) | Private IPs of master instances |
| <a name="output_master_server_instance_public_dns"></a> [master\_server\_instance\_public\_dns](#output\_master\_server\_instance\_public\_dns) | Public domain names of master instances |
| <a name="output_master_server_instance_public_ip"></a> [master\_server\_instance\_public\_ip](#output\_master\_server\_instance\_public\_ip) | Public IPs of master instances |
| <a name="output_s3_access_key"></a> [s3\_access\_key](#output\_s3\_access\_key) | S3 access key |
| <a name="output_s3_address"></a> [s3\_address](#output\_s3\_address) | S3 address |
| <a name="output_s3_bucket"></a> [s3\_bucket](#output\_s3\_bucket) | S3 bucket name |
| <a name="output_s3_regional_domain_name"></a> [s3\_regional\_domain\_name](#output\_s3\_regional\_domain\_name) | S3 regional domain name |
| <a name="output_s3_secret"></a> [s3\_secret](#output\_s3\_secret) | S3 access secret |
| <a name="output_vm_server_instance_id"></a> [vm\_server\_instance\_id](#output\_vm\_server\_instance\_id) | Instance IDs of standalone instances |
| <a name="output_vm_server_instance_private_ip"></a> [vm\_server\_instance\_private\_ip](#output\_vm\_server\_instance\_private\_ip) | Private IPs of standalone instances |
| <a name="output_vm_server_instance_public_dns"></a> [vm\_server\_instance\_public\_dns](#output\_vm\_server\_instance\_public\_dns) | Public domain names of standalone instances |
| <a name="output_vm_server_instance_public_ip"></a> [vm\_server\_instance\_public\_ip](#output\_vm\_server\_instance\_public\_ip) | Public IPs of standalone instances |
| <a name="output_worker_server_instance_id"></a> [worker\_server\_instance\_id](#output\_worker\_server\_instance\_id) | Instance IDs of worker instances |
| <a name="output_worker_server_instance_private_ip"></a> [worker\_server\_instance\_private\_ip](#output\_worker\_server\_instance\_private\_ip) | Private IPs of worker instances |
| <a name="output_worker_server_instance_public_dns"></a> [worker\_server\_instance\_public\_dns](#output\_worker\_server\_instance\_public\_dns) | Public domain names of worker instances |
| <a name="output_worker_server_instance_public_ip"></a> [worker\_server\_instance\_public\_ip](#output\_worker\_server\_instance\_public\_ip) | Public IPs of worker instances |
| <a name="output_zookeeper_server_instance_id"></a> [zookeeper\_server\_instance\_id](#output\_zookeeper\_server\_instance\_id) | Instance IDs of zookeeper instances |
| <a name="output_zookeeper_server_instance_private_ip"></a> [zookeeper\_server\_instance\_private\_ip](#output\_zookeeper\_server\_instance\_private\_ip) | Private IPs of zookeeper instances |
| <a name="output_zookeeper_server_instance_public_dns"></a> [zookeeper\_server\_instance\_public\_dns](#output\_zookeeper\_server\_instance\_public\_dns) | Public domain names of zookeeper instances |
| <a name="output_zookeeper_server_instance_public_ip"></a> [zookeeper\_server\_instance\_public\_ip](#output\_zookeeper\_server\_instance\_public\_ip) | Public IPs of zookeeper instances |
