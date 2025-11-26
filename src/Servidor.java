import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Servidor {
	private final static int MAX_BYTES = 1400; // Tamaño máximo de los datos en bytes para los paquetes UDP. Se utiliza para evitar fragmentación y asegurar compatibilidad.
	private final static String COD_TEXTO = "UTF-8";
	private static boolean inicio = true; // Bandera estática para controlar si es la primera vez que se muestra el prompt del servidor. Inicialmente es verdadero.
	
	static Scanner sc = new Scanner(System.in);
	public static void main(String[] args) {
		//Argumentos iniciales
		int puerto = configurarPuertoServidor(); //Variable que obtiene el puerto especificado.
		String nombre = obtenerNombrePersona(); // Variable para almacenar el nombre de la persona.

		try(DatagramSocket serverSocket = new DatagramSocket(puerto); // Crea un DatagramSocket que se vinculará al puerto especificado para escuchar las solicitudes.
			InputStreamReader isr = new InputStreamReader(System.in, COD_TEXTO); // Crea un InputStreamReader para leer la entrada estándar (teclado) utilizando la codificación UTF-8.
			BufferedReader br = new BufferedReader(isr)) { // Crea un BufferedReader para leer texto de forma eficiente desde el InputStreamReader.
			
			System.out.println("Esperando que se conecte la otra persona al chat");

			// Recibir el primer paquete del cliente, que se espera que sea una solicitud de conexión con el nombre del cliente.
			byte[] datosRecibidos = new byte[MAX_BYTES]; // Crea un array de bytes para almacenar los datos recibidos, con el tamaño máximo definido.
			DatagramPacket paqueteRecibido = new DatagramPacket(datosRecibidos, datosRecibidos.length); // Crea un DatagramPacket para recibir datos. Se asocia con el array de bytes.
			serverSocket.receive(paqueteRecibido); // Espera y recibe un paquete de datos UDP. La ejecución se detiene aquí hasta que se recibe un paquete.

			// Extraer el nombre del cliente del paquete recibido. Se asume que el primer paquete contiene el nombre del cliente.
			String nombre_C = new String(paqueteRecibido.getData(), 0, paqueteRecibido.getLength(), COD_TEXTO);

			// Obtener la dirección IP y el puerto del cliente que envió el paquete.
			InetAddress IPCliente = paqueteRecibido.getAddress();
			int puertoCliente = paqueteRecibido.getPort();
			System.out.println("\n" + nombre_C + " se ha unido al chat");

			// Enviar el nombre del servidor al cliente como respuesta a su solicitud de conexión.
			String respuesta = nombre;
			byte[] b = respuesta.getBytes(COD_TEXTO);
			DatagramPacket paqueteEnviado = new DatagramPacket(b, b.length, IPCliente, puertoCliente); // Crea un DatagramPacket para enviar la respuesta.

			// Se utilizan los bytes de respuesta, su longitud, la IP y el puerto del cliente para construir el paquete.
			serverSocket.connect(IPCliente, puertoCliente); // Conecta el socket del servidor al cliente. Aunque UDP no es orientado a conexión, connect aquí guarda la IP y puerto del cliente por defecto
			serverSocket.send(paqueteEnviado);

			String mensaje_Enviado; // Variable para almacenar el mensaje que envíe el servidor.
			while(true) {
				if(inicio) System.out.print(nombre + "> "); // Mostrar el prompt del servidor, solo la primera vez que se entra en este bucle.
				inicio = false; // Desactiva la bandera para que el prompt no se vuelva a mostrar hasta que se reinicie el programa.

				// Leer el mensaje del usuario (servidor) desde la consola.
				while((mensaje_Enviado = br.readLine()) != null && mensaje_Enviado.length() > 0) {
					if(mensaje_Enviado.equals(".")) {
						// Si es ".", se interpreta como una señal para terminar la conversación y desconectar.
						// Enviar el punto al cliente para que también sepa que debe desconectarse.
						byte[] bytes = mensaje_Enviado.getBytes(COD_TEXTO);
						paqueteEnviado = new DatagramPacket(bytes, bytes.length, IPCliente, puertoCliente);
						serverSocket.send(paqueteEnviado);
						
						System.out.println("\t- Desconectado");
						serverSocket.disconnect();
						System.exit(0);
					} else { // Si el mensaje no es ".", se trata como un mensaje normal para enviar al cliente.
						// Enviar el mensaje del servidor al cliente.
						byte[] bytes = mensaje_Enviado.getBytes(COD_TEXTO);
						paqueteEnviado = new DatagramPacket(bytes, bytes.length, IPCliente, puertoCliente);
						serverSocket.send(paqueteEnviado);
						System.out.println("\tMensaje enviado a " + nombre_C + ", esperando respuesta del mensaje anterior.");

						// Recibir la respuesta del cliente al mensaje enviado por el servidor.
						datosRecibidos = new byte[MAX_BYTES];
						paqueteRecibido = new DatagramPacket(datosRecibidos, datosRecibidos.length);
						serverSocket.receive(paqueteRecibido);
						
						// Extraer el mensaje de la respuesta del cliente.
						String mensaje = new String(paqueteRecibido.getData(), 0, paqueteRecibido.getLength(), COD_TEXTO);
						if(mensaje.equals(".")) {
							System.out.println("\tLa otra persona se ha desconectado del chat, desconectando.");
							
							serverSocket.disconnect();
							System.exit(0);
						} else {
							System.out.println(nombre_C + ": " + mensaje); 
							System.out.print(nombre + "> "); // Vuelve a mostrar el prompt del servidor para que el usuario pueda ingresar el siguiente mensaje.
						}
					}
				}
			}
		} catch(SocketException ex) {
			System.out.println("===============================================");
			if(ex.getClass().equals(PortUnreachableException.class)) {
				System.out.println("\t- Puerto inalcanzable, puede que la otra persona no se haya conectado o que ese puerto no exista.");
			}
			if(ex.getClass().equals(BindException.class)) {
				System.out.println("\t- El puerto ya lo está usando otra persona.");
			}
		} catch(SocketTimeoutException e) {
			System.out.println("===============================================");
			System.out.println("\t- Tiempo de espera agotado: Puede que el chat esté completo (lleno al máximo).");
		} catch(IOException e) {
			System.out.println("===============================================");
			System.out.println("\t- Excepción de E/S: " + e.getMessage());
		}

	}
	private static int configurarPuertoServidor() {
		/*Método que permite al usuario configurar el puerto del servidor.
		  Para asegurarnos de que se escribe correctamente el puerto este debe ser de 4 dígitos.
		*/
		int puerto = 0;
		boolean noNumerico = false;
		while(puerto < 1000 || puerto > 9999) {
			System.out.print("Especifica el puerto del chat: ");
			try {
				puerto = sc.nextInt();
			} catch(InputMismatchException e) {
				noNumerico = true; //Switch para controlar que no se muestre el mensaje siguiente 2 veces.
				
				System.out.println("=====================================================");
				System.out.println("\t- El puerto tiene que ser de 4 dígitos numéricos.\n\n");
				sc.nextLine();
			}
			
			if(!noNumerico) {
				if((puerto < 1000 || puerto > 9999)) {
					System.out.println("=====================================================");
					System.out.println("\t- El puerto tiene que ser de 4 dígitos numéricos.\n\n");
				}
			} else {//Si el puerto introducido no es numérico se apaga el switch.
				noNumerico = false;
			}
		}
		System.out.println("Puerto del chat configurado correctamente.\n");
		return puerto;
	}
	private static String obtenerNombrePersona() {
		//Método que solicita a la persona su nombre, controlando que no esté vacío ni sean sólo espacios.
		
		String nombre_P = "";
		while(nombre_P.isBlank()) {
			System.out.print("¿Cómo te llamas? (sin espacios): ");
			nombre_P = sc.next();
		}
		return nombre_P;
	}

}