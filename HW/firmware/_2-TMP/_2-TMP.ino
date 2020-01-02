#include <LiquidCrystal.h>
#define RS 12
#define E 11
#define D4 2
#define D5 3
#define D6 4
#define D7 5
#define S_TEMP A0
unsigned curr_ms;
LiquidCrystal lcd(RS,E,D4,D5,D6,D7);
struct Sensor {
  byte pin;
  unsigned long periodo_ms;
  unsigned long last_ms;
  float valor;
  int cont;
  float suma;
};
void setup_sensor(struct Sensor * s, byte pin, unsigned long per, unsigned long ms){
  s->pin=pin;
  s->periodo_ms=per;
  s->last_ms=ms;
  s->cont=0;
  s->suma=0.0;
}
void loop_sensor(struct Sensor * s, unsigned long ms){
  int lectura = analogRead(s->pin);
  float volt = (lectura / 1024.0) * 5.0;
  s->suma = s->suma + ((volt - 0.5) * 100.0);
  s->cont = s->cont + 1;
  if(ms-s->last_ms>=s->periodo_ms){
    s->valor = s->suma / (float)s->cont;
    s->cont=0;
    s->suma=0.0;
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
void loop_monitor(struct Monitor * m, const struct Sensor * temp, unsigned long ms){
  if(ms-m->last_ms>=m->periodo_ms){
    m->last_ms=ms;
    lcd.clear();
    lcd.print("Temp. actual:");
    lcd.setCursor(0,1);
    lcd.print(temp->valor);
    lcd.print(" C");
  }
}

struct Sensor temp;
struct Monitor mnt;

void setup() {
  lcd.begin(16,2);
  lcd.print("Pruebas LCD:");
  lcd.setCursor(0,1);
  lcd.print("Prueba 2");
  delay(3500);
  curr_ms=millis();
  setup_sensor(&temp, S_TEMP, 3000, curr_ms);
  setup_monitor(&mnt, 5000, curr_ms);
}

void loop() {
  curr_ms=millis();
  loop_sensor(&temp, curr_ms);
  loop_monitor(&mnt, &temp, curr_ms);
}
