import sys
import os


# ─── ALSA 로그 완전 무력화 ───
sys.stderr = open(os.devnull, 'w')  # stderr 전체 무시 

os.environ["JACK_NO_START_SERVER"] = "1"
os.environ["SDL_AUDIODRIVER"] = "dummy"
os.environ["ALSA_CARD"] = "null"

# [3] ctypes로 ALSA C레벨 핸들러 무력화
import ctypes
from ctypes.util import find_library

try:
    asound = ctypes.cdll.LoadLibrary(find_library('asound'))
    CMPFUNC = ctypes.CFUNCTYPE(None, ctypes.c_char_p, ctypes.c_int,
                                ctypes.c_char_p, ctypes.c_int, ctypes.c_char_p)

    def py_error_handler(filename, line, function, err, fmt):
        return

    c_error_handler = CMPFUNC(py_error_handler)
    asound.snd_lib_error_set_handler(c_error_handler)
except Exception:
    pass  # 실패해도 무시

import time
import threading
import serial
import requests

import speech_recognition as sr
import re
from datetime import datetime
from dateutil import parser

from human_follower_test import main as run_tracking
track_running = False
    
# human_follower의 Flask app 가져오기
#from human_follower import app as flask_app

def flask_thread():
    flask_app.run(host='0.0.0.0', port=2204, threaded=True)
    
# ─────────────── 공통 설정 ───────────────
PRODUCT_ID = 2
SPRING_API_BASE = "https://lightproject.duckdns.org/api"
SERIAL_PORT = "/dev/ttyACM0"
BAUD_RATE = 9600

import socket

def get_local_ip():
    # 현재 라즈베리파이의 로컬 IP 가져오기
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        # 인터넷이 안 되어도 대충 IP 가져오기 가능
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
    except Exception:
        ip = "127.0.0.1"
    finally:
        s.close()
    return ip

def send_ip_to_server(product_id):
    ip = get_local_ip()
    url = "https://lightproject.duckdns.org/api/product/ip"  # ❗ 정확한 주소
    params = {
        "productId": product_id,
        "ip": ip
    }

    try:
        response = requests.post(url, params=params, timeout=5)
        response.raise_for_status()
        print(f"[OK] 내 IP 보고 완료 → {ip}")
    except Exception as e:
        print(f"[ERROR] IP 보고 실패: {e}")
        
# ─────────────── 설정값 가져오기 ───────────────
MOTOR_SPEED = 600
IR_AUTO_ON = False
ULTRA_THRESHOLD = 30
DEFAULT_RGB = (255, 255, 255)

from threading import Lock

config_lock = Lock()
shared_config = {
    "motorSpeed": 600,
    "irLightAutoOn": False,
    "ultrasonicThresholdCm": 30,
    "led_r": 255,
    "led_g": 255,
    "led_b": 255
}

def get_product_settings():
    try:
        res = requests.get(f"{SPRING_API_BASE}/product-settings", params={"productId": PRODUCT_ID})
        raw_settings = res.json()
        settings = {}
        for item in raw_settings:
            key = item["settingKey"]
            val = item["settingValue"]
            if key in ["motorSpeed", "ultrasonicThresholdCm", "led_r", "led_g", "led_b"]:
                settings[key] = int(val)
            elif key == "irLightAutoOn":
                settings[key] = val.lower() == "true"
            else:
                settings[key] = val
        return settings
    except Exception as e:
        print(f"[ERROR] 설정값 불러오기 실패: {e}")
        return {}
# ─────────────── 시리얼 포트 초기화 (조명 전용) ───────────────
ser = None
for i in range(3):
    try:
        ser = serial.Serial(SERIAL_PORT, BAUD_RATE, timeout=1)
        print(f"[OK] Serial 포트 열림: {SERIAL_PORT}")
        break
    except serial.SerialException as e:
        print(f"[WARN] 포트 점유 중: {e}")
        time.sleep(2)
if ser is None:
    print("[FAIL] Serial 포트를 열 수 없습니다.")
    sys.exit(1)

# ─────────────── 음성 API 환경 설정 ───────────────
os.environ["PYTHONWARNINGS"] = "ignore"
os.environ["SDL_AUDIODRIVER"] = "dummy"
os.environ["ALSA_CARD"] = "null"
import ctypes
asound = ctypes.cdll.LoadLibrary('libasound.so')
asound.snd_lib_error_set_handler(None)

# ─────────────── 유틸 함수 ───────────────
def normalize(text):
    return re.sub(r"[ \-_\s]", "", text)

# ─────────────── 명령어 맵 ───────────────
command_map = {}

voice_cmd_error_count = 0
VOICE_CMD_ERROR_THRESHOLD = 5

def load_command_map_from_api(product_id):
    global command_map, voice_cmd_error_count

    try:
        res = requests.get(
            f"{SPRING_API_BASE}/voice-command",
            params={"productId": product_id},
            timeout=5
        )
        res.raise_for_status()
        data = res.json()

        # 정상 응답 시 에러 카운터 초기화 및 command_map 업데이트
        command_map = {
            normalize(item['inputText']): item['actionName']
            for item in data
        }
        voice_cmd_error_count = 0
        print("[DEBUG] 명령어 로딩 성공:", command_map)

    except Exception as e:
        voice_cmd_error_count += 1
        print(f"[WARN] voice-command API 실패 {voice_cmd_error_count}회: {e}")

        if voice_cmd_error_count >= VOICE_CMD_ERROR_THRESHOLD:
            print("[INFO] 실패 누적 → 로컬 fallback 명령어 적용")
            command_map = {
                "켜기": "켜기",
                "끄기": "끄기",
                "밝기증가": "밝기 증가",
                "밝기감소": "밝기 감소",
                "추적": "추적",
                "정지": "정지"
            }

# ─────────────── 조명 제어용 API ───────────────
light_mode_error_count = 0
LIGHT_MODE_ERROR_THRESHOLD = 5

def get_light_mode():
    global light_mode_error_count
    try:
        res = requests.get(f"{SPRING_API_BASE}/light-mode", params={"productId": PRODUCT_ID}, timeout=2)
        light_mode_error_count = 0
        return res.json().get("mode", "").strip()
    except Exception as e:
        print(f"[WARN] get_light_mode 실패: {e}")
        light_mode_error_count += 1
        if light_mode_error_count >= LIGHT_MODE_ERROR_THRESHOLD:
            print("[INFO] 서버 연결 실패 누적 → LOCAL 모드 전환")
            return "LOCAL"
        return None

def get_pending_light_commands():
    try:
        res = requests.get(f"{SPRING_API_BASE}/light-control/pending", params={"productId": PRODUCT_ID})
        return res.json()
    except Exception as e:
        print(f"[ERROR] get_pending_commands: {e}")
        return []

def mark_light_command_executed(command_id):
    try:
        requests.post(f"{SPRING_API_BASE}/light-control/{command_id}/execute")
    except Exception as e:
        print(f"[ERROR] mark_command_executed: {e}")

def send_to_arduino(r, g, b):
    command = f"{r},{g},{b}\n"
    print(f"[INFO] 보낼 명령: {command.strip()}")
    try:
        ser.write(command.encode())
    except Exception as e:
        print(f"[ERROR] 아두이노 전송 실패: {e}")

def set_light_mode(mode):
    try:
        res = requests.post(f"{SPRING_API_BASE}/light-mode", json={"productId": PRODUCT_ID, "mode": mode}, timeout=3)
        print(f"[OK] 조명 모드 설정: {mode} →", res.status_code)
    except Exception as e:
        print("[ERROR] 조명 모드 설정 실패:", e)

def send_light_command(r, g, b):
    try:
        res = requests.post(f"{SPRING_API_BASE}/light-control", json={
            "productId": PRODUCT_ID, "red": r, "green": g, "blue": b
        }, timeout=3)
        print("[OK] 조명 명령 전송 완료:", res.status_code)
    except Exception as e:
        print("[ERROR] 조명 명령 전송 실패:", e)

def get_light_data_from_arduino():
    try:
        ser.write(b"GET_LIGHT_DATA\n")  # 필요 시 아두이노 트리거
        line = ser.readline().decode().strip()

        # 예: "LIGHT:123,POT:456,R:200,G:200,B:200"
        parts = line.split(",")
        data = {}

        for part in parts:
            
            if "[설정 적용]" in part:
                continue  # 디버그 메시지 무시
    
            if ":" in part:
                try:
                    key, value = part.split(":", 1)  # ✅ 고정
                    key = key.strip()
                    value = value.strip()
                    if key == "DISTANCE":
                        data[key] = float(value)
                    else:
                        data[key] = int(value)
                except Exception as e:
                    print(f"[ERROR] 파싱 실패: {part} → {e}")
    
        # 안전하게 가져오기 (없으면 0으로 처리)
        lux = data.get("LIGHT", 0)
        pot = data.get("POT", 0)
        r = data.get("R", 0)
        g = data.get("G", 0)
        b = data.get("B", 0)

        return lux, pot, r, g, b

    except Exception as e:
        print(f"[ERROR] 아두이노 조명 데이터 수신 실패: {e}")
        return 0, 0, 0, 0, 0

last_saved_rgb = (-1, -1, -1)
def save_light_data(mode, r, g, b, lux=None, pot=None):
    try:
        payload = {
            "productId": PRODUCT_ID,
            "brightnessMode": mode,  # 서버는 이걸로 모드 저장
            "ledColorR": r,
            "ledColorG": g,
            "ledColorB": b,
            "brightnessSensorValue": lux,
            "potentiometerValue": pot
        }

        res = requests.post(f"{SPRING_API_BASE}/light-data", json=payload, timeout=3)
        print(f"[LOG] light_data 저장 완료: {payload}")
    except Exception as e:
        print(f"[ERROR] light_data 저장 실패: {e}")

# ─────────────── 모터 제어용 API ───────────────
from motor_control import control_motor  # 모터 제어
import util as ut

motor_mode_error_count = 0
MOTOR_MODE_ERROR_THRESHOLD = 5

def get_motor_mode():
    global motor_mode_error_count
    try:
        res = requests.get(f"{SPRING_API_BASE}/motor-mode", params={"productId": PRODUCT_ID}, timeout=2)
        data = res.json()
        motor_mode_error_count = 0  # 정상 응답 → 에러 카운터 초기화

        mode = data.get("mode", "STOP").strip().upper()
        updated_at_str = data.get("updatedAt")
        updated_at = parser.parse(updated_at_str) if updated_at_str else datetime.min
        return mode, updated_at

    except Exception as e:
        print(f"[WARN] get_motor_mode 실패: {e}")
        motor_mode_error_count += 1
        if motor_mode_error_count >= MOTOR_MODE_ERROR_THRESHOLD:
            print("[INFO] 서버 연결 실패 누적 → fallback: STOP 모드")
            return "STOP", datetime.min
        return None, None

def get_pending_motor_command():
    try:
        res = requests.get(f"{SPRING_API_BASE}/motor-control/pending", params={"productId": PRODUCT_ID})
        res.raise_for_status()
        return res.json()
    except Exception as e:
        print(f"[ERROR] get_pending_command: {e}")
        return []

def mark_motor_command_executed(command_id):
    try:
        res = requests.post(f"{SPRING_API_BASE}/motor-control/{command_id}/execute")
        print(f"[INFO] Command {command_id} marked as executed")
    except Exception as e:
        print(f"[ERROR] mark_command_executed: {e}")

def set_motor_mode(mode):
    try:
        res = requests.post(f"{SPRING_API_BASE}/motor-mode", json={"productId": PRODUCT_ID, "mode": mode}, timeout=3)
        print(f"[OK] 모터 모드 설정: {mode} →", res.status_code)
    except Exception as e:
        print("[ERROR] 모터 모드 설정 실패:", e)

def save_motor_data(mode, direction, left_speed, right_speed, source="USER_APP"):
    try:
        ultrasonic = get_ultrasonic()
        ir = get_ir()
        res = requests.post(f"{SPRING_API_BASE}/motor-data", json={
            "productId": PRODUCT_ID,
            "mode": mode,
            "direction": direction,
            "leftSpeed": left_speed,
            "rightSpeed": right_speed,
            "ultrasonicCm": ultrasonic,
            "irDetected": ir,
            "source": source
        })
        print(f"[LOG] motor_data 저장 완료: mode={mode}, dir={direction}, L={left_speed}, R={right_speed}, src={source}")
    except Exception as e:
        print(f"[ERROR] motor_data 저장 중 오류: {e}")

import RPi.GPIO as GPIO

IR_SENSOR_PIN = 18

GPIO.setmode(GPIO.BCM)
GPIO.setup(IR_SENSOR_PIN, GPIO.IN)

def get_ultrasonic():
    try:
        ser.write(b"GET_ULTRASONIC\n")
        line = ser.readline().decode().strip()
        print(f"[DEBUG] 초음파 수신 라인: '{line}'")
        if line.startswith("DISTANCE:"):
            distance = float(line.split(":")[1])
            print(f"[DEBUG] 초음파 측정: {distance}cm")
            return distance
        else:
            print("[WARN] DISTANCE 응답이 아님")
    except Exception as e:
        print(f"[ERROR] 아두이노 초음파 데이터 수신 실패: {e}")
    return None


def is_mode_track():
    mode, _ = get_motor_mode()
    return mode == "TRACK"

def get_ir():
    val = GPIO.input(IR_SENSOR_PIN)
    #print(f"[DEBUG] IR 센서 값: {val}")  # ← 추가 로그
    return val == 0

# ─────────────── 쓰레드 1: 조명 루프 ───────────────
def light_controller_thread():
    global last_saved_rgb
    prev_mode = None
    while True:
        try:
            mode = get_light_mode()
            if mode and mode != prev_mode:
                print(f"[INFO] 조명 모드 변경됨: {mode}")
                prev_mode = mode
                ser.write((mode + "\n").encode())
                
            if mode == "MANUAL":
                lux, pot, r, g, b = get_light_data_from_arduino()
                with config_lock:
                    r = shared_config["led_r"]
                    g = shared_config["led_g"]
                    b = shared_config["led_b"]
                if (r, g, b) != last_saved_rgb:
                    save_light_data("MANUAL", r, g, b, lux=None, pot=pot)
                    last_saved_rgb = (r, g, b)

            elif mode == "AUTO":
                lux, pot, r, g, b = get_light_data_from_arduino()
                with config_lock:
                    r = shared_config["led_r"]
                    g = shared_config["led_g"]
                    b = shared_config["led_b"]
                if (r, g, b) != last_saved_rgb:
                    save_light_data("AUTO", r, g, b, lux=lux, pot=None)
                    last_saved_rgb = (r, g, b)

            elif mode == "APP":
                commands = get_pending_light_commands()
                for cmd in commands:
                    r, g, b = cmd["r"], cmd["g"], cmd["b"]
                    send_to_arduino(r, g, b)
                    save_light_data("APP", r, g, b)
                    mark_light_command_executed(cmd["id"])
                time.sleep(1)
            else:
                time.sleep(5)
        except Exception as e:
            print(f"[ERROR] light_controller: {e}")
            time.sleep(3)

# ─────────────── 쓰레드 2: 모터 루프 ───────────────
def motor_controller_thread():
    global track_running
    prev_mode = None

    while True:
        mode, mode_updated_at = get_motor_mode()
        if mode is None:
            print("[MOTOR] 서버 재시도 대기 중...")
            time.sleep(2)
            continue

        if mode != prev_mode:
            print(f"[MOTOR] 현재 모드 변경됨: {mode}")
            prev_mode = mode

        if mode == "TRACK":
            if not track_running:
                print("[TRACK] 추적 시작됨")
                threading.Thread(target=run_tracking, daemon=True).start()
                track_running = True
        else:
            track_running = False
            # fallback으로 STOP 모드 들어왔을 경우
            if mode == "STOP":
                control_motor("STOP", 0)

        if mode == "MANUAL":
            commands = get_pending_motor_command()
            if commands:
                cmd = commands[0]
                direction = cmd.get("commandType", "STOP")
                print(f"[MANUAL] 수신 명령: {direction}")
                
                #with config_lock:
                #    speed = shared_config["motorSpeed"]
                #speed = 30
                try:
                    with config_lock:
                        print(f"[DEBUG] 현재 shared_config 내용: {shared_config}")
                        raw_speed = shared_config.get("motorSpeed", 600)
                        print(f"[DEBUG] 가져온 motorSpeed 값 (raw): {raw_speed} (type: {type(raw_speed)})")
                        speed = int(raw_speed)
                except Exception as e:
                    print(f"[WARN] motorSpeed 변환 실패 → 기본값 사용: {e}")
                    speed = 600
                
                control_motor(direction, speed, duration=1.0)
                
                save_motor_data("MANUAL", direction, speed if direction != "STOP" else 0, speed if direction != "STOP" else 0, get_ir())
                mark_motor_command_executed(cmd["id"])

        time.sleep(2)

# ─────────────── 쓰레드 3: 음성 인식 루프 ───────────────
led_brightness = 128  # 전역 밝기 변수

def voice_command_thread():
    global led_brightness
    load_command_map_from_api(PRODUCT_ID)
    r = sr.Recognizer()
    last_reload = time.time()

    try:
        with sr.Microphone() as source:
            r.adjust_for_ambient_noise(source)
            print("말씀해주세요...")

            while True:
                print(f"[VOICE DEBUG] 음성 루프 작동 중...")  
                if time.time() - last_reload > 60:
                    load_command_map_from_api(PRODUCT_ID)
                    last_reload = time.time()

                audio = r.listen(source)
                try:
                    text = r.recognize_google(audio, language='ko-KR').replace(" ", "")
                    if text:
                        print("인식된 명령:", text)
                    action = command_map.get(normalize(text))

                    if action == "켜기":
                        with config_lock:
                            base_r = shared_config.get("led_r", 255)
                            base_g = shared_config.get("led_g", 255)
                            base_b = shared_config.get("led_b", 255)
                        set_light_mode("APP")
                        send_light_command(base_r, base_g, base_b)

                    elif action == "끄기":
                        set_light_mode("APP")
                        send_light_command(0, 0, 0)

                    elif action == "밝기 증가":
                        led_brightness = min(255, led_brightness + 50)
                        with config_lock:
                            base_r = shared_config.get("led_r", 255)
                            base_g = shared_config.get("led_g", 255)
                            base_b = shared_config.get("led_b", 255)
    
                        def scale(val): return int(val * led_brightness / 255)
                        send_light_command(scale(base_r), scale(base_g), scale(base_b))
                        print(f"[VOICE] 밝기 증가 → {led_brightness}")

                    elif action == "밝기 감소":
                        led_brightness = min(255, led_brightness - 50)
                        with config_lock:
                            base_r = shared_config.get("led_r", 255)
                            base_g = shared_config.get("led_g", 255)
                            base_b = shared_config.get("led_b", 255)
    
                        def scale(val): return int(val * led_brightness / 255)
                        send_light_command(scale(base_r), scale(base_g), scale(base_b))
                        print(f"[VOICE] 밝기 감소 → {led_brightness}")

                    elif action == "추적":
                        set_motor_mode("TRACK")
                        print("[VOICE] 추적 모드 시작")

                    elif action == "정지":
                        set_motor_mode("STOP")
                        
                        #send_motor_command("STOP")
                        print("[VOICE] 모터 정지")

                    else:
                        print("등록되지 않은 명령:", text)

                except sr.UnknownValueError:
                    pass
                except sr.RequestError:
                    print("[ERROR] 음성 API 오류")

                time.sleep(1)

    except KeyboardInterrupt:
        print("[INFO] 음성 인식 종료")
        
# ─────────────── 쓰레드 4: LED 스케줄 실행 루프 ───────────────
def led_schedule_thread():
    while True:
        try:
            res = requests.get(f"{SPRING_API_BASE}/led/schedule", params={"productId": PRODUCT_ID}, timeout=5)
            res.raise_for_status()
            schedules = res.json()
            now = datetime.now()

            for s in schedules:
                if not s.get("executed") and "scheduledTime" in s:
                    try:
                        scheduled_time = parser.parse(s["scheduledTime"])
                        if now >= scheduled_time:
                            print(f"[SCHEDULE] 실행 시각 도달: {scheduled_time}, 명령 실행")
                            send_light_command(s["ledColorR"], s["ledColorG"], s["ledColorB"])
                            mark_led_schedule_executed(s["id"])
                    except Exception:
                        pass  # 시간 파싱 실패 로그 생략
        except Exception:
            pass  # 네트워크 실패 로그 생략
        time.sleep(5)

def mark_led_schedule_executed(schedule_id):
    try:
        payload = {"id": schedule_id}
        res = requests.post(
            f"{SPRING_API_BASE}/led/schedule/mark-executed",
            json=payload,
            timeout=3
        )
        if res.status_code == 200:
            print(f"[SCHEDULE] 실행 완료로 표시됨 (ID: {schedule_id})")
        else:
            print(f"[ERROR] mark 실패: {res.status_code} - {res.text}")
    except Exception as e:
        print(f"[ERROR] 스케줄 실행 표시 실패: {e}")

# ─────────────── 쓰레드 5: 초음파 센서 자동정지 실행 루프 ───────────────
def safety_stop_thread():
    while True:
        try:
            if is_mode_track():
                with config_lock:
                    threshold = shared_config["ultrasonicThresholdCm"]
                    speed = shared_config["motorSpeed"]
                
                dist = get_ultrasonic()
                print(f"[SAFETY] 측정된 거리: {dist}cm / 임계값: {threshold}cm")

                # 너무 가까우면 → 후진 시작
                if dist and dist <= threshold:
                    print(f"[SAFETY] 너무 가까움! ({dist}cm) → 후진 시작")
                    while dist and dist <= threshold and is_mode_track():
                        ut.back(speed=speed)
                        save_motor_data("TRACK", "BACKWARD", speed, speed)
                        time.sleep(0.3)  # 적절한 간격으로 반복
                        dist = get_ultrasonic()
                        print(f"[SAFETY] 후진 중... 현재 거리: {dist}cm")
                    
                    ut.stop()
                    save_motor_data("TRACK", "STOP", 0, 0)
                    print("[SAFETY] 안전 거리 확보 → 정지")

            time.sleep(0.5)

        except Exception as e:
            print(f"[ERROR] safety_stop_thread 예외 발생: {e}")

# ─────────────── 쓰레드 6: 설정 주기적 동기화 쓰레드 함수
def setting_polling_thread():
    prev_settings = {}
    while True:
        settings = get_product_settings()
        updated = False

        with config_lock:
            for key in shared_config:
                if key in settings and shared_config[key] != settings[key]:
                    print(f"[CONFIG] 설정 변경됨: {key} → {shared_config[key]} → {settings[key]}")
                    shared_config[key] = settings[key]
                    updated = True

        if updated:
            try:
                cmd = f"SETTINGS:{shared_config['ultrasonicThresholdCm']},{shared_config['led_r']},{shared_config['led_g']},{shared_config['led_b']}\n"
                ser.write(cmd.encode())
                print(f"[CONFIG] 설정 변경 감지 → 아두이노 설정 재전송: {cmd.strip()}")
            except Exception as e:
                print(f"[ERROR] 아두이노 설정 재전송 실패: {e}")

        time.sleep(10)  # 10초마다 확인

# ─────────────── 쓰레드 7: IR 자동 조명 감지 ───────────────
def ir_auto_light_thread():
    while True:
        with config_lock:
            if shared_config["irLightAutoOn"] and get_ir():
                print("[IR] 감지됨 → 조명 켜기")
                send_light_command(255, 255, 255)
        time.sleep(1)


# ─────────────── 메인 ───────────────
if __name__ == "__main__":
    send_ip_to_server(2)
    threads = [
        threading.Thread(target=ir_auto_light_thread, daemon=True),
        threading.Thread(target=setting_polling_thread, daemon=True),
        threading.Thread(target=light_controller_thread, daemon=True),
        threading.Thread(target=motor_controller_thread, daemon=True),
        threading.Thread(target=voice_command_thread, daemon=True),
        threading.Thread(target=led_schedule_thread, daemon=True),
        #threading.Thread(target=flask_thread, daemon=True),
        threading.Thread(target=safety_stop_thread, daemon=True)
    ]
    for t in threads:
        t.start()

    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("\n[종료] 시스템 종료됨.")