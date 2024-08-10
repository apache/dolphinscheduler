#---------------------------------------------------------------
# RDS Postgres Database for Apache Dolphinscheduler Metadata
#---------------------------------------------------------------
module "db" {
  name                 = "${local.name}-postgresqlv2"
  count   = var.enable_dolphinscheduler ? 1 : 0
  source  = "terraform-aws-modules/rds-aurora/aws"
  storage_encrypted = true
  # All available versions: https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_PostgreSQL.html#PostgreSQL.Concepts
  engine               = "aurora-postgresql"
  instance_class       = "db.serverless"
  engine_version       = "16.1"
  manage_master_user_password = false
  master_username      = local.dolphinscheduler_name
  master_password      = sensitive(aws_secretsmanager_secret_version.postgres[0].secret_string)
  port                 = 5432
  vpc_id               = module.vpc.vpc_id
  enable_http_endpoint = true

  db_subnet_group_name   = module.vpc.database_subnet_group
  vpc_security_group_ids = [module.security_group[0].security_group_id]

  security_group_rules = {
    vpc_ingress = {
      cidr_blocks = module.vpc.private_subnets_cidr_blocks
    }
  }
  
  instances = {
    one = {}
    two = {}
  }
  monitoring_interval = 60

  apply_immediately   = true
  skip_final_snapshot = true

  serverlessv2_scaling_configuration = {
    min_capacity = 2
    max_capacity = 16
  }

  tags = local.tags
}

#---------------------------------------------------------------
# Apache Dolphinscheduler Postgres Metastore DB Master password
#---------------------------------------------------------------
resource "random_password" "postgres" {
  count   = var.enable_dolphinscheduler ? 1 : 0
  length  = 16
  special = false
}
#tfsec:ignore:aws-ssm-secret-use-customer-key
resource "aws_secretsmanager_secret" "postgres" {
  count                   = var.enable_dolphinscheduler ? 1 : 0
  name                    = "postgres-pass"
  recovery_window_in_days = 0 # Set to zero for this example to force delete during Terraform destroy
}

resource "aws_secretsmanager_secret_version" "postgres" {
  count         = var.enable_dolphinscheduler ? 1 : 0
  secret_id     = aws_secretsmanager_secret.postgres[0].id
  secret_string = random_password.postgres[0].result
}

#---------------------------------------------------------------
# PostgreSQL RDS security group
#---------------------------------------------------------------
module "security_group" {
  count   = var.enable_dolphinscheduler ? 1 : 0
  source  = "terraform-aws-modules/security-group/aws"
  version = "~> 5.0"

  name        = local.name
  description = "Complete PostgreSQL example security group"
  vpc_id      = module.vpc.vpc_id

  # ingress
  ingress_with_cidr_blocks = [
    {
      from_port   = 5432
      to_port     = 5432
      protocol    = "tcp"
      description = "PostgreSQL access from within VPC"
      cidr_blocks = "${module.vpc.vpc_cidr_block},${module.vpc.vpc_secondary_cidr_blocks[0]}"
    },
  ]

  tags = local.tags
}

#---------------------------------------------------------------
# Create Apache Dolphinscheduler Postgres Metastore DB
#---------------------------------------------------------------

resource "kubernetes_secret" "postgres_secret" {
  metadata {
    name = "postgres-secret"
  }

  data = {
    host     = "${module.db[0].cluster_endpoint}"
    port     = "5432"
    user     = "${local.dolphinscheduler_name}"
    password = "${sensitive(aws_secretsmanager_secret_version.postgres[0].secret_string)}"
    connect_database = "postgres"
    dolphinscheduler_database = "${local.dolphinscheduler_name}"
  }
}

resource "kubernetes_job" "create_database" {
  metadata {
    name = "create-database-job"
  }

  spec {
    template {
      metadata {
        name = "create-database"
      }

      spec {
        container {
          name  = "create-database"
          image = "postgres:13"

          env {
            name = "PGHOST"
            value_from {
              secret_key_ref {
                name = kubernetes_secret.postgres_secret.metadata[0].name
                key  = "host"
              }
            }
          }

          env {
            name = "PGPORT"
            value_from {
              secret_key_ref {
                name = kubernetes_secret.postgres_secret.metadata[0].name
                key  = "port"
              }
            }
          }

          env {
            name = "PGUSER"
            value_from {
              secret_key_ref {
                name = kubernetes_secret.postgres_secret.metadata[0].name
                key  = "user"
              }
            }
          }

          env {
            name = "PGPASSWORD"
            value_from {
              secret_key_ref {
                name = kubernetes_secret.postgres_secret.metadata[0].name
                key  = "password"
              }
            }
          }

          env {
            name = "PGDATABASE"
            value_from {
              secret_key_ref {
                name = kubernetes_secret.postgres_secret.metadata[0].name
                key  = "connect_database"
              }
            }
          }

          env {
            name = "DSDATABASE"
            value_from {
              secret_key_ref {
                name = kubernetes_secret.postgres_secret.metadata[0].name
                key  = "dolphinscheduler_database"
              }
            }
          }

          command = [
            "sh", 
            "-c", 
            <<-EOCMD
            psql -v ON_ERROR_STOP=1 --username "$PGUSER" --host "$PGHOST" --command "CREATE DATABASE $DSDATABASE;"
            EOCMD
          ]

        }
        restart_policy = "Never"
      }
    }

    backoff_limit = 4
  }
}

#---------------------------------------------------------------
# Dolphinscheduler  Namespace
#---------------------------------------------------------------
resource "kubernetes_namespace_v1" "dolphinscheduler" {
  count = var.enable_dolphinscheduler ? 1 : 0
  metadata {
    name = local.dolphinscheduler_namespace
  }
  timeouts {
    delete = "15m"
  }
}

#---------------------------------------------------------------
# IRSA module for Dolphinscheduler Master
#---------------------------------------------------------------
resource "kubernetes_service_account_v1" "dolphinscheduler_master" {
  count = var.enable_dolphinscheduler ? 1 : 0
  metadata {
    name        = local.dolphinscheduler_master_service_account
    namespace   = kubernetes_namespace_v1.dolphinscheduler[0].metadata[0].name
    annotations = { "eks.amazonaws.com/role-arn" : module.dolphinscheduler_irsa_master[0].iam_role_arn }
  }

  automount_service_account_token = true
}

resource "kubernetes_secret_v1" "dolphinscheduler_master" {
  count = var.enable_dolphinscheduler? 1 : 0
  metadata {
    name      = "${kubernetes_service_account_v1.dolphinscheduler_master[0].metadata[0].name}-secret"
    namespace = kubernetes_namespace_v1.dolphinscheduler[0].metadata[0].name
    annotations = {
      "kubernetes.io/service-account.name"      = kubernetes_service_account_v1.dolphinscheduler_master[0].metadata[0].name
      "kubernetes.io/service-account.namespace" = kubernetes_namespace_v1.dolphinscheduler[0].metadata[0].name
    }
  }

  type = "kubernetes.io/service-account-token"
}

module "dolphinscheduler_irsa_master" {
  source  = "aws-ia/eks-blueprints-addon/aws"
  version = "~> 1.0" # ensure to update this to the latest/desired version

  count = var.enable_dolphinscheduler ? 1 : 0
  # IAM role for service account (IRSA)
  create_release = false
  create_policy  = false # Policy is created in the next resource

  create_role = var.enable_dolphinscheduler
  role_name   = local.dolphinscheduler_master_service_account

  role_policies = { Dolphinscheduler = aws_iam_policy.dolphinscheduler_master[0].arn }

  oidc_providers = {
    this = {
      provider_arn    = module.eks.oidc_provider_arn
      namespace       = kubernetes_namespace_v1.dolphinscheduler[0].metadata[0].name
      service_account = local.dolphinscheduler_master_service_account
    }
  }
}

resource "aws_iam_policy" "dolphinscheduler_master" {
  count = var.enable_dolphinscheduler ? 1 : 0

  description = "IAM policy for Dolphinscheduler Master Pod"
  name_prefix = local.dolphinscheduler_master_service_account
  path        = "/"
  policy      = data.aws_iam_policy_document.dolphinscheduler_s3_logs[0].json
}

resource "aws_iam_user" "dolphinscheduler_user" {
  name = "dolphinscheduler-user"
}

resource "aws_iam_access_key" "dolphinscheduler_access_key" {
  user = aws_iam_user.dolphinscheduler_user.name
}

resource "aws_iam_user_policy_attachment" "dolphinscheduler_user_policy_attachment" {
  user       = aws_iam_user.dolphinscheduler_user.name
  policy_arn = aws_iam_policy.dolphinscheduler_master[0].arn
}

#---------------------------------------------------------------
# IRSA module for Dolphinscheduler Worker
#---------------------------------------------------------------
resource "kubernetes_service_account_v1" "dolphinscheduler_worker" {
  count = var.enable_dolphinscheduler ? 1 : 0
  metadata {
    name        = local.dolphinscheduler_worker_service_account
    namespace   = kubernetes_namespace_v1.dolphinscheduler[0].metadata[0].name
    annotations = { "eks.amazonaws.com/role-arn" : module.dolphinscheduler_irsa_worker[0].iam_role_arn }
  }

  automount_service_account_token = true
}

resource "kubernetes_secret_v1" "dolphinscheduler_worker" {
  count = var.enable_dolphinscheduler? 1 : 0
  metadata {
    name      = "${kubernetes_service_account_v1.dolphinscheduler_worker[0].metadata[0].name}-secret"
    namespace = kubernetes_namespace_v1.dolphinscheduler[0].metadata[0].name
    annotations = {
      "kubernetes.io/service-account.name"      = kubernetes_service_account_v1.dolphinscheduler_worker[0].metadata[0].name
      "kubernetes.io/service-account.namespace" = kubernetes_namespace_v1.dolphinscheduler[0].metadata[0].name
    }
  }

  type = "kubernetes.io/service-account-token"
}

module "dolphinscheduler_irsa_worker" {
  source  = "aws-ia/eks-blueprints-addon/aws"
  version = "~> 1.0" # ensure to update this to the latest/desired version

  count = var.enable_dolphinscheduler ? 1 : 0
  # IAM role for service account (IRSA)
  create_release = false
  create_policy  = false # Policy is created in the next resource

  create_role = var.enable_dolphinscheduler
  role_name   = local.dolphinscheduler_worker_service_account

  role_policies = { Dolphinscheduler = aws_iam_policy.dolphinscheduler_worker[0].arn }

  oidc_providers = {
    this = {
      provider_arn    = module.eks.oidc_provider_arn
      namespace       = kubernetes_namespace_v1.dolphinscheduler[0].metadata[0].name
      service_account = local.dolphinscheduler_worker_service_account
    }
  }
}

resource "aws_iam_policy" "dolphinscheduler_worker" {
  count = var.enable_dolphinscheduler ? 1 : 0

  description = "IAM policy for Dolphinscheduler Worker Pod"
  name_prefix = local.dolphinscheduler_worker_service_account
  path        = "/"
  policy      = data.aws_iam_policy_document.dolphinscheduler_s3_logs[0].json
}

#---------------------------------------------------------------
# IRSA module for Dolphinscheduler API
#---------------------------------------------------------------
resource "kubernetes_service_account_v1" "dolphinscheduler_api" {
  count = var.enable_dolphinscheduler ? 1 : 0
  metadata {
    name        = local.dolphinscheduler_api_service_account
    namespace   = kubernetes_namespace_v1.dolphinscheduler[0].metadata[0].name
    annotations = { "eks.amazonaws.com/role-arn" : module.dolphinscheduler_irsa_api[0].iam_role_arn }
  }

  automount_service_account_token = true
}

resource "kubernetes_secret_v1" "dolphinscheduler_api" {
  count = var.enable_dolphinscheduler? 1 : 0
  metadata {
    name      = "${kubernetes_service_account_v1.dolphinscheduler_api[0].metadata[0].name}-secret"
    namespace = kubernetes_namespace_v1.dolphinscheduler[0].metadata[0].name
    annotations = {
      "kubernetes.io/service-account.name"      = kubernetes_service_account_v1.dolphinscheduler_api[0].metadata[0].name
      "kubernetes.io/service-account.namespace" = kubernetes_namespace_v1.dolphinscheduler[0].metadata[0].name
    }
  }

  type = "kubernetes.io/service-account-token"
}

module "dolphinscheduler_irsa_api" {
  source  = "aws-ia/eks-blueprints-addon/aws"
  version = "~> 1.0" # ensure to update this to the latest/desired version

  count = var.enable_dolphinscheduler ? 1 : 0
  # IAM role for service account (IRSA)
  create_release = false
  create_policy  = false # Policy is created in the next resource

  create_role = var.enable_dolphinscheduler
  role_name   = local.dolphinscheduler_api_service_account

  role_policies = { Dolphinscheduler = aws_iam_policy.dolphinscheduler_api[0].arn }

  oidc_providers = {
    this = {
      provider_arn    = module.eks.oidc_provider_arn
      namespace       = kubernetes_namespace_v1.dolphinscheduler[0].metadata[0].name
      service_account = local.dolphinscheduler_api_service_account
    }
  }
}

resource "aws_iam_policy" "dolphinscheduler_api" {
  count = var.enable_dolphinscheduler ? 1 : 0

  description = "IAM policy for Dolphinscheduler API Pod"
  name_prefix = local.dolphinscheduler_api_service_account
  path        = "/"
  policy      = data.aws_iam_policy_document.dolphinscheduler_s3_logs[0].json
}


#---------------------------------------------------------------
# IRSA module for Dolphinscheduler Alert
#---------------------------------------------------------------
resource "kubernetes_service_account_v1" "dolphinscheduler_alert" {
  count = var.enable_dolphinscheduler ? 1 : 0
  metadata {
    name        = local.dolphinscheduler_alert_service_account
    namespace   = kubernetes_namespace_v1.dolphinscheduler[0].metadata[0].name
    annotations = { "eks.amazonaws.com/role-arn" : module.dolphinscheduler_irsa_alert[0].iam_role_arn }
  }

  automount_service_account_token = true
}

resource "kubernetes_secret_v1" "dolphinscheduler_alert" {
  count = var.enable_dolphinscheduler? 1 : 0
  metadata {
    name      = "${kubernetes_service_account_v1.dolphinscheduler_alert[0].metadata[0].name}-secret"
    namespace = kubernetes_namespace_v1.dolphinscheduler[0].metadata[0].name
    annotations = {
      "kubernetes.io/service-account.name"      = kubernetes_service_account_v1.dolphinscheduler_alert[0].metadata[0].name
      "kubernetes.io/service-account.namespace" = kubernetes_namespace_v1.dolphinscheduler[0].metadata[0].name
    }
  }

  type = "kubernetes.io/service-account-token"
}

module "dolphinscheduler_irsa_alert" {
  source  = "aws-ia/eks-blueprints-addon/aws"
  version = "~> 1.0" # ensure to update this to the latest/desired version

  count = var.enable_dolphinscheduler ? 1 : 0
  # IAM role for service account (IRSA)
  create_release = false
  create_policy  = false # Policy is created in the next resource

  create_role = var.enable_dolphinscheduler
  role_name   = local.dolphinscheduler_alert_service_account

  role_policies = { Dolphinscheduler = aws_iam_policy.dolphinscheduler_alert[0].arn }

  oidc_providers = {
    this = {
      provider_arn    = module.eks.oidc_provider_arn
      namespace       = kubernetes_namespace_v1.dolphinscheduler[0].metadata[0].name
      service_account = local.dolphinscheduler_alert_service_account
    }
  }
}

resource "aws_iam_policy" "dolphinscheduler_alert" {
  count = var.enable_dolphinscheduler ? 1 : 0

  description = "IAM policy for Dolphinscheduler Alert Pod"
  name_prefix = local.dolphinscheduler_alert_service_account
  path        = "/"
  policy      = data.aws_iam_policy_document.dolphinscheduler_s3_logs[0].json
}

#---------------------------------------------------------------
# EFS Filesystem for Dolphinscheduler Persistent Storage
#---------------------------------------------------------------
resource "aws_efs_file_system" "efs" {
  count          = var.enable_dolphinscheduler ? 1 : 0
  creation_token = "efs"
  encrypted      = true

  tags = local.tags
}

resource "aws_efs_mount_target" "efs_mt" {
  count = var.enable_dolphinscheduler ? length(var.eks_data_plane_subnet_secondary_cidr) : 0

  file_system_id  = aws_efs_file_system.efs[0].id
  subnet_id       = compact([for subnet_id, cidr_block in zipmap(module.vpc.private_subnets, module.vpc.private_subnets_cidr_blocks) : substr(cidr_block, 0, 4) == "100." ? subnet_id : null])[count.index]
  security_groups = [aws_security_group.efs[0].id]
}

resource "aws_security_group" "efs" {
  count       = var.enable_dolphinscheduler ? 1 : 0
  name        = "${local.name}-efs"
  description = "Allow inbound NFS traffic from private subnets of the VPC"
  vpc_id      = module.vpc.vpc_id

  ingress {
    description = "Allow NFS 2049/tcp"
    cidr_blocks = module.vpc.private_subnets_cidr_blocks
    from_port   = 2049
    to_port     = 2049
    protocol    = "tcp"
  }

  tags = local.tags
}

#---------------------------------------------------------------
# EFS Storage Class
#---------------------------------------------------------------
resource "kubectl_manifest" "efs_sc" {
  count     = var.enable_dolphinscheduler ? 1 : 0
  yaml_body = <<-YAML
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: ${local.efs_storage_class}
provisioner: efs.csi.aws.com
parameters:
  provisioningMode: efs-ap
  fileSystemId: ${aws_efs_file_system.efs[0].id}
  directoryPerms: "700"
YAML

  depends_on = [module.eks.cluster_name]
}


#---------------------------------------------------------------
# S3 log bucket for Dolphinscheduler Logs
#---------------------------------------------------------------

#tfsec:ignore:*
module "dolphinscheduler_s3_bucket" {
  count   = var.enable_dolphinscheduler? 1 : 0
  source  = "terraform-aws-modules/s3-bucket/aws"
  version = "~> 3.0"

  bucket_prefix = "${local.name}-logs-"

  # For example only - please evaluate for your environment
  force_destroy = true

  server_side_encryption_configuration = {
    rule = {
      apply_server_side_encryption_by_default = {
        sse_algorithm = "AES256"
      }
    }
  }

  tags = local.tags
}


#---------------------------------------------------------------
# Example IAM policy for Dolphinscheduler S3 logging
#---------------------------------------------------------------
data "aws_iam_policy_document" "dolphinscheduler_s3_logs" {
  count = var.enable_dolphinscheduler ? 1 : 0
  statement {
    sid       = ""
    effect    = "Allow"
    resources = ["arn:${data.aws_partition.current.partition}:s3:::${module.dolphinscheduler_s3_bucket[0].s3_bucket_id}"]

    actions = [
      "s3:ListBucket"
    ]
  }
  statement {
    sid       = ""
    effect    = "Allow"
    resources = ["arn:${data.aws_partition.current.partition}:s3:::${module.dolphinscheduler_s3_bucket[0].s3_bucket_id}/*"]

    actions = [
      "s3:GetObject",
      "s3:PutObject",
    ]
  }
}

