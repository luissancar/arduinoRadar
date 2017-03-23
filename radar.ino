#include <Servo.h>
#define trigPin 6
#define echoPin 7
#define BTPWR 12
#define servo 5

Servo myservo;


   

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);
  pinMode(BTPWR,OUTPUT);
  digitalWrite(BTPWR,HIGH); //encendemos el CH-05
  delay(2000);
  myservo.attach(10);

}

void loop() {
  int movimiento=5;   //de 0 a 180 grados
  boolean direccion=true;  // sumamos a movimiento
  while (true)
  {
    myservo.write(movimiento);
    Serial.print('#');
    Serial.print(distan());
     Serial.print('+');
    Serial.print(movimiento);
     Serial.print('-');
     Serial.print(distan());
     Serial.print('+');
    Serial.print(movimiento);
    Serial.print('~'); //used as an end of transmission character - used in app for string length
    Serial.println();
    delay(100);
    if (direccion)
       movimiento++;
    else
       movimiento--;
    if (movimiento==100)
        direccion=false;  // restamos a movimiento    
    if (movimiento==10)
        direccion=true;   // sumamos a movimiento   
   }
  
}
long distan() {
  long duracion, distancia ;
  digitalWrite(trigPin, LOW);        // Nos aseguramos de que el trigger está desactivado
  delayMicroseconds(2);              // Para asegurarnos de que el trigger esta LOW
  digitalWrite(trigPin, HIGH);       // Activamos el pulso de salida
  delayMicroseconds(10);             // Esperamos 10µs. El pulso sigue active este tiempo
  digitalWrite(trigPin, LOW);        // Cortamos el pulso y a esperar el echo
  duracion = pulseIn(echoPin, HIGH) ;
  distancia = duracion / 2 / 29.1  ;
          //  Serial.println(String(distancia) + " cm.") ;
  return distancia;
}          
