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


public class ChefMenuGUI {

	private static final double WINDOW_WIDTH = 1200;
	private static final double WINDOW_HEIGHT = 800;
	private static final double SIDEBAR_WIDTH = 240;
	private static final double ANIMATION_DURATION = 250;

	private Chef chefLoggato;

	private CorsoCucinaDAO corsoDAO;
	private ChefDAO chefDAO;
	private TieneDAO tieneDAO;
	private IscrizioneDAO iscrizioneDAO;
	private OnlineDAO onlineDAO;
	private InPresenzaDAO inPresenzaDAO;
	private RicettaDAO ricettaDAO;
	private CucinaDAO cucinaDAO;

	private GestioneCorsiCucina gestioneCorsiService;
	private GestioneChef gestioneChefService;
	private GestioneRicette gestioneRicetteService;
	private GestioneCucina gestioneCucinaService;
	private GestioneSessioni gestioneSessioniService;

	private VisualizzaCorsiController corsiController;
	private GestioneCorsoController gestioneCorsoController;
	private RicettaController ricettaController;
	private IngredienteController ingredienteController;
	private ChefController chefController;

	private controller.ReportMensileController reportMensileController;

	private StackPane menuRoot;
	private StackPane contentPane;
	private VBox sidebar;
	private Button hamburgerBtn;
	private Stage currentStage;
	private Region background;
	private HBox mainLayout;

	private boolean sidebarVisible = true;
	private double xOffset = 0;
	private double yOffset = 0;

	public ChefMenuGUI() {

	}

	public void setChefLoggato(Chef chef) {
		if (chef == null)
			throw new IllegalArgumentException("Chef non può essere null");
		this.chefLoggato = chef;
		inizializzaDAO();
		inizializzaServices();
		inizializzaControllers();
	}

	public void setRicettaController(RicettaController ricettaController) {
		this.ricettaController = ricettaController;
	}

	public void setIngredienteController(IngredienteController ingredienteController) {
		this.ingredienteController = ingredienteController;
	}

	private void inizializzaDAO() {
		this.corsoDAO = new CorsoCucinaDAO();
		this.chefDAO = new ChefDAO();
		this.tieneDAO = new TieneDAO();
		this.iscrizioneDAO = new IscrizioneDAO();
		this.onlineDAO = new OnlineDAO();
		this.cucinaDAO = new CucinaDAO();
		this.inPresenzaDAO = new InPresenzaDAO(cucinaDAO);
		this.ricettaDAO = new RicettaDAO();
	}

	private void inizializzaServices() {

		this.gestioneRicetteService = new GestioneRicette(ricettaDAO);
		this.gestioneCucinaService = new GestioneCucina(cucinaDAO);

		this.gestioneCorsiService = new GestioneCorsiCucina(corsoDAO, chefDAO, tieneDAO, iscrizioneDAO, onlineDAO,
				inPresenzaDAO, gestioneRicetteService, gestioneCucinaService);

		this.gestioneChefService = new GestioneChef(chefDAO, tieneDAO);
		

		this.gestioneSessioniService = new GestioneSessioni(inPresenzaDAO, onlineDAO, cucinaDAO);
	}

	private void inizializzaControllers() {
		try {
			this.corsiController = new VisualizzaCorsiController(gestioneCorsiService, chefLoggato);

			this.gestioneCorsoController = new GestioneCorsoController(gestioneCorsiService, gestioneChefService);
			this.gestioneCorsoController.setChefLoggato(chefLoggato);

			this.chefController = new ChefController(gestioneChefService);
			this.chefController.setGestioneCorsoController(gestioneCorsoController);

			this.reportMensileController = new controller.ReportMensileController(gestioneCorsiService,
					gestioneSessioniService, chefLoggato);

		} catch (Exception e) {
			StyleHelper.showErrorDialog("Errore", "Impossibile inizializzare i controller: " + e.getMessage());
			throw new RuntimeException("Errore inizializzazione controller", e);
		}
	}

	public void start(Stage stage) {
		if (chefLoggato == null)
			throw new IllegalStateException("Chef non impostato! Chiamare setChefLoggato() prima di start()");
		this.currentStage = stage;
		setupStage(stage);
		menuRoot = createRootPane(stage);
		setupDraggable(menuRoot, stage);

		Scene scene = new Scene(menuRoot, WINDOW_WIDTH, WINDOW_HEIGHT);
		scene.setFill(Color.TRANSPARENT);
		stage.setScene(scene);
		stage.show();
	}

	private void setupStage(Stage stage) {
		stage.initStyle(StageStyle.UNDECORATED);
		stage.setTitle("Menu Chef - " + chefLoggato.getUsername());
	}

	private StackPane createRootPane(Stage stage) {
		StackPane root = new StackPane();
		background = createBackground(root);
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

	private HBox createMainLayout(Stage stage) {
		mainLayout = new HBox(); // Salva il riferimento
		mainLayout.setSpacing(0);

		sidebar = createSidebar(stage);
		contentPane = new StackPane();
		contentPane.setStyle("-fx-background-color: " + StyleHelper.BG_WHITE + ";");
		HBox.setHgrow(contentPane, Priority.ALWAYS);

		mostraBenvenutoIniziale();
		mainLayout.getChildren().addAll(sidebar, contentPane);
		return mainLayout;
	}

	private Region createBackground(StackPane root) {
		LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
				new Stop(0, Color.web(StyleHelper.BG_ORANGE_START)),
				new Stop(1, Color.web(StyleHelper.BG_ORANGE_LIGHT)));

		Region bg = new Region();
		bg.setBackground(new Background(new BackgroundFill(gradient, null, null)));
		bg.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
		bg.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		root.getChildren().add(bg);
		return bg;
	}

	private VBox createSidebar(Stage stage) {
		VBox sidebar = new VBox(20);
		sidebar.setAlignment(Pos.TOP_CENTER);
		sidebar.setPadding(new Insets(30, 15, 30, 15));
		sidebar.setStyle("-fx-background-color: " + StyleHelper.PRIMARY_ORANGE + ";");
		sidebar.setPrefWidth(SIDEBAR_WIDTH);

		Label welcomeLabel = createWelcomeLabel();
		sidebar.getChildren().add(welcomeLabel);

		Button btnVisualizzaCorsi = StyleHelper.createStyledButtonWithWhiteBorder("📚 Visualizza Corsi",
				StyleHelper.PRIMARY_LIGHT);
		Button btnCreaCorso = StyleHelper.createStyledButtonWithWhiteBorder("➕ Crea Corso", StyleHelper.PRIMARY_LIGHT);
		Button btnGestisciRicette = StyleHelper.createStyledButtonWithWhiteBorder("📖 Gestisci Ricette",
				StyleHelper.PRIMARY_LIGHT);
		Button btnCreaRicetta = StyleHelper.createStyledButtonWithWhiteBorder("✨ Crea Ricetta",
				StyleHelper.PRIMARY_LIGHT);
		Button btnReportMensile = StyleHelper.createStyledButtonWithWhiteBorder("📊 Report", StyleHelper.PRIMARY_LIGHT);

		Button btnEliminaAccount = StyleHelper.createStyledButtonWithWhiteBorder("🗑️ Elimina Account",
				StyleHelper.ERROR_RED);
		Button btnLogout = StyleHelper.createStyledButtonWithWhiteBorder("🚪 Logout", StyleHelper.NEUTRAL_GRAY);

		btnVisualizzaCorsi.setOnAction(e -> safeRun(this::apriVisualizzaCorsi, "Visualizza Corsi"));
		btnCreaCorso.setOnAction(e -> safeRun(this::apriCreaCorso, "Crea Corso"));
		btnGestisciRicette.setOnAction(e -> safeRun(this::apriVisualizzaRicette, "Gestisci Ricette"));
		btnCreaRicetta.setOnAction(e -> safeRun(this::apriCreaRicetta, "Crea Ricetta"));
		btnReportMensile.setOnAction(e -> safeRun(this::apriReportMensile, "Report Mensile"));
		btnEliminaAccount.setOnAction(e -> safeRun(this::eliminaAccount, "Elimina Account"));
		btnLogout.setOnAction(e -> safeRun(() -> {
		    try {
		        LoginChefGUI loginGui = new LoginChefGUI();
		        loginGui.start(stage);
		    } catch (Exception ex) {
		        // rilanciamo per far gestire l'errore da safeRun
		        throw new RuntimeException("Errore durante il logout: " + ex.getMessage(), ex);
		    }
		}, "Logout"));

		for (Button btn : new Button[] { btnVisualizzaCorsi, btnCreaCorso, btnGestisciRicette, btnCreaRicetta,
				btnReportMensile, btnEliminaAccount, btnLogout }) {
			btn.setPrefWidth(200);
		}

		sidebar.getChildren().addAll(btnVisualizzaCorsi, btnCreaCorso, btnGestisciRicette, btnCreaRicetta,
				btnReportMensile, btnEliminaAccount, btnLogout);

		return sidebar;
	}

	private Label createWelcomeLabel() {
		Label label = new Label("Benvenuto\n" + chefLoggato.getUsername());
		label.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
		label.setTextFill(Color.WHITE);
		label.setWrapText(true);
		label.setAlignment(Pos.CENTER);
		return label;
	}

	private Button createHamburgerButton() {
		Button btn = new Button("☰");
		btn.setPrefSize(45, 45);
		btn.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
		btn.setTextFill(Color.WHITE);
		btn.setStyle("-fx-background-color: " + StyleHelper.PRIMARY_ORANGE + ";" + "-fx-background-radius: 25;"
				+ "-fx-cursor: hand;");
		btn.setOnAction(e -> toggleSidebar());

		DropShadow shadow = new DropShadow();
		shadow.setRadius(8);
		shadow.setColor(Color.web("#000000", 0.3));
		btn.setEffect(shadow);

		return btn;
	}

	private void toggleSidebar() {
		TranslateTransition sidebarTransition = new TranslateTransition(Duration.millis(ANIMATION_DURATION), sidebar);

		if (sidebarVisible) {

			sidebarTransition.setToX(-SIDEBAR_WIDTH);

			background.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));

			sidebarTransition.setOnFinished(e -> {
				mainLayout.getChildren().remove(sidebar);
				sidebar.setTranslateX(0);
			});

			sidebarTransition.play();
			sidebarVisible = false;
		} else {

			mainLayout.getChildren().add(0, sidebar);

			sidebar.setTranslateX(-SIDEBAR_WIDTH);

			sidebarTransition.setToX(0);

			LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
					new Stop(0, Color.web(StyleHelper.BG_ORANGE_START)),
					new Stop(1, Color.web(StyleHelper.BG_ORANGE_LIGHT)));
			background.setBackground(new Background(new BackgroundFill(gradient, null, null)));

			sidebarTransition.play();
			sidebarVisible = true;
		}
	}

	private HBox createWindowButtons(Stage stage) {
		Button closeButton = StyleHelper.createWindowButton("✕", stage::close, StyleHelper.ERROR_RED);
		Button minimizeButton = StyleHelper.createWindowButton("−", () -> stage.setIconified(true),
				"rgba(255,107,53,0.8)");
		Button maximizeButton = StyleHelper.createWindowButton("", () -> stage.setMaximized(!stage.isMaximized()),
				"rgba(255,107,53,0.8)");

		HBox box = new HBox(3, minimizeButton, maximizeButton, closeButton);
		box.setAlignment(Pos.TOP_RIGHT);
		box.setPickOnBounds(false);
		return box;
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

		benvenutoBox.getChildren().addAll(titoloLabel, sottotitoloLabel, benvenutoLabel, istruzioniLabel);

		showInContentPane(benvenutoBox);
	}

	private void apriVisualizzaCorsi() {
		if (ricettaController == null || ingredienteController == null) {
			StyleHelper.showErrorDialog("Errore",
					"Controller non inizializzati.\n\n" + "Impossibile aprire la visualizzazione corsi.");
			return;
		}

		VisualizzaCorsiGUI corsiGUI = new VisualizzaCorsiGUI();

		corsiGUI.setControllers(corsiController, gestioneCorsoController, chefController, contentPane);
		corsiGUI.setRicettaController(ricettaController);
		corsiGUI.setIngredienteController(ingredienteController);

		showInContentPane(corsiGUI.getRoot());
	}

	private void apriCreaCorso() {
		CreaCorsoGUI gui = new CreaCorsoGUI(gestioneCorsoController, chefController, ricettaController,
				ingredienteController);
		showInContentPane(gui.getRoot());
	}

	private void apriVisualizzaRicette() {
		VisualizzaRicetteGUI ricetteGUI = new VisualizzaRicetteGUI(ricettaController, ingredienteController);
		showInContentPane(ricetteGUI.getRoot());
	}

	private void apriCreaRicetta() {
    try {
       
        CreaRicettaGUI creaGUI = new CreaRicettaGUI(ricettaController, ingredienteController);
        
       
        creaGUI.setOnRicettaCreata(nuovaRicetta -> {
            System.out.println("DEBUG: Ricetta creata: " + nuovaRicetta.getNome());
            
          
            StyleHelper.showSuccessDialog("Successo",
                    String.format("✅ Ricetta '%s' creata con successo!\n\n" 
                            + "⏱️ Tempo preparazione: %d minuti\n"
                            + "🥕 Ingredienti: %d",
                            nuovaRicetta.getNome(), 
                            nuovaRicetta.getTempoPreparazione(),
                            nuovaRicetta.getNumeroIngredienti()));
            
          
            mostraBenvenutoIniziale();
            
          
        });
        
      
        creaGUI.setOnAnnulla(() -> {
            System.out.println("DEBUG: Creazione ricetta annullata dall'utente");
            
           
            mostraBenvenutoIniziale();
        });
        
       
        VBox contenuto = creaGUI.getContent();
        showInContentPane(contenuto);
        
    } catch (Exception e) {
        System.err.println("ERROR: Errore durante la creazione della ricetta: " + e.getMessage());
        e.printStackTrace();
        StyleHelper.showErrorDialog("Errore", 
            "Errore durante l'apertura della schermata crea ricetta:\n" + e.getMessage());
    }
}


	private void apriReportMensile() {
		try {
			ReportMensileGUI reportGUI = new ReportMensileGUI(reportMensileController);
			showInContentPane(reportGUI.getRoot());
		} catch (Exception e) {
			StyleHelper.showErrorDialog("Errore", "Errore durante l'apertura del report mensile: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void eliminaAccount() {
		StyleHelper.showConfirmationDialog("Conferma Eliminazione",
				"Eliminare definitivamente l'account?\nQuesta operazione non può essere annullata.", () -> {
					try {
						chefController.eliminaAccount(chefLoggato);
						StyleHelper.showSuccessDialog("Account eliminato", "Account eliminato con successo");
						currentStage.close();
					} catch (ValidationException ve) {
						StyleHelper.showValidationDialog("Errore Validazione", ve.getMessage());
					} catch (DataAccessException dae) {
						StyleHelper.showErrorDialog("Errore Database",
								"Errore durante l'eliminazione dell'account: " + dae.getMessage());
					} catch (Exception e) {
						StyleHelper.showErrorDialog("Errore", "Errore imprevisto: " + e.getMessage());
						e.printStackTrace();
					}
				});
	}

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

	private void showInContentPane(Node guiRoot) {
		contentPane.getChildren().setAll(guiRoot);
	}

	private void safeRun(Runnable action, String actionName) {
		try {
			action.run();
		} catch (Exception e) {
			StyleHelper.showErrorDialog("Errore", "Errore durante: " + actionName + "\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public controller.ReportMensileController getReportMensileController() {
		return reportMensileController;
	}
}
