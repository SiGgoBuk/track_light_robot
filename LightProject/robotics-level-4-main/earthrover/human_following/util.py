'''
Project: Earthrover Robot
Author: Jitesh Saini
Github: https://github.com/jiteshsaini
website: https://helloworld.co.in

'''

import RPi.GPIO as GPIO
GPIO.setwarnings(False)

import os, time

edgetpu=0 # If Coral USB Accelerator connected, then make it '1' otherwise '0'

m1_1 = 19
m1_2 = 13
m2_1 = 6 
m2_2 = 5 
m3_1 = 22 # 15pin
m3_2 = 23 # 16pin
m4_1 = 24 # 18pin
m4_2 = 25 # 22pin
# ENA2 = 12 # 32pin\
# ENB2 = 16 # 36pin

cam_light = 17
headlight_right = 18
headlight_left = 27 
sp_light=9 


def init_gpio():
	GPIO.setmode(GPIO.BCM)
	# ========= Motor ==========
	GPIO.setup(m1_1,GPIO.OUT)
	GPIO.setup(m1_2,GPIO.OUT)
	
	GPIO.setup(m2_1,GPIO.OUT)
	GPIO.setup(m2_2,GPIO.OUT)
	
	GPIO.setup(m3_1, GPIO.OUT)
	GPIO.setup(m3_2, GPIO.OUT)
	
	GPIO.setup(m4_1, GPIO.OUT)
	GPIO.setup(m4_2, GPIO.OUT)
	# ===========================
	GPIO.setup(cam_light,GPIO.OUT)
	GPIO.setup(headlight_right,GPIO.OUT)
	GPIO.setup(headlight_left,GPIO.OUT)
	GPIO.setup(sp_light,GPIO.OUT)
	

def back():
    print("moving back!!!!!!")
    # 모터 세트 1
    GPIO.output(m1_1, False)
    GPIO.output(m1_2, True)
    GPIO.output(m2_1, True)
    GPIO.output(m2_2, False)
    
    # 모터 세트 2
    GPIO.output(m3_1, False)
    GPIO.output(m3_2, True)
    GPIO.output(m4_1, True)
    GPIO.output(m4_2, False)

def right():
    # 모터 세트 1
    GPIO.output(m1_1, True)
    GPIO.output(m1_2, False)
    GPIO.output(m2_1, True)
    GPIO.output(m2_2, False)

    # 모터 세트 2
    GPIO.output(m3_1, True)
    GPIO.output(m3_2, False)
    GPIO.output(m4_1, True)
    GPIO.output(m4_2, False)

def left():
    # 모터 세트 1
    GPIO.output(m1_1, False)
    GPIO.output(m1_2, True)
    GPIO.output(m2_1, False)
    GPIO.output(m2_2, True)

    # 모터 세트 2
    GPIO.output(m3_1, False)
    GPIO.output(m3_2, True)
    GPIO.output(m4_1, False)
    GPIO.output(m4_2, True)

def forward():
    # 모터 세트 1
    GPIO.output(m1_1, True)
    GPIO.output(m1_2, False)
    GPIO.output(m2_1, False)
    GPIO.output(m2_2, True)

    # 모터 세트 2
    GPIO.output(m3_1, True)
    GPIO.output(m3_2, False)
    GPIO.output(m4_1, False)
    GPIO.output(m4_2, True)

def stop():
    # 모터 세트 1
    GPIO.output(m1_1, False)
    GPIO.output(m1_2, False)
    GPIO.output(m2_1, False)
    GPIO.output(m2_2, False)

    # 모터 세트 2
    GPIO.output(m3_1, False)
    GPIO.output(m3_2, False)
    GPIO.output(m4_1, False)
    GPIO.output(m4_2, False)

def speak_tts(text,gender):
	cmd="python /var/www/html/earthrover/speaker/speaker_tts.py '" + text + "' " + gender + " &"
	os.system(cmd)
	
def camera_light(state):
	if(state=="ON"):
		GPIO.output(cam_light, True)
		#print("light on")
	else:
		GPIO.output(cam_light, False)
		#print("light off")
		
def head_lights(state):
	if(state=="ON"):
		GPIO.output(headlight_left, True)
		GPIO.output(headlight_right, True)
		#print("light on")
	else:
		GPIO.output(headlight_left, False)
		GPIO.output(headlight_right, False)
		#print("light off")
		
def red_light(state):
	if(state=="ON"):
		GPIO.output(sp_light, True)
		#print("light on")
	else:
		GPIO.output(sp_light, False)
		#print("light off")
	
