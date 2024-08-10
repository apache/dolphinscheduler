variable "name" {
  description = "Name of the VPC and EKS Cluster"
  default     = "dolphinscheduler"
  type        = string
}

variable "region" {
  description = "Region"
  default     = "us-east-1"
  type        = string
}

variable "eks_cluster_version" {
  description = "EKS Cluster version"
  default     = "1.30"
  type        = string
}

variable "vpc_cidr" {
  description = "VPC CIDR"
  default     = "10.0.0.0/16"
  type        = string
}

# Routable Public subnets with NAT Gateway and Internet Gateway. Not required for fully private clusters
variable "public_subnets" {
  description = "Public Subnets CIDRs. 62 IPs per Subnet/AZ"
  default     = ["10.0.0.0/26", "10.0.0.64/26"]
  type        = list(string)
}

# Routable Private subnets only for Private NAT Gateway -> Transit Gateway -> Second VPC for overlapping overlapping CIDRs
variable "private_subnets" {
  description = "Private Subnets CIDRs. 254 IPs per Subnet/AZ for Private NAT + NLB + Dolphinscheduler + EC2 Jumphost etc."
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
  type        = list(string)
}

# Routable Private subnets only for Private NAT Gateway -> Transit Gateway -> Second VPC for overlapping overlapping CIDRs
variable "db_private_subnets" {
  description = "Private Subnets CIDRs. 254 IPs per Subnet/AZ for Dolphinscheduler DB."
  default     = ["10.0.20.0/26", "10.0.21.0/26"]
  type        = list(string)
}

# RFC6598 range 100.64.0.0/10
# Note you can only /16 range to VPC. You can add multiples of /16 if required
variable "secondary_cidr_blocks" {
  description = "Secondary CIDR blocks to be attached to VPC"
  default     = ["100.64.0.0/16"]
  type        = list(string)
}

# EKS Worker nodes and pods will be placed on these subnets. Each Private subnet can get 32766 IPs.
# RFC6598 range 100.64.0.0/10
variable "eks_data_plane_subnet_secondary_cidr" {
  description = "Secondary CIDR blocks. 32766 IPs per Subnet per Subnet/AZ for EKS Node and Pods"
  default     = ["100.64.0.0/17", "100.64.128.0/17"]
  type        = list(string)
}

variable "enable_amazon_prometheus" {
  description = "Enable AWS Managed Prometheus service"
  type        = bool
  default     = true
}

variable "enable_dolphinscheduler" {
  description = "Enable Apache Dolphinscheduler"
  type        = bool
  default     = true
}

