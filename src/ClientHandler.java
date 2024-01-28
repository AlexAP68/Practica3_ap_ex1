import org.json.JSONObject;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private static final String FILE_NAME = "bbdd.txt";

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String jsonStr = in.readLine();
            JSONObject json = new JSONObject(jsonStr);
            String command = json.getString("command");
//segun el comando que le ha dado el cliente hara una cosa u otra
            switch (command) {
                //insertar en la base de datos
                case "insert":
                    String idInsert = json.getString("ID");
                    String nombreInsert = json.getString("nombre");
                    String apellidoInsert = json.getString("apellido");
                    insertData(idInsert, nombreInsert, apellidoInsert, out);
                    break;

                    //selecionar de la base de datos
                case "select":
                    String idSelect = json.getString("ID");
                    String selectedEntry = selectData(idSelect);
                    out.println(selectedEntry != null ? selectedEntry : "No se encontró ninguna entrada con ese ID.");
                    break;

                    //borra de la base de datos
                case "delete":
                    String idDelete = json.getString("ID");
                    deleteData(idDelete, out);
                    break;

                default:
                    out.println("Comando no reconocido.");
                    break;
            }
        } catch (IOException e) {
            System.out.println("Error al manejar la solicitud del cliente: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error al cerrar el socket del cliente: " + e.getMessage());
            }
        }
    }



    //llama para hacer el insert
    private static void insertData(String id, String nombre, String apellido, PrintWriter out) {
        String entry = id + "," + nombre + "," + apellido;
        if (isIdExists(id)) {
            out.println("Ya existe un dato con esa ID en la base de datos.");
        } else {
            saveEntryToFile(entry);
            out.println("Datos insertados correctamente.");
        }
    }

    //llama para hacer el select
    private static String selectData(String entryId) {
        String entry = readEntryFromFile(entryId);
        return (entry != null) ? entry : "No se encontró ninguna entrada con ese ID.";
    }

    //llama para hacer el delete
    private static void deleteData(String entryId, PrintWriter out) {
        if (deleteEntryFromFile(entryId)) {
            out.println("Entrada eliminada correctamente.");
        } else {
            out.println("No se pudo eliminar la entrada o el ID no se encontró.");
        }
    }

    //hace el insert
    private static void saveEntryToFile(String entry) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME, true))) {
            writer.println(entry);
        } catch (IOException e) {
            System.out.println("Error al escribir en el archivo: " + e.getMessage());
        }
    }

    //hace el select
    private static String readEntryFromFile(String entryId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(entryId)) {
                    return line;
                }
            }
        } catch (IOException e) {
            System.out.println("Error al leer del archivo: " + e.getMessage());
        }
        return null;
    }

    //hace el delete
    private static boolean deleteEntryFromFile(String entryId) {
        File originalFile = new File(FILE_NAME);
        List<String> remainingLines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(originalFile))) {
            String line;
            boolean found = false;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(entryId)) {
                    found = true;
                    continue;
                }
                remainingLines.add(line);
            }

            if (!found) {
                return false;
            }

        } catch (IOException e) {
            System.out.println("Error al leer el archivo: " + e.getMessage());
            return false;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(originalFile))) {
            for (String remainingLine : remainingLines) {
                writer.println(remainingLine);
            }
        } catch (IOException e) {
            System.out.println("Error al escribir en el archivo: " + e.getMessage());
            return false;
        }
        return true;
    }

    //comprueba si el id existe
    private static boolean isIdExists(String entryId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 0 && parts[0].equals(entryId)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("Error al leer del archivo: " + e.getMessage());
        }
        return false;
    }
}
