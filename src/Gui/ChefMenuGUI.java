package Gui;

import controller.GestioneCorsoController;
import controller.VisualizzaCorsiController;
import dao.*;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import model.Chef;
import model.Ricetta;
import service.GestioneRicette;
import util.StyleHelper;


public class ChefMenuGUI {
    private Chef chefLoggato;
    private VisualizzaCorsiController corsiController;
    private GestioneCorsoController gestioneCorsoController;
    private double xOffset = 0;
    private double yOffset = 0;
    private StackPane menuRoot;
    private StackPane contentPane;

    // Servizi
    private GestioneRicette gestioneRicette;

    // Menu hamburger per dashboard laterale
    private VBox sidebar;
    private Button hamburgerBtn;
    private boolean sidebarVisible = true;

    public void setChefLoggato(Chef chef) {
        this.chefLoggato = chef;
    }

    public void setController(VisualizzaCorsiController controller, GestioneCorsoController gestione) {
        this.corsiController = controller;
        this.gestioneCorsoController = gestione;
        initializeServices();
    }

    private void initializeServices() {
        try {
            RicettaDAO ricettaDAO = new RicettaDAO();
            UsaDAO usaDAO = new UsaDAO();
            IngredienteDAO ingredienteDAO = new IngredienteDAO();
            this.gestioneRicette = new GestioneRicette(ricettaDAO, usaDAO, ingredienteDAO);
        } catch (Exception e) {
            System.err.println("Errore nell'inizializzazione servizi: " + e.getMessage());
        }
    }

    public StackPane getRoot() {
        return menuRoot;
    }

    public void start(Stage stage) {
        if (chefLoggato == null || corsiController == null || gestioneCorsoController == null) {
            throw new IllegalStateException("Chef e controller devono essere impostati prima di start().");
        }

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Menu Chef - " + chefLoggato.getUsername());

        menuRoot = new StackPane();
        createBackground(menuRoot);

        HBox mainLayout = new HBox();
        mainLayout.setSpacing(0); 

  
        sidebar = new VBox(20);
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setPadding(new Insets(30, 15, 30, 15));
        sidebar.setStyle("-fx-background-color: #FF6600;");
        sidebar.setPrefWidth(240); 
        sidebar.setMinWidth(240);
        sidebar.setMaxWidth(240);

        Label welcomeLabel = new Label("Benvenuto\n" + chefLoggato.getUsername());
        welcomeLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
        welcomeLabel.setTextFill(Color.WHITE);
        welcomeLabel.setWrapText(true);
        welcomeLabel.setAlignment(Pos.CENTER);

        //Pulsanti sidebar 
        Button corsiBtn = createSidebarButton("📚 Visualizza Corsi");
        corsiBtn.setOnAction(e -> apriVisualizzaCorsi());

        Button creaCorsoBtn = createSidebarButton("➕ Crea Corso");
        creaCorsoBtn.setOnAction(e -> apriCreaCorso());

        Button ricetteBtn = createSidebarButton("📖 Gestisci Ricette");
        ricetteBtn.setOnAction(e -> apriVisualizzaRicette());

        Button creaRicettaBtn = createSidebarButton("✨ Crea Ricetta");
        creaRicettaBtn.setOnAction(e -> apriCreaRicettaDiretta());

        Button eliminaBtn = createSidebarButton("🗑️ Elimina Account");
        eliminaBtn.setOnAction(e -> eliminaAccount(stage));

        Button logoutBtn = createSidebarButton("🚪 Logout");
        logoutBtn.setOnAction(e -> stage.close());

        sidebar.getChildren().addAll(
            welcomeLabel, 
            corsiBtn, 
            creaCorsoBtn, 
            ricetteBtn, 
            creaRicettaBtn,
            eliminaBtn, 
            logoutBtn
        );

  
        contentPane = new StackPane();
        contentPane.setStyle("-fx-background-color: #FFFFFF;");
        HBox.setHgrow(contentPane, Priority.ALWAYS);

       
        hamburgerBtn = new Button("☰");
        hamburgerBtn.setPrefSize(45, 45);
        hamburgerBtn.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
        hamburgerBtn.setTextFill(Color.WHITE);
        hamburgerBtn.setStyle("-fx-background-color: " + StyleHelper.PRIMARY_ORANGE + "; " +
            "-fx-background-radius: 25; -fx-cursor: hand;");
        hamburgerBtn.setOnAction(e -> toggleSidebar());

        DropShadow shadow = new DropShadow();
        shadow.setRadius(8);
        shadow.setColor(Color.web("#000000", 0.3));
        hamburgerBtn.setEffect(shadow);

        mostraBenvenutoIniziale();

        mainLayout.getChildren().addAll(sidebar, contentPane);
        menuRoot.getChildren().add(mainLayout);

      
        menuRoot.getChildren().add(hamburgerBtn);
        StackPane.setAlignment(hamburgerBtn, Pos.TOP_LEFT);
        StackPane.setMargin(hamburgerBtn, new Insets(12));

        // Bottoni finestra
        HBox windowButtons = createWindowButtons(stage);
        menuRoot.getChildren().add(windowButtons);
        StackPane.setAlignment(windowButtons, Pos.TOP_RIGHT);
        StackPane.setMargin(windowButtons, new Insets(8));

        makeDraggable(menuRoot, stage);

        Scene scene = new Scene(menuRoot, 1200, 800);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.show();
    }

   
    private void toggleSidebar() {
        TranslateTransition transition = new TranslateTransition(Duration.millis(250), sidebar);

        if (sidebarVisible) {
       
            transition.setToX(-sidebar.getWidth());
            hamburgerBtn.setText("☰");
            hamburgerBtn.setTooltip(new Tooltip("Mostra menu"));
        } else {
            
            transition.setToX(0);
            hamburgerBtn.setText("✕");
            hamburgerBtn.setTooltip(new Tooltip("Nascondi menu"));
        }

        transition.setOnFinished(e -> sidebarVisible = !sidebarVisible);
        transition.play();
    }

    private void mostraBenvenutoIniziale() {
        VBox benvenutoBox = new VBox(25);
        benvenutoBox.setAlignment(Pos.CENTER);
        benvenutoBox.setPadding(new Insets(50));

        Label titoloLabel = new Label("🍽️ UninaFoodLab");
        titoloLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 36));
        titoloLabel.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        Label sottotitoloLabel = new Label("Gestione Corsi di Cucina");
        sottotitoloLabel.setFont(Font.font("Roboto", FontWeight.NORMAL, 18));
        sottotitoloLabel.setTextFill(Color.GRAY);

        Label benvenutoLabel = new Label("Benvenuto, Chef " + chefLoggato.getUsername() + "!");
        benvenutoLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 24));
        benvenutoLabel.setTextFill(Color.web(StyleHelper.INFO_BLUE));

        Label istruzioniLabel = new Label("💡 Usa il menu laterale per gestire corsi e ricette");
        istruzioniLabel.setFont(Font.font("Roboto", FontWeight.NORMAL, 14));
        istruzioniLabel.setTextFill(Color.GRAY);

        Label istruzioni2Label = new Label("☰ Clicca l'icona hamburger per nascondere/mostrare il menu");
        istruzioni2Label.setFont(Font.font("Roboto", FontWeight.NORMAL, 12));
        istruzioni2Label.setTextFill(Color.web(StyleHelper.INFO_BLUE));

        benvenutoBox.getChildren().addAll(
            titoloLabel, sottotitoloLabel, benvenutoLabel, istruzioniLabel, istruzioni2Label);

        contentPane.getChildren().clear();
        contentPane.getChildren().add(benvenutoBox);
    }

    private void createBackground(StackPane root) {
        LinearGradient gradient = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#FF9966")),
            new Stop(1, Color.web("#FFCC99"))
        );

        Region background = new Region();
        background.setBackground(new Background(new BackgroundFill(gradient, null, null)));
        background.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        background.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        root.getChildren().add(background);
    }

    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(200);
        btn.setPrefHeight(40);
        btn.setFont(Font.font("Roboto", FontWeight.BOLD, 13));
        btn.setTextFill(Color.WHITE);
        btn.setStyle("-fx-background-color: #FF8533; -fx-background-radius: 10; -fx-cursor: hand;");

        DropShadow shadow = new DropShadow();
        shadow.setRadius(4);
        shadow.setColor(Color.web("#000000", 0.2));
        btn.setEffect(shadow);

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #FF6600; -fx-background-radius: 10; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #FF8533; -fx-background-radius: 10; -fx-cursor: hand;"));

        return btn;
    }

    private HBox createWindowButtons(Stage stage) {
        Button closeButton = new Button("✕");
        Button minimizeButton = new Button("−");
        Button maximizeButton = new Button("□");

        Button[] buttons = { minimizeButton, maximizeButton, closeButton };
        for (Button btn : buttons) {
            btn.setPrefSize(30, 30);
            btn.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
            btn.setTextFill(Color.WHITE);
            btn.setStyle("-fx-background-color: rgba(255,140,0,0.7); -fx-background-radius: 15; -fx-cursor: hand;");
            btn.setFocusTraversable(false);
        }

        closeButton.setOnAction(e -> stage.close());
        minimizeButton.setOnAction(e -> stage.setIconified(true));
        maximizeButton.setOnAction(e -> stage.setMaximized(!stage.isMaximized()));

        HBox box = new HBox(3, minimizeButton, maximizeButton, closeButton);
        box.setAlignment(Pos.TOP_RIGHT);
        box.setPickOnBounds(false);
        return box;
    }

    private void apriVisualizzaCorsi() {
        try {
            VisualizzaCorsiGUI corsiGUI = new VisualizzaCorsiGUI();
            corsiGUI.setControllers(corsiController, gestioneCorsoController, contentPane);
            contentPane.getChildren().clear();
            contentPane.getChildren().add(corsiGUI.getRoot());
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Errore aprendo i corsi: " + ex.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    private void apriCreaCorso() {
        try {
            CreaCorsoGUI gui = new CreaCorsoGUI();
            gui.setController(gestioneCorsoController);
            contentPane.getChildren().clear();
            contentPane.getChildren().add(gui.getRoot());
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Errore aprendo il form Crea Corso: " + ex.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    private void apriVisualizzaRicette() {
        try {
            VisualizzaRicetteGUI ricetteGUI = new VisualizzaRicetteGUI(gestioneRicette);
            
          
            contentPane.getChildren().clear();
            contentPane.getChildren().add(ricetteGUI.getRoot());
            
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, 
                "Errore aprendo le ricette: " + ex.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }


    private void apriCreaRicettaDiretta() {
        try {
            CreaRicettaGUI creaRicettaGUI = new CreaRicettaGUI(gestioneRicette);
            Ricetta nuovaRicetta = creaRicettaGUI.showAndReturn();

            if (nuovaRicetta != null) {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Successo");
                successAlert.setHeaderText("Ricetta creata!");
                successAlert.setContentText("La ricetta '" + nuovaRicetta.getNome() + "' è stata creata con successo.");
                successAlert.showAndWait();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Errore nella creazione ricetta: " + ex.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    private void eliminaAccount(Stage stage) {
        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
        conferma.setHeaderText("Vuoi eliminare definitivamente il tuo account?");
        conferma.setContentText("Questa operazione non può essere annullata.");
        conferma.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setHeaderText("Account eliminato!");
                    info.showAndWait();
                    stage.close();
                } catch (Exception ex) {
                    Alert errore = new Alert(Alert.AlertType.ERROR);
                    errore.setHeaderText("Errore nell'eliminazione: " + ex.getMessage());
                    errore.showAndWait();
                }
            }
        });
    }

    private void makeDraggable(StackPane root, Stage stage) {
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }
}
