import common as cm
from picamera2 import Picamera2
import cv2
import requests
import threading

picam2 = None  # 파일 상단에 전역 선언

def init_camera():
    global picam2
    if picam2 is None:
        picam2 = Picamera2()
        picam2.start()

# API 불러오기
SPRING_API_BASE = "https://lightproject.duckdns.org/api"
PRODUCT_ID = 2  # 현재 추적 대상 제품

def is_mode_track():
    try:
        res = requests.get(f"{SPRING_API_BASE}/motor-mode", params={"productId": PRODUCT_ID}, timeout=3)
        print(f"[DEBUG] 응답 상태: {res.status_code}, 본문: '{res.text.strip()}'")
        res.raise_for_status()
        data = res.json()
        return data.get("mode", "").strip().upper() == "TRACK"
    except Exception as e:
        print(f"[ERROR] TRACK 모드 확인 실패: {e}")
        return True  # 오류일 경우 종료 방지
    
def save_motor_data(mode, direction, left_speed, right_speed, source="USER_TRACK"):
    try:
        ultrasonic = get_ultrasonic()
        ir = get_ir()
        res = requests.post(f"{SPRING_API_BASE}/motor-data", json={
            "productId": PRODUCT_ID,
            "mode": mode,
            "direction": direction,
            "leftSpeed": left_speed,
            "rightSpeed": right_speed,
            "ultrasonicCm": ultrasonic,
            "irDetected": ir,
            "source": source
        })
        print(f"[LOG] motor_data 저장 완료: mode={mode}, dir={direction}, L={left_speed}, R={right_speed}, src={source}")
    except Exception as e:
        print(f"[ERROR] motor_data 저장 중 오류: {e}")
        
# 추적 모드
track_thread = None

def track_loop():
    while True:
        if not is_mode_track():
            print("[INFO] TRACK 모드 종료 → 루프 대기 중...")
            ut.stop()
            time.sleep(1)
            continue

        print("[INFO] TRACK 모드 감지 → 추적 시작")
        try:
            #main_loop_once()  # 기존 main() 내용을 반복 가능한 함수로 분리
            print("[DEBUG] 추적 로직은 main() 에서 실행 중")
        except Exception as e:
            print(f"[ERROR] 추적 중 예외 발생: {e}")
        time.sleep(1)  # 너무 빠른 재시작 방지


# picam2로 프레임을 가져오는 함수 작성
def get_frame():
    frame = picam2.capture_array()
    return frame

import numpy as np
from PIL import Image
import time
from threading import Thread

import sys
sys.path.insert(0, '/var/www/html/earthrover')
import util as ut
ut.init_gpio()

threshold=0.2
top_k=5 #first five objects with prediction probability above threshhold (0.2) to be considered
#edgetpu=0

model_dir = '/home/pi/Desktop/robotics-level-4-main/all_models'
model = 'mobilenet_ssd_v2_coco_quant_postprocess.tflite'
model_edgetpu = 'mobilenet_ssd_v2_coco_quant_postprocess_edgetpu.tflite'
lbl = 'coco_labels.txt'

tolerance=0.1
x_deviation=0
y_max=0
arr_track_data=[0,0,0,0,0,0]

object_to_track='person'

#---------Flask----------------------------------------
from flask import Flask, Response
from flask import render_template

app = Flask(__name__)

@app.route('/')
def index():
    #return "Default Message"
    return render_template("index.html")

@app.route('/video_feed')
def video_feed():
    def safe_main():
        try:
            for frame in main():
                yield frame
        except Exception as e:
            print(f"[ERROR] video_feed 예외: {e}")
            # 빈 프레임 또는 종료 처리 가능
            yield b''

    return Response(safe_main(), mimetype='multipart/x-mixed-replace; boundary=frame')


                    
#-----initialise motor speed-----------------------------------
"""
import RPi.GPIO as GPIO 
GPIO.setmode(GPIO.BCM)  # choose BCM numbering scheme  

GPIO.setup(26, GPIO.OUT)# set GPIO 20 as output pin
GPIO.setup(0, GPIO.OUT)# set GPIO 21 as output pin
GPIO.setup(12, GPIO.OUT)# set GPIO 12 as output pin
GPIO.setup(16, GPIO.OUT)# set GPIO 16 as output pin
      
pin26 = GPIO.PWM(26, 100)    # create object pin26 for PWM on port 20 at 100 Hertz  
pin0 = GPIO.PWM(0, 100)    # create object pin0 for PWM on port 21 at 100 Hertz  
pin12 = GPIO.PWM(12, 100)    # create object pin12 for PWM on port 20 at 100 Hertz  
pin16 = GPIO.PWM(16, 100)    # create object pin16 for PWM on port 21 at 100 Hertz  

val=100
pin26.start(val)              # start pin26 on 0 percent duty cycle (off)  
pin0.start(val)              # start pin0 on 0 percent duty cycle (off)

pin12.start(val)              # start pin12 on 0 percent duty cycle (off)  
pin16.start(val)              # start pin16 on 0 percent duty cycle (off)  


print("speed set to: ", val)
"""
#------------------------------------------

def track_object(objs,labels):
   
    global x_deviation, y_max, tolerance, arr_track_data
    
    if(len(objs)==0):
        print("no objects to track")
        ut.stop()
        #ut.red_light("OFF")
        arr_track_data=[0,0,0,0,0,0]
        return
    
    flag=0
    for obj in objs:
        lbl=labels.get(obj.id, obj.id)
        if (lbl==object_to_track):
            x_min, y_min, x_max, y_max = list(obj.bbox)
            flag=1
            break
        
    #print(x_min, y_min, x_max, y_max)
    if(flag==0):
        print("selected object no present")
        return
        
    x_diff=x_max-x_min
    y_diff=y_max-y_min
    print("x_diff: ",round(x_diff,5))
    print("y_diff: ",round(y_diff,5))
        
        
    obj_x_center=x_min+(x_diff/2)
    obj_x_center=round(obj_x_center,3)
    
    obj_y_center=y_min+(y_diff/2)
    obj_y_center=round(obj_y_center,3)
    
    #print("[",obj_x_center, obj_y_center,"]")
        
    x_deviation=round(0.5-obj_x_center,3)
    y_max=round(y_max,3)
        
    print("{",x_deviation,y_max,"}")
   
    thread = Thread(target = move_robot)
    thread.start()
    
    arr_track_data[0]=obj_x_center
    arr_track_data[1]=obj_y_center
    arr_track_data[2]=x_deviation
    arr_track_data[3]=y_max
    

def move_robot():
    global x_deviation, y_max, tolerance, arr_track_data
    
    print("moving robot .............!!!!!!!!!!!!!!")
    print(x_deviation, tolerance, arr_track_data)
    
    y=1-y_max #distance from bottom of the frame
    
    if(abs(x_deviation)<tolerance):
        delay1=0
        if(y<0.1):
            cmd="STOP"
            #ut.red_light("ON")
            ut.stop()
            save_motor_data("TRACK", cmd, 50, 50)
        else:
            cmd="FORWARD"
            #ut.red_light("OFF")
            ut.forward(speed=50)
            save_motor_data("TRACK", cmd, 50, 50)
    
    else:
        #ut.red_light("OFF")
        if(x_deviation>=tolerance):
            cmd="LEFT"
            delay1=get_delay(x_deviation)
                
            ut.left(speed=50)
            time.sleep(delay1)
            ut.stop()
            save_motor_data("TRACK", cmd, 20, 50)
                
        if(x_deviation<=-1*tolerance):
            cmd="RIGHT"
            delay1=get_delay(x_deviation)
                
            ut.right(speed=50)
            time.sleep(delay1)
            ut.stop()
            save_motor_data("TRACK", cmd, 50, 20)

    arr_track_data[4]=cmd
    arr_track_data[5]=delay1

def get_delay(deviation):
    deviation=abs(deviation)
    if(deviation>=0.4):
        d=0.080
    elif(deviation>=0.35 and deviation<0.40):
        d=0.060
    elif(deviation>=0.20 and deviation<0.35):
        d=0.050
    else:
        d=0.040
    return d
    
def main():
    
    global track_thread
    if track_thread is None or not track_thread.is_alive():
        track_thread = threading.Thread(target=track_loop)
        track_thread.start()
    
    if not is_mode_track():
        print("[INFO] 현재 TRACK 모드가 아님 → 종료")
        return
    
    try:
        init_camera()
    except RuntimeError as e:
        print(f"[ERROR] 카메라 초기화 실패: {e}")
        return
    
    from util import edgetpu
    
    if edgetpu == 1:
        mdl = model_edgetpu
    else:
        mdl = model
        
    interpreter, labels = cm.load_model(model_dir, mdl, lbl, edgetpu)
    
    fps = 1
    arr_dur = [0, 0, 0]
    
    last_check = 0
    while True:
        
        if time.time() - last_check > 2:  # 2초마다 모드 체크
            if not is_mode_track():
                print("[INFO] TRACK 모드 종료 감지 → 추적 루프 종료")
                ut.stop()
                break
            last_check = time.time()

        start_time = time.time()
        
        #----------------Capture Camera Frame-----------------
        start_t0 = time.time()
        frame = get_frame()
        if frame is None:
            print("Failed to capture frame.")
            break      
        cv2_im = frame
        #cv2_im = cv2.flip(cv2_im, 0)
        cv2_im = cv2.flip(cv2_im, 1)

        cv2_im_rgb = cv2.cvtColor(cv2_im, cv2.COLOR_BGR2RGB)
        pil_im = Image.fromarray(cv2_im_rgb)
       
        arr_dur[0] = time.time() - start_t0
        #----------------------------------------------------
    
       
        #-------------------Inference---------------------------------
        start_t1 = time.time()
        cm.set_input(interpreter, pil_im)
        interpreter.invoke()
        objs = cm.get_output(interpreter, score_threshold=threshold, top_k=top_k)
        
        arr_dur[1] = time.time() - start_t1
        #----------------------------------------------------
       
        #-----------------other------------------------------------
        start_t2 = time.time()
        track_object(objs, labels)  # tracking <<<<<<<
       
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break
 
        cv2_im = append_text_img1(cv2_im, objs, labels, arr_dur, arr_track_data)
        
        # Try encoding the image to JPEG
        try:
            ret, jpeg = cv2.imencode('.jpg', cv2_im)
            if not ret:
                print("Failed to encode image.")
                continue
            pic = jpeg.tobytes()
        except Exception as e:
            print(f"Error during image encoding: {e}")
            continue
        
        # Flask streaming
        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' + pic + b'\r\n\r\n')
       
        arr_dur[2] = time.time() - start_t2
        fps = round(1.0 / (time.time() - start_time), 1)
        print("*********FPS: ", fps, "************")

    cv2.destroyAllWindows()



def append_text_img1(cv2_im, objs, labels, arr_dur, arr_track_data):
    height, width, channels = cv2_im.shape
    font=cv2.FONT_HERSHEY_SIMPLEX
    
    global tolerance
    
    #draw black rectangle on top
    cv2_im = cv2.rectangle(cv2_im, (0,0), (width, 24), (0,0,0), -1)
   
    #write processing durations
    cam=round(arr_dur[0]*1000,0)
    inference=round(arr_dur[1]*1000,0)
    other=round(arr_dur[2]*1000,0)
    text_dur = 'Camera: {}ms   Inference: {}ms   other: {}ms'.format(cam,inference,other)
    cv2_im = cv2.putText(cv2_im, text_dur, (int(width/4)-30, 16),font, 0.4, (255, 255, 255), 1)
    
    #write FPS 
    total_duration=cam+inference+other
    fps=round(1000/total_duration,1)
    text1 = 'FPS: {}'.format(fps)
    cv2_im = cv2.putText(cv2_im, text1, (10, 20),font, 0.7, (150, 150, 255), 2)

    #write command, tracking status and speed
    cmd=arr_track_data[4]
    cv2_im = cv2.putText(cv2_im, str(cmd), (int(width/2) + 10, height-8),font, 0.68, (0, 255, 255), 2)
    
    if(cmd==0):
        str1="No object"
    elif(cmd=='Stop'):
        str1='Acquired'
    else:
        str1='Tracking'
    cv2_im = cv2.putText(cv2_im, str1, (width-140, 18),font, 0.7, (0, 255, 255), 2)
      
    #draw the tolerance box
    cv2_im = cv2.rectangle(cv2_im, (int(width/2-tolerance*width),0), (int(width/2+tolerance*width),height), (0,255,0), 2)
    
    for obj in objs:
        x0, y0, x1, y1 = list(obj.bbox)
        x0, y0, x1, y1 = int(x0*width), int(y0*height), int(x1*width), int(y1*height)
        percent = int(100 * obj.score)
        
        box_color, text_color, thickness=(0,150,255), (0,255,0),1
        

        text3 = '{}% {}'.format(percent, labels.get(obj.id, obj.id))
        
        if(labels.get(obj.id, obj.id)=="person"):
            cv2_im = cv2.rectangle(cv2_im, (x0, y0), (x1, y1), box_color, thickness)
            cv2_im = cv2.putText(cv2_im, text3, (x0, y1-5),font, 0.5, text_color, thickness)
            
    return cv2_im

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=2204, threaded=True) # Run FLASK
    main()
