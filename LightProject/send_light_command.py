# send_light_command.py
import requests

SPRING_API_BASE = "https://lightproject.duckdns.org/api"
PRODUCT_ID = 2  # 테스트할 제품 ID

def set_mode(mode):
    try:
        res = requests.post(f"{SPRING_API_BASE}/light-mode", params={
            "productId": PRODUCT_ID,
            "mode": mode
        })
        if res.status_code == 200:
            print(f"[OK] 조명 모드 설정됨: {mode}")
        else:
            print(f"[ERROR] 응답 실패: {res.status_code} - {res.text}")
    except Exception as e:
        print(f"[ERROR] 조명 모드 설정 실패: {e}")

def send_rgb_command(r, g, b):
    try:
        res = requests.post(f"{SPRING_API_BASE}/light-control", params={
            "productId": PRODUCT_ID,
            "r": r,
            "g": g,
            "b": b
        })
        print(f"[OK] 명령 전송됨: {r},{g},{b}")
    except Exception as e:
        print(f"[ERROR] 명령 전송 실패: {e}")

if __name__ == "__main__":
    print("1. 모드 설정 (REMOTE/AUTO/OTHER)")
    mode = input("모드를 입력하세요: ").strip().upper()
    set_mode(mode)

    if mode == "REMOTE":
        while True:
            rgb = input("RGB 값 입력 (예: 255,100,50), 종료하려면 EXIT: ").strip()
            if rgb.upper() == "EXIT":
                break
            try:
                r, g, b = map(int, rgb.split(","))
                send_rgb_command(r, g, b)
            except:
                print("[ERROR] 올바른 형식이 아닙니다. 예: 255,100,0")
