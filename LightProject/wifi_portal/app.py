from flask import Flask, render_template, request, jsonify
import os
import subprocess
import threading
import re

app = Flask(__name__)
WPA_CONF_PATH = "/etc/wpa_supplicant/wpa_supplicant.conf"

def get_known_ssids():
    known_ssids = set()

    # 1. wpa_supplicant
    try:
        with open(WPA_CONF_PATH, "r") as f:
            conf = f.read()
            known_ssids.update(re.findall(r'ssid="([^"]+)"', conf))
    except:
        pass

    # 2. NetworkManager
    try:
        nm_files = os.listdir("/etc/NetworkManager/system-connections/")
        for file in nm_files:
            try:
                with open(f"/etc/NetworkManager/system-connections/{file}", "r") as f:
                    for line in f:
                        if line.strip().startswith("ssid="):
                            ssid = line.strip().split("=", 1)[1]
                            known_ssids.add(ssid)
            except:
                continue
    except:
        pass

    return list(known_ssids)

def get_saved_ssids():
    if not os.path.exists(WPA_CONF_PATH):
        return []
    with open(WPA_CONF_PATH, "r") as f:
        content = f.read()
    return re.findall(r'ssid="([^"]+)"', content)

@app.route('/')
def index():
    try:
        # NetworkManager 및 wpa_supplicant 기반 저장된 SSID
        saved_list = get_known_ssids()

        # 현재 검색된 SSID 목록
        result = subprocess.check_output(['sudo', '/sbin/iwlist', 'wlan0', 'scan'], universal_newlines=True)
        ssids = re.findall(r'ESSID:"([^"]+)"', result)
        wifi_list = list(set(ssids))

        print(f"[DEBUG] 저장된 SSID: {saved_list}")
        print(f"[DEBUG] 검색된 SSID 리스트: {wifi_list}")
    except Exception as e:
        print(f"[ERROR] Wi-Fi 리스트 스캔 실패: {e}")
        wifi_list, saved_list = [], []

    return render_template('index.html', wifi_list=wifi_list, saved_list=saved_list)


@app.route('/connect', methods=['POST'])
def connect():
    ssid = request.form.get('ssid')
    if not ssid:
        return "SSID가 필요합니다.", 400

    try:
        # 저장된 SSID의 우선순위 조정
        with open(WPA_CONF_PATH, 'r') as f:
            content = f.read()

        blocks = re.findall(r'(network=\{[^}]+\})', content, re.DOTALL)
        updated = []

        for block in blocks:
            if f'ssid="{ssid}"' in block:
                block = re.sub(r'priority=\d+', 'priority=100', block)
                if 'priority=' not in block:
                    block = block.rstrip('}') + '\n    priority=100\n}'
            else:
                block = re.sub(r'priority=\d+', 'priority=1', block)
                if 'priority=' not in block:
                    block = block.rstrip('}') + '\n    priority=1\n}'
            updated.append(block)

        final_conf = 'ctrl_interface=DIR=/var/run/wpa_supplicant GROUP=netdev\nupdate_config=1\ncountry=KR\n' + "\n".join(updated)

        with open("/tmp/wpa_supplicant.conf", "w") as f:
            f.write(final_conf)

        os.system(f"sudo cp /tmp/wpa_supplicant.conf {WPA_CONF_PATH}")
        os.system("sudo systemctl stop hostapd")
        os.system("sudo systemctl stop dnsmasq")
        os.system("sudo wpa_cli -i wlan0 reconfigure")

        return "연결 시도 중입니다. 잠시 기다려 주세요."
    except Exception as e:
        return f"오류 발생: {e}", 500


@app.route('/submit', methods=['POST'])
def submit():
    ssid = request.form.get('ssid')
    password = request.form.get('password')
    identity = request.form.get('identity')

    if not ssid or not password:
        return "SSID 또는 비밀번호가 누락되었습니다.", 400

    # 새로운 SSID 블록 만들기
    if identity:  # Enterprise Wi-Fi
        new_block = f'''
network={{
    ssid="{ssid}"
    key_mgmt=WPA-EAP
    eap=PEAP
    identity="{identity}"
    password="{password}"
    phase2="auth=MSCHAPV2"
    priority=100
}}'''
    else:
        new_block = f'''
network={{
    ssid="{ssid}"
    psk="{password}"
    priority=100
}}'''

    try:
        # 기존 conf 읽기
        existing_conf = ""
        if os.path.exists(WPA_CONF_PATH):
            with open(WPA_CONF_PATH, "r") as f:
                existing_conf = f.read()

        # 모든 블록을 낮은 priority로 바꾸고, 현재 SSID는 100으로 유지
        def update_priorities(conf_text, target_ssid):
            blocks = re.findall(r'(network=\{[^}]+\})', conf_text, re.DOTALL)
            updated = []

            for block in blocks:
                if f'ssid="{target_ssid}"' in block:
                    block = re.sub(r'priority=\d+', 'priority=100', block)
                    if 'priority=' not in block:
                        block = block.rstrip('}') + '\n    priority=100\n}'
                else:
                    block = re.sub(r'priority=\d+', 'priority=1', block)
                    if 'priority=' not in block:
                        block = block.rstrip('}') + '\n    priority=1\n}'
                updated.append(block)

            return "\n".join(updated)

        new_conf_body = update_priorities(existing_conf, ssid)
        final_conf = f'''
ctrl_interface=DIR=/var/run/wpa_supplicant GROUP=netdev
update_config=1
country=KR
{new_block}
{new_conf_body}
'''

        with open("/tmp/wpa_supplicant.conf", "w") as f:
            f.write(final_conf)

        os.system(f"sudo cp /tmp/wpa_supplicant.conf {WPA_CONF_PATH}")
        os.system("sudo systemctl stop hostapd")
        os.system("sudo systemctl stop dnsmasq")
        os.system("sudo wpa_cli -i wlan0 reconfigure")

        # ⏳ 연결 확인 로직
        import time
        def check_connection(ssid, timeout=10):
            for _ in range(timeout):
                try:
                    current = subprocess.check_output(["iwgetid", "-r"], text=True).strip()
                    if current == ssid:
                        return True
                except:
                    pass
                time.sleep(1)
            return False

        if check_connection(ssid):
            return "설정이 적용되었습니다. 연결 성공!"
        else:
            try:
                subprocess.run(["sudo", "nmcli", "connection", "delete", ssid], check=False)
            except:
                pass
            return "⚠️ 연결 실패. 비밀번호 또는 아이디가 틀렸을 수 있습니다. 다시 입력해주세요.", 400

    except Exception as e:
        return f"오류 발생: {e}", 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
