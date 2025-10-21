package Gui;

import controller.ChefController;
import controller.GestioneCorsoController;
import controller.IngredienteController;
import controller.RicettaController;
import controller.VisualizzaCorsiController;
import guihelper.StyleHelper;
import javafx.animation.PauseTransition;
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
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import model.CorsoCucina;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class VisualizzaCorsiGUI {

	private VisualizzaCorsiController visualizzaController;
	private GestioneCorsoController gestioneCorsoController;
	private ChefController chefController;
	private RicettaController ricettaController;
	private IngredienteController ingredienteController;
	private StackPane contentRoot;

	private final ObservableList<CorsoCucina> corsiData = FXCollections.observableArrayList();
	private FilteredList<CorsoCucina> filteredCorsi;

	private ProgressIndicator progressIndicator;
	private final PauseTransition filterPause = new PauseTransition(Duration.millis(350));

	private boolean mostraNonFiniti = true;
	private boolean mostraFiniti = true;
	private boolean filtroFondatoreAttivo = false;

	public VisualizzaCorsiGUI() {
	}

	public void setControllers(VisualizzaCorsiController visualizzaController,
			GestioneCorsoController gestioneCorsoController, ChefController chefController, StackPane contentRoot) {
		this.visualizzaController = visualizzaController;
		this.gestioneCorsoController = gestioneCorsoController;
		this.chefController = chefController;
		this.contentRoot = contentRoot;
	}

	public void setRicettaController(RicettaController ricettaController) {
		this.ricettaController = ricettaController;
	}

	public void setIngredienteController(IngredienteController ingredienteController) {
		this.ingredienteController = ingredienteController;
	}

	public StackPane getRoot() {
		StackPane root = new StackPane();
		root.setMinSize(400, 400);

		Region background = new Region();
		StyleHelper.applyBackgroundGradient(background);
		background.prefWidthProperty().bind(root.widthProperty());
		background.prefHeightProperty().bind(root.heightProperty());
		root.getChildren().add(background);

		VBox card = createMainCard(root);
		StackPane.setMargin(card, new Insets(20));
		root.getChildren().add(card);

		return root;
	}

	private VBox createMainCard(StackPane root) {
		VBox card = new VBox(16);
		card.setAlignment(Pos.TOP_CENTER);
		card.setPadding(new Insets(30));
		card.setMaxWidth(1400);
		card.setMaxHeight(Double.MAX_VALUE);
		card.setStyle("-fx-background-color: white;" + "-fx-background-radius: 25;" + "-fx-border-radius: 25;"
				+ "-fx-border-color: " + StyleHelper.PRIMARY_ORANGE + ";" + "-fx-border-width: 2;"
				+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4);");

		VBox headerSection = createHeaderSection();
		card.getChildren().add(headerSection);

		TableView<CorsoCucina> table = createOptimizedTable();
		table.setPrefHeight(500);
		table.setMaxHeight(Double.MAX_VALUE);
		VBox.setVgrow(table, Priority.ALWAYS);

		progressIndicator = new ProgressIndicator();
		progressIndicator.setPrefSize(40, 40);
		progressIndicator.setStyle("-fx-accent: " + StyleHelper.PRIMARY_ORANGE + ";");
		progressIndicator.setVisible(false);

		StackPane tableContainer = new StackPane(table, progressIndicator);
		VBox.setVgrow(tableContainer, Priority.ALWAYS);
		card.getChildren().add(tableContainer);

		VBox buttonSection = createButtonSection();
		card.getChildren().add(buttonSection);

		TextField nomeField = (TextField) ((HBox) headerSection.getChildren().get(1)).getChildren().get(0);
		TextField argomentoField = (TextField) ((HBox) headerSection.getChildren().get(1)).getChildren().get(1);
		MenuButton filtroStatoBtn = (MenuButton) ((HBox) headerSection.getChildren().get(1)).getChildren().get(2);
		CheckBox filtroFondatoreCheck = (CheckBox) ((HBox) headerSection.getChildren().get(1)).getChildren().get(3);
		Button mostraTuttiBtn = (Button) ((HBox) buttonSection.getChildren().get(0)).getChildren().get(0);
		Button mieiBtn = (Button) ((HBox) buttonSection.getChildren().get(0)).getChildren().get(1);

		setupTableDataBinding(table);
		setupFilters(nomeField, argomentoField, filtroStatoBtn, filtroFondatoreCheck, mostraTuttiBtn, mieiBtn);
		setupTableDoubleClick(table);
		loadDataAsync(true);

		return card;
	}

	private VBox createHeaderSection() {
		VBox headerSection = new VBox(15);
		headerSection.setAlignment(Pos.CENTER);

		Label title = StyleHelper.createTitleLabel("📚 Lista dei Corsi");

		HBox filters = new HBox(12);
		filters.setAlignment(Pos.CENTER);

		TextField nomeField = StyleHelper.createTextField("👨‍🍳 Cerca per nome...");
		nomeField.setPrefWidth(220);

		TextField argomentoField = StyleHelper.createTextField("📖 Cerca per argomento...");
		argomentoField.setPrefWidth(220);

		MenuButton filtroStatoBtn = createFiltroStatoButton();

		CheckBox filtroFondatoreCheck = new CheckBox("👑 Solo Fondatore");
		filtroFondatoreCheck.setStyle(
				"-fx-font-size: 13px;" + "-fx-font-weight: bold;" + "-fx-text-fill: " + StyleHelper.TEXT_BLACK + ";");
		filtroFondatoreCheck.setSelected(false);

		filters.getChildren().addAll(nomeField, argomentoField, filtroStatoBtn, filtroFondatoreCheck);
		headerSection.getChildren().addAll(title, filters);

		return headerSection;
	}

	private MenuButton createFiltroStatoButton() {
		CheckBox mostraNonFinitiCheck = new CheckBox(" Corsi non finiti");
		CheckBox mostraFinitiCheck = new CheckBox(" Corsi finiti");

		mostraNonFinitiCheck.setSelected(true);
		mostraFinitiCheck.setSelected(true);

		String checkStyle = "-fx-font-size: 13px; -fx-text-fill: " + StyleHelper.TEXT_BLACK + ";";
		mostraNonFinitiCheck.setStyle(checkStyle);
		mostraFinitiCheck.setStyle(checkStyle);

		CustomMenuItem item1 = new CustomMenuItem(mostraNonFinitiCheck);
		CustomMenuItem item2 = new CustomMenuItem(mostraFinitiCheck);
		item1.setHideOnClick(false);
		item2.setHideOnClick(false);

		MenuButton menuBtn = new MenuButton("🔍 Filtra Stato");
		menuBtn.setPrefHeight(42);
		menuBtn.setPrefWidth(180);
		menuBtn.setFont(Font.font("Roboto", FontWeight.BOLD, 13));

		StyleHelper.styleMenuButton(menuBtn);

		menuBtn.getItems().addAll(item1, item2);

		mostraNonFinitiCheck.selectedProperty().addListener((obs, old, val) -> {
			mostraNonFiniti = val;
			applicaFiltriConStato();
		});

		mostraFinitiCheck.selectedProperty().addListener((obs, old, val) -> {
			mostraFiniti = val;
			applicaFiltriConStato();
		});

		return menuBtn;
	}

	private void applicaFiltriConStato() {
		if (filteredCorsi == null)
			return;

		filteredCorsi.setPredicate(corso -> {
			if (corso == null)
				return false;

			LocalDateTime ora = LocalDateTime.now();
			boolean isFinito = corso.getDataFineCorso() != null && corso.getDataFineCorso().isBefore(ora);

			if (!mostraNonFiniti && !mostraFiniti)
				return false;
			if (mostraNonFiniti && mostraFiniti)
				return true;
			if (mostraNonFiniti && !isFinito)
				return true;
			if (mostraFiniti && isFinito)
				return true;

			return false;
		});
	}

	private VBox createButtonSection() {
		VBox buttonSection = new VBox(15);
		buttonSection.setAlignment(Pos.CENTER);

		HBox actionButtons = new HBox(15);
		actionButtons.setAlignment(Pos.CENTER);

		Button mostraTuttiBtn = StyleHelper.createInfoButton("📋 Tutti i Corsi");
		mostraTuttiBtn.setPrefSize(160, 45);

		Button mieiBtn = StyleHelper.createPrimaryButton("👨‍🍳 I Miei Corsi");
		mieiBtn.setPrefSize(160, 45);

		actionButtons.getChildren().addAll(mostraTuttiBtn, mieiBtn);
		buttonSection.getChildren().add(actionButtons);

		return buttonSection;
	}

	private TableView<CorsoCucina> createOptimizedTable() {
		TableView<CorsoCucina> table = new TableView<>();

		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

		table.setPrefHeight(Region.USE_COMPUTED_SIZE);
		table.setMaxHeight(Double.MAX_VALUE);

		table.setStyle("-fx-background-color: white;" + "-fx-background-radius: 12;" + "-fx-border-radius: 12;"
				+ "-fx-border-color: " + StyleHelper.BORDER_LIGHT + ";" + "-fx-border-width: 1;"
				+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0.0, 0.0, 2.0);");

		TableColumn<CorsoCucina, String> nomeCol = createNomeColumn();
		TableColumn<CorsoCucina, String> argomentoCol = createArgomentoColumn();
		TableColumn<CorsoCucina, Double> prezzoCol = createPrezzoColumn();
		TableColumn<CorsoCucina, Integer> sessioniCol = createSessioniColumn();
		TableColumn<CorsoCucina, String> freqCol = createFrequenzaColumn();
		TableColumn<CorsoCucina, Integer> postiCol = createPostiColumn();
		TableColumn<CorsoCucina, String> inizioCol = createInizioColumn();
		TableColumn<CorsoCucina, String> fineCol = createFineColumn();

		
		table.getColumns().add(nomeCol);
		table.getColumns().add(argomentoCol);
		table.getColumns().add(prezzoCol);
		table.getColumns().add(sessioniCol);
		table.getColumns().add(freqCol);
		table.getColumns().add(postiCol);
		table.getColumns().add(inizioCol);
		table.getColumns().add(fineCol);

		table.getSortOrder().add(nomeCol);

		setupResponsiveColumns(table, nomeCol, argomentoCol, prezzoCol, sessioniCol, freqCol, postiCol, inizioCol,
				fineCol);

		return table;
	}

	private TableColumn<CorsoCucina, String> createNomeColumn() {
		TableColumn<CorsoCucina, String> col = new TableColumn<>("📚 Nome Corso");
		col.setCellValueFactory(
				c -> new SimpleStringProperty(c.getValue().getNomeCorso() != null ? c.getValue().getNomeCorso() : ""));
		col.setPrefWidth(250);
		col.setMinWidth(200);
		return col;
	}

	private TableColumn<CorsoCucina, String> createArgomentoColumn() {
		TableColumn<CorsoCucina, String> col = new TableColumn<>("📖 Argomento");
		col.setCellValueFactory(
				c -> new SimpleStringProperty(c.getValue().getArgomento() != null ? c.getValue().getArgomento() : ""));
		col.setPrefWidth(130);
		col.setMinWidth(100);
		return col;
	}

	private TableColumn<CorsoCucina, Double> createPrezzoColumn() {
		TableColumn<CorsoCucina, Double> col = new TableColumn<>("💰 Prezzo");
		col.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrezzo()).asObject());
		col.setCellFactory(tc -> new TableCell<CorsoCucina, Double>() {
			@Override
			protected void updateItem(Double item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null : String.format("€%.0f", item));
			}
		});
		col.setPrefWidth(90);
		col.setMinWidth(80);
		return col;
	}

	private TableColumn<CorsoCucina, Integer> createSessioniColumn() {
		TableColumn<CorsoCucina, Integer> col = new TableColumn<>("⏰ Sessioni");
		col.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNumeroSessioni()).asObject());
		col.setPrefWidth(90);
		col.setMinWidth(80);
		return col;
	}

	private TableColumn<CorsoCucina, String> createFrequenzaColumn() {
		TableColumn<CorsoCucina, String> col = new TableColumn<>("📅 Frequenza");
		col.setCellValueFactory(c -> new SimpleStringProperty(
				c.getValue().getFrequenzaCorso() != null ? c.getValue().getFrequenzaCorso().toString() : ""));
		col.setPrefWidth(100);
		col.setMinWidth(90);
		return col;
	}

	private TableColumn<CorsoCucina, Integer> createPostiColumn() {
		TableColumn<CorsoCucina, Integer> col = new TableColumn<>("🪑 Posti");
		col.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNumeroPosti()).asObject());
		col.setPrefWidth(80);
		col.setMinWidth(70);
		return col;
	}

	private TableColumn<CorsoCucina, String> createInizioColumn() {
		TableColumn<CorsoCucina, String> col = new TableColumn<>("🕑 Inizio");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

		col.setCellValueFactory(c -> {
			if (c.getValue().getDataInizioCorso() != null) {
				String dataFormattata = c.getValue().getDataInizioCorso().toLocalDate().format(formatter);
				return new SimpleStringProperty(dataFormattata);
			}
			return new SimpleStringProperty("");
		});

		col.setPrefWidth(100);
		col.setMinWidth(90);
		return col;
	}

	private TableColumn<CorsoCucina, String> createFineColumn() {
		TableColumn<CorsoCucina, String> col = new TableColumn<>("🏁 Fine");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

		col.setCellValueFactory(c -> {
			if (c.getValue().getDataFineCorso() != null) {
				String dataFormattata = c.getValue().getDataFineCorso().toLocalDate().format(formatter);
				return new SimpleStringProperty(dataFormattata);
			}
			return new SimpleStringProperty("");
		});

		col.setPrefWidth(100);
		col.setMinWidth(90);
		return col;
	}

	private void setupResponsiveColumns(TableView<CorsoCucina> table, TableColumn<CorsoCucina, String> nomeCol,
			TableColumn<CorsoCucina, String> argomentoCol, TableColumn<CorsoCucina, Double> prezzoCol,
			TableColumn<CorsoCucina, Integer> sessioniCol, TableColumn<CorsoCucina, String> freqCol,
			TableColumn<CorsoCucina, Integer> postiCol, TableColumn<CorsoCucina, String> inizioCol,
			TableColumn<CorsoCucina, String> fineCol) {

		table.widthProperty().addListener((obs, oldWidth, newWidth) -> {
			double totalWidth = newWidth.doubleValue();
			if (totalWidth > 0 && totalWidth > 500) {
				nomeCol.setPrefWidth(totalWidth * 0.25);
				argomentoCol.setPrefWidth(totalWidth * 0.15);
				prezzoCol.setPrefWidth(totalWidth * 0.12);
				sessioniCol.setPrefWidth(totalWidth * 0.12);
				freqCol.setPrefWidth(totalWidth * 0.12);
				postiCol.setPrefWidth(totalWidth * 0.08);
				inizioCol.setPrefWidth(totalWidth * 0.08);
				fineCol.setPrefWidth(totalWidth * 0.08);
			}
		});
	}

	private void setupTableDataBinding(TableView<CorsoCucina> table) {
		filteredCorsi = new FilteredList<>(corsiData, c -> true);
		SortedList<CorsoCucina> sortedCorsi = new SortedList<>(filteredCorsi);
		sortedCorsi.comparatorProperty().bind(table.comparatorProperty());
		table.setItems(sortedCorsi);
	}

	private void setupTableDoubleClick(TableView<CorsoCucina> table) {
		table.setRowFactory(tv -> {
			TableRow<CorsoCucina> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !row.isEmpty()) {
					CorsoCucina selected = row.getItem();
					loadAndShowDettagli(selected);
				}
			});
			return row;
		});
	}

	private void loadAndShowDettagli(CorsoCucina corso) {
		Task<CorsoCucina> loadDetailsTask = new Task<CorsoCucina>() {
			@Override
			protected CorsoCucina call() throws Exception {
				return gestioneCorsoController != null ? gestioneCorsoController.getCorsoCompleto(corso.getIdCorso())
						: corso;
			}

			@Override
			protected void succeeded() {
				Platform.runLater(() -> apriDettagliCorso(getValue()));
			}

			@Override
			protected void failed() {
				Platform.runLater(() -> StyleHelper.showErrorDialog("Errore",
						"Impossibile caricare i dettagli: " + getException().getMessage()));
			}
		};

		new Thread(loadDetailsTask, "LoadDettagliThread").start();
	}

	private void apriDettagliCorso(CorsoCucina corso) {
		if (contentRoot == null) {
			StyleHelper.showErrorDialog("Errore", "Navigazione non disponibile");
			return;
		}

		try {
			if (ricettaController == null || ingredienteController == null) {
				StyleHelper.showErrorDialog("Errore",
						"Controller non inizializzati.\n\n" + "Impossibile aprire i dettagli del corso.");
				return;
			}

			DettagliCorsoGUI dettagliGUI = new DettagliCorsoGUI();
			dettagliGUI.setController(gestioneCorsoController);
			dettagliGUI.setChefController(chefController);
			dettagliGUI.setRicettaController(ricettaController);
			dettagliGUI.setIngredienteController(ingredienteController);
			dettagliGUI.setCorso(corso);

			dettagliGUI.setOnChiudiCallback(() -> {
				VisualizzaCorsiGUI nuovaListaCorsi = new VisualizzaCorsiGUI();
				nuovaListaCorsi.setControllers(visualizzaController, gestioneCorsoController, chefController,
						contentRoot);
				nuovaListaCorsi.setRicettaController(ricettaController);
				nuovaListaCorsi.setIngredienteController(ingredienteController);
				contentRoot.getChildren().setAll(nuovaListaCorsi.getRoot());
			});

			contentRoot.getChildren().setAll(dettagliGUI.getRoot());

		} catch (Exception ex) {
			StyleHelper.showErrorDialog("Errore", "Errore apertura dettagli: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	private void loadDataAsync(boolean tuttiICorsi) {
		if (visualizzaController == null) {
			progressIndicator.setVisible(false);
			StyleHelper.showErrorDialog("Errore", "Controller non inizializzato");
			return;
		}

		Task<List<CorsoCucina>> loadTask = new Task<List<CorsoCucina>>() {
			@Override
			protected List<CorsoCucina> call() throws Exception {
				List<CorsoCucina> list;
				if (tuttiICorsi) {
					list = visualizzaController.getTuttiICorsi();
				} else {
					list = visualizzaController.getCorsiDelChef();
				}
				return list == null ? Collections.emptyList() : list;
			}

			@Override
			protected void succeeded() {
				Platform.runLater(() -> {
					List<CorsoCucina> result = getValue();
					for (CorsoCucina corso : result) {
						try {
							int numSessioni = visualizzaController.getNumeroSessioniPerCorso(corso.getIdCorso());
							corso.setNumeroSessioni(numSessioni);
						} catch (SQLException e) {
							corso.setNumeroSessioni(0);
						}
					}

					corsiData.setAll(result);
					progressIndicator.setVisible(false);
					applicaFiltriConStato();
				});
			}

			@Override
			protected void failed() {
				Platform.runLater(() -> {
					progressIndicator.setVisible(false);
					StyleHelper.showErrorDialog("Errore", "Errore caricamento corsi: " + getException().getMessage());
				});
			}
		};

		progressIndicator.setVisible(true);
		new Thread(loadTask, "LoadCorsiThread").start();
	}

	private void setupFilters(TextField nomeField, TextField argomentoField, MenuButton filtroStatoBtn,
			CheckBox filtroFondatoreCheck, Button mostraTuttiBtn, Button mieiBtn) {

		nomeField.textProperty().addListener((obs, oldValue, newValue) -> {
			filterPause.setOnFinished(event -> applicaFiltriLocali(nomeField.getText(), argomentoField.getText()));
			filterPause.playFromStart();
		});

		argomentoField.textProperty().addListener((obs, oldValue, newValue) -> {
			filterPause.setOnFinished(event -> applicaFiltriLocali(nomeField.getText(), argomentoField.getText()));
			filterPause.playFromStart();
		});

		filtroFondatoreCheck.selectedProperty().addListener((obs, old, val) -> {
			filtroFondatoreAttivo = val;
			applicaFiltriLocali(nomeField.getText(), argomentoField.getText());
		});

		mostraTuttiBtn.setOnAction(e -> {
			nomeField.clear();
			argomentoField.clear();
			filtroFondatoreCheck.setSelected(false);
			mostraNonFiniti = true;
			mostraFiniti = true;
			loadDataAsync(true);
		});

		mieiBtn.setOnAction(e -> {
			nomeField.clear();
			argomentoField.clear();
			filtroFondatoreCheck.setSelected(false);
			loadDataAsync(false);
		});
	}

	private void applicaFiltriLocali(String nome, String argomento) {
		if (filteredCorsi == null)
			return;

		String nomeLower = nome == null ? "" : nome.toLowerCase().trim();
		String argomentoLower = argomento == null ? "" : argomento.toLowerCase().trim();

		filteredCorsi.setPredicate(c -> {
			if (c == null)
				return false;

			LocalDateTime ora = LocalDateTime.now();
			boolean isFinito = c.getDataFineCorso() != null && c.getDataFineCorso().isBefore(ora);

			boolean passaFiltroStato = false;
			if (mostraNonFiniti && mostraFiniti)
				passaFiltroStato = true;
			else if (mostraNonFiniti && !isFinito)
				passaFiltroStato = true;
			else if (mostraFiniti && isFinito)
				passaFiltroStato = true;

			if (!passaFiltroStato)
				return false;

			boolean matchNome = nomeLower.isEmpty()
					|| (c.getNomeCorso() != null && c.getNomeCorso().toLowerCase().contains(nomeLower));

			boolean matchArgomento = argomentoLower.isEmpty()
					|| (c.getArgomento() != null && c.getArgomento().toLowerCase().contains(argomentoLower));

			boolean matchFondatore = true;
			if (filtroFondatoreAttivo && visualizzaController != null) {
				String cfLoggato = visualizzaController.getChefLoggato().getCodFiscale();
				matchFondatore = c.getCodfiscaleFondatore() != null
						&& c.getCodfiscaleFondatore().equalsIgnoreCase(cfLoggato);
			}

			return matchNome && matchArgomento && matchFondatore;
		});
	}
}