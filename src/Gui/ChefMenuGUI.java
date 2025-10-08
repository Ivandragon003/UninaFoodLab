package Gui;

import controller.*;
import dao.*;
import service.*;
import guihelper.StyleHelper;
import exceptions.ValidationException;
import exceptions.DataAccessException;
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

/**
 * Menu principale per lo Chef.
 * Best practice: DAO e Service inizializzati tramite setChefLoggato().
 */
public class ChefMenuGUI {

    // ========== COSTANTI ==========
    private static final double WINDOW_WIDTH = 1200;
    private static final double WINDOW_HEIGHT = 800;
    private static final double SIDEBAR_WIDTH = 240;
    private static final double ANIMATION_DURATION = 250;

    // ========== DATI ==========
    private Chef chefLoggato;

    // ========== DAO ==========
    private CorsoCucinaDAO corsoDAO;
    private ChefDAO chefDAO;
    private TieneDAO tieneDAO;
    private IscrizioneDAO iscrizioneDAO;
    private OnlineDAO onlineDAO;
    private InPresenzaDAO inPresenzaDAO;
    private RicettaDAO ricettaDAO;
    private IngredienteDAO ingredienteDAO;
    private UsaDAO usaDAO;
    private CucinaDAO cucinaDAO;

    // ========== SERVICE ==========
    private GestioneCorsiCucina gestioneCorsiService;
    private GestioneChef gestioneChefService;
    private GestioneRicette gestioneRicetteService;
    private GestioneIngrediente gestioneIngredienteService;
    private GestioneUsa gestioneUsaService;
    private GestioneCucina gestioneCucinaService;

    // ========== CONTROLLER ==========
    private VisualizzaCorsiController corsiController;
    private GestioneCorsoController gestioneCorsoController;
    private RicettaController ricettaController;
    private IngredienteController ingredienteController;
    private ChefController chefController;

    // ========== COMPONENTI UI ==========
    private StackPane menuRoot;
    private StackPane contentPane;
    private VBox sidebar;
    private Button hamburgerBtn;
    private Stage currentStage;
    
    // ========== STATO ==========
    private boolean sidebarVisible = true;
    private double xOffset = 0;
    private double yOffset = 0;

    // ========== COSTRUTTORE VUOTO ==========
    public ChefMenuGUI() {
        // Costruttore vuoto - l'inizializzazione avviene in setChefLoggato()
    }

    /**
     * Imposta lo chef loggato e inizializza tutti i componenti.
     * DEVE essere chiamato PRIMA di start().
     */
    public void setChefLoggato(Chef chef) {
        if (chef == null) {
            throw new IllegalArgumentException("Chef non può essere null");
        }
        this.chefLoggato = chef;
        inizializzaDAO();
        inizializzaServices();
        inizializzaControllers();
    }

    /**
     * Step 1: Inizializza tutti i DAO una sola volta.
     */
    private void inizializzaDAO() {
        this.corsoDAO = new CorsoCucinaDAO();
        this.chefDAO = new ChefDAO();
        this.tieneDAO = new TieneDAO();
        this.iscrizioneDAO = new IscrizioneDAO();
        this.onlineDAO = new OnlineDAO();
        this.inPresenzaDAO = new InPresenzaDAO();
        this.ricettaDAO = new RicettaDAO();
        this.ingredienteDAO = new IngredienteDAO();
        this.usaDAO = new UsaDAO();
        this.cucinaDAO = new CucinaDAO();
    }

    /**
     * Step 2: Inizializza tutti i service passando i DAO.
     */
    private void inizializzaServices() {
        this.gestioneCorsiService = new GestioneCorsiCucina(
            corsoDAO, chefDAO, tieneDAO, iscrizioneDAO, onlineDAO, inPresenzaDAO
        );
        
        this.gestioneChefService = new GestioneChef(chefDAO, tieneDAO);
        this.gestioneRicetteService = new GestioneRicette(ricettaDAO);
        this.gestioneIngredienteService = new GestioneIngrediente(ingredienteDAO);
        this.gestioneUsaService = new GestioneUsa(usaDAO, ingredienteDAO);
        this.gestioneCucinaService = new GestioneCucina(cucinaDAO);
    }

    /**
     * Step 3: Inizializza tutti i controller passando i service.
     */
    private void inizializzaControllers() {
        try {
            this.corsiController = new VisualizzaCorsiController(gestioneCorsiService, chefLoggato);
            
            this.gestioneCorsoController = new GestioneCorsoController(gestioneCorsiService, gestioneChefService);
            this.gestioneCorsoController.setChefLoggato(chefLoggato);
            
            this.ricettaController = new RicettaController(
                gestioneRicetteService, gestioneUsaService, gestioneCucinaService
            );
            
            this.ingredienteController = new IngredienteController(gestioneIngredienteService);
            this.chefController = new ChefController(gestioneChefService);
            this.chefController.setGestioneCorsoController(gestioneCorsoController);
            
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", 
                "Impossibile inizializzare i controller: " + e.getMessage());
            throw new RuntimeException("Errore inizializzazione controller", e);
        }
    }

    // ========== START ==========
    public void start(Stage stage) {
        if (chefLoggato == null) {
            throw new IllegalStateException("Chef non impostato! Chiamare setChefLoggato() prima di start()");
        }
        
        this.currentStage = stage;
        setupStage(stage);
        
        menuRoot = createRootPane(stage);
        setupDraggable(menuRoot, stage);
        
        Scene scene = new Scene(menuRoot, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.show();
    }

    // ========== SETUP STAGE ==========
    private void setupStage(Stage stage) {
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Menu Chef - " + chefLoggato.getUsername());
    }

    // ========== CREAZIONE ROOT PANE ==========
    private StackPane createRootPane(Stage stage) {
        StackPane root = new StackPane();
        createBackground(root);
        HBox mainLayout = createMainLayout(stage);
        hamburgerBtn = createHamburgerButton();
        HBox windowButtons = createWindowButtons(stage);
        
        root.getChildren().addAll(mainLayout, hamburgerBtn, windowButtons);
        StackPane.setAlignment(hamburgerBtn, Pos.TOP_LEFT);
        StackPane.setMargin(hamburgerBtn, new Insets(12));
        StackPane.setAlignment(windowButtons, Pos.TOP_RIGHT);
        StackPane.setMargin(windowButtons, new Insets(8));
        
        return root;
    }

    // ========== CREAZIONE MAIN LAYOUT ==========
    private HBox createMainLayout(Stage stage) {
        HBox mainLayout = new HBox();
        mainLayout.setSpacing(0);

        sidebar = createSidebar(stage);
        contentPane = new StackPane();
        contentPane.setStyle("-fx-background-color: #FFFFFF;");
        HBox.setHgrow(contentPane, Priority.ALWAYS);

        mostraBenvenutoIniziale();
        mainLayout.getChildren().addAll(sidebar, contentPane);
        return mainLayout;
    }

    // ========== CREAZIONE SFONDO ==========
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

    // ========== CREAZIONE SIDEBAR ==========
    private VBox createSidebar(Stage stage) {
        VBox sidebar = new VBox(20);
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setPadding(new Insets(30, 15, 30, 15));
        sidebar.setStyle("-fx-background-color: " + StyleHelper.PRIMARY_ORANGE + ";");
        sidebar.setPrefWidth(SIDEBAR_WIDTH);

        Label welcomeLabel = createWelcomeLabel();
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

    // ========== WELCOME LABEL ==========
    private Label createWelcomeLabel() {
        Label label = new Label("Benvenuto\n" + chefLoggato.getUsername());
        label.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
        label.setTextFill(Color.WHITE);
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    // ========== SIDEBAR BUTTON ==========
    private Button createSidebarButton(String text, Runnable action) {
        Button btn = StyleHelper.createPrimaryButton(text);
        btn.setPrefWidth(200);
        btn.setOnAction(e -> safeRun(action, text));
        return btn;
    }

    // ========== HAMBURGER BUTTON ==========
    private Button createHamburgerButton() {
        Button btn = new Button("☰");
        btn.setPrefSize(45, 45);
        btn.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
        btn.setTextFill(Color.WHITE);
        btn.setStyle(
            "-fx-background-color: " + StyleHelper.PRIMARY_ORANGE + ";" +
            "-fx-background-radius: 25;" +
            "-fx-cursor: hand;"
        );
        btn.setOnAction(e -> toggleSidebar());

        DropShadow shadow = new DropShadow();
        shadow.setRadius(8);
        shadow.setColor(Color.web("#000000", 0.3));
        btn.setEffect(shadow);

        return btn;
    }

    // ========== TOGGLE SIDEBAR ==========
    private void toggleSidebar() {
        TranslateTransition transition = new TranslateTransition(
            Duration.millis(ANIMATION_DURATION), sidebar
        );
        
        if (sidebarVisible) {
            transition.setToX(-sidebar.getWidth());
            sidebarVisible = false;
        } else {
            transition.setToX(0);
            sidebarVisible = true;
        }
        
        transition.play();
    }

    // ========== WINDOW BUTTONS ==========
    private HBox createWindowButtons(Stage stage) {
        Button closeButton = StyleHelper.createWindowButton("✕", stage::close);
        Button minimizeButton = StyleHelper.createWindowButton("−", () -> stage.setIconified(true));
        Button maximizeButton = StyleHelper.createWindowButton("□", 
            () -> stage.setMaximized(!stage.isMaximized()));

        HBox box = new HBox(3, minimizeButton, maximizeButton, closeButton);
        box.setAlignment(Pos.TOP_RIGHT);
        box.setPickOnBounds(false);
        return box;
    }

    // ========== MOSTRA BENVENUTO INIZIALE ==========
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
            titoloLabel, sottotitoloLabel, benvenutoLabel, istruzioniLabel, istruzioni2Label
        );

        showInContentPane(benvenutoBox);
    }

    // ========== AZIONI MENU ==========
    
    private void apriVisualizzaCorsi() {
        VisualizzaCorsiGUI corsiGUI = new VisualizzaCorsiGUI();
        corsiGUI.setControllers(corsiController, gestioneCorsoController, contentPane);
        showInContentPane(corsiGUI.getRoot());
    }

    private void apriCreaCorso() {
        CreaCorsoGUI gui = new CreaCorsoGUI(gestioneCorsoController, chefController, ricettaController);
        showInContentPane(gui.getRoot());
    }

    private void apriVisualizzaRicette() {
        VisualizzaRicetteGUI ricetteGUI = new VisualizzaRicetteGUI(ricettaController, ingredienteController);
        showInContentPane(ricetteGUI.getRoot());
    }

    private void apriCreaRicetta() {
        try {
            CreaRicettaGUI creaGUI = new CreaRicettaGUI(ricettaController, ingredienteController);
            Ricetta nuovaRicetta = creaGUI.showAndReturn();
            if (nuovaRicetta != null) {
                StyleHelper.showSuccessDialog("Successo", 
                    "Ricetta '" + nuovaRicetta.getNome() + "' creata con successo");
            }
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", 
                "Errore durante la creazione della ricetta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void eliminaAccount() {
        StyleHelper.showConfirmationDialog(
            "Conferma Eliminazione",
            "Eliminare definitivamente l'account?\nQuesta operazione non può essere annullata.",
            () -> {
                try {
                    chefController.eliminaAccount(chefLoggato);
                    StyleHelper.showSuccessDialog("Account eliminato", 
                        "Account eliminato con successo");
                    currentStage.close();
                } catch (ValidationException ve) {
                    StyleHelper.showValidationDialog("Errore Validazione", ve.getMessage());
                } catch (DataAccessException dae) {
                    StyleHelper.showErrorDialog("Errore Database", 
                        "Errore durante l'eliminazione dell'account: " + dae.getMessage());
                } catch (Exception e) {
                    StyleHelper.showErrorDialog("Errore", 
                        "Errore imprevisto: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        );
    }

    // ========== DRAGGABLE ==========
    private void setupDraggable(StackPane root, Stage stage) {
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        
        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    // ========== SHOW IN CONTENT PANE ==========
    private void showInContentPane(Node guiRoot) {
        contentPane.getChildren().setAll(guiRoot);
    }

    // ========== SAFE RUN ==========
    private void safeRun(Runnable action, String actionName) {
        try {
            action.run();
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", 
                "Errore durante: " + actionName + "\n" + e.getMessage());
            e.printStackTrace();
        }
    }
}
