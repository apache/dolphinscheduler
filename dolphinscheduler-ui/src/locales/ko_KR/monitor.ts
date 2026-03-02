/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

export default {
  master: {
    cpu_usage: 'CPU 사용량',
    memory_usage: '메모리 사용량',
    disk_usage: '디스크 사용량',
    create_time: '생성 시간',
    last_heartbeat_time: '마지막 하트비트',
    directory_detail: '디렉터리 상세',
    host: '호스트',
    directory: '등록 디렉터리',
    master_no_data_result_title: 'Master 노드가 없습니다',
    master_no_data_result_desc:
      'Master 노드가 없습니다. 먼저 Master 노드를 생성한 후 이 페이지에 접근하세요.'
  },
  worker: {
    cpu_usage: 'CPU 사용량',
    memory_usage: '메모리 사용량',
    disk_usage: '디스크 사용량',
    thread_pool_usage: '스레드 풀 사용량',
    create_time: '생성 시간',
    last_heartbeat_time: '마지막 하트비트',
    directory_detail: '디렉터리 상세',
    host: '호스트',
    directory: '등록 디렉터리',
    worker_no_data_result_title: 'Worker 노드가 없습니다',
    worker_no_data_result_desc:
      'Worker 노드가 없습니다. 먼저 Worker 노드를 생성한 후 이 페이지에 접근하세요.'
  },
  alert_server: {
    alert_server_no_data_result_title: 'Alert Server 노드가 없습니다',
    alert_server_no_data_result_desc:
      'Alert Server 노드가 없습니다. 먼저 Alert Server 노드를 생성한 후 이 페이지에 접근하세요.'
  },
  db: {
    health_state: '헬스 상태',
    max_connections: '최대 연결 수',
    threads_connections: '현재 연결 수',
    threads_running_connections: 'DB 활성 연결 수',
    db_no_data_result_title: 'DB 노드가 없습니다',
    db_no_data_result_desc:
      'DB 노드가 없습니다. 먼저 DB 노드를 생성한 후 이 페이지에 접근하세요.'
  },
  statistics: {
    command_statistics_list: '명령 통계 목록',
    failure_command_statistics_list: '실패 명령 통계 목록',
    command_type: '명령 유형',
    command_param: '명령 파라미터',
    task_info: '태스크 정보',
    task_params: '태스크 파라미터',
    worker_info: 'Worker 정보',
    warning_info: '경고 정보',
    executor_id: '실행 사용자 ID',
    message: '오류 메시지',
    time: '시간'
  },
  audit_log: {
    user_name: '사용자명',
    operation_type: '작업 유형',
    model_type: '모델 유형',
    model_name: '모델명',
    latency: '소요 시간',
    description: '설명',
    create_time: '생성 시간',
    start_time: '시작 시간',
    end_time: '종료 시간',
    user_audit: '사용자 관리 감사',
    project_audit: '프로젝트 관리 감사',
    create: '생성',
    update: '수정',
    delete: '삭제',
    read: '읽기'
  }
}
