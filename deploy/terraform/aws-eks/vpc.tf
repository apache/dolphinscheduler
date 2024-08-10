#---------------------------------------------------------------
# Supporting Network Resources
#---------------------------------------------------------------
# WARNING: This VPC module includes the creation of an Internet Gateway and NAT Gateway, which simplifies cluster deployment and testing, primarily intended for sandbox accounts.
# IMPORTANT: For preprod and prod use cases, it is crucial to consult with your security team and AWS architects to design a private infrastructure solution that aligns with your security requirements

module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.0"

  name = local.name
  cidr = var.vpc_cidr
  azs  = local.azs

  # Secondary CIDR block attached to VPC for EKS Control Plane ENI + Nodes + Pods
  secondary_cidr_blocks = var.secondary_cidr_blocks

  # 1/ RFC6598 range 100.64.0.0/16 for EKS Data Plane for two subnets(32766 IPs per Subnet) across two AZs for EKS Control Plane ENI + Nodes + Pods
  # 2/ Two private Subnets with RFC1918 private IPv4 address range for Private NAT + NLB + Dolphinscheduler + EC2 Jumphost etc.
  private_subnets = concat(var.private_subnets, var.eks_data_plane_subnet_secondary_cidr)

  # ------------------------------
  # Optional Public Subnets for NAT and IGW for PoC/Dev/Test environments
  # Public Subnets can be disabled while deploying to Production and use Private NAT + TGW
  public_subnets = var.public_subnets

  # ------------------------------
  # Private Subnets for Dolphinscheduler metadata store
  database_subnets                   = var.db_private_subnets
  create_database_subnet_group       = true
  create_database_subnet_route_table = true

  enable_nat_gateway   = true
  single_nat_gateway   = true
  enable_dns_hostnames = true
  #-------------------------------

  public_subnet_tags = {
    "kubernetes.io/role/elb" = 1
  }

  private_subnet_tags = {
    "kubernetes.io/role/internal-elb" = 1
    # Tags subnets for Karpenter auto-discovery
    "karpenter.sh/discovery" = local.name
  }

  tags = local.tags
}
