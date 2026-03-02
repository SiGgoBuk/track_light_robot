"""
import RPi.GPIO as GPIO
import time

GPIO.setmode(GPIO.BCM)

m1_1 = 19 # 35pin
m1_2 = 13 # 33pin
m2_1 = 6  # 31pin
m2_2 = 5  # 29pin
m3_1 = 22 # 15pin
m3_2 = 23 # 16pin
m4_1 = 24 # 18pin
m4_2 = 25 # 22pin

ENA1 = 26 # 37pin
ENB1 = 0  # 27pin
ENA2 = 12 # 32pin
ENB2 = 16 # 36pin

GPIO.setup(19, GPIO.OUT)  # m1_1
GPIO.setup(13, GPIO.OUT)  # m1_2

GPIO.setup(6, GPIO.OUT)  # m2_1
GPIO.setup(5, GPIO.OUT)  # m2_2

GPIO.setup(22, GPIO.OUT)  # m3_1
GPIO.setup(23, GPIO.OUT)  # m3_2

GPIO.setup(24, GPIO.OUT)  # m4_1
GPIO.setup(25, GPIO.OUT)  # m4_2

GPIO.setup(ENA1, GPIO.OUT)  # ENA1
GPIO.setup(ENA2, GPIO.OUT)  # ENA2
GPIO.setup(ENB1, GPIO.OUT)  # ENB1
GPIO.setup(ENB2, GPIO.OUT)  # ENB2

pwm = GPIO.PWM(ENA1, 1000)
pwm2 = GPIO.PWM(ENA2, 1000)
pwm3 = GPIO.PWM(ENB1, 1000)
pwm4 = GPIO.PWM(ENB2, 1000)
pwm.start(50)
pwm2.start(50)
pwm3.start(50)
pwm4.start(50)


GPIO.output(m1_1, True)
GPIO.output(m1_2, False)

GPIO.output(m2_1, True)
GPIO.output(m2_2, False)

GPIO.output(m3_1, True)
GPIO.output(m3_2, False)

GPIO.output(m4_1, True)
GPIO.output(m4_2, False)

time.sleep(1)

GPIO.output(22, False)
GPIO.output(23, False)

GPIO.output(19, False)
GPIO.output(13, False)

pwm.stop()
pwm2.stop()
pwm3.stop()
pwm4.stop()
GPIO.cleanup()
"""
import time
from util import init_gpio, forward, back, left, right, stop

def main():
    init_gpio()
    print("=== 모터 리모컨 테스트 시작 ===")
    print("명령어 입력: f=forward, b=back, l=left, r=right, s=stop, q=quit")

    while True:
        cmd = input("▶️ 명령 입력: ").strip().lower()

        if cmd == "f":
            forward(50)
        elif cmd == "b":
            back(50)
        elif cmd == "l":
            left(50)
        elif cmd == "r":
            right(50)
        elif cmd == "s":
            stop()
        elif cmd == "q":
            print("종료합니다.")
            stop()
            break
        else:
            print("❌ 알 수 없는 명령입니다. 다시 입력하세요.")

        time.sleep(0.5)  # 각 명령마다 0.5초 유지

if __name__ == "__main__":
    main()
