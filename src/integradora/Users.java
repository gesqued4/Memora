
package integradora;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Paths;

public class Users {
    // Atributes
    private String usrName;
    private String passwd;
    // Constructor
    public Users(String usrName, String passwd) {
        this.usrName = usrName;
        this.passwd = passwd;
    }
    // Getters and setters for the diff atributes
    public void setUsrName(String usrName) {
        this.usrName = usrName;
    }
    public String getUsrName() {
        return usrName;
    }
    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }
    public String getPasswd() {
        return passwd;
    }
    /*
        Methods to add, update and delete a user
     */  
    private static final String filePath = Paths.get("src", "resources", "users.txt").toString();
    public static List<Users> userList = new ArrayList<>();
   
    public static void addToList(Users user) {
        userList.add(user);
    }
   
    // Save all users to the file
    public static boolean saveToFile(Users user) {
        try (BufferedWriter wr = new BufferedWriter(new FileWriter(filePath, true))) {
            wr.write(user.getUsrName() + "\t" + user.getPasswd() + "\n");
            wr.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    // Update all users to the file
    public static void updateFile(List<Users> userList) {
        try (BufferedWriter wr = new BufferedWriter(new FileWriter(filePath))) {
            for (Users user : userList) {
                wr.write(user.getUsrName() + "\t" + user.getPasswd());
                wr.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Method to validate the user login (compare username and password)
    public static boolean validateUser(String usrName, String passwd) {
        // Load users from the file
        loadUsers();

        // Check if the provided username and password match any user in the list
        for (Users user : userList) {
            if (user.getUsrName().equals(usrName) && user.getPasswd().equals(passwd)) {
                return true;  // Return true if a match is found
            }
        }
        return false;  // Return false if no match is found
    }
    // Helper method to load users from the file
    public static void loadUsers() {
        userList.clear();  // Clear the list to avoid duplicates
        try (BufferedReader rd = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = rd.readLine()) != null) {
                String[] userInfo = line.split("\t"); // Assuming fields are tab-separated
                if (userInfo.length == 2) {
                    String usrName = userInfo[0];
                    String passwd = userInfo[1];
                    Users user = new Users(usrName, passwd);
                    userList.add(user);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

