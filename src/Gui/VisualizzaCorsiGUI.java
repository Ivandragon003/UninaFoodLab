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
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import model.CorsoCucina;

import java.time.LocalDateTime;
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
	private List<CorsoCucina> cachedCorsiChef = null;

	private ProgressIndicator progressIndicator;
	private final PauseTransition filterPause = new PauseTransition(Duration.millis(350));

	private boolean mostraNonFiniti = true;
	private boolean mostraFiniti = true;

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
		table.setPrefHeight(500); // ✅ Altezza fissa invece di binding
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
		Button mostraTuttiBtn = (Button) ((HBox) buttonSection.getChildren().get(0)).getChildren().get(0);
		Button mieiBtn = (Button) ((HBox) buttonSection.getChildren().get(0)).getChildren().get(1);

		setupTableDataBinding(table);
		setupFilters(nomeField, argomentoField, filtroStatoBtn, mostraTuttiBtn, mieiBtn);
		setupTableDoubleClick(table);

		loadDataAsync();

		return card;
	}

	private VBox createHeaderSection() {
		VBox headerSection = new VBox(15);
		headerSection.setAlignment(Pos.CENTER);

		// ✅ USA StyleHelper per il titolo
		Label title = StyleHelper.createTitleLabel("📚 Lista dei Corsi");

		HBox filters = new HBox(12);
		filters.setAlignment(Pos.CENTER);

		// ✅ USA StyleHelper.createTextField
		TextField nomeField = StyleHelper.createTextField("👨‍🍳 Cerca per nome...");
		nomeField.setPrefWidth(220);

		TextField argomentoField = StyleHelper.createTextField("📖 Cerca per argomento...");
		argomentoField.setPrefWidth(220);

		MenuButton filtroStatoBtn = createFiltroStatoButton();

		filters.getChildren().addAll(nomeField, argomentoField, filtroStatoBtn);

		headerSection.getChildren().addAll(title, filters);
		return headerSection;
	}

	private MenuButton createFiltroStatoButton() {
		CheckBox mostraNonFinitiCheck = new CheckBox("  Corsi non finiti");
		CheckBox mostraFinitiCheck = new CheckBox("  Corsi finiti");

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

		// ✅ USA colori di StyleHelper
		menuBtn.setStyle("-fx-background-color: " + StyleHelper.PRIMARY_ORANGE + ";" + "-fx-text-fill: white;"
				+ "-fx-background-radius: 20;" + "-fx-cursor: hand;" + "-fx-font-weight: bold;"
				+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 8, 0.4, 1.5, 3.5);");

		menuBtn.setOnMouseEntered(e -> menuBtn.setStyle("-fx-background-color: derive(" + StyleHelper.PRIMARY_ORANGE
				+ ", -15%);" + "-fx-text-fill: white;" + "-fx-background-radius: 20;" + "-fx-cursor: hand;"
				+ "-fx-font-weight: bold;" + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.28), 10, 0.5, 2.0, 4.5);"));

		menuBtn.setOnMouseExited(e -> menuBtn.setStyle("-fx-background-color: " + StyleHelper.PRIMARY_ORANGE + ";"
				+ "-fx-text-fill: white;" + "-fx-background-radius: 20;" + "-fx-cursor: hand;"
				+ "-fx-font-weight: bold;" + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 8, 0.4, 1.5, 3.5);"));

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

		// ✅ USA StyleHelper per i bottoni
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
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		table.setPrefHeight(Region.USE_COMPUTED_SIZE);
		table.setMaxHeight(Double.MAX_VALUE);

		// ✅ USA colori di StyleHelper
		table.setStyle("-fx-background-color: white;" + "-fx-background-radius: 12;" + "-fx-border-radius: 12;"
				+ "-fx-border-color: " + StyleHelper.BORDER_LIGHT + ";" + "-fx-border-width: 1;"
				+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0.0, 0.0, 2.0);");

		TableColumn<CorsoCucina, Integer> idCol = createIdColumn();
		TableColumn<CorsoCucina, String> nomeCol = createNomeColumn();
		TableColumn<CorsoCucina, String> argomentoCol = createArgomentoColumn();
		TableColumn<CorsoCucina, Double> prezzoCol = createPrezzoColumn();
		TableColumn<CorsoCucina, Integer> sessioniCol = createSessioniColumn();
		TableColumn<CorsoCucina, String> freqCol = createFrequenzaColumn();
		TableColumn<CorsoCucina, Integer> postiCol = createPostiColumn();
		TableColumn<CorsoCucina, String> inizioCol = createInizioColumn();
		TableColumn<CorsoCucina, String> fineCol = createFineColumn();

		table.getColumns().addAll(idCol, nomeCol, argomentoCol, prezzoCol, sessioniCol, freqCol, postiCol, inizioCol,
				fineCol);
		table.getSortOrder().add(nomeCol);

		setupResponsiveColumns(table, idCol, nomeCol, argomentoCol, prezzoCol, sessioniCol, freqCol, postiCol,
				inizioCol, fineCol);

		return table;
	}

	private TableColumn<CorsoCucina, Integer> createIdColumn() {
		TableColumn<CorsoCucina, Integer> col = new TableColumn<>("ID");
		col.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getIdCorso()).asObject());
		col.setPrefWidth(60);
		col.setMinWidth(50);
		col.setMaxWidth(70);
		col.setResizable(false);
		return col;
	}

	private TableColumn<CorsoCucina, String> createNomeColumn() {
		TableColumn<CorsoCucina, String> col = new TableColumn<>("📚 Nome Corso");
		col.setCellValueFactory(
				c -> new SimpleStringProperty(c.getValue().getNomeCorso() != null ? c.getValue().getNomeCorso() : ""));
		col.setPrefWidth(200);
		col.setMinWidth(150);
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
		col.setCellFactory(tc -> new TableCell<>() {
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
		col.setCellValueFactory(c -> new SimpleStringProperty(
				c.getValue().getDataInizioCorso() != null ? c.getValue().getDataInizioCorso().toLocalDate().toString()
						: ""));
		col.setPrefWidth(100);
		col.setMinWidth(90);
		return col;
	}

	private TableColumn<CorsoCucina, String> createFineColumn() {
		TableColumn<CorsoCucina, String> col = new TableColumn<>("🏁 Fine");
		col.setCellValueFactory(c -> new SimpleStringProperty(
				c.getValue().getDataFineCorso() != null ? c.getValue().getDataFineCorso().toLocalDate().toString()
						: ""));
		col.setPrefWidth(100);
		col.setMinWidth(90);
		return col;
	}

	private void setupResponsiveColumns(TableView<CorsoCucina> table, TableColumn<CorsoCucina, Integer> idCol,
			TableColumn<CorsoCucina, String> nomeCol, TableColumn<CorsoCucina, String> argomentoCol,
			TableColumn<CorsoCucina, Double> prezzoCol, TableColumn<CorsoCucina, Integer> sessioniCol,
			TableColumn<CorsoCucina, String> freqCol, TableColumn<CorsoCucina, Integer> postiCol,
			TableColumn<CorsoCucina, String> inizioCol, TableColumn<CorsoCucina, String> fineCol) {

		table.widthProperty().addListener((obs, oldWidth, newWidth) -> {
			double totalWidth = newWidth.doubleValue();
			if (totalWidth > 0) {
				double flexibleWidth = totalWidth - idCol.getWidth() - 50;
				if (flexibleWidth > 500) {
					nomeCol.setPrefWidth(flexibleWidth * 0.25);
					argomentoCol.setPrefWidth(flexibleWidth * 0.15);
					prezzoCol.setPrefWidth(flexibleWidth * 0.12);
					sessioniCol.setPrefWidth(flexibleWidth * 0.12);
					freqCol.setPrefWidth(flexibleWidth * 0.12);
					postiCol.setPrefWidth(flexibleWidth * 0.08);
					inizioCol.setPrefWidth(flexibleWidth * 0.08);
					fineCol.setPrefWidth(flexibleWidth * 0.08);
				}
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
		Task<CorsoCucina> loadDetailsTask = new Task<>() {
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

	private void loadDataAsync() {
		if (visualizzaController == null) {
			progressIndicator.setVisible(false);
			StyleHelper.showErrorDialog("Errore", "Controller non inizializzato");
			return;
		}

		Task<List<CorsoCucina>> loadTask = new Task<>() {
			@Override
			protected List<CorsoCucina> call() throws Exception {
				long start = System.currentTimeMillis();
				List<CorsoCucina> list = visualizzaController.getTuttiICorsi();
				long end = System.currentTimeMillis();
				System.out.println("[VisualizzaCorsiGUI] Caricati " + (list == null ? 0 : list.size()) + " corsi in "
						+ (end - start) + " ms");
				return list == null ? Collections.emptyList() : list;
			}

			@Override
			protected void succeeded() {
				Platform.runLater(() -> {
					corsiData.setAll(getValue());
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
			Button mostraTuttiBtn, Button mieiBtn) {

		nomeField.textProperty().addListener((obs, oldValue, newValue) -> {
			filterPause
					.setOnFinished(event -> applicaFiltriLocali(nomeField.getText(), argomentoField.getText(), false));
			filterPause.playFromStart();
		});

		argomentoField.textProperty().addListener((obs, oldValue, newValue) -> {
			filterPause
					.setOnFinished(event -> applicaFiltriLocali(nomeField.getText(), argomentoField.getText(), false));
			filterPause.playFromStart();
		});

		mostraTuttiBtn.setOnAction(e -> {
			nomeField.clear();
			argomentoField.clear();
			mostraNonFiniti = true;
			mostraFiniti = true;
			applicaFiltriConStato();
		});

		mieiBtn.setOnAction(e -> applicaFiltriLocali(nomeField.getText(), argomentoField.getText(), true));
	}

	private void applicaFiltriLocali(String nome, String argomento, boolean soloChefLoggato) {
		if (filteredCorsi == null)
			return;

		progressIndicator.setVisible(true);

		Task<Void> filterTask = new Task<>() {
			@Override
			protected Void call() throws Exception {
				if (soloChefLoggato && visualizzaController != null) {
					List<CorsoCucina> corsiChef = visualizzaController.getCorsiChefLoggato();
					cachedCorsiChef = (corsiChef == null) ? Collections.emptyList() : corsiChef;

					System.out.println("[DEBUG] Corsi dello chef loggato: "
							+ (cachedCorsiChef == null ? "NULL" : cachedCorsiChef.size()));
				}
				return null;
			}

			@Override
			protected void succeeded() {
				Platform.runLater(() -> {
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
						boolean matchArgomento = argomentoLower.isEmpty() || (c.getArgomento() != null
								&& c.getArgomento().toLowerCase().contains(argomentoLower));
						boolean match = matchNome && matchArgomento;

						if (soloChefLoggato) {
							if (cachedCorsiChef == null || cachedCorsiChef.isEmpty()) {
								return false;
							}

							boolean isChefCorso = cachedCorsiChef.stream()
									.anyMatch(corsoChef -> corsoChef.getIdCorso() == c.getIdCorso());

							return match && isChefCorso;
						}

						return match;
					});

					progressIndicator.setVisible(false);

					// ✅ USA StyleHelper.showInfoDialog
					if (soloChefLoggato && filteredCorsi.isEmpty()) {
						StyleHelper.showInfoDialog("Nessun Corso Trovato",
								"Non ci sono corsi assegnati a questo chef.\n\n"
										+ "Verifica di essere loggato come chef\ne di avere corsi assegnati.");
					}
				});
			}

			@Override
			protected void failed() {
				Platform.runLater(() -> {
					progressIndicator.setVisible(false);
					StyleHelper.showErrorDialog("Errore",
							"Errore durante il filtraggio:\n" + getException().getMessage());
				});
			}
		};

		new Thread(filterTask, "FilterThread").start();
	}
}
