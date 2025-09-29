package Gui;

import controller.GestioneCorsoController;
import controller.VisualizzaCorsiController;
import controller.VisualizzaRicetteController;
import dao.RicettaDAO;
import dao.UsaDAO;
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
import service.GestioneRicette;

public class ChefMenuGUI {

    private Chef chefLoggato;
    private VisualizzaCorsiController corsiController;
    private double xOffset = 0;
    private double yOffset = 0;
    private StackPane menuRoot;

    // Area contenuti a destra
    private StackPane contentPane;

    public void setChefLoggato(Chef chef) {
        this.chefLoggato = chef;
    }

    public void setController(VisualizzaCorsiController controller) {
        this.corsiController = controller;
    }

    public StackPane getRoot() {
        return menuRoot;
    }

    public void start(Stage stage) {
        if (chefLoggato == null || corsiController == null) {
            throw new IllegalStateException("Chef e controller devono essere impostati prima di start().");
        }

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Menu Chef - " + chefLoggato.getUsername());

        menuRoot = new StackPane();
        createBackground(menuRoot);

        // --- Layout principale: HBox (sidebar sx + contenuto dx) ---
        HBox mainLayout = new HBox();

        // --- Sidebar sinistra ---
        VBox sidebar = new VBox(20);
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setPadding(new Insets(30, 10, 30, 10));
        sidebar.setStyle("-fx-background-color: #FF6600;");
        sidebar.setPrefWidth(200);

        Label welcomeLabel = new Label("Benvenuto, " + chefLoggato.getUsername());
        welcomeLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
        welcomeLabel.setTextFill(Color.WHITE);
        welcomeLabel.setWrapText(true);
        welcomeLabel.setAlignment(Pos.CENTER);

        Button corsiBtn = createSidebarButton("Corsi");
        corsiBtn.setOnAction(e -> apriVisualizzaCorsi());

        Button ricetteBtn = createSidebarButton("Ricette");
        ricetteBtn.setOnAction(e -> apriVisualizzaRicette());

        Button eliminaBtn = createSidebarButton("Elimina");
        eliminaBtn.setOnAction(e -> eliminaAccount(stage));

        Button logoutBtn = createSidebarButton("Logout");
        logoutBtn.setOnAction(e -> stage.close());

        sidebar.getChildren().addAll(welcomeLabel, corsiBtn, ricetteBtn, eliminaBtn, logoutBtn);

        // --- Area contenuti a destra ---
        contentPane = new StackPane();
        contentPane.setStyle("-fx-background-color: #FFFFFF;");
        HBox.setHgrow(contentPane, Priority.ALWAYS);

        // Contenitore principale HBox
        mainLayout.getChildren().addAll(sidebar, contentPane);

        menuRoot.getChildren().add(mainLayout);

        // Bottone chiusura/minimizza/maximizza
        HBox windowButtons = createWindowButtons(stage);
        menuRoot.getChildren().add(windowButtons);
        StackPane.setAlignment(windowButtons, Pos.TOP_RIGHT);
        StackPane.setMargin(windowButtons, new Insets(10));

        makeDraggable(menuRoot, stage);

        Scene scene = new Scene(menuRoot, 1200, 800); // finestra più grande e adattabile
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

        Button[] buttons = { minimizeButton, maximizeButton, closeButton };
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
            GestioneCorsoController gestioneCorsoController =
                    new GestioneCorsoController(corsiController.getGestioneCorsi(), null);
            gestioneCorsoController.setChefLoggato(corsiController.getChefLoggato());

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

    private void apriVisualizzaRicette() {
        try {
            RicettaDAO ricettaDAO = new RicettaDAO();
            UsaDAO usaDAO = new UsaDAO();

            GestioneRicette gestioneRicette = new GestioneRicette(ricettaDAO, usaDAO);

            VisualizzaRicetteController controller = new VisualizzaRicetteController(gestioneRicette);

            VisualizzaRicetteGUI gui = new VisualizzaRicetteGUI();
            gui.setController(controller, contentPane);

            contentPane.getChildren().clear();
            contentPane.getChildren().add(gui.getRoot());

        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Errore aprendo le ricette: " + ex.getMessage(), ButtonType.OK);
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
