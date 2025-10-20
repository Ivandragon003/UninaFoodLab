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
			// Gradiente applicato al ROOT, così copre sempre tutta la finestra
			StyleHelper.applyBackgroundGradient(mainContainer);
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
		// Facoltativo: il gradiente è già sul root; mantenere bianco interno per le
		// card
		// StyleHelper.applyBackgroundGradient(container);

		Label title = StyleHelper.createTitleLabel("📖 Gestione Ricette");
		title.setAlignment(Pos.CENTER);
		title.setTextFill(Color.WHITE);

		VBox scrollContent = new VBox(15, buildFiltri(), new Separator(), buildLista());

		ScrollPane scroll = new ScrollPane(scrollContent);
		scroll.setFitToWidth(true);
		scroll.setFitToHeight(true);
		// Trasparenza completa (pane + viewport + corner)
		scroll.setStyle("-fx-background-color: transparent;");
		scroll.getContent().setStyle("-fx-background-color: transparent;");
		// Prova a forzare il corner trasparente via pseudo-lookup (senza CSS esterno)
		scroll.skinProperty().addListener((obs, old, skin) -> {
			Region corner = (Region) scroll.lookup(".corner");
			if (corner != null)
				corner.setStyle("-fx-background-color: transparent;");
		});
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
		// Il gradiente è già sul root; questa vista può restare “card-based”
		// StyleHelper.applyBackgroundGradient(container);

		Label title = StyleHelper.createTitleLabel("📖 Modifica: " + ricetta.getNome());
		title.setTextFill(Color.WHITE);
		title.setAlignment(Pos.CENTER);

		if (modificaIngredientiMap == null) {
			modificaIngredientiMap = new HashMap<>(ricetta.getIngredienti());
		}

		VBox inner = new VBox(15, buildModificaInfoSection(ricetta), new Separator(),
				buildModificaIngredientiSection(ricetta));

		ScrollPane scroll = new ScrollPane(inner);
		scroll.setFitToWidth(true);
		scroll.setFitToHeight(true);
		scroll.setStyle("-fx-background-color: transparent;");
		scroll.getContent().setStyle("-fx-background-color: transparent;");
		scroll.skinProperty().addListener((obs, old, skin) -> {
			Region corner = (Region) scroll.lookup(".corner");
			if (corner != null)
				corner.setStyle("-fx-background-color: transparent;");
		});
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
		quantField.textProperty().addListener((obs, old, val) -> {
			if (!val.matches("^\\d*\\.?\\d*$")) {
				quantField.setText(old);
			} else if (!val.isEmpty()) {
				try {
					double nuovaQ = Double.parseDouble(val);
					if (nuovaQ > 0) {
						modificaIngredientiMap.put(ing, nuovaQ);
						aggiornaListaIngredienti();
					}
				} catch (NumberFormatException ignored) {
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
		// Il gradiente resta sul root; qui puoi tenerlo oppure no
		// StyleHelper.applyBackgroundGradient(selezionaView);

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
			modificaView = buildModificaLayout(ricetta);
			mainContainer.getChildren().setAll(modificaView);
			StyleHelper.showSuccessDialog("✅ Ingrediente Aggiunto",
					String.format("'%s' aggiunto con %.0fg", ing.getNome(), q));
		});
	}

	private void mostraDialogQuantita(Ingrediente ing, java.util.function.Consumer<Double> onSuccess) {
    VBox dialogView = new VBox(24);
    dialogView.setPadding(new Insets(32, 32, 28, 32));
    dialogView.setAlignment(Pos.TOP_CENTER);

    // Titolo ben distanziato
    Label title = new Label("🥕 Quantità Ingrediente");
    title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 34));
    title.setTextFill(Color.WHITE);
    title.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 8, 0.5, 0, 2);");
    title.setAlignment(Pos.CENTER);

    // Card principale
    VBox card = StyleHelper.createSection();
    card.setSpacing(22);
    card.setAlignment(Pos.CENTER);
    card.setPadding(new Insets(32));
    card.setMaxWidth(680);           // limite max più largo ma controllato
    card.setPrefWidth(640);
    card.setStyle(card.getStyle() + "-fx-min-height: 420px;");

    Label iconLabel = new Label("🥕");
    iconLabel.setFont(Font.font("Segoe UI Emoji", 64));

    Label nomeLabel = new Label(ing.getNome());
    nomeLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 26));
    nomeLabel.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));
    nomeLabel.setWrapText(true);
    nomeLabel.setMaxWidth(520);
    nomeLabel.setAlignment(Pos.CENTER);

    Label tipoLabel = new Label("📂 " + ing.getTipo());
    tipoLabel.setFont(Font.font("Roboto", FontWeight.SEMI_BOLD, 14));
    tipoLabel.setTextFill(Color.WHITE);
    tipoLabel.setAlignment(Pos.CENTER);
    tipoLabel.setStyle("-fx-background-color: " + StyleHelper.INFO_BLUE + "; -fx-padding: 6 14; -fx-background-radius: 16;");

    VBox headerBox = new VBox(10, iconLabel, nomeLabel, tipoLabel);
    headerBox.setAlignment(Pos.CENTER);

    Separator sep1 = new Separator();
    sep1.setMaxWidth(420);

    Label istruzioni = new Label("Inserisci la quantità in grammi:");
    istruzioni.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
    istruzioni.setTextFill(Color.web(StyleHelper.TEXT_BLACK));
    istruzioni.setAlignment(Pos.CENTER);

    TextField quantField = new TextField();
    quantField.setPromptText("Es. 250");
    quantField.setPrefWidth(300);
    quantField.setPrefHeight(56);
    quantField.setAlignment(Pos.CENTER);
    quantField.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: " + StyleHelper.PRIMARY_ORANGE + ";" +
            "-fx-border-width: 3;" +
            "-fx-border-radius: 14;" +
            "-fx-background-radius: 14;" +
            "-fx-font-size: 24px;" +
            "-fx-font-weight: bold;" +
            "-fx-prompt-text-fill: #c3c3c3;" +
            "-fx-effect: dropshadow(gaussian, rgba(255,107,53,0.18), 7, 0.3, 0, 2);"
    );
    quantField.textProperty().addListener((obs, old, val) -> {
        if (!val.matches("^\\d*\\.?\\d*$")) quantField.setText(old);
    });

    Label unitLabel = new Label("grammi (g)");
    unitLabel.setFont(Font.font("Roboto", FontWeight.SEMI_BOLD, 13));
    unitLabel.setTextFill(Color.web(StyleHelper.TEXT_GRAY));

    VBox fieldBox = new VBox(8, quantField, unitLabel);
    fieldBox.setAlignment(Pos.CENTER);

    card.getChildren().addAll(headerBox, sep1, istruzioni, fieldBox);

    // Colonna centrata con margini elastici sopra/sotto
    Region topSpacer = new Region();
    Region bottomSpacer = new Region();
    VBox centeredCol = new VBox(topSpacer, card, bottomSpacer);
    centeredCol.setAlignment(Pos.CENTER);
    VBox.setVgrow(topSpacer, Priority.ALWAYS);
    VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

    // “Pista” centrale con padding laterale per non far sembrare tutto schiacciato
    VBox content = new VBox(centeredCol);
    content.setFillWidth(true);
    content.setPadding(new Insets(10, 24, 10, 24));
    content.setMaxWidth(900);        // limita la colonna per monitor larghi
    content.setAlignment(Pos.CENTER);

    ScrollPane scroll = new ScrollPane(content);
    scroll.setFitToWidth(true);
    scroll.setPannable(true);
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    scroll.setStyle("-fx-background-color: transparent;");
    scroll.getContent().setStyle("-fx-background-color: transparent;");
    scroll.skinProperty().addListener((obs, old, skin) -> {
        Region corner = (Region) scroll.lookup(".corner");
        if (corner != null) corner.setStyle("-fx-background-color: transparent;");
    });
    VBox.setVgrow(scroll, Priority.ALWAYS);

    Separator sep2 = new Separator();
    sep2.setMaxWidth(720);

    // Bottoni con area di respiro
    HBox buttons = new HBox(18);
    buttons.setAlignment(Pos.CENTER);
    buttons.setPadding(new Insets(6, 0, 0, 0));

    Button annullaBtn = StyleHelper.createSecondaryButton("❌ Annulla");
    annullaBtn.setPrefSize(160, 48);
    annullaBtn.setOnAction(e -> mainContainer.getChildren().setAll(modificaView));

    Button confermaBtn = StyleHelper.createSuccessButton("✅ Conferma");
    confermaBtn.setPrefSize(160, 48);
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

    // Layout finale
    dialogView.getChildren().setAll(title, scroll, sep2, buttons);
    VBox.setVgrow(scroll, Priority.ALWAYS);

    mainContainer.getChildren().setAll(dialogView);
    javafx.application.Platform.runLater(quantField::requestFocus);
}


	// ==================== NAVIGAZIONE ====================

	private void mostraLista() {
		carica();
		mainContainer.getChildren().setAll(listaView);
	}

	private void mostraModifica(Ricetta ricetta) {
		try {
			Ricetta ricettaAggiornata = ricettaController.getRicettaPerId(ricetta.getIdRicetta());

			if (ricettaAggiornata == null) {
				StyleHelper.showErrorDialog("Errore", "Ricetta non trovata");
				mostraLista();
				return;
			}

			modificaIngredientiMap = new HashMap<>();
			for (Map.Entry<Ingrediente, Double> entry : ricettaAggiornata.getIngredienti().entrySet()) {
				Ingrediente ing = entry.getKey();
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
			ricettaController.aggiornaRicetta(ricetta.getIdRicetta(), nome.trim(), tempo, modificaIngredientiMap);
			StyleHelper.showSuccessDialog("✅ Successo", "Ricetta aggiornata!");
			mostraLista();

		} catch (Exception e) {
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
