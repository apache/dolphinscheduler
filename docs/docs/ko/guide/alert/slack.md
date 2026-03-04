# 슬랙

알림을 위해 'Slack'을 사용해야 하는 경우 알림 인스턴스 관리에서 알림 인스턴스를 생성하고 선택하세요.
'Slack' 플러그인.

다음은 `Slack` 구성 예를 보여줍니다.

![경고-슬랙](../../../../img/new_ui/dev/alert/alert_slack.png)

## 매개변수 구성

* 웹훅

> 앱에서 `Incoming Webhooks` 주소를 복사하고 아래 이미지를 확인하세요.

![alert-slack-webhook](../../../../img/new_ui/dev/alert/alert_slack_webhook_addr.png)

* 사용자 이름

> (더 이상 사용되지 않음) 보낸 사람 이름입니다.현재 Slack은 Slack 업데이트로 인해 발신자 이름으로 APP를 사용하고 있습니다.

## 웹훅을 얻는 방법

새로운 웹훅을 생성하려면 공식 문서 [Slack: Incoming Webhooks를 사용하여 메시지 보내기](https://api.slack.com/messaging/webhooks)를 참조하세요.

### 새 Slack 앱 만들기

[Slack 공식 웹사이트](https://api.slack.com/apps/new)를 방문하여 새로운 앱을 만드세요.

### 들어오는 Webhooks 설정 활성화

새 APP 생성이 완료되면 APP 페이지 왼쪽 'Feature' 열의 'Incoming Webhooks'를 선택하고 'Activate Incoming Webhooks'를 'ON'으로 전환하세요.
![alert-slack-webhook](../../../../img/new_ui/dev/alert/alert_slack_enable.png)

### 새로운 수신 웹훅 생성

'작업공간에 새 웹훅 추가'를 클릭하고 메시지를 게시할 그룹을 선택하세요.
![선택-포스트 그룹](../../../../img/new_ui/dev/alert/alert_slack_new.png)

### 들어오는 Webhooks 주소 획득

'Incoming Webhooks' 주소를 DolphinScheduler에 복사하고 아래 이미지를 확인하세요.
`수신 웹후크`의 기본 형식: `https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXXXXXX`

![alert-slack-webhook](../../../../img/new_ui/dev/alert/alert_slack_webhook_addr.png)

참조:[Slack:수신 Webhooks를 사용하여 메시지 보내기](https://api.slack.com/messaging/webhooks)