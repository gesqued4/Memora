package integradora;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class Memories {

    // Attributes
    String usrName;
    String title;
    String description;
    String emojies;
    String date;
    String rutaArchivo;

    public Memories(String usrName, String title, String description, String emojies, String date, String rutaArchivo) {
        this.usrName = usrName;
        this.title = title;
        this.description = description;
        this.emojies = emojies;
        this.date = date;
        this.rutaArchivo = rutaArchivo;
    }
    
    public String getEmojies() {
        return emojies;
    }
    
    public String getUsrName() {
        return usrName;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public void setUsrName(String usrName) {
        this.usrName = usrName;
    }
    
    public void setEmojies(String emojies) {
        this.emojies = emojies;
    }

    /*
        Methods to add and update memories
     */
    private static final String filePath = Paths.get("src", "resources", "memories.txt").toString();

    // Save all memories to the file
    public static boolean saveToFile(Memories memorie) {
        try (BufferedWriter wr = new BufferedWriter(new FileWriter(filePath, true))) {
            wr.write(memorie.getUsrName() + "\t" + memorie.getTitle() + "\t" + memorie.getDescription() + "\t" + memorie.getEmojies() + "\t" + memorie.getDate() + "\t" + memorie.getRutaArchivo() + "\n");
            wr.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update all memories to the file
    public static void updateFile(Nodo inicio) {
        try (BufferedWriter wr = new BufferedWriter(new FileWriter(filePath))) {
            Nodo auxiliar = inicio;
            if (auxiliar != null) {
                do {
                    Memories memorie = auxiliar.getMemorie();
                    wr.write(memorie.getUsrName() + "\t" + memorie.getTitle() + "\t" + memorie.getDescription() + "\t" + memorie.getEmojies() + "\t" + memorie.getDate() + "\t" + memorie.getRutaArchivo() + "\n");
                    wr.newLine();
                    auxiliar = auxiliar.getSiguiente();
                } while (auxiliar != inicio);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Checks if the txt file is not empty
    public static boolean fileEmpty() {
        try (BufferedReader rd = new BufferedReader(new FileReader(filePath))) {
            String line = rd.readLine(); // Read the first line in the file
            if (line == null || line.trim().isEmpty()) { // If it is null, that means that the file is empty
                return true;
            }
            return false; // Else if there is a line, the file is not empty
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
    }
}
