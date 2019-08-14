/*
 * Copyright 2017 StreamSets Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.escheduler.plugin.sdk.util;

import cn.escheduler.plugin.api.ErrorCode;
import cn.escheduler.plugin.api.GenerateResourceBundle;

// we are using the annotation for reference purposes only.
// the annotation processor does not work on this maven project
// we have a hardcoded 'datacollector-resource-bundles.json' file in resources
@GenerateResourceBundle
public enum ContainerError implements ErrorCode {
    // Unchecked exception
    CONTAINER_0000("Runtime exception: {}"),

    // StageContext
    CONTAINER_0001("{}"),
    CONTAINER_0002("{}"),

    CONTAINER_0010("Stage configuration validation error: {}"),
    CONTAINER_0011("Pipeline memory consumption {} MB exceeded allowed memory {} MB. Largest consumer is {} at {} MB. " +
            "Remaining stages: {}"),

    // RequiredFieldsErrorPredicateSink
    CONTAINER_0050("The stage requires records to include the following required fields: '{}'"),

    // PreconditionsErrorPredicateSink
    CONTAINER_0051("Unsatisfied precondition(s) '{}'"),
    CONTAINER_0052("Failed to evaluate precondition '{}': {}"),

    // PipelineManager
    CONTAINER_0100("Cannot set state: {}"),
    CONTAINER_0101("Cannot get state: {}"),
    CONTAINER_0102("Cannot change state from {} to {}"),
    CONTAINER_0103("Cannot set the source offset during a run"),
    CONTAINER_0104("Cannot reset the source offset when the pipeline is running"),
    CONTAINER_0105("Cannot capture a snapshot because the pipeline is not running"),
    CONTAINER_0106("Cannot get error records because the pipeline is not running"),
    CONTAINER_0107("Invalid batch size: {}"),
    CONTAINER_0108("Cannot start the pipeline manager: {}"),
    CONTAINER_0109("Pipeline {} does not exist"),
    CONTAINER_0110("Cannot create pipeline '{}': {}"),
    CONTAINER_0111("Cannot delete errors for pipeline '{}' when the pipeline is running"),
    CONTAINER_0112("Origin Parallelism cannot be less than 1"),
    CONTAINER_0113("Cannot delete history for pipeline '{}' when the pipeline is running"),
    CONTAINER_0114("Error while retrieving state from cache: {}"),
    CONTAINER_0115("Failed to fetch history for pipeline: '{}', '{}' due to: {}"),
    CONTAINER_0116("Cannot load pipeline '{}:{}' configuration: {}"),
    CONTAINER_0117("Could not determine parallelism: {}"),
    CONTAINER_0118("Cannot update the source offset when the pipeline is running"),

    // PipelineRunners
    CONTAINER_0150("Pipeline configuration error: {}"),
    CONTAINER_0151("Pipeline build error: {}"),
    CONTAINER_0152("Stage '{}', instance '{}', variable '{}', value '{}', configuration injection error: {}"),
    CONTAINER_0153("Stage '{}', instance '{}', property '{}' is not configured"),
    CONTAINER_0154("Cannot preview due to the following configuration issues: {}"),
    CONTAINER_0155("Instance '{}' required fields configuration must be a list instead of a '{}'"),
    CONTAINER_0156("Invalid instance '{}'"),
    CONTAINER_0157("Cannot do a preview stage run on an origin, instance '{}'"),
    CONTAINER_0158("Cannot run the pipeline due to validation issues ({} issues)"),
    CONTAINER_0159("Cannot perform raw source preview because pipeline '{}' is empty"),
    CONTAINER_0160("Cannot perform raw source preview until the following required parameters are configured: '{}'"),
    CONTAINER_0161("Stage '{}', instance '{}', variable '{}', configuration injection error: Value List has non-string elements"),
    CONTAINER_0162("Stage '{}', instance '{}', variable '{}', configuration injection error: Value Map has non-string keys"),
    CONTAINER_0163("Stage '{}', instance '{}', variable '{}', configuration injection error: Value Map has non-string values"),
    CONTAINER_0164("Stage '{}', instance '{}', variable '{}', configuration injection error: Value Map as List has non-string elements"),
    CONTAINER_0165("Stage configuration validation issues: {}"),
    CONTAINER_0166("Cannot start pipeline '{}' as there are not enough threads available. The runner.thread.pool.size property in the Data Collector configuration file, $SDC_CONF/sdc.properties, determines the number of threads in the pool that are available to run pipelines."),
    CONTAINER_0167("Cannot preview pipeline with test origin '{}' as it doesn't support events."),

    //PipelineStore
    CONTAINER_0200("Pipeline '{}' does not exist"),
    CONTAINER_0201("Pipeline '{}' already exists"),
    CONTAINER_0202("Cannot create pipeline '{}': {}"),
    CONTAINER_0203("Cannot delete pipeline '{}': {}"),
    CONTAINER_0204("Cannot save pipeline '{}': {}"),
    CONTAINER_0205("The pipeline '{}' has been changed. Reload the page to view or edit the latest version of the pipeline."),
    CONTAINER_0206("Cannot load details for pipeline '{}': {}"),
    CONTAINER_0207("Definition for Stage '{}' from library '{}' with version '{}' is not available"),
    CONTAINER_0208("Pipeline in state '{}' cannot be saved"),
    CONTAINER_0209("Pipeline state file '{}' doesn't exist"),
    CONTAINER_0210("Cannot fetch JSON string: {}"),
    CONTAINER_0211("Pipeline state doesn't exist for pipeline '{}::{}' in execution mode: '{}'"),
    CONTAINER_0212("Cannot save state of pipeline '{}::{}' in execution mode: '{}' as there is already an existing"
            + "pipeline '{}::{}'"),
    CONTAINER_0213("Could not retrieve pipelines from Pipeline Store. See stacktrace for additional details: '{}'"),
    CONTAINER_0214("Can't store new pipeline state: {}"),

    //Previewr
    CONTAINER_0250("Cannot create previewer: '{}'"),

    // AdminResource
    CONTAINER_0300("Reached maximum number of concurrent clients '{}'. Tailing the log through the REST API."),

    //Observer
    CONTAINER_0400("Failed to evaluate expression '{}' for record '{}': {}"),
    CONTAINER_0401("Failed to evaluate expression '{}': {}"),
    CONTAINER_0402("Cannot access alerts because the pipeline is not running"),
    CONTAINER_0403("Cannot load rule definitions for pipeline '{}': {}"),
    CONTAINER_0404("Cannot store rule definitions for pipeline '{}': {}"),
    CONTAINER_0405("Cannot store UI info for pipeline '{}': {}"),
    CONTAINER_0406("Cannot store ACL for pipeline '{}': {}"),

    CONTAINER_0500("EmailSender error: {}"),

    //Snapshot
    CONTAINER_0600("Error retrieving snapshot '{}' for pipeline with name '{}' and revision '{}' : '{}'"),
    CONTAINER_0601("Error deleting snapshot '{}' for pipeline with name '{}' and revision '{}'"),
    CONTAINER_0602("Error persisting snapshot info '{}' for pipeline with name '{}' and revision '{}' : '{}'"),
    CONTAINER_0603("Error persisting snapshot '{}' for pipeline with name '{}' and revision '{}' : '{}'"),
    CONTAINER_0604("Error retrieving snapshot info '{}' for pipeline with name '{}' and revision '{}' : '{}'"),
    CONTAINER_0605("Snapshot must be created before saving"),
    CONTAINER_0606("Snapshot {} for pipeline {} already exists"),

    CONTAINER_0700("Error stage initialization error: {}"),
    CONTAINER_0701("Stage '{}' initialization error: {}"),
    CONTAINER_0702("Pipeline initialization error: {}"),
    CONTAINER_0703("Stats Aggregator stage initialization error: {}"),
    CONTAINER_0704("Can't create additional pipeline runners: {}"),
    CONTAINER_0705("Requested number of runners {} is higher than allowed maximum of {}"),

    //Pipeline Lifecycle events
    CONTAINER_0790("Pipeline lifecycle event stage initialization error: {}"),
    CONTAINER_0791("Pipeline lifecycle event stage run error: {}"),
    CONTAINER_0792("Pipeline lifecycle event stage generated error record: {}"),

    //Runner
    CONTAINER_0800("Pipeline '{}' validation error : {}"),
    CONTAINER_0801("Thread unexpectedly interrupted"),
    CONTAINER_0802("Detected run away pipeline runners (only {} out of {} runners have finished)"),
    CONTAINER_0803("Trying to acquire pipeline runner after the pool was destroyed."),

    //PipelineConfigurationUpgrader
    CONTAINER_0900("Error while upgrading stage configuration from version '{}' to version '{}': {}"),
    CONTAINER_0901("Could not find stage definition for '{}:{}'"),
    CONTAINER_0902("Definition requires at least version {} which is higher then available library supports ({})"),
    CONTAINER_0903("Could not find service definition for '{}'"),
    CONTAINER_0904("Service Upgrader can't register another service."),

    //Email Notifier
    CONTAINER_01000("Error loading email template, reason : {}"),
    CONTAINER_01001("Error sending email : {}"),

    // Remote Control pipelines
    CONTAINER_01100("Cannot perform operation: '{}' on local pipeline '{}'"),
    CONTAINER_01101("Cannot perform operation: '{}' on remote pipeline '{}'"),

    // ACL
    CONTAINER_01200("{} '{}' doesn't have permissions {} on pipeline {}"),
    CONTAINER_01201("Only owner of the pipeline {} or admin is allowed to updated the ACL information"),

    // misc
    CONTAINER_01300("Environment variable 'STREAMSETS_LIBRARIES_EXTRA_DIR' is not set"),
    CONTAINER_01301("Invalid library name '{}'"),

    //LineageEvent problems:
    CONTAINER_01401("Cannot create framework-level event '{}' in a stage"),
    CONTAINER_01402("Cannot create stage-level event '{}' in the framework"),
    CONTAINER_01403("Missing or Empty SpecificAttributes '{}'"),
    CONTAINER_01404("Unknown LineageEventType passed to missingSpecificAttributes() '{}'"),

    CONTAINER_01500("Stage '{}' configuration '{}', EL must resolve to String or to a CredentialValue resolved to '{}'"),


    CONTAINER_01600("Not supported for pipeline execution mode: '{}'"),
    CONTAINER_01601("Start operation is not supported for pipeline '{}' with execution mode: '{}'"),
    CONTAINER_01602("Data Collector cannot connect to Data Collector Edge URL '{}'. Verify that Data Collector Edge is " +
            "running and that the URL is correctly configured in the pipeline general properties."),
    CONTAINER_01603("Starting Edge pipeline failed, status code '{}': {}"),
    CONTAINER_01604("Reset Offset for Edge pipeline failed, status code '{}': {}"),
    CONTAINER_01605("Publishing Edge pipeline failed, status code '{}': {}"),
    ;

    private final String msg;

    ContainerError(String msg) {
        this.msg = msg;
    }


    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getMessage() {
        return msg;
    }

}
