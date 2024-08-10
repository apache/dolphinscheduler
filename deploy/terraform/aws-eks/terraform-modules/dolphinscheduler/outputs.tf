output "dolphinscheduler" {
  value       = try(helm_release.dolphinscheduler[0].metadata, null)
  description = "Dolphinscheduler Helm Chart metadata"
}
