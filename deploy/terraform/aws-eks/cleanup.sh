#!/bin/bash
set -o errexit
set -o pipefail

targets=(
  "module.eks_data_addons"
  "module.eks_blueprints_addons"
)

#-------------------------------------------
# Helpful to delete the stuck in "Terminating" namespaces
# Rerun the cleanup.sh script to detect and delete the stuck resources
#-------------------------------------------

# Function to handle the cleanup of terminating namespaces
cleanup_terminating_namespaces() {
  echo "Checking for terminating namespaces..."

  terminating_namespaces=$(kubectl get namespaces --field-selector status.phase=Terminating -o json | jq -r '.items[].metadata.name')

  if [[ -z $terminating_namespaces ]]; then
      echo "No terminating namespaces found"
  else
    for ns in $terminating_namespaces; do
      echo "Terminating namespace: $ns"
      kubectl get namespace $ns -o json | sed 's/"kubernetes"//' | kubectl replace --raw "/api/v1/namespaces/$ns/finalize" -f -
    done
  fi
}

# Function to handle Terraform destroy with error checking
destroy_target() {
  local target=$1
  echo "Destroying target: $target"
  terraform destroy -target="$target" -auto-approve
  local destroy_output=$(terraform destroy -target="$target" -auto-approve 2>&1)
  if [[ $? -eq 0 && $destroy_output == *"Destroy complete!"* ]]; then
    echo "SUCCESS: Terraform destroy of $target completed successfully"
  else
    echo "FAILED: Terraform destroy of $target failed"
  fi
}

# Function to check if the EKS cluster exists
check_eks_cluster_exists() {
  echo "Checking if EKS cluster exists..."
  eks_cluster_name="your-eks-cluster-name"
  eks_cluster_status=$(aws eks describe-cluster --name "$eks_cluster_name" --query "cluster.status" --output text 2>/dev/null)

  if [[ $? -ne 0 || "$eks_cluster_status" != "ACTIVE" ]]; then
    echo "EKS cluster does not exist or is not in ACTIVE state. Skipping Kubernetes cleanup steps."
    return 1
  fi
  return 0
}

# Ensure cleanup steps are executed even if the script fails
trap 'terraform destroy -auto-approve' EXIT

# Check if the EKS cluster exists before attempting to clean up Kubernetes resources
if check_eks_cluster_exists; then
  cleanup_terminating_namespaces || echo "Namespace cleanup step failed, but continuing..."
else
  echo "Skipping Kubernetes namespace cleanup due to EKS cluster not being available."
fi

# Loop through targets and destroy each
for target in "${targets[@]}"; do
  destroy_target "$target" || echo "Destroy step for $target failed, but continuing..."
done

# Perform a full destroy
echo "Destroying all remaining Terraform resources..."
terraform destroy -auto-approve
destroy_output=$(terraform destroy -auto-approve 2>&1)
if [[ $? -eq 0 && $destroy_output == *"Destroy complete!"* ]]; then
  echo "SUCCESS: Terraform destroy of all targets completed successfully"
else
  echo "FAILED: Terraform destroy of all targets failed"
fi
