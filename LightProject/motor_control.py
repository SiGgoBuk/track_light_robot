import time
from util import init_gpio, forward, back, left, right, stop
init_gpio()

def control_motor(direction: str, speed: int = 10, duration: float = 0.5):
    direction = direction.upper()
    print(f"[GPIO] 방향: {direction}, 속도: {speed}, 지속시간: {duration}")

    if direction == "FORWARD":
        forward(speed)
    elif direction == "BACKWARD":
        back(speed)
    elif direction == "LEFT":
        left(speed)
    elif direction == "RIGHT":
        right(speed)
    elif direction == "STOP":
        stop()
        return
    else:
        print(f"[WARN] 알 수 없는 방향: {direction}")
        stop()
        return

    if duration:
        time.sleep(duration)
        stop()

