# send_motor_command.py
import requests

SPRING_API_BASE = "https://lightproject.duckdns.org/api"
PRODUCT_ID = 2  # 테스트할 제품 ID

def set_mode(mode):
    try:
        res = requests.post(
            f"{SPRING_API_BASE}/motor-mode",
            json={  # <-- 반드시 JSON 본문으로 보내야 함
                "productId": PRODUCT_ID,
                "mode": mode
            }
        )
        print(f"[OK] 모드 설정됨: {mode}")
        print(f"응답코드: {res.status_code}, 응답내용: {res.text}")
    except Exception as e:
        print(f"[ERROR] 모드 설정 실패: {e}")

def send_command(command_type):
    try:
        res = requests.post(f"{SPRING_API_BASE}/motor-control", json={
            "productId": PRODUCT_ID,
            "commandType": command_type
        })
        print(f"[OK] 명령 전송됨: {command_type}")
        print(f"응답코드: {res.status_code}, 응답내용: {res.text}")
    except Exception as e:
        print(f"[ERROR] 명령 전송 실패: {e}")

if __name__ == "__main__":
    print("1. 모드 설정 (MANUAL/STOP/TRACK)")
    mode = input("모드를 입력하세요: ").strip().upper()
    set_mode(mode)

    if mode == "REMOTE":
        while True:
            cmd = input("보낼 명령 (FORWARD/LEFT/RIGHT/STOP): ").strip().upper()
            if cmd == "EXIT":
                break
            send_command(cmd)
