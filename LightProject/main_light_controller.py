import time
import serial
import requests
import sys

PRODUCT_ID = 2
SPRING_API_BASE = "https://lightproject.duckdns.org/api"
SERIAL_PORT = "/dev/ttyACM0"
BAUD_RATE = 9600

# 시리얼 포트 오픈 시도
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
    print("[FAIL] Serial 포트를 열 수 없습니다. 다른 프로그램이 사용 중일 수 있습니다.")
    sys.exit(1)

def get_light_mode():
    try:
        res = requests.get(f"{SPRING_API_BASE}/light-mode", params={"productId": PRODUCT_ID})
        data = res.json()
        return data.get("mode", "").strip()
    except Exception as e:
        print(f"[ERROR] get_light_mode: {e}")
        return None

def get_pending_commands():
    try:
        res = requests.get(f"{SPRING_API_BASE}/light-control/pending", params={"productId": PRODUCT_ID})
        return res.json()
    except Exception as e:
        print(f"[ERROR] get_pending_commands: {e}")
        return []

def mark_command_executed(command_id):
    try:
        requests.post(f"{SPRING_API_BASE}/light-control/{command_id}/execute")
    except Exception as e:
        print(f"[ERROR] mark_command_executed: {e}")

def send_to_arduino(r, g, b):
    command = f"{r},{g},{b}\n"
    print(f"[INFO] (LOG) 보낼 명령: {command.strip()}")
    try:
        ser.write(command.encode())  # 실제 전송
    except Exception as e:
        print(f"[ERROR] 아두이노 전송 실패: {e}")

if __name__ == "__main__":
    prev_mode = None
    while True:
        try:
            mode = get_light_mode()
            if mode and mode != prev_mode:
                print(f"[INFO] Mode changed: {mode}")
                prev_mode = mode
                # → 아두이노에게 모드 전송
                try:
                    ser.write((mode + "\n").encode())
                    print(f"[INFO] 아두이노에 모드 전송: {mode}")
                except Exception as e:
                    print(f"[ERROR] 아두이노 모드 전송 실패: {e}")

            if mode == "APP":
                commands = get_pending_commands()
                for cmd in commands:
                    r, g, b = cmd["r"], cmd["g"], cmd["b"]
                    send_to_arduino(r, g, b)
                    mark_command_executed(cmd["id"])
                time.sleep(1)
            else:
                time.sleep(5)
        except KeyboardInterrupt:
            break

