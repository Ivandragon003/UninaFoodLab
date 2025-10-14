package Gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import model.*;
import controller.RicettaController;
import controller.IngredienteController;
import guihelper.StyleHelper;

import java.util.List;
import java.util.ArrayList;

public class VisualizzaRicetteDialog extends Stage {

	private final RicettaController ricettaController;
	private final IngredienteController ingredienteController;
	private final ObservableList<Ricetta> ricetteDisponibili;
	private final ObservableList<Ricetta> ricetteSelezionate;
	private FilteredList<Ricetta> filteredRicette;

	private ListView<Ricetta> listaDisponibili;
	private ListView<Ricetta> listaSelezionate;
	private TextField searchField;
	private Label countDisponibiliLabel;
	private Label countSelezionateLabel;

	private List<Ricetta> risultato = new ArrayList<>();

	public VisualizzaRicetteDialog(RicettaController ricettaController, IngredienteController ingredienteController) {
		// ✅ VALIDAZIONE: Verifica che i controller siano validi
		if (ricettaController == null) {
			throw new IllegalArgumentException("RicettaController non può essere null");
		}
		if (ingredienteController == null) {
			throw new IllegalArgumentException("IngredienteController non può essere null");
		}

		this.ricettaController = ricettaController;
		this.ingredienteController = ingredienteController;
		this.ricetteDisponibili = FXCollections.observableArrayList();
		this.ricetteSelezionate = FXCollections.observableArrayList();

		initStyle(StageStyle.UNDECORATED);
		initModality(Modality.APPLICATION_MODAL);
		setResizable(false);

		createLayout();
		caricaRicette();
	}

	private void createLayout() {
		VBox root = createMainContainer();
		Scene scene = new Scene(root, 950, 700);
		setScene(scene);
	}

	private VBox createMainContainer() {
		VBox root = new VBox(0);
		root.setStyle("-fx-background-color: white;" + "-fx-background-radius: 20;" + "-fx-border-color: "
				+ StyleHelper.PRIMARY_ORANGE + ";" + "-fx-border-width: 3;" + "-fx-border-radius: 20;");

		DropShadow shadow = new DropShadow(25, Color.rgb(0, 0, 0, 0.25));
		root.setEffect(shadow);

		VBox header = createHeader();
		VBox content = createContent();
		HBox footer = createFooter();

		VBox.setVgrow(content, Priority.ALWAYS);
		root.getChildren().addAll(header, content, footer);
		return root;
	}

	private VBox createHeader() {
		VBox header = new VBox(12);
		header.setPadding(new Insets(25, 25, 20, 25));
		header.setStyle("-fx-background-color: linear-gradient(to bottom, " + StyleHelper.PRIMARY_ORANGE + ", "
				+ StyleHelper.PRIMARY_LIGHT + ");" + "-fx-background-radius: 20 20 0 0;");

		HBox titleBox = new HBox();
		titleBox.setAlignment(Pos.CENTER_LEFT);

		Label titleLabel = new Label("📚 Seleziona Ricette per Sessione In Presenza");
		titleLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 22));
		titleLabel.setTextFill(Color.WHITE);

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Button closeBtn = new Button("✖");
		closeBtn.setStyle("-fx-background-color: rgba(255,255,255,0.3);" + "-fx-text-fill: white;"
				+ "-fx-font-weight: bold;" + "-fx-background-radius: 18;" + "-fx-cursor: hand;" + "-fx-min-width: 36;"
				+ "-fx-min-height: 36;" + "-fx-font-size: 14px;");
		closeBtn.setOnAction(e -> {
			risultato.clear();
			close();
		});

		titleBox.getChildren().addAll(titleLabel, spacer, closeBtn);

		Label infoLabel = new Label("💡 Le sessioni in presenza richiedono almeno una ricetta associata");
		infoLabel.setFont(Font.font("Roboto", FontWeight.NORMAL, 13));
		infoLabel.setTextFill(Color.web("#FFF8E1"));
		infoLabel.setStyle(
				"-fx-background-color: rgba(255,255,255,0.2);" + "-fx-padding: 10;" + "-fx-background-radius: 10;");

		header.getChildren().addAll(titleBox, infoLabel);
		return header;
	}

	private VBox createContent() {
		VBox content = new VBox(15);
		content.setPadding(new Insets(20, 25, 15, 25));

		HBox mainContainer = createMainContainerLayout();
		content.getChildren().add(mainContainer);

		return content;
	}

	private HBox createMainContainerLayout() {
		HBox container = new HBox(15);
		container.setAlignment(Pos.TOP_CENTER);

		VBox disponibiliSection = createDisponibiliSection();
		VBox buttonSection = createButtonSection();
		VBox selezionateSection = createSelezionateSection();

		HBox.setHgrow(disponibiliSection, Priority.ALWAYS);
		HBox.setHgrow(selezionateSection, Priority.ALWAYS);

		container.getChildren().addAll(disponibiliSection, buttonSection, selezionateSection);
		return container;
	}

	private VBox createDisponibiliSection() {
		VBox section = new VBox(12);
		section.setPrefWidth(350);

		Label titleLabel = new Label("🍽️ Ricette Disponibili");
		titleLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
		titleLabel.setTextFill(Color.web("#2c3e50"));

		searchField = new TextField();
		searchField.setPromptText("🔍 Cerca per nome...");
		searchField.setPrefHeight(38);
		searchField.setStyle("-fx-background-color: #f8f9fa;" + "-fx-background-radius: 19;" + "-fx-border-radius: 19;"
				+ "-fx-border-color: #dee2e6;" + "-fx-border-width: 1.5;" + "-fx-padding: 0 15 0 15;"
				+ "-fx-font-size: 13px;");

		listaDisponibili = new ListView<>();
		listaDisponibili.setPrefHeight(350);
		listaDisponibili.setStyle("-fx-background-color: #f8f9fa;" + "-fx-background-radius: 12;"
				+ "-fx-border-color: #dee2e6;" + "-fx-border-width: 1.5;" + "-fx-border-radius: 12;");
		listaDisponibili.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		filteredRicette = new FilteredList<>(ricetteDisponibili, p -> true);
		listaDisponibili.setItems(filteredRicette);

		setupCellFactory(listaDisponibili, false);
		setupSearchFilter();

		countDisponibiliLabel = new Label("📊 Disponibili: 0 ricette");
		countDisponibiliLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
		countDisponibiliLabel.setTextFill(Color.web("#6c757d"));

		section.getChildren().addAll(titleLabel, searchField, listaDisponibili, countDisponibiliLabel);
		return section;
	}

	private VBox createButtonSection() {
		VBox section = new VBox(12);
		section.setAlignment(Pos.CENTER);
		section.setPrefWidth(120);
		section.setPadding(new Insets(40, 0, 0, 0));

		Button addBtn = createActionButton("➡️", "Aggiungi", "#28a745");
		addBtn.setOnAction(e -> aggiungiRicetteSelezionate());

		Button addAllBtn = createActionButton("⏩", "Aggiungi Tutte", "#17a2b8");
		addAllBtn.setOnAction(e -> aggiungiTutteRicette());

		Button removeBtn = createActionButton("⬅️", "Rimuovi", "#dc3545");
		removeBtn.setOnAction(e -> rimuoviRicetteSelezionate());

		Button removeAllBtn = createActionButton("⏪", "Rimuovi Tutte", "#e67e22");
		removeAllBtn.setOnAction(e -> rimuoviTutteRicette());

		Separator sep = new Separator();
		sep.setPrefWidth(100);
		sep.setStyle("-fx-background-color: #dee2e6;");

		Button createBtn = createActionButton("➕", "Crea Nuova", "#007bff");
		createBtn.setPrefHeight(55);
		createBtn.setOnAction(e -> creaRicettaDialog());

		section.getChildren().addAll(addBtn, addAllBtn, removeBtn, removeAllBtn, sep, createBtn);
		return section;
	}

	private VBox createSelezionateSection() {
		VBox section = new VBox(12);
		section.setPrefWidth(350);

		Label titleLabel = new Label("✅ Ricette Selezionate");
		titleLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
		titleLabel.setTextFill(Color.web("#28a745"));

		listaSelezionate = new ListView<>(ricetteSelezionate);
		listaSelezionate.setPrefHeight(350);
		listaSelezionate.setStyle("-fx-background-color: #d4edda;" + "-fx-background-radius: 12;"
				+ "-fx-border-color: #28a745;" + "-fx-border-width: 2;" + "-fx-border-radius: 12;");
		listaSelezionate.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		setupCellFactory(listaSelezionate, true);

		countSelezionateLabel = new Label("✅ Selezionate: 0 ricette");
		countSelezionateLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
		countSelezionateLabel.setTextFill(Color.web("#28a745"));

		ricetteSelezionate.addListener((javafx.collections.ListChangeListener<Ricetta>) c -> {
			aggiornaContatori();
		});

		section.getChildren().addAll(titleLabel, listaSelezionate, countSelezionateLabel);
		return section;
	}

	private HBox createFooter() {
		HBox footer = new HBox(15);
		footer.setPadding(new Insets(15, 25, 25, 25));
		footer.setAlignment(Pos.CENTER_RIGHT);
		footer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 0 0 20 20;");

		Button annullaBtn = createFooterButton("❌ Annulla", "#6c757d");
		annullaBtn.setOnAction(e -> {
			risultato.clear();
			close();
		});

		Button confermaBtn = createFooterButton("✅ Conferma Selezione", "#28a745");
		confermaBtn.setOnAction(e -> {
			if (ricetteSelezionate.isEmpty()) {
				StyleHelper.showValidationDialog("Attenzione",
						"Devi selezionare almeno una ricetta per le sessioni in presenza");
				return;
			}
			risultato.clear();
			risultato.addAll(ricetteSelezionate);
			close();
		});

		footer.getChildren().addAll(annullaBtn, confermaBtn);
		return footer;
	}

	private Button createActionButton(String icon, String text, String color) {
		Button button = new Button(icon + "\n" + text);
		button.setFont(Font.font("Roboto", FontWeight.BOLD, 11));
		button.setTextFill(Color.WHITE);
		button.setPrefWidth(100);
		button.setPrefHeight(50);
		button.setStyle("-fx-background-color: " + color + ";" + "-fx-background-radius: 12;" + "-fx-cursor: hand;"
				+ "-fx-text-alignment: center;");

		button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: derive(" + color + ", -15%);"
				+ "-fx-background-radius: 12;" + "-fx-cursor: hand;" + "-fx-text-alignment: center;"
				+ "-fx-text-fill: white;" + "-fx-font-weight: bold;"));

		button.setOnMouseExited(e -> button
				.setStyle("-fx-background-color: " + color + ";" + "-fx-background-radius: 12;" + "-fx-cursor: hand;"
						+ "-fx-text-alignment: center;" + "-fx-text-fill: white;" + "-fx-font-weight: bold;"));

		return button;
	}

	private Button createFooterButton(String text, String color) {
		Button button = new Button(text);
		button.setFont(Font.font("Roboto", FontWeight.BOLD, 13));
		button.setTextFill(Color.WHITE);
		button.setPrefWidth(160);
		button.setPrefHeight(40);
		button.setStyle("-fx-background-color: " + color + ";" + "-fx-background-radius: 20;" + "-fx-cursor: hand;");

		button.setOnMouseEntered(
				e -> button.setStyle("-fx-background-color: derive(" + color + ", -15%);" + "-fx-background-radius: 20;"
						+ "-fx-cursor: hand;" + "-fx-text-fill: white;" + "-fx-font-weight: bold;"));

		button.setOnMouseExited(
				e -> button.setStyle("-fx-background-color: " + color + ";" + "-fx-background-radius: 20;"
						+ "-fx-cursor: hand;" + "-fx-text-fill: white;" + "-fx-font-weight: bold;"));

		return button;
	}

	private void setupCellFactory(ListView<Ricetta> listView, boolean isSelected) {
		listView.setCellFactory(lv -> new ListCell<Ricetta>() {
			@Override
			protected void updateItem(Ricetta ricetta, boolean empty) {
				super.updateItem(ricetta, empty);

				if (empty || ricetta == null) {
					setText(null);
					setGraphic(null);
					setStyle("");
				} else {
					HBox cellContent = new HBox(12);
					cellContent.setAlignment(Pos.CENTER_LEFT);
					cellContent.setPadding(new Insets(10));

					if (isSelected) {
						cellContent.setStyle("-fx-background-color: white;" + "-fx-background-radius: 8;"
								+ "-fx-border-color: #28a745;" + "-fx-border-width: 2;" + "-fx-border-radius: 8;");
					} else {
						cellContent.setStyle("-fx-background-color: white;" + "-fx-background-radius: 8;"
								+ "-fx-border-color: #e9ecef;" + "-fx-border-width: 1;" + "-fx-border-radius: 8;");
					}

					Label iconLabel = new Label("🍽️");
					iconLabel.setFont(Font.font(18));

					VBox infoBox = new VBox(4);

					Label nameLabel = new Label(ricetta.getNome());
					nameLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
					nameLabel.setTextFill(Color.web("#2c3e50"));

					Label timeLabel = new Label("⏱️ " + ricetta.getTempoPreparazione() + " minuti");
					timeLabel.setFont(Font.font("Roboto", FontWeight.NORMAL, 12));
					timeLabel.setTextFill(Color.web("#6c757d"));

					infoBox.getChildren().addAll(nameLabel, timeLabel);

					cellContent.getChildren().addAll(iconLabel, infoBox);

					setGraphic(cellContent);
					setText(null);
					setStyle("-fx-background-color: transparent; -fx-padding: 5;");

					setOnMouseEntered(e -> {
						if (!isEmpty()) {
							if (isSelected) {
								cellContent.setStyle("-fx-background-color: #e8f5e9;" + "-fx-background-radius: 8;"
										+ "-fx-border-color: #28a745;" + "-fx-border-width: 2;"
										+ "-fx-border-radius: 8;");
							} else {
								cellContent.setStyle("-fx-background-color: #f1f3f5;" + "-fx-background-radius: 8;"
										+ "-fx-border-color: " + StyleHelper.PRIMARY_ORANGE + ";"
										+ "-fx-border-width: 1.5;" + "-fx-border-radius: 8;");
							}
						}
					});

					setOnMouseExited(e -> {
						if (!isEmpty()) {
							if (isSelected) {
								cellContent.setStyle("-fx-background-color: white;" + "-fx-background-radius: 8;"
										+ "-fx-border-color: #28a745;" + "-fx-border-width: 2;"
										+ "-fx-border-radius: 8;");
							} else {
								cellContent.setStyle("-fx-background-color: white;" + "-fx-background-radius: 8;"
										+ "-fx-border-color: #e9ecef;" + "-fx-border-width: 1;"
										+ "-fx-border-radius: 8;");
							}
						}
					});
				}
			}
		});
	}

	private void setupSearchFilter() {
		searchField.textProperty().addListener((obs, oldValue, newValue) -> {
			String searchText = newValue == null ? "" : newValue.toLowerCase().trim();

			filteredRicette.setPredicate(ricetta -> {
				if (searchText.isEmpty()) {
					return true;
				}

				String nome = ricetta.getNome() != null ? ricetta.getNome().toLowerCase() : "";
				return nome.contains(searchText);
			});

			aggiornaContatori();
		});
	}

	private void aggiungiRicetteSelezionate() {
		List<Ricetta> selected = new ArrayList<>(listaDisponibili.getSelectionModel().getSelectedItems());

		if (selected.isEmpty()) {
			StyleHelper.showValidationDialog("Attenzione", "Seleziona almeno una ricetta da aggiungere");
			return;
		}

		for (Ricetta ricetta : selected) {
			if (!ricetteSelezionate.contains(ricetta)) {
				ricetteSelezionate.add(ricetta);
			}
		}

		listaDisponibili.getSelectionModel().clearSelection();
		aggiornaContatori();
	}

	private void aggiungiTutteRicette() {
		for (Ricetta ricetta : filteredRicette) {
			if (!ricetteSelezionate.contains(ricetta)) {
				ricetteSelezionate.add(ricetta);
			}
		}
		aggiornaContatori();
	}

	private void rimuoviRicetteSelezionate() {
		List<Ricetta> selected = new ArrayList<>(listaSelezionate.getSelectionModel().getSelectedItems());

		if (selected.isEmpty()) {
			StyleHelper.showValidationDialog("Attenzione", "Seleziona almeno una ricetta da rimuovere");
			return;
		}

		ricetteSelezionate.removeAll(selected);
		listaSelezionate.getSelectionModel().clearSelection();
		aggiornaContatori();
	}

	private void rimuoviTutteRicette() {
		if (ricetteSelezionate.isEmpty()) {
			return;
		}

		ricetteSelezionate.clear();
		aggiornaContatori();
	}

	/**
	 * ✅ CORRETTO: Usa il costruttore normale invece del metodo factory
	 */
	private void creaRicettaDialog() {
		try {
			System.out.println("DEBUG: Apertura dialogo crea ricetta...");

			Stage dialogStage = new Stage();
			dialogStage.initModality(Modality.APPLICATION_MODAL);
			dialogStage.initOwner(this.getScene().getWindow());
			dialogStage.setTitle("Crea Nuova Ricetta");
			dialogStage.setResizable(false);

			CreaRicettaGUI creaRicettaGUI = new CreaRicettaGUI(ricettaController, ingredienteController);

			creaRicettaGUI.setOnRicettaCreata(nuovaRicetta -> {
				System.out.println("DEBUG: Ricetta creata: " + nuovaRicetta.getNome());

				caricaRicette();

				if (!ricetteSelezionate.contains(nuovaRicetta)) {
					ricetteSelezionate.add(nuovaRicetta);
				}

				StyleHelper.showSuccessDialog("Successo", String.format(
						"✅ Ricetta '%s' creata e aggiunta alla selezione!\n\n" + "⏱️ Tempo preparazione: %d minuti\n"
								+ "🥕 Ingredienti: %d",
						nuovaRicetta.getNome(), nuovaRicetta.getTempoPreparazione(),
						nuovaRicetta.getNumeroIngredienti()));

				aggiornaContatori();

				dialogStage.close();
			});

			creaRicettaGUI.setOnAnnulla(() -> {
				System.out.println("DEBUG: Creazione ricetta annullata dall'utente");
				dialogStage.close();
			});

			VBox content = creaRicettaGUI.getContent();
			Scene scene = new Scene(content, 650, 650);
			dialogStage.setScene(scene);

			System.out.println("DEBUG: Apertura finestra crea ricetta...");
			dialogStage.showAndWait();

		} catch (IllegalArgumentException e) {

			System.err.println("ERROR: Parametri non validi: " + e.getMessage());
			StyleHelper.showErrorDialog("Errore Parametri", "I controller non sono stati inizializzati correttamente:\n"
					+ e.getMessage() + "\n\nProva a riavviare l'applicazione.");
		} catch (Exception e) {
			System.err.println("ERROR: Errore generico nella creazione ricetta: " + e.getMessage());
			e.printStackTrace();
			StyleHelper.showErrorDialog("Errore", "Errore durante la creazione della ricetta:\n" + e.getMessage()
					+ "\n\nVerifica che il database sia accessibile.");
		}
	}

	/**
	 * ✅ MIGLIORATO: Caricamento ricette con gestione errori
	 */
	private void caricaRicette() {
		try {
			System.out.println("DEBUG: Caricamento ricette...");
			List<Ricetta> ricette = ricettaController.getAllRicette();

			ricetteDisponibili.clear();

			if (ricette != null && !ricette.isEmpty()) {
				ricetteDisponibili.addAll(ricette);
				System.out.println("DEBUG: Caricate " + ricette.size() + " ricette");
			} else {
				System.out.println("DEBUG: Nessuna ricetta trovata nel database");

				// ✅ Mostra messaggio informativo se non ci sono ricette
				javafx.application.Platform.runLater(() -> {
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setTitle("Nessuna Ricetta");
					alert.setHeaderText("Database vuoto");
					alert.setContentText(
							"Non ci sono ricette nel database.\n\nUsa il pulsante '➕ Crea Nuova' per aggiungere la prima ricetta.");
					alert.initOwner(this);
					alert.show();
				});
			}

			aggiornaContatori();

		} catch (Exception e) {
			System.err.println("ERROR: Errore nel caricamento ricette: " + e.getMessage());
			e.printStackTrace();

			StyleHelper.showErrorDialog("Errore Caricamento", "Errore durante il caricamento delle ricette:\n"
					+ e.getMessage() + "\n\nVerifica che il database sia accessibile.");
		}
	}

	private void aggiornaContatori() {
		int disponibili = filteredRicette.size();
		int selezionate = ricetteSelezionate.size();

		countDisponibiliLabel.setText(String.format("📊 Disponibili: %d ricette", disponibili));
		countSelezionateLabel.setText(String.format("✅ Selezionate: %d ricette", selezionate));

		if (selezionate == 0) {
			countSelezionateLabel.setTextFill(Color.web("#e74c3c"));
			countSelezionateLabel.setStyle("-fx-font-weight: bold;");
		} else {
			countSelezionateLabel.setTextFill(Color.web("#28a745"));
			countSelezionateLabel.setStyle("-fx-font-weight: bold;");
		}
	}

	public List<Ricetta> showAndReturn() {
		showAndWait();
		return new ArrayList<>(risultato);
	}

	public void preSelezionaRicette(List<Ricetta> ricetteDaPreselezionare) {
		if (ricetteDaPreselezionare != null && !ricetteDaPreselezionare.isEmpty()) {
			System.out.println("DEBUG: Pre-selezione di " + ricetteDaPreselezionare.size() + " ricette");

			for (Ricetta ricetta : ricetteDaPreselezionare) {
				Ricetta ricettaTrovata = ricetteDisponibili.stream()
						.filter(r -> r.getIdRicetta() == ricetta.getIdRicetta()).findFirst().orElse(null);

				if (ricettaTrovata != null && !ricetteSelezionate.contains(ricettaTrovata)) {
					ricetteSelezionate.add(ricettaTrovata);
					System.out.println("DEBUG: Pre-selezionata ricetta: " + ricettaTrovata.getNome());
				}
			}

			aggiornaContatori();
		}
	}
}
