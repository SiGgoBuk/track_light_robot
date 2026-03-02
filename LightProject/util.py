'''
util.py
'''

import RPi.GPIO as GPIO
GPIO.setwarnings(False)

import os, time

edgetpu=0 # If Coral USB Accelerator connected, then make it '1' otherwise '0'

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
# GND 	    # 6, 39pin


cam_light = 17
headlight_right = 18
headlight_left = 27 
sp_light=9 

# PWM 객체 전역
pwm_ena1 = None
pwm_ena2 = None
pwm_enb1 = None
pwm_enb2 = None

def init_gpio():
    global pwm_ena1, pwm_ena2, pwm_enb1, pwm_enb2
    
    if pwm_ena1 and pwm_ena2 and pwm_enb1 and pwm_enb2:
        print("[INFO] GPIO 이미 초기화됨 → 중복 방지")
        return
	
    GPIO.setmode(GPIO.BCM)
    motor_pins = [m1_1, m1_2, m2_1, m2_2, m3_1, m3_2, m4_1, m4_2]
    light_pins = [cam_light, headlight_right, headlight_left, sp_light]
    en_pins = [ENA1, ENA2, ENB1, ENB2]

    for pin in motor_pins + light_pins + en_pins:
        GPIO.setup(pin, GPIO.OUT)

    pwm_ena1 = GPIO.PWM(ENA1, 1000)
    pwm_ena2 = GPIO.PWM(ENA2, 1000)
    pwm_enb1 = GPIO.PWM(ENB1, 1000)
    pwm_enb2 = GPIO.PWM(ENB2, 1000)

    pwm_ena1.start(0)
    pwm_ena2.start(0)
    pwm_enb1.start(0)
    pwm_enb2.start(0)

def set_speed(left_speed, right_speed):
    # 100을 넘지 않도록 제한 (0~100)
    left_duty = min(max(left_speed / 10, 0), 100)
    right_duty = min(max(right_speed / 10, 0), 100)

    pwm_ena1.ChangeDutyCycle(right_duty)  # 오른쪽
    pwm_enb1.ChangeDutyCycle(left_duty)   # 왼쪽
    pwm_ena2.ChangeDutyCycle(right_duty)  # 오른쪽
    pwm_enb2.ChangeDutyCycle(left_duty)   # 왼쪽
    
def forward(speed=200):
    set_speed(left_speed=speed, right_speed=speed)

    GPIO.output(m1_1, False)
    GPIO.output(m1_2, True)
    
    GPIO.output(m2_1, True)
    GPIO.output(m2_2, False)
    GPIO.output(m3_1, True)
    GPIO.output(m3_2, False)
    GPIO.output(m4_1, True)
    GPIO.output(m4_2, False)
    
    '''
    GPIO.output(m1_1, False)
    GPIO.output(m1_2, True)
    GPIO.output(m2_1, False)
    GPIO.output(m2_2, True)
    GPIO.output(m3_1, False)
    GPIO.output(m3_2, True)
    GPIO.output(m4_1, False)
    GPIO.output(m4_2, True)
    '''

def back(speed=200):
    set_speed(left_speed=speed, right_speed=speed)
    
    GPIO.output(m1_1, True)
    GPIO.output(m1_2, False)
    
    GPIO.output(m2_1, False)
    GPIO.output(m2_2, True)
    GPIO.output(m3_1, False)
    GPIO.output(m3_2, True)
    GPIO.output(m4_1, False)
    GPIO.output(m4_2, True)
    
'''
    GPIO.output(m1_1, True)
    GPIO.output(m1_2, False)
    GPIO.output(m2_1, True)
    GPIO.output(m2_2, False)
    GPIO.output(m3_1, True)
    GPIO.output(m3_2, False)
    GPIO.output(m4_1, True)
    GPIO.output(m4_2, False)
'''

def left(speed=200):
    set_speed(left_speed=speed, right_speed=speed)
    # forward
    GPIO.output(m2_1, True)
    GPIO.output(m2_2, False)
    GPIO.output(m4_1, True)
    GPIO.output(m4_2, False)
    
    # back
    GPIO.output(m1_1, True)
    GPIO.output(m1_2, False)
    GPIO.output(m3_1, False)
    GPIO.output(m3_2, True)
    

def right(speed=200):
    set_speed(left_speed=speed, right_speed=speed)
    # forward
    GPIO.output(m1_1, False)
    GPIO.output(m1_2, True)
    GPIO.output(m3_1, True)
    GPIO.output(m3_2, False)
    
    # back
    GPIO.output(m2_1, False)
    GPIO.output(m2_2, True)
    GPIO.output(m4_1, False)
    GPIO.output(m4_2, True)
    

def stop():
    set_speed(0, 0)

    for pin in [m1_1, m1_2, m2_1, m2_2, m3_1, m3_2, m4_1, m4_2]:
        GPIO.output(pin, False)
