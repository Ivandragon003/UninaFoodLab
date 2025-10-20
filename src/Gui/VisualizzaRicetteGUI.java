package Gui;

import controller.RicettaController;
import controller.IngredienteController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Ricetta;
import model.Ingrediente;
import guihelper.StyleHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisualizzaRicetteGUI {
	private final RicettaController ricettaController;
	private final IngredienteController ingredienteController;
	private final ObservableList<Ricetta> ricetteData;
	private ListView<Ricetta> ricetteListView;
	private TextField filtroNome, filtroTempoMin, filtroTempoMax;
	private TextField filtroIngMin, filtroIngMax;

	private VBox mainContainer;
	private VBox listaView;
	private VBox modificaView;

	private TextField nomeFieldModifica;
	private TextField tempoFieldModifica;
	private VBox listaIngredientiModifica;
	private Map<Ingrediente, Double> modificaIngredientiMap;

	public VisualizzaRicetteGUI(RicettaController ricettaController, IngredienteController ingredienteController) {
		if (ricettaController == null || ingredienteController == null) {
			throw new IllegalArgumentException("I controller non possono essere null");
		}
		this.ricettaController = ricettaController;
		this.ingredienteController = ingredienteController;
		this.ricetteData = FXCollections.observableArrayList();
		carica();
	}

	public VBox getRoot() {
		if (mainContainer == null) {
			mainContainer = new VBox();
			listaView = buildListaLayout();
			mainContainer.getChildren().add(listaView);
			VBox.setVgrow(listaView, Priority.ALWAYS);
		}
		return mainContainer;
	}

	// ==================== LAYOUT LISTA ====================

	private VBox buildListaLayout() {
		VBox container = new VBox(15);
		container.setPadding(new Insets(20));
		StyleHelper.applyBackgroundGradient(container);

		Label title = StyleHelper.createTitleLabel("📖 Gestione Ricette");
		title.setAlignment(Pos.CENTER);
		title.setTextFill(Color.WHITE);

		VBox scrollContent = new VBox(15, buildFiltri(), new Separator(), buildLista());

		ScrollPane scroll = new ScrollPane(scrollContent);
		scroll.setFitToWidth(true);
		scroll.setFitToHeight(true);
		scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
		VBox.setVgrow(scroll, Priority.ALWAYS);

		Separator bottomSep = new Separator();
		HBox buttons = buildButtons();

		container.getChildren().addAll(title, scroll, bottomSep, buttons);
		setupListeners();
		return container;
	}

	private VBox buildFiltri() {
		VBox section = StyleHelper.createSection();
		Label title = createTitle("🔍 Filtri Avanzati");

		filtroNome = StyleHelper.createTextField("Cerca per nome...");
		filtroNome.setPrefWidth(300);

		filtroTempoMin = StyleHelper.createTextField("Min");
		filtroTempoMax = StyleHelper.createTextField("Max");
		filtroIngMin = StyleHelper.createTextField("Min");
		filtroIngMax = StyleHelper.createTextField("Max");

		for (TextField f : new TextField[] { filtroTempoMin, filtroTempoMax, filtroIngMin, filtroIngMax }) {
			f.setPrefWidth(80);
		}

		GridPane grid = new GridPane();
		grid.setHgap(15);
		grid.setVgap(10);
		grid.add(StyleHelper.createLabel("Nome:"), 0, 0);
		grid.add(filtroNome, 1, 0, 3, 1);
		grid.add(StyleHelper.createLabel("Tempo:"), 0, 1);
		grid.add(createRange(filtroTempoMin, filtroTempoMax, "min"), 1, 1);
		grid.add(StyleHelper.createLabel("Ingredienti:"), 2, 1);
		grid.add(createRange(filtroIngMin, filtroIngMax, ""), 3, 1);

		Button resetBtn = StyleHelper.createInfoButton("🔄 Reset");
		resetBtn.setOnAction(e -> reset());
		grid.add(resetBtn, 4, 1);

		section.getChildren().addAll(title, grid);
		return section;
	}

	private VBox buildLista() {
		VBox section = StyleHelper.createSection();

		Label title = createTitle("📋 Lista Ricette");
		Label info = new Label("💡 Doppio click per modificare una ricetta");
		info.setFont(Font.font("Roboto", 12));
		info.setTextFill(Color.web(StyleHelper.INFO_BLUE));

		HBox header = new HBox(10);
		header.setAlignment(Pos.CENTER_LEFT);
		header.getChildren().add(new VBox(5, title, info));

		ricetteListView = new ListView<>(ricetteData);
		ricetteListView.setPrefHeight(400);
		ricetteListView.setStyle("-fx-background-color: white;" + "-fx-border-color: " + StyleHelper.BORDER_LIGHT + ";"
				+ "-fx-border-radius: 8;" + "-fx-background-radius: 8;" + "-fx-border-width: 1;");
		ricetteListView.setCellFactory(lv -> new RicettaCell());
		ricetteListView.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2) {
				Ricetta sel = ricetteListView.getSelectionModel().getSelectedItem();
				if (sel != null) {
					mostraModifica(sel);
				}
			}
		});

		section.getChildren().addAll(header, ricetteListView);
		VBox.setVgrow(ricetteListView, Priority.ALWAYS);
		return section;
	}

	private HBox buildButtons() {
		Button aggiornaBtn = StyleHelper.createInfoButton("🔄 Aggiorna");
		aggiornaBtn.setPrefWidth(150);
		aggiornaBtn.setOnAction(e -> ricarica());

		HBox box = new HBox(15, aggiornaBtn);
		box.setAlignment(Pos.CENTER);
		box.setPadding(new Insets(15, 0, 5, 0));
		return box;
	}

	// ==================== LAYOUT MODIFICA ====================

	private VBox buildModificaLayout(Ricetta ricetta) {
		VBox container = new VBox(15);
		container.setPadding(new Insets(20));
		StyleHelper.applyBackgroundGradient(container);

		Label title = StyleHelper.createTitleLabel("📖 Modifica: " + ricetta.getNome());
		title.setTextFill(Color.WHITE);
		title.setAlignment(Pos.CENTER);

		// Inizializza la mappa solo se è null (così non sovrascriviamo le modifiche già
		// fatte)
		if (modificaIngredientiMap == null) {
			modificaIngredientiMap = new HashMap<>(ricetta.getIngredienti());
		}

		ScrollPane scroll = new ScrollPane(new VBox(15, buildModificaInfoSection(ricetta), new Separator(),
				buildModificaIngredientiSection(ricetta)));
		scroll.setFitToWidth(true);
		scroll.setFitToHeight(true);
		scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
		VBox.setVgrow(scroll, Priority.ALWAYS);

		Separator bottomSep = new Separator();
		HBox buttons = buildModificaButtons(ricetta);

		container.getChildren().addAll(title, scroll, bottomSep, buttons);
		VBox.setVgrow(scroll, Priority.ALWAYS);
		return container;
	}

	private VBox buildModificaInfoSection(Ricetta ricetta) {
		VBox section = StyleHelper.createSection();
		Label title = createTitle("📝 Informazioni Base");

		nomeFieldModifica = StyleHelper.createTextField("Nome ricetta");
		nomeFieldModifica.setText(ricetta.getNome());
		nomeFieldModifica.setPrefHeight(45);

		tempoFieldModifica = StyleHelper.createTextField("Tempo (min)");
		tempoFieldModifica.setText(String.valueOf(ricetta.getTempoPreparazione()));
		tempoFieldModifica.setPrefHeight(45);
		tempoFieldModifica.textProperty().addListener((obs, old, val) -> {
			if (!val.matches("\\d*"))
				tempoFieldModifica.setText(old);
		});

		GridPane grid = new GridPane();
		grid.setHgap(15);
		grid.setVgap(15);
		grid.add(StyleHelper.createLabel("Nome:"), 0, 0);
		grid.add(nomeFieldModifica, 1, 0);
		grid.add(StyleHelper.createLabel("Tempo (min):"), 0, 1);
		grid.add(tempoFieldModifica, 1, 1);

		section.getChildren().addAll(title, grid);
		return section;
	}

	private VBox buildModificaIngredientiSection(Ricetta ricetta) {
		VBox section = StyleHelper.createSection();
		Label title = createTitle("🥕 Gestione Ingredienti");

		Button aggiungiBtn = StyleHelper.createSuccessButton("➕ Aggiungi Ingrediente");
		aggiungiBtn.setPrefWidth(220);

		Label info = new Label("💡 Modifica la quantità direttamente. Clicca ✕ per rimuovere.");
		info.setFont(Font.font("Roboto", 12));
		info.setTextFill(Color.web(StyleHelper.INFO_BLUE));
		info.setWrapText(true);

		listaIngredientiModifica = new VBox(12);
		listaIngredientiModifica.setPadding(new Insets(15));
		listaIngredientiModifica.setMinHeight(200);
		listaIngredientiModifica.setStyle("-fx-background-color: " + StyleHelper.BG_LIGHT + ";"
				+ "-fx-background-radius: 12;" + "-fx-border-color: " + StyleHelper.BORDER_LIGHT + ";"
				+ "-fx-border-radius: 12;" + "-fx-border-width: 1;");

		aggiornaListaIngredienti();

		aggiungiBtn.setOnAction(e -> mostraSelezinaIngredientePerModifica(ricetta));

		section.getChildren().addAll(title, aggiungiBtn, info, listaIngredientiModifica);
		return section;
	}

	private void aggiornaListaIngredienti() {
		listaIngredientiModifica.getChildren().clear();
		if (modificaIngredientiMap == null || modificaIngredientiMap.isEmpty()) {
			Label empty = new Label("Nessun ingrediente");
			empty.setFont(Font.font("Roboto", 13));
			empty.setTextFill(Color.GRAY);
			empty.setStyle("-fx-font-style: italic;");
			listaIngredientiModifica.getChildren().add(empty);
		} else {
			modificaIngredientiMap.forEach((ing, q) -> {
				HBox row = createIngredienteRowModifica(ing, q);
				listaIngredientiModifica.getChildren().add(row);
			});
		}
	}

	private HBox createIngredienteRowModifica(Ingrediente ing, double quantita) {
		HBox box = new HBox(12);
		box.setAlignment(Pos.CENTER_LEFT);
		box.setPadding(new Insets(12));
		box.setStyle("-fx-background-color: white;" + "-fx-background-radius: 10;" + "-fx-border-color: "
				+ StyleHelper.PRIMARY_ORANGE + ";" + "-fx-border-radius: 10;" + "-fx-border-width: 2;");

		Label icon = new Label("🥕");
		icon.setFont(Font.font("Segoe UI Emoji", 20));

		VBox info = new VBox(4);
		Label nomeLabel = new Label(ing.getNome());
		nomeLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
		nomeLabel.setTextFill(Color.BLACK);

		Label tipoLabel = new Label("📂 " + ing.getTipo());
		tipoLabel.setFont(Font.font("Roboto", 12));
		tipoLabel.setTextFill(Color.GRAY);

		info.getChildren().addAll(nomeLabel, tipoLabel);

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		TextField quantField = new TextField(String.format("%.0f", quantita));
		quantField.setPrefWidth(80);
		quantField.setPrefHeight(35);
		quantField.setAlignment(Pos.CENTER);
		quantField.setStyle("-fx-background-color: white;" + "-fx-border-color: " + StyleHelper.PRIMARY_ORANGE + ";"
				+ "-fx-border-width: 2;" + "-fx-border-radius: 8;" + "-fx-background-radius: 8;"
				+ "-fx-padding: 5 10;");
		// permetti numeri interi e decimali con punto
		quantField.textProperty().addListener((obs, old, val) -> {
			if (!val.matches("^\\d*\\.?\\d*$")) {
				quantField.setText(old);
			} else if (!val.isEmpty()) {
				try {
					double nuovaQ = Double.parseDouble(val);
					if (nuovaQ > 0) {
						// aggiorna la mappa e la visuale
						modificaIngredientiMap.put(ing, nuovaQ);
						aggiornaListaIngredienti();
					}
				} catch (NumberFormatException e) {
				}
			}
		});

		Label unit = new Label("g");
		unit.setFont(Font.font("Roboto", FontWeight.SEMI_BOLD, 13));
		unit.setTextFill(Color.web(StyleHelper.TEXT_GRAY));

		Button rimuoviBtn = StyleHelper.createDangerButton("✕");
		rimuoviBtn.setPrefSize(40, 40);
		rimuoviBtn.setOnAction(e -> {
			modificaIngredientiMap.remove(ing);
			aggiornaListaIngredienti();
		});

		box.getChildren().addAll(icon, info, spacer, quantField, unit, rimuoviBtn);
		return box;
	}

	private HBox buildModificaButtons(Ricetta ricetta) {
		Button indietroBtn = StyleHelper.createSecondaryButton("← Indietro");
		indietroBtn.setPrefWidth(140);
		indietroBtn.setOnAction(e -> mostraLista());

		Button eliminaBtn = StyleHelper.createDangerButton("🗑️ Elimina");
		eliminaBtn.setPrefWidth(140);
		eliminaBtn.setOnAction(e -> StyleHelper.showConfirmationDialog("Conferma Eliminazione",
				"Sei sicuro di voler eliminare '" + ricetta.getNome() + "'?", () -> {
					try {
						ricettaController.eliminaRicetta(ricetta.getIdRicetta());
						carica();
						StyleHelper.showSuccessDialog("✅ Successo", "Ricetta eliminata");
						mostraLista();
					} catch (Exception ex) {
						StyleHelper.showErrorDialog("Errore", ex.getMessage());
					}
				}));

		Button salvaBtn = StyleHelper.createSuccessButton("💾 Salva");
		salvaBtn.setPrefWidth(140);
		salvaBtn.setOnAction(e -> salvaModifica(ricetta));

		HBox box = new HBox(15, indietroBtn, eliminaBtn, salvaBtn);
		box.setAlignment(Pos.CENTER);
		box.setPadding(new Insets(15, 0, 5, 0));
		return box;
	}

	// ==================== SELEZIONE INGREDIENTE PER MODIFICA ====================

	private void mostraSelezinaIngredientePerModifica(Ricetta ricetta) {
		VBox selezionaView = new VBox(20);
		selezionaView.setPadding(new Insets(20));
		StyleHelper.applyBackgroundGradient(selezionaView);

		Label title = StyleHelper.createTitleLabel("🥕 Seleziona Ingrediente");
		title.setTextFill(Color.WHITE);
		title.setAlignment(Pos.CENTER);

		VisualizzaIngredientiGUI selGUI = new VisualizzaIngredientiGUI(ingredienteController);
		selGUI.setModalitaSelezione(true);
		selGUI.setOnIngredienteSelezionato(ing -> {

			boolean giaPresente = modificaIngredientiMap.keySet().stream()
					.anyMatch(i -> i.getIdIngrediente() == ing.getIdIngrediente());

			if (giaPresente) {
				StyleHelper.showValidationDialog("Attenzione",
						"L'ingrediente '" + ing.getNome() + "' è già presente nella ricetta");
			} else {
				chiediQuantitaPerModifica(ing, ricetta);
			}
		});

		VBox content = selGUI.getContent();
		VBox.setVgrow(content, Priority.ALWAYS);

		Button indietroBtn = StyleHelper.createSecondaryButton("← Indietro");
		indietroBtn.setPrefWidth(150);
		indietroBtn.setOnAction(e -> mainContainer.getChildren().setAll(modificaView));

		HBox buttons = new HBox(indietroBtn);
		buttons.setAlignment(Pos.CENTER);
		buttons.setPadding(new Insets(15, 0, 5, 0));

		selezionaView.getChildren().addAll(title, content, new Separator(), buttons);
		mainContainer.getChildren().setAll(selezionaView);
	}

	private void chiediQuantitaPerModifica(Ingrediente ing, Ricetta ricetta) {
		mostraDialogQuantita(ing, q -> {
			modificaIngredientiMap.put(ing, q);
			// Ricostruiamo la view: buildModificaLayout non sovrascriverà più la mappa
			modificaView = buildModificaLayout(ricetta);
			mainContainer.getChildren().setAll(modificaView);
			StyleHelper.showSuccessDialog("✅ Ingrediente Aggiunto",
					String.format("'%s' aggiunto con %.0fg", ing.getNome(), q));
		});
	}

	private void mostraDialogQuantita(Ingrediente ing, java.util.function.Consumer<Double> onSuccess) {
		VBox dialogView = new VBox(30);
		dialogView.setPadding(new Insets(40));
		dialogView.setAlignment(Pos.CENTER);

		// ✅ SFONDO ARANCIONE PIENO come nelle foto originali
		dialogView.setStyle("-fx-background-color: #FF9B7A;" // Arancione chiaro uniforme
		);

		// ✅ TITOLO PRINCIPALE con ombra
		Label title = new Label("🥕 Quantità Ingrediente");
		title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 38));
		title.setTextFill(Color.WHITE);
		title.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0.4, 0, 2);");
		title.setAlignment(Pos.CENTER);

		// ✅ CARD CENTRALE bianca ben centrata
		VBox card = new VBox(25);
		card.setAlignment(Pos.CENTER);
		card.setPadding(new Insets(40));
		card.setMaxWidth(700);
		card.setMinHeight(450);
		card.setStyle("-fx-background-color: white;" + "-fx-background-radius: 20;"
				+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0.2, 0, 5);");

		// ✅ ICONA GRANDE per l'ingrediente
		Label iconLabel = new Label("🥕");
		iconLabel.setFont(Font.font("Segoe UI Emoji", 72));
		iconLabel.setAlignment(Pos.CENTER);

		// ✅ NOME INGREDIENTE
		Label nomeLabel = new Label(ing.getNome());
		nomeLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 28));
		nomeLabel.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));
		nomeLabel.setAlignment(Pos.CENTER);
		nomeLabel.setWrapText(true);
		nomeLabel.setMaxWidth(600);

		// ✅ BADGE per il tipo
		Label tipoLabel = new Label("📂 " + ing.getTipo());
		tipoLabel.setFont(Font.font("Roboto", FontWeight.SEMI_BOLD, 16));
		tipoLabel.setTextFill(Color.WHITE);
		tipoLabel.setAlignment(Pos.CENTER);
		tipoLabel.setStyle("-fx-background-color: " + StyleHelper.INFO_BLUE + ";" + "-fx-padding: 8 24;"
				+ "-fx-background-radius: 20;");

		VBox headerBox = new VBox(15, iconLabel, nomeLabel, tipoLabel);
		headerBox.setAlignment(Pos.CENTER);

		// ✅ SEPARATORE
		Separator sep = new Separator();
		sep.setMaxWidth(550);
		sep.setStyle("-fx-background-color: " + StyleHelper.BORDER_LIGHT + ";");

		// ✅ ISTRUZIONI
		Label istruzioni = new Label("Inserisci la quantità in grammi:");
		istruzioni.setFont(Font.font("Roboto", FontWeight.BOLD, 17));
		istruzioni.setTextFill(Color.web(StyleHelper.TEXT_BLACK));
		istruzioni.setAlignment(Pos.CENTER);

		// ✅ CAMPO QUANTITÀ grande e centrato
		TextField quantField = new TextField();
		quantField.setPromptText("Es. 250");
		quantField.setPrefWidth(280);
		quantField.setPrefHeight(70);
		quantField.setAlignment(Pos.CENTER);
		quantField.setStyle("-fx-background-color: white;" + "-fx-border-color: " + StyleHelper.PRIMARY_ORANGE + ";"
				+ "-fx-border-width: 3;" + "-fx-border-radius: 15;" + "-fx-background-radius: 15;"
				+ "-fx-font-size: 32px;" + "-fx-font-weight: bold;" + "-fx-prompt-text-fill: #ccc;"
				+ "-fx-effect: dropshadow(gaussian, rgba(255,107,53,0.15), 6, 0.2, 0, 2);");

		// Validazione input
		quantField.textProperty().addListener((obs, old, val) -> {
			if (!val.matches("^\\d*\\.?\\d*$"))
				quantField.setText(old);
		});

		// ✅ LABEL UNITÀ
		Label unitLabel = new Label("grammi (g)");
		unitLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
		unitLabel.setTextFill(Color.web(StyleHelper.TEXT_GRAY));
		unitLabel.setAlignment(Pos.CENTER);

		VBox fieldBox = new VBox(12, quantField, unitLabel);
		fieldBox.setAlignment(Pos.CENTER);

		// Aggiungi tutto alla card
		card.getChildren().addAll(headerBox, sep, istruzioni, fieldBox);

		// ✅ PULSANTI sotto la card (non dentro)
		HBox buttons = new HBox(20);
		buttons.setAlignment(Pos.CENTER);
		buttons.setPadding(new Insets(20, 0, 0, 0));

		Button annullaBtn = StyleHelper.createSecondaryButton("❌ Annulla");
		annullaBtn.setPrefSize(170, 55);
		annullaBtn.setStyle(annullaBtn.getStyle() + "-fx-font-size: 16px;");
		annullaBtn.setOnAction(e -> mainContainer.getChildren().setAll(modificaView));

		Button confermaBtn = StyleHelper.createSuccessButton("✅ Conferma");
		confermaBtn.setPrefSize(170, 55);
		confermaBtn.setStyle(confermaBtn.getStyle() + "-fx-font-size: 16px;");
		confermaBtn.setOnAction(e -> {
			String text = quantField.getText().trim();
			if (text.isEmpty()) {
				StyleHelper.showValidationDialog("Errore", "Inserisci una quantità");
				quantField.requestFocus();
				return;
			}
			try {
				double q = Double.parseDouble(text);
				if (q > 0) {
					onSuccess.accept(q);
				} else {
					StyleHelper.showValidationDialog("Errore", "La quantità deve essere maggiore di zero");
					quantField.requestFocus();
				}
			} catch (NumberFormatException ex) {
				StyleHelper.showValidationDialog("Errore", "Inserisci un numero valido");
				quantField.requestFocus();
			}
		});

		buttons.getChildren().addAll(annullaBtn, confermaBtn);

		// ✅ LAYOUT FINALE
		dialogView.getChildren().addAll(title, card, buttons);

		mainContainer.getChildren().setAll(dialogView);

		// Focus automatico
		javafx.application.Platform.runLater(() -> quantField.requestFocus());
	}

	// ==================== NAVIGAZIONE ====================

	private void mostraLista() {
		carica();
		mainContainer.getChildren().setAll(listaView);
	}

	private void mostraModifica(Ricetta ricetta) {
		try {
			// 🔄 IMPORTANTE: Ricarica la ricetta fresca dal controller
			Ricetta ricettaAggiornata = ricettaController.getRicettaPerId(ricetta.getIdRicetta());

			if (ricettaAggiornata == null) {
				StyleHelper.showErrorDialog("Errore", "Ricetta non trovata");
				mostraLista();
				return;
			}

			// Crea una NUOVA mappa con gli ingredienti "freschi" dal DB
			modificaIngredientiMap = new HashMap<>();

			// Normalizza gli ingredienti usando l'ID come chiave univoca
			for (Map.Entry<Ingrediente, Double> entry : ricettaAggiornata.getIngredienti().entrySet()) {
				Ingrediente ing = entry.getKey();
				// Verifica che l'ingrediente abbia un ID valido
				if (ing.getIdIngrediente() > 0) {
					modificaIngredientiMap.put(ing, entry.getValue());
				}
			}

			modificaView = buildModificaLayout(ricettaAggiornata);
			mainContainer.getChildren().setAll(modificaView);

		} catch (Exception e) {
			StyleHelper.showErrorDialog("Errore", "Errore caricamento ricetta: " + e.getMessage());
			mostraLista();
		}
	}

	private void salvaModifica(Ricetta ricetta) {
		String nome = nomeFieldModifica.getText();
		String tempoStr = tempoFieldModifica.getText();

		if (nome == null || nome.trim().isEmpty()) {
			StyleHelper.showValidationDialog("Errore", "Inserisci il nome");
			nomeFieldModifica.requestFocus();
			return;
		}

		int tempo;
		try {
			tempo = Integer.parseInt(tempoStr.trim());
			if (tempo <= 0) {
				StyleHelper.showValidationDialog("Errore", "Il tempo deve essere maggiore di zero");
				tempoFieldModifica.requestFocus();
				return;
			}
		} catch (NumberFormatException e) {
			StyleHelper.showValidationDialog("Errore", "Tempo non valido");
			tempoFieldModifica.requestFocus();
			return;
		}

		if (modificaIngredientiMap == null || modificaIngredientiMap.isEmpty()) {
			StyleHelper.showValidationDialog("Errore", "Aggiungi almeno un ingrediente");
			return;
		}

		try {
			// Aggiorna nome e tempo della ricetta tramite controller
			ricettaController.aggiornaRicetta(ricetta.getIdRicetta(), nome.trim(), tempo, modificaIngredientiMap);

			StyleHelper.showSuccessDialog("✅ Successo", "Ricetta aggiornata!");
			mostraLista();

		} catch (Exception e) {
			// Tutti i controlli e validazioni sono gestiti dal server/controller
			StyleHelper.showErrorDialog("Errore", e.getMessage());
		}
	}

	// ==================== LOGICA ====================

	private void setupListeners() {
		filtroNome.textProperty().addListener((obs, old, val) -> filtra());
		filtroTempoMin.textProperty().addListener((obs, old, val) -> filtra());
		filtroTempoMax.textProperty().addListener((obs, old, val) -> filtra());
		filtroIngMin.textProperty().addListener((obs, old, val) -> filtra());
		filtroIngMax.textProperty().addListener((obs, old, val) -> filtra());
	}

	private void filtra() {
		try {
			List<Ricetta> filtrate = ricettaController.filtraCombinato(filtroNome.getText(),
					parseIntSafe(filtroTempoMin.getText()), parseIntSafe(filtroTempoMax.getText()),
					parseIntSafe(filtroIngMin.getText()), parseIntSafe(filtroIngMax.getText()));
			ricetteData.setAll(filtrate);
		} catch (Exception e) {
			StyleHelper.showErrorDialog("Errore", e.getMessage());
		}
	}

	private void carica() {
		try {
			ricetteData.setAll(ricettaController.getAllRicette());
		} catch (Exception e) {
			StyleHelper.showErrorDialog("Errore", e.getMessage());
		}
	}

	private void reset() {
		filtroNome.clear();
		filtroTempoMin.clear();
		filtroTempoMax.clear();
		filtroIngMin.clear();
		filtroIngMax.clear();
		carica();
	}

	private void ricarica() {
		try {
			ricettaController.ricaricaCache();
			carica();
			StyleHelper.showSuccessDialog("✅ Successo", "Ricette ricaricate");
		} catch (Exception e) {
			StyleHelper.showErrorDialog("Errore", e.getMessage());
		}
	}

	// ==================== HELPER ====================

	private Label createTitle(String text) {
		Label lbl = new Label(text);
		lbl.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
		lbl.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));
		return lbl;
	}

	private HBox createRange(TextField min, TextField max, String unit) {
		HBox box = new HBox(5, min, new Label("-"), max);
		if (!unit.isEmpty())
			box.getChildren().add(new Label(unit));
		box.setAlignment(Pos.CENTER_LEFT);
		return box;
	}

	private Integer parseIntSafe(String txt) {
		if (txt == null || txt.trim().isEmpty())
			return null;
		try {
			return Integer.parseInt(txt.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	// ==================== CELL RENDERER ====================

	private static class RicettaCell extends ListCell<Ricetta> {
		@Override
		protected void updateItem(Ricetta item, boolean empty) {
			super.updateItem(item, empty);
			if (empty || item == null) {
				setGraphic(null);
				setText(null);
			} else {
				setGraphic(buildCell(item));
			}
		}

		private HBox buildCell(Ricetta r) {
			Label nome = new Label("📖 " + r.getNome());
			nome.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
			nome.setTextFill(Color.BLACK);

			Label tempo = new Label("⏱️ " + r.getTempoPreparazione() + " min");
			tempo.setFont(Font.font("Roboto", 12));
			tempo.setTextFill(Color.web(StyleHelper.INFO_BLUE));

			Label ing = new Label("🥕 " + r.getNumeroIngredienti() + " ingredienti");
			ing.setFont(Font.font("Roboto", 12));
			ing.setTextFill(Color.web(StyleHelper.SUCCESS_GREEN));

			HBox details = new HBox(20, tempo, ing);
			details.setAlignment(Pos.CENTER_LEFT);

			VBox info = new VBox(5, nome, details);
			HBox cell = new HBox(info);
			cell.setPadding(new Insets(12));
			cell.setAlignment(Pos.CENTER_LEFT);
			cell.setStyle("-fx-background-color: #f8f9fa;" + "-fx-background-radius: 8;" + "-fx-border-color: "
					+ StyleHelper.BORDER_LIGHT + ";" + "-fx-border-radius: 8;" + "-fx-border-width: 1;");
			return cell;
		}
	}
}