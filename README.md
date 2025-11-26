# ChatUDP
Programa de consola en Java que consiste en un chat entre un módulo servidor y un módulo cliente que se envían mensajes en forma de datagramas UDP.  
  
- Más información en el manual de usuario (memoria del proyecto) en PDF [aquí](https://drive.google.com/file/d/1l0hXYFflOYH_4WYi65i2iH4iPUarCI_F/view?usp=sharing).

## Tecnologías utilizadas
Lenguaje de Programación: **_Java_**  
Entorno de Desarrollo: **_Eclipse IDE_**  
Protocolo de Comunicaciones: **_UDP_**  

## Requerimientos para su ejecución
JVM (17 o 21 recomendados como versiones a instalar) instalado en el PC de cada una de las 2 personas a comunicarse para poder ejecutar los ficheros JAR en el fichero ZIP del lanzamiento publicado.

## Instrucciones de instalación y de uso
1. Descargar el ZIP que contiene los ficheros JAR publicado en el lanzamiento del proyecto [aquí](https://github.com/SergioR29/ChatUDP/releases).
2. Abrir una ventana de Terminal (CMD si es Windows) para ejecutar cada fichero JAR por separado ya que son el módulo Servidor y el módulo Cliente, es decir, 2 personas diferentes son las que se van a comunicar.
3. Para ejecutar un fichero JAR:  
  
    - 1º Persona (Servidor): java -jar Persona1.jar
    - 2º Persona (Cliente): java -jar Persona2.jar

4. Primero debe conectarse el módulo servidor (Persona 1) para que la otra persona se pueda conectar después y así que el programa pueda tener utilidad. Si se hace al revés la persona cliente no va a poder conectarse porque no habrá ningún servidor cerca con un puerto configurado para la conexión.

5. Para salir del programa y desconectarse de la conexión UDP, independientemente de que uno sea la persona servidor o cliente tiene que introducir el carácter '.' y darle a la tecla enter.

## Ejemplo de uso (módulo servidor == persona 1)
<img width="674" height="362" alt="Captura de pantalla 2025-11-26 132222" src="https://github.com/user-attachments/assets/13ed8163-8f3a-46e2-9bfc-036b0c1c3593" />

## Ejemplo de uso (módulo cliente == persona 2)
<img width="673" height="283" alt="Captura de pantalla 2025-11-26 132231" src="https://github.com/user-attachments/assets/a9774b27-2185-4b9d-b3ae-2d90c3996068" />    

- También el cliente puede introducir la IP de su equipo (para verla en el CMD puede introducir el comando ipconfig) y si no sabe "localhost".
