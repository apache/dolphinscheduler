terraform {
  required_version = ">= 1.0.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 5.58.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = ">= 2.30.0"
    }
    helm = {
      source  = "hashicorp/helm"
      version = ">= 2.14.0"
    }
    kubectl = {
      source  = "gavinbunney/kubectl"
      version = ">= 1.14.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "3.6.2"
    }
  }
}
