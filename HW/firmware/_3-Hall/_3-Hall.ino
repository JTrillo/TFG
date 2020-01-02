#include <LiquidCrystal.h>
#define RS 12
#define E 11
#define D4 2
#define D5 3
#define D6 4
#define D7 5
#define S_HALL 8
unsigned curr_ms;
LiquidCrystal lcd(RS,E,D4,D5,D6,D7);
struct Sensor {
  byte pin;
  unsigned long periodo_ms;
  unsigned long last_ms;
  int cont;
  int revs;
  byte estado;
  byte anterior;
};
void setup_sensor(struct Sensor * s, byte pin, unsigned long per, unsigned long ms){
  pinMode(pin,INPUT);
  s->pin=pin;
  s->periodo_ms=per;
  s->last_ms=ms;
  s->cont=0;
  s->revs=0;
  s->estado=digitalRead(pin);
  s->anterior=true;
}
void loop_sensor(struct Sensor * s, unsigned long ms){
  s->anterior=s->estado;
  s->estado=digitalRead(s->pin);
  if(s->anterior && !s->estado){
    s->cont = s->cont + 1;
  }
  if(ms-s->last_ms>=s->periodo_ms){
    s->revs+=s->cont;
    s->cont=0;
    s->last_ms=ms;
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
void loop_monitor(struct Monitor * m, const struct Sensor * hall, unsigned long ms){
  if(ms-m->last_ms>=m->periodo_ms){
    m->last_ms=ms;
    lcd.clear();
    lcd.print("Lecturas campos");
    lcd.setCursor(0,1);
    lcd.print("magneticos: ");
    lcd.print(hall->revs);
  }
}

struct Sensor hall;
struct Monitor mnt;

void setup() {
  lcd.begin(16,2);
  lcd.print("Pruebas LCD:");
  lcd.setCursor(0,1);
  lcd.print("Prueba 3");
  delay(3500);
  curr_ms=millis();
  setup_sensor(&hall, S_HALL, 3000, curr_ms);
  setup_monitor(&mnt, 5000, curr_ms);
}

void loop() {
  curr_ms=millis();
  loop_sensor(&hall, curr_ms);
  loop_monitor(&mnt, &hall, curr_ms);
}
