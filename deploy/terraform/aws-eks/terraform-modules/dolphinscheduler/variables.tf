variable "oidc_provider_arn" {
  description = "The ARN of the cluster OIDC Provider"
  type        = string
}


#---------------------------------------------------
# DOLPHINSCHEDULER
#---------------------------------------------------
variable "enable_dolphinscheduler" {
  description = "Enable Dolphinscheduler add-on"
  type        = bool
  default     = false
}

variable "dolphinscheduler_helm_config" {
  description = "Dolphinscheduler Helm Chart config"
  type        = any
  default     = {}
}

#---------------------------------------------------
# Karpenter Resources
#---------------------------------------------------
variable "enable_karpenter_resources" {
  description = "Enable Karpenter Resources (NodePool and EC2NodeClass)"
  type        = bool
  default     = false
}

variable "karpenter_resources_helm_config" {
  description = "Karpenter Resources Helm Chart config"
  type        = any
  default     = {}
}

