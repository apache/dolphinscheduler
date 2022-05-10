data_path=${data_path}
export MLFLOW_TRACKING_URI=${MLFLOW_TRACKING_URI}
echo $data_path
repo=${repo}
mlflow run $repo -P algorithm=${algorithm} -P data_path=$data_path -P params="${params}" -P param_file=${param_file} -P search_params="${search_params}" -P model_name=${model_name} --experiment-name=${experiment_name}

echo "training finish"
