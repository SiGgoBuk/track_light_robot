import speech_recognition as sr
import requests
import time

import os
os.environ["PYTHONWARNINGS"] = "ignore"
os.environ["SDL_AUDIODRIVER"] = "dummy"  # 또는 dummy
os.environ["ALSA_CARD"] = "null"

# ALSA 출력 억제
import ctypes
asound = ctypes.cdll.LoadLibrary('libasound.so')
asound.snd_lib_error_set_handler(None)

VOICE_API_URL = "https://lightproject.duckdns.org/api/voice-command"
LED_API_URL = "https://lightproject.duckdns.org/api/light-control"
BASE_API_URL = "https://lightproject.duckdns.org/api"
product_id = 2
command_map = {}

# ========== 명령어 API로 불러오기 ==========
import re

def normalize(text):
    return re.sub(r"[ \-_\s]", "", text)  # 공백, 하이픈, 언더스코어 제거

def load_command_map_from_api(product_id):
    global command_map
    try:
        res = requests.get(f"{BASE_API_URL}/voice-command", params={"productId": product_id}, timeout=5)
        res.raise_for_status()
        data = res.json()
        command_map = {
            normalize(item['inputText']): item['actionName'] for item in data
        }
        print("[DEBUG] 명령어 로딩 성공:", command_map)
    except Exception as e:
        print("[ERROR] 명령어 API 호출 실패:", e)
        command_map = {}

# ========== 음성 인식 ==========
def listen(prompt="말씀하세요..."):
    print(f"\n{prompt}")
    r = sr.Recognizer()
    with sr.Microphone() as source:
        r.adjust_for_ambient_noise(source)
        print("듣고 있습니다...")
        audio = r.listen(source)
    try:
        text = r.recognize_google(audio, language='ko-KR')
        print("[INFO] 인식된 텍스트:", text)
        return text.replace(" ", "")
    except sr.UnknownValueError:
        print("[WARN] 음성을 인식하지 못했습니다.")
        return None
    except sr.RequestError:
        print("[ERROR] Google API 오류")
        return None

# ========== 명령 실행 ==========
def match_command(text):
    normalized = normalize(text)
    action = command_map.get(normalized)
    if action:
        print(f"[INFO] '{normalized}' → 실행: '{action}'")
        run_action(action)
    else:
        print(f"[WARN] 등록되지 않은 명령어: '{normalized}'")
        log_unmatched(normalized)

def run_action(action):
    if action == "켜기":
        print("[ACT] 조명 켜기 명령 전송")
        set_light_mode("APP")
        send_light_command(255, 255, 255)  # 흰색 점등
    elif action == "LED_OFF":
        print("[ACT] 조명 끄기 명령 전송")
        set_light_mode("APP")
        send_light_command(0, 0, 0)
    elif action == "MOTOR_FORWARD":
        print("[ACT] 모터 전진 명령 전송")
        set_motor_mode("MANUAL")
        send_motor_command("FORWARD")
    elif action == "정지":
        print("[ACT] 모터 정지 명령 전송")
        set_motor_mode("MANUAL")
        send_motor_command("STOP")
    else:
        print(f"[ACT] '{action}' 은(는) 정의되지 않은 액션입니다.")


def log_unmatched(text):
    print(f"[LOG] 미등록 명령: '{text}'")
    # 향후 서버로 log 전송 가능

def print_command_map():
    print("[INFO] 등록된 명령어 목록:")
    if not command_map:
        print("  (없음)")
    for k, v in command_map.items():
        print(f"  - '{k}' → '{v}'")
        
def set_light_mode(mode):
    try:
        res = requests.post(f"{BASE_API_URL}/light-mode", json={
            "productId": product_id,
            "mode": mode
        }, timeout=3)
        print(f"[OK] 조명 모드 설정: {mode} →", res.status_code)
    except Exception as e:
        print("[ERROR] 조명 모드 설정 실패:", e)

def set_motor_mode(mode):
    try:
        res = requests.post(f"{BASE_API_URL}/motor-mode", json={
            "productId": product_id,
            "mode": mode
        }, timeout=3)
        print(f"[OK] 모터 모드 설정: {mode} →", res.status_code)
    except Exception as e:
        print("[ERROR] 모터 모드 설정 실패:", e)
# =========== 제어 테스트 =============
def send_light_command(r, g, b):
    try:
        res = requests.post(f"{BASE_API_URL}/light-control", json={
            "productId": product_id,
            "red": r,
            "green": g,
            "blue": b
        }, timeout=3)
        print("[OK] 조명 명령 전송 완료:", res.status_code)
    except Exception as e:
        print("[ERROR] 조명 명령 전송 실패:", e)

def send_motor_command(commandType):
    try:
        res = requests.post(f"{BASE_API_URL}/motor-control", json={
            "productId": product_id,
            "commandType": commandType
        }, timeout=3)
        print("[OK] 모터 명령 전송 완료:", res.status_code)
    except Exception as e:
        print("[ERROR] 모터 명령 전송 실패:", e)


# ========== 메인 ==========
def main():
    load_command_map_from_api(product_id)
    print_command_map()  # 여기에 추가
    try:
        while True:
            text = listen("명령어를 말씀해주세요.")
            if text:
                match_command(text)
            time.sleep(1)
    except KeyboardInterrupt:
        print("\n[INFO] 종료합니다.")

if __name__ == "__main__":
    main()
