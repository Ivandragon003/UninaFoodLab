package Gui;

import controller.GestioneCorsoController;
import controller.ChefController;
import controller.RicettaController;
import exceptions.ValidationException;
import exceptions.DataAccessException;
import exceptions.ErrorMessages;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.*;
import util.StyleHelper;
import util.FrequenzaHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Interfaccia per la creazione di un corso.
 */
public class CreaCorsoGUI {
	private final GestioneCorsoController gestioneCorsoController;
	private final ChefController chefController;
	private final RicettaController ricettaController;

	// Campi input
	private TextField nomeField, prezzoField, argomentoField, postiField;
	private ComboBox<Frequenza> frequenzaBox;
	private DatePicker startDatePicker, endDatePicker;
	private ComboBox<Integer> startHour, startMinute, endHour, endMinute;

	// Container liste
	private VBox listaChefContainer, listaSessioniContainer;
	private Label numeroSessioniLabel;

	// Dati
	private final ObservableList<Chef> chefSelezionati = FXCollections.observableArrayList();
	private final ObservableList<Sessione> corsoSessioni = FXCollections.observableArrayList();

	private VBox root;
	private Button aggiungiSessioneBtn;

	public CreaCorsoGUI(GestioneCorsoController gestioneCorsoController,
						ChefController chefController,
						RicettaController ricettaController) {
		this.gestioneCorsoController = gestioneCorsoController;
		this.chefController = chefController;
		this.ricettaController = ricettaController;
	}

	public VBox getRoot() {
		if (root == null) {
			root = createMainLayout();
		}
		return root;
	}

	// ==================== LAYOUT PRINCIPALE ====================

	private VBox createMainLayout() {
		VBox container = new VBox(15);
		container.setPadding(new Insets(20));
		StyleHelper.applyOrangeBackground(container);

		Label titleLabel = StyleHelper.createTitleLabel("✨ Crea Nuovo Corso di Cucina");
		titleLabel.setAlignment(Pos.CENTER);
		titleLabel.setTextFill(Color.WHITE);

		ScrollPane scrollPane = new ScrollPane(new VBox(15,
			createInfoSection(),
			new Separator(),
			createDateTimeSection(),
			new Separator(),
			createChefSection(),
			new Separator(),
			createSessionSection(),
			new Separator(),
			createButtonSection()));
		scrollPane.setFitToWidth(true);
		scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

		container.getChildren().addAll(titleLabel, scrollPane);
		return container;
	}

	// ==================== SEZIONI ====================

	private VBox createInfoSection() {
		VBox section = StyleHelper.createSection();

		nomeField = StyleHelper.createTextField("Es. Corso Base di Pasta Italiana");
		prezzoField = StyleHelper.createTextField("Es. 150.00");
		argomentoField = StyleHelper.createTextField("Es. Pasta fresca e condimenti");
		postiField = StyleHelper.createTextField("Es. 12");

		// reset stile automatico mentre l'utente scrive
		nomeField.textProperty().addListener((obs,old,val)-> { if (!val.trim().isEmpty()) StyleHelper.setNeutralStyle(nomeField); });
		prezzoField.textProperty().addListener((obs,old,val)-> { if (!val.trim().isEmpty()) StyleHelper.setNeutralStyle(prezzoField); });
		postiField.textProperty().addListener((obs,old,val)-> { if (!val.trim().isEmpty()) StyleHelper.setNeutralStyle(postiField); });

		frequenzaBox = createFrequenzaComboBox();

		GridPane grid = createGrid(15, 15);
		grid.add(StyleHelper.createLabel("Nome Corso:"), 0, 0);
		grid.add(nomeField, 1, 0);
		grid.add(StyleHelper.createLabel("Prezzo (€):"), 2, 0);
		grid.add(prezzoField, 3, 0);
		grid.add(StyleHelper.createLabel("Argomento:"), 0, 1);
		grid.add(argomentoField, 1, 1);
		grid.add(StyleHelper.createLabel("Numero Posti:"), 2, 1);
		grid.add(postiField, 3, 1);
		grid.add(StyleHelper.createLabel("Frequenza:"), 0, 2);
		grid.add(frequenzaBox, 1, 2);

		section.getChildren().addAll(createSectionTitle("📋 Informazioni Corso"), grid);
		return section;
	}

	private VBox createDateTimeSection() {
		VBox section = StyleHelper.createSection();

		startDatePicker = StyleHelper.createDatePicker();
		endDatePicker = StyleHelper.createDatePicker();
		startDatePicker.setPromptText("Data inizio");
		endDatePicker.setPromptText("Data fine");

		// Migliore gestione eventi date
		startDatePicker.setOnAction(e -> {
			onDataInizioChange();
			validateDatesForSessions();
			aggiornaNumeroSessioniCalcolato();
		});

		endDatePicker.setOnAction(e -> {
			aggiornaFrequenzeDisponibili();
			validateDatesForSessions();
			aggiornaNumeroSessioniCalcolato();
		});

		startHour = createTimeComboBox(24, 9);
		startMinute = createTimeComboBox(60, 0, 15);
		endHour = createTimeComboBox(24, 17);
		endMinute = createTimeComboBox(60, 0, 15);

		GridPane grid = createGrid(15, 15);
		grid.add(StyleHelper.createLabel("Data Inizio:"), 0, 0);
		grid.add(startDatePicker, 1, 0);
		grid.add(StyleHelper.createLabel("Ora Inizio:"), 2, 0);
		grid.add(createTimeBox(startHour, startMinute), 3, 0);
		grid.add(StyleHelper.createLabel("Data Fine:"), 0, 1);
		grid.add(endDatePicker, 1, 1);
		grid.add(StyleHelper.createLabel("Ora Fine:"), 2, 1);
		grid.add(createTimeBox(endHour, endMinute), 3, 1);

		numeroSessioniLabel = createInfoLabel("📊 Sessioni: Non calcolate", "#666666");

		Label avisoLabel = createInfoLabel("⚠️ Seleziona le date per vedere le frequenze disponibili",
			StyleHelper.ERROR_RED);

		section.getChildren().addAll(
			createSectionTitle("📅 Date e Orari - OBBLIGATORIO"),
			grid,
			numeroSessioniLabel,
			avisoLabel
		);
		return section;
	}

	private VBox createChefSection() {
		VBox section = StyleHelper.createSection();

		Button selezionaChefBtn = StyleHelper.createPrimaryButton("+ Seleziona Chef");
		selezionaChefBtn.setOnAction(e -> apriDialogSelezionaChef());

		listaChefContainer = createListContainer();
		updateChefDisplay();

		section.getChildren().addAll(
			createSectionTitle("👨‍🍳 Selezione Chef"),
			selezionaChefBtn,
			StyleHelper.createLabel("Chef Selezionati:"),
			listaChefContainer
		);
		return section;
	}

	private VBox createSessionSection() {
		VBox section = StyleHelper.createSection();

		aggiungiSessioneBtn = StyleHelper.createSuccessButton("+ Aggiungi Sessione");
		aggiungiSessioneBtn.setDisable(true);
		aggiungiSessioneBtn.setOnAction(e -> aggiungiSessione());

		Button eliminaBtn = StyleHelper.createDangerButton("🗑️ Rimuovi Ultima");
		eliminaBtn.setOnAction(e -> eliminaUltimaSessione());

		HBox buttonBox = new HBox(10, aggiungiSessioneBtn, eliminaBtn);

		listaSessioniContainer = createListContainer();
		updateSessioniDisplay();

		section.getChildren().addAll(
			createSectionTitle("🎯 Sessioni del Corso"),
			buttonBox,
			StyleHelper.createLabel("Sessioni aggiunte:"),
			listaSessioniContainer
		);
		return section;
	}

	private HBox createButtonSection() {
		Button resetBtn = StyleHelper.createSecondaryButton("🔄 Reset Form");
		resetBtn.setPrefWidth(150);
		resetBtn.setOnAction(e -> confermaReset());

		Button salvaBtn = StyleHelper.createPrimaryButton("💾 Salva Corso");
		salvaBtn.setPrefWidth(150);
		salvaBtn.setOnAction(e -> salvaCorso());

		HBox box = new HBox(15, resetBtn, salvaBtn);
		box.setAlignment(Pos.CENTER);
		box.setPadding(new Insets(20, 0, 0, 0));
		return box;
	}

	// ==================== GESTIONE FREQUENZA E DATE ====================

	private ComboBox<Frequenza> createFrequenzaComboBox() {
		ComboBox<Frequenza> combo = new ComboBox<>();
		combo.setPromptText("Seleziona frequenza");
		combo.setPrefHeight(35);
		combo.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
		combo.setOnAction(e -> onFrequenzaChange());
		return combo;
	}

	private void onDataInizioChange() {
		LocalDate inizio = startDatePicker.getValue();

		if (frequenzaBox.getValue() == Frequenza.UNICA && inizio != null) {
			endDatePicker.setValue(inizio);
		}

		aggiornaFrequenzeDisponibili();
	}

	private void onFrequenzaChange() {
		Frequenza selezionata = frequenzaBox.getValue();

		if (selezionata == Frequenza.UNICA) {
			if (startDatePicker.getValue() != null) {
				endDatePicker.setValue(startDatePicker.getValue());
			}
			endDatePicker.setDisable(true);
			endDatePicker.setStyle("-fx-opacity: 0.6;");
		} else {
			endDatePicker.setDisable(false);
			endDatePicker.setStyle("");
		}

		aggiornaNumeroSessioniCalcolato();
	}

	private void aggiornaFrequenzeDisponibili() {
		LocalDate inizio = startDatePicker.getValue();
		LocalDate fine = endDatePicker.getValue();

		Frequenza attuale = frequenzaBox.getValue();

		if (inizio != null && fine != null) {
			// usa getFrequenzeDisponibili per ridurre opzioni non valide
			java.util.List<Frequenza> disponibili = FrequenzaHelper.getFrequenzeDisponibili(inizio, fine);
			frequenzaBox.getItems().setAll(disponibili);
			// se attuale è ancora valido, mantienilo
			if (attuale != null && disponibili.contains(attuale)) {
				frequenzaBox.setValue(attuale);
			}
		} else {
			frequenzaBox.getItems().setAll(Frequenza.values());
			if (attuale != null && frequenzaBox.getItems().contains(attuale)) {
				frequenzaBox.setValue(attuale);
			}
		}

		aggiornaNumeroSessioniCalcolato();
	}

	private void aggiornaNumeroSessioniCalcolato() {
		LocalDate inizio = startDatePicker.getValue();
		LocalDate fine = endDatePicker.getValue();
		Frequenza freq = frequenzaBox.getValue();

		if (inizio != null && fine != null && freq != null) {
			try {
				int numSessioni = FrequenzaHelper.calcolaNumeroSessioni(inizio, fine, freq);
				numeroSessioniLabel.setText(String.format("📊 Sessioni calcolate: %d", numSessioni));
				numeroSessioniLabel.setTextFill(Color.web("#4CAF50"));
			} catch (Exception e) {
				numeroSessioniLabel.setText("❌ Errore calcolo sessioni");
				numeroSessioniLabel.setTextFill(Color.web("#F44336"));
			}
		} else {
			numeroSessioniLabel.setText("📊 Sessioni: Non calcolate");
			numeroSessioniLabel.setTextFill(Color.web("#666666"));
		}
	}

	private void validateDatesForSessions() {
		boolean dateValid = startDatePicker.getValue() != null && endDatePicker.getValue() != null;
		if (aggiungiSessioneBtn != null) {
			aggiungiSessioneBtn.setDisable(!dateValid);
			aggiungiSessioneBtn.setText(dateValid ? "+ Aggiungi Sessione" : "❌ Date Corso Richieste");
		}
	}

	// ==================== GESTIONE CHEF ====================

	private void updateChefDisplay() {
		listaChefContainer.getChildren().clear();

		if (chefSelezionati.isEmpty()) {
			listaChefContainer.getChildren().add(createEmptyLabel("Nessun chef selezionato"));
		} else {
			chefSelezionati.forEach(chef -> listaChefContainer.getChildren().add(createChefCard(chef)));
		}
	}

	private HBox createChefCard(Chef chef) {
		VBox infoBox = new VBox(3);

		Label nameLabel = new Label("👨‍🍳 " + chef.getNome() + " " + chef.getCognome());
		nameLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
		nameLabel.setTextFill(Color.BLACK);

		Label expLabel = new Label("📅 " + chef.getAnniEsperienza() + " anni di esperienza");
		expLabel.setFont(Font.font("Roboto", 12));
		expLabel.setTextFill(Color.GRAY);

		// Mostra disponibilità
		String disponibilitaText = chef.getDisponibilita() ? "✅ Disponibile" : "❌ Non disponibile";
		Label dispLabel = new Label(disponibilitaText);
		dispLabel.setFont(Font.font("Roboto", 12));
		dispLabel.setTextFill(chef.getDisponibilita() ?
			Color.web(StyleHelper.SUCCESS_GREEN) : Color.web(StyleHelper.ERROR_RED));

		infoBox.getChildren().addAll(nameLabel, expLabel, dispLabel);

		Button removeBtn = createRemoveButton(() -> {
			chefSelezionati.remove(chef);
			updateChefDisplay();
		});

		return createItemCard(infoBox, removeBtn, "#f0f8ff", "#87ceeb");
	}

	// dialog per selezionare chef (usa SelezionaChefDialog)
	private void apriDialogSelezionaChef() {
		try {
			SelezionaChefDialog dialog = new SelezionaChefDialog(chefController);
			Chef scelto = dialog.showAndReturn();

			if (scelto != null) {
				if (isChefGiaSelezionato(scelto)) {
					StyleHelper.showInfoDialog("Chef già selezionato",
						"Questo chef è già stato selezionato per questo corso.");
				} else {
					chefSelezionati.add(scelto);
					updateChefDisplay();

					if (!scelto.getDisponibilita()) {
						StyleHelper.showInfoDialog("Attenzione",
							"Chef aggiunto, ma risulta non disponibile.");
					} else {
						StyleHelper.showSuccessDialog("Chef aggiunto",
							"Chef " + scelto.getNome() + " " + scelto.getCognome() + " aggiunto con successo!");
					}
				}
			}
		} catch (Exception e) {
			StyleHelper.showErrorDialog("Errore", "Errore nella selezione chef: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private boolean isChefGiaSelezionato(Chef chef) {
		return chefSelezionati.stream()
			.anyMatch(c -> c.getUsername().equals(chef.getUsername()));
	}

	// ==================== GESTIONE SESSIONI ====================

	private void updateSessioniDisplay() {
		listaSessioniContainer.getChildren().clear();

		if (corsoSessioni.isEmpty()) {
			listaSessioniContainer.getChildren().add(createEmptyLabel("Nessuna sessione aggiunta"));
		} else {
			for (int i = 0; i < corsoSessioni.size(); i++) {
				final int index = i;
				listaSessioniContainer.getChildren().add(createSessioneCard(corsoSessioni.get(i), index));
			}
		}
	}

	private HBox createSessioneCard(Sessione sessione, int index) {
		VBox infoBox = new VBox(3);

		String tipo = sessione instanceof Online ? "🌐 Online" : "🏢 In Presenza";
		Label tipoLabel = new Label(tipo);
		tipoLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
		tipoLabel.setTextFill(Color.BLACK);

		String data = sessione.getDataInizioSessione() != null
			? "📅 " + sessione.getDataInizioSessione().toLocalDate().toString()
			: "Data non specificata";
		Label dataLabel = new Label(data);
		dataLabel.setFont(Font.font("Roboto", 13));
		dataLabel.setTextFill(Color.web("#333333"));

		infoBox.getChildren().addAll(tipoLabel, dataLabel);

		String dettagli = getSessioneDettagli(sessione);
		if (!dettagli.isEmpty()) {
			Label dettagliLabel = new Label(dettagli);
			dettagliLabel.setFont(Font.font("Roboto", 12));
			dettagliLabel.setTextFill(Color.GRAY);
			infoBox.getChildren().add(dettagliLabel);
		}

		Button removeBtn = createRemoveButton(() -> {
			corsoSessioni.remove(index);
			updateSessioniDisplay();
		});

		return createItemCard(infoBox, removeBtn, "#f0fff0", "#90ee90");
	}

	private String getSessioneDettagli(Sessione sessione) {
		if (sessione instanceof Online) {
			return "💻 " + ((Online) sessione).getPiattaformaStreaming();
		} else if (sessione instanceof InPresenza) {
			InPresenza p = (InPresenza) sessione;
			return "📍 " + p.getVia() + ", " + p.getCitta();
		}
		return "";
	}

	private void aggiungiSessione() {
		LocalDate dataInizio = startDatePicker.getValue();
		LocalDate dataFine = endDatePicker.getValue();
		Frequenza frequenza = frequenzaBox.getValue();

		if (dataInizio == null || dataFine == null) {
			StyleHelper.showErrorDialog("Errore", "Inserire prima le date del corso");
			return;
		}

		if (frequenza == null) {
			StyleHelper.showErrorDialog("Errore", "Selezionare prima la frequenza del corso");
			return;
		}

		// Validazione date corso
		if (dataInizio.isAfter(dataFine)) {
			StyleHelper.showErrorDialog("Errore", "La data di inizio non può essere dopo la data di fine");
			return;
		}

		try {
			Set<LocalDate> dateOccupate = new HashSet<>();
			corsoSessioni.forEach(s -> {
				if (s.getDataInizioSessione() != null) {
					dateOccupate.add(s.getDataInizioSessione().toLocalDate());
				}
			});

			CreaSessioniGUI sessioneDialog = new CreaSessioniGUI(
				dataInizio,
				dataFine,
				frequenza,
				dateOccupate,
				ricettaController
			);
			Sessione nuovaSessione = sessioneDialog.showDialog();

			if (nuovaSessione != null) {
				// verifica duplicati per la stessa data
				if (nuovaSessione.getDataInizioSessione() != null) {
					LocalDate d = nuovaSessione.getDataInizioSessione().toLocalDate();
					boolean dup = corsoSessioni.stream()
						.anyMatch(s -> s.getDataInizioSessione() != null &&
						               s.getDataInizioSessione().toLocalDate().equals(d));
					if (dup) {
						StyleHelper.showValidationDialog("Duplicato", "Esiste già una sessione per la data " + d);
						return;
					}
				}

				corsoSessioni.add(nuovaSessione);
				updateSessioniDisplay();
				StyleHelper.showSuccessDialog("Successo", "Sessione creata con successo!");
			}
		} catch (Exception e) {
			StyleHelper.showErrorDialog("Errore", "Errore creazione sessione: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void eliminaUltimaSessione() {
		if (!corsoSessioni.isEmpty()) {
			corsoSessioni.remove(corsoSessioni.size() - 1);
			updateSessioniDisplay();
			StyleHelper.showInfoDialog("Sessione rimossa", "Ultima sessione rimossa con successo");
		} else {
			StyleHelper.showInfoDialog("Nessuna sessione", "Non ci sono sessioni da rimuovere");
		}
	}

	// ==================== SALVATAGGIO ====================

	private void salvaCorso() {
		try {
			if (!validateForm()) return;

			String nome = nomeField.getText().trim();
			double prezzo = Double.parseDouble(prezzoField.getText().trim());
			String argomento = argomentoField.getText().trim();
			Frequenza frequenza = frequenzaBox.getValue();
			int numeroPosti = Integer.parseInt(postiField.getText().trim());

			// safer: fallback values in case ComboBox values are null
			int sh = startHour.getValue() != null ? startHour.getValue() : 9;
			int sm = startMinute.getValue() != null ? startMinute.getValue() : 0;
			int eh = endHour.getValue() != null ? endHour.getValue() : 17;
			int em = endMinute.getValue() != null ? endMinute.getValue() : 0;

			LocalDateTime dataInizio = LocalDateTime.of(
				startDatePicker.getValue(),
				LocalTime.of(sh, sm)
			);

			LocalDateTime dataFine = LocalDateTime.of(
				endDatePicker.getValue(),
				LocalTime.of(eh, em)
			);

			// Validazione orari
			if (dataInizio.isAfter(dataFine) || dataInizio.isEqual(dataFine)) {
				StyleHelper.showValidationDialog("Validazione",
					"La data/ora di inizio deve essere prima della data/ora di fine");
				return;
			}

			// Crea l'oggetto CorsoCucina
			CorsoCucina nuovoCorso = new CorsoCucina(nome, prezzo, argomento, frequenza, numeroPosti);
			nuovoCorso.setDataInizioCorso(dataInizio);
			nuovoCorso.setDataFineCorso(dataFine);
			nuovoCorso.setNumeroSessioni(corsoSessioni.size());

			// Aggiungi chef e sessioni
			nuovoCorso.setChef(new ArrayList<>(chefSelezionati));
			nuovoCorso.setSessioni(new ArrayList<>(corsoSessioni));

			// Salva tramite controller
			gestioneCorsoController.creaCorso(nuovoCorso);

			StyleHelper.showSuccessDialog("Successo",
				"Corso salvato con successo!\n\n" +
					"Nome: " + nome + "\n" +
					"Prezzo: €" + prezzo + "\n" +
					buildChefInfo() +
					"Sessioni create: " + corsoSessioni.size());

			clearForm();

		} catch (ValidationException ex) {
			StyleHelper.showValidationDialog("Validazione", ex.getMessage());
		} catch (DataAccessException ex) {
			StyleHelper.showErrorDialog("Errore Database", ex.getMessage());
		} catch (NumberFormatException ex) {
			StyleHelper.showValidationDialog("Validazione", ErrorMessages.FORMATO_NON_VALIDO);
		} catch (Exception ex) {
			StyleHelper.showErrorDialog("Errore", "Errore nella creazione del corso: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	private boolean validateForm() {
		// uso helper per ridurre duplicazione e applicare stile
		if (markIfEmpty(nomeField, ErrorMessages.NOME_CORSO_MANCANTE)) return false;

		if (!isValidDouble(prezzoField.getText())) {
			StyleHelper.setValidationStyle(prezzoField, false);
			StyleHelper.showValidationDialog("Validazione", ErrorMessages.FORMATO_NON_VALIDO);
			return false;
		}

		double prezzo = Double.parseDouble(prezzoField.getText().trim());
		if (prezzo <= 0) {
			StyleHelper.setValidationStyle(prezzoField, false);
			StyleHelper.showValidationDialog("Validazione", ErrorMessages.PREZZO_NON_VALIDO);
			return false;
		}

		if (!isValidInteger(postiField.getText())) {
			StyleHelper.setValidationStyle(postiField, false);
			StyleHelper.showValidationDialog("Validazione", ErrorMessages.FORMATO_NON_VALIDO);
			return false;
		}

		int posti = Integer.parseInt(postiField.getText().trim());
		if (posti <= 0) {
			StyleHelper.showValidationDialog("Validazione", ErrorMessages.NUMERO_POSTI_NON_VALIDO);
			return false;
		}

		if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
			StyleHelper.showValidationDialog("Validazione", ErrorMessages.DATE_CORSO_MANCANTI);
			return false;
		}

		if (frequenzaBox.getValue() == null) {
			StyleHelper.showValidationDialog("Validazione", "Selezionare una frequenza");
			return false;
		}

		if (!FrequenzaHelper.isFrequenzaValida(
			startDatePicker.getValue(),
			endDatePicker.getValue(),
			frequenzaBox.getValue())) {
			StyleHelper.showValidationDialog("Frequenza non valida",
				FrequenzaHelper.getMessaggioErroreFrequenza(
					startDatePicker.getValue(),
					endDatePicker.getValue(),
					frequenzaBox.getValue()));
			return false;
		}

		if (chefSelezionati.isEmpty()) {
			StyleHelper.showValidationDialog("Validazione", "Selezionare almeno uno chef per il corso");
			return false;
		}

		if (corsoSessioni.isEmpty()) {
			StyleHelper.showValidationDialog("Validazione", "Aggiungere almeno una sessione al corso");
			return false;
		}

		return true;
	}


	// NUOVO: Conferma reset
	private void confermaReset() {
		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
		confirm.setTitle("Conferma Reset");
		confirm.setHeaderText("Sei sicuro di voler resettare il form?");
		confirm.setContentText("Tutti i dati inseriti verranno persi.");

		confirm.showAndWait().ifPresent(response -> {
			if (response == ButtonType.OK) {
				clearForm();
				StyleHelper.showInfoDialog("Reset", "Form resettato con successo");
			}
		});
	}

	private void clearForm() {
		nomeField.clear();
		prezzoField.clear();
		argomentoField.clear();
		postiField.clear();
		frequenzaBox.setValue(null);
		startDatePicker.setValue(null);
		endDatePicker.setValue(null);
		endDatePicker.setDisable(false);
		startHour.setValue(9);
		startMinute.setValue(0);
		endHour.setValue(17);
		endMinute.setValue(0);
		chefSelezionati.clear();
		corsoSessioni.clear();
		updateChefDisplay();
		updateSessioniDisplay();
		validateDatesForSessions();
		numeroSessioniLabel.setText("📊 Sessioni: Non calcolate");
		numeroSessioniLabel.setTextFill(Color.web("#666666"));
	}

	// ==================== HELPER UI ====================

	private Label createSectionTitle(String text) {
		Label label = new Label(text);
		label.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
		label.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));
		return label;
	}

	private Label createInfoLabel(String text, String color) {
		Label label = new Label(text);
		label.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
		label.setTextFill(Color.web(color));
		return label;
	}

	private Label createEmptyLabel(String text) {
		Label label = new Label(text);
		label.setTextFill(Color.GRAY);
		label.setFont(Font.font("Roboto", 12));
		return label;
	}

	private VBox createListContainer() {
		VBox container = new VBox(8);
		container.setPrefHeight(150);
		container.setStyle(
			"-fx-background-color: white; -fx-border-color: #e0e0e0; " +
				"-fx-border-radius: 8; -fx-padding: 10;"
		);
		return container;
	}

	private HBox createItemCard(VBox infoBox, Button removeBtn, String bgColor, String borderColor) {
		HBox card = new HBox(10);
		card.setAlignment(Pos.CENTER_LEFT);
		card.setPadding(new Insets(10));
		card.setStyle(
			"-fx-background-color: " + bgColor + "; " +
				"-fx-border-color: " + borderColor + "; " +
				"-fx-border-radius: 8; -fx-background-radius: 8;"
		);

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		card.getChildren().addAll(infoBox, spacer, removeBtn);
		return card;
	}

	private Button createRemoveButton(Runnable action) {
		Button btn = new Button("✕");
		btn.setStyle(
			"-fx-background-color: #ff6b6b; -fx-text-fill: white; " +
				"-fx-background-radius: 15; -fx-min-width: 30; -fx-min-height: 30; " +
				"-fx-max-width: 30; -fx-max-height: 30; -fx-cursor: hand; -fx-font-weight: bold;"
		);
		btn.setOnAction(e -> action.run());
		return btn;
	}

	private GridPane createGrid(double hgap, double vgap) {
		GridPane grid = new GridPane();
		grid.setHgap(hgap);
		grid.setVgap(vgap);
		return grid;
	}

	private HBox createTimeBox(ComboBox<Integer> hour, ComboBox<Integer> minute) {
		HBox box = new HBox(5, hour, new Label(":"), minute);
		box.setAlignment(Pos.CENTER_LEFT);
		return box;
	}

	/**
	 * Combo time con step personalizzato.
	 */
	private ComboBox<Integer> createTimeComboBox(int max, int defaultValue, int step) {
		ComboBox<Integer> combo = new ComboBox<>();
		for (int i = 0; i < max; i += step) {
			combo.getItems().add(i);
		}
		// Se defaultValue non è presente nella lista (es. valori fuori step), aggiungilo
		if (!combo.getItems().contains(defaultValue)) {
			combo.getItems().add(defaultValue);
		}
		combo.setValue(defaultValue);
		combo.setPrefHeight(35);
		combo.setStyle(
			"-fx-background-color: white; -fx-background-radius: 8; " +
				"-fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-border-width: 1;"
		);
		return combo;
	}

	/**
	 * Overload: combo time con step = 1.
	 */
	private ComboBox<Integer> createTimeComboBox(int max, int defaultValue) {
		return createTimeComboBox(max, defaultValue, 1);
	}

	/**
	 * Overload: combo time con defaultValue = 0 e step = 1.
	 */
	private ComboBox<Integer> createTimeComboBox(int max) {
		return createTimeComboBox(max, 0, 1);
	}

	// ==================== VALIDAZIONI HELPER ====================

	private boolean isValidDouble(String text) {
		try {
			Double.parseDouble(text.trim());
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean isValidInteger(String text) {
		try {
			Integer.parseInt(text.trim());
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private String buildChefInfo() {
		if (chefSelezionati.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder("Chef selezionati:\n");
		chefSelezionati.forEach(
			chef -> sb.append("• ")
				.append(chef.getNome())
				.append(" ")
				.append(chef.getCognome())
				.append("\n")
		);
		return sb.toString();
	}

	// Helper per validazione + styling
	private boolean markIfEmpty(TextField field, String message) {
		if (field.getText() == null || field.getText().trim().isEmpty()) {
			StyleHelper.setValidationStyle(field, false);
			StyleHelper.showValidationDialog("Validazione", message);
			field.requestFocus();
			return true;
		} else {
			StyleHelper.setNeutralStyle(field);
			return false;
		}
	}
}
