package Gui;

import controller.GestioneCorsoController;
import controller.VisualizzaCorsiController;
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
import model.Chef;

public class ChefMenuGUI {

    private Chef chefLoggato;
    private VisualizzaCorsiController corsiController;
    private GestioneCorsoController gestioneCorsoController;
    private double xOffset = 0;
    private double yOffset = 0;
    private StackPane menuRoot;
    private StackPane contentPane;

    public void setChefLoggato(Chef chef) {
        this.chefLoggato = chef;
    }

    public void setController(VisualizzaCorsiController controller, GestioneCorsoController gestione) {
        this.corsiController = controller;
        this.gestioneCorsoController = gestione;
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

        VBox sidebar = new VBox(20);
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setPadding(new Insets(30, 10, 30, 10));
        sidebar.setStyle("-fx-background-color: #FF6600;");
        sidebar.setPrefWidth(200);

        Label welcomeLabel = new Label("Benvenuto, " + chefLoggato.getUsername());
        welcomeLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
        welcomeLabel.setTextFill(Color.WHITE);

        Button corsiBtn = createSidebarButton("Visualizza corsi");
        corsiBtn.setOnAction(e -> apriVisualizzaCorsi());

        Button creaCorsoBtn = createSidebarButton("Crea corso");
        creaCorsoBtn.setOnAction(e -> apriCreaCorso());

        Button ricetteBtn = createSidebarButton("Ricette");
        ricetteBtn.setOnAction(e -> apriVisualizzaRicette());

        Button eliminaBtn = createSidebarButton("Elimina");
        eliminaBtn.setOnAction(e -> eliminaAccount(stage));

        Button logoutBtn = createSidebarButton("Logout");
        logoutBtn.setOnAction(e -> stage.close());

        sidebar.getChildren().addAll(welcomeLabel, corsiBtn, creaCorsoBtn, ricetteBtn, eliminaBtn, logoutBtn);

        contentPane = new StackPane();
        contentPane.setStyle("-fx-background-color: #FFFFFF;");
        HBox.setHgrow(contentPane, Priority.ALWAYS);

        Label welcomeMessage = new Label("👋 Benvenuto nel pannello Chef!\n\nSeleziona un'opzione dal menu laterale per iniziare.");
        welcomeMessage.setFont(Font.font("Roboto", FontWeight.NORMAL, 18));
        welcomeMessage.setTextFill(Color.web("#6C757D"));
        welcomeMessage.setStyle("-fx-text-alignment: center;");
        welcomeMessage.setWrapText(true);
        contentPane.getChildren().add(welcomeMessage);

        mainLayout.getChildren().addAll(sidebar, contentPane);
        menuRoot.getChildren().add(mainLayout);

        HBox windowButtons = createWindowButtons(stage);
        menuRoot.getChildren().add(windowButtons);
        StackPane.setAlignment(windowButtons, Pos.TOP_RIGHT);
        StackPane.setMargin(windowButtons, new Insets(10));

        makeDraggable(menuRoot, stage);

        Scene scene = new Scene(menuRoot, 1200, 800);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.show();
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
        btn.setPrefWidth(160);
        btn.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
        btn.setTextFill(Color.WHITE);
        btn.setStyle("-fx-background-color: #FF8533; -fx-background-radius: 10; -fx-cursor: hand;");
        DropShadow shadow = new DropShadow();
        shadow.setRadius(5);
        shadow.setColor(Color.web("#000000", 0.2));
        btn.setEffect(shadow);

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #FF6600; -fx-background-radius: 10; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #FF8533; -fx-background-radius: 10; -fx-cursor: hand;"));
        return btn;
    }

    private HBox createWindowButtons(Stage stage) {
        Button closeButton = new Button("✕");
        Button minimizeButton = new Button("_");
        Button maximizeButton = new Button("□");

        Button[] buttons = {minimizeButton, maximizeButton, closeButton};
        for (Button btn : buttons) {
            btn.setPrefSize(35, 35);
            btn.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
            btn.setTextFill(Color.WHITE);
            btn.setStyle("-fx-background-color: rgba(255,140,0,0.5); -fx-background-radius: 20; -fx-cursor: hand;");
            btn.setFocusTraversable(false);
        }

        closeButton.setOnAction(e -> stage.close());
        minimizeButton.setOnAction(e -> stage.setIconified(true));
        maximizeButton.setOnAction(e -> stage.setMaximized(!stage.isMaximized()));

        HBox box = new HBox(5, minimizeButton, maximizeButton, closeButton);
        box.setAlignment(Pos.TOP_RIGHT);
        box.setPickOnBounds(false);
        return box;
    }

    private void apriVisualizzaCorsi() {
        try {
            System.out.println("🔍 DEBUG: Avvio caricamento VisualizzaCorsiGUI...");
            VisualizzaCorsiGUI corsiGUI = new VisualizzaCorsiGUI();
            corsiGUI.setControllers(corsiController, gestioneCorsoController, contentPane);
            
            System.out.println("🔍 DEBUG: contentPane children prima: " + contentPane.getChildren().size());
            contentPane.getChildren().setAll(corsiGUI.getRoot());
            System.out.println("🔍 DEBUG: contentPane children dopo: " + contentPane.getChildren().size());
            
        } catch (Exception ex) {
            System.err.println("🔥 ERRORE in apriVisualizzaCorsi: " + ex.getMessage());
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Errore aprendo i corsi: " + ex.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    private void apriCreaCorso() {
        try {
            CreaCorsoGUI creaCorso = new CreaCorsoGUI();
            creaCorso.setController(gestioneCorsoController);
            contentPane.getChildren().clear();
            contentPane.getChildren().add(creaCorso.getRoot());
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Errore aprendo il form Crea Corso: " + ex.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    private void apriVisualizzaRicette() {
        try {
            dao.RicettaDAO ricettaDAO = new dao.RicettaDAO();
            dao.UsaDAO usaDAO = new dao.UsaDAO();
            dao.IngredienteDAO ingredienteDAO = new dao.IngredienteDAO();
            service.GestioneRicette gestioneRicette = new service.GestioneRicette(ricettaDAO, usaDAO, ingredienteDAO);

            VisualizzaRicetteGUI ricetteGUI = new VisualizzaRicetteGUI(gestioneRicette);

            contentPane.getChildren().clear();
            contentPane.getChildren().add(ricetteGUI.getRoot());
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Errore aprendo la schermata ricette: " + ex.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    private void eliminaAccount(Stage stage) {
        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
        conferma.setHeaderText("Vuoi eliminare definitivamente il tuo account?");
        conferma.setContentText("Questa operazione non può essere annullata.");
        conferma.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                stage.close();
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
