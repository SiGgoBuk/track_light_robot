#define LED_R 9
#define LED_G 10
#define LED_B 11

#define POTENTIOMETER A1
#define PHOTORESISTOR A0
#define ULTRASONIC_TRIG 8
#define ULTRASONIC_ECHO 12

String currentMode = "APP";
unsigned long lastAutoUpdate = 0;

int currentR = 0;
int currentG = 0;
int currentB = 0;

int ultrasonicThreshold = 30;  // 설정값으로 받은 초음파 거리 임계값
int defaultR = 255, defaultG = 255, defaultB = 255;  // 설정값 기본 RGB

void setup() {
  Serial.begin(9600);
  pinMode(LED_R, OUTPUT);
  pinMode(LED_G, OUTPUT);
  pinMode(LED_B, OUTPUT);
  pinMode(ULTRASONIC_TRIG, OUTPUT);
  pinMode(ULTRASONIC_ECHO, INPUT);
}

float getUltrasonicDistanceCm() {
  digitalWrite(ULTRASONIC_TRIG, LOW);
  delayMicroseconds(2);
  digitalWrite(ULTRASONIC_TRIG, HIGH);
  delayMicroseconds(10);
  digitalWrite(ULTRASONIC_TRIG, LOW);

  long duration = pulseIn(ULTRASONIC_ECHO, HIGH, 30000);  // 30ms timeout
  float distance = duration * 0.034 / 2;
  return distance;
}

void loop() {
  // 1. 시리얼 명령 처리
  if (Serial.available()) {
    String input = Serial.readStringUntil('\n');
    input.trim();

    if (input == "GET_ULTRASONIC") {
      float distance = getUltrasonicDistanceCm();
      Serial.print("DISTANCE:");
      Serial.println(distance);
      return;
    }

    // ✅ Python 요청 처리: 센서 및 RGB 데이터 반환
    if (input == "GET_LIGHT_DATA") {
      int pot = analogRead(POTENTIOMETER);
      int lightLevel = analogRead(PHOTORESISTOR);
      float distance = getUltrasonicDistanceCm();
      Serial.print("LIGHT:");
      Serial.print(lightLevel);
      Serial.print(",POT:");
      Serial.print(pot);
      Serial.print(",R:");
      Serial.print(currentR);
      Serial.print(",G:");
      Serial.print(currentG);
      Serial.print(",B:");
      Serial.print(currentB);
      Serial.print(",DISTANCE:");
      Serial.println(distance);

      input = "";
      return;
    }

    // ✅ 설정값 수신: SETTINGS:<초음파임계값>,<R>,<G>,<B>
    if (input.startsWith("SETTINGS:")) {
      input = input.substring(9);  // "SETTINGS:" 제거
      int idx1 = input.indexOf(',');
      int idx2 = input.indexOf(',', idx1 + 1);
      int idx3 = input.indexOf(',', idx2 + 1);

      if (idx1 > 0 && idx2 > idx1 && idx3 > idx2) {
        ultrasonicThreshold = input.substring(0, idx1).toInt();
        defaultR = input.substring(idx1 + 1, idx2).toInt();
        defaultG = input.substring(idx2 + 1, idx3).toInt();
        defaultB = input.substring(idx3 + 1).toInt();

        Serial.print("[설정 적용] 초음파:");
        Serial.print(ultrasonicThreshold);
        Serial.print(" RGB:");
        Serial.print(defaultR);
        Serial.print(",");
        Serial.print(defaultG);
        Serial.print(",");
        Serial.println(defaultB);
      }
    }

    // ✅ LOCAL fallback
    if (input == "LOCAL") {
      currentMode = "MANUAL";
    }

    // ✅ 모드 전환 처리
    if (input == "APP" || input == "MANUAL" || input == "AUTO") {
      currentMode = input;
      Serial.print("모드 변경됨: ");
      Serial.println(currentMode);
    }

    // ✅ APP 모드에서 RGB 값 수신
    else if (currentMode == "APP") {
      int r, g, b;
      int idx1 = input.indexOf(',');
      int idx2 = input.indexOf(',', idx1 + 1);

      if (idx1 > 0 && idx2 > idx1) {
        r = input.substring(0, idx1).toInt();
        g = input.substring(idx1 + 1, idx2).toInt();
        b = input.substring(idx2 + 1).toInt();

        analogWrite(LED_R, r);
        analogWrite(LED_G, g);
        analogWrite(LED_B, b);

        currentR = r;
        currentG = g;
        currentB = b;
      }
    }
  }

  // 2. MANUAL 모드: 가변저항 → RGB (설정 기본색 적용)
  if (currentMode == "MANUAL") {
    int pot = analogRead(POTENTIOMETER);
    int brightness = map(pot, 0, 1023, 0, 255);

    analogWrite(LED_R, brightness * defaultR / 255);
    analogWrite(LED_G, brightness * defaultG / 255);
    analogWrite(LED_B, brightness * defaultB / 255);

    currentR = brightness * defaultR / 255;
    currentG = brightness * defaultG / 255;
    currentB = brightness * defaultB / 255;

    delay(100);
  }

  // 3. AUTO 모드: 조도센서 → RGB (설정 기본색 적용)
  if (currentMode == "AUTO") {
    int lightLevel = analogRead(PHOTORESISTOR);
    int brightness = 0;

    if (lightLevel >= 300) {
      brightness = 255;
    } else if (lightLevel <= 150) {
      brightness = 0;
    } else {
      brightness = (lightLevel - 150) * 255 / 150;
    }

    analogWrite(LED_R, brightness * defaultR / 255);
    analogWrite(LED_G, brightness * defaultG / 255);
    analogWrite(LED_B, brightness * defaultB / 255);

    currentR = brightness * defaultR / 255;
    currentG = brightness * defaultG / 255;
    currentB = brightness * defaultB / 255;

    delay(100);
  }
}
