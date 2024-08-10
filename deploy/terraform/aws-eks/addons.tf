#---------------------------------------------------------------
# IRSA for EBS CSI Driver
#---------------------------------------------------------------
module "ebs_csi_driver_irsa" {
  source                = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version               = "~> 5.41.0"
  role_name_prefix      = format("%s-%s-", local.name, "ebs-csi-driver")
  attach_ebs_csi_policy = true
  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["kube-system:ebs-csi-controller-sa"]
    }
  }
  tags = local.tags
}
#---------------------------------------------------------------
# EKS Blueprints Kubernetes Addons
#---------------------------------------------------------------
module "eks_blueprints_addons" {
  # Short commit hash from 8th May using git rev-parse --short HEAD
  source  = "aws-ia/eks-blueprints-addons/aws"
  version = "~> 1.2"

  cluster_name      = module.eks.cluster_name
  cluster_endpoint  = module.eks.cluster_endpoint
  cluster_version   = module.eks.cluster_version
  oidc_provider_arn = module.eks.oidc_provider_arn

  #---------------------------------------
  # Amazon EKS Managed Add-ons
  #---------------------------------------
  eks_addons = {
    aws-ebs-csi-driver = {
      service_account_role_arn = module.ebs_csi_driver_irsa.iam_role_arn
    }
    coredns = {
      preserve = true
    }
    vpc-cni = {
      preserve = true
    }
    kube-proxy = {
      preserve = true
    }
  }
  #---------------------------------------
  # EFS CSI driver
  #---------------------------------------
  enable_aws_efs_csi_driver = true

  #---------------------------------------
  # CAUTION: This blueprint creates a PUBLIC facing load balancer to show the Dolphinscheduler Web UI for demos.
  # Please change this to a private load balancer if you are using this in production.
  #---------------------------------------
  enable_aws_load_balancer_controller = true

  #---------------------------------------
  # Karpenter Autoscaler for EKS Cluster
  #---------------------------------------
  enable_karpenter                  = true
  karpenter_enable_spot_termination = true
  karpenter_node = {
    iam_role_additional_policies = {
      AmazonSSMManagedInstanceCore = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
    }
  }
  karpenter = {
    chart_version       = "v0.34.0"
    repository_username = data.aws_ecrpublic_authorization_token.token.user_name
    repository_password = data.aws_ecrpublic_authorization_token.token.password
  }

  #---------------------------------------
  # CloudWatch metrics for EKS
  #---------------------------------------
  enable_aws_cloudwatch_metrics = true
  aws_cloudwatch_metrics = {
    values = [
      <<-EOT
        resources:
          limits:
            cpu: 500m
            memory: 2Gi
          requests:
            cpu: 200m
            memory: 1Gi

        # This toleration allows Daemonset pod to be scheduled on any node, regardless of their Taints.
        tolerations:
          - operator: Exists
      EOT
    ]
  }
}


#---------------------------------------------------------------
# Data on EKS Kubernetes Addons
#---------------------------------------------------------------

module "eks_data_addons" {
  source  = "./terraform-modules/dolphinscheduler"
  #version = "~> 3.2.2" # ensure to update this to the latest/desired version

  oidc_provider_arn = module.eks.oidc_provider_arn

  #---------------------------------------------------------------
  # Dolphinscheduler Add-on
  #---------------------------------------------------------------
  enable_dolphinscheduler = true
  dolphinscheduler_helm_config = {
    namespace = try(kubernetes_namespace_v1.dolphinscheduler[0].metadata[0].name, local.dolphinscheduler_namespace)
    version   = "3.2.2"
    repository = "${path.module}/helm-values"
    values = [templatefile("${path.module}/helm-values/dolphinscheduler/values.yaml", {
      # Dolphinscheduler Postgres RDS Config
      dolphinscheduler_db_user = local.dolphinscheduler_name
      dolphinscheduler_db_pass = sensitive(aws_secretsmanager_secret_version.postgres[0].secret_string)
      dolphinscheduler_db_name = local.dolphinscheduler_name
      dolphinscheduler_db_port = try(module.db[0].port, "5432")
      dolphinscheduler_db_host = try(module.db[0].cluster_endpoint, "")
      #Service Accounts
      master_service_account   = try(kubernetes_service_account_v1.dolphinscheduler_master[0].metadata[0].name, local.dolphinscheduler_master_service_account)
      worker_service_account   = try(kubernetes_service_account_v1.dolphinscheduler_worker[0].metadata[0].name, local.dolphinscheduler_worker_service_account)
      api_service_account      = try(kubernetes_service_account_v1.dolphinscheduler_api[0].metadata[0].name, local.dolphinscheduler_api_service_account)
      alert_service_account    = try(kubernetes_service_account_v1.dolphinscheduler_alert[0].metadata[0].name, local.dolphinscheduler_alert_service_account)
      # S3 bucket config for Logs
      aws_region               = var.region
      s3_bucket_name           = try(module.dolphinscheduler_s3_bucket[0].s3_bucket_id, "")
      s3_access_id            = sensitive(aws_iam_access_key.dolphinscheduler_access_key.id)
      s3_access_secret         = sensitive(aws_iam_access_key.dolphinscheduler_access_key.secret)
      efs_pvc                  = local.efs_pvc
      # Subnet for ELB 
      public_subnets           = try(module.vpc[0].public_subnets, "")
    })]
  }


  #---------------------------------------------------------------
  # Enable Karpenter Resources for Dolphinscheduler
  #---------------------------------------------------------------

  enable_karpenter_resources = true
  karpenter_resources_helm_config = {
    dolphinscheduler-compute-optimized = {
      values = [
        <<-EOT
        name: dolphinscheduler-compute-optimized
        clusterName: ${module.eks.cluster_name}
        ec2NodeClass:
          karpenterRole: ${split("/", module.eks_blueprints_addons.karpenter.node_iam_role_arn)[1]}
          subnetSelectorTerms:
            tags:
              Name: "${module.eks.cluster_name}-private*"
          securityGroupSelectorTerms:
            tags:
              Name: ${module.eks.cluster_name}-node
        nodePool:
          labels:
            - type: karpenter
          requirements:
            - key: "topology.kubernetes.io/zone"
              operator: In
              values: [${local.region}a]
            - key: "node.kubernetes.io/instance-type"
              operator: In
              values: ["r5.large"] 
            - key: "kubernetes.io/arch"
              operator: In
              values: ["amd64"]
            - key: "karpenter.sh/capacity-type"
              operator: In
              values: ["spot", "on-demand"]
      EOT
      ]
    }
  }

}


