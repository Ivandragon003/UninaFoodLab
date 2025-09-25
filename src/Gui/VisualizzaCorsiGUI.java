package Gui;

import controller.GestioneCorsoController;
import controller.VisualizzaCorsiController;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.CorsoCucina;

import java.sql.SQLException;
import java.util.List;

public class VisualizzaCorsiGUI {

	private VisualizzaCorsiController visualizzaController;
	private GestioneCorsoController gestioneCorsoController;
	private StackPane menuRoot;

	private final ObservableList<CorsoCucina> corsiData = FXCollections.observableArrayList();
	private FilteredList<CorsoCucina> filteredCorsi;

	// Variabili per trascinamento
	private double xOffset = 0;
	private double yOffset = 0;

	public void setControllers(VisualizzaCorsiController visualizzaController,
			GestioneCorsoController gestioneCorsoController, StackPane menuRoot) {
		this.visualizzaController = visualizzaController;
		this.gestioneCorsoController = gestioneCorsoController;
		this.menuRoot = menuRoot;
	}

	public StackPane getRoot() {
    StackPane root = new StackPane();
    root.setPrefSize(800, 750);

    // ===== Sfondo moderno con gradiente =====
    createBackground(root);

    // ===== Card principale modernizzata =====
    VBox card = new VBox(25);
    card.setAlignment(Pos.TOP_CENTER);
    card.setPadding(new Insets(35));
    card.setMaxWidth(750);
    card.setStyle("-fx-background-color: white;"
            + "-fx-background-radius: 25;"
            + "-fx-border-radius: 25;"
            + "-fx-border-color: #FF9966;"
            + "-fx-border-width: 2;");

    DropShadow shadow = new DropShadow();
    shadow.setRadius(15);
    shadow.setColor(Color.web("#000000", 0.15));
    shadow.setOffsetY(5);
    card.setEffect(shadow);

    root.getChildren().add(card);

    // ===== Titolo =====
    Label title = new Label("📚 Lista dei Corsi");
    title.setFont(Font.font("Roboto", FontWeight.BOLD, 28));
    title.setTextFill(Color.web("#FF6600"));
    card.getChildren().add(title);

    // ===== Sezione filtri =====
    VBox filterSection = new VBox(15);
    filterSection.setAlignment(Pos.CENTER);

    Label filterLabel = new Label("🔍 Filtri di Ricerca");
    filterLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
    filterLabel.setTextFill(Color.web("#666666"));

    HBox filters = new HBox(15);
    filters.setAlignment(Pos.CENTER);

    TextField nomeField = createModernTextField("Cerca per nome...", "👨‍🍳");
    TextField argomentoField = createModernTextField("Cerca per argomento...", "📖");

    filters.getChildren().addAll(nomeField, argomentoField);
    filterSection.getChildren().addAll(filterLabel, filters);
    card.getChildren().add(filterSection);

    // ===== TableView modernizzata in ScrollPane =====
    TableView<CorsoCucina> table = createModernTable();

    ScrollPane scrollPane = new ScrollPane(table);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true);
    scrollPane.setPrefHeight(350);
    card.getChildren().add(scrollPane);
    VBox.setVgrow(scrollPane, Priority.ALWAYS);

    // ===== Bottoni azione =====
    VBox buttonSection = new VBox(15);
    buttonSection.setAlignment(Pos.CENTER);

    HBox actionButtons = new HBox(15);
    actionButtons.setAlignment(Pos.CENTER);

    Button mostraTuttiBtn = createStylishButton("📋 Tutti i Corsi", "#FF6600", "#FF8533");
    Button mieiBtn = createStylishButton("👨‍🍳 I Miei Corsi", "#FF6600", "#FF8533");

    actionButtons.getChildren().addAll(mostraTuttiBtn, mieiBtn);

    Button backBtn = createStylishButton("⬅ Torna al Menu", "#FFCC99", "#FFD9B3");
    backBtn.setPrefWidth(200);

    buttonSection.getChildren().addAll(actionButtons, backBtn);
    card.getChildren().add(buttonSection);

    // ===== Caricamento dati completi =====
    corsiData.clear();
    filteredCorsi = new FilteredList<>(corsiData, p -> true);
    table.setItems(filteredCorsi);
    if (visualizzaController != null) {
        try {
            corsiData.addAll(visualizzaController.getTuttiICorsiCompleti());
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Errore caricando corsi: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    // ===== Eventi filtri =====
    setupFilters(nomeField, argomentoField, mostraTuttiBtn, mieiBtn);

    // ===== Doppio click per dettagli =====
    setupTableDoubleClick(table, root);

    // ===== Bottone torna indietro =====
    setupBackButton(backBtn, root);

    // ===== Bottoni finestra =====
    HBox windowButtons = createWindowButtons(root);
    root.getChildren().add(windowButtons);
    StackPane.setAlignment(windowButtons, Pos.TOP_RIGHT);
    StackPane.setMargin(windowButtons, new Insets(10));

    // ===== Rende la finestra trascinabile =====
    makeDraggable(root);

    return root;
}

	private void createBackground(StackPane root) {
		LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
				new Stop(0, Color.web("#FF9966")), new Stop(1, Color.web("#FFCC99")));
		Region background = new Region();
		background.setBackground(new Background(new BackgroundFill(gradient, null, null)));
		background.setPrefSize(800, 750);
		root.getChildren().add(background);
	}

	private TextField createModernTextField(String prompt, String icon) {
		TextField field = new TextField();
		field.setPromptText(icon + " " + prompt);
		field.setPrefHeight(40);
		field.setPrefWidth(200);
		field.setFont(Font.font("Roboto", 14));
		field.setStyle("-fx-background-color: #f8f9fa;" + "-fx-background-radius: 20;" + "-fx-border-radius: 20;"
				+ "-fx-border-color: #dee2e6;" + "-fx-border-width: 1;" + "-fx-padding: 0 15 0 15;");

		field.setOnMouseEntered(e -> field
				.setStyle("-fx-background-color: #e9ecef;" + "-fx-background-radius: 20;" + "-fx-border-radius: 20;"
						+ "-fx-border-color: #FF9966;" + "-fx-border-width: 2;" + "-fx-padding: 0 15 0 15;"));

		field.setOnMouseExited(e -> field
				.setStyle("-fx-background-color: #f8f9fa;" + "-fx-background-radius: 20;" + "-fx-border-radius: 20;"
						+ "-fx-border-color: #dee2e6;" + "-fx-border-width: 1;" + "-fx-padding: 0 15 0 15;"));

		return field;
	}

	private TableView<CorsoCucina> createModernTable() {
		TableView<CorsoCucina> table = new TableView<>();
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		table.setPrefHeight(350);
		table.setStyle("-fx-background-color: #f8f9fa;" + "-fx-background-radius: 15;" + "-fx-border-radius: 15;"
				+ "-fx-border-color: #dee2e6;" + "-fx-border-width: 1;");

		// ID
		TableColumn<CorsoCucina, Integer> idCol = new TableColumn<>("ID");
		idCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getIdCorso()).asObject());
		idCol.setPrefWidth(60);

		// Nome
		TableColumn<CorsoCucina, String> nomeCol = new TableColumn<>("📚 Nome Corso");
		nomeCol.setCellValueFactory(
				c -> new SimpleStringProperty(c.getValue().getNomeCorso() != null ? c.getValue().getNomeCorso() : ""));

		// Argomento
		TableColumn<CorsoCucina, String> argomentoCol = new TableColumn<>("📖 Argomento");
		argomentoCol.setCellValueFactory(
				c -> new SimpleStringProperty(c.getValue().getArgomento() != null ? c.getValue().getArgomento() : ""));

		// Iscritti
		TableColumn<CorsoCucina, Integer> iscrittiCol = new TableColumn<>("👥 Iscritti");
		iscrittiCol.setCellValueFactory(c -> new SimpleIntegerProperty(
				c.getValue().getIscrizioni() != null ? c.getValue().getIscrizioni().size() : 0).asObject());

		// Prezzo
		TableColumn<CorsoCucina, Double> prezzoCol = new TableColumn<>("💰 Prezzo €");
		prezzoCol.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrezzo()).asObject());

		// Sessioni
		TableColumn<CorsoCucina, Integer> sessioniCol = new TableColumn<>("⏰ Sessioni");
		sessioniCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNumeroSessioni()).asObject()); // usa
																														// numeroSessioni
																														// calcolato

		// Frequenza
		TableColumn<CorsoCucina, String> freqCol = new TableColumn<>("📅 Frequenza");
		freqCol.setCellValueFactory(c -> new SimpleStringProperty(
				c.getValue().getFrequenzaCorso() != null ? c.getValue().getFrequenzaCorso().toString() : ""));

		// Numero posti
		TableColumn<CorsoCucina, Integer> postiCol = new TableColumn<>("🪑 Posti");
		postiCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNumeroPosti()).asObject());

		// Date Inizio/Fine
		TableColumn<CorsoCucina, String> inizioCol = new TableColumn<>("🕑 Inizio");
		inizioCol.setCellValueFactory(c -> new SimpleStringProperty(
				c.getValue().getDataInizioCorso() != null ? c.getValue().getDataInizioCorso().toString() : ""));

		TableColumn<CorsoCucina, String> fineCol = new TableColumn<>("🕑 Fine");
		fineCol.setCellValueFactory(c -> new SimpleStringProperty(
				c.getValue().getDataFineCorso() != null ? c.getValue().getDataFineCorso().toString() : ""));

		table.getColumns().addAll(idCol, nomeCol, argomentoCol, iscrittiCol, prezzoCol, sessioniCol, freqCol, postiCol,
				inizioCol, fineCol);

		return table;
	}

	private Button createStylishButton(String text, String baseColor, String hoverColor) {
		Button button = new Button(text);
		button.setPrefSize(160, 45);
		button.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
		button.setTextFill(Color.web("#4B2E2E"));
		button.setStyle(
				"-fx-background-color: " + baseColor + ";" + "-fx-background-radius: 25;" + "-fx-cursor: hand;");

		DropShadow shadow = new DropShadow();
		shadow.setRadius(8);
		shadow.setColor(Color.web("#000000", 0.2));
		shadow.setOffsetY(3);
		button.setEffect(shadow);

		button.setOnMouseEntered(e -> {
			button.setStyle(
					"-fx-background-color: " + hoverColor + ";" + "-fx-background-radius: 25;" + "-fx-cursor: hand;");
			DropShadow hoverShadow = new DropShadow();
			hoverShadow.setRadius(12);
			hoverShadow.setColor(Color.web("#000000", 0.3));
			hoverShadow.setOffsetY(5);
			button.setEffect(hoverShadow);
		});

		button.setOnMouseExited(e -> {
			button.setStyle(
					"-fx-background-color: " + baseColor + ";" + "-fx-background-radius: 25;" + "-fx-cursor: hand;");
			button.setEffect(shadow);
		});

		return button;
	}

	private void makeDraggable(StackPane pane) {
		pane.setOnMousePressed(event -> {
			Stage stage = getStage(pane);
			if (stage != null) {
				xOffset = event.getSceneX();
				yOffset = event.getSceneY();
			}
		});

		pane.setOnMouseDragged(event -> {
			Stage stage = getStage(pane);
			if (stage != null) {
				stage.setX(event.getScreenX() - xOffset);
				stage.setY(event.getScreenY() - yOffset);
			}
		});
	}

	private void setupFilters(TextField nomeField, TextField argomentoField, Button mostraTuttiBtn, Button mieiBtn) {
		nomeField.textProperty()
				.addListener((obs, o, n) -> applicaFiltri(nomeField.getText(), argomentoField.getText(), false));

		argomentoField.textProperty()
				.addListener((obs, o, n) -> applicaFiltri(nomeField.getText(), argomentoField.getText(), false));

		mostraTuttiBtn.setOnAction(e -> {
			nomeField.clear();
			argomentoField.clear();
			applicaFiltri("", "", false);
		});

		mieiBtn.setOnAction(e -> applicaFiltri(nomeField.getText(), argomentoField.getText(), true));
	}

	private void setupTableDoubleClick(TableView<CorsoCucina> table, StackPane root) {
		table.setRowFactory(tv -> {
			TableRow<CorsoCucina> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && (!row.isEmpty())) {
					CorsoCucina selected = row.getItem();
					CorsoCucina dettagli = selected;
					try {
						dettagli = gestioneCorsoController.getCorsoCompleto(selected.getIdCorso());
					} catch (Exception ex) {
						/* fallback: usiamo selected */ }

					try {
						DettagliCorsoGUI detGui = new DettagliCorsoGUI();
						detGui.setController(gestioneCorsoController);
						detGui.setCorso(dettagli);
						VBox dettagliRoot = detGui.getRoot();

						Stage detailsStage = new Stage();
						detailsStage.initStyle(StageStyle.UNDECORATED);

						StackPane detailsPane = new StackPane();
						detailsPane.setPrefSize(650, 700);

						createBackground(detailsPane);

						dettagliRoot.setMaxWidth(600);
						detailsPane.getChildren().add(dettagliRoot);

						HBox winBtn = createWindowButtonsForStage(detailsStage);
						detailsPane.getChildren().add(winBtn);
						StackPane.setAlignment(winBtn, Pos.TOP_RIGHT);
						StackPane.setMargin(winBtn, new Insets(10));

						Scene scene = new Scene(detailsPane);
						scene.setFill(Color.TRANSPARENT);
						detailsStage.setScene(scene);

						Stage owner = getStage(root);
						if (owner != null)
							detailsStage.initOwner(owner);

						// Rende trascinabile anche il dettaglio
						makeDraggable(detailsPane);

						detailsStage.show();
					} catch (Exception ex) {
						ex.printStackTrace();
						new Alert(Alert.AlertType.ERROR, "Errore aprendo i dettagli: " + ex.getMessage(), ButtonType.OK)
								.showAndWait();
					}
				}
			});
			return row;
		});
	}

	private void setupBackButton(Button backBtn, StackPane root) {
		backBtn.setOnAction(e -> {
			if (menuRoot == null) {
				new Alert(Alert.AlertType.WARNING, "menuRoot non impostato. Non posso tornare indietro.", ButtonType.OK)
						.showAndWait();
				return;
			}
			Stage stage = getStage(root);
			if (stage != null && stage.getScene() != null) {
				stage.getScene().setRoot(menuRoot);
			} else {
				root.getChildren().setAll(menuRoot);
			}
		});
	}

	private HBox createWindowButtons(StackPane root) {
		Stage stage = getStage(root);
		Button closeButton = new Button("✕");
		Button minimizeButton = new Button("_");
		Button maximizeButton = new Button("□");

		Button[] buttons = { minimizeButton, maximizeButton, closeButton };
		for (Button btn : buttons) {
			btn.setPrefSize(35, 35);
			btn.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
			btn.setTextFill(Color.WHITE);
			btn.setStyle(
					"-fx-background-color: rgba(255,140,0,0.6);" + "-fx-background-radius: 20;" + "-fx-cursor: hand;");
			btn.setFocusTraversable(false);
		}

		closeButton.setOnAction(e -> {
			if (stage != null)
				stage.close();
		});
		minimizeButton.setOnAction(e -> {
			if (stage != null)
				stage.setIconified(true);
		});
		maximizeButton.setOnAction(e -> {
			if (stage != null)
				stage.setMaximized(!stage.isMaximized());
		});

		HBox box = new HBox(5, minimizeButton, maximizeButton, closeButton);
		box.setAlignment(Pos.TOP_RIGHT);
		box.setPickOnBounds(false);
		return box;
	}

	private HBox createWindowButtonsForStage(Stage stage) {
		Button closeButton = new Button("✕");
		Button minimizeButton = new Button("_");
		Button maximizeButton = new Button("□");

		Button[] buttons = { minimizeButton, maximizeButton, closeButton };
		for (Button btn : buttons) {
			btn.setPrefSize(35, 35);
			btn.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
			btn.setTextFill(Color.WHITE);
			btn.setStyle(
					"-fx-background-color: rgba(255,140,0,0.6);" + "-fx-background-radius: 20;" + "-fx-cursor: hand;");
			btn.setFocusTraversable(false);
		}

		closeButton.setOnAction(e -> stage.close());
		minimizeButton.setOnAction(e -> stage.setIconified(true));
		maximizeButton.setOnAction(e -> stage.setMaximized(!stage.isMaximized()));

		HBox box = new HBox(5, minimizeButton, maximizeButton, closeButton);
		box.setAlignment(Pos.CENTER_RIGHT);
		return box;
	}

	private void caricaCorsi() {
		corsiData.clear();
		if (visualizzaController == null)
			return;
		try {
			corsiData.addAll(visualizzaController.getTuttiICorsi());
		} catch (SQLException e) {
			e.printStackTrace();
			new Alert(Alert.AlertType.ERROR, "Errore caricando corsi: " + e.getMessage(), ButtonType.OK).showAndWait();
		}
	}
	private void applicaFiltri(String nome, String argomento, boolean soloChefLoggato) {
	    if (filteredCorsi == null || visualizzaController == null) return;

	    try {
	        String filtro = "";
	        if ((nome != null && !nome.isEmpty()) || (argomento != null && !argomento.isEmpty())) {
	            filtro = nome + " " + argomento; // puoi unire i due valori per passare al DAO
	        }

	
	        List<CorsoCucina> corsiFiltrati = visualizzaController.cercaPerNomeOCategoria(filtro);

	        if (soloChefLoggato) {
	            List<CorsoCucina> corsiChef = visualizzaController.getCorsiChefLoggato();
	            corsiFiltrati = corsiFiltrati.stream()
	                    .filter(corsiChef::contains)
	                    .toList();
	        }

	        corsiData.setAll(corsiFiltrati);

	    } catch (SQLException e) {
	        e.printStackTrace();
	        new Alert(Alert.AlertType.ERROR, "Errore applicando filtri: " + e.getMessage(), ButtonType.OK).showAndWait();
	    }
	}


	private Stage getStage(Node node) {
		if (node == null)
			return null;
		Scene s = node.getScene();
		if (s == null)
			return null;
		if (s.getWindow() instanceof Stage)
			return (Stage) s.getWindow();
		return null;
	}

}
