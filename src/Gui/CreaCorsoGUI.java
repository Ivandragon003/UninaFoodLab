package Gui;

import controller.GestioneCorsoController;
import controller.ChefController;
import controller.RicettaController;
import controller.IngredienteController;
import exceptions.ValidationException;
import guihelper.StyleHelper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;
import model.*;

import java.time.LocalDate;
import java.util.*;

public class CreaCorsoGUI {

	private static final int DEFAULT_START_HOUR = 9;
	private static final int DEFAULT_START_MINUTE = 0;
	private static final int DEFAULT_END_HOUR = 17;
	private static final int DEFAULT_END_MINUTE = 0;

	private final GestioneCorsoController corsoController;
	private final ChefController chefController;
	private final RicettaController ricettaController;
	private final IngredienteController ingredienteController;

	private VBox root;
	private TextField nomeField;
	private TextField prezzoField;
	private TextField argomentoField;
	private TextField postiField;
	private ComboBox<Frequenza> frequenzaBox;
	private DatePicker startDatePicker;
	private DatePicker endDatePicker;
	private TextField numeroSessioniField;
	private ComboBox<Integer> startHour;
	private ComboBox<Integer> startMinute;
	private ComboBox<Integer> endHour;
	private ComboBox<Integer> endMinute;

	private VBox listaChefContainer;
	private VBox listaSessioniContainer;
	private Label numeroSessioniLabel;

	private final ObservableList<Chef> chefSelezionati = FXCollections.observableArrayList();
	private final ObservableList<Sessione> corsoSessioni = FXCollections.observableArrayList();

	private Button aggiungiSessioneBtn;

	public CreaCorsoGUI(GestioneCorsoController corsoController, ChefController chefController,
			RicettaController ricettaController, IngredienteController ingredienteController) {
		this.corsoController = corsoController;
		this.chefController = chefController;
		this.ricettaController = ricettaController;
		this.ingredienteController = ingredienteController;
	}

	public VBox getRoot() {
		if (root == null) {
			root = createMainLayout();
		}
		return root;
	}

	private VBox createMainLayout() {
		VBox container = new VBox(15);
		container.setPadding(new Insets(20));
		StyleHelper.applyBackgroundGradient(container);

		Label titleLabel = StyleHelper.createTitleLabel("Crea Nuovo Corso di Cucina");
		titleLabel.setTextFill(Color.WHITE);
		titleLabel.setAlignment(Pos.CENTER);

		ScrollPane scrollPane = createScrollPane();
		container.getChildren().addAll(titleLabel, scrollPane);
		return container;
	}

	private ScrollPane createScrollPane() {
		VBox content = new VBox(15);
		content.getChildren().addAll(createInfoSection(), new Separator(), createDateTimeSection(), new Separator(),
				createChefSection(), new Separator(), createSessionSection(), new Separator(), createButtonSection());

		ScrollPane scrollPane = new ScrollPane(content);
		scrollPane.setFitToWidth(true);
		scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
		return scrollPane;
	}

	private VBox createInfoSection() {
		VBox section = StyleHelper.createSection();

		nomeField = StyleHelper.createTextField("Es. Corso Base di Pasta Italiana");
		prezzoField = StyleHelper.createTextField("Es. 150.00");
		argomentoField = StyleHelper.createTextField("Es. Pasta fresca e condimenti");
		postiField = StyleHelper.createTextField("Es. 12");

		postiField.textProperty().addListener((obs, oldVal, newVal) -> {
			if (!newVal.matches("\\d*")) {
				postiField.setText(newVal.replaceAll("[^\\d]", ""));
			}
		});

		GridPane grid = createGrid(15, 15);
		grid.add(StyleHelper.createLabel("Nome Corso:"), 0, 0);
		grid.add(nomeField, 1, 0);
		grid.add(StyleHelper.createLabel("Prezzo (EUR):"), 2, 0);
		grid.add(prezzoField, 3, 0);
		grid.add(StyleHelper.createLabel("Argomento:"), 0, 1);
		grid.add(argomentoField, 1, 1);
		grid.add(StyleHelper.createLabel("Numero Posti:"), 2, 1);
		grid.add(postiField, 3, 1);

		section.getChildren().addAll(createSectionTitle("Informazioni Corso"), grid);
		return section;
	}

	private VBox createDateTimeSection() {
		VBox section = StyleHelper.createSection();

		startDatePicker = StyleHelper.createDatePicker();
		endDatePicker = StyleHelper.createDatePicker();
		startDatePicker.setPromptText("Data inizio");
		endDatePicker.setPromptText("Data fine (calcolata automaticamente)");
		endDatePicker.setDisable(true);

		numeroSessioniField = StyleHelper.createTextField("Es. 12");
		numeroSessioniField.textProperty().addListener((obs, oldVal, newVal) -> {
			if (!newVal.matches("\\d*")) {
				numeroSessioniField.setText(newVal.replaceAll("[^\\d]", ""));
			}
			calcolaDataFine();
		});

		frequenzaBox = createFrequenzaComboBox();
		frequenzaBox.getItems().setAll(Frequenza.values());
		frequenzaBox.setOnAction(e -> calcolaDataFine());

		startDatePicker.setOnAction(e -> calcolaDataFine());

		startHour = createTimeComboBox(24, DEFAULT_START_HOUR);
		startMinute = createTimeComboBox(60, DEFAULT_START_MINUTE, 15);
		endHour = createTimeComboBox(24, DEFAULT_END_HOUR);
		endMinute = createTimeComboBox(60, DEFAULT_END_MINUTE, 15);

		GridPane grid = createGrid(15, 15);
		grid.add(StyleHelper.createLabel("Data Inizio:"), 0, 0);
		grid.add(startDatePicker, 1, 0);
		grid.add(StyleHelper.createLabel("Ora Inizio:"), 2, 0);
		grid.add(createTimeBox(startHour, startMinute), 3, 0);
		grid.add(StyleHelper.createLabel("Data Fine:"), 0, 1);
		grid.add(endDatePicker, 1, 1);
		grid.add(StyleHelper.createLabel("Ora Fine:"), 2, 1);
		grid.add(createTimeBox(endHour, endMinute), 3, 1);
		grid.add(StyleHelper.createLabel("Frequenza:"), 0, 2);
		grid.add(frequenzaBox, 1, 2);
		grid.add(StyleHelper.createLabel("Numero Sessioni:"), 2, 2);
		grid.add(numeroSessioniField, 3, 2);

		numeroSessioniLabel = createInfoLabel("Seleziona data inizio, frequenza e numero sessioni", "#e74c3c");

		section.getChildren().addAll(createSectionTitle("Date e Orari - OBBLIGATORIO"), grid, numeroSessioniLabel);
		return section;
	}

	private void calcolaDataFine() {
		try {
			LocalDate inizio = startDatePicker.getValue();
			Frequenza freq = frequenzaBox.getValue();
			String numSessioniStr = numeroSessioniField.getText().trim();

			if (inizio == null || freq == null || numSessioniStr.isEmpty()) {
				endDatePicker.setValue(null);
				numeroSessioniLabel.setText("Seleziona data inizio, frequenza e numero sessioni");
				numeroSessioniLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
				return;
			}

			int numeroSessioni = Integer.parseInt(numSessioniStr);
			if (numeroSessioni <= 0) {
				endDatePicker.setValue(null);
				numeroSessioniLabel.setText("Il numero di sessioni deve essere maggiore di 0");
				numeroSessioniLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
				return;
			}

			LocalDate dataFine = calcolaDataFineFromFrequenza(inizio, numeroSessioni, freq);
			endDatePicker.setValue(dataFine);

			numeroSessioniLabel
					.setText(String.format("Sessioni: %d | Periodo: %s -> %s", numeroSessioni, inizio, dataFine));
			numeroSessioniLabel.setStyle("-fx-text-fill: #28a745; -fx-font-size: 13px; -fx-font-weight: bold;");

		} catch (NumberFormatException e) {
			endDatePicker.setValue(null);
			numeroSessioniLabel.setText("Numero sessioni non valido");
			numeroSessioniLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
		}
	}

	private LocalDate calcolaDataFineFromFrequenza(LocalDate inizio, int numeroSessioni, Frequenza frequenza) {
		return switch (frequenza) {
		case unica -> inizio;
		case giornaliero -> inizio.plusDays(numeroSessioni - 1);
		case ogniDueGiorni -> inizio.plusDays((numeroSessioni - 1) * 2);
		case settimanale -> inizio.plusWeeks(numeroSessioni - 1);
		case mensile -> inizio.plusMonths(numeroSessioni - 1);
		};
	}

	private VBox createChefSection() {
		VBox section = StyleHelper.createSection();

		Button selezionaChefBtn = StyleHelper.createPrimaryButton("Seleziona Chef");
		selezionaChefBtn.setOnAction(e -> apriDialogSelezionaChef());

		listaChefContainer = createListContainer();
		chefSelezionati.addListener((ListChangeListener<Chef>) c -> updateChefDisplay());
		updateChefDisplay();

		section.getChildren().addAll(createSectionTitle("Selezione Chef"), selezionaChefBtn,
				StyleHelper.createLabel("Chef Selezionati:"), listaChefContainer);
		return section;
	}

	private VBox createSessionSection() {
		VBox section = StyleHelper.createSection();

		aggiungiSessioneBtn = StyleHelper.createSuccessButton("Aggiungi Sessione");
		aggiungiSessioneBtn.setOnAction(e -> aggiungiSessione());

		listaSessioniContainer = createListContainer();
		corsoSessioni.addListener((ListChangeListener<Sessione>) c -> updateSessioniDisplay());
		updateSessioniDisplay();

		section.getChildren().addAll(createSectionTitle("Sessioni del Corso"), aggiungiSessioneBtn,
				StyleHelper.createLabel("Sessioni aggiunte:"), listaSessioniContainer);
		return section;
	}

	private HBox createButtonSection() {
		Button resetBtn = StyleHelper.createSecondaryButton("Reset Form");
		resetBtn.setPrefWidth(150);
		resetBtn.setOnAction(e -> clearForm());

		Button salvaBtn = StyleHelper.createPrimaryButton("Salva Corso");
		salvaBtn.setPrefWidth(150);
		salvaBtn.setOnAction(e -> salvaCorso());

		HBox box = new HBox(15, resetBtn, salvaBtn);
		box.setAlignment(Pos.CENTER);
		box.setPadding(new Insets(20, 0, 0, 0));
		return box;
	}

	private ComboBox<Frequenza> createFrequenzaComboBox() {
		ComboBox<Frequenza> combo = StyleHelper.createComboBox();
		combo.setPromptText("Seleziona frequenza");
		combo.setConverter(new StringConverter<Frequenza>() {
			@Override
			public String toString(Frequenza frequenza) {
				return frequenza != null ? frequenza.getDescrizione() : "";
			}

			@Override
			public Frequenza fromString(String string) {
				return combo.getItems().stream().filter(f -> f.getDescrizione().equals(string)).findFirst()
						.orElse(null);
			}
		});
		return combo;
	}

	private HBox createTimeBox(ComboBox<Integer> hour, ComboBox<Integer> minute) {
		HBox box = new HBox(5, hour, new Label(":"), minute);
		box.setAlignment(Pos.CENTER_LEFT);
		return box;
	}

	private ComboBox<Integer> createTimeComboBox(int max, int defaultVal) {
		return createTimeComboBox(max, defaultVal, 1);
	}

	private ComboBox<Integer> createTimeComboBox(int max, int defaultVal, int step) {
		ComboBox<Integer> combo = StyleHelper.createComboBox();
		for (int i = 0; i < max; i += step) {
			combo.getItems().add(i);
		}
		combo.setValue(defaultVal);
		return combo;
	}

	private VBox createListContainer() {
		VBox box = new VBox(10);
		box.setPadding(new Insets(10));
		box.setStyle(
				"-fx-background-color: #f5f5f5; -fx-background-radius: 8; -fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 8;");
		return box;
	}

	private Label createSectionTitle(String text) {
		Label label = new Label(text);
		label.setFont(Font.font("Arial", FontWeight.BOLD, 16));
		label.setTextFill(Color.web("#FF6F00"));
		return label;
	}

	private Label createInfoLabel(String text, String color) {
		Label label = new Label(text);
		label.setTextFill(Color.web(color));
		label.setFont(Font.font(13));
		return label;
	}

	private Label createEmptyLabel(String text) {
		Label label = new Label(text);
		label.setTextFill(Color.web("#d32f2f"));
		label.setFont(Font.font(13));
		label.setStyle("-fx-font-weight: bold;");
		return label;
	}

	private GridPane createGrid(int hgap, int vgap) {
		GridPane grid = new GridPane();
		grid.setHgap(hgap);
		grid.setVgap(vgap);
		grid.setAlignment(Pos.CENTER_LEFT);
		return grid;
	}

	private void apriDialogSelezionaChef() {
		try {
			SelezionaChefDialog dialog = new SelezionaChefDialog(chefController);
			Chef scelto = dialog.showAndReturn();

			if (scelto != null) {
				if (!chefSelezionati.contains(scelto)) {
					chefSelezionati.add(scelto);
				} else {
					StyleHelper.showValidationDialog("Chef già selezionato",
							"Questo chef è già stato aggiunto al corso");
				}
			}
		} catch (Exception e) {
			StyleHelper.showErrorDialog("Errore", "Errore durante la selezione chef: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void aggiungiSessione() {
		try {
			LocalDate inizio = startDatePicker.getValue();
			LocalDate fine = endDatePicker.getValue();
			Frequenza freq = frequenzaBox.getValue();

			if (inizio == null || fine == null) {
				throw new ValidationException("Seleziona le date di inizio e fine del corso");
			}
			if (freq == null) {
				throw new ValidationException("Seleziona la frequenza del corso");
			}

			
			Set<LocalDate> dateFineOccupate = new HashSet<>();
			for (Sessione s : corsoSessioni) {
				if (s.getDataFineSessione() != null) {
					dateFineOccupate.add(s.getDataFineSessione().toLocalDate());
				}
			}

			CreaSessioniGUI dialog = new CreaSessioniGUI(inizio, fine, freq, dateFineOccupate, // ✅ Passa date FINE
					ricettaController, ingredienteController);

			Sessione nuovaSessione = dialog.showDialog();

			if (nuovaSessione != null) {
				corsoSessioni.add(nuovaSessione);
				StyleHelper.showSuccessDialog("Successo", "Sessione aggiunta alla lista del corso");
			}

		} catch (ValidationException ve) {
			StyleHelper.showValidationDialog("Errore", ve.getMessage());
		} catch (Exception e) {
			StyleHelper.showErrorDialog("Errore", "Errore durante l'aggiunta della sessione: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void salvaCorso() {
		try {
			if (nomeField.getText().trim().isEmpty()) {
				throw new ValidationException("Inserisci il nome del corso");
			}
			if (chefSelezionati.isEmpty()) {
				throw new ValidationException("Seleziona almeno uno chef per il corso");
			}
			if (corsoSessioni.isEmpty()) {
				throw new ValidationException("Aggiungi almeno una sessione al corso");
			}

			LocalDate inizio = startDatePicker.getValue();
			LocalDate fine = endDatePicker.getValue();
			Frequenza freq = frequenzaBox.getValue();

			if (inizio == null || fine == null) {
				throw new ValidationException("Seleziona data inizio e numero sessioni");
			}
			if (freq == null) {
				throw new ValidationException("Seleziona la frequenza del corso");
			}

			String numSessioniStr = numeroSessioniField.getText().trim();
			if (numSessioniStr.isEmpty()) {
				throw new ValidationException("Specifica il numero di sessioni");
			}

			int numeroSessioniPreviste = Integer.parseInt(numSessioniStr);
			int numeroSessioniAggiunte = corsoSessioni.size();

			if (numeroSessioniAggiunte < numeroSessioniPreviste) {
				throw new ValidationException(String.format(
						"Sessioni incomplete!\nHai aggiunto %d sessioni ma ne sono previste %d.\nMancano %d sessioni.",
						numeroSessioniAggiunte, numeroSessioniPreviste,
						numeroSessioniPreviste - numeroSessioniAggiunte));
			}

			if (numeroSessioniAggiunte > numeroSessioniPreviste) {
				throw new ValidationException(String.format(
						"Troppo sessioni!\nHai aggiunto %d sessioni ma ne sono previste %d.\nRimuovi %d sessioni.",
						numeroSessioniAggiunte, numeroSessioniPreviste,
						numeroSessioniAggiunte - numeroSessioniPreviste));
			}

			chefController.saveCorsoFromForm(nomeField.getText(), prezzoField.getText(), argomentoField.getText(),
					postiField.getText(), freq, inizio, startHour.getValue(), startMinute.getValue(), fine,
					endHour.getValue(), endMinute.getValue(), new ArrayList<>(chefSelezionati),
					new ArrayList<>(corsoSessioni));

			StyleHelper.showSuccessDialog("Successo", "Corso creato con successo");
			clearForm();

		} catch (ValidationException ve) {
			StyleHelper.showValidationDialog("Errore", ve.getMessage());
		} catch (NumberFormatException nfe) {
			StyleHelper.showValidationDialog("Errore", "Numero di sessioni non valido");
		} catch (Exception e) {
			StyleHelper.showErrorDialog("Errore", "Errore durante il salvataggio: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void updateChefDisplay() {
		listaChefContainer.getChildren().clear();

		if (chefSelezionati.isEmpty()) {
			listaChefContainer.getChildren().add(createEmptyLabel("Nessuno chef selezionato"));
		} else {
			chefSelezionati.forEach(c -> listaChefContainer.getChildren().add(createChefBox(c)));
		}
	}

	private HBox createChefBox(Chef chef) {
		HBox chefBox = new HBox(10);
		chefBox.setAlignment(Pos.CENTER_LEFT);
		chefBox.setPadding(new Insets(8));
		chefBox.setStyle(
				"-fx-background-color: white; -fx-background-radius: 6; -fx-border-color: #FF6600; -fx-border-radius: 6; -fx-border-width: 1.5;");

		Label chefLabel = new Label(String.format("%s %s", chef.getNome(), chef.getCognome()));
		chefLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Button rimuoviBtn = new Button("X");
		rimuoviBtn.setStyle(
				"-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-cursor: hand; -fx-min-width: 25; -fx-min-height: 25; -fx-max-width: 25; -fx-max-height: 25; -fx-font-size: 11px;");
		rimuoviBtn.setOnAction(e -> {
			chefSelezionati.remove(chef);
			updateChefDisplay();
		});

		chefBox.getChildren().addAll(chefLabel, spacer, rimuoviBtn);
		return chefBox;
	}

	private void updateSessioniDisplay() {
		listaSessioniContainer.getChildren().clear();

		if (corsoSessioni.isEmpty()) {
			listaSessioniContainer.getChildren().add(createEmptyLabel("Nessuna sessione aggiunta"));
		} else {
			for (Sessione s : corsoSessioni) {
				listaSessioniContainer.getChildren().add(createSessioneBox(s));
			}
		}

		String numSessioniStr = numeroSessioniField.getText().trim();
		if (!numSessioniStr.isEmpty()) {
			try {
				int previste = Integer.parseInt(numSessioniStr);
				int aggiunte = corsoSessioni.size();

				if (aggiunte == previste) {
					numeroSessioniLabel.setText(String.format("Sessioni: %d/%d completate", aggiunte, previste));
					numeroSessioniLabel.setStyle("-fx-text-fill: #28a745; -fx-font-size: 13px; -fx-font-weight: bold;");
				} else if (aggiunte < previste) {
					numeroSessioniLabel.setText(
							String.format("Sessioni: %d/%d (mancano %d)", aggiunte, previste, previste - aggiunte));
					numeroSessioniLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 13px; -fx-font-weight: bold;");
				} else {
					numeroSessioniLabel.setText(String.format("Sessioni: %d (previste %d)", aggiunte, previste));
					numeroSessioniLabel.setStyle("-fx-text-fill: #17a2b8; -fx-font-size: 13px; -fx-font-weight: bold;");
				}
			} catch (NumberFormatException e) {
				// Ignora
			}
		}
	}

	private HBox createSessioneBox(Sessione s) {
		HBox sessioneBox = new HBox(10);
		sessioneBox.setAlignment(Pos.CENTER_LEFT);
		sessioneBox.setPadding(new Insets(8));
		sessioneBox.setStyle(
				"-fx-background-color: white; -fx-background-radius: 6; -fx-border-color: #28a745; -fx-border-radius: 6; -fx-border-width: 1.5;");

		Label numeroLabel = new Label((corsoSessioni.indexOf(s) + 1) + ".");
		numeroLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF6600; -fx-font-size: 13px;");

		Label infoLabel = new Label(formatSessioneDettagliata(s));
		infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c3e50;");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Button rimuoviBtn = new Button("Rimuovi");
		rimuoviBtn.setStyle(
				"-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand; -fx-min-width: 30; -fx-min-height: 30;");
		rimuoviBtn.setOnAction(e -> {
			corsoSessioni.remove(s);
			updateSessioniDisplay();
		});

		sessioneBox.getChildren().addAll(numeroLabel, infoLabel, spacer, rimuoviBtn);
		return sessioneBox;
	}

	private String formatSessioneDettagliata(Sessione s) {
		String tipo = s instanceof Online ? "Online" : "In Presenza";
		String data = s.getDataInizioSessione() != null ? s.getDataInizioSessione().toLocalDate().toString()
				: "Data non specificata";
		String orario = s.getDataInizioSessione() != null && s.getDataFineSessione() != null
				? s.getDataInizioSessione().toLocalTime() + " - " + s.getDataFineSessione().toLocalTime()
				: "";

		if (s instanceof InPresenza ip) {
			int numRicette = ip.getRicette() != null ? ip.getRicette().size() : 0;
			return tipo + " | " + data + " " + orario + " | Ricette: " + numRicette;
		} else if (s instanceof Online on) {
			return tipo + " (" + on.getPiattaformaStreaming() + ") | " + data + " " + orario;
		}

		return tipo + " | " + data + " " + orario;
	}

	private void clearForm() {
		nomeField.clear();
		prezzoField.clear();
		argomentoField.clear();
		postiField.clear();
		frequenzaBox.setValue(null);
		frequenzaBox.getItems().clear();
		frequenzaBox.getItems().setAll(Frequenza.values());
		startDatePicker.setValue(null);
		endDatePicker.setValue(null);
		numeroSessioniField.clear();
		startHour.setValue(DEFAULT_START_HOUR);
		startMinute.setValue(DEFAULT_START_MINUTE);
		endHour.setValue(DEFAULT_END_HOUR);
		endMinute.setValue(DEFAULT_END_MINUTE);
		chefSelezionati.clear();
		corsoSessioni.clear();
		updateChefDisplay();
		updateSessioniDisplay();
		numeroSessioniLabel.setText("Seleziona data inizio, frequenza e numero sessioni");
		numeroSessioniLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
	}
}