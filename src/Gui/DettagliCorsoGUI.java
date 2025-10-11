package Gui;

import controller.GestioneCorsoController;
import model.CorsoCucina;
import model.Frequenza;
import model.Chef;
import util.FrequenzaHelper;
import guihelper.StyleHelper;
import exceptions.ValidationException;
import exceptions.DataAccessException;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DettagliCorsoGUI {
	private GestioneCorsoController gestioneController;
	private CorsoCucina corso;
	private VBox card;
	private boolean editable = false;
	private Runnable onChiudiCallback;

	private TextField nomeField, prezzoField, argomentoField, numeroPostiField, numeroSessioniField;
	private ComboBox<Frequenza> frequenzaCombo;
	private DatePicker dataInizioPicker, dataFinePicker;
	private ListView<Chef> chefListView;
	private ComboBox<Chef> addChefCombo;
	private Button addChefBtn, modificaBtn, salvaBtn, eliminaCorsoBtn;
	private Label selezionatoLabel, avisoCorsoFinitoLabel;

	public void setController(GestioneCorsoController controller) {
		this.gestioneController = controller;
	}

	public void setCorso(CorsoCucina corso) {
		this.corso = corso;
	}

	public void setOnChiudiCallback(Runnable callback) {
		this.onChiudiCallback = callback;
	}

	public StackPane getRoot() {
		if (gestioneController == null || corso == null) {
			throw new IllegalStateException("Controller o corso non impostati!");
		}

		StackPane mainContainer = new StackPane();
		mainContainer.setStyle("-fx-background-color: #F8F9FA;");

		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setFitToWidth(true);
		scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #F8F9FA;");
		scrollPane.setPadding(new Insets(30));

		card = new VBox(18);
		card.setAlignment(Pos.TOP_CENTER);
		card.setPadding(new Insets(30));
		card.setMaxWidth(850);
		card.setMinWidth(700);
		card.setStyle("-fx-background-color: white;" + "-fx-background-radius: 16;" + "-fx-border-radius: 16;"
				+ "-fx-border-color: #FF9966;" + "-fx-border-width: 2;");

		DropShadow shadow = new DropShadow(12, Color.web("#000000", 0.15));
		shadow.setOffsetY(4);
		card.setEffect(shadow);

		Label title = new Label("📋 Dettagli Corso");
		title.setFont(Font.font("Roboto", FontWeight.BOLD, 28));
		title.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

		Label fondatoreLabel = createFondatoreLabel();

		avisoCorsoFinitoLabel = new Label("⚠️ CORSO TERMINATO - Solo visualizzazione");
		avisoCorsoFinitoLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
		avisoCorsoFinitoLabel.setStyle("-fx-background-color: #FFF3CD;" + "-fx-text-fill: #856404;" + "-fx-padding: 12;"
				+ "-fx-background-radius: 8;" + "-fx-border-color: #FFEAA7;" + "-fx-border-width: 2;"
				+ "-fx-border-radius: 8;");
		avisoCorsoFinitoLabel.setVisible(false);
		avisoCorsoFinitoLabel.setManaged(false);

		createFormFields();

		chefListView = createChefListView();
		selezionatoLabel = new Label("Selezionato: nessuno");
		selezionatoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");

		createChefSelection();

		GridPane grid = createFormGrid();
		HBox addBox = new HBox(10, addChefCombo, addChefBtn);
		addBox.setAlignment(Pos.CENTER_LEFT);

		HBox buttons = createButtonsBox();

		card.getChildren().addAll(title, fondatoreLabel, avisoCorsoFinitoLabel, new Separator(), grid, new Separator(),
				StyleHelper.createLabel("👥 Chef assegnati al corso:"), chefListView, selezionatoLabel,
				StyleHelper.createLabel("➕ Aggiungi uno chef dal sistema:"), addBox, buttons);

		setEditable(false);
		applicaRestrizioniCorsoFinito();
		refreshChefListView();
		aggiornaStatoDataFine();

		VBox wrapper = new VBox(card);
		wrapper.setAlignment(Pos.TOP_CENTER);
		wrapper.setPadding(new Insets(20));
		scrollPane.setContent(wrapper);

		mainContainer.getChildren().add(scrollPane);
		return mainContainer;
	}

	private void createFormFields() {
		nomeField = StyleHelper.createTextField(safeString(corso.getNomeCorso()));
		prezzoField = StyleHelper.createTextField(String.valueOf(corso.getPrezzo()));
		argomentoField = StyleHelper.createTextField(safeString(corso.getArgomento()));
		numeroPostiField = StyleHelper.createTextField(String.valueOf(corso.getNumeroPosti()));

		setReadOnlyStyle(nomeField, prezzoField, argomentoField, numeroPostiField);

		frequenzaCombo = StyleHelper.createComboBox();
		frequenzaCombo.getItems().setAll(Frequenza.values());
		frequenzaCombo.setValue(corso.getFrequenzaCorso());
		frequenzaCombo.setDisable(true);
		frequenzaCombo.setOnAction(e -> onFrequenzaChange());

		numeroSessioniField = StyleHelper
				.createTextField(corso.getSessioni() != null ? String.valueOf(corso.getSessioni().size()) : "0");
		numeroSessioniField.setEditable(false);
		numeroSessioniField.setFocusTraversable(false);
		numeroSessioniField.setStyle("-fx-control-inner-background: #E9ECEF; -fx-text-fill: black;");

		dataInizioPicker = StyleHelper.createDatePicker();
		dataInizioPicker.setValue(corso.getDataInizioCorso() != null ? corso.getDataInizioCorso().toLocalDate() : null);
		dataInizioPicker.setDisable(true);
		dataInizioPicker.setOnAction(e -> onDataInizioChange());

		dataFinePicker = StyleHelper.createDatePicker();
		dataFinePicker.setValue(corso.getDataFineCorso() != null ? corso.getDataFineCorso().toLocalDate() : null);
		dataFinePicker.setDisable(true);
		dataFinePicker.setOnAction(e -> aggiornaFrequenzeDisponibili());
	}

	private void setReadOnlyStyle(TextInputControl... fields) {
		String readOnlyStyle = "-fx-text-fill: black; -fx-background-color: white;";
		for (TextInputControl field : fields) {
			field.setStyle(readOnlyStyle);
			field.setEditable(false);
			field.setFocusTraversable(false);
		}
	}

	private ListView<Chef> createChefListView() {
		ListView<Chef> list = new ListView<>();
		list.setPrefHeight(150);
		list.setMinHeight(100);
		list.setMaxHeight(250);
		list.setStyle("-fx-background-color: white;" + "-fx-border-color: " + StyleHelper.BORDER_LIGHT + ";"
				+ "-fx-border-radius: 8;" + "-fx-background-radius: 8;" + "-fx-border-width: 1;");

		list.setCellFactory(lv -> createChefCell());
		return list;
	}

	private ListCell<Chef> createChefCell() {
		return new ListCell<Chef>() {
			private final HBox box = new HBox(8);
			private final Label nameLabel = new Label();
			private final Label meLabel = new Label(" (io)");
			private final Label foundLabel = new Label(" 👑");
			private final Button removeBtn = new Button("🗑️ Rimuovi");

			{
				meLabel.setStyle("-fx-text-fill: " + StyleHelper.PRIMARY_ORANGE + "; -fx-font-weight: bold;");
				foundLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 16px;");
				removeBtn.setStyle("-fx-background-radius: 8;" + "-fx-background-color: " + StyleHelper.ERROR_RED + ";"
						+ "-fx-text-fill: white;" + "-fx-cursor: hand;" + "-fx-font-size: 11px;"
						+ "-fx-padding: 4 8 4 8;");
				removeBtn.setOnAction(e -> {
					Chef item = getItem();
					if (item != null)
						rimuoviChef(item);
				});
				box.setAlignment(Pos.CENTER_LEFT);
			}

			@Override
			protected void updateItem(Chef item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
					setGraphic(null);
				} else {
					nameLabel.setText(item.getNome() + " " + item.getCognome());
					box.getChildren().clear();
					box.getChildren().add(nameLabel);
					if (isFondatore(item))
						box.getChildren().add(foundLabel);
					if (isChefLoggato(item))
						box.getChildren().add(meLabel);
					if (editable && !isCorsoFinito()) {
						removeBtn.setDisable(isChefLoggato(item));
						box.getChildren().add(removeBtn);
					}
					setGraphic(box);
				}
			}
		};
	}

	private void createChefSelection() {
		addChefCombo = StyleHelper.createComboBox();
		addChefCombo.setPrefWidth(300);
		addChefCombo.setDisable(true);

		addChefCombo.setCellFactory(lv -> new ListCell<>() {
			@Override
			protected void updateItem(Chef item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null : item.getNome() + " " + item.getCognome());
			}
		});

		addChefCombo.setButtonCell(new ListCell<>() {
			@Override
			protected void updateItem(Chef item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null : item.getNome() + " " + item.getCognome());
			}
		});

		addChefBtn = StyleHelper.createSuccessButton("➕ Aggiungi");
		addChefBtn.setDisable(true);
		addChefBtn.setOnAction(e -> {
			Chef toAdd = addChefCombo.getValue();
			if (toAdd == null) {
				showStyledValidationDialog("⚠️ Attenzione", "Seleziona uno chef dalla lista");
				return;
			}
			aggiungiChef(toAdd, null);
		});

		chefListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
			if (newV == null) {
				selezionatoLabel.setText("Selezionato: nessuno");
			} else {
				String suffix = "";
				if (isFondatore(newV))
					suffix += " 👑";
				if (isChefLoggato(newV))
					suffix += " (io)";
				selezionatoLabel.setText("Selezionato: " + newV.getNome() + " " + newV.getCognome() + suffix);
			}
		});
	}

	private GridPane createFormGrid() {
		GridPane grid = new GridPane();
		grid.setHgap(15);
		grid.setVgap(15);
		grid.setAlignment(Pos.CENTER);

		int row = 0;
		grid.add(StyleHelper.createLabel("📚 Nome:"), 0, row);
		grid.add(nomeField, 1, row++);
		grid.add(StyleHelper.createLabel("💰 Prezzo (€):"), 0, row);
		grid.add(prezzoField, 1, row++);
		grid.add(StyleHelper.createLabel("📖 Argomento:"), 0, row);
		grid.add(argomentoField, 1, row++);
		grid.add(StyleHelper.createLabel("📅 Frequenza:"), 0, row);
		grid.add(frequenzaCombo, 1, row++);
		grid.add(StyleHelper.createLabel("🪑 Numero posti:"), 0, row);
		grid.add(numeroPostiField, 1, row++);
		grid.add(StyleHelper.createLabel("⏰ Numero sessioni:"), 0, row);
		grid.add(numeroSessioniField, 1, row++);
		grid.add(StyleHelper.createLabel("🕑 Data inizio:"), 0, row);
		grid.add(dataInizioPicker, 1, row++);
		grid.add(StyleHelper.createLabel("🏁 Data fine:"), 0, row);
		grid.add(dataFinePicker, 1, row++);

		ColumnConstraints c0 = new ColumnConstraints(150);
		ColumnConstraints c1 = new ColumnConstraints();
		c1.setHgrow(Priority.ALWAYS);
		c1.setMinWidth(350);
		grid.getColumnConstraints().addAll(c0, c1);
		return grid;
	}

	private HBox createButtonsBox() {
		HBox buttons = new HBox(15);
		buttons.setAlignment(Pos.CENTER);
		buttons.setPadding(new Insets(20, 0, 0, 0));

		modificaBtn = StyleHelper.createInfoButton("✏️ Modifica");
		modificaBtn.setPrefWidth(140);
		modificaBtn.setOnAction(e -> {
			setEditable(true);
			salvaBtn.setDisable(false);
			modificaBtn.setDisable(true);
		});

		salvaBtn = StyleHelper.createSuccessButton("💾 Salva");
		salvaBtn.setPrefWidth(140);
		salvaBtn.setDisable(true);
		salvaBtn.setOnAction(e -> salvaModifiche());

		Button visualizzaSessioniBtn = StyleHelper.createPrimaryButton("👁️ Sessioni");
		visualizzaSessioniBtn.setPrefWidth(140);
		visualizzaSessioniBtn.setOnAction(e -> apriVisualizzaSessioni());

		eliminaCorsoBtn = StyleHelper.createDangerButton("🗑️ Elimina");
		eliminaCorsoBtn.setPrefWidth(140);
		eliminaCorsoBtn.setOnAction(e -> richiediEliminazione());

		Button chiudiBtn = StyleHelper.createSecondaryButton("❌ Chiudi");
		chiudiBtn.setPrefWidth(140);
		chiudiBtn.setOnAction(e -> tornaAllaListaCorsi());

		buttons.getChildren().addAll(modificaBtn, salvaBtn, visualizzaSessioniBtn, eliminaCorsoBtn, chiudiBtn);
		return buttons;
	}

	private Label createFondatoreLabel() {
		Chef chefLoggato = gestioneController.getChefLoggato();
		boolean sonoIlFondatore = chefLoggato != null && corso.getCodfiscaleFondatore() != null
				&& chefLoggato.getCodFiscale().equals(corso.getCodfiscaleFondatore());

		Label label = new Label(
				sonoIlFondatore ? "👑 Sei il fondatore di questo corso" : "👤 Fondatore: " + getNomeFondatore());
		label.setMaxWidth(Double.MAX_VALUE);
		label.setAlignment(Pos.CENTER);
		label.setStyle(sonoIlFondatore
				? "-fx-background-color: linear-gradient(to right, #FFD700, #FFA500);"
						+ "-fx-text-fill: #4B2E2E; -fx-padding: 12; -fx-background-radius: 10;"
						+ "-fx-font-weight: bold; -fx-font-size: 14px;"
						+ "-fx-effect: dropshadow(gaussian, rgba(255, 215, 0, 0.4), 8, 0, 0, 2);"
				: "-fx-background-color: #E3F2FD; -fx-text-fill: #1565C0;"
						+ "-fx-padding: 10; -fx-background-radius: 8; -fx-font-size: 13px;");
		return label;
	}

	private String getNomeFondatore() {
		if (corso.getChef() != null && corso.getCodfiscaleFondatore() != null) {
			return corso.getChef().stream().filter(c -> c.getCodFiscale().equals(corso.getCodfiscaleFondatore()))
					.map(c -> c.getNome() + " " + c.getCognome()).findFirst().orElse("Sconosciuto");
		}
		return "Sconosciuto";
	}

	private boolean isFondatore(Chef chef) {
		return chef != null && corso.getCodfiscaleFondatore() != null
				&& chef.getCodFiscale().equals(corso.getCodfiscaleFondatore());
	}

	private boolean isChefLoggato(Chef c) {
		Chef me = gestioneController.getChefLoggato();
		return me != null && c != null && me.getCodFiscale() != null && me.getCodFiscale().equals(c.getCodFiscale());
	}

	private boolean isCorsoFinito() {
		return corso.getDataFineCorso() != null && corso.getDataFineCorso().isBefore(LocalDateTime.now());
	}

	private boolean canDeleteCourse() {
		Chef chefLoggato = gestioneController.getChefLoggato();
		return chefLoggato != null && corso.getCodfiscaleFondatore() != null
				&& chefLoggato.getCodFiscale().equals(corso.getCodfiscaleFondatore());
	}

	private void applicaRestrizioniCorsoFinito() {
		if (isCorsoFinito()) {
			avisoCorsoFinitoLabel.setVisible(true);
			avisoCorsoFinitoLabel.setManaged(true);
			nomeField.setDisable(true);
			prezzoField.setDisable(true);
			argomentoField.setDisable(true);
			frequenzaCombo.setDisable(true);
			numeroPostiField.setDisable(true);
			dataInizioPicker.setDisable(true);
			dataFinePicker.setDisable(true);
			modificaBtn.setVisible(false);
			modificaBtn.setManaged(false);
			salvaBtn.setVisible(false);
			salvaBtn.setManaged(false);
			addChefCombo.setVisible(false);
			addChefCombo.setManaged(false);
			addChefBtn.setVisible(false);
			addChefBtn.setManaged(false);
			editable = false;
		}
		eliminaCorsoBtn.setDisable(!canDeleteCourse());
	}

	private void onDataInizioChange() {
		LocalDate dataInizio = dataInizioPicker.getValue();
		if (dataInizio == null)
			return;
		if (dataFinePicker.getValue() != null && dataFinePicker.getValue().isBefore(dataInizio)) {
			dataFinePicker.setValue(dataInizio.plusDays(1));
		}
		if (frequenzaCombo.getValue() == Frequenza.unica) {
			dataFinePicker.setValue(dataInizio);
		}
		aggiornaFrequenzeDisponibili();
	}

	private void onFrequenzaChange() {
		Frequenza nuovaFreq = frequenzaCombo.getValue();
		if (nuovaFreq == null)
			return;
		dataFinePicker.setDisable(nuovaFreq == Frequenza.unica);
		if (nuovaFreq == Frequenza.unica) {
			dataFinePicker.setValue(dataInizioPicker.getValue());
		}
	}

	private void aggiornaFrequenzeDisponibili() {
		LocalDate dataInizio = dataInizioPicker.getValue();
		LocalDate dataFine = dataFinePicker.getValue();
		if (dataInizio == null || dataFine == null) {
			frequenzaCombo.getItems().setAll(Frequenza.values());
			return;
		}
		List<Frequenza> frequenzeValide = new ArrayList<>();
		for (Frequenza freq : Frequenza.values()) {
			if (FrequenzaHelper.isFrequenzaValida(dataInizio, dataFine, freq)) {
				frequenzeValide.add(freq);
			}
		}
		Frequenza freqAttuale = frequenzaCombo.getValue();
		frequenzaCombo.getItems().setAll(frequenzeValide);
		if (frequenzeValide.contains(freqAttuale)) {
			frequenzaCombo.setValue(freqAttuale);
		} else if (!frequenzeValide.isEmpty()) {
			frequenzaCombo.setValue(frequenzeValide.get(0));
		}
	}

	private void aggiornaStatoDataFine() {
		if (corso.getFrequenzaCorso() == Frequenza.unica) {
			dataFinePicker.setDisable(!editable);
			if (!editable) {
				dataFinePicker.setStyle("-fx-opacity: 0.6;");
			}
		}
	}

	private void tornaAllaListaCorsi() {
		if (onChiudiCallback != null) {
			onChiudiCallback.run();
		} else {
			Stage stage = getStage(card);
			if (stage != null)
				stage.close();
		}
	}

	private void richiediEliminazione() {
		if (!canDeleteCourse()) {
			showStyledErrorDialog("🔒 Permessi Insufficienti",
					"Solo il fondatore del corso può eliminarlo.\n\n👑 Fondatore: " + getNomeFondatore());
			return;
		}
		StyleHelper.showConfirmationDialog("⚠️ Conferma Eliminazione",
				"Eliminare il corso '" + corso.getNomeCorso() + "'?\n\n⚠️ Questa azione è IRREVERSIBILE!",
				this::eliminaCorso);
	}

	private void eliminaCorso() {
		try {
			int corsoId = corso.getIdCorso();
			gestioneController.eliminaCorso(corsoId);
			showStyledSuccessDialog("✅ Successo", "Il corso è stato eliminato con successo!");
			tornaAllaListaCorsi();
		} catch (Exception ex) {
			showStyledErrorDialog("❌ Errore", "Errore nell'eliminazione del corso:\n" + ex.getMessage());
		}
	}

	private void salvaModifiche() {
		try {
			validateAndSave();
		} catch (ValidationException ex) {
			showStyledValidationDialog("⚠️ Errore di Validazione", ex.getMessage());
		} catch (DataAccessException ex) {
			showStyledErrorDialog("❌ Errore Database", ex.getMessage());
		}
	}

	private void validateAndSave() throws ValidationException {
		String nome = nomeField.getText().trim();
		if (nome.isEmpty())
			throw new ValidationException("Il campo 'Nome Corso' è obbligatorio.");

		String argomento = argomentoField.getText().trim();
		if (argomento.isEmpty())
			throw new ValidationException("Il campo 'Argomento' è obbligatorio.");

		double prezzo;
		try {
			prezzo = Double.parseDouble(prezzoField.getText().trim().replace(',', '.'));
			if (prezzo < 0)
				throw new ValidationException("Il prezzo non può essere negativo.");
		} catch (NumberFormatException e) {
			throw new ValidationException("Il prezzo deve essere un numero valido (es: 50.00).");
		}

		int posti;
		try {
			posti = Integer.parseInt(numeroPostiField.getText().trim());
			if (posti <= 0)
				throw new ValidationException("Il numero di posti deve essere > 0.");
		} catch (NumberFormatException e) {
			throw new ValidationException("Il numero di posti deve essere un numero intero.");
		}

		LocalDate dataInizio = dataInizioPicker.getValue();
		LocalDate dataFine = dataFinePicker.getValue();
		if (dataInizio != null && dataFine != null && dataInizio.isAfter(dataFine)) {
			throw new ValidationException("La data di inizio deve precedere la data di fine.");
		}

		Frequenza freq = frequenzaCombo.getValue();
		if (freq == null)
			throw new ValidationException("Selezionare una frequenza.");

		if (dataInizio != null && dataFine != null && !FrequenzaHelper.isFrequenzaValida(dataInizio, dataFine, freq)) {
			throw new ValidationException(FrequenzaHelper.getMessaggioErroreFrequenza(dataInizio, dataFine, freq));
		}

		int numSessioni = corso.getSessioni() != null ? corso.getSessioni().size() : 0;
		if (numSessioni > 0 && dataInizio != null && dataFine != null) {
			String errore = validaNumeroSessioni(freq, dataInizio, dataFine, numSessioni);
			if (errore != null)
				throw new ValidationException(errore);
		}

		corso.setNomeCorso(nome);
		corso.setPrezzo(prezzo);
		corso.setArgomento(argomento);
		corso.setFrequenzaCorso(freq);
		corso.setNumeroPosti(posti);
		if (dataInizio != null)
			corso.setDataInizioCorso(dataInizio.atStartOfDay());
		if (dataFine != null)
			corso.setDataFineCorso(dataFine.atStartOfDay());

		gestioneController.modificaCorso(corso);
		showStyledSuccessDialog("✅ Successo", "Il corso è stato modificato correttamente!");
		setEditable(false);
		salvaBtn.setDisable(true);
		modificaBtn.setDisable(false);
		refreshChefListView();
	}

	private String validaNumeroSessioni(Frequenza freq, LocalDate inizio, LocalDate fine, int numSessioni) {
		if (freq == null || inizio == null || fine == null)
			return null;
		switch (freq) {
		case unica:
			return numSessioni == 1 ? null : "Frequenza 'Sessione Unica' richiede esattamente 1 sessione.";
		case giornaliero:
			long giorni = java.time.temporal.ChronoUnit.DAYS.between(inizio, fine) + 1;
			return numSessioni >= giorni ? null : "Frequenza 'Giornaliero' richiede almeno " + giorni + " sessioni.";
		case settimanale:
			long settimane = java.time.temporal.ChronoUnit.WEEKS.between(inizio, fine) + 1;
			return numSessioni >= settimane ? null
					: "Frequenza 'Settimanale' richiede almeno " + settimane + " sessioni.";
		case mensile:
			long mesi = java.time.temporal.ChronoUnit.MONTHS.between(inizio.withDayOfMonth(1), fine.withDayOfMonth(1))
					+ 1;
			return numSessioni >= mesi ? null : "Frequenza 'Mensile' richiede almeno " + mesi + " sessioni.";
		default:
			return null;
		}
	}

	private void apriVisualizzaSessioni() {
		try {
			VisualizzaSessioniGUI visualizzaSessioniGUI = new VisualizzaSessioniGUI();
			visualizzaSessioniGUI.setCorso(corso);

			Stage sessioniStage = new Stage();
			sessioniStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
			sessioniStage.setTitle("📅 Gestione Sessioni - " + corso.getNomeCorso());

			javafx.scene.Scene scene = new javafx.scene.Scene(visualizzaSessioniGUI.getRoot(), 900, 700);
			sessioniStage.setScene(scene);

			sessioniStage.setOnHidden(e -> {
				int numSessioni = corso.getSessioni() != null ? corso.getSessioni().size() : 0;
				numeroSessioniField.setText(String.valueOf(numSessioni));
				aggiornaDataFineFromSessioni();
			});

			sessioniStage.showAndWait();
		} catch (Exception ex) {
			showStyledErrorDialog("❌ Errore", "Impossibile aprire la gestione sessioni:\n" + ex.getMessage());
		}
	}

	private void aggiornaDataFineFromSessioni() {
		if (corso.getSessioni() == null || corso.getSessioni().isEmpty())
			return;
		LocalDate maxDate = corso.getSessioni().stream().map(s -> s.getDataFineSessione().toLocalDate())
				.max(LocalDate::compareTo).orElse(null);
		if (maxDate != null && !maxDate.equals(dataFinePicker.getValue())) {
			dataFinePicker.setValue(maxDate);
		}
	}

	private void setEditable(boolean edit) {
		if (isCorsoFinito()) {
			this.editable = false;
			return;
		}
		this.editable = edit;
		LocalDate oggi = LocalDate.now();
		LocalDate dataInizio = corso.getDataInizioCorso() != null ? corso.getDataInizioCorso().toLocalDate() : null;
		LocalDate dataFine = corso.getDataFineCorso() != null ? corso.getDataFineCorso().toLocalDate() : null;

		boolean corsoGiaIniziato = dataInizio != null && dataInizio.isBefore(oggi);
		boolean corsoGiaFinito = dataFine != null && dataFine.isBefore(oggi);

		nomeField.setEditable(edit);
		prezzoField.setEditable(edit);
		argomentoField.setEditable(edit);
		numeroPostiField.setEditable(edit);

		if (!edit) {
			nomeField.setFocusTraversable(false);
			prezzoField.setFocusTraversable(false);
			argomentoField.setFocusTraversable(false);
			numeroPostiField.setFocusTraversable(false);
			nomeField.setMouseTransparent(true);
			prezzoField.setMouseTransparent(true);
			argomentoField.setMouseTransparent(true);
			numeroPostiField.setMouseTransparent(true);
		} else {
			nomeField.setFocusTraversable(true);
			prezzoField.setFocusTraversable(true);
			argomentoField.setFocusTraversable(true);
			numeroPostiField.setFocusTraversable(true);
			nomeField.setMouseTransparent(false);
			prezzoField.setMouseTransparent(false);
			argomentoField.setMouseTransparent(false);
			numeroPostiField.setMouseTransparent(false);
		}

		frequenzaCombo.setDisable(!edit);
		dataInizioPicker.setDisable(!edit || corsoGiaIniziato);

		if (edit && frequenzaCombo.getValue() == Frequenza.unica) {
			dataFinePicker.setDisable(true);
		} else {
			dataFinePicker.setDisable(!edit || corsoGiaFinito);
		}

		addChefCombo.setDisable(!edit);
		addChefBtn.setDisable(!edit);

		String textColor = "-fx-text-fill: black;";
		String bgColor = "-fx-background-color: white;";
		String opacity = edit ? "-fx-opacity: 1.0;" : "-fx-opacity: 0.7;";
		String borderColor = edit ? StyleHelper.PRIMARY_ORANGE : StyleHelper.BORDER_LIGHT;

		String fieldStyle = textColor + bgColor + opacity + "-fx-border-color: " + borderColor + ";"
				+ "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8;";

		nomeField.setStyle(fieldStyle);
		prezzoField.setStyle(fieldStyle);
		argomentoField.setStyle(fieldStyle);
		numeroPostiField.setStyle(fieldStyle);

		card.setStyle("-fx-background-color: white; -fx-background-radius: 16;"
				+ "-fx-border-radius: 16; -fx-border-color: " + borderColor + "; -fx-border-width: 2;");

		refreshChefListView();
	}

	private void refreshChefListView() {
		Platform.runLater(() -> {
			List<Chef> lista = corso.getChef() != null ? new ArrayList<>(corso.getChef()) : new ArrayList<>();
			lista.sort(
					Comparator.comparing((Chef ch) -> !isFondatore(ch)).thenComparing((Chef ch) -> !isChefLoggato(ch))
							.thenComparing(Chef::getCognome).thenComparing(Chef::getNome));

			chefListView.getItems().setAll(lista);

			try {
				List<Chef> all = gestioneController.getTuttiGliChef();
				List<Chef> avail = all.stream().filter(c -> !lista.contains(c)).collect(Collectors.toList());
				addChefCombo.getItems().setAll(avail);
				if (!avail.isEmpty())
					addChefCombo.setValue(avail.get(0));
			} catch (Exception ex) {
				addChefCombo.getItems().clear();
			}
		});
	}

	private void rimuoviChef(Chef chef) {
		if (!editable || isCorsoFinito())
			return;

		if (isChefLoggato(chef)) {
			showStyledValidationDialog("⚠️ Operazione Non Permessa",
					"Non puoi rimuovere te stesso dall'elenco del corso.");
			return;
		}

		if (isFondatore(chef)) {
			showStyledErrorDialog("❌ Operazione Non Permessa",
					"Non è possibile rimuovere il fondatore del corso.\n\n👑 " + chef.getNome() + " "
							+ chef.getCognome() + " ha creato questo corso.");
			return;
		}

		Chef chefLoggato = gestioneController.getChefLoggato();
		if (chefLoggato == null || corso.getCodfiscaleFondatore() == null
				|| !chefLoggato.getCodFiscale().equals(corso.getCodfiscaleFondatore())) {
			showStyledErrorDialog("🔒 Permessi Insufficienti",
					"Solo il fondatore del corso può rimuovere altri chef.\n\n" + "👑 Fondatore: "
							+ getNomeFondatore());
			return;
		}

		StyleHelper.showConfirmationDialog("Conferma Rimozione", "Rimuovere " + chef.getNome() + " " + chef.getCognome()
				+ " dal corso?\n\n" + "⚠️ Questa azione è irreversibile.", () -> {
					try {
						gestioneController.rimuoviChefDaCorso(corso, chef);
						corso.getChef().remove(chef);
						chefListView.getItems().remove(chef);
						refreshChefListView();
						showStyledSuccessDialog("✅ Chef Rimosso",
								chef.getNome() + " " + chef.getCognome() + " è stato rimosso con successo.");
					} catch (Exception ex) {
						showStyledErrorDialog("❌ Errore", "Errore durante la rimozione:\n" + ex.getMessage());
					}
				});
	}

	private void aggiungiChef(Chef chef, String password) {
		if (!editable || isCorsoFinito())
			return;

		if (corso.getChef() != null && corso.getChef().contains(chef)) {
			showStyledValidationDialog("⚠️ Chef già Presente",
					chef.getNome() + " " + chef.getCognome() + " è già assegnato a questo corso.");
			return;
		}

		try {
			gestioneController.aggiungiChefACorso(corso, chef, password);
			if (corso.getChef() == null)
				corso.setChef(new ArrayList<>());
			corso.getChef().add(chef);
			refreshChefListView();
			addChefCombo.setValue(null);
			showStyledSuccessDialog("✅ Chef Aggiunto",
					chef.getNome() + " " + chef.getCognome() + " è stato aggiunto con successo!");
		} catch (Exception ex) {
			showStyledErrorDialog("❌ Errore", "Errore durante l'aggiunta dello chef:\n" + ex.getMessage());
		}
	}

	private String safeString(String s) {
		return s == null ? "" : s;
	}

	private Stage getStage(Node node) {
		if (node == null)
			return null;
		javafx.scene.Scene s = node.getScene();
		if (s == null)
			return null;
		return (s.getWindow() instanceof Stage) ? (Stage) s.getWindow() : null;
	}

	private void showStyledValidationDialog(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(null);

		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStyleClass().remove("alert");

		dialogPane.setStyle("-fx-background-color: #FFF3CD;" + "-fx-border-color: #FFB84D;" + "-fx-border-width: 2px;"
				+ "-fx-border-radius: 10px;" + "-fx-background-radius: 10px;" + "-fx-padding: 25px;"
				+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);");

		VBox content = new VBox(15);
		content.setAlignment(Pos.CENTER);

		Label iconLabel = new Label("⚠️");
		iconLabel.setStyle("-fx-font-size: 48px;");

		Label titleLabel = new Label(title);
		titleLabel.setStyle(
				"-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #856404; -fx-wrap-text: true; -fx-text-alignment: center;");
		titleLabel.setMaxWidth(450);
		titleLabel.setWrapText(true);

		Label messageLabel = new Label(message);
		messageLabel.setStyle(
				"-fx-font-size: 14px; -fx-text-fill: #856404; -fx-wrap-text: true; -fx-text-alignment: center;");
		messageLabel.setMaxWidth(450);
		messageLabel.setWrapText(true);

		content.getChildren().addAll(iconLabel, titleLabel, messageLabel);
		dialogPane.setContent(content);

		Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
		okButton.setText("OK");
		okButton.setStyle(
				"-fx-background-color: #FF9966; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 40 12 40; -fx-background-radius: 8px; -fx-cursor: hand;");

		alert.showAndWait();
	}

	private void showStyledErrorDialog(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(null);

		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStyleClass().remove("alert");

		dialogPane.setStyle("-fx-background-color: #FFE5E5;" + "-fx-border-color: #FF6B6B;" + "-fx-border-width: 2px;"
				+ "-fx-border-radius: 10px;" + "-fx-background-radius: 10px;" + "-fx-padding: 25px;"
				+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);");

		VBox content = new VBox(15);
		content.setAlignment(Pos.CENTER);

		Label iconLabel = new Label("❌");
		iconLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: #FF6B6B;");

		Label titleLabel = new Label(title);
		titleLabel.setStyle(
				"-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #721c24; -fx-wrap-text: true; -fx-text-alignment: center;");
		titleLabel.setMaxWidth(450);
		titleLabel.setWrapText(true);

		Label messageLabel = new Label(message);
		messageLabel.setStyle(
				"-fx-font-size: 14px; -fx-text-fill: #721c24; -fx-wrap-text: true; -fx-text-alignment: center; -fx-line-spacing: 3px;");
		messageLabel.setMaxWidth(450);
		messageLabel.setWrapText(true);

		content.getChildren().addAll(iconLabel, titleLabel, messageLabel);
		dialogPane.setContent(content);

		Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
		okButton.setText("OK");
		okButton.setStyle(
				"-fx-background-color: #FF6B6B; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 40 12 40; -fx-background-radius: 8px; -fx-cursor: hand;");

		alert.showAndWait();
	}

	private void showStyledSuccessDialog(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(null);

		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStyleClass().remove("alert");

		dialogPane.setStyle("-fx-background-color: #D4EDDA;" + "-fx-border-color: #28A745;" + "-fx-border-width: 2px;"
				+ "-fx-border-radius: 10px;" + "-fx-background-radius: 10px;" + "-fx-padding: 25px;"
				+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);");

		VBox content = new VBox(15);
		content.setAlignment(Pos.CENTER);

		Label iconLabel = new Label("✅");
		iconLabel.setStyle("-fx-font-size: 48px;");

		Label titleLabel = new Label(title);
		titleLabel.setStyle(
				"-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #155724; -fx-wrap-text: true; -fx-text-alignment: center;");
		titleLabel.setMaxWidth(450);
		titleLabel.setWrapText(true);

		Label messageLabel = new Label(message);
		messageLabel.setStyle(
				"-fx-font-size: 14px; -fx-text-fill: #155724; -fx-wrap-text: true; -fx-text-alignment: center;");
		messageLabel.setMaxWidth(450);
		messageLabel.setWrapText(true);

		content.getChildren().addAll(iconLabel, titleLabel, messageLabel);
		dialogPane.setContent(content);

		Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
		okButton.setText("OK");
		okButton.setStyle(
				"-fx-background-color: #28A745; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 40 12 40; -fx-background-radius: 8px; -fx-cursor: hand;");

		alert.showAndWait();
	}
}