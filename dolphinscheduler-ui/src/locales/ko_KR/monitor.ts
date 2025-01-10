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
    disk_available: '사용 가능한 디스크',
    load_average: '부하 평균',
    create_time: '생성 시간',
    last_heartbeat_time: '마지막 하트비트 시간',
    directory_detail: '디렉토리 세부 정보',
    host: '호스트',
    directory: '디렉토리',
    master_no_data_result_title: '마스터 노드가 존재하지 않음',
    master_no_data_result_desc:
        '현재 마스터 노드가 존재하지 않습니다. 마스터 노드를 생성하고 이 페이지를 새로 고침하세요.'
  },
  worker: {
    cpu_usage: 'CPU 사용량',
    memory_usage: '메모리 사용량',
    disk_available: '사용 가능한 디스크',
    load_average: '부하 평균',
    thread_pool_usage: '스레드 풀 사용량',
    create_time: '생성 시간',
    last_heartbeat_time: '마지막 하트비트 시간',
    directory_detail: '디렉토리 세부 정보',
    host: '호스트',
    directory: '디렉토리',
    worker_no_data_result_title: '워커 노드가 존재하지 않음',
    worker_no_data_result_desc:
        '현재 워커 노드가 존재하지 않습니다. 워커 노드를 생성하고 이 페이지를 새로 고침하세요.'
  },
  alert_server: {
    alert_server_no_data_result_title: '알림 서버 노드가 존재하지 않음',
    alert_server_no_data_result_desc:
        '현재 알림 서버 노드가 존재하지 않습니다. 알림 서버 노드를 생성하고 이 페이지를 새로 고침하세요.'
  },
  db: {
    health_state: '건강 상태',
    max_connections: '최대 연결 수',
    threads_connections: '스레드 연결 수',
    threads_running_connections: '실행 중인 스레드 연결 수',
    db_no_data_result_title: 'DB 노드가 존재하지 않음',
    db_no_data_result_desc:
        '현재 DB 노드가 존재하지 않습니다. DB 노드를 생성하고 이 페이지를 새로 고침하세요.'
  },
  statistics: {
    command_statistics_list: '명령 통계 목록',
    failure_command_statistics_list: '실패한 명령 통계 목록',
    command_type: '명령 유형',
    command_param: '명령 파라미터',
    task_info: '작업 정보',
    task_params: '작업 파라미터',
    worker_info: '워커 정보',
    warning_info: '경고 정보',
    executor_id: '실행자 ID',
    message: '메시지',
    time: '시간'
  },
  audit_log: {
    user_name: '사용자 이름',
    operation_type: '작업 유형',
    model_type: '모델 유형',
    model_name: '모델 이름',
    latency: '지연 시간',
    description: '설명',
    create_time: '생성 시간',
    start_time: '시작 시간',
    end_time: '끝 시간',
    user_audit: '사용자 감사',
    project_audit: '프로젝트 감사',
    create: '생성',
    update: '업데이트',
    delete: '삭제',
    read: '읽기'
  }
}
