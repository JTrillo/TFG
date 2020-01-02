#include <LiquidCrystal.h>
const int RS=12, E=11, D4=2, D5=3, D6=4, D7=5;
LiquidCrystal lcd(RS,E,D4,D5,D6,D7);
void setup() {
  lcd.begin(16,2);
  lcd.print("Pruebas LCD:");
  lcd.setCursor(0,1);
  lcd.print("Prueba 1");
}

void loop() {

}
