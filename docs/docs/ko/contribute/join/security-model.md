# Apache DolphinScheduler 보안 모델

이 문서는 주로 다양한 역할을 가진 사용자의 작업 범위, 책임 및 주요 기능을 설명하는 데 사용됩니다.사용자 권한과 기능을 소개하여 배포, 사용, 운영, 유지 관리 단계에서 주의사항과 규칙을 사용자가 이해할 수 있도록 도와줍니다.개발자는 이 문서를 사용하여 보안 취약점과 정상적인 기능 간의 경계를 이해할 수 있습니다.

## Apache DolphinScheduler 작업 흐름

Apache DolphinScheduler에 대한 이해부터 사용까지 사용자는 일반적으로 다음 단계를 거치게 됩니다.

1. 시스템 배포, 운영 환경 구성

2. 시스템 사용자 생성 및 해당 리소스 구성

3. 워크플로 정의 생성 및 작업 작업 구성

4. 시스템 운영 및 유지관리

사용자가 단일 노드를 사용하든, 의사 클러스터를 사용하든, 클러스터 배포(서버 또는 클라우드 배포)를 사용하든 시스템 사용은 위의 네 단계를 거치게 됩니다.위의 네 단계에는 일반적으로 다음 세 가지 유형의 사용자가 포함됩니다.

## 사용자 유형

### 1. 서비스 배포 인력

서비스 배포 담당자는 서버를 운영할 수 있는 권한이 필요합니다.서비스 배포 담당자는 서버 보안 경계 및 환경 요구 사항을 보장하기 위해 관련 작업이 실행되는 방식을 이해해야 합니다.
(1).다중 테넌트 작업 시나리오의 경우 서버 배포 사용자는 사용자를 만들고 전환할 수 있는 권한이 있어야 합니다.
(2).Apache DolphinScheduler는 사용자 정의 스크립트와 코드를 실행할 수 있습니다.사용자는 노드 구성을 통해 시스템에서 모든 명령이나 코드를 실행할 수 있습니다.서비스 배포 담당자는 서비스 시작 사용자의 권한을 확인하고, 권한을 통해 일부 민감한 파일을 보호하며, 배포 사용자의 작업 권한 경계를 명확히 해야 합니다.
(3).서버는 데이터 소스 연결 작업을 수행하고 사용자 정의 SQL 문을 실행합니다.플랫폼은 사용자가 실행하는 SQL 유형을 제한하지 않습니다.SQL 실행 권한은 데이터 소스 생성을 위한 사용자 권한과 관련이 있습니다.
(4).서버 구축 인력은 업무 운영에 필요한 자원과 업무에서 요구하는 작업자 그룹 내 모든 작업자 서버 간의 네트워크 및 상호작용 보안 요구사항을 보장해야 합니다.
(5).작업자 로컬 작업 유형(예: datax)의 경우 해당 서비스를 호출할 수 있는 권한이 필요합니다.
(6).Apache DolphinScheduler에서 제공하는 리소스 센터는 로컬 파일 시스템에 직접 연결할 수 있습니다.클러스터 배포 환경에서는 파일 액세스를 위해 공유 파일을 통해 다른 서버 파일을 API 서버에 마운트할 수 있습니다.여기서 서비스 배포 담당자는 탑재된 파일 디렉터리에 포함된 파일을 통해 시스템 사용자가 운영할 수 있고 운영 사용자의 작업 동작을 신뢰할 수 있는지 확인해야 합니다.
(7).Apache DolphinScheduler는 k8s 작업 유형을 지원합니다.k8s 클러스터는 운영 및 유지보수를 위해 제공됩니다.운영 및 유지 관리는 k8s 서비스의 보안을 보장하고 포드 탈출과 같은 보안 문제를 방지해야 합니다.

### 2. 시스템 관리자시스템 관리자는 Apache DolphinScheduler의 모든 작업 권한을 갖습니다.실제 사용 시에는 관리자 사용자의 사용 범위가 보장되어야 하며, 이 기능을 남용하지 않도록 관리자 사용자에 대한 신뢰도가 높아야 합니다.
(1).관리자 사용자는 대기열 관리, 테넌트 관리, 사용자 관리, 알람 그룹 관리, 작업자 그룹 관리, 토큰 관리 및 기타 기능을 조작할 수 있습니다.관리자 사용자는 리소스에 연결하는 데 필요한 중요한 자격 증명과 같은 중요한 정보를 포함하여 모든 구성을 조작할 수 있습니다.관리자 사용자를 사용하는 사람이 해당 자원을 운용할 수 있는지 확인이 필요합니다.동시에 관리자 사용자는 사용자 관리 모듈에서 리소스, 데이터 소스, 프로젝트 등에 대한 작업을 승인할 수 있습니다.관리자 사용자는 해당 리소스에 대한 모든 사용 권한을 갖도록 사용자에게 명확하게 권한을 부여해야 합니다.
(2).시스템 관리자는 일반 사용자가 갖는 모든 운영 권한을 갖습니다.

### 3. 일반 시스템 사용자

Apache DolphinScheduler의 일반 사용자는 실제 워크플로우 개발 및 운영 사용자로 정의됩니다.물론 워크플로 개발 프로세스 중에 필요한 일부 리소스도 유지해야 합니다.이러한 사용자는 이 기능을 남용하지 않도록 높은 신뢰를 받아야 합니다.

(1).사용자는 워크플로와 작업을 생성할 수 있습니다.지원되는 작업 유형 목록은 [작업 목록]을 참조하세요.작업은 작업자에서 실행됩니다.사용자는 지정된 작업자 그룹에서 실행할 명령과 코드를 사용자 정의할 수 있습니다.여기에 있는 모든 명령과 코드에 주의하세요.사용자는 쉘, SQL을 포함하여 Apache DolphinScheduler가 지원하는 작업 유형에서 모든 작업을 실행할 수 있으며 쉘 스크립트를 실행하기 위해 다른 서버로 이동할 수 있습니다.동시에 작업 실행 프로세스 중에 로그가 생성됩니다.사용자는 UI 페이지를 통해 작업 실행 로그를 보고 다운로드할 수 있습니다.

(2).사용자는 데이터 소스 연결을 생성하고, 해당 구성을 포함하여 승인된 연결을 수정 및 삭제할 수 있으며, 특히 승인된 연결의 민감한 자격 증명에 대한 작업을 수행할 수 있습니다.이러한 작업은 리소스 자체나 시스템에 특정 영향을 미칠 수 있습니다.데이터 소스에는 다양한 유형이 포함됩니다.자세한 내용은 공식 홈페이지의 [데이터 소스 목록]을 참조하세요.

### 4. 로그인하지 않은 사용자

Apache DolphinScheduler는 로그인하지 않은 사용자가 시스템에 액세스하는 것을 허용하지 않습니다.아래 언급된 사용자에는 이러한 유형의 사용자가 포함되지 않습니다.

핵심 워크플로 개발 및 운영 외에도 플랫폼을 정상적으로 사용하려면 해당 환경과 리소스에 대한 구성 및 관리도 필요합니다.

## 데이터 소스 관리

데이터소스 관리는 모든 사용자가 할 수 있으며, 관리자 사용자는 일반 사용자 인증 후 작업이 가능합니다.데이터 소스 작업에 대한 해당 권한은 데이터 소스 연결을 통해 제공되며 연결 구성은 작업 실행 권한을 제어해야 합니다.데이터 소스 구성에서 사용자는 연결 매개변수를 사용자 정의할 수 있으며 해당 매개변수는 데이터 소스를 사용하는 모든 작업에 적용됩니다.

## 리소스 센터

리소스 센터는 로컬, 분산 파일 스토리지, 클라우드 개체 스토리지 및 기타 방법을 구성할 수 있습니다.관련 파일을 생성하거나 업로드하기 위해 리소스 센터를 사용해야 하는 경우 모든 파일과 리소스는 분산 파일 시스템 HDFS 또는 원격 개체 저장소에 저장됩니다.동시에 사용자는 승인된 파일의 내용을 수정할 수 있습니다.이 과정에서 사용자가 파일을 손상시키지 않으며 다른 보안 위험을 초래하지 않을 것이라는 신뢰가 필요합니다.

## 알람 관리지원되는 알람 방식 목록은 공식 홈페이지 [알람]에서 확인하실 수 있습니다.모든 사용자는 승인된 알람 채널을 각자의 프로세스에 구성할 수 있습니다.사용자는 민감한 자격 증명이 포함된 경보 구성을 수정할 수 있습니다.알람 구성은 워크플로 시간 초과 및 결과와 같은 규칙의 알람에 적용됩니다.사용자 알람 구성 및 알람 정보 전송이 알람 채널 및 알람 수신자에게 영향을 미치지 않을 것이라는 신뢰가 필요합니다.

## 인증방법

Apache DolphinScheduler는 자신의 계정과 비밀번호로 로그인, LDAP, Casdoor를 통한 SSO 로그인, Oauth2 인증을 통한 로그인의 4가지 인증 방식을 지원하며, oauth2 인증 로그인 방식은 다른 인증 방식과 병행하여 사용할 수 있습니다.어떤 방식으로든 로그인하는 사용자는 해당 권한과 기능을 남용하지 않을 것이라는 높은 신뢰가 필요합니다.

## 보안 센터

관리자 사용자는 대기열, 테넌트, 사용자, 경보 그룹, 작업자 그룹, 토큰, k8s 클러스터, k8s 네임스페이스 등과 같은 리소스를 구성할 수 있습니다. 사용자의 리소스 권한 할당, 사용 및 유지 관리가 플랫폼 및 서비스 자체에 영향을 미치지 않는다는 신뢰가 필요합니다.

## 네트워크 환경

Apache DolphinScheduler의 배포 및 사용은 사용자의 네트워크가 안전하고 신뢰할 수 있다는 가정을 기반으로 합니다.Apache DolphinScheduler는 내부 네트워크 교차 침입 문제를 해결하지 않습니다.

### 잘못된 보안 취약점의 예

다음은 과거에 사용자와 개발자가 제기한 몇 가지 잘못된 취약점이다.1. Using the insecure settings of plug-ins to attack or perform other operations
   When a user uses a plug-in, some parameters are set to insecure configurations, and then the system is attacked through the configuration. This problem does not belong to a security vulnerability. This type of plug-in includes but is not limited to data sources, tasks, etc. The user's setting of parameters is an active behavior, and the authorization has trusted the user's parameter configuration operations. When setting the corresponding parameters or a certain configuration, the user believes that the configuration user has fully understood the configured functions and the risks brought about, so this type of problem does not belong to a vulnerability. For example, when using MySQL driver to connect to Doris, {"aaa":"dsf&allowLoadLocalInfile=true#"} is added to the JDBC connection parameters. This configuration may send local sensitive files to the server. In this process, the user adds configurations as needed, and all operations of the user are trusted.
2. Use the security configuration during deployment to access the system for attack or other operations
   When deploying the system, the deployment user should follow the operation of the official website to modify the sensitive configuration. The configuration belongs to the service sensitive information, and its importance and security level are equivalent to the service database connection and other information. When other users obtain sensitive configurations through any means, the platform considers the user to be a normal authorized user and fully trusts all operations of the authorized user. For example, the user obtains auth-token data from the configuration file, authenticates and creates a user through the configuration, and uses the created user to operate the system. In this process, since the user obtains the authentication information of the platform, all operations of the user are considered to be trusted.
3. Intermediate files generated by the execution platform during task execution
   In the process of running tasks in Apache DolphinScheduler, some intermediate files are generated. This file mainly encapsulates the environment and parameters required for the task to run. The file is related to the task and is stored in the same node as the running task. Running these files is no different from running the same task in the same node by other users. During the deployment and permission allocation process, the corresponding worker or other resources are allocated to the corresponding user. This operation means that the user is fully trusted for all operations on the service node, including task running and reading and modifying the resources with permissions in the server, and of course, the files generated by the platform. Therefore, this type of problem does not belong to a vulnerability. For example, the remote shell task will produce an intermediate file in the server. The user knows the file information and operates the file through the shell node. In this process, the user has the permission of the node, and all operations of the user on the node are trusted.
4. Authorized users enter scripts through the page input box to attack or other operations
   There are multiple input boxes in Apache DolphinScheduler, allowing users to customize configurations as needed. As an open source task scheduling system, Apache DolphinScheduler requires administrators to fully trust all authorized operations of the target user in the process of deployment, authorization, and other security-related processes. If the user's behavior of adding and modifying configurations through pages or calling interfaces is within the scope of permissions, then the behavior of attacking or other operations in this way does not belong to security vulnerabilities.
5. Attack or other operations by modifying the image or providing an unsafe image to run
   Apache DolphinScheduler itself and task operations both support k8s clusters. Before the service or task runs, the user needs to ensure the image's functions and configured parameters, and trust all operations during the service and task running process. Therefore, modifying tasks or parameters by any means before the image runs to attack or complete other operations does not constitute a security vulnerability.
6. Attacks by obtaining certain sensitive information printed in service logs
   Apache DolphinScheduler prints some sensitive information in its service logs, which can be used by service deployers to view detailed information about the program's operation. Service deployers are considered trusted users, and we do not believe that service deployers will attack the program, so this type of issue is not a vulnerability.
7. Security problems caused by system administrators accessing untrusted third-party websites
   System administrators using Apache DolphinScheduler may access untrusted third-party websites, resulting in system attacks; such issues are not considered security vulnerabilities. System administrators are considered to be trusted users, and we believe that system administrators have a basic awareness of security precautions. Problems caused by weak security precautions on the part of system administrators are not considered vulnerabilities.