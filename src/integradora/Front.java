package integradora;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.nio.file.Paths;
import emojis.EmojiParser;
import java.util.ArrayList;
import java.util.Random;

public class Front extends javax.swing.JFrame {

    // Variables para manejar la logica de la pestana actual
    int currentTab = 0;
    // Variables para manejar la logica del nodo
    Nodo actual = null, inicio = null, fin = null;
    Image img;
    String temporalImg;
    // Variables para manejar la logica de suggestions
    Random rand = new Random();
    int indexRand = 0;
    // Ruta del archivo memories.txt
    String filePath = Paths.get("src", "resources", "memories.txt").toString();
    // Ruta del archivo suggestions.txt
    String pathSuggtions = Paths.get("src", "resources", "suggestions.txt").toString();

    /*
        * Metodos para crear una cuenta nueva
        * o iniciar sesion
     */
    private void signIn() { // Sesion de usuario
        String usrName = userField.getText();
        String passwd = passwdField.getText();
        if (Users.validateUser(usrName, passwd)) { // Comparamos la entrada con la data en el txt
            currentTab = 2; // Si el usuario existe continua a la siguiente seccion "memories"
            content.setSelectedIndex(currentTab);
            if (!Memories.fileEmpty()) {
                // Si existe informacion previamente almacenada en el txt la muestra
                // Muestra las img de los ultimos 5 nodos anadidos
                // El titulo y la descripcion del ultimo nodo es la que se muestra
                loadNodes(); // Llena los nodos desde la info en el txt 
                refreshMemories(); // Actualiza el visor Memories
                suggestions();
                JOptionPane.showMessageDialog(this, "Welcome " + usrName, "Login succesful", JOptionPane.INFORMATION_MESSAGE);
            } else { // de lo contrario pasa a la siguiente seccion anadir nuevos nodos addMemories
                currentTab = 3;
                content.setSelectedIndex(currentTab); // Seccion "addMemories"", 
                JOptionPane.showMessageDialog(this, "Welcome " + usrName + ". No saved memories. Add a new one!", "Login succesful", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            currentTab = 0; // Seccion "signIn"
            content.setSelectedIndex(currentTab); // Si el usuario no existe se queda en la seccion signIn
            userField.setText("Username");  // Reestablece el mensaje del textfield
            passwdField.setText("Password");
            JOptionPane.showMessageDialog(this, "Incorrect username or password", "Login error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void signUp() { // Este metodo es para crear un nuevo usuario en la seccion signUp
        String usrName = userField1.getText();
        String passwd = passwdField1.getText();
        if (!usrName.isEmpty() && !passwd.isEmpty()) {
            Users newUser = new Users(usrName, passwd);  // Crea una nueva instancia de Users
            Users.addToList(newUser);   // Agrega el usuario a la lista
            Users.saveToFile(newUser);
            content.setSelectedIndex(0); // Muestra la seccion "signIn"
            currentTab = 0; // Almacena la pestana actual
            JOptionPane.showMessageDialog(this, "User " + usrName + " created succesfully", "Registration succesful", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid input, please complete all the fields", "Signup error", JOptionPane.ERROR_MESSAGE);
            userField.setText("Username");  // Restablece el msj del text field
            passwdField.setText("Password");
        }
        userField.setText("Username");  // Reestablece el msj del text field
        passwdField.setText("Password");
    }

    /*
        *
        * Metodos utilizados en la sesion del usuario utilizando nodos
        *
     */
    boolean listaVacia() {
        return inicio == null;
    }

    private void addNodes() { // Añade nodos al final de la lista tipo LIFO 
        // Este metodo es para añadir un nuevo nodo a la lista doblemente enlazada circular
        // Cuando el usuario presiona el boton add, se crea un nuevo nodo con todos los parametros
        // Ademas crea una copia de los datos en la clase Memories para almacenarlos en un txt
        // Para tener registros y para su uso posterior en los nodos sin tener que volver a agregar la misma informacion
        String usrName = userField.getText();
        String titulo = titleField.getText();
        String descripcion = descriptionField.getText();
        String emojies = EmojiParser.parseToUnicode(emojisField.getText()); // Convierte el alias a codigo unicode
        if (!titulo.isEmpty() && !descripcion.isEmpty() && !emojies.isEmpty()) {
            java.time.LocalDateTime ahora = java.time.LocalDateTime.now(); // Obtener la fecha actual formateada
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String fechaActual = ahora.format(formatter);
            Memories newMemorie = new Memories(usrName, titulo, descripcion, emojies, fechaActual, temporalImg); // Instancia de Memories, copia los datos al txt
            Nodo nuevo = new Nodo(newMemorie, null, null);
            if (listaVacia()) {
                System.out.println("Primer nodo insertado");
                fin = nuevo;
                inicio = nuevo;
                Memories.saveToFile(newMemorie); // Guarda los datos en el archivo memories.txt
                JOptionPane.showMessageDialog(this, "First memory saved succesfully!", "Memory added", JOptionPane.INFORMATION_MESSAGE);
            } else {
                fin.setSiguiente(nuevo);
                nuevo.setAnterior(fin);
                System.out.println("Nuevo nodo insertado con exito");
                fin = nuevo;
                Memories.saveToFile(newMemorie); // Guarda los datos en el arhivo memories.txt
                JOptionPane.showMessageDialog(this, "Memory added succesfully!", "Memory added", JOptionPane.INFORMATION_MESSAGE);
            }
            inicio.setAnterior(fin); // Le da la circularidad a la lista
            fin.setSiguiente(inicio);
            actual = fin;
            titleField.setText("Title"); // Reestablece los textfield una vez agregado el nodo
            descriptionField.setText("Description");
            emojisField.setText("Emojies");
            imagen.setIcon(null);
            imagen.setText("Drag and drop an image here");
            temporalImg = null;
        } else {
            String errorMsg = "Entrada inválida. ";
            if (titulo.isEmpty()) {
                errorMsg += "Falta título. ";
            }
            if (descripcion.isEmpty()) {
                errorMsg += "Falta descripción. ";
            }
            if (emojies.isEmpty()) {
                errorMsg += "Falta emojies.";
            }
            if (temporalImg == null) {
                errorMsg += "Falta imagen.";
            }
            JOptionPane.showMessageDialog(this, errorMsg, "Error adding memory", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadNodes() { // Metodo para cargar memories previamente almacenados en un txt dentro de nuevos nodos
        try (BufferedReader rd = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = rd.readLine()) != null) {
                String[] memorieInfo = line.split("\t"); // Los campos estan separados por tabulador
                if (memorieInfo.length == 6) {
                    String usrName = memorieInfo[0];
                    String title = memorieInfo[1];
                    String description = memorieInfo[2];
                    String emojies = memorieInfo[3];
                    String date = memorieInfo[4];
                    String rutaArchivo = memorieInfo[5];
                    Memories memorie = new Memories(usrName, title, description, emojies, date, rutaArchivo);
                    Nodo nuevo = new Nodo(memorie, null, null);
                    if (inicio == null) {
                        System.out.println("Primer nodo insertado");
                        fin = nuevo;
                        inicio = nuevo;
                    } else {
                        fin.setSiguiente(nuevo);
                        nuevo.setAnterior(fin);
                        System.out.println("Nuevo nodo insertado con exito");
                        fin = nuevo;
                    }
                }
                inicio.setAnterior(fin); // Le da la circularidad a la lista
                fin.setSiguiente(inicio);
                actual = fin;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cargarPrevisualizacion(int size, Nodo nodoActual, JLabel title, JLabel description, JLabel emojies, JLabel date, JLabel image) { // Metodo para cargar el visor del contenido del nodo
        if (actual != null) {
            title.setText(actual.memorie.getTitle());
            description.setText(actual.memorie.getDescription());
            emojies.setText(actual.memorie.getEmojies());
            date.setText("Date: " + actual.memorie.getDate());
            if(image != null) { // Valida que el parametro del label image no sea nulo 
                ImageIcon icon = new ImageIcon(actual.memorie.getRutaArchivo());
                img = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                image.setIcon(new ImageIcon(img)); // Reestablecer el label con la imagen del nuevo elemento
                image.setText("");
            } 
        } else {
            title.setText("Title");
            description.setText("Description");
            emojies.setText("Emojies");
            date.setText("Date");
            image.setIcon(null);
            image.setText("Imagen");
        }
    }

    private void refreshMemories() { // Actualiza el visor Memories en la seccion "memories"
        if (actual != null) { // Llena el visor con las imagenes de los ultimos 5 nodos
            actual = fin; // Manteniendo tipo lifo
            cargarPrevisualizacion(192, actual, titleMemories, descriptionMemories, emojiesMemories, dateMemories, imagen1);
            actual = fin.getAnterior();
            cargarPrevisualizacion(90, actual, titleMemories, descriptionMemories, emojiesMemories, dateMemories, imagen2);
            actual = fin.getAnterior().getAnterior();
            cargarPrevisualizacion(90, actual, titleMemories, descriptionMemories, emojiesMemories, dateMemories, imagen3);
            actual = fin.getAnterior().getAnterior().getAnterior();
            cargarPrevisualizacion(90, actual, titleMemories, descriptionMemories, emojiesMemories, dateMemories, imagen4);
            actual = fin.getAnterior().getAnterior().getAnterior().getAnterior();
            cargarPrevisualizacion(90, actual, titleMemories, descriptionMemories, emojiesMemories, dateMemories, imagen5);
            actual = fin;
            cargarPrevisualizacion(90, actual, titleMemories, descriptionMemories, emojiesMemories, dateMemories, null); // Muestra el titulo y descripcion del ultimo nodo agregado       
        } else {
            titleMemories.setText("Title");
            descriptionMemories.setText("Description");
            emojiesMemories.setText("Emojies");
            dateMemories.setText("Date ");
            imagen1.setIcon(null);
            imagen1.setText("Imagen");
            imagen4.setIcon(null);
            imagen4.setText("Imagen");
            imagen5.setIcon(null);
            imagen5.setText("Imagen");
            imagen2.setIcon(null);
            imagen2.setText("Imagen");
            imagen3.setIcon(null);
            imagen3.setText("Imagen");
        }
    }

    private void avanzar() {
        if (!listaVacia()) {
            actual = actual.getSiguiente();
            if (actual == null) {
                actual = inicio;
            }
            cargarPrevisualizacion(330, actual, titleAllMemories, descriptionAllMemories, emojiesAllMemories, dateAllMemories, imagen6);
        } else {
            JOptionPane.showMessageDialog(this, "No memories to display", "Empty list", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void retroceder() {
        if (!listaVacia()) {
            actual = actual.getAnterior();
            if (actual == null) {
                actual = fin;
            }
            cargarPrevisualizacion(330, actual, titleAllMemories, descriptionAllMemories, emojiesAllMemories, dateAllMemories, imagen6);
        } else {
            JOptionPane.showMessageDialog(this, "No memories to display", "Empty list", JOptionPane.WARNING_MESSAGE);
        }

    }

    private void irInicio() {
        if (!listaVacia()) {
            actual = inicio;
            cargarPrevisualizacion(330, actual, titleAllMemories, descriptionAllMemories, emojiesAllMemories, dateAllMemories, imagen6);
            JOptionPane.showMessageDialog(this, "Moving to the first image", "First image", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No memories to display", "Empty list", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void irFin() {
        if (!listaVacia()) {
            actual = fin;
            cargarPrevisualizacion(330, actual, titleAllMemories, descriptionAllMemories, emojiesAllMemories, dateAllMemories, imagen6);
            JOptionPane.showMessageDialog(this, "Moving to the last image", "Last image", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No memories to display", "Empty list", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void deleteActual() {
        if (listaVacia()) {
            JOptionPane.showMessageDialog(this, "No memories to delete", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            int confirm = JOptionPane.showConfirmDialog(this, // Panel de confirmacion antes de eliminar
                    "Are you sure you want to delete this memory?",
                    "Confirm deletion",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                String titulo = actual.memorie.getTitle();
                if (inicio == actual && fin == actual) { // El nodo actual es el unico nodo en la lista
                    inicio = null;
                    fin = null;
                    actual = null;
                    Memories.updateFile(inicio);
                    cargarPrevisualizacion(330, actual, titleAllMemories, descriptionAllMemories, emojiesAllMemories, dateAllMemories, imagen6); // Actualiza el visor
                    System.out.println("Nodo actual eliminado, la lista esta vacia");
                } else if (actual == inicio) { // El nodo actual es el primer nodo
                    inicio = actual.getSiguiente(); // El siguiente nodo se convierte en el primer nodo
                    fin.setSiguiente(inicio); // El último nodo ahora apunta al nuevo primer nodo
                    inicio.setAnterior(fin); // El nuevo primer nodo apunta al último nodo
                    actual = null;//
                    actual = inicio;//
                    Memories.updateFile(inicio);//
                    cargarPrevisualizacion(330, actual, titleAllMemories, descriptionAllMemories, emojiesAllMemories, dateAllMemories, imagen6); // Actualiza el visor
                    System.out.println("Nodo actual eliminado, es el primer nodo");
                } else if (actual == fin) { // El nodo actual es el ultimo nodo
                    fin = actual.getAnterior(); // El nodo anterior se convierte en el ultimo nodo
                    inicio.setAnterior(fin); // El primer nodo ahora apunta al nuevo último nodo
                    fin.setSiguiente(inicio); // El nuevo último nodo apunta al primer nodo
                    actual = null;
                    actual = fin;//
                    Memories.updateFile(inicio);//
                    cargarPrevisualizacion(330, actual, titleAllMemories, descriptionAllMemories, emojiesAllMemories, dateAllMemories, imagen6); // Actualiza el visor
                    System.out.println("Nodo actual eliminado, es el ultimo nodo");
                } else { // El nodo actual esta en el medio de la lista
                    Nodo anterior = actual.getAnterior();
                    Nodo siguiente = actual.getSiguiente();
                    anterior.setSiguiente(siguiente); // Conectar el anterior al siguietne
                    siguiente.setAnterior(anterior); // Conectar el siguiente al anterior
                    actual.setSiguiente(null);
                    actual.setAnterior(null);
                    actual = null;
                    actual = anterior;
                    Memories.updateFile(inicio);
                    cargarPrevisualizacion(330, actual, titleAllMemories, descriptionAllMemories, emojiesAllMemories, dateAllMemories, imagen6); // Actualiza el visor
                    System.out.println("Nodo actual eliminado, es un nodo intermedio");
                }
            }
        }
    }

    /*
        * 
        * Metodos para mostrar una sugerencia aleatoria de una base de datos de un txt 
        *
     */
    private void loadSuggestionsList(ArrayList<String> suggestionsList) { // Llena array list a partir de la info del txt suggestions.txt
        try (BufferedReader br = new BufferedReader(new FileReader(pathSuggtions))) { // Lee el archivo y carga la info en el txt
            String line;
            while ((line = br.readLine()) != null) {
                suggestionsList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void suggestions() {
        ArrayList<String> suggestionsList = new ArrayList<>(); // Crea un arraylist para almacenar la info del txt
        loadSuggestionsList(suggestionsList);
        indexRand = rand.nextInt(10);
        String suggestions = suggestionsList.get(indexRand);
        suggestionsDescription.setText("<html><body style='width: 250px'>" + suggestions + "</body></html>");
    }

    /*
        * Metodo para crear un objeto de la clase DropTarget para que 
        * el label reciba una imagen al ser arrastrada sobre el
        * componente lblImage
     */
    private void cargarImg() {
        DropTarget dropTarget = new DropTarget(imagen, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) { // Se activa cuando el usuario suelta el archivo
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY); // Indica que se acepta la operacion de copiar el archivo al programa
                    Object transferData = dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor); // Obtiene la lista de archivos arrastrados
                    if (transferData instanceof List) { // Verifica que los datos sean una lista de archivos
                        List<?> fileList = (List<?>) transferData;
                        if (!fileList.isEmpty() && fileList.get(0) instanceof File) { // Asegura que la lista contiene al menos un archivo
                            File file = (File) fileList.get(0);
                            temporalImg = file.getAbsolutePath();
                            ImageIcon icon = new ImageIcon(temporalImg);
                            img = icon.getImage().getScaledInstance(330, 330, Image.SCALE_SMOOTH);
                            if (img != null) {
                                imagen.setIcon(new ImageIcon(img));
                                imagen.setText(null);
                            }
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error al cargar la imagen", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    /*
        *
        * Metodo para ocultar el input passwdField al momento de ingresar contrasena
        *
     */
    private void setupTransparentTextFields() {
        // Lista de todos tus text fields que quieres modificar
        javax.swing.JTextField[] textFields = {
            titleField,
            descriptionField,
            emojisField
        };

        // También puedes aplicarlo a los campos de texto de tipo TextField si es necesario
        java.awt.TextField[] awtFields = {
            userField,
            passwdField,
            userField1,
            passwdField1
        };

        // Configurar los JTextField para que sean transparentes
        for (javax.swing.JTextField field : textFields) {

            // Añadir placeholder y comportamiento para ocultar/mostrar texto
            final String placeholder = field.getText();

            // Añadir listener para cuando el campo reciba el foco (al hacer clic)
            field.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent evt) {
                    if (field.getText().equals(placeholder)) {
                        field.setText("");
                    }
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent evt) {
                    if (field.getText().isEmpty()) {
                        field.setText(placeholder);
                    }
                }
            });
        }

        // Configurar los TextField de AWT para que sean transparentes
        for (java.awt.TextField field : awtFields) {
            // En TextField de AWT el efecto de transparencia es más limitado
            //field.setBackground(new java.awt.Color(255, 255, 255, 80)); // Semi-transparente

            final String placeholder = field.getText();

            // Para los campos de contraseña, implementar el ocultamiento del texto
            final boolean isPassword = field == passwdField || field == passwdField1;

            field.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent evt) {
                    if (field.getText().equals(placeholder)) {
                        field.setText("");
                        if (isPassword) {
                            field.setEchoChar('*'); // Caracteres de contraseña
                        }
                    }
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent evt) {
                    if (field.getText().isEmpty()) {
                        field.setText(placeholder);
                        if (isPassword) {
                            field.setEchoChar((char) 0); // Mostrar texto normal
                        }
                    }
                }
            });
        }
    }

    public Front() {
        initComponents();
        setupTransparentTextFields();
        cargarImg();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        background = new javax.swing.JPanel();
        topBar = new javax.swing.JPanel();
        content = new javax.swing.JTabbedPane();
        signIn = new javax.swing.JPanel();
        signInBg = new javax.swing.JPanel();
        title = new javax.swing.JLabel();
        userField = new java.awt.TextField();
        passwdField = new java.awt.TextField();
        LoginBtton = new javax.swing.JPanel();
        logInTitle = new javax.swing.JLabel();
        signUpTitle = new javax.swing.JLabel();
        signUp = new javax.swing.JPanel();
        signUpBg = new javax.swing.JPanel();
        title1 = new javax.swing.JLabel();
        userField1 = new java.awt.TextField();
        passwdField1 = new java.awt.TextField();
        signUpBtton = new javax.swing.JPanel();
        signUpBttonTitle = new javax.swing.JLabel();
        signInTitle = new javax.swing.JLabel();
        memories = new javax.swing.JPanel();
        recuerdoTitle = new javax.swing.JLabel();
        imgBg = new javax.swing.JPanel();
        imagen1 = new javax.swing.JLabel();
        imagen4 = new javax.swing.JLabel();
        imagen5 = new javax.swing.JLabel();
        imagen2 = new javax.swing.JLabel();
        imagen3 = new javax.swing.JLabel();
        titleMemories = new javax.swing.JLabel();
        descriptionMemories = new javax.swing.JLabel();
        dateMemories = new javax.swing.JLabel();
        emojiesMemories = new javax.swing.JLabel();
        masIconBg = new javax.swing.JPanel();
        masIcon = new javax.swing.JLabel();
        imageIcon = new javax.swing.JLabel();
        homeIcon = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        suggestionsTitle = new javax.swing.JLabel();
        suggestionsDescription = new javax.swing.JLabel();
        refreshIcon = new javax.swing.JLabel();
        addMemories = new javax.swing.JPanel();
        recuerdoTitle1 = new javax.swing.JLabel();
        imgBg1 = new javax.swing.JPanel();
        imagen = new javax.swing.JLabel();
        titleField = new javax.swing.JTextField();
        descriptionField = new javax.swing.JTextField();
        emojisField = new javax.swing.JTextField();
        masIconBg1 = new javax.swing.JPanel();
        masIcon1 = new javax.swing.JLabel();
        homeIcon2 = new javax.swing.JLabel();
        imageIcon2 = new javax.swing.JLabel();
        allMyMemories = new javax.swing.JPanel();
        recuerdoTitle2 = new javax.swing.JLabel();
        imgBg2 = new javax.swing.JPanel();
        imagen6 = new javax.swing.JLabel();
        dateAllMemories = new javax.swing.JLabel();
        titleAllMemories = new javax.swing.JLabel();
        descriptionAllMemories = new javax.swing.JLabel();
        emojiesAllMemories = new javax.swing.JLabel();
        trashIconBg = new javax.swing.JPanel();
        trashIcon = new javax.swing.JLabel();
        homeIcon3 = new javax.swing.JLabel();
        imageIcon3 = new javax.swing.JLabel();
        flechaIzq = new javax.swing.JLabel();
        flechaDer = new javax.swing.JLabel();
        irInicio = new javax.swing.JLabel();
        irFin = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setMaximumSize(new java.awt.Dimension(500, 900));
        setMinimumSize(new java.awt.Dimension(500, 900));
        setResizable(false);

        background.setBackground(new java.awt.Color(28, 30, 47));
        background.setMaximumSize(new java.awt.Dimension(500, 900));
        background.setMinimumSize(new java.awt.Dimension(500, 900));
        background.setPreferredSize(new java.awt.Dimension(500, 900));
        background.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        topBar.setBackground(new java.awt.Color(28, 30, 47));

        javax.swing.GroupLayout topBarLayout = new javax.swing.GroupLayout(topBar);
        topBar.setLayout(topBarLayout);
        topBarLayout.setHorizontalGroup(
            topBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 500, Short.MAX_VALUE)
        );
        topBarLayout.setVerticalGroup(
            topBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        background.add(topBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 500, 40));

        content.setBackground(new java.awt.Color(28, 30, 47));
        content.setMaximumSize(new java.awt.Dimension(500, 600));
        content.setMinimumSize(new java.awt.Dimension(500, 600));
        content.setPreferredSize(new java.awt.Dimension(500, 600));

        signIn.setBackground(new java.awt.Color(28, 30, 47));
        signIn.setMaximumSize(new java.awt.Dimension(500, 900));
        signIn.setMinimumSize(new java.awt.Dimension(500, 900));
        signIn.setPreferredSize(new java.awt.Dimension(500, 900));

        signInBg.setBackground(new java.awt.Color(57, 58, 95));

        title.setFont(new java.awt.Font("Trebuchet MS", 0, 36)); // NOI18N
        title.setForeground(new java.awt.Color(190, 190, 190));
        title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        title.setText("SIGN IN");
        title.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        userField.setBackground(new java.awt.Color(85, 87, 110));
        userField.setFont(new java.awt.Font("Trebuchet MS", 0, 18)); // NOI18N
        userField.setForeground(new java.awt.Color(145, 147, 170));
        userField.setText("Username");
        userField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                userFieldMouseClicked(evt);
            }
        });

        passwdField.setBackground(new java.awt.Color(85, 87, 110));
        passwdField.setFont(new java.awt.Font("Trebuchet MS", 0, 18)); // NOI18N
        passwdField.setForeground(new java.awt.Color(145, 147, 170));
        passwdField.setText("Password");
        passwdField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                passwdFieldMouseClicked(evt);
            }
        });

        LoginBtton.setBackground(new java.awt.Color(255, 101, 0));
        LoginBtton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        LoginBtton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                LoginBttonMouseClicked(evt);
            }
        });

        logInTitle.setBackground(new java.awt.Color(255, 101, 0));
        logInTitle.setFont(new java.awt.Font("Trebuchet MS", 0, 24)); // NOI18N
        logInTitle.setForeground(new java.awt.Color(190, 190, 190));
        logInTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        logInTitle.setText("Login");
        logInTitle.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                logInTitleMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout LoginBttonLayout = new javax.swing.GroupLayout(LoginBtton);
        LoginBtton.setLayout(LoginBttonLayout);
        LoginBttonLayout.setHorizontalGroup(
            LoginBttonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, LoginBttonLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(logInTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        LoginBttonLayout.setVerticalGroup(
            LoginBttonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, LoginBttonLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(logInTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        signUpTitle.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        signUpTitle.setForeground(new java.awt.Color(190, 190, 190));
        signUpTitle.setText("Signup");
        signUpTitle.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        signUpTitle.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                signUpTitleMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout signInBgLayout = new javax.swing.GroupLayout(signInBg);
        signInBg.setLayout(signInBgLayout);
        signInBgLayout.setHorizontalGroup(
            signInBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(signInBgLayout.createSequentialGroup()
                .addGroup(signInBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(signInBgLayout.createSequentialGroup()
                        .addGap(52, 52, 52)
                        .addGroup(signInBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(signUpTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(signInBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(userField, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(signInBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(passwdField, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                                    .addComponent(LoginBtton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                    .addGroup(signInBgLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(title, javax.swing.GroupLayout.PREFERRED_SIZE, 362, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        signInBgLayout.setVerticalGroup(
            signInBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(signInBgLayout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(title, javax.swing.GroupLayout.DEFAULT_SIZE, 51, Short.MAX_VALUE)
                .addGap(96, 96, 96)
                .addComponent(userField, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addComponent(passwdField, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22)
                .addComponent(signUpTitle)
                .addGap(18, 18, 18)
                .addComponent(LoginBtton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37))
        );

        javax.swing.GroupLayout signInLayout = new javax.swing.GroupLayout(signIn);
        signIn.setLayout(signInLayout);
        signInLayout.setHorizontalGroup(
            signInLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(signInLayout.createSequentialGroup()
                .addGap(58, 58, 58)
                .addComponent(signInBg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(58, 58, 58))
        );
        signInLayout.setVerticalGroup(
            signInLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(signInLayout.createSequentialGroup()
                .addGap(95, 95, 95)
                .addComponent(signInBg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(378, Short.MAX_VALUE))
        );

        content.addTab("tab1", signIn);

        signUp.setBackground(new java.awt.Color(28, 30, 47));

        signUpBg.setBackground(new java.awt.Color(57, 58, 95));

        title1.setFont(new java.awt.Font("Trebuchet MS", 0, 36)); // NOI18N
        title1.setForeground(new java.awt.Color(190, 190, 190));
        title1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        title1.setText("SIGN UP");
        title1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        userField1.setBackground(new java.awt.Color(85, 87, 110));
        userField1.setFont(new java.awt.Font("Trebuchet MS", 0, 18)); // NOI18N
        userField1.setForeground(new java.awt.Color(145, 147, 170));
        userField1.setText("Username");
        userField1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                userField1MouseClicked(evt);
            }
        });

        passwdField1.setBackground(new java.awt.Color(85, 87, 110));
        passwdField1.setFont(new java.awt.Font("Trebuchet MS", 0, 18)); // NOI18N
        passwdField1.setForeground(new java.awt.Color(145, 147, 170));
        passwdField1.setText("Password");
        passwdField1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                passwdField1MouseClicked(evt);
            }
        });

        signUpBtton.setBackground(new java.awt.Color(255, 101, 0));
        signUpBtton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        signUpBtton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                signUpBttonMouseClicked(evt);
            }
        });

        signUpBttonTitle.setBackground(new java.awt.Color(255, 101, 0));
        signUpBttonTitle.setFont(new java.awt.Font("Trebuchet MS", 0, 24)); // NOI18N
        signUpBttonTitle.setForeground(new java.awt.Color(190, 190, 190));
        signUpBttonTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        signUpBttonTitle.setText("Sign up");
        signUpBttonTitle.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                signUpBttonTitleMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout signUpBttonLayout = new javax.swing.GroupLayout(signUpBtton);
        signUpBtton.setLayout(signUpBttonLayout);
        signUpBttonLayout.setHorizontalGroup(
            signUpBttonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, signUpBttonLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(signUpBttonTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        signUpBttonLayout.setVerticalGroup(
            signUpBttonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, signUpBttonLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(signUpBttonTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        signInTitle.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        signInTitle.setForeground(new java.awt.Color(190, 190, 190));
        signInTitle.setText("Sign in");
        signInTitle.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        signInTitle.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                signInTitleMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout signUpBgLayout = new javax.swing.GroupLayout(signUpBg);
        signUpBg.setLayout(signUpBgLayout);
        signUpBgLayout.setHorizontalGroup(
            signUpBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(signUpBgLayout.createSequentialGroup()
                .addGroup(signUpBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(signUpBgLayout.createSequentialGroup()
                        .addGap(52, 52, 52)
                        .addGroup(signUpBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(signUpBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(passwdField1, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                                .addComponent(signUpBtton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(userField1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(signInTitle, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(signUpBgLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(title1, javax.swing.GroupLayout.PREFERRED_SIZE, 362, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        signUpBgLayout.setVerticalGroup(
            signUpBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(signUpBgLayout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(title1, javax.swing.GroupLayout.DEFAULT_SIZE, 51, Short.MAX_VALUE)
                .addGap(96, 96, 96)
                .addComponent(userField1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addComponent(passwdField1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(signInTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(signUpBtton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37))
        );

        javax.swing.GroupLayout signUpLayout = new javax.swing.GroupLayout(signUp);
        signUp.setLayout(signUpLayout);
        signUpLayout.setHorizontalGroup(
            signUpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(signUpLayout.createSequentialGroup()
                .addGap(58, 58, 58)
                .addComponent(signUpBg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(58, 58, 58))
        );
        signUpLayout.setVerticalGroup(
            signUpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(signUpLayout.createSequentialGroup()
                .addGap(95, 95, 95)
                .addComponent(signUpBg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(340, Short.MAX_VALUE))
        );

        content.addTab("tab2", signUp);

        memories.setBackground(new java.awt.Color(28, 30, 47));

        recuerdoTitle.setFont(new java.awt.Font("Trebuchet MS", 0, 48)); // NOI18N
        recuerdoTitle.setForeground(new java.awt.Color(190, 190, 190));
        recuerdoTitle.setText("Memories");

        imgBg.setBackground(new java.awt.Color(57, 58, 95));
        imgBg.setMaximumSize(new java.awt.Dimension(420, 400));
        imgBg.setMinimumSize(new java.awt.Dimension(420, 400));
        imgBg.setPreferredSize(new java.awt.Dimension(420, 400));

        imagen1.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        imagen1.setForeground(new java.awt.Color(190, 190, 190));
        imagen1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imagen1.setText("Image");
        imagen1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        imagen1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        imagen1.setMaximumSize(new java.awt.Dimension(192, 192));
        imagen1.setMinimumSize(new java.awt.Dimension(192, 192));
        imagen1.setPreferredSize(new java.awt.Dimension(192, 192));
        imagen1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                imagen1MouseClicked(evt);
            }
        });

        imagen4.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        imagen4.setForeground(new java.awt.Color(190, 190, 190));
        imagen4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imagen4.setText("Image");
        imagen4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        imagen4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        imagen4.setMaximumSize(new java.awt.Dimension(90, 90));
        imagen4.setMinimumSize(new java.awt.Dimension(90, 90));
        imagen4.setPreferredSize(new java.awt.Dimension(90, 90));
        imagen4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                imagen4MouseClicked(evt);
            }
        });

        imagen5.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        imagen5.setForeground(new java.awt.Color(190, 190, 190));
        imagen5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imagen5.setText("Image");
        imagen5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        imagen5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        imagen5.setMaximumSize(new java.awt.Dimension(90, 90));
        imagen5.setMinimumSize(new java.awt.Dimension(90, 90));
        imagen5.setPreferredSize(new java.awt.Dimension(90, 90));
        imagen5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                imagen5MouseClicked(evt);
            }
        });

        imagen2.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        imagen2.setForeground(new java.awt.Color(190, 190, 190));
        imagen2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imagen2.setText("Image");
        imagen2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        imagen2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        imagen2.setMaximumSize(new java.awt.Dimension(90, 90));
        imagen2.setMinimumSize(new java.awt.Dimension(90, 90));
        imagen2.setPreferredSize(new java.awt.Dimension(90, 90));
        imagen2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                imagen2MouseClicked(evt);
            }
        });

        imagen3.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        imagen3.setForeground(new java.awt.Color(190, 190, 190));
        imagen3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imagen3.setText("Image");
        imagen3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        imagen3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        imagen3.setMaximumSize(new java.awt.Dimension(90, 90));
        imagen3.setMinimumSize(new java.awt.Dimension(90, 90));
        imagen3.setPreferredSize(new java.awt.Dimension(90, 90));
        imagen3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                imagen3MouseClicked(evt);
            }
        });

        titleMemories.setFont(new java.awt.Font("Trebuchet MS", 0, 32)); // NOI18N
        titleMemories.setForeground(new java.awt.Color(190, 190, 190));
        titleMemories.setText("Title");

        descriptionMemories.setFont(new java.awt.Font("Trebuchet MS", 0, 18)); // NOI18N
        descriptionMemories.setForeground(new java.awt.Color(190, 190, 190));
        descriptionMemories.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        descriptionMemories.setText("Description");
        descriptionMemories.setToolTipText("");
        descriptionMemories.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        dateMemories.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        dateMemories.setForeground(new java.awt.Color(190, 190, 190));
        dateMemories.setText("Date");

        emojiesMemories.setFont(new java.awt.Font("Segoe UI Emoji", 0, 14)); // NOI18N
        emojiesMemories.setForeground(new java.awt.Color(190, 190, 190));
        emojiesMemories.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        emojiesMemories.setText("Emojies");
        emojiesMemories.setToolTipText("");

        javax.swing.GroupLayout imgBgLayout = new javax.swing.GroupLayout(imgBg);
        imgBg.setLayout(imgBgLayout);
        imgBgLayout.setHorizontalGroup(
            imgBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(imgBgLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(imgBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(titleMemories, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(imgBgLayout.createSequentialGroup()
                        .addComponent(imagen1, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(imgBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(imgBgLayout.createSequentialGroup()
                                .addComponent(imagen4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(imagen5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(imgBgLayout.createSequentialGroup()
                                .addComponent(imagen2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(imagen3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(descriptionMemories, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(emojiesMemories, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dateMemories, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        imgBgLayout.setVerticalGroup(
            imgBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(imgBgLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(imgBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(imagen1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(imgBgLayout.createSequentialGroup()
                        .addGroup(imgBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(imagen2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(imagen3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(imgBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(imagen4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(imagen5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(titleMemories, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(descriptionMemories, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(emojiesMemories, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dateMemories, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        masIconBg.setBackground(new java.awt.Color(255, 101, 0));
        masIconBg.setMaximumSize(new java.awt.Dimension(85, 85));
        masIconBg.setMinimumSize(new java.awt.Dimension(85, 85));
        masIconBg.setPreferredSize(new java.awt.Dimension(85, 85));

        masIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        masIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/mas.png"))); // NOI18N
        masIcon.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        masIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                masIconMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout masIconBgLayout = new javax.swing.GroupLayout(masIconBg);
        masIconBg.setLayout(masIconBgLayout);
        masIconBgLayout.setHorizontalGroup(
            masIconBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(masIconBgLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(masIcon, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
                .addContainerGap())
        );
        masIconBgLayout.setVerticalGroup(
            masIconBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(masIconBgLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(masIcon, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
                .addContainerGap())
        );

        imageIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imageIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/imagen.png"))); // NOI18N
        imageIcon.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        imageIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                imageIconMouseClicked(evt);
            }
        });

        homeIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        homeIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/home.png"))); // NOI18N
        homeIcon.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        homeIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                homeIconMouseClicked(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(57, 58, 95));

        suggestionsTitle.setFont(new java.awt.Font("Trebuchet MS", 0, 36)); // NOI18N
        suggestionsTitle.setForeground(new java.awt.Color(190, 190, 190));
        suggestionsTitle.setText("Suggestions");
        suggestionsTitle.setToolTipText("");
        suggestionsTitle.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        suggestionsDescription.setFont(new java.awt.Font("Trebuchet MS", 0, 18)); // NOI18N
        suggestionsDescription.setForeground(new java.awt.Color(190, 190, 190));
        suggestionsDescription.setText("suggestionsDescription");
        suggestionsDescription.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        refreshIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/refrescar.png"))); // NOI18N
        refreshIcon.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        refreshIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                refreshIconMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(suggestionsDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 392, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(suggestionsTitle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(refreshIcon)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(suggestionsTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(refreshIcon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(suggestionsDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout memoriesLayout = new javax.swing.GroupLayout(memories);
        memories.setLayout(memoriesLayout);
        memoriesLayout.setHorizontalGroup(
            memoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(memoriesLayout.createSequentialGroup()
                .addGroup(memoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(memoriesLayout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addGroup(memoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(recuerdoTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 282, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(imgBg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(memoriesLayout.createSequentialGroup()
                        .addGap(198, 198, 198)
                        .addComponent(masIconBg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(37, 37, 37)
                        .addComponent(imageIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(40, Short.MAX_VALUE))
            .addGroup(memoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(memoriesLayout.createSequentialGroup()
                    .addGap(90, 90, 90)
                    .addComponent(homeIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(343, Short.MAX_VALUE)))
        );
        memoriesLayout.setVerticalGroup(
            memoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(memoriesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(recuerdoTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(imgBg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(memoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, memoriesLayout.createSequentialGroup()
                        .addComponent(masIconBg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(73, 73, 73))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, memoriesLayout.createSequentialGroup()
                        .addComponent(imageIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(82, 82, 82))))
            .addGroup(memoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, memoriesLayout.createSequentialGroup()
                    .addContainerGap(718, Short.MAX_VALUE)
                    .addComponent(homeIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(76, 76, 76)))
        );

        content.addTab("tab3", memories);

        addMemories.setBackground(new java.awt.Color(28, 30, 47));

        recuerdoTitle1.setFont(new java.awt.Font("Trebuchet MS", 0, 48)); // NOI18N
        recuerdoTitle1.setForeground(new java.awt.Color(190, 190, 190));
        recuerdoTitle1.setText("Add memories");

        imgBg1.setBackground(new java.awt.Color(57, 58, 95));
        imgBg1.setMaximumSize(new java.awt.Dimension(420, 400));
        imgBg1.setMinimumSize(new java.awt.Dimension(420, 400));

        imagen.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        imagen.setForeground(new java.awt.Color(190, 190, 190));
        imagen.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imagen.setText("Drag and drop an image here");
        imagen.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        imagen.setMaximumSize(new java.awt.Dimension(192, 192));
        imagen.setMinimumSize(new java.awt.Dimension(192, 192));
        imagen.setPreferredSize(new java.awt.Dimension(192, 192));

        titleField.setBackground(new java.awt.Color(85, 87, 110));
        titleField.setFont(new java.awt.Font("Trebuchet MS", 0, 18)); // NOI18N
        titleField.setForeground(new java.awt.Color(190, 190, 190));
        titleField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        titleField.setText("Title");
        titleField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                titleFieldMouseClicked(evt);
            }
        });

        descriptionField.setBackground(new java.awt.Color(85, 87, 110));
        descriptionField.setFont(new java.awt.Font("Trebuchet MS", 0, 18)); // NOI18N
        descriptionField.setForeground(new java.awt.Color(190, 190, 190));
        descriptionField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        descriptionField.setText("Description");
        descriptionField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                descriptionFieldMouseClicked(evt);
            }
        });

        emojisField.setBackground(new java.awt.Color(85, 87, 110));
        emojisField.setFont(new java.awt.Font("Trebuchet MS", 0, 18)); // NOI18N
        emojisField.setForeground(new java.awt.Color(190, 190, 190));
        emojisField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        emojisField.setText("Emojis");
        emojisField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                emojisFieldMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout imgBg1Layout = new javax.swing.GroupLayout(imgBg1);
        imgBg1.setLayout(imgBg1Layout);
        imgBg1Layout.setHorizontalGroup(
            imgBg1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(imgBg1Layout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addComponent(imagen, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, imgBg1Layout.createSequentialGroup()
                .addGap(0, 27, Short.MAX_VALUE)
                .addGroup(imgBg1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(descriptionField, javax.swing.GroupLayout.PREFERRED_SIZE, 368, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(titleField, javax.swing.GroupLayout.PREFERRED_SIZE, 368, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(emojisField, javax.swing.GroupLayout.PREFERRED_SIZE, 368, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25))
        );
        imgBg1Layout.setVerticalGroup(
            imgBg1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(imgBg1Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(imagen, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(titleField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(descriptionField, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(emojisField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(25, Short.MAX_VALUE))
        );

        masIconBg1.setBackground(new java.awt.Color(255, 101, 0));
        masIconBg1.setMaximumSize(new java.awt.Dimension(85, 85));
        masIconBg1.setMinimumSize(new java.awt.Dimension(85, 85));

        masIcon1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        masIcon1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/mas.png"))); // NOI18N
        masIcon1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        masIcon1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                masIcon1MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout masIconBg1Layout = new javax.swing.GroupLayout(masIconBg1);
        masIconBg1.setLayout(masIconBg1Layout);
        masIconBg1Layout.setHorizontalGroup(
            masIconBg1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(masIcon1, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
        );
        masIconBg1Layout.setVerticalGroup(
            masIconBg1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(masIcon1, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
        );

        homeIcon2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        homeIcon2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/home.png"))); // NOI18N
        homeIcon2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        homeIcon2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                homeIcon2MouseClicked(evt);
            }
        });

        imageIcon2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imageIcon2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/imagen.png"))); // NOI18N
        imageIcon2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        imageIcon2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                imageIcon2MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout addMemoriesLayout = new javax.swing.GroupLayout(addMemories);
        addMemories.setLayout(addMemoriesLayout);
        addMemoriesLayout.setHorizontalGroup(
            addMemoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addMemoriesLayout.createSequentialGroup()
                .addGroup(addMemoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addMemoriesLayout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addGroup(addMemoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(imgBg1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(recuerdoTitle1, javax.swing.GroupLayout.PREFERRED_SIZE, 331, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(addMemoriesLayout.createSequentialGroup()
                        .addGap(91, 91, 91)
                        .addComponent(homeIcon2, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(39, 39, 39)
                        .addComponent(masIconBg1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(36, 36, 36)
                        .addComponent(imageIcon2, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(40, Short.MAX_VALUE))
        );
        addMemoriesLayout.setVerticalGroup(
            addMemoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addMemoriesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(recuerdoTitle1, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(imgBg1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(addMemoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addMemoriesLayout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addComponent(imageIcon2, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(addMemoriesLayout.createSequentialGroup()
                        .addGap(44, 44, 44)
                        .addComponent(homeIcon2, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(addMemoriesLayout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(masIconBg1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        content.addTab("tab3", addMemories);

        allMyMemories.setBackground(new java.awt.Color(28, 30, 47));

        recuerdoTitle2.setFont(new java.awt.Font("Trebuchet MS", 0, 48)); // NOI18N
        recuerdoTitle2.setForeground(new java.awt.Color(190, 190, 190));
        recuerdoTitle2.setText("All my memories");

        imgBg2.setBackground(new java.awt.Color(57, 58, 95));
        imgBg2.setMaximumSize(new java.awt.Dimension(420, 400));
        imgBg2.setMinimumSize(new java.awt.Dimension(420, 400));

        imagen6.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        imagen6.setForeground(new java.awt.Color(190, 190, 190));
        imagen6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imagen6.setText("image");
        imagen6.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        imagen6.setMaximumSize(new java.awt.Dimension(192, 192));
        imagen6.setMinimumSize(new java.awt.Dimension(192, 192));
        imagen6.setPreferredSize(new java.awt.Dimension(192, 192));

        dateAllMemories.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        dateAllMemories.setForeground(new java.awt.Color(190, 190, 190));
        dateAllMemories.setText("Date");

        titleAllMemories.setFont(new java.awt.Font("Trebuchet MS", 0, 32)); // NOI18N
        titleAllMemories.setForeground(new java.awt.Color(190, 190, 190));
        titleAllMemories.setText("Title");

        descriptionAllMemories.setFont(new java.awt.Font("Trebuchet MS", 0, 18)); // NOI18N
        descriptionAllMemories.setForeground(new java.awt.Color(190, 190, 190));
        descriptionAllMemories.setText("Description");

        emojiesAllMemories.setFont(new java.awt.Font("Segoe UI Emoji", 0, 18)); // NOI18N
        emojiesAllMemories.setForeground(new java.awt.Color(190, 190, 190));
        emojiesAllMemories.setText("Emojies");

        javax.swing.GroupLayout imgBg2Layout = new javax.swing.GroupLayout(imgBg2);
        imgBg2.setLayout(imgBg2Layout);
        imgBg2Layout.setHorizontalGroup(
            imgBg2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(imgBg2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(imgBg2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(imagen6, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                    .addComponent(descriptionAllMemories, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(titleAllMemories, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dateAllMemories, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(emojiesAllMemories, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        imgBg2Layout.setVerticalGroup(
            imgBg2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(imgBg2Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(imagen6, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(titleAllMemories, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(descriptionAllMemories, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(emojiesAllMemories, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(dateAllMemories, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5))
        );

        trashIconBg.setBackground(new java.awt.Color(255, 101, 0));
        trashIconBg.setMaximumSize(new java.awt.Dimension(85, 85));
        trashIconBg.setMinimumSize(new java.awt.Dimension(85, 85));

        trashIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        trashIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/trash.png"))); // NOI18N
        trashIcon.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        trashIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                trashIconMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout trashIconBgLayout = new javax.swing.GroupLayout(trashIconBg);
        trashIconBg.setLayout(trashIconBgLayout);
        trashIconBgLayout.setHorizontalGroup(
            trashIconBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(trashIcon, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
        );
        trashIconBgLayout.setVerticalGroup(
            trashIconBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(trashIcon, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
        );

        homeIcon3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        homeIcon3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/home.png"))); // NOI18N
        homeIcon3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        homeIcon3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                homeIcon3MouseClicked(evt);
            }
        });

        imageIcon3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imageIcon3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/imagen.png"))); // NOI18N
        imageIcon3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        imageIcon3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                imageIcon3MouseClicked(evt);
            }
        });

        flechaIzq.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        flechaIzq.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/flechaDer.png"))); // NOI18N
        flechaIzq.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                flechaIzqMouseClicked(evt);
            }
        });

        flechaDer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        flechaDer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/flechaIzq.png"))); // NOI18N
        flechaDer.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                flechaDerMouseClicked(evt);
            }
        });

        irInicio.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        irInicio.setForeground(new java.awt.Color(190, 190, 190));
        irInicio.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        irInicio.setText("inicio");
        irInicio.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        irInicio.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                irInicioMouseClicked(evt);
            }
        });

        irFin.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        irFin.setForeground(new java.awt.Color(190, 190, 190));
        irFin.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        irFin.setText("fin");
        irFin.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        irFin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                irFinMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout allMyMemoriesLayout = new javax.swing.GroupLayout(allMyMemories);
        allMyMemories.setLayout(allMyMemoriesLayout);
        allMyMemoriesLayout.setHorizontalGroup(
            allMyMemoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(allMyMemoriesLayout.createSequentialGroup()
                .addGroup(allMyMemoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(allMyMemoriesLayout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(recuerdoTitle2, javax.swing.GroupLayout.PREFERRED_SIZE, 383, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(allMyMemoriesLayout.createSequentialGroup()
                        .addGap(91, 91, 91)
                        .addComponent(homeIcon3, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(39, 39, 39)
                        .addComponent(trashIconBg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(36, 36, 36)
                        .addComponent(imageIcon3, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(allMyMemoriesLayout.createSequentialGroup()
                        .addGroup(allMyMemoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(allMyMemoriesLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(flechaIzq, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(allMyMemoriesLayout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addComponent(irInicio, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(imgBg2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(allMyMemoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(allMyMemoriesLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(flechaDer, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(allMyMemoriesLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(irFin, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(12, Short.MAX_VALUE))
        );
        allMyMemoriesLayout.setVerticalGroup(
            allMyMemoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(allMyMemoriesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(recuerdoTitle2, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(allMyMemoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(allMyMemoriesLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(imgBg2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(allMyMemoriesLayout.createSequentialGroup()
                        .addGap(292, 292, 292)
                        .addComponent(flechaIzq, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(irInicio))
                    .addGroup(allMyMemoriesLayout.createSequentialGroup()
                        .addGap(287, 287, 287)
                        .addComponent(flechaDer, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(irFin)))
                .addGroup(allMyMemoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(allMyMemoriesLayout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(imageIcon3, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(allMyMemoriesLayout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(homeIcon3, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(allMyMemoriesLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(trashIconBg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        content.addTab("tab3", allMyMemories);

        background.add(content, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 500, 900));

        getContentPane().add(background, java.awt.BorderLayout.PAGE_START);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void userFieldMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_userFieldMouseClicked
        userField.setText("");  // Cuando el usuario clickea el textfield borra el mensaje
    }//GEN-LAST:event_userFieldMouseClicked

    private void passwdFieldMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_passwdFieldMouseClicked
        passwdField.setText("");  // Cuando el usuario clickea el textfield borra el mensaje
    }//GEN-LAST:event_passwdFieldMouseClicked

    private void signUpTitleMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_signUpTitleMouseClicked
        content.setSelectedIndex(1); // Muestra la seccion de crear cuenta "signUp"
        currentTab = 1; // Almacena la pestana actual
        userField1.setText("Username");  // Reestablece el mensaje en el textfield
        passwdField1.setText("Password");
    }//GEN-LAST:event_signUpTitleMouseClicked

    private void signInTitleMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_signInTitleMouseClicked
        content.setSelectedIndex(0); // Muestra la seccion de iniciar sesion "signIn"
        currentTab = 0; // Almacena la pestana actual
        userField.setText("Username");  // Reestablece el mensaje en el textfield
        passwdField.setText("Password");
    }//GEN-LAST:event_signInTitleMouseClicked

    private void userField1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_userField1MouseClicked
        userField1.setText("");  // Cuando el usuario clickea el textfield borra el mensaje
    }//GEN-LAST:event_userField1MouseClicked

    private void passwdField1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_passwdField1MouseClicked
        passwdField1.setText("");  // Cuando el usuario clickea el textfield borra el mensaje
    }//GEN-LAST:event_passwdField1MouseClicked

    private void logInTitleMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logInTitleMouseClicked
        signIn();
    }//GEN-LAST:event_logInTitleMouseClicked

    private void LoginBttonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_LoginBttonMouseClicked
        signIn();
    }//GEN-LAST:event_LoginBttonMouseClicked

    private void signUpBttonTitleMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_signUpBttonTitleMouseClicked
        signUp();
    }//GEN-LAST:event_signUpBttonTitleMouseClicked

    private void signUpBttonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_signUpBttonMouseClicked
        signUp();
    }//GEN-LAST:event_signUpBttonMouseClicked

    private void masIconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_masIconMouseClicked
        currentTab = 3; // Muestra la seccion "addMemories"
        content.setSelectedIndex(currentTab);

    }//GEN-LAST:event_masIconMouseClicked

    private void titleFieldMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_titleFieldMouseClicked
        titleField.setText("");
    }//GEN-LAST:event_titleFieldMouseClicked

    private void descriptionFieldMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_descriptionFieldMouseClicked
        descriptionField.setText("");
    }//GEN-LAST:event_descriptionFieldMouseClicked

    private void homeIconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_homeIconMouseClicked
        currentTab = 2; // Muestra la seccion "memories"
        content.setSelectedIndex(currentTab);
        refreshMemories();
    }//GEN-LAST:event_homeIconMouseClicked

    private void imageIconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imageIconMouseClicked
        currentTab = 4; // Muestra la seccion "allMyMemories"
        content.setSelectedIndex(currentTab);
        cargarPrevisualizacion(330, actual, titleAllMemories, descriptionAllMemories, emojiesAllMemories, dateAllMemories, imagen6);
    }//GEN-LAST:event_imageIconMouseClicked

    private void masIcon1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_masIcon1MouseClicked
        addNodes();
        refreshMemories();
    }//GEN-LAST:event_masIcon1MouseClicked

    private void homeIcon2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_homeIcon2MouseClicked
        currentTab = 2; // Muestra la seccion "memories"
        content.setSelectedIndex(currentTab);
        refreshMemories();
    }//GEN-LAST:event_homeIcon2MouseClicked

    private void imageIcon2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imageIcon2MouseClicked
        currentTab = 4; // Muestra la seccion "allMyMemories"
        content.setSelectedIndex(currentTab);
        cargarPrevisualizacion(330, actual, titleAllMemories, descriptionAllMemories, emojiesAllMemories, dateAllMemories, imagen6);
    }//GEN-LAST:event_imageIcon2MouseClicked

    private void trashIconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_trashIconMouseClicked
        deleteActual();
    }//GEN-LAST:event_trashIconMouseClicked

    private void homeIcon3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_homeIcon3MouseClicked
        currentTab = 2; // Muestra la seccion "memories"
        content.setSelectedIndex(currentTab);
        refreshMemories();
    }//GEN-LAST:event_homeIcon3MouseClicked

    private void imageIcon3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imageIcon3MouseClicked
        currentTab = 4; // Muestra la seccion "allMyMemories"
        content.setSelectedIndex(currentTab);
        cargarPrevisualizacion(330, actual, titleAllMemories, descriptionAllMemories, emojiesAllMemories, dateAllMemories, imagen6);
    }//GEN-LAST:event_imageIcon3MouseClicked

    private void flechaDerMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_flechaDerMouseClicked
        avanzar();
    }//GEN-LAST:event_flechaDerMouseClicked

    private void flechaIzqMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_flechaIzqMouseClicked
        retroceder();
    }//GEN-LAST:event_flechaIzqMouseClicked

    private void imagen1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imagen1MouseClicked
        actual = fin;
        cargarPrevisualizacion(330, actual, titleAllMemories, descriptionAllMemories, emojiesAllMemories, dateAllMemories, imagen6);
        currentTab = 4; // Muestra la seccion "allMyMemories"
        content.setSelectedIndex(currentTab);
    }//GEN-LAST:event_imagen1MouseClicked

    private void imagen2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imagen2MouseClicked
        actual = fin.getAnterior();
        cargarPrevisualizacion(330, actual, titleAllMemories, descriptionAllMemories, emojiesAllMemories, dateAllMemories, imagen6);
        currentTab = 4; // Muestra la seccion "allMyMemories"
        content.setSelectedIndex(currentTab);
    }//GEN-LAST:event_imagen2MouseClicked

    private void imagen3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imagen3MouseClicked
        actual = fin.getAnterior().getAnterior();
        cargarPrevisualizacion(330, actual, titleAllMemories, descriptionAllMemories, emojiesAllMemories, dateAllMemories, imagen6);
        currentTab = 4; // Muestra la seccion "allMyMemories"
        content.setSelectedIndex(currentTab);
    }//GEN-LAST:event_imagen3MouseClicked

    private void imagen4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imagen4MouseClicked
        actual = fin.getAnterior().getAnterior().getAnterior();
        cargarPrevisualizacion(330, actual, titleAllMemories, descriptionAllMemories, emojiesAllMemories, dateAllMemories, imagen6);
        currentTab = 4; // Muestra la seccion "allMyMemories"
        content.setSelectedIndex(currentTab);
    }//GEN-LAST:event_imagen4MouseClicked

    private void imagen5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imagen5MouseClicked
        actual = fin.getAnterior().getAnterior().getAnterior().getAnterior();
        cargarPrevisualizacion(330, actual, titleAllMemories, descriptionAllMemories, emojiesAllMemories, dateAllMemories, imagen6);
        currentTab = 4; // Muestra la seccion "allMyMemories"
        content.setSelectedIndex(currentTab);
    }//GEN-LAST:event_imagen5MouseClicked

    private void irInicioMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_irInicioMouseClicked
        irInicio();
    }//GEN-LAST:event_irInicioMouseClicked

    private void irFinMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_irFinMouseClicked
        irFin();
    }//GEN-LAST:event_irFinMouseClicked

    private void emojisFieldMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_emojisFieldMouseClicked
        emojisField.setText("");
    }//GEN-LAST:event_emojisFieldMouseClicked

    private void refreshIconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refreshIconMouseClicked
        suggestions();
    }//GEN-LAST:event_refreshIconMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Front.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Front.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Front.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Front.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new Front().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel LoginBtton;
    private javax.swing.JPanel addMemories;
    private javax.swing.JPanel allMyMemories;
    private javax.swing.JPanel background;
    private javax.swing.JTabbedPane content;
    private javax.swing.JLabel dateAllMemories;
    private javax.swing.JLabel dateMemories;
    private javax.swing.JLabel descriptionAllMemories;
    private javax.swing.JTextField descriptionField;
    private javax.swing.JLabel descriptionMemories;
    private javax.swing.JLabel emojiesAllMemories;
    private javax.swing.JLabel emojiesMemories;
    private javax.swing.JTextField emojisField;
    private javax.swing.JLabel flechaDer;
    private javax.swing.JLabel flechaIzq;
    private javax.swing.JLabel homeIcon;
    private javax.swing.JLabel homeIcon2;
    private javax.swing.JLabel homeIcon3;
    private javax.swing.JLabel imageIcon;
    private javax.swing.JLabel imageIcon2;
    private javax.swing.JLabel imageIcon3;
    private javax.swing.JLabel imagen;
    private javax.swing.JLabel imagen1;
    private javax.swing.JLabel imagen2;
    private javax.swing.JLabel imagen3;
    private javax.swing.JLabel imagen4;
    private javax.swing.JLabel imagen5;
    private javax.swing.JLabel imagen6;
    private javax.swing.JPanel imgBg;
    private javax.swing.JPanel imgBg1;
    private javax.swing.JPanel imgBg2;
    private javax.swing.JLabel irFin;
    private javax.swing.JLabel irInicio;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel logInTitle;
    private javax.swing.JLabel masIcon;
    private javax.swing.JLabel masIcon1;
    private javax.swing.JPanel masIconBg;
    private javax.swing.JPanel masIconBg1;
    private javax.swing.JPanel memories;
    private java.awt.TextField passwdField;
    private java.awt.TextField passwdField1;
    private javax.swing.JLabel recuerdoTitle;
    private javax.swing.JLabel recuerdoTitle1;
    private javax.swing.JLabel recuerdoTitle2;
    private javax.swing.JLabel refreshIcon;
    private javax.swing.JPanel signIn;
    private javax.swing.JPanel signInBg;
    private javax.swing.JLabel signInTitle;
    private javax.swing.JPanel signUp;
    private javax.swing.JPanel signUpBg;
    private javax.swing.JPanel signUpBtton;
    private javax.swing.JLabel signUpBttonTitle;
    private javax.swing.JLabel signUpTitle;
    private javax.swing.JLabel suggestionsDescription;
    private javax.swing.JLabel suggestionsTitle;
    private javax.swing.JLabel title;
    private javax.swing.JLabel title1;
    private javax.swing.JLabel titleAllMemories;
    private javax.swing.JTextField titleField;
    private javax.swing.JLabel titleMemories;
    private javax.swing.JPanel topBar;
    private javax.swing.JLabel trashIcon;
    private javax.swing.JPanel trashIconBg;
    private java.awt.TextField userField;
    private java.awt.TextField userField1;
    // End of variables declaration//GEN-END:variables
}
