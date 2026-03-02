import requests
import time
from datetime import datetime
# ⚠️ 추후 활성화용: pip install python-dateutil 필요
from dateutil import parser

from motor_control import control_motor
from util import stop
from human_follower import main as track_person
import threading

PRODUCT_ID = 2
SPRING_API_BASE = "https://lightproject.duckdns.org/api"

# ✅ 모드 + updatedAt 받아오기 (현재는 text만 받아서 동작, 추후 JSON 응답 가능 시 주석 해제)
from dateutil import parser  # 주석 해제 필요

def get_motor_mode(product_id):
    try:
        res = requests.get(f"{SPRING_API_BASE}/motor-mode", params={"productId": product_id})
        res.raise_for_status()

        data = res.json()
        mode = data.get("mode", "STOP").strip().upper()

        # updatedAt 값이 없거나 None이면 datetime.min으로 대체
        updated_at_str = data.get("updatedAt")
        if updated_at_str:
            updated_at = parser.parse(updated_at_str)
        else:
            updated_at = datetime.min

        return mode, updated_at

    except Exception as e:
        print(f"[ERROR] get_motor_mode: {e}")
        return "STOP", datetime.min

# 실행 안 된 명령 가져오기
def get_pending_command(product_id):
    try:
        res = requests.get(f"{SPRING_API_BASE}/motor-control/pending", params={"productId": product_id})
        res.raise_for_status()
        return res.json()
    except Exception as e:
        print(f"[ERROR] get_pending_command: {e}")
        return []

def mark_command_executed(command_id):
    try:
        res = requests.post(f"{SPRING_API_BASE}/motor-control/{command_id}/execute")
        res.raise_for_status()
        print(f"[INFO] Command {command_id} marked as executed")
    except Exception as e:
        print(f"[ERROR] mark_command_executed: {e}")

def save_motor_data(product_id, mode, speed, direction, ultrasonic, ir_detected):
    try:
        res = requests.post(f"{SPRING_API_BASE}/motor-data", json={
            "productId": product_id,
            "mode": mode,
            "speed": speed,
            "direction": direction,
            "ultrasonicCm": ultrasonic,
            "irDetected": ir_detected
        })
        print(f"[DEBUG] motor_data 응답 코드: {res.status_code}")
        print(f"[DEBUG] motor_data 응답 본문: {res.text}")
        if res.status_code == 200:
            print("[LOG] motor_data 저장 완료")
        else:
            print("[WARN] motor_data 저장 실패")
    except Exception as e:
        print(f"[ERROR] save_motor_data: {e}")

# 센서 더미값
def get_ultrasonic():
    return 42.0

def get_ir():
    return False

# TRACK 모드 실행 (스레드 방식)
track_thread = None

def run_track_mode():
    global track_thread
    if track_thread and track_thread.is_alive():
        print("[INFO] TRACK 모드 이미 실행 중")
        return
    track_thread = threading.Thread(target=track_person)
    track_thread.start()

def main():
    print("Main Motor Controller 시작됨")
    while True:
        mode, mode_updated_at = get_motor_mode(PRODUCT_ID)
        print(f"▶️ 현재 모드: {mode}")

        if mode == "TRACK":
            run_track_mode()

        elif mode in ("REMOTE", "MANUAL"):
            commands = get_pending_command(PRODUCT_ID)
            if commands and len(commands) > 0:
                control = commands[0]

                # --- [현재는 시간 비교 생략] ---
                direction = control.get("commandType", "STOP")
                speed = 50
                print(f"[LOG] 유효 명령 실행: direction={direction}")
                control_motor(direction, speed, duration=1.0)
                save_motor_data(PRODUCT_ID, mode, speed, direction, get_ultrasonic(), get_ir())
                mark_command_executed(control["id"])

                # --- [추후 시간 비교 기능] ---
                # from dateutil import parser
                # command_time = parser.parse(control["createdAt"])
                # if command_time >= mode_updated_at:
                #     direction = control.get("commandType", "STOP")
                #     speed = 80
                #     print(f"[LOG] 유효 명령 실행: direction={direction}")
                #     control_motor(direction, speed)
                #     save_motor_data(PRODUCT_ID, mode, speed, direction, get_ultrasonic(), get_ir())
                #     mark_command_executed(control["id"])
                # else:
                #     print("[SKIP] 모드 변경 전 명령 무시됨")

        elif mode == "STOP":
            stop()
            print("[LOG] STOP 모드 실행됨")

        else:
            print(f"[WARN] 알 수 없는 모드: {mode}")

        time.sleep(2)

if __name__ == "__main__":
    main()