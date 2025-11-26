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
import java.util.regex.Pattern;

public class Cliente {
	static Scanner sc = new Scanner(System.in);
	
	// Expresión regular para el formato general de una dirección IPv4
	private static final String IPv4_PATTERN =
            "^([0-9]{1,3}\\.){3}[0-9]{1,3}$";
	
	// Compilamos la expresión regular en un patrón
    private static final Pattern pattern = Pattern.compile(IPv4_PATTERN);
    
    
	public static void main(String[] args) {
		//Argumentos iniciales
		String servidor = ConectarCliente(); // Dirección IP del servidor a conectarse.
		int puerto = conectarPuertoServidor(); //Puerto del servidor a conectarse
		String nombre_C = obtenerNombrePersona(); // Nombre de la persona.

		try(DatagramSocket clientSocket = new DatagramSocket();
			InputStreamReader isr = new InputStreamReader(System.in, "UTF-8");
			BufferedReader br = new BufferedReader(isr)) {

			byte[] solicitudEnviar = nombre_C.getBytes("UTF-8"); // Convierte el nombre del cliente a un array de bytes utilizando la codificación UTF-8 para enviarlo a través de la red.
			InetAddress IPServidor = InetAddress.getByName(servidor); // Resuelve la dirección IP del servidor a partir del nombre de host o dirección IP proporcionada en el argumento.
			DatagramPacket paqueteEnviado = new DatagramPacket(solicitudEnviar, solicitudEnviar.length, IPServidor, puerto); // Crea un DatagramPacket que contiene el nombre del cliente en bytes, la longitud de los datos, la dirección IP del servidor y el puerto del servidor.
			
			clientSocket.setSoTimeout(20000); //Tiempo de espera máximo que el cliente tiene para intentar conectarse al servidor.
			System.out.println("Uniéndose al chat, espera un momento...");
			
			clientSocket.connect(IPServidor, puerto); // Conectar el socket al servidor.
			clientSocket.send(paqueteEnviado); // Envía el paquete de solicitud de conexión al servidor, conteniendo el nombre del cliente.

			// Recibir la respuesta del servidor (su nombre)
			byte[] respuesta = new byte[1400]; // Crea un array de bytes para almacenar la respuesta del servidor. Se usa un tamaño máximo esperado para los datos
			DatagramPacket paqueteRecibido = new DatagramPacket(respuesta, respuesta.length); // Crea un DatagramPacket para recibir la respuesta del servidor. Vincula el buffer 'respuesta' para almacenar los datos recibidos.
			clientSocket.receive(paqueteRecibido); // Espera y recibe un paquete de datos UDP desde el servidor. La ejecución se bloquea hasta que se recibe un paquete.

			// Extraer el nombre del servidor del paquete recibido
			String nombre_S = new String(paqueteRecibido.getData(), 0, paqueteRecibido.getLength(), "UTF-8"); // Convierte los bytes recibidos del servidor a un String, usando UTF-8. Extrae el nombre del servidor.
			System.out.println("\n\n" + nombre_S + " se ha unido al chat");

			String mensaje; // Variable para almacenar los mensajes que el cliente enviará.
			System.out.print(nombre_C + "> ");

			// Bucle principal para la comunicación
			while((mensaje = br.readLine()) != null && mensaje.length() > 0) {
				if(mensaje.equals(".")) { // Salir si el usuario ingresa ".". Comprueba si el mensaje ingresado por el cliente es un punto.
					//Enviar el punto para que el servidor también se desconecte al enviar un mensaje
					byte[] bytes = mensaje.getBytes("UTF-8");
					paqueteEnviado = new DatagramPacket(bytes, bytes.length, IPServidor, puerto);
					clientSocket.send(paqueteEnviado); // Envía el paquete con el mensaje "." al servidor.
					
					System.out.println("\t- Desconectado");
					clientSocket.disconnect();
					System.exit(0);
				} else { // Si el mensaje no es ".", se trata como un mensaje normal para enviar al servidor.
					byte[] bytes = mensaje.getBytes("UTF-8"); 
					paqueteEnviado = new DatagramPacket(bytes, bytes.length, IPServidor, puerto);
					clientSocket.send(paqueteEnviado);
					System.out.println("\tMensaje enviado a " + nombre_S + ", esperando respuesta del mensaje anterior."); 

					// Recibir la respuesta del servidor
					respuesta = new byte[1400];
					paqueteRecibido = new DatagramPacket(respuesta, respuesta.length);
					clientSocket.receive(paqueteRecibido); // Espera y recibe el paquete de respuesta del servidor.

					// Extraer el mensaje del servidor
					String mensaje_recibido = new String(paqueteRecibido.getData(), 0, paqueteRecibido.getLength(), "UTF-8"); 
					if(!mensaje_recibido.equalsIgnoreCase(".")) {
						System.out.println(nombre_S + ": " + mensaje_recibido);
						System.out.print(nombre_C + "> "); // Vuelve a mostrar el prompt del cliente para que el usuario pueda ingresar el siguiente mensaje.
					} else {
						System.out.println("\tLa otra persona se ha desconectado del chat, desconectando.");

						clientSocket.disconnect();
						System.exit(0);
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
			System.out.println("\t- Tiempo de espera agotado: Puede que el chat esté completo (lleno al máximo) o que la otra persona no se haya conectado aún.");
		} catch(IOException e) {
			System.out.println("===============================================");
			System.out.println("\t- Excepción de E/S: " + e.getMessage());
		}
	}
	private static String ConectarCliente() {
		//Método que solicita la IP del servidor a conectarse, se validará que la IP sea correcta.
		
		String IP_S = "";
		boolean IP_correcta = false;
		
		while(IP_S.isBlank() || !IP_correcta) {
			System.out.print("IP del servidor a conectarse (de tipo IPv4 o simplemente escribe 'localhost'): ");
			IP_S = sc.next();
			
			IP_correcta = isValidIPv4(IP_S);
			if(IP_correcta) break;
			else {
				//Si la IP introducida es "localhost" se tomará como correcta.
				if(IP_S.equalsIgnoreCase("localhost")) {
					break;
				} else {
					System.out.println("\t==============");
					System.out.println("\t- IP inválida\n");
				}
			}
		}
		return IP_S;
	}
	
    public static boolean isValidIPv4(String ip) {
    	// Método que valida si una dirección IP es válida
    	
    	
        if(ip == null) {
            return false; // La dirección IP no puede ser nula
        }

        if(!pattern.matcher(ip).matches()) {
            return false; // La dirección IP no coincide con el patrón general
        }

        // Dividimos la dirección IP en sus octetos
        String[] parts = ip.split("\\.");
        for(String part : parts) {
            int value;
            try {
                value = Integer.parseInt(part); // Convertimos cada octeto a un entero
            } catch(NumberFormatException e) {
                return false; // No se pudo convertir el octeto a entero
            }

            if(value < 0 || value > 255) {
                return false; // El valor del octeto debe estar entre 0 y 255
            }
        }
        return true; // La dirección IP es válida
    }
	
    private static int conectarPuertoServidor() {
		/*Método que permite al usuario configurar el puerto del servidor.
		  Para asegurarnos de que se escribe correctamente el puerto este debe ser de 4 dígitos.
		*/
		int puerto = 0;
		boolean noNumerico = false;
		while(puerto < 1000 || puerto > 9999) {
			System.out.print("Especifica el puerto del servidor a conectarse: ");
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
		System.out.println();
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