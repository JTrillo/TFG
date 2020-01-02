#include <LedControl.h>
#define NumMatrix 1       // Cuantas matrices vamos a usar
#define NOLISTO 0
LedControl lc=LedControl(11,9,10, NumMatrix);  // Creamos una instancia de LedControl
//12,11,10

byte no_listo[]{
  B10000001,
  B01000010,
  B00100100,
  B00011000,
  B00011000,
  B00100100,
  B01000010,
  B10000001
};
byte listo[]{
  B01101000,
  B10011000,
  B10011001,
  B10011010,
  B10011100,
  B10011100,
  B10011010,
  B01101001
};
byte uno[]{
  B00001000,
  B00011000,
  B00001000,
  B00001000,
  B00001000,
  B00001000,
  B00001000,
  B00011100
};
byte dos[]{
  B00111100,
  B01000010,
  B00000010,
  B00000100,
  B00001000,
  B00010000,
  B00100000,
  B01111110
};
byte tres[]{
  B01111110,
  B00000100,
  B00001000,
  B00000100,
  B00000010,
  B00000010,
  B01000010,
  B00111100
};
byte cuatro[]{
  B00000100,
  B00001100,
  B00010100,
  B00100100,
  B01111110,
  B00000100,
  B00000100,
  B00000100
};
byte cinco[]{
  B01111110,
  B01000000,
  B01111100,
  B00000010,
  B00000010,
  B00000010,
  B01000010,
  B00111100
};
void setup(){
  lc.shutdown(0,false);    // Activo la matriz
  lc.setIntensity(0,8);    // Poner el brillo a un valor intermedio
  lc.clearDisplay(0);      // Y borrar todo
}

void loop(){
  //no_listo_f();
  //delay(3500);
  //listo_f();
  //delay(3500);
  cuenta_atras();
}

void no_listo_f(){
  for(int i=0; i<8; i++){
    lc.setRow(0,i,no_listo[i]);
  }
}

void listo_f(){
  for(int i=0; i<8; i++){
    lc.setRow(0,i,listo[i]);
  }
}
void cuenta_atras(){
  int cont=1;
  while(cont<=5){
    switch(cont){
      case 1:
      for(int i=0; i<8; i++){
        lc.setRow(0,i,cinco[i]);
      }
      break;
      case 2:
      for(int i=0; i<8; i++){
        lc.setRow(0,i,cuatro[i]);
      }
      break;
      case 3:
      for(int i=0; i<8; i++){
        lc.setRow(0,i,tres[i]);
      }
      break;
      case 4:
      for(int i=0; i<8; i++){
        lc.setRow(0,i,dos[i]);
      }
      break;
      case 5:
      for(int i=0; i<8; i++){
        lc.setRow(0,i,uno[i]);
      }
      break;
    }
    cont++;
    delay(1000);
  }
}

