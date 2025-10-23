package Gui;

import controller.RicettaController;
import controller.IngredienteController;
import exceptions.DataAccessException;
import exceptions.ValidationException;
import helper.StyleHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Ricetta;
import model.Ingrediente;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.function.Consumer;

public class VisualizzaRicetteGUI extends Stage {
	private final RicettaController ricettaController;
	private final IngredienteController ingredienteController;
	private final ObservableList<Ricetta> ricetteData;
	private final ObservableList<Ricetta> ricetteSelezionate;
	
	private ListView<Ricetta> ricetteListView;
	private TextField filtroNome, filtroTempoMin, filtroTempoMax;
	private TextField filtroIngMin, filtroIngMax;
	private Label countLabel;

	private VBox mainContainer;
	private VBox listaView;
	private VBox modificaView;
	private VBox selezioneView;

	private TextField nomeFieldModifica;
	private TextField tempoFieldModifica;
	private VBox listaIngredientiModifica;
	private Map<Ingrediente, Double> modificaIngredientiMap;
	private javafx.animation.Timeline debounceTimer;
	
	private boolean modalitaSelezione = false;
	private List<Ricetta> risultatoSelezione = new ArrayList<>();

	public VisualizzaRicetteGUI(RicettaController ricettaController, IngredienteController ingredienteController) {
		this(ricettaController, ingredienteController, false);
	}

	public VisualizzaRicetteGUI(RicettaController ricettaController, IngredienteController ingredienteController, boolean modalitaSelezione) {
		if (ricettaController == null || ingredienteController == null) {
			throw new IllegalArgumentException("I controller non possono essere null");
		}
		this.ricettaController = ricettaController;
		this.ingredienteController = ingredienteController;
		this.ricetteData = FXCollections.observableArrayList();
		this.ricetteSelezionate = FXCollections.observableArrayList();
		this.modalitaSelezione = modalitaSelezione;
		
		if (modalitaSelezione) {
			initStyle(StageStyle.UNDECORATED);
			initModality(Modality.APPLICATION_MODAL);
			setResizable(false);
			createSelectionDialog();
		}
		
		carica();
	}

	private void createSelectionDialog() {
		StackPane root = new StackPane();
		root.setMinSize(1100, 850);

		Region bg = new Region();
		StyleHelper.applyBackgroundGradient(bg);

		mainContainer = new VBox(20);
		mainContainer.setAlignment(Pos.TOP_CENTER);
		mainContainer.setPadding(new Insets(30));

		selezioneView = buildSelezioneView();
		mainContainer.getChildren().add(selezioneView);

		HBox winBtns = buildWindowButtons();

		root.getChildren().addAll(bg, mainContainer, winBtns);
		StackPane.setAlignment(winBtns, Pos.TOP_RIGHT);
		StackPane.setMargin(winBtns, new Insets(10));

		makeDraggable(root);

		Scene scene = new Scene(root, 1100, 850);
		scene.setFill(Color.TRANSPARENT);
		setScene(scene);
	}

	private VBox buildSelezioneView() {
		VBox container = new VBox(20);
		container.setAlignment(Pos.TOP_CENTER);

		Label title = new Label("📚 Seleziona Ricette per Sessione");
		title.setFont(Font.font("Roboto", FontWeight.BOLD, 28));
		title.setTextFill(Color.WHITE);
		title.setAlignment(Pos.CENTER);

		Label subtitle = new Label("💡 Fai doppio click su una ricetta per aggiungerla alla selezione");
		subtitle.setFont(Font.font("Roboto", FontWeight.SEMI_BOLD, 15));
		subtitle.setTextFill(Color.web("#FFFFFF", 0.9));
		subtitle.setAlignment(Pos.CENTER);

		VBox titleBox = new VBox(10, title, subtitle);
		titleBox.setAlignment(Pos.CENTER);

		VBox contentCard = new VBox(20);
		contentCard.setPadding(new Insets(30));
		contentCard.setStyle("-fx-background-color: white;" + "-fx-background-radius: 20;" + "-fx-border-color: "
				+ StyleHelper.PRIMARY_ORANGE + ";" + "-fx-border-width: 3;" + "-fx-border-radius: 20;"
				+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 20, 0, 0, 8);");

		VBox guiContent = buildListaLayout();
		guiContent.setStyle("-fx-background-color: transparent;");

		if (ricetteListView != null) {
			ricetteListView.setOnMouseClicked(e -> {
				if (e.getClickCount() == 2) {
					Ricetta selected = ricetteListView.getSelectionModel().getSelectedItem();
					if (selected != null) {
						aggiungiRicetta(selected);
					}
				}
			});
		}

		VBox.setVgrow(guiContent, Priority.ALWAYS);

		VBox riepilogoSection = buildRiepilogoSection();
		HBox footer = createFooter();

		contentCard.getChildren().addAll(guiContent, new Separator(), riepilogoSection, footer);
		container.getChildren().addAll(titleBox, contentCard);
		VBox.setVgrow(container, Priority.ALWAYS);

		return container;
	}

	private VBox buildRiepilogoSection() {
		VBox section = new VBox(15);
		section.setPadding(new Insets(20));
		section.setStyle("-fx-background-color: #e8f5e9;" + "-fx-background-radius: 12;" + "-fx-border-color: "
				+ StyleHelper.SUCCESS_GREEN + ";" + "-fx-border-width: 2;" + "-fx-border-radius: 12;");

		HBox headerBox = new HBox(15);
		headerBox.setAlignment(Pos.CENTER_LEFT);

		Label titleLabel = new Label("✅ Ricette Selezionate");
		titleLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
		titleLabel.setTextFill(Color.web(StyleHelper.SUCCESS_GREEN));

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		countLabel = new Label("📊 0 ricette selezionate");
		countLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
		countLabel.setTextFill(Color.web(StyleHelper.ERROR_RED));

		headerBox.getChildren().addAll(titleLabel, spacer, countLabel);

		ListView<Ricetta> listaSelezionate = new ListView<>(ricetteSelezionate);
		listaSelezionate.setPrefHeight(250);
		listaSelezionate.setMaxHeight(250);
		listaSelezionate.setStyle("-fx-background-color: white;" + "-fx-background-radius: 8;" + "-fx-border-color: "
				+ StyleHelper.SUCCESS_GREEN + ";" + "-fx-border-width: 1;" + "-fx-border-radius: 8;"
				+ "-fx-padding: 10;");

		listaSelezionate.setCellFactory(lv -> new ListCell<Ricetta>() {
			@Override
			protected void updateItem(Ricetta ricetta, boolean empty) {
				super.updateItem(ricetta, empty);

				if (empty || ricetta == null) {
					setText(null);
					setGraphic(null);
					setStyle("");
				} else {
					HBox cellContent = new HBox(15);
					cellContent.setAlignment(Pos.CENTER_LEFT);
					cellContent.setPadding(new Insets(15, 20, 15, 20));
					cellContent.setMinHeight(70);
					cellContent.setStyle("-fx-background-color: white;" + "-fx-background-radius: 10;"
							+ "-fx-border-color: " + StyleHelper.SUCCESS_GREEN + ";" + "-fx-border-width: 2;"
							+ "-fx-border-radius: 10;");

					Label iconLabel = new Label("🍽️");
					iconLabel.setFont(Font.font(24));
					iconLabel.setMinWidth(35);

					VBox infoBox = new VBox(6);
					infoBox.setMinWidth(350);

					Label nameLabel = new Label(ricetta.getNome());
					nameLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
					nameLabel.setTextFill(Color.BLACK);

					Label timeLabel = new Label("⏱️ " + ricetta.getTempoPreparazione() + " min  •  🥕 "
							+ ricetta.getNumeroIngredienti() + " ingredienti");
					timeLabel.setFont(Font.font("Roboto", FontWeight.NORMAL, 13));
					timeLabel.setTextFill(Color.web(StyleHelper.TEXT_GRAY));

					infoBox.getChildren().addAll(nameLabel, timeLabel);

					Region spacer2 = new Region();
					HBox.setHgrow(spacer2, Priority.ALWAYS);

					Button removeBtn = StyleHelper.createDangerButton("✕");
					removeBtn.setPrefSize(45, 45);
					removeBtn.setStyle("-fx-background-color: " + StyleHelper.ERROR_RED + ";" + "-fx-text-fill: white;"
							+ "-fx-font-size: 18px;" + "-fx-font-weight: bold;" + "-fx-background-radius: 10;"
							+ "-fx-cursor: hand;");
					removeBtn.setOnAction(e -> {
						ricetteSelezionate.remove(ricetta);
						aggiornaRiepilogo();
					});

					cellContent.getChildren().addAll(iconLabel, infoBox, spacer2, removeBtn);
					setGraphic(cellContent);
					setText(null);
					setStyle("-fx-background-color: transparent; -fx-padding: 8;");
				}
			}
		});

		HBox actionBox = new HBox(15);
		actionBox.setAlignment(Pos.CENTER);
		actionBox.setPadding(new Insets(10, 0, 0, 0));

		Button creaBtn = StyleHelper.createSuccessButton("➕ Crea Nuova Ricetta");
		creaBtn.setPrefSize(200, 45);
		creaBtn.setOnAction(e -> mostraCreaRicettaInterna());

		Button rimuoviTutteBtn = StyleHelper.createSecondaryButton("🗑️ Rimuovi Tutte");
		rimuoviTutteBtn.setPrefSize(150, 45);
		rimuoviTutteBtn.setOnAction(e -> {
			if (!ricetteSelezionate.isEmpty()) {
				ricetteSelezionate.clear();
				aggiornaRiepilogo();
			}
		});

		actionBox.getChildren().addAll(creaBtn, rimuoviTutteBtn);

		section.getChildren().addAll(headerBox, listaSelezionate, actionBox);
		return section;
	}

	private void mostraCreaRicettaInterna() {
		VBox creaView = new VBox(20);
		creaView.setAlignment(Pos.TOP_CENTER);

		Label title = new Label("➕ Crea Nuova Ricetta");
		title.setFont(Font.font("Roboto", FontWeight.BOLD, 28));
		title.setTextFill(Color.WHITE);
		title.setAlignment(Pos.CENTER);

		VBox contentCard = new VBox(20);
		contentCard.setPadding(new Insets(30));
		contentCard.setStyle("-fx-background-color: white;" + "-fx-background-radius: 20;" + "-fx-border-color: "
				+ StyleHelper.PRIMARY_ORANGE + ";" + "-fx-border-width: 3;" + "-fx-border-radius: 20;"
				+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 20, 0, 0, 8);");

		CreaRicettaGUI creaGUI = new CreaRicettaGUI(ricettaController, ingredienteController);

		creaGUI.setOnRicettaCreata(nuovaRicetta -> {
			if (nuovaRicetta != null && !ricetteSelezionate.contains(nuovaRicetta)) {
				ricetteSelezionate.add(nuovaRicetta);
			}

			carica();
			selezioneView = buildSelezioneView();
			mainContainer.getChildren().setAll(selezioneView);
			aggiornaRiepilogo();

			if (nuovaRicetta != null) {
				StyleHelper.showSuccessDialog("Successo",
						String.format("✅ Ricetta '%s' creata e aggiunta!", nuovaRicetta.getNome()));
			}
		});

		creaGUI.setOnAnnulla(() -> {
			mainContainer.getChildren().setAll(selezioneView);
		});

		VBox content = creaGUI.getContent();
		VBox.setVgrow(content, Priority.ALWAYS);

		HBox buttons = new HBox(15);
		buttons.setAlignment(Pos.CENTER);
		buttons.setPadding(new Insets(15, 0, 5, 0));

		Button indietroBtn = StyleHelper.createSecondaryButton("← Indietro");
		indietroBtn.setPrefSize(150, 45);
		indietroBtn.setOnAction(e -> mainContainer.getChildren().setAll(selezioneView));

		buttons.getChildren().add(indietroBtn);

		contentCard.getChildren().addAll(content, new Separator(), buttons);
		creaView.getChildren().addAll(title, contentCard);

		mainContainer.getChildren().setAll(creaView);
	}

	private void aggiungiRicetta(Ricetta ricetta) {
		if (!ricetteSelezionate.contains(ricetta)) {
			ricetteSelezionate.add(ricetta);
			aggiornaRiepilogo();
		} else {
			StyleHelper.showValidationDialog("Attenzione",
					"La ricetta '" + ricetta.getNome() + "' è già stata selezionata");
		}
	}

	private void aggiornaRiepilogo() {
		if (countLabel != null) {
			int count = ricetteSelezionate.size();
			countLabel.setText(String.format("📊 %d ricette selezionate", count));

			if (count == 0) {
				countLabel.setTextFill(Color.web(StyleHelper.ERROR_RED));
			} else {
				countLabel.setTextFill(Color.web(StyleHelper.SUCCESS_GREEN));
			}
		}
	}

	private HBox createFooter() {
		HBox footer = new HBox(20);
		footer.setAlignment(Pos.CENTER);
		footer.setPadding(new Insets(20, 0, 10, 0));

		Button annullaBtn = StyleHelper.createSecondaryButton("❌ Annulla");
		annullaBtn.setPrefSize(170, 50);
		annullaBtn.setOnAction(e -> {
			risultatoSelezione.clear();
			close();
		});

		Button confermaBtn = StyleHelper.createSuccessButton("✅ Conferma Selezione");
		confermaBtn.setPrefSize(210, 50);
		confermaBtn.setOnAction(e -> {
			if (ricetteSelezionate.isEmpty()) {
				StyleHelper.showValidationDialog("Attenzione",
						"Devi selezionare almeno una ricetta per le sessioni in presenza");
				return;
			}
			risultatoSelezione.clear();
			risultatoSelezione.addAll(ricetteSelezionate);
			close();
		});

		footer.getChildren().addAll(annullaBtn, confermaBtn);
		return footer;
	}

	private HBox buildWindowButtons() {
		Button close = StyleHelper.createWindowButtonByType("close", this::close);
		Button minimize = StyleHelper.createWindowButtonByType("minimize", () -> setIconified(true));
		Button maximize = StyleHelper.createWindowButtonByType("maximize", this::toggleMaximize);

		HBox box = new HBox(3, minimize, maximize, close);
		box.setAlignment(Pos.TOP_RIGHT);
		box.setPickOnBounds(false);
		return box;
	}

	private void toggleMaximize() {
		setMaximized(!isMaximized());
	}

	private void makeDraggable(StackPane root) {
		final double[] xOffset = { 0 };
		final double[] yOffset = { 0 };

		root.setOnMousePressed(e -> {
			xOffset[0] = e.getSceneX();
			yOffset[0] = e.getSceneY();
		});

		root.setOnMouseDragged(e -> {
			setX(e.getScreenX() - xOffset[0]);
			setY(e.getScreenY() - yOffset[0]);
		});
	}

	public VBox getRoot() {
		if (mainContainer == null) {
			mainContainer = new VBox();
			StyleHelper.applyBackgroundGradient(mainContainer);
			listaView = buildListaLayout();
			mainContainer.getChildren().add(listaView);
			VBox.setVgrow(listaView, Priority.ALWAYS);
		}
		return mainContainer;
	}

	private VBox buildListaLayout() {
		VBox container = new VBox(15);
		container.setPadding(new Insets(20));

		if (!modalitaSelezione) {
			Label title = StyleHelper.createTitleLabel("📖 Visualizza Ricette");
			title.setAlignment(Pos.CENTER);
			title.setTextFill(Color.WHITE);
			container.getChildren().add(title);
		}

		VBox scrollContent = new VBox(15, buildFiltri(), new Separator(), buildLista());

		ScrollPane scroll = new ScrollPane(scrollContent);
		scroll.setFitToWidth(true);
		scroll.setFitToHeight(false);
		scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scroll.setPannable(true);
		scroll.setStyle("-fx-background-color: transparent;");
		scroll.getContent().setStyle("-fx-background-color: transparent;");
		scroll.skinProperty().addListener((obs, old, skin) -> {
			Region corner = (Region) scroll.lookup(".corner");
			if (corner != null)
				corner.setStyle("-fx-background-color: transparent;");
		});
		VBox.setVgrow(scroll, Priority.ALWAYS);

		container.getChildren().add(scroll);

		if (!modalitaSelezione) {
			Separator bottomSep = new Separator();
			HBox buttons = buildButtons();
			container.getChildren().addAll(bottomSep, buttons);
		}

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
		Label info = new Label(modalitaSelezione ? "💡 Doppio click per aggiungere alla selezione" : "💡 Doppio click per modificare una ricetta");
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
		
		if (!modalitaSelezione) {
			ricetteListView.setOnMouseClicked(e -> {
				if (e.getClickCount() == 2) {
					Ricetta sel = ricetteListView.getSelectionModel().getSelectedItem();
					if (sel != null) {
						mostraModifica(sel);
					}
				}
			});
		}

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

	private VBox buildModificaLayout(Ricetta ricetta) {
		VBox container = new VBox(15);
		container.setPadding(new Insets(20));

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
		scroll.setFitToHeight(false);
		scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scroll.setPannable(true);
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
		Label title = createTitle("🥕 Visualizza Ingredienti");

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
		eliminaBtn.setOnAction(e -> {
			StyleHelper.showCustomConfirmationDialog("Conferma Eliminazione",
					String.format("Sei sicuro di voler eliminare '%s'?", ricetta.getNome()), () -> {
						try {
							ricettaController.eliminaRicetta(ricetta.getIdRicetta());
							carica();
							StyleHelper.showSuccessDialog("✅ Successo", "Ricetta eliminata");
							mostraLista();
						} catch (ValidationException | DataAccessException ex) {
							StyleHelper.showErrorDialog("❌ Errore", ex.getMessage());
							ex.printStackTrace();
						}
					});
		});

		Button salvaBtn = StyleHelper.createSuccessButton("💾 Salva");
		salvaBtn.setPrefWidth(140);
		salvaBtn.setOnAction(e -> salvaModifica(ricetta));

		HBox box = new HBox(15, indietroBtn, eliminaBtn, salvaBtn);
		box.setAlignment(Pos.CENTER);
		box.setPadding(new Insets(15, 0, 5, 0));
		return box;
	}

	private void mostraSelezinaIngredientePerModifica(Ricetta ricetta) {
		VBox selezionaView = new VBox(20);
		selezionaView.setPadding(new Insets(20));

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

	private void mostraDialogQuantita(Ingrediente ing, Consumer<Double> onSuccess) {
		Stage dialogStage = new Stage();
		dialogStage.initModality(Modality.APPLICATION_MODAL);
		dialogStage.initStyle(StageStyle.TRANSPARENT);
		dialogStage.setResizable(false);

		VBox content = new VBox(20);
		content.setMaxWidth(600);
		content.setMinHeight(350);
		content.setPadding(new Insets(40));
		content.setAlignment(Pos.CENTER);
		content.setStyle("-fx-background-color: white;" + "-fx-background-radius: 16;" + "-fx-border-color: "
				+ StyleHelper.PRIMARY_ORANGE + ";" + "-fx-border-radius: 16;" + "-fx-border-width: 3;"
				+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.20), 15, 0, 0, 6);");

		Label titleLabel = new Label("🥕 Quantità Ingrediente");
		titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
		titleLabel.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));
		titleLabel.setAlignment(Pos.CENTER);
		titleLabel.setMaxWidth(Double.MAX_VALUE);

		Separator sepTop = new Separator();

		VBox infoBox = new VBox(8);
		infoBox.setAlignment(Pos.CENTER);

		Label iconLabel = new Label("🥕");
		iconLabel.setStyle("-fx-font-size: 52px;");

		Label nomeLabel = new Label(ing.getNome());
		nomeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
		nomeLabel.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

		Label tipoLabel = new Label("📂 " + ing.getTipo());
		tipoLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
		tipoLabel.setTextFill(Color.WHITE);
		tipoLabel.setStyle("-fx-background-color: " + StyleHelper.INFO_BLUE
				+ "; -fx-background-radius: 16; -fx-padding: 4 12 4 12;");

		infoBox.getChildren().addAll(iconLabel, nomeLabel, tipoLabel);

		Label istruzioni = new Label("Inserisci la quantità in grammi:");
		istruzioni.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
		istruzioni.setTextFill(Color.web("#444"));

		TextField quantField = new TextField();
		quantField.setPromptText("Es. 250");
		quantField.setMaxWidth(220);
		quantField.setPrefHeight(50);
		quantField.setAlignment(Pos.CENTER);
		quantField.setStyle("-fx-background-color: white;" + "-fx-border-color: " + StyleHelper.PRIMARY_ORANGE + ";"
				+ "-fx-border-width: 2;" + "-fx-border-radius: 10;" + "-fx-background-radius: 10;"
				+ "-fx-font-size: 20px;" + "-fx-font-weight: bold;" + "-fx-padding: 10;");

		quantField.textProperty().addListener((obs, old, val) -> {
			if (!val.matches("^\\d*\\.?\\d*$"))
				quantField.setText(old);
		});

		Label unitLabel = new Label("grammi (g)");
		unitLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
		unitLabel.setTextFill(Color.web(StyleHelper.TEXT_GRAY));

		VBox fieldContainer = new VBox(8, quantField, unitLabel);
		fieldContainer.setAlignment(Pos.CENTER);

		HBox buttons = new HBox(15);
		buttons.setAlignment(Pos.CENTER);

		Button annullaBtn = StyleHelper.createSecondaryButton("← Annulla");
		annullaBtn.setPrefWidth(150);
		annullaBtn.setOnAction(e -> dialogStage.close());

		Button confermaBtn = StyleHelper.createSuccessButton("✓ Conferma");
		confermaBtn.setPrefWidth(150);
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
					dialogStage.close();
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

		content.getChildren().addAll(titleLabel, sepTop, infoBox, istruzioni, fieldContainer, buttons);

		StackPane root = new StackPane(content);
		root.setStyle("-fx-background-color: transparent;");

		Scene scene = new Scene(root);
		scene.setFill(Color.TRANSPARENT);
		dialogStage.setScene(scene);

		javafx.application.Platform.runLater(quantField::requestFocus);
		dialogStage.showAndWait();
	}

	private void mostraLista() {
		carica();
		mainContainer.getChildren().setAll(listaView);
	}

	private void mostraModifica(Ricetta ricetta) {
		try {
			Ricetta ricettaAggiornata = ricettaController.getRicettaCompleta(ricetta.getIdRicetta());

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
			e.printStackTrace();
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
			ricettaController.modificaRicetta(ricetta.getIdRicetta(), nome.trim(), tempo, modificaIngredientiMap);
			StyleHelper.showSuccessDialog("✅ Successo", "Ricetta aggiornata!");
			mostraLista();
		} catch (ValidationException | DataAccessException e) {
			StyleHelper.showErrorDialog("Errore", e.getMessage());
			e.printStackTrace();
		}
	}

	private void setupListeners() {
		javafx.beans.value.ChangeListener<String> debounceListener = (obs, oldVal, newVal) -> {
			if (debounceTimer != null) {
				debounceTimer.stop();
			}

			debounceTimer = new javafx.animation.Timeline(
					new javafx.animation.KeyFrame(javafx.util.Duration.millis(300), event -> filtra()));
			debounceTimer.setCycleCount(1);
			debounceTimer.play();
		};

		filtroNome.textProperty().addListener(debounceListener);
		filtroTempoMin.textProperty().addListener(debounceListener);
		filtroTempoMax.textProperty().addListener(debounceListener);
		filtroIngMin.textProperty().addListener(debounceListener);
		filtroIngMax.textProperty().addListener(debounceListener);
	}

	private void filtra() {
		try {
			String nomeFilter = filtroNome.getText() != null ? filtroNome.getText().trim() : "";
			Integer tempoMin = parseIntSafe(filtroTempoMin.getText());
			Integer tempoMax = parseIntSafe(filtroTempoMax.getText());
			Integer ingMin = parseIntSafe(filtroIngMin.getText());
			Integer ingMax = parseIntSafe(filtroIngMax.getText());

			List<Ricetta> filtrate = ricettaController.filtraCombinato(nomeFilter.isEmpty() ? null : nomeFilter,
					tempoMin, tempoMax, ingMin, ingMax);

			ricetteData.setAll(filtrate);
		} catch (ValidationException | DataAccessException e) {
			StyleHelper.showErrorDialog("Errore", e.getMessage());
			e.printStackTrace();
		}
	}

	private void carica() {
		try {
			ricetteData.setAll(ricettaController.visualizzaRicette());
		} catch (DataAccessException e) {
			StyleHelper.showErrorDialog("Errore", e.getMessage());
			e.printStackTrace();
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
		carica();
		StyleHelper.showSuccessDialog("✅ Successo", "Ricette ricaricate");
	}

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

	public List<Ricetta> showAndReturn() {
		showAndWait();
		return new ArrayList<>(risultatoSelezione);
	}

	public void preSelezionaRicette(List<Ricetta> ricetteDaPreselezionare) {
		if (ricetteDaPreselezionare != null && !ricetteDaPreselezionare.isEmpty()) {
			ricetteSelezionate.addAll(ricetteDaPreselezionare);
			aggiornaRiepilogo();
		}
	}

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