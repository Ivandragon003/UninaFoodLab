package Gui;

import controller.GestioneCorsoController;
import controller.VisualizzaCorsiController;
import controller.RicettaController;
import controller.IngredienteController;
import controller.ChefController;
import dao.IngredienteDAO;
import dao.ChefDAO;
import dao.TieneDAO;
import service.GestioneIngrediente;
import service.GestioneChef;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
import util.StyleHelper;

public class ChefMenuGUI {

    private Chef chefLoggato;
    private VisualizzaCorsiController corsiController;
    private GestioneCorsoController gestioneCorsoController;
    private RicettaController ricettaController;
    private IngredienteController ingredienteController;
    private ChefController chefController;

    private StackPane menuRoot;
    private StackPane contentPane;
    private VBox sidebar;
    private Button hamburgerBtn;
    private boolean sidebarVisible = true;
    private Stage currentStage;

    private double xOffset = 0;
    private double yOffset = 0;

    public void setChefLoggato(Chef chef) {
        this.chefLoggato = chef;
    }

    public void setControllers(VisualizzaCorsiController corsiController,
                               GestioneCorsoController gestioneCorsoController,
                               RicettaController ricettaController) {
        this.corsiController = corsiController;
        this.gestioneCorsoController = gestioneCorsoController;
        this.ricettaController = ricettaController;
    }

    public StackPane getRoot() {
        return menuRoot;
    }

    public void start(Stage stage) {
        if (chefLoggato == null || corsiController == null ||
            gestioneCorsoController == null || ricettaController == null) {
            throw new IllegalStateException("Chef e controller devono essere impostati prima di start()");
        }

        initializeControllers();

        this.currentStage = stage;
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Menu Chef - " + chefLoggato.getUsername());

        menuRoot = new StackPane();
        createBackground(menuRoot);

        HBox mainLayout = new HBox();
        mainLayout.setSpacing(0);

        sidebar = createSidebar(stage);

        contentPane = new StackPane();
        contentPane.setStyle("-fx-background-color: #FFFFFF;");
        HBox.setHgrow(contentPane, Priority.ALWAYS);

        hamburgerBtn = createHamburgerButton();

        mostraBenvenutoIniziale();

        mainLayout.getChildren().addAll(sidebar, contentPane);
        menuRoot.getChildren().add(mainLayout);

        menuRoot.getChildren().add(hamburgerBtn);
        StackPane.setAlignment(hamburgerBtn, Pos.TOP_LEFT);
        StackPane.setMargin(hamburgerBtn, new Insets(12));

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

    private void initializeControllers() {
        try {
            if (ingredienteController == null) {
                ingredienteController = new IngredienteController(
                        new GestioneIngrediente(new IngredienteDAO())
                );
            }
            if (chefController == null) {
                chefController = new ChefController(
                        new GestioneChef(new ChefDAO(), new TieneDAO())
                );
            }
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Impossibile inizializzare i controller: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private VBox createSidebar(Stage stage) {
        VBox sidebar = new VBox(20);
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setPadding(new Insets(30, 15, 30, 15));
        sidebar.setStyle("-fx-background-color: " + StyleHelper.PRIMARY_ORANGE + ";");
        sidebar.setPrefWidth(240);

        Label welcomeLabel = new Label("Benvenuto\n" + chefLoggato.getUsername());
        welcomeLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
        welcomeLabel.setTextFill(Color.WHITE);
        welcomeLabel.setWrapText(true);
        welcomeLabel.setAlignment(Pos.CENTER);

        sidebar.getChildren().add(welcomeLabel);

        sidebar.getChildren().addAll(
                createSidebarButton("📚 Visualizza Corsi", this::apriVisualizzaCorsi),
                createSidebarButton("➕ Crea Corso", this::apriCreaCorso),
                createSidebarButton("📖 Gestisci Ricette", this::apriVisualizzaRicette),
                createSidebarButton("✨ Crea Ricetta", this::apriCreaRicetta),
                createSidebarButton("🗑️ Elimina Account", this::eliminaAccount),
                createSidebarButton("🚪 Logout", stage::close)
        );

        return sidebar;
    }

    private Button createSidebarButton(String text, Runnable action) {
        Button btn = StyleHelper.createPrimaryButton(text);
        btn.setPrefWidth(200);
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private Button createHamburgerButton() {
        Button btn = new Button("☰");
        btn.setPrefSize(45, 45);
        btn.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
        btn.setTextFill(Color.WHITE);
        btn.setStyle("-fx-background-color: " + StyleHelper.PRIMARY_ORANGE + "; -fx-background-radius: 25; -fx-cursor: hand;");
        btn.setOnAction(e -> toggleSidebar());

        DropShadow shadow = new DropShadow();
        shadow.setRadius(8);
        shadow.setColor(Color.web("#000000", 0.3));
        btn.setEffect(shadow);

        return btn;
    }

    private void toggleSidebar() {
        TranslateTransition transition = new TranslateTransition(Duration.millis(250), sidebar);
        if (sidebarVisible) {
            transition.setToX(-sidebar.getWidth());
            sidebarVisible = false;
        } else {
            transition.setToX(0);
            sidebarVisible = true;
        }
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

        benvenutoBox.getChildren().addAll(titoloLabel, sottotitoloLabel, benvenutoLabel, istruzioniLabel, istruzioni2Label);

        showInContentPane(benvenutoBox);
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

    private HBox createWindowButtons(Stage stage) {
        Button closeButton = StyleHelper.createWindowButton("✕", () -> stage.close());
        Button minimizeButton = StyleHelper.createWindowButton("−", () -> stage.setIconified(true));
        Button maximizeButton = StyleHelper.createWindowButton("□", () -> stage.setMaximized(!stage.isMaximized()));

        HBox box = new HBox(3, minimizeButton, maximizeButton, closeButton);
        box.setAlignment(Pos.TOP_RIGHT);
        box.setPickOnBounds(false);
        return box;
    }

    private void apriVisualizzaCorsi() {
        try {
            VisualizzaCorsiGUI corsiGUI = new VisualizzaCorsiGUI();
            corsiGUI.setControllers(corsiController, gestioneCorsoController, contentPane);
            showInContentPane(corsiGUI.getRoot());
        } catch (Exception ex) {
            StyleHelper.showErrorDialog("Errore", "Errore nell'apertura corsi: " + ex.getMessage());
        }
    }

    private void apriCreaCorso() {
        try {
            CreaCorsoGUI gui = new CreaCorsoGUI(
                    gestioneCorsoController,
                    chefController,
                    ricettaController
            );
            showInContentPane(gui.getRoot());
        } catch (Exception ex) {
            StyleHelper.showErrorDialog("Errore", "Errore nell'apertura creazione corso: " + ex.getMessage());
        }
    }

    private void apriVisualizzaRicette() {
        try {
            VisualizzaRicetteGUI ricetteGUI = new VisualizzaRicetteGUI(ricettaController, ingredienteController);
            showInContentPane(ricetteGUI.getRoot());
        } catch (Exception ex) {
            StyleHelper.showErrorDialog("Errore", "Errore nell'apertura ricette: " + ex.getMessage());
        }
    }

    private void apriCreaRicetta() {
        try {
            CreaRicettaGUI creaGUI = new CreaRicettaGUI(ricettaController, ingredienteController);
            Ricetta nuovaRicetta = creaGUI.showAndReturn();
            if (nuovaRicetta != null) {
                StyleHelper.showSuccessDialog("Successo", "Ricetta '" + nuovaRicetta.getNome() + "' creata con successo");
            }
        } catch (Exception ex) {
            StyleHelper.showErrorDialog("Errore", "Errore nella creazione ricetta: " + ex.getMessage());
        }
    }

    private void eliminaAccount() {
        StyleHelper.showConfirmationDialog(
                "Conferma Eliminazione",
                "Eliminare definitivamente l'account? Questa operazione non può essere annullata.",
                () -> {
                    try {
                        chefController.eliminaAccount(chefLoggato);
                        StyleHelper.showSuccessDialog("Account eliminato", "Account eliminato con successo");
                        currentStage.close();
                    } catch (Exception ex) {
                        StyleHelper.showErrorDialog("Errore", "Errore nell'eliminazione: " + ex.getMessage());
                    }
                }
        );
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

    private void showInContentPane(Node guiRoot) {
        contentPane.getChildren().setAll(guiRoot);
    }
}
