#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

{{/* vim: set filetype=mustache: */}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "dolphinscheduler.fullname" -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default docker image fullname.
*/}}
{{- define "dolphinscheduler.image.fullname" -}}
{{- .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion -}}
{{- end -}}

{{/*
Create a default common labels.
*/}}
{{- define "dolphinscheduler.common.labels" -}}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/version: {{ .Chart.AppVersion }}
{{- end -}}

{{/*
Create a master labels.
*/}}
{{- define "dolphinscheduler.master.labels" -}}
app.kubernetes.io/name: {{ include "dolphinscheduler.fullname" . }}-master
app.kubernetes.io/component: master
{{ include "dolphinscheduler.common.labels" . }}
{{- end -}}

{{/*
Create a worker labels.
*/}}
{{- define "dolphinscheduler.worker.labels" -}}
app.kubernetes.io/name: {{ include "dolphinscheduler.fullname" . }}-worker
app.kubernetes.io/component: worker
{{ include "dolphinscheduler.common.labels" . }}
{{- end -}}

{{/*
Create an alert labels.
*/}}
{{- define "dolphinscheduler.alert.labels" -}}
app.kubernetes.io/name: {{ include "dolphinscheduler.fullname" . }}-alert
app.kubernetes.io/component: alert
{{ include "dolphinscheduler.common.labels" . }}
{{- end -}}

{{/*
Create an api labels.
*/}}
{{- define "dolphinscheduler.api.labels" -}}
app.kubernetes.io/name: {{ include "dolphinscheduler.fullname" . }}-api
app.kubernetes.io/component: api
{{ include "dolphinscheduler.common.labels" . }}
{{- end -}}

{{/*
Create a default fully qualified postgresql name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "dolphinscheduler.postgresql.fullname" -}}
{{- $name := default "postgresql" .Values.postgresql.nameOverride -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified zookkeeper name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "dolphinscheduler.zookeeper.fullname" -}}
{{- $name := default "zookeeper" .Values.zookeeper.nameOverride -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified zookkeeper quorum.
*/}}
{{- define "dolphinscheduler.zookeeper.quorum" -}}
{{- $port := default "2181" (.Values.zookeeper.service.port | toString) -}}
{{- printf "%s:%s" (include "dolphinscheduler.zookeeper.fullname" .) $port -}}
{{- end -}}

{{/*
Create a database environment variables.
*/}}
{{- define "dolphinscheduler.database.env_vars" -}}
- name: DATABASE_TYPE
  {{- if .Values.postgresql.enabled }}
  value: "postgresql"
  {{- else }}
  value: {{ .Values.externalDatabase.type | quote }}
  {{- end }}
- name: DATABASE_DRIVER
  {{- if .Values.postgresql.enabled }}
  value: "org.postgresql.Driver"
  {{- else }}
  value: {{ .Values.externalDatabase.driver | quote }}
  {{- end }}
- name: DATABASE_HOST
  {{- if .Values.postgresql.enabled }}
  value: {{ template "dolphinscheduler.postgresql.fullname" . }}
  {{- else }}
  value: {{ .Values.externalDatabase.host | quote }}
  {{- end }}
- name: DATABASE_PORT
  {{- if .Values.postgresql.enabled }}
  value: "5432"
  {{- else }}
  value: {{ .Values.externalDatabase.port | quote }}
  {{- end }}
- name: DATABASE_USERNAME
  {{- if .Values.postgresql.enabled }}
  value: {{ .Values.postgresql.postgresqlUsername }}
  {{- else }}
  value: {{ .Values.externalDatabase.username | quote }}
  {{- end }}
- name: DATABASE_PASSWORD
  valueFrom:
    secretKeyRef:
      {{- if .Values.postgresql.enabled }}
      name: {{ template "dolphinscheduler.postgresql.fullname" . }}
      key: postgresql-password
      {{- else }}
      name: {{ include "dolphinscheduler.fullname" . }}-externaldb
      key: database-password
      {{- end }}
- name: DATABASE_DATABASE
  {{- if .Values.postgresql.enabled }}
  value: {{ .Values.postgresql.postgresqlDatabase }}
  {{- else }}
  value: {{ .Values.externalDatabase.database | quote }}
  {{- end }}
- name: DATABASE_PARAMS
  {{- if .Values.postgresql.enabled }}
  value: "characterEncoding=utf8"
  {{- else }}
  value: {{ .Values.externalDatabase.params | quote }}
  {{- end }}
{{- end -}}

{{/*
Create a registry environment variables.
*/}}
{{- define "dolphinscheduler.registry.env_vars" -}}
- name: REGISTRY_PLUGIN_NAME
  {{- if .Values.zookeeper.enabled }}
  value: "zookeeper"
  {{- else }}
  value: {{ .Values.externalRegistry.registryPluginName }}
  {{- end }}
- name: REGISTRY_SERVERS
  {{- if .Values.zookeeper.enabled }}
  value: {{ template "dolphinscheduler.zookeeper.quorum" . }}
  {{- else }}
  value: {{ .Values.externalRegistry.registryServers }}
  {{- end }}
{{- end -}}

{{/*
Create a common fs_s3a environment variables.
*/}}
{{- define "dolphinscheduler.fs_s3a.env_vars" -}}
{{- if eq (default "HDFS" .Values.common.configmap.RESOURCE_STORAGE_TYPE) "S3" -}}
- name: FS_S3A_SECRET_KEY
  valueFrom:
    secretKeyRef:
      key: fs-s3a-secret-key
      name: {{ include "dolphinscheduler.fullname" . }}-fs-s3a
{{- end -}}
{{- end -}}

{{/*
Create a sharedStoragePersistence volume.
*/}}
{{- define "dolphinscheduler.sharedStorage.volume" -}}
{{- if .Values.common.sharedStoragePersistence.enabled -}}
- name: {{ include "dolphinscheduler.fullname" . }}-shared
  persistentVolumeClaim:
    claimName: {{ include "dolphinscheduler.fullname" . }}-shared
{{- end -}}
{{- end -}}

{{/*
Create a sharedStoragePersistence volumeMount.
*/}}
{{- define "dolphinscheduler.sharedStorage.volumeMount" -}}
{{- if .Values.common.sharedStoragePersistence.enabled -}}
- mountPath: {{ .Values.common.sharedStoragePersistence.mountPath | quote }}
  name: {{ include "dolphinscheduler.fullname" . }}-shared
{{- end -}}
{{- end -}}

{{/*
Create a fsFileResourcePersistence volume.
*/}}
{{- define "dolphinscheduler.fsFileResource.volume" -}}
{{- if .Values.common.fsFileResourcePersistence.enabled -}}
- name: {{ include "dolphinscheduler.fullname" . }}-fs-file
  persistentVolumeClaim:
    claimName: {{ include "dolphinscheduler.fullname" . }}-fs-file
{{- end -}}
{{- end -}}

{{/*
Create a fsFileResourcePersistence volumeMount.
*/}}
{{- define "dolphinscheduler.fsFileResource.volumeMount" -}}
{{- if .Values.common.fsFileResourcePersistence.enabled -}}
- mountPath: {{ default "/dolphinscheduler" .Values.common.configmap.RESOURCE_UPLOAD_PATH | quote }}
  name: {{ include "dolphinscheduler.fullname" . }}-fs-file
{{- end -}}
{{- end -}}
