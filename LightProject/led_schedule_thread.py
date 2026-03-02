import requests
import time
from datetime import datetime
from dateutil import parser

SPRING_API_BASE = "https://lightproject.duckdns.org/api"
PRODUCT_ID = 2

def send_light_command(r, g, b):
    # 테스트용 dummy 출력 (실제 LED 제어는 생략)
    print(f"[DEBUG] 조명 명령 전송 → R:{r}, G:{g}, B:{b}")

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

def led_schedule_thread():
    try:
        res = requests.get(f"{SPRING_API_BASE}/led/schedule", params={"productId": PRODUCT_ID}, timeout=5)
        res.raise_for_status()
        schedules = res.json()
        now = datetime.now()

        print(f"[INFO] 현재 시각: {now.strftime('%Y-%m-%d %H:%M:%S')}")

        for s in schedules:
            if not s.get("executed") and "scheduledTime" in s:
                try:
                    scheduled_time = parser.parse(s["scheduledTime"])
                    if now >= scheduled_time:
                        print(f"[SCHEDULE] 실행 시각 도달: {scheduled_time}, 명령 실행")
                        send_light_command(s["ledColorR"], s["ledColorG"], s["ledColorB"])
                        mark_led_schedule_executed(s["id"])
                    else:
                        print(f"[SKIP] 아직 실행 시간 아님: {scheduled_time}")
                except Exception as parse_err:
                    print(f"[SCHEDULE] 시간 파싱 실패: {parse_err}")
    except Exception as e:
        print(f"[ERROR] 스케줄 조회 실패: {e}")

if __name__ == "__main__":
    led_schedule_thread()