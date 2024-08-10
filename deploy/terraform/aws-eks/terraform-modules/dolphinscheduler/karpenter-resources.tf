locals {
  namespace = "karpenter-resources"
  version   = "0.0.1"
}

resource "helm_release" "karpenter_resources" {
  for_each                   = var.enable_karpenter_resources ? var.karpenter_resources_helm_config : {}
  name                       = each.key
  repository                 = try(each.value["repository"], null)
  chart                      = try(each.value["chart"], "${path.module}/helm-charts/karpenter-resources")
  version                    = try(each.value["version"], local.version)
  timeout                    = try(each.value["timeout"], 300)
  values                     = try(each.value["values"], null)
  create_namespace           = try(each.value["create_namespace"], true)
  namespace                  = try(each.value["namespace"], local.namespace)
  lint                       = try(each.value["lint"], false)
  description                = try(each.value["description"], "")
  repository_key_file        = try(each.value["repository_key_file"], "")
  repository_cert_file       = try(each.value["repository_cert_file"], "")
  repository_username        = try(each.value["repository_username"], "")
  repository_password        = try(each.value["repository_password"], "")
  verify                     = try(each.value["verify"], false)
  keyring                    = try(each.value["keyring"], "")
  disable_webhooks           = try(each.value["disable_webhooks"], false)
  reuse_values               = try(each.value["reuse_values"], false)
  reset_values               = try(each.value["reset_values"], false)
  force_update               = try(each.value["force_update"], false)
  recreate_pods              = try(each.value["recreate_pods"], false)
  cleanup_on_fail            = try(each.value["cleanup_on_fail"], false)
  max_history                = try(each.value["max_history"], 0)
  atomic                     = try(each.value["atomic"], false)
  skip_crds                  = try(each.value["skip_crds"], false)
  render_subchart_notes      = try(each.value["render_subchart_notes"], true)
  disable_openapi_validation = try(each.value["disable_openapi_validation"], false)
  wait                       = try(each.value["wait"], true)
  wait_for_jobs              = try(each.value["wait_for_jobs"], false)
  dependency_update          = try(each.value["dependency_update"], false)
  replace                    = try(each.value["replace"], false)

  postrender {
    binary_path = try(each.value["postrender"], "")
  }

  dynamic "set" {
    for_each = try(each.value["set"], [])
    content {
      name  = each_item.value.name
      value = each_item.value.value
      type  = try(each_item.value.type, null)
    }
  }

  dynamic "set_sensitive" {
    for_each = try(each.value["set_sensitive"], [])
    content {
      name  = each_item.value.name
      value = each_item.value.value
      type  = try(each_item.value.type, null)
    }
  }
}