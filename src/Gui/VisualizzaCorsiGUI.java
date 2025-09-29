package Gui;

import controller.GestioneCorsoController;
import controller.VisualizzaCorsiController;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
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
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.CorsoCucina;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VisualizzaCorsiGUI {

	private VisualizzaCorsiController visualizzaController;
	private GestioneCorsoController gestioneCorsoController;
	private StackPane menuRoot;

	private final ObservableList<CorsoCucina> corsiData = FXCollections.observableArrayList();
	private FilteredList<CorsoCucina> filteredCorsi;
	private List<CorsoCucina> cachedCorsiChef = null;

	private double xOffset = 0;
	private double yOffset = 0;

	private ProgressIndicator progressIndicator;
	private TableView<CorsoCucina> table;
	private TextField nomeField;
	private TextField argomentoField;
	private Label countLabel;
	private Button mostraTuttiBtn;
	private Button mieiBtn;

	// riferimento alla root principale per tornare indietro
	private StackPane mainRoot;

	public void setControllers(VisualizzaCorsiController visualizzaController,
			GestioneCorsoController gestioneCorsoController, StackPane menuRoot) {
		this.visualizzaController = visualizzaController;
		this.gestioneCorsoController = gestioneCorsoController;
		this.menuRoot = menuRoot;
	}

	public StackPane getRoot() {
		mainRoot = new StackPane();
		createOrangeBackground(mainRoot);

		VBox mainContainer = createMainContainer(mainRoot);
		mainRoot.getChildren().add(mainContainer);

		VBox headerSection = createHeader();
		mainContainer.getChildren().add(headerSection);

		HBox filterSection = createFilterSection();
		mainContainer.getChildren().add(filterSection);

		createTable();
		StackPane tableContainer = new StackPane(table);
		tableContainer.setStyle("-fx-background-color: rgba(255,255,255,0.95);" + "-fx-background-radius: 20;"
				+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 5);");
		VBox.setVgrow(tableContainer, Priority.ALWAYS);
		mainContainer.getChildren().add(tableContainer);

		createProgressIndicator(tableContainer);

		HBox actionSection = createActionButtons();
		mainContainer.getChildren().add(actionSection);

		setupFilters();
		addWindowControls(mainRoot);
		makeDraggable(mainRoot, headerSection);

		Platform.runLater(this::refreshData);

		return mainRoot;
	}

	private void createOrangeBackground(StackPane root) {
		LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
				new Stop(0, Color.web("#FF9966")), new Stop(0.5, Color.web("#FFB366")),
				new Stop(1, Color.web("#FFCC99")));
		Region background = new Region();
		background.setBackground(new Background(new BackgroundFill(gradient, null, null)));
		background.prefWidthProperty().bind(root.widthProperty());
		background.prefHeightProperty().bind(root.heightProperty());
		root.getChildren().add(background);

		for (int i = 0; i < 4; i++) {
			Circle decorCircle = new Circle(15 + Math.random() * 30);
			decorCircle.setFill(Color.web("#FFFFFF", 0.08));
			decorCircle.setTranslateX((i - 2) * 120 + Math.random() * 60);
			decorCircle.setTranslateY((i % 2 == 0 ? -1 : 1) * (80 + Math.random() * 40));
			root.getChildren().add(decorCircle);
		}
	}

	private VBox createMainContainer(StackPane root) {
		VBox container = new VBox(20);
		container.setAlignment(Pos.TOP_CENTER);
		container.setPadding(new Insets(28));
		container.prefWidthProperty().bind(root.widthProperty().multiply(0.92));
		container.prefHeightProperty().bind(root.heightProperty().multiply(0.9));
		container.setStyle("-fx-background-color: rgba(255,255,255,0.18);" + "-fx-background-radius: 20;"
				+ "-fx-border-radius: 20;" + "-fx-border-color: rgba(255,255,255,0.35);" + "-fx-border-width: 1;");
		DropShadow containerShadow = new DropShadow(18, Color.web("#000000", 0.12));
		containerShadow.setOffsetY(6);
		container.setEffect(containerShadow);
		return container;
	}

	private VBox createHeader() {
		VBox header = new VBox(10);
		header.setAlignment(Pos.CENTER);
		header.setPadding(new Insets(8, 0, 12, 0));

		Label title = new Label("📚 Lista dei Corsi");
		title.setFont(Font.font("Inter", FontWeight.EXTRA_BOLD, 26));
		title.setTextFill(Color.WHITE);
		title.setEffect(new DropShadow(8, Color.web("#FF6600", 0.8)));

		Label subtitle = new Label("Esplora e gestisci i corsi di cucina disponibili");
		subtitle.setFont(Font.font("Inter", FontWeight.NORMAL, 12));
		subtitle.setTextFill(Color.web("#ffffff", 0.9));

		header.getChildren().addAll(title, subtitle);
		return header;
	}

	private HBox createFilterSection() {
		HBox filters = new HBox(14);
		filters.setAlignment(Pos.CENTER);
		filters.setPadding(new Insets(0, 12, 6, 12));

		nomeField = createOrangeTextField("🔍 Cerca per nome corso...", 250);
		argomentoField = createOrangeTextField("📖 Cerca per argomento...", 200);

		countLabel = new Label("0 corsi");
		countLabel.setFont(Font.font("Inter", FontWeight.MEDIUM, 12));
		countLabel.setTextFill(Color.WHITE);
		countLabel
				.setStyle("-fx-background-color: rgba(255,102,0,0.75); -fx-background-radius: 15; -fx-padding: 8 14;");

		filters.getChildren().addAll(nomeField, argomentoField, countLabel);
		return filters;
	}

	private void createTable() {
		table = new TableView<>();
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		table.setPlaceholder(new Label("📚 Nessun corso trovato"));
		table.setFixedCellSize(42);
		table.setStyle("-fx-background-color: transparent;" + "-fx-table-cell-border-color: rgba(0,0,0,0.04);"
				+ "-fx-selection-bar: rgba(255,153,102,0.25);"
				+ "-fx-selection-bar-non-focused: rgba(255,153,102,0.18);");

		// Colonne principali
		TableColumn<CorsoCucina, String> nomeCol = new TableColumn<>("📚 Nome Corso");
		nomeCol.setCellValueFactory(
				c -> new SimpleStringProperty(c.getValue().getNomeCorso() != null ? c.getValue().getNomeCorso() : ""));
		nomeCol.setMinWidth(200);

		TableColumn<CorsoCucina, String> argomentoCol = new TableColumn<>("📖 Argomento");
		argomentoCol.setCellValueFactory(
				c -> new SimpleStringProperty(c.getValue().getArgomento() != null ? c.getValue().getArgomento() : ""));
		argomentoCol.setMinWidth(130);

		TableColumn<CorsoCucina, Double> prezzoCol = new TableColumn<>("💰 Prezzo");
		prezzoCol.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrezzo()).asObject());
		prezzoCol.setCellFactory(col -> new TableCell<>() {
			@Override
			protected void updateItem(Double item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null)
					setText(null);
				else {
					setText(String.format("€%.0f", item));
					setFont(Font.font("Inter", 13));
					setTextFill(Color.web("#2E7D32"));
				}
			}
		});
		prezzoCol.setMinWidth(90);

		TableColumn<CorsoCucina, Integer> sessioniCol = new TableColumn<>("⏰ Sessioni");
		sessioniCol.setCellValueFactory(c -> {
			try {
				int num = visualizzaController != null
						? visualizzaController.getNumeroSessioniPerCorso(c.getValue().getIdCorso())
						: 0;
				return new SimpleIntegerProperty(num).asObject();
			} catch (Exception e) {
				return new SimpleIntegerProperty(0).asObject();
			}
		});
		sessioniCol.setMinWidth(90);

		table.getColumns().addAll(nomeCol, argomentoCol, prezzoCol, sessioniCol);
		table.getSortOrder().add(nomeCol);


		table.setRowFactory(tv -> {
			TableRow<CorsoCucina> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !row.isEmpty()) {
					apriDettagliCorso(row.getItem());
				}
			});
			return row;
		});


		table.setOnMouseClicked(event -> {
			if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
				CorsoCucina selected = table.getSelectionModel().getSelectedItem();
				if (selected != null) {
					apriDettagliCorso(selected);
				}
			}
		});
	}

	private void createProgressIndicator(StackPane parent) {
		progressIndicator = new ProgressIndicator();
		progressIndicator.setPrefSize(50, 50);
		progressIndicator.setStyle("-fx-accent: #FF6600;");
		progressIndicator.setVisible(false);
		parent.getChildren().add(progressIndicator);
	}

	private HBox createActionButtons() {
		HBox actions = new HBox(12);
		actions.setAlignment(Pos.CENTER);
		actions.setPadding(new Insets(18, 0, 8, 0));

		mostraTuttiBtn = createOrangeButton("📋 Tutti i Corsi", 160);
		mieiBtn = createOrangeButton("👨‍🍳 I Miei Corsi", 160);
		Button backBtn = createOrangeButton("⬅️ Torna al Menu", 160);

		mostraTuttiBtn.setOnAction(e -> {
			nomeField.clear();
			argomentoField.clear();
			cachedCorsiChef = null;
			filteredCorsi.setPredicate(p -> true);
		});

		mieiBtn.setOnAction(e -> applicaFiltriLocali(nomeField.getText(), argomentoField.getText(), true));

		backBtn.setOnAction(e -> tornaAlMenu());

		actions.getChildren().addAll(backBtn, mostraTuttiBtn, mieiBtn);
		return actions;
	}

	private void tornaAlMenu() {
		if (menuRoot != null) {
			if (menuRoot.getChildren().contains(mainRoot)) {
				menuRoot.getChildren().remove(mainRoot);
				return;
			}
			Stage stage = getStage(mainRoot);
			if (stage != null && stage.getScene() != null) {
				stage.getScene().setRoot(menuRoot);
				return;
			}
		} else {
		
			Stage stage = getStage(mainRoot);
			if (stage != null) {
				stage.close();
			}
		}
	}

	private TextField createOrangeTextField(String prompt, double width) {
		TextField field = new TextField();
		field.setPromptText(prompt);
		field.setPrefHeight(40);
		field.setMaxWidth(width);
		field.setFont(Font.font("Inter", 13));
		field.setStyle("-fx-background-color: rgba(255,255,255,0.95);" + "-fx-background-radius: 20;"
				+ "-fx-border-radius: 20;" + "-fx-border-color: #FF9966;" + "-fx-border-width: 1.2;"
				+ "-fx-text-fill: #333;" + "-fx-prompt-text-fill: #999;" + "-fx-padding: 0 14;");
		return field;
	}

	private Button createOrangeButton(String text, double width) {
		Button btn = new Button(text);
		btn.setFont(Font.font("Inter", FontWeight.BOLD, 14));
		btn.setTextFill(Color.WHITE);
		btn.setPrefWidth(width);
		btn.setPrefHeight(44);
		btn.setStyle("-fx-background-color: #FF6600; -fx-background-radius: 22;");
		return btn;
	}

	private void setupFilters() {
		filteredCorsi = new FilteredList<>(corsiData, p -> true);
		SortedList<CorsoCucina> sorted = new SortedList<>(filteredCorsi);
		sorted.comparatorProperty().bind(table.comparatorProperty());
		table.setItems(sorted);

		nomeField.textProperty().addListener((obs, old, text) -> Platform
				.runLater(() -> applicaFiltriLocali(text, argomentoField.getText(), false)));
		argomentoField.textProperty().addListener(
				(obs, old, text) -> Platform.runLater(() -> applicaFiltriLocali(nomeField.getText(), text, false)));

		corsiData.addListener((javafx.collections.ListChangeListener<CorsoCucina>) c -> updateCountLabel());
		updateCountLabel();
	}

	private void apriDettagliCorso(CorsoCucina selected) {
		if (selected == null) return;
		if (gestioneCorsoController == null) {
			Platform.runLater(() -> {
				Alert a = new Alert(Alert.AlertType.ERROR, "Controller gestione corso non impostato.", ButtonType.OK);
				a.showAndWait();
			});
			return;
		}
		progressIndicator.setVisible(true);

		Task<CorsoCucina> loadDetailsTask = new Task<>() {
			@Override
			protected CorsoCucina call() throws Exception {
				return gestioneCorsoController.getCorsoCompleto(selected.getIdCorso());
			}

			@Override
			protected void succeeded() {
				CorsoCucina dettagli = getValue();
				progressIndicator.setVisible(false);

				DettagliCorsoGUI detGui = new DettagliCorsoGUI();
				detGui.setController(gestioneCorsoController);
				detGui.setCorso(dettagli != null ? dettagli : selected);

				StackPane dettagliRoot = detGui.getRoot();

				if (menuRoot != null) {
					menuRoot.getChildren().add(dettagliRoot);
				} else {
					Stage stage = getStage(table);
					if (stage != null && stage.getScene() != null) {
						stage.getScene().setRoot(dettagliRoot);
					}
				}
			}

			@Override
			protected void failed() {
				progressIndicator.setVisible(false);
				Throwable ex = getException();
				ex.printStackTrace();
				Platform.runLater(() -> {
					Alert a = new Alert(Alert.AlertType.ERROR);
					a.setHeaderText("Errore caricamento dettagli corso");
					a.setContentText(ex != null ? ex.getMessage() : "Errore sconosciuto");
					a.showAndWait();
				});
			}
		};

		new Thread(loadDetailsTask).start();
	}

	private void updateCountLabel() {
		int count = filteredCorsi == null ? corsiData.size() : filteredCorsi.size();
		String text = count + (count == 1 ? " corso" : " corsi");
		countLabel.setText(text);
	}

	private void refreshData() {
		if (visualizzaController == null) {
			if (progressIndicator != null)
				progressIndicator.setVisible(false);
			return;
		}

		Task<List<CorsoCucina>> loadTask = new Task<>() {
			@Override
			protected List<CorsoCucina> call() throws Exception {
				List<CorsoCucina> list = visualizzaController.getTuttiICorsi();
				return list == null ? Collections.emptyList() : list;
			}

			@Override
			protected void succeeded() {
				corsiData.setAll(getValue());
				progressIndicator.setVisible(false);
				updateCountLabel();
			}

			@Override
			protected void failed() {
				progressIndicator.setVisible(false);
				getException().printStackTrace();
			}
		};

		progressIndicator.setVisible(true);
		Thread t = new Thread(loadTask, "LoadCorsiThread");
		t.setDaemon(true);
		t.start();
	}

	private void applicaFiltriLocali(String nome, String argomento, boolean soloChefLoggato) {
		if (filteredCorsi == null)
			return;

		String n = nome == null ? "" : nome.toLowerCase().trim();
		String a = argomento == null ? "" : argomento.toLowerCase().trim();

		if (soloChefLoggato && cachedCorsiChef == null && visualizzaController != null) {
			Task<List<CorsoCucina>> chefCorsiTask = new Task<>() {
				@Override
				protected List<CorsoCucina> call() throws Exception {
					List<CorsoCucina> tmp = visualizzaController.getCorsiChefLoggato();
					return tmp == null ? new ArrayList<>() : new ArrayList<>(tmp);
				}

				@Override
				protected void succeeded() {
					cachedCorsiChef = getValue();
					Platform.runLater(() -> applicaFiltroConCache(n, a, soloChefLoggato));
				}

				@Override
				protected void failed() {
					cachedCorsiChef = new ArrayList<>();
					Platform.runLater(() -> applicaFiltroConCache(n, a, soloChefLoggato));
				}
			};
			Thread t = new Thread(chefCorsiTask, "LoadChefCorsiThread");
			t.setDaemon(true);
			t.start();
			return;
		}

		applicaFiltroConCache(n, a, soloChefLoggato);
	}

	private void applicaFiltroConCache(String n, String a, boolean soloChefLoggato) {
		filteredCorsi.setPredicate(c -> {
			boolean matchNome = n.isEmpty() || (c.getNomeCorso() != null && c.getNomeCorso().toLowerCase().contains(n));
			boolean matchArg = a.isEmpty() || (c.getArgomento() != null && c.getArgomento().toLowerCase().contains(a));
			boolean match = matchNome && matchArg;

			if (soloChefLoggato) {
				return match && cachedCorsiChef != null && cachedCorsiChef.contains(c);
			}
			return match;
		});
		updateCountLabel();
	}

	private void addWindowControls(StackPane root) {
		HBox controls = new HBox(6);
		controls.setAlignment(Pos.TOP_RIGHT);
		controls.setPadding(new Insets(8));
		controls.setPickOnBounds(false);

		Button minimizeBtn = createWindowButton("_", Color.WHITE);
		Button maximizeBtn = createWindowButton("⬜", Color.WHITE);
		Button closeBtn = createWindowButton("✖", Color.web("#FF4444"));

		minimizeBtn.setOnAction(e -> {
			Stage stage = getStage(root);
			if (stage != null)
				stage.setIconified(true);
		});
		maximizeBtn.setOnAction(e -> {
			Stage stage = getStage(root);
			if (stage != null)
				stage.setMaximized(!stage.isMaximized());
		});
		closeBtn.setOnAction(e -> {
			Stage stage = getStage(root);
			if (stage != null)
				stage.close();
		});

		controls.getChildren().addAll(minimizeBtn, maximizeBtn, closeBtn);
		StackPane.setAlignment(controls, Pos.TOP_RIGHT);
		root.getChildren().add(controls);
	}

	private Button createWindowButton(String symbol, Color color) {
		Button btn = new Button(symbol);
		btn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
		btn.setTextFill(color);
		btn.setStyle("-fx-background-color: transparent;");
		btn.setOnMouseEntered(e -> btn.setTextFill(color.brighter()));
		btn.setOnMouseExited(e -> btn.setTextFill(color));
		btn.setPrefSize(28, 28);
		return btn;
	}

	private void makeDraggable(StackPane root, Node dragNode) {
		dragNode.setOnMousePressed(e -> {
			xOffset = e.getSceneX();
			yOffset = e.getSceneY();
		});
		dragNode.setOnMouseDragged(e -> {
			Stage stage = getStage(root);
			if (stage != null && !stage.isMaximized()) {
				stage.setX(e.getScreenX() - xOffset);
				stage.setY(e.getScreenY() - yOffset);
			}
		});
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
