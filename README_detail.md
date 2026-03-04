# Tracking Light Robot (상세 문서)
**야간 작업자를 위한 객체 추적 스마트 조명 시스템**

> 📄 [README](README.md)

## 수상
**2025 캡스톤디자인 장려상**

---

## 프로젝트 배경

### 문제 인식

**야간 작업 환경의 조명 문제**

1. **안전성 문제**
   - 조명 조작 중 작업 중단 → 사고 위험 증가
   - 고정 조명으로 인한 사각지대 발생
   - 수동 조작의 번거로움

2. **작업 효율성 저하**
   - 작업 위치 이동 시 조명 재조정 필요
   - 양손 사용 작업 중 조명 제어 불가
   - 조명 밝기 수동 조절의 비효율

3. **기존 솔루션의 한계**
   - 헤드랜턴: 조명 범위 협소, 장시간 착용 불편
   - 고정 조명: 이동성 부족
   - 기존 스마트 조명: 추적 기능 없음

### 해결 방안

**AI 기반 자동 추적 조명 로봇**

- 객체 인식 → 사용자 자동 추적
- 음성 제어 → 핸즈프리 작업
- 원격 제어 → 앱/웹 통합 관리
- 자동 밝기 조절 → 환경 적응

---

## 프로젝트 개요

| 구분 | 내용 |
|------|------|
| **팀명** | 불켜조 (5인 팀) |
| **제작 기간** | 2025년 3월 ~ 6월 (3개월) |
| **프로젝트 유형** | IoT 기반 스마트 조명 로봇 |
| **수상** | 2025 캡스톤디자인 장려상 |

---

## 시스템 동작 흐름

```
1. 모드 선택 (앱/웹/음성)
   ├─ TRACK: 자동 추적
   ├─ REMOTE: 원격 제어
   ├─ MANUAL: 수동 제어
   └─ AUTO: 자동 조명

2. 명령 처리
   - 앱/웹 → Spring Boot API
   - 음성 → 라즈베리파이 → API
   - 센서 → 자동 처리

3. 실행
   ├─ 모터 제어 (이동)
   ├─ 조명 제어 (RGB)
   └─ 데이터 저장 (로그)

4. 피드백
   - 실시간 영상 스트리밍
   - 센서 데이터 전송
   - 상태 업데이트
```

---

## 주요 기능

### 1. 객체 추적 시스템 (TRACK 모드)

**AI 기반 실시간 추적**

- **MobileNet SSD v2** (TensorFlow Lite)
  - 사람 객체 인식 (COCO dataset)
  - 실시간 처리 (평균 15 FPS)
  - 정확도 > 85%

- **추적 알고리즘**
  - 중심점 기반 추적
  - 거리 계산 (초음파 센서)
  - 방향 결정 (좌/우 편차)

- **자동 제어**
  - 전진/후진: 거리 기반
  - 좌회전/우회전: 중심 편차 기반
  - 정지: 목표 도달 시

**코드 예시:**
```python
def track_object(objs, labels):
    if len(objs) == 0:
        ut.stop()
        return
    
    for obj in objs:
        if labels.get(obj.id) == 'person':
            x_min, y_min, x_max, y_max = obj.bbox
            
            # 중심점 계산
            obj_x_center = x_min + (x_max - x_min) / 2
            x_deviation = 0.5 - obj_x_center
            
            # 방향 결정
            if abs(x_deviation) < tolerance:
                ut.forward(speed=50)
            elif x_deviation >= tolerance:
                ut.left(speed=50)
            else:
                ut.right(speed=50)
```

---

### 2. 원격 제어 시스템

**Multi-Platform 지원**

- **Android App** (팀원 개발)
  - 조명/모터 제어
  - 일정 관리

- **Web Dashboard** (Thymeleaf)
  - 관리자 패널
  - 통계 대시보드
  - 사용자 관리

- **API 통신**
  - RESTful API
  - HTTPS (SSL/TLS)
  - JSON 데이터 교환

---

### 3. 음성 인식 시스템

**한국어 음성 명령**

- **Google Speech Recognition API**
  - 한국어 인식 (ko-KR)
  - 실시간 처리
  - 정확도 > 90%

- **지원 명령어**
  ```
  조명:
  - "켜기" → LED ON (255,255,255)
  - "끄기" → LED OFF (0,0,0)
  
  모터:
  - "전진" → FORWARD
  - "정지" → STOP
  - "왼쪽" → LEFT
  - "오른쪽" → RIGHT
  ```

- **동작 방식**
  ```python
  def listen():
      r = sr.Recognizer()
      with sr.Microphone() as source:
          audio = r.listen(source)
      text = r.recognize_google(audio, language='ko-KR')
      return text.replace(" ", "")
  
  def match_command(text):
      action = command_map.get(text)
      run_action(action)
  ```

---

### 4. 자동 조명 제어

**환경 적응형 조명**

- **조도센서 (CdS Cell)**
  - 실시간 밝기 감지
  - 임계값 기반 제어
  - 자동 ON/OFF

- **RGB LED 제어**
  - 색상 조절 (0-255)
  - 밝기 조절 (PWM)
  - 일정 예약

- **일정 관리**
  ```kotlin
  // Spring Boot API
  @PostMapping("/led/schedule")
  fun createSchedule(
      @RequestBody schedule: LedSchedule
  ): ResponseEntity<Any> {
      // 일정 저장
      scheduleRepository.save(schedule)
  }
  
  // Python 스케줄러
  def led_schedule_thread():
      schedules = get_schedules()
      for s in schedules:
          if now >= s.scheduled_time:
              send_light_command(s.r, s.g, s.b)
  ```

---

## 시스템 구성

### 하드웨어

| 구분 | 부품명 | 역할 |
|------|--------|------|
| **메인보드** | Raspberry Pi 4 (4GB) | 객체 인식, 제어 |
| **MCU** | Arduino Uno | LED 제어 |
| **카메라** | 카메라 모듈 (OV2640) | 영상 촬영 |
| **마이크** | 음성인식 모듈 | 음성 입력 |
| **조명** | RGB LED 모듈 | 조명 |
| **센서** | CdS 조도센서 | 밝기 감지 |
| **센서** | 초음파 센서 (HC-SR04) | 거리 측정 |
| **센서** | 적외선 센서 | 장애물 감지 |
| **모터** | DC 모터 (4개) | 4륜 구동 |
| **드라이버** | L298N 모터 드라이버 (2개) | 모터 제어 |
| **입력** | 가변저항 (다이얼) | 수동 제어 |
| **전원** | 배터리 | 독립 동작 |
| **케이스** | 아크릴 + 목재 | 외관 |

### 소프트웨어 아키텍처

```
┌─────────────────────────────────┐
│  Frontend                        │
│  - Android App (Kotlin)         │
│  - Web Dashboard (Thymeleaf)    │
└────────┬────────────────────────┘
         │ HTTPS
         ↓
┌─────────────────────────────────┐
│  Backend API Server              │
│  ┌─────────────────────────────┐│
│  │ Spring Boot 3.2.5 (Kotlin)  ││
│  │ - AuthController            ││
│  │ - MobileApiController       ││
│  │ - RoboticsApiController     ││
│  │ - DashboardController       ││
│  └─────────────────────────────┘│
│  ┌─────────────────────────────┐│
│  │ Spring Security             ││
│  │ - JWT Authentication        ││
│  │ - CORS Configuration        ││
│  └─────────────────────────────┘│
│  ┌─────────────────────────────┐│
│  │ Spring Data JPA             ││
│  │ - ProductRepository         ││
│  │ - MotorDataRepository       ││
│  │ - UserRepository            ││
│  └─────────────────────────────┘│
└────────┬────────────────────────┘
         │
         ↓
┌─────────────────────────────────┐
│  Database                        │
│  - MariaDB (Ubuntu VM)          │
└─────────────────────────────────┘

         ↕ HTTP API
         
┌─────────────────────────────────┐
│  Raspberry Pi 4 (Python)        │
│  ┌─────────────────────────────┐│
│  │ main_controller.py          ││
│  │ - 전체 시스템 조율          ││
│  └─────────────────────────────┘│
│  ┌─────────────────────────────┐│
│  │ human_follower.py           ││
│  │ - MobileNet SSD v2          ││
│  │ - OpenCV 영상처리           ││
│  │ - 객체 추적 알고리즘        ││
│  └─────────────────────────────┘│
│  ┌─────────────────────────────┐│
│  │ main_motor_controller.py    ││
│  │ - GPIO 제어                 ││
│  │ - PWM 모터 제어             ││
│  │ - 센서 데이터 처리          ││
│  └─────────────────────────────┘│
│  ┌─────────────────────────────┐│
│  │ voice_command_runner.py     ││
│  │ - Google Speech API         ││
│  │ - 음성 명령 처리            ││
│  └─────────────────────────────┘│
│  ┌─────────────────────────────┐│
│  │ main_light_controller.py    ││
│  │ - Serial 통신               ││
│  │ - LED 제어 명령 전송        ││
│  └─────────────────────────────┘│
└────────┬────────────────────────┘
         │ Serial (USB)
         ↓
┌─────────────────────────────────┐
│  Arduino Uno (C/C++)            │
│  - RGB LED PWM 제어             │
│  - 조도센서 ADC 읽기             │
│  - Serial 통신                  │
└─────────────────────────────────┘
```

---

## 팀원 역할

### 김동진 - 소프트웨어 총괄 & 서기

**전체 소프트웨어 설계 및 구현**

#### 1. Spring Boot API 서버 (Kotlin)
- **아키텍처 설계**
  - 3-Tier 구조 (Controller-Service-Repository)
  - RESTful API 설계
  - DTO 패턴 적용

- **구현 내용** (44개 파일)
  - AuthController: 사용자 인증/인가
  - MobileApiController: 앱 API
  - RoboticsApiController: 라즈베리파이 API
  - DashboardController: 웹 대시보드

- **데이터베이스**
  - MariaDB JPA 연동
  - 10개 테이블 설계
  - 관계 매핑 (OneToMany, ManyToOne)

- **보안**
  - Spring Security 설정
  - CORS 설정
  - CSRF 보호

#### 2. Raspberry Pi 제어 (Python)
- **객체 추적 시스템**
  - TensorFlow Lite 모델 로딩
  - OpenCV 영상 처리
  - 추적 알고리즘 구현
  - 실시간 프레임 처리 최적화

- **모터 제어**
  - GPIO 핀 매핑
  - PWM 속도 제어
  - 4륜 구동 로직
  - 센서 기반 장애물 회피

- **음성 인식**
  - Google Speech API 연동
  - 한국어 음성 인식
  - 명령어 매핑
  - API 연동

- **통신**
  - Spring Boot API 통신
  - Serial 통신 (Arduino)
  - 실시간 데이터 전송

#### 3. Arduino (C/C++)
- RGB LED PWM 제어
- 조도센서 ADC 읽기
- Serial 통신 프로토콜

#### 4. 인프라 구축
- **서버 환경**
  - Ubuntu VM 구축
  - MariaDB 설치 및 설정
  - Nginx 리버스 프록시 설정

- **네트워크**
  - DuckDNS 동적 DNS 설정
  - 공유기 포트포워딩
  - Certbot SSL 인증서 발급
  - HTTPS 설정

#### 5. 문서화
- 회의록 작성
- 기술 문서 정리
- API 문서화

### 하드웨어팀 (4명)
- 장OO, 이OO, 이OO
- 3D 모델링 (케이스 설계)
- 하드웨어 조립
- 배선 및 테스트

---

## 기술 스택

### Backend
- **Spring Boot 3.2.5** + Kotlin 1.9.23
- **MariaDB** (Spring Data JPA)
- **Spring Security** (인증/인가)
- **Thymeleaf** (웹 템플릿)
- **Gradle** (빌드 도구)

### Embedded System
- **Raspberry Pi 4** (4GB RAM)
  - Python 3.11
  - OpenCV 4.x
  - TensorFlow Lite
  - RPi.GPIO (하드웨어 제어)

- **Arduino Uno**
  - C/C++
  - Arduino IDE

### AI/ML
- **TensorFlow Lite**
  - MobileNet SSD v2
  - COCO dataset (사람 인식)
  - Edge TPU 지원 가능

- **OpenCV**
  - 영상 처리
  - 프레임 인코딩/디코딩
  - 실시간 스트리밍

### Infrastructure
- **Ubuntu 20.04** (VM)
- **Nginx** (Reverse Proxy)
- **DuckDNS** (Dynamic DNS)
- **Certbot** (Let's Encrypt SSL)

### Communication
- **RESTful API** (JSON)
- **HTTPS** (SSL/TLS)
- **Serial** (USB)
- **GPIO** (하드웨어)

---

## 기술적 도전 & 해결

### 1. 실시간 객체 추적 성능 최적화

**문제:**
```
초기 FPS: 5-7 FPS
→ 실시간 추적 불가능
→ 사용자 경험 저하
```

**해결:**
```python
# 1. 프레임 해상도 최적화
picam2.configure(
    picam2.create_preview_configuration(
        main={"size": (640, 480)}  # 320x240 → 640x480
    )
)

# 2. 모델 경량화
# MobileNet SSD v2 (Quantized)
# Edge TPU 버전 준비 (필요 시)

# 3. 비동기 처리
thread = Thread(target=move_robot)
thread.start()  # 모터 제어 분리
```

**결과:**
- FPS 향상: 5 → 25 FPS
- 응답성 개선
- 안정적인 추적

---

### 2. API 서버와 라즈베리파이 통신 안정성

**문제:**
```
네트워크 불안정
→ API 요청 실패
→ 제어 명령 유실
```

**해결:**
```python
# 1. 타임아웃 설정
res = requests.get(
    f"{API_BASE}/motor-mode",
    params={"productId": PRODUCT_ID},
    timeout=3  # 3초 타임아웃
)

# 2. 예외 처리
try:
    data = res.json()
    return data.get("mode", "STOP")
except Exception as e:
    print(f"[ERROR] {e}")
    return "STOP"  # 기본값 반환

# 3. 재시도 로직
for i in range(3):
    try:
        res = requests.post(...)
        break
    except:
        time.sleep(1)
```

**결과:**
- 안정성 향상
- 명령 유실 방지
- 에러 핸들링 개선

---

### 3. 음성 인식 정확도 향상

**문제:**
```
주변 소음 간섭
→ 인식 실패 증가
→ 명령 오인식
```

**해결:**
```python
# 1. 노이즈 필터링
r = sr.Recognizer()
with sr.Microphone() as source:
    r.adjust_for_ambient_noise(source)  # 주변 소음 제거
    audio = r.listen(source)

# 2. 명령어 정규화
def normalize(text):
    return re.sub(r"[ \-_\s]", "", text)  # 공백 제거

# 3. 명령어 매핑 DB화
command_map = load_from_api()  # 서버에서 로딩
```

**결과:**
- 오인식 감소

---

### 4. 모터 제어 정밀도

**문제:**
```
좌우 모터 속도 차이
→ 직진 불가
→ 추적 오차 누적
```

**해결:**
```python
# 1. PWM Duty Cycle 보정
def set_speed(left_speed, right_speed):
    # 100을 넘지 않도록 제한
    left_duty = min(max(left_speed / 10, 0), 100)
    right_duty = min(max(right_speed / 10, 0), 100)
    
    pwm_ena1.ChangeDutyCycle(right_duty)
    pwm_enb1.ChangeDutyCycle(left_duty)

# 2. 속도 테스트 및 캘리브레이션
# 좌우 속도 차이 측정 → 보정값 적용

# 3. 방향 제어 개선
def get_delay(deviation):
    deviation = abs(deviation)
    if deviation >= 0.4:
        return 0.080
    elif deviation >= 0.35:
        return 0.060
    # ...
```

**결과:**
- 직진 정확도 향상
- 추적 정밀도 개선

---

## 개발 과정

### 타임라인

| 주차 | 소프트웨어팀 | 하드웨어팀 |
|------|-------------|-----------|
| 1-2 | 기획 및 추진계획서 작성 | - |
| 3-4 | 기술 스택 선정, DB 설계 | 부품 선정 및 주문 |
| 5-6 | Spring Boot API 개발 시작 | 케이스 설계 (onShape) |
| 7-8 | 객체 추적 알고리즘 구현 | 시제품 제작 |
| 9-10 | 음성 인식 연동 | 하드웨어 조립 |
| 11-12 | 인프라 구축 (Nginx, SSL) | 배선 및 테스트 |
| 13-14 | 통합 테스트 및 버그 수정 | 최종 조립 |
| 15 | 최종 발표 및 시연 | 완성품 전시 |

---

## 인프라 구성

### 데이터베이스 (MariaDB)

**테이블 구조:**

```sql
-- 제품 정보
products
├── id (PK)
├── product_number
├── name
└── user_id (FK)

-- 조명 제어
light_control
├── id (PK)
├── product_id (FK)
├── led_color_r
├── led_color_g
└── led_color_b

-- 모터 제어
motor_control
├── id (PK)
├── product_id (FK)
├── command_type
└── executed

-- 모터 데이터 (로그)
motor_data
├── id (PK)
├── product_id (FK)
├── mode
├── direction
├── left_speed
├── right_speed
├── ultrasonic_cm
└── ir_detected

-- 음성 명령
voice_log
├── id (PK)
├── product_id (FK)
├── input_text
└── action_name
```

### 네트워크 구성

**아키텍처:**

```
인터넷
  ↓
공유기 (공인 IP)
  ↓ Port Forwarding
  ├─ 80 → 443 (리다이렉션)
  └─ 443 → Ubuntu VM
         ↓
      Nginx (Reverse Proxy)
         ↓ Port 8080
    Spring Boot Server
         ↓ Port 3306
       MariaDB
```

**설정 상세:**

1. **DuckDNS**
   - 도메인: lightproject.duckdns.org
   - 동적 IP → 도메인 자동 갱신
   - 사용자가 고정 URL로 접속 가능

2. **포트포워딩**
   ```
   외부 Port 80  → 내부 Port 443 (Nginx)
   외부 Port 443 → 내부 Port 443 (Nginx)
   ```

3. **Nginx 설정**
   ```nginx
   server {
       listen 80;
       server_name lightproject.duckdns.org;
       return 301 https://$server_name$request_uri;
   }
   
   server {
       listen 443 ssl;
       server_name lightproject.duckdns.org;
       
       ssl_certificate /etc/letsencrypt/live/lightproject.duckdns.org/fullchain.pem;
       ssl_certificate_key /etc/letsencrypt/live/lightproject.duckdns.org/privkey.pem;
       
       location / {
           proxy_pass http://localhost:8080;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
       }
   }
   ```

4. **SSL 인증서**
   ```bash
   # Certbot 설치
   sudo apt install certbot python3-certbot-nginx
   
   # SSL 인증서 발급
   sudo certbot --nginx -d lightproject.duckdns.org
   
   # 자동 갱신 테스트
   sudo certbot renew --dry-run
   ```

---

## 외관 디자인

### 재료 선택

**아크릴**

- **아크릴**
  - 가벼운 재질 (무게 감소)
  - 투명도 (내부 확인 가능)
  - 가공 용이
  - 내구성 우수

### 설계 고려사항

1. **정확한 수치 측정**
   - 라즈베리파이 크기
   - 모터 배치
   - 배터리 위치
   - 배선 공간

2. **하중 분산**
   - 3층 구조
   - 1층: 배터리, 모터
   - 2층: 라즈베리파이, 아두이노
   - 3층: RGB LED, 센서

3. **유지보수성**
   - 분리 가능한 구조
   - 배선 정리
   - 부품 접근성

---

## 한계점 & 개선 방향

### 현재 한계

| 문제점 | 영향 |
|--------|------|
| **배터리 용량** | 작동 시간 제한 (약 2시간) |
| **추적 정확도** | 복잡한 환경에서 오차 발생 |
| **네트워크 의존성** | 인터넷 필수 |
| **단일 객체 추적** | 다중 사용자 불가 |
| **실내 전용** | 실외 사용 어려움 |

### 향후 개선 방향

#### Phase 1: 성능 개선
```
배터리 용량 증대 (4시간 이상)
Edge TPU 적용 (FPS 향상)
로컬 모드 추가 (오프라인 동작)
```

#### Phase 2: 기능 확장
```
다중 객체 추적
제스처 인식 추가
충돌 회피 알고리즘 고도화
실외 적용 (방수, 내구성 강화)
```

#### Phase 3: 산업 적용
```
건설 현장 적용
창고/물류 센터 적용
야간 경비 시스템 연동
다중 로봇 관리 시스템
```

---

## 기대 효과

### 사회적 기여
- **작업 안전성 향상**
  - 야간 작업 사고 감소
  - 핸즈프리 작업 환경

### 기술적 의의
- **AI 기반 IoT 시스템**
  - 실시간 객체 추적
  - 클라우드 연동

### 경제적 효과
- **작업 효율성 증대**
  - 조명 자동 제어
  - 작업 중단 최소화

### 확장 가능성
- **산업 현장 적용**
  - 건설, 물류, 경비
  - 스마트 팩토리

---

## 성과

- **2025 캡스톤디자인 장려상** 수상
- 전체 소프트웨어 아키텍처 설계 및 구현
- AI 기반 실시간 객체 추적 시스템 구현
- 클라우드 기반 IoT 플랫폼 구축
- HTTPS 보안 통신 구현
- 팀 협업 경험
  - 소프트웨어/하드웨어 협업
  - 산업체 멘토링 활용

---

## 참고 자료

- [TensorFlow Lite](https://www.tensorflow.org/lite)
- [MobileNet SSD v2](https://arxiv.org/abs/1801.04381)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Raspberry Pi Documentation](https://www.raspberrypi.org/documentation/)
- [Google Speech API](https://cloud.google.com/speech-to-text)

---

## 팀원

**불켜조 (5인 팀)**

- **김동진** (소프트웨어 총괄, 서기)
- 장OO (팀장)
- 이OO (하드웨어, 그래픽)
- 고OO (어플리케이션, 구매)
- 이OO (하드웨어, 포스터)

---

**제작 기간**: 2025년 3월 ~ 6월  
**프로젝트 지원**: 명지전문대학교 캡스톤디자인
