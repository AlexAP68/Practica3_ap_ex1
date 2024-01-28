import org.json.JSONObject;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Cliente1 {
    public static BufferedReader getFlujo(InputStream is) {
        return new BufferedReader(new InputStreamReader(is));
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        boolean menu = false;

        while (!menu) {
            try (Socket socket = new Socket("localhost", 2000)) {
                PrintWriter pw = new PrintWriter(socket.getOutputStream());
                BufferedReader bfr = getFlujo(socket.getInputStream());

                String opcion = obtenerOpcionValida(sc);

                JSONObject json = new JSONObject();

                switch (opcion) {
                    //insert coge los datos y los envia al servidor
                    case "1":
                        System.out.println("Escribe una ID");
                        String ID = sc.nextLine();
                        System.out.println("Escribe el Nombre");
                        String nombre = sc.nextLine();
                        System.out.println("Escribe el/los apellidos");
                        String apellido = sc.nextLine();

                        json.put("command", "insert");
                        json.put("ID", ID);
                        json.put("nombre", nombre);
                        json.put("apellido", apellido);
                        break;

                        //Select
                    case "2":
                        System.out.println("Escribe el ID a seleccionar:");
                        String selectId = sc.nextLine();

                        json.put("command", "select");
                        json.put("ID", selectId);
                        break;

                        //elimina los datos que has proporcionado
                    case "3":
                        System.out.println("Escribe el ID a eliminar:");
                        String deleteId = sc.nextLine();

                        json.put("command", "delete");
                        json.put("ID", deleteId);
                        break;

                    case "4":
                        System.out.println("Saliendo del programa...");
                        menu = true;
                        continue;

                    default:
                        // Esta línea nunca debería alcanzarse debido a obtenerOpcionValida
                        continue;
                }

                sendJson(pw, json);
                //devuelve el resultado del servidor
                System.out.println("El resultado fue: " + bfr.readLine());

            } catch (IOException e) {
                System.out.println("Error Client: " + e.getMessage());
            }
        }
        System.exit(0);
    }

    //envia la informacion al servidor
    private static void sendJson(PrintWriter pw, JSONObject json) {
        pw.print(json.toString() + "\n");
        pw.flush();
    }

    //El menu para elegir las diferentes opciones
    private static String obtenerOpcionValida(Scanner sc) {
        String opcion;
        do {
            System.out.println("Selecciona una opción:");
            System.out.println("1. Insertar");
            System.out.println("2. Seleccionar");
            System.out.println("3. Eliminar");
            System.out.println("4. Salir");
            opcion = sc.nextLine();

            if (!opcion.equals("1") && !opcion.equals("2") && !opcion.equals("3") && !opcion.equals("4")) {
                System.out.println("Opción no válida. Intenta de nuevo.");
            }
        } while (!opcion.equals("1") && !opcion.equals("2") && !opcion.equals("3") && !opcion.equals("4"));

        return opcion;
    }
}
