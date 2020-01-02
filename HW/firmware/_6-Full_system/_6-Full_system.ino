#include <LiquidCrystal.h>  // Librería de la pantalla LCD
#include <LedControl.h>     // Librería de la matriz LED
//LCD
#define RS 6 // pines de la pantalla
#define E  7 // pines de la pantalla
#define D4 2 // pines de la pantalla
#define D5 3 // pines de la pantalla
#define D6 4 // pines de la pantalla
#define D7 5 // pines de la pantalla
//MATRIZ LED
#define NumMatrix 1 // Cuantas matrices LED se van a usar
#define DIN 11      // pines de la matriz
#define CS 10      // pines de la matriz
#define CLK  9      // pines de la matriz
//PINES DE LOS SENSORES
#define S_TEMP A0 // pin sensor temperatura
#define S_HALL 8  // pin sensor efecto hall
#define ULTRA_TRIG 12 // pin trigger ultrasonido
#define ULTRA_ECHO 13 // pin echo    ultrasonido
//ESTADOS DEL SISTEMA
#define ESTADO_INICIAL 1
#define ESTADO_PREPARADO 2
#define ESTADO_RUTA 3
#define ESTADO_FINAL 4
#define ESTADO_ESPERA 5
#define ESTADO_ENVIO 6

unsigned long curr_ms, start_time, end_time;
unsigned long aux, horas, minutos, segundos;
float long_rueda; // longitud de la rueda de la bici en centímetros
boolean inicial; // para tomar valor inicial de la temperatura
String bufferBT;
//DATOS A ENVIAR AL SMARTPHONE
unsigned long duracion;
float metros, kilometros, vmedia;

//DIBUJOS DE LA MATRIZ LED
byte ok[]{
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
byte prohibido[]{
  B01111110,
  B10000011,
  B10000101,
  B10001001,
  B10010001,
  B10100001,
  B11000001,
  B01111110
};
byte flecha[]{
  B00001000,
  B00011100,
  B00111110,
  B00011100,
  B00011100,
  B00011100,
  B00011100,
  B00011100,
};
byte blue[]{
  B00001000,
  B00001100,
  B00101010,
  B00011100,
  B00011100,
  B00101010,
  B00001100,
  B00001000,
};
LiquidCrystal lcd(RS,E,D4,D5,D6,D7); // declaración de la pantalla
LedControl lc=LedControl(DIN,CLK,CS, NumMatrix);  // declaración de la matriz

// SENSOR TEMPERATURA
struct S_Temperatura{   //Esctructura para el sensor de temperatura
  byte pin;
  unsigned long periodo_ms;
  unsigned long last_ms;
  float valor;
  int cont;
  float suma;
};
void setup_s_temp(struct S_Temperatura * s, byte pin, unsigned long per, int cont, unsigned long ms){
  s->pin=pin;
  s->periodo_ms=per;
  s->last_ms=ms;
  s->cont=cont;  
  s->suma=0.0;
}
void loop_s_temp(struct S_Temperatura * s, unsigned long ms){
  if(ms-s->last_ms>=s->periodo_ms || inicial){
    s->last_ms=ms;
    int lectura;
    float volt;
    for(int i=0; i<s->cont; i++){     //Se realizan 'cont' mediciones por si el sensor toma algún valor erróneo
      lectura = analogRead(s->pin);
      volt = (lectura / 1024.0) * 5.0;
      s->suma = s->suma + ((volt - 0.5) * 100.0);
    }
    s->valor = s->suma / (float)s->cont; //El valor que se almacena es la media de esas 'cont' mediciones
    s->suma=0.0;
  }
}

//SENSOR EFECTO HALL
struct S_Hall{  //Esctructura para el sensor de efecto hall
  byte pin;
  unsigned long last_ms;
  int cont;
  int revs;
  float velocidad;
  byte estado;
  byte anterior;
};
void setup_s_hall(struct S_Hall * s, byte pin){
  pinMode(pin,INPUT);
  s->pin=pin;
  s->cont=0;
  s->revs=0;
  s->estado=digitalRead(pin);
  s->anterior=s->estado;
}
void loop_s_hall(struct S_Hall * s, unsigned long ms){
  s->anterior=s->estado;
  s->estado=digitalRead(s->pin);
  if(s->anterior && !s->estado){
    s->cont = s->cont + 1;
    s->revs = s->revs + 1;
    if(s->cont==10){  //velocidad = 10 revoluciones de la rueda / tiempo que se tardan en dar esas 10 vueltas
      float metros = (s->cont*long_rueda)/1000;
      unsigned long segs = (ms-s->last_ms)/1000;
      //Serial.println(metros/segs);
      s->velocidad = (metros/segs) * 3.6; //se guarda la velocidad en kilometros por hora
      s->cont=0;
      s->last_ms=ms;
    }
  }
}

//ULTRASONIDO
struct Ultrasonido { //Esctructura para el ultrasonido
  byte pin_eco;
  byte pin_trig;
  int distancia;
};
void setup_ultra(struct Ultrasonido * s, byte pin_eco, byte pin_trig){
  pinMode(pin_eco,INPUT);
  pinMode(pin_trig,OUTPUT);
  s->pin_eco=pin_eco;
  s->pin_trig=pin_trig;
  s->distancia=20;
}
void loop_ultra(struct Ultrasonido * s){
  s->distancia=f_ping(s->pin_trig,s->pin_eco);
  //Serial.println(s->distancia);
}

//MONITOR
struct Monitor{ //Esctructura para el monitor
  unsigned long periodo_ms;
  unsigned long last_ms;
  int pantalla; //el contenido a mostrar cambia segun el periodo
};
void setup_monitor(struct Monitor * m, unsigned long per, unsigned long ms){
  m->periodo_ms=per;
  m->last_ms=ms;
  m->pantalla=1;
}
void loop_monitor(struct Monitor * m, const struct S_Temperatura * temp, const struct S_Hall * hall, unsigned long ms){
  switch (m->pantalla){
    case 1:
      //Velocidad
      lcd.setCursor(0,0);
      lcd.print("Vel: ");
      lcd.print(hall->velocidad);
      lcd.print(" Km/h");
      //Temperatura
      lcd.setCursor(0,1);
      lcd.print("Temp: "); 
      lcd.print(temp->valor);
      lcd.print(" C");
      break;
    case 2:
      //Distancia
      lcd.setCursor(0,0);
      lcd.print("Dist: ");
      lcd.print((hall->revs*long_rueda)/1000000); //en kilómetros
      lcd.print(" Km");
      //Tiempo
      lcd.setCursor(0,1);
      lcd.print("Tiempo: ");
      aux = (ms-start_time)/1000; //segundos totales de trayecto
      //Serial.println(aux);
      horas = aux/3600;
      //Serial.println(horas);
      if(horas<10){
        lcd.print("0");
      }
      lcd.print(horas);
      lcd.print(":");
      minutos = (aux%3600)/60;
      //Serial.println(minutos);
      if(minutos<10){
        lcd.print("0");
      }
      lcd.print(minutos);
      lcd.print(":");
      segundos = aux%60;
      //Serial.println(segundos);
      if(segundos<10){
        lcd.print("0");
      }
      lcd.print(segundos);
      break;
    case 3:
      lcd.setCursor(0,0);
      lcd.print("Ruta finalizada!");
      lcd.setCursor(0,1);
      lcd.print("Vm: ");
      lcd.print(vmedia);
      lcd.print(" Km/h");
      break;
    case 4:
      lcd.setCursor(0,0);
      lcd.print("Dist: ");
      lcd.print(kilometros); //en kilómetros
      lcd.print(" Km");
      //Tiempo
      lcd.setCursor(0,1);
      lcd.print("Dur: ");
      horas = duracion/3600;
      //Serial.println(horas);
      if(horas<10){
        lcd.print("0");
      }
      lcd.print(horas);
      lcd.print(":");
      minutos = (aux%3600)/60;
      //Serial.println(minutos);
      if(minutos<10){
        lcd.print("0");
      }
      lcd.print(minutos);
      lcd.print(":");
      segundos = aux%60;
      //Serial.println(segundos);
      if(segundos<10){
        lcd.print("0");
      }
      lcd.print(segundos);
      break;
  }
  if(ms-m->last_ms>=m->periodo_ms){
    m->last_ms=ms;
    lcd.clear();
    switch (m->pantalla){
      case 1:
        m->pantalla=2;
        break;
      case 2:
        m->pantalla=1;
        break;
      case 3:
        m->pantalla=4;
        break;
      case 4:
        m->pantalla=3;
        break;
    }
  }
}

//SISTEMA
struct Sistema {
  int estado;
  unsigned long periodo;  //para el parpadepo de la flecha
  unsigned long last_ms;  //para el parpadepo de la flecha
  int cont;               //para el parpadepo de la flecha
};
void setup_sistema(struct Sistema * s){
  s->estado=ESTADO_INICIAL;
  s->periodo=350;
  s->cont=0;
}
void loop_sistema(struct Sistema * s, const LiquidCrystal lcd, const struct Ultrasonido * u, struct S_Hall * hall, struct Monitor * m, unsigned long ms){
  switch(s->estado){
    case ESTADO_INICIAL:
      while(Serial.available()){ //El smartphone ha enviado el nuevo radio de la rueda
        delay(3); //delay para que el buffer se llene
        if(Serial.available() > 0){ //Lectura caracter a caracter
          char c = Serial.read();
          bufferBT += c;
        }
      }
      if(bufferBT.length() > 0){
        long_rueda = bufferBT.toFloat(); //Se actualiza el radio de la bici
        //Mostramos por pantalla el nuevo radio durante 5 segundos
        lcd.clear();
        lcd.print("Longitud rueda");
        lcd.setCursor(0,1);
        lcd.print("recibida: ");
        lcd.print(long_rueda);
        lc.clearDisplay(0);
        delay(5000);
        //Volvemos al mensaje previo a iniciar la ruta
        lcd.clear();
        lcd.print("Acerque la mano");
        lcd.setCursor(0,1);
        lcd.print("al ultrasonido");
        f_ok();
        bufferBT = "";
      }
      if(u->distancia<=2){ //si detecta presencia a 2 o menos cm, pasamos al estado preparado
        s->estado=ESTADO_PREPARADO;
        lcd.clear();
        lcd.print("Preparese para");
        lcd.setCursor(0,1);
        lcd.print("comenzar la ruta");
        f_cuenta_atras();      
      }
      break;
    case ESTADO_PREPARADO:
      start_time=ms;
      hall->last_ms=ms;
      hall->cont=0;
      hall->revs=0;
      hall->velocidad=0.0;
      s->last_ms=ms;
      s->cont=0;
      s->estado=ESTADO_RUTA;
      lcd.clear();
      m->last_ms=ms;
      m->pantalla=1;
      f_flecha();
      break;
    case ESTADO_RUTA:
      //Parpadeo de la flecha
      if(ms-s->last_ms>=s->periodo && s->cont<=10){
        s->cont=s->cont+1;
        if(s->cont%2==0){
          f_flecha();
        }else{
          lc.clearDisplay(0);
        }
        s->last_ms=ms;
      }
      if(u->distancia<=2){ //si detecta presencia a 2 o menos cm, pasamos al estado final
        end_time=ms;
        duracion=(end_time-start_time)/1000;      //Tiempo en segundos
        metros = (hall->revs*long_rueda)/1000;    //Distancia en metros
        kilometros = metros/1000;                 //Distancia en kilómetros
        vmedia = (metros/duracion) * 3.6;         //En kilómetros por hora
        //Serial.println(duracion);
        //Serial.println(metros);
        //Serial.println(vmedia);
        m->last_ms=ms;
        m->pantalla=3;
        s->estado=ESTADO_FINAL;
        f_para();
        lcd.clear();
        delay(2000); //para evitar que salte directamente al estado espera
      }
      break;
    case ESTADO_FINAL:
      if(u->distancia<=2){ //si detecta presencia a 2 o menos cm, pasamos al estado espera
         s->estado=ESTADO_ESPERA;
         s->last_ms=ms;
         delay(2000); //para evitar que salte directamente al estado preparado
      }
      break;
    case ESTADO_ESPERA:
      if(u->distancia<=2){ //si detecta presencia a 2 o menos cm, pasamos al estado inicial
         s->estado=ESTADO_INICIAL;
        lcd.clear();
        lcd.print("Acerque la mano");
        lcd.setCursor(0,1);
        lcd.print("al ultrasonido");
        f_ok();
        delay(2000); //para evitar que salte directamente al estado preparado
      }
      if(ms-s->last_ms>=10000){ //Si en 10 segundos no detecta presencia es que queremos enviar y pasamos al estado envio
        s->estado=ESTADO_ENVIO;
      }
      break;
    case ESTADO_ENVIO:
      f_bluetooth();
      lcd.clear();
      lcd.print("Enviando datos");
      lcd.setCursor(0,1);
      lcd.print("al smartphone");
      envio(duracion, kilometros, vmedia);
      delay(10000);
      s->estado=ESTADO_INICIAL;
      lcd.clear();
      lcd.print("Acerque la mano");
      lcd.setCursor(0,1);
      lcd.print("al ultrasonido");
      f_ok();
      break;
  }
}

struct S_Temperatura temp;
struct S_Hall hall;
struct Ultrasonido ultra;
struct Sistema sis;
struct Monitor mnt;

void setup() {
  Serial.begin(9600);
  long_rueda = 2114;      //Longitud de la rueda de la bicicleta usada (en milímetros)
  lcd.begin(16,2); //Para iniciar la pantalla
  lcd.print("Navegador de a");   //Mensaje inicial
  lcd.setCursor(0,1);            //Mensaje inicial
  lcd.print("bordo para bicis"); //Mensaje inicial
  delay(3500);
  //Inicialización de la matriz LED
  lc.shutdown(0,false);    // Activo la matriz
  lc.setIntensity(0,8);    // Poner el brillo a un valor intermedio
  lc.clearDisplay(0);      // Y borrar todo
  //Inicialización de la matriz LED
  curr_ms=millis();
  setup_s_temp(&temp, S_TEMP, 120000, 50, curr_ms); //Cada 2 minutos tomamos el valor de la temperatura y realizamos 50 mediciones
  setup_s_hall(&hall, S_HALL);                      //Continuamente detectando el posible paso del radio de la bicicleta con el iman
  setup_ultra(&ultra, ULTRA_ECHO, ULTRA_TRIG);      //Continuamente detectando si se acerca la mano del usuario
  setup_monitor(&mnt, 5000, curr_ms);               //Cada 5 segundos, cambiamos los datos a mostrar de la pantalla
  setup_sistema(&sis);
  inicial=true;
  loop_s_temp(&temp, curr_ms);  //Tomamos el valor inicial de la temperatura
  inicial=false;
  lcd.clear(); //Se limpia el mensaje inicial
  lcd.print("Acerque la mano");
  lcd.setCursor(0,1);
  lcd.print("al ultrasonido");
  f_ok();
}

void loop() {
  curr_ms=millis();
  loop_s_temp(&temp, curr_ms);
  loop_s_hall(&hall, curr_ms);
  if(curr_ms>5000){
    loop_ultra(&ultra);
  }
  if(sis.estado==ESTADO_RUTA || sis.estado==ESTADO_FINAL || sis.estado==ESTADO_ESPERA){
    loop_monitor(&mnt, &temp, &hall, curr_ms);
  }
  loop_sistema(&sis, lcd, &ultra, &hall, &mnt, curr_ms);
}

//FUNCIONES
int f_ping(int TriggerPin, int EchoPin) { //calcula distancia gracias al ultrasonido
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
void f_ok(){ //OK
  for(int i=0; i<8; i++){
    lc.setRow(0,i,ok[i]);
  }
}

void f_flecha(){ //flecha parpadeando
  for(int j=0; j<10; j++){
    for(int i=0; i<8; i++){
      lc.setRow(0,i,flecha[i]);
    }
  }
}
void f_para(){ //señal prohibido
  for(int i=0; i<8; i++){
    lc.setRow(0,i,prohibido[i]);
  }
}
void f_bluetooth(){ //señal bluetooth
  for(int i=0; i<8; i++){
    lc.setRow(0,i,blue[i]);
  }
}
void f_cuenta_atras(){ //Cuenta atrás del 5 al 1
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
void envio(unsigned long dur, float dis, float vel){
  Serial.print('#'); //Para que el smartphone sepa que comienza la transmision de datos
  Serial.print(dur);
  Serial.print('+'); //Separador
  Serial.print(dis);
  Serial.print('+'); //Separador
  Serial.print(vel);
  Serial.print('*'); //Final del envio
  Serial.println();
  delay(10);        //agregamos este delay para eliminar tramisiones faltantes
}

