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
Create default docker images' fullname.
*/}}
{{- define "dolphinscheduler.image.fullname.master" -}}
{{- .Values.image.registry }}/{{ .Values.image.master }}:{{ .Values.image.tag | default .Chart.AppVersion -}}
{{- end -}}
{{- define "dolphinscheduler.image.fullname.worker" -}}
{{- .Values.image.registry }}/{{ .Values.image.worker }}:{{ .Values.image.tag | default .Chart.AppVersion -}}
{{- end -}}
{{- define "dolphinscheduler.image.fullname.api" -}}
{{- .Values.image.registry }}/{{ .Values.image.api }}:{{ .Values.image.tag | default .Chart.AppVersion -}}
{{- end -}}
{{- define "dolphinscheduler.image.fullname.alert" -}}
{{- .Values.image.registry }}/{{ .Values.image.alert }}:{{ .Values.image.tag | default .Chart.AppVersion -}}
{{- end -}}
{{- define "dolphinscheduler.image.fullname.tools" -}}
{{- .Values.image.registry }}/{{ .Values.image.tools }}:{{ .Values.image.tag | default .Chart.AppVersion -}}
{{- end -}}

{{/*
Create a default common labels.
*/}}
{{- define "dolphinscheduler.common.labels" -}}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
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
Create a default fully qualified mysql name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "dolphinscheduler.mysql.fullname" -}}
{{- $name := default "mysql" .Values.mysql.nameOverride -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified zookeeper name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "dolphinscheduler.zookeeper.fullname" -}}
{{- $name := default "zookeeper" .Values.zookeeper.nameOverride -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified minio name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "dolphinscheduler.minio.fullname" -}}
{{- $name := default "minio" .Values.minio.nameOverride -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified zookkeeper quorum.
*/}}
{{- define "dolphinscheduler.zookeeper.quorum" -}}
{{- $port := default "2181" .Values.zookeeper.service.port | toString -}}
{{- printf "%s:%s" (include "dolphinscheduler.zookeeper.fullname" .) $port -}}
{{- end -}}

{{/*
Create a database environment variables.
*/}}
{{- define "dolphinscheduler.database.env_vars" -}}
- name: DATABASE
  {{- if .Values.postgresql.enabled }}
  value: "postgresql"
  {{- else if .Values.mysql.enabled }}
  value: "mysql"
  {{- else }}
  value: {{ .Values.externalDatabase.type | quote }}
  {{- end }}
{{- if or .Values.mysql.enabled (eq .Values.externalDatabase.type "mysql") }}
- name: SPRING_PROFILES_ACTIVE
  value: mysql
{{- end }}
- name: SPRING_DATASOURCE_URL
  {{- if .Values.postgresql.enabled }}
  value: jdbc:postgresql://{{ template "dolphinscheduler.postgresql.fullname" . }}:5432/{{ .Values.postgresql.postgresqlDatabase }}?{{ .Values.postgresql.params }}
  {{- else if .Values.mysql.enabled }}
  value: jdbc:mysql://{{ template "dolphinscheduler.mysql.fullname" . }}:3306/{{ .Values.mysql.auth.database }}?{{ .Values.mysql.auth.params }}
  {{- else }}
  value: jdbc:{{ .Values.externalDatabase.type }}://{{ .Values.externalDatabase.host }}:{{ .Values.externalDatabase.port }}/{{ .Values.externalDatabase.database }}?{{ .Values.externalDatabase.params }}
  {{- end }}
- name: SPRING_DATASOURCE_USERNAME
  {{- if .Values.postgresql.enabled }}
  value: {{ .Values.postgresql.postgresqlUsername }}
  {{- else if .Values.mysql.enabled }}
  value: {{ .Values.mysql.auth.username }}
  {{- else }}
  value: {{ .Values.externalDatabase.username | quote }}
  {{- end }}
- name: SPRING_DATASOURCE_PASSWORD
  valueFrom:
    secretKeyRef:
      {{- if .Values.postgresql.enabled }}
      name: {{ template "dolphinscheduler.postgresql.fullname" . }}
      key: postgresql-password
      {{- else if .Values.mysql.enabled }}
      name: {{ template "dolphinscheduler.mysql.fullname" . }}
      key: mysql-password
      {{- else }}
      name: {{ include "dolphinscheduler.fullname" . }}-externaldb
      key: database-password
      {{- end }}
- name: SPRING_DATASOURCE_DRIVER-CLASS-NAME
  {{- if .Values.postgresql.enabled }}
  value: {{ .Values.postgresql.driverClassName }}
  {{- else if .Values.mysql.enabled }}
  value: {{ .Values.mysql.driverClassName }}
  {{- else }}
  value: {{ .Values.externalDatabase.driverClassName | quote }}
  {{- end }}
{{- end -}}

{{/*
Create a security environment variables.
*/}}
{{- define "dolphinscheduler.security.env_vars" -}}
- name: SECURITY_AUTHENTICATION_TYPE
  value: {{ .Values.security.authentication.type | quote }}
{{- if eq .Values.security.authentication.type "LDAP" }}
- name: SECURITY_AUTHENTICATION_LDAP_URLS
  value: {{ .Values.security.authentication.ldap.urls | quote }}
- name: SECURITY_AUTHENTICATION_LDAP_BASE_DN
  value: {{ .Values.security.authentication.ldap.basedn | quote }}
- name: SECURITY_AUTHENTICATION_LDAP_USERNAME
  value: {{ .Values.security.authentication.ldap.username | quote }}
- name: SECURITY_AUTHENTICATION_LDAP_PASSWORD
  value: {{ .Values.security.authentication.ldap.password | quote }}
- name: SECURITY_AUTHENTICATION_LDAP_USER_ADMIN
  value: {{ .Values.security.authentication.ldap.user.admin | quote }}
- name: SECURITY_AUTHENTICATION_LDAP_USER_IDENTITY_ATTRIBUTE
  value: {{ .Values.security.authentication.ldap.user.identityattribute | quote }}
- name: SECURITY_AUTHENTICATION_LDAP_USER_EMAIL_ATTRIBUTE
  value: {{ .Values.security.authentication.ldap.user.emailattribute | quote }}
- name: SECURITY_AUTHENTICATION_LDAP_USER_NOT_EXIST_ACTION
  value: {{ .Values.security.authentication.ldap.user.notexistaction | quote }}
- name: SECURITY_AUTHENTICATION_LDAP_SSL_ENABLE
  value: {{ .Values.security.authentication.ldap.ssl.enable | quote }}
- name: SECURITY_AUTHENTICATION_LDAP_SSL_TRUST_STORE
  value: {{ .Values.security.authentication.ldap.ssl.truststore | quote }}
- name: SECURITY_AUTHENTICATION_LDAP_SSL_TRUST_STORE_PASSWORD
  value: {{ .Values.security.authentication.ldap.ssl.truststorepassword | quote }}
{{- end }}
{{- end -}}

{{/*
Wait for database to be ready.
*/}}
{{- define "dolphinscheduler.database.wait-for-ready" -}}
- name: wait-for-database
  image: {{ .Values.initImage.busybox }}
  imagePullPolicy: {{ .Values.initImage.pullPolicy }}
{{- if .Values.postgresql.enabled }}
  command: ['sh', '-xc', 'for i in $(seq 1 180); do nc -z -w3 {{ template "dolphinscheduler.postgresql.fullname" . }} 5432 && exit 0 || sleep 5; done; exit 1']
{{- else if .Values.mysql.enabled }}
  command: ['sh', '-xc', 'for i in $(seq 1 180); do nc -z -w3 {{ template "dolphinscheduler.mysql.fullname" . }} 3306 && exit 0 || sleep 5; done; exit 1']
{{- else }}
  command: ['sh', '-xc', 'for i in $(seq 1 180); do nc -z -w3 {{ .Values.externalDatabase.host }} {{ .Values.externalDatabase.port }} && exit 0 || sleep 5; done; exit 1']
{{- end }}
{{- end -}}

{{/*
Wait for minio to be ready.
*/}}
{{- define "dolphinscheduler.minio.wait-for-ready" -}}
{{- if .Values.minio.enabled }}
- name: wait-for-minio
  image: {{ .Values.initImage.busybox }}
  imagePullPolicy: {{ .Values.initImage.pullPolicy }}
  command: ['sh', '-xc', 'for i in $(seq 1 180); do nc -z -w3 {{ template "dolphinscheduler.minio.fullname" . }} 9000 && exit 0 || sleep 5; done; exit 1']
{{- end }}
{{- end -}}

{{/*
Create a registry environment variables.
*/}}
{{- define "dolphinscheduler.registry.env_vars" -}}
- name: REGISTRY_TYPE
  {{- if .Values.zookeeper.enabled }}
  value: "zookeeper"
  {{- else if .Values.registryEtcd.enabled }}
  value: "etcd"
  {{- else if .Values.registryJdbc.enabled }}
  value: "jdbc"
  {{- else }}
  value: {{ .Values.externalRegistry.registryPluginName }}
  {{- end }}
{{- if .Values.registryEtcd.enabled }}
- name: REGISTRY_ENDPOINTS
  value: {{ .Values.registryEtcd.endpoints }}
- name: REGISTRY_NAMESPACE
  value: {{ .Values.registryEtcd.namespace }}
- name: REGISTRY_USER
  value: {{ .Values.registryEtcd.user }}
- name: REGISTRY_PASSWORD
  value: {{ .Values.registryEtcd.passWord }}
- name: REGISTRY_AUTHORITY
  value: {{ .Values.registryEtcd.authority }}
- name: REGISTRY_CERT_FILE
  value: {{ .Values.registryEtcd.ssl.certFile }}
- name: REGISTRY_KEY_CERT_CHAIN_FILE
  value: {{ .Values.registryEtcd.ssl.keyCertChainFile }}
- name: REGISTRY_KEY_FILE
  value: {{ .Values.registryEtcd.ssl.keyFile }}
{{- else if .Values.registryJdbc.enabled }}
- name: REGISTRY_TERM_REFRESH_INTERVAL
  value: {{ .Values.registryJdbc.termRefreshInterval }}
- name: REGISTRY_TERM_EXPIRE_TIMES
  value: {{ .Values.registryJdbc.termExpireTimes | quote}}
{{- if .Values.registryJdbc.hikariConfig.enabled }}
- name: REGISTRY_HIKARI_CONFIG_DRIVER_CLASS_NAME
  value: {{ .Values.registryJdbc.hikariConfig.driverClassName }}
- name: REGISTRY_HIKARI_CONFIG_JDBC_URL
  value: {{ .Values.registryJdbc.hikariConfig.jdbcurl }}
- name: REGISTRY_HIKARI_CONFIG_USERNAME
  value: {{ .Values.registryJdbc.hikariConfig.username }}
- name: REGISTRY_HIKARI_CONFIG_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ include "dolphinscheduler.fullname" . }}-registry-db
      key: registry-password
{{- end }}
{{- else }}
- name: REGISTRY_ZOOKEEPER_CONNECT_STRING
  {{- if .Values.zookeeper.enabled }}
  value: {{ template "dolphinscheduler.zookeeper.quorum" . }}
  {{- else }}
  value: {{ .Values.externalRegistry.registryServers }}
  {{- end }}
{{- end }}
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

{{/*
Create a etcd ssl volume.
*/}}
{{- define "dolphinscheduler.etcd.ssl.volume" -}}
{{- if .Values.registryEtcd.ssl.enabled -}}
- name: etcd-ssl
  secret:
    secretName: {{ include "dolphinscheduler.fullname" . }}-etcd-ssl
{{- end -}}
{{- end -}}

{{/*
Create a etcd ssl volumeMount.
*/}}
{{- define "dolphinscheduler.etcd.ssl.volumeMount" -}}
{{- if .Values.registryEtcd.ssl.enabled -}}
- mountPath: /opt/dolphinscheduler/{{ .Values.registryEtcd.ssl.certFile }}
  name: etcd-ssl
  subPath: cert-file
- mountPath: /opt/dolphinscheduler/{{ .Values.registryEtcd.ssl.keyCertChainFile  }}
  name: etcd-ssl
  subPath: key-cert-chain-file
- mountPath: /opt/dolphinscheduler/{{ .Values.registryEtcd.ssl.keyFile }}
  name: etcd-ssl
  subPath: key-file
{{- end -}}
{{- end -}}

{{/*
Create a ldap ssl volume.
*/}}
{{- define "dolphinscheduler.ldap.ssl.volume" -}}
{{- if .Values.security.authentication.ldap.ssl.enable -}}
- name: jks-file
  secret:
    secretName: {{ include "dolphinscheduler.fullname" . }}-ldap-ssl
{{- end -}}
{{- end -}}

{{/*
Create a ldap ssl volumeMount.
*/}}
{{- define "dolphinscheduler.ldap.ssl.volumeMount" -}}
{{- if .Values.security.authentication.ldap.ssl.enable -}}
- mountPath: {{ .Values.security.authentication.ldap.ssl.truststore }}
  name: jks-file
  subPath: jks-file
{{- end -}}
{{- end -}}
