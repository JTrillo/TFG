#include <LiquidCrystal.h>
#define RS 12
#define E  11
#define D4 2
#define D5 3
#define D6 4
#define D7 5
#define S_ULTR_ECO  8
#define S_ULTR_TRIG 9
unsigned curr_ms;
LiquidCrystal lcd(RS,E,D4,D5,D6,D7);
struct Sensor {
  byte pin_eco;
  byte pin_trig;
  unsigned long periodo_ms;
  unsigned long last_ms;
  int distancia;
};
void setup_sensor(struct Sensor * s, byte pin_eco, byte pin_trig, unsigned long per, unsigned long ms){
  pinMode(pin_eco,INPUT);
  pinMode(pin_trig,OUTPUT);
  s->pin_eco=pin_eco;
  s->pin_trig=pin_trig;
  s->periodo_ms=per;
  s->last_ms=ms;
}
void loop_sensor(struct Sensor * s, unsigned long ms){
  if(ms-s->last_ms>=s->periodo_ms){
    s->distancia=ping(s->pin_trig,s->pin_eco);
  }
}
struct Monitor{
  unsigned long periodo_ms;
  unsigned long last_ms;
};
void setup_monitor(struct Monitor * m, unsigned long per, unsigned long ms){
  m->periodo_ms=per;
  m->last_ms=ms;
}
void loop_monitor(struct Monitor * m, const struct Sensor * ultra, unsigned long ms){
  if(ms-m->last_ms>=m->periodo_ms){
    m->last_ms=ms;
    lcd.clear();
    lcd.print("Distancia: ");
    lcd.print(ultra->distancia);
  }
}

struct Sensor ultra;
struct Monitor mnt;

void setup() {
  lcd.begin(16,2);
  lcd.print("Pruebas LCD:");
  lcd.setCursor(0,1);
  lcd.print("Prueba 4");
  delay(3500);
  curr_ms=millis();
  setup_sensor(&ultra, S_ULTR_ECO, S_ULTR_TRIG, 3000, curr_ms);
  setup_monitor(&mnt, 5000, curr_ms);
}

void loop() {
  curr_ms=millis();
  loop_sensor(&ultra, curr_ms);
  loop_monitor(&mnt, &ultra, curr_ms);
}

int ping(int TriggerPin, int EchoPin) {
 long duration, distanceCm;
 
 digitalWrite(TriggerPin, LOW);  //para generar un pulso limpio ponemos a LOW 4us
 delayMicroseconds(4);
 digitalWrite(TriggerPin, HIGH);  //generamos Trigger (disparo) de 10us
 delayMicroseconds(10);
 digitalWrite(TriggerPin, LOW);
 
 duration = pulseIn(EchoPin, HIGH);  //medimos el tiempo entre pulsos, en microsegundos
 
 distanceCm = duration * 10 / 292/ 2;   //convertimos a distancia, en cm
 return distanceCm;
}
