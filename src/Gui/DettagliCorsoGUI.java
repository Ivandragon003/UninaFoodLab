package Gui;

import dao.*;
import controller.*;
import model.*;
import exceptions.ValidationException;
import helper.StyleHelper;
import exceptions.DataAccessException;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DettagliCorsoGUI {
	private GestioneCorsoController gestioneController;
	private VisualizzaCorsiController visualizzaController;
	private ChefController chefController;
	private RicettaController ricettaController;
	private IngredienteController ingredienteController;
	private CorsoCucina corso;
	private VBox card;
	private boolean editable = false;
	private Runnable onChiudiCallback;
	private boolean hasUnsavedChanges = false;

	
	private TextField nomeField, prezzoField, argomentoField, numeroPostiField, numeroSessioniField;
	private ComboBox<Frequenza> frequenzaCombo;
	private DatePicker dataInizioPicker, dataFinePicker;
	private ListView<Chef> chefListView;
	private Button addChefBtn, modificaBtn, salvaBtn, eliminaCorsoBtn;
	private Label selezionatoLabel, avisoCorsoFinitoLabel, avisoSolaVisualizzazioneLabel;

	public void setVisualizzaController(VisualizzaCorsiController controller) {
		this.visualizzaController = controller;
	}
	
	public void setController(GestioneCorsoController controller) {
		this.gestioneController = controller;
	}

	public void setChefController(ChefController chefController) {
		this.chefController = chefController;
	}

	public void setCorso(CorsoCucina corso) {
		this.corso = corso;
	}

	public void setOnChiudiCallback(Runnable callback) {
		this.onChiudiCallback = callback;
	}

	public void setRicettaController(RicettaController ricettaController) {
		this.ricettaController = ricettaController;
	}

	public void setIngredienteController(IngredienteController ingredienteController) {
		this.ingredienteController = ingredienteController;
	}


	public StackPane getRoot() {
		if (gestioneController == null || corso == null) {
			throw new IllegalStateException("Controller o corso non impostati!");
		}

		StackPane mainContainer = new StackPane();
		mainContainer.setMinSize(400, 400);

		Region background = new Region();
		StyleHelper.applyBackgroundGradient(background);
		background.prefWidthProperty().bind(mainContainer.widthProperty());
		background.prefHeightProperty().bind(mainContainer.heightProperty());
		mainContainer.getChildren().add(background);

		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setFitToWidth(true);
		scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
		scrollPane.setPadding(new Insets(30));

		card = createMainCard();

		VBox wrapper = new VBox(card);
		wrapper.setAlignment(Pos.TOP_CENTER);
		wrapper.setPadding(new Insets(30));
		wrapper.setStyle("-fx-background-color: transparent;");
		scrollPane.setContent(wrapper);

		mainContainer.getChildren().add(scrollPane);
		return mainContainer;
	}

	private VBox createMainCard() {
		VBox card = new VBox(18);
		card.setAlignment(Pos.TOP_CENTER);
		card.setPadding(new Insets(30));
		card.setMaxWidth(900);
		card.setStyle("-fx-background-color: white;" + "-fx-background-radius: 20;" + "-fx-border-color: "
				+ StyleHelper.PRIMARY_ORANGE + ";" + "-fx-border-width: 2;" + "-fx-border-radius: 20");

		Label title = StyleHelper.createTitleLabel("📋 Dettagli Corso");
		Label fondatoreLabel = createFondatoreLabel();

		createAvisoLabels();
		createFormFields();
		createChefComponents();

		GridPane grid = createFormGrid();
		HBox addBox = new HBox(10, addChefBtn);
		addBox.setAlignment(Pos.CENTER_LEFT);
		HBox buttons = createButtonsBox();

		card.getChildren().addAll(title, fondatoreLabel, avisoCorsoFinitoLabel, avisoSolaVisualizzazioneLabel,
				new Separator(), grid, new Separator(), StyleHelper.createLabel("👥 Chef assegnati al corso:"),
				chefListView, selezionatoLabel, StyleHelper.createLabel("➕ Aggiungi uno chef dal sistema:"), addBox,
				buttons);

		setEditable(false);
		applicaRestrizioniPermessi();
		refreshChefListView();

		return card;
	}


	private void createAvisoLabels() {
		avisoCorsoFinitoLabel = createAvisoLabel("⚠️ CORSO TERMINATO - Solo visualizzazione", "#FFF3CD", "#856404",
				"#FFEAA7");

		avisoSolaVisualizzazioneLabel = createAvisoLabel("👁️ NON SEI PARTECIPANTE - Solo visualizzazione", "#E3F2FD",
				"#1565C0", "#90CAF9");
	}

	private Label createAvisoLabel(String text, String bgColor, String textColor, String borderColor) {
		Label label = new Label(text);
		label.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
		label.setStyle("-fx-background-color: " + bgColor + ";" + "-fx-text-fill: " + textColor + ";"
				+ "-fx-padding: 12;" + "-fx-background-radius: 8;" + "-fx-border-color: " + borderColor + ";"
				+ "-fx-border-width: 2;" + "-fx-border-radius: 8;");
		label.setVisible(false);
		label.setManaged(false);
		return label;
	}

	private void createFormFields() {
		nomeField = createReadOnlyTextField(corso.getNomeCorso());
		prezzoField = createReadOnlyTextField(String.valueOf(corso.getPrezzo()));
		argomentoField = createReadOnlyTextField(corso.getArgomento());
		numeroPostiField = createReadOnlyTextField(String.valueOf(corso.getNumeroPosti()));

		frequenzaCombo = StyleHelper.createComboBox();
		frequenzaCombo.getItems().setAll(Frequenza.values());
		frequenzaCombo.setValue(corso.getFrequenzaCorso());
		frequenzaCombo.setDisable(true);

		numeroSessioniField = createDisabledTextField(
				corso.getSessioni() != null ? String.valueOf(corso.getSessioni().size()) : "0");

		dataInizioPicker = createDatePicker(corso.getDataInizioCorso());
		dataFinePicker = createDatePicker(corso.getDataFineCorso());
	}

	private TextField createReadOnlyTextField(String value) {
		TextField field = new TextField(safeString(value));
		field.setStyle("-fx-text-fill: #000000;" + "-fx-background-color: white;" + "-fx-border-color: "
				+ StyleHelper.BORDER_LIGHT + ";" + "-fx-border-width: 2;" + "-fx-border-radius: 12;"
				+ "-fx-background-radius: 12;" + "-fx-padding: 10 15;" + "-fx-font-size: 14px;");
		field.setEditable(false);
		field.setFocusTraversable(false);
		return field;
	}

	private TextField createDisabledTextField(String value) {
		TextField field = new TextField(value);
		field.setEditable(false);
		field.setFocusTraversable(false);
		field.setMouseTransparent(true);
		field.setStyle("-fx-text-fill: #000000;" + "-fx-control-inner-background: #E9ECEF;" + "-fx-border-color: "
				+ StyleHelper.BORDER_LIGHT + ";" + "-fx-border-width: 2;" + "-fx-border-radius: 12;"
				+ "-fx-background-radius: 12;" + "-fx-padding: 10 15;" + "-fx-font-size: 14px;");
		return field;
	}

	private DatePicker createDatePicker(LocalDateTime dateTime) {
		DatePicker picker = StyleHelper.createDatePicker();
		picker.setValue(dateTime != null ? dateTime.toLocalDate() : null);
		picker.setDisable(true);
		picker.setEditable(false);
		picker.setMouseTransparent(true);
		picker.setStyle("-fx-opacity: 1.0; -fx-control-inner-background: #E9ECEF;");
		return picker;
	}

	private void createChefComponents() {
		chefListView = createChefListView();
		selezionatoLabel = new Label("Selezionato: nessuno");
		selezionatoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");

		addChefBtn = StyleHelper.createSuccessButton("➕ Seleziona e Aggiungi Chef");
		addChefBtn.setDisable(true);
		addChefBtn.setOnAction(e -> apriDialogSelezionaChef());

		chefListView.getSelectionModel().selectedItemProperty()
				.addListener((obs, oldV, newV) -> updateSelezionatoLabel(newV));
	}

	private void updateSelezionatoLabel(Chef chef) {
		if (chef == null) {
			selezionatoLabel.setText("Selezionato: nessuno");
		} else {
			String suffix = "";
			if (isFondatore(chef))
				suffix += " 👑";
			if (isChefLoggato(chef))
				suffix += " (io)";
			selezionatoLabel.setText("Selezionato: " + chef.getNome() + " " + chef.getCognome() + suffix);
		}
	}

	private ListView<Chef> createChefListView() {
		ListView<Chef> list = new ListView<>();
		list.setPrefHeight(150);
		list.setMinHeight(100);
		list.setMaxHeight(250);
		list.setStyle("-fx-background-color: white;" + "-fx-border-color: " + StyleHelper.BORDER_LIGHT + ";"
				+ "-fx-border-radius: 8;" + "-fx-background-radius: 8;" + "-fx-border-width: 1;");
		list.setCellFactory(lv -> new ChefCell());
		return list;
	}

	private class ChefCell extends ListCell<Chef> {
		private final HBox box = new HBox(8);
		private final Label nameLabel = new Label();
		private final Label meLabel = new Label(" (io)");
		private final Label foundLabel = new Label(" 👑");
		private final Button removeBtn = new Button("🗑️ Rimuovi");

		{
			box.setAlignment(Pos.CENTER_LEFT);
			meLabel.setStyle("-fx-text-fill: " + StyleHelper.PRIMARY_ORANGE + "; -fx-font-weight: bold;");
			foundLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 16px;");
			removeBtn.setStyle("-fx-background-radius: 8;" + "-fx-background-color: " + StyleHelper.ERROR_RED + ";"
					+ "-fx-text-fill: white;" + "-fx-cursor: hand;" + "-fx-font-size: 11px;" + "-fx-padding: 4 8 4 8;");
			removeBtn.setOnAction(e -> rimuoviChef(getItem()));
		}

		@Override
		protected void updateItem(Chef chef, boolean empty) {
			super.updateItem(chef, empty);
			if (empty || chef == null) {
				setGraphic(null);
				return;
			}

			nameLabel.setText(chef.getNome() + " " + chef.getCognome());
			box.getChildren().clear();
			box.getChildren().add(nameLabel);

			if (isFondatore(chef))
				box.getChildren().add(foundLabel);
			if (isChefLoggato(chef))
				box.getChildren().add(meLabel);

			if (canModifyChef()) {
				removeBtn.setDisable(isChefLoggato(chef));
				box.getChildren().add(removeBtn);
			}

			setGraphic(box);
		}
	}

	private GridPane createFormGrid() {
		GridPane grid = new GridPane();
		grid.setHgap(15);
		grid.setVgap(15);
		grid.setAlignment(Pos.CENTER);

		addFieldToGrid(grid, "📚 Nome:", nomeField, 0);
		addFieldToGrid(grid, "💰 Prezzo (€):", prezzoField, 1);
		addFieldToGrid(grid, "📖 Argomento:", argomentoField, 2);
		addFieldToGrid(grid, "📅 Frequenza:", frequenzaCombo, 3);
		addFieldToGrid(grid, "🪑 Numero posti:", numeroPostiField, 4);
		addFieldToGrid(grid, "⏰ Numero sessioni:", numeroSessioniField, 5);
		addFieldToGrid(grid, "🕑 Data inizio:", dataInizioPicker, 6);
		addFieldToGrid(grid, "🏁 Data fine:", dataFinePicker, 7);

		ColumnConstraints c0 = new ColumnConstraints(150);
		ColumnConstraints c1 = new ColumnConstraints();
		c1.setHgrow(Priority.ALWAYS);
		c1.setMinWidth(350);
		grid.getColumnConstraints().addAll(c0, c1);
		return grid;
	}

	private void addFieldToGrid(GridPane grid, String labelText, Control control, int row) {
		grid.add(StyleHelper.createLabel(labelText), 0, row);
		grid.add(control, 1, row);
	}

	private HBox createButtonsBox() {
		HBox buttons = new HBox(15);
		buttons.setAlignment(Pos.CENTER);
		buttons.setPadding(new Insets(20, 0, 0, 0));

		modificaBtn = StyleHelper.createInfoButton("✏️ Modifica");
		modificaBtn.setPrefWidth(140);
		modificaBtn.setOnAction(e -> attivaModalitaModifica());

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
		chiudiBtn.setOnAction(e -> gestisciChiusura());

		buttons.getChildren().addAll(modificaBtn, salvaBtn, visualizzaSessioniBtn, eliminaCorsoBtn, chiudiBtn);
		return buttons;
	}


	private boolean isCorsoFinito() {
		return corso.getDataFineCorso() != null && corso.getDataFineCorso().isBefore(LocalDateTime.now());
	}

	private boolean isChefPartecipante() {
		Chef chefLoggato = gestioneController.getChefLoggato();
		return chefLoggato != null && corso.getChef() != null && corso.getChef().stream()
				.anyMatch(c -> c.getCodFiscale() != null && c.getCodFiscale().equals(chefLoggato.getCodFiscale()));
	}

	private boolean isFondatore(Chef chef) {
		return chef != null && corso.getCodfiscaleFondatore() != null
				&& chef.getCodFiscale().equals(corso.getCodfiscaleFondatore());
	}

	private boolean isChefLoggato(Chef c) {
		Chef me = gestioneController.getChefLoggato();
		return me != null && c != null && me.getCodFiscale() != null && me.getCodFiscale().equals(c.getCodFiscale());
	}

	private boolean canDeleteCourse() {
		Chef chefLoggato = gestioneController.getChefLoggato();
		return chefLoggato != null && corso.getCodfiscaleFondatore() != null
				&& chefLoggato.getCodFiscale().equals(corso.getCodfiscaleFondatore());
	}

	private boolean canModifyChef() {
		return editable && !isCorsoFinito() && isChefPartecipante();
	}

	private void applicaRestrizioniPermessi() {
		boolean corsoFinito = isCorsoFinito();
		boolean sonoPartecipante = isChefPartecipante();
		boolean sonoFondatore = canDeleteCourse();

		if (corsoFinito) {
			mostraAviso(avisoCorsoFinitoLabel);
			disabilitaModifiche();
		} else if (!sonoPartecipante) {
			mostraAviso(avisoSolaVisualizzazioneLabel);
			disabilitaModifiche();
		} else {
			nascondiAvisi();
		}

		eliminaCorsoBtn.setDisable(!sonoFondatore);
		if (!sonoFondatore) {
			eliminaCorsoBtn.setVisible(false);
			eliminaCorsoBtn.setManaged(false);
		}
	}

	private void mostraAviso(Label aviso) {
		avisoCorsoFinitoLabel.setVisible(false);
		avisoCorsoFinitoLabel.setManaged(false);
		avisoSolaVisualizzazioneLabel.setVisible(false);
		avisoSolaVisualizzazioneLabel.setManaged(false);

		aviso.setVisible(true);
		aviso.setManaged(true);
	}

	private void nascondiAvisi() {
		avisoCorsoFinitoLabel.setVisible(false);
		avisoCorsoFinitoLabel.setManaged(false);
		avisoSolaVisualizzazioneLabel.setVisible(false);
		avisoSolaVisualizzazioneLabel.setManaged(false);
	}

	private void disabilitaModifiche() {
		modificaBtn.setVisible(false);
		modificaBtn.setManaged(false);
		salvaBtn.setVisible(false);
		salvaBtn.setManaged(false);
		addChefBtn.setVisible(false);
		addChefBtn.setManaged(false);
		editable = false;
	}


	private void attivaModalitaModifica() {
		setEditable(true);
		salvaBtn.setDisable(false);
		modificaBtn.setDisable(true);
	}

	private void salvaModifiche() {
		try {
			corso.setNomeCorso(nomeField.getText());
			corso.setPrezzo(Double.parseDouble(prezzoField.getText().replace(',', '.')));
			corso.setArgomento(argomentoField.getText());
			corso.setNumeroPosti(Integer.parseInt(numeroPostiField.getText()));

			gestioneController.modificaCorso(corso);

			StyleHelper.showSuccessDialog("✅ Successo", "Corso modificato correttamente!");
			finalizzaSalvataggio();

		} catch (NumberFormatException e) {
			StyleHelper.showValidationDialog("Errore", "Inserisci valori numerici validi per prezzo e posti");
		} catch (IllegalArgumentException e) {
			StyleHelper.showValidationDialog("Errore", e.getMessage());
		} catch (ValidationException e) {
			StyleHelper.showValidationDialog("Errore", e.getMessage());
		} catch (DataAccessException e) {
			StyleHelper.showErrorDialog("Errore Database", e.getMessage());
		}
	}

	private void finalizzaSalvataggio() {
		hasUnsavedChanges = false;
		setEditable(false);
		salvaBtn.setDisable(true);
		modificaBtn.setDisable(false);
		refreshChefListView();
	}

	private void gestisciChiusura() {
		if (editable && hasUnsavedChanges) {
			mostraDialogModificheNonSalvate();
		} else {
			tornaAllaListaCorsi();
		}
	}

	private void mostraDialogModificheNonSalvate() {
		StyleHelper.showUnsavedChangesDialog("⚠️ Modifiche Non Salvate",
				"Hai effettuato modifiche non salvate.\n\nCosa desideri fare?", () -> {
					salvaModifiche();
					if (!hasUnsavedChanges)
						tornaAllaListaCorsi();
				}, this::tornaAllaListaCorsi);
	}

	private void richiediEliminazione() {
		if (!canDeleteCourse()) {
			StyleHelper.showErrorDialog("🔒 Permessi Insufficienti",
					"Solo il fondatore può eliminare il corso.\n\n👑 Fondatore: " + getNomeFondatore());
			return;
		}

		StyleHelper.showConfirmationDialog("⚠️ Conferma Eliminazione",
				"Eliminare '" + corso.getNomeCorso() + "'?\n\n⚠️ IRREVERSIBILE!", this::eliminaCorso);
	}

	private void eliminaCorso() {
		gestioneController.eliminaCorso(corso.getIdCorso());
		StyleHelper.showSuccessDialog("✅ Successo", "Corso eliminato!");
		tornaAllaListaCorsi();
	}


	private void apriDialogSelezionaChef() {
		if (!canModifyChef())
			return;

		if (chefController == null) {
			StyleHelper.showErrorDialog("Errore", "ChefController non inizializzato");
			return;
		}

		Chef chefSelezionato = new SelezionaChefDialog(chefController).showAndReturn();
		if (chefSelezionato != null) {
			aggiungiChef(chefSelezionato);
		}
	}

	private void aggiungiChef(Chef chef) {
		if (!canModifyChef())
			return;

		if (corso.getChef() != null && corso.getChef().contains(chef)) {
			StyleHelper.showValidationDialog("⚠️ Chef già Presente",
					chef.getNome() + " " + chef.getCognome() + " è già assegnato");
			return;
		}

		try {
			gestioneController.aggiungiChefACorso(corso, chef, null);
			refreshChefListView();
			StyleHelper.showSuccessDialog("✅ Chef Aggiunto",
					chef.getNome() + " " + chef.getCognome() + " aggiunto con successo!");
		} catch (ValidationException | DataAccessException ex) {
			StyleHelper.showErrorDialog("Errore", ex.getMessage());
		}
	}

	private void rimuoviChef(Chef chef) {
		if (!canModifyChef() || chef == null)
			return;

		if (isChefLoggato(chef)) {
			StyleHelper.showValidationDialog("⚠️ Operazione Non Permessa", "Non puoi rimuovere te stesso");
			return;
		}

		if (isFondatore(chef)) {
			StyleHelper.showErrorDialog("❌ Operazione Non Permessa", "Non puoi rimuovere il fondatore");
			return;
		}

		if (!canDeleteCourse()) {
			StyleHelper.showErrorDialog("🔒 Permessi Insufficienti", "Solo il fondatore può rimuovere chef");
			return;
		}

		StyleHelper.showCustomConfirmationDialog("Rimuovi Chef",
				"Rimuovere " + chef.getNome() + " " + chef.getCognome() + "?\n\n⚠️ Irreversibile",
				() -> eseguiRimozioneChef(chef));
	}

	private void eseguiRimozioneChef(Chef chef) {
		try {
			gestioneController.rimuoviChefDaCorso(corso, chef);
			corso.getChef().remove(chef);
			refreshChefListView();
			StyleHelper.showSuccessDialog("✅ Chef Rimosso", chef.getNome() + " " + chef.getCognome() + " rimosso");
		} catch (ValidationException | DataAccessException ex) {
			StyleHelper.showErrorDialog("Errore", ex.getMessage());
		}
	}


	private void apriVisualizzaSessioni() {
		if (ricettaController == null || ingredienteController == null) {
			StyleHelper.showErrorDialog("Errore", "Controller non inizializzati");
			return;
		}

		try {
			GestioneSessioniController sessioniController = creaSessioniController();

			VisualizzaSessioniGUI visualizzaSessioniGUI = new VisualizzaSessioniGUI();
			visualizzaSessioniGUI.setCorso(corso);
			visualizzaSessioniGUI.setController(sessioniController);
			visualizzaSessioniGUI.setRicettaController(ricettaController);
			visualizzaSessioniGUI.setIngredienteController(ingredienteController);
			visualizzaSessioniGUI.setGestioneCorsoController(gestioneController);

			StackPane contentPane = trovaContentPane();
			if (contentPane == null) {
				StyleHelper.showErrorDialog("Errore", "Impossibile trovare contenitore principale");
				return;
			}

			visualizzaSessioniGUI.setContentPane(contentPane);
			visualizzaSessioniGUI.setOnChiudiCallback(() -> tornaADettagliDaSessioni(contentPane));

			contentPane.getChildren().setAll(visualizzaSessioniGUI.getRoot());

		} catch (Exception ex) {
			StyleHelper.showErrorDialog("Errore", "Impossibile inizializzare gestione sessioni: " + ex.getMessage());
		}
	}

	private GestioneSessioniController creaSessioniController() {
		CucinaDAO cucinaDAO = new CucinaDAO();
		InPresenzaDAO inPresenzaDAO = new InPresenzaDAO(cucinaDAO);
		OnlineDAO onlineDAO = new OnlineDAO();
		RicettaDAO ricettaDAO = new RicettaDAO();
		return new GestioneSessioniController(corso, inPresenzaDAO, onlineDAO, cucinaDAO, ricettaDAO);

	}

	private void tornaADettagliDaSessioni(StackPane contentPane) {
		try {
			CorsoCucina corsoAggiornato = visualizzaController != null 
					? visualizzaController.getCorsoCompleto(corso.getIdCorso())
					: corso;
			DettagliCorsoGUI dettagliGUI = new DettagliCorsoGUI();
			dettagliGUI.setController(gestioneController);
			dettagliGUI.setChefController(chefController);
			dettagliGUI.setRicettaController(ricettaController);
			dettagliGUI.setIngredienteController(ingredienteController);
			dettagliGUI.setCorso(corsoAggiornato);
			dettagliGUI.setOnChiudiCallback(onChiudiCallback);

			contentPane.getChildren().setAll(dettagliGUI.getRoot());

		} catch (Exception ex) {
			StyleHelper.showErrorDialog("Errore", "Impossibile ricaricare dettagli corso");
		}
	}

	private StackPane trovaContentPane() {
		try {
			if (card != null && card.getScene() != null) {
				javafx.scene.Parent sceneRoot = card.getScene().getRoot();

				if (sceneRoot instanceof StackPane) {
					StackPane mainContainer = (StackPane) sceneRoot;

					for (javafx.scene.Node node : mainContainer.getChildren()) {
						if (node instanceof HBox) {
							HBox mainLayout = (HBox) node;

							if (mainLayout.getChildren().size() > 1) {
								javafx.scene.Node possibleContentPane = mainLayout.getChildren().get(1);

								if (possibleContentPane instanceof StackPane) {
									return (StackPane) possibleContentPane;
								}
							}
						}
					}
				}
			}
		} catch (Exception ex) {
		}

		return null;
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


	private void setEditable(boolean edit) {
		if (isCorsoFinito() || !isChefPartecipante()) {
			this.editable = false;
			return;
		}

		this.editable = edit;

		if (edit) {
			addChangeListeners();
		} else {
			hasUnsavedChanges = false;
		}

		applicaEditabileCampi(edit);
		addChefBtn.setDisable(!edit);
		refreshChefListView();
	}

	private void applicaEditabileCampi(boolean edit) {
		TextField[] fields = { nomeField, prezzoField, argomentoField, numeroPostiField };

		for (TextField field : fields) {
			field.setEditable(edit);
			field.setFocusTraversable(edit);
			field.setMouseTransparent(!edit);
		}

		frequenzaCombo.setDisable(true);
		dataInizioPicker.setDisable(true);
		dataFinePicker.setDisable(true);

		String borderColor = edit ? StyleHelper.PRIMARY_ORANGE : StyleHelper.BORDER_LIGHT;
		String fieldStyle = "-fx-text-fill: " + StyleHelper.TEXT_BLACK + ";" + "-fx-background-color: white;"
				+ "-fx-opacity: 1.0;" + "-fx-border-color: " + borderColor + ";" + "-fx-border-width: 2;"
				+ "-fx-border-radius: 12;" + "-fx-background-radius: 12;" + "-fx-padding: 10 15;"
				+ "-fx-font-size: 14px;";

		for (TextField field : fields) {
			field.setStyle(fieldStyle);
		}
	}

	private void addChangeListeners() {
		nomeField.textProperty().addListener((obs, old, newVal) -> hasUnsavedChanges = true);
		prezzoField.textProperty().addListener((obs, old, newVal) -> hasUnsavedChanges = true);
		argomentoField.textProperty().addListener((obs, old, newVal) -> hasUnsavedChanges = true);
		numeroPostiField.textProperty().addListener((obs, old, newVal) -> hasUnsavedChanges = true);
	}

	private void refreshChefListView() {
		Platform.runLater(() -> {
			List<Chef> lista = corso.getChef() != null ? new ArrayList<>(corso.getChef()) : new ArrayList<>();

			lista.sort(
					Comparator.comparing((Chef ch) -> !isFondatore(ch)).thenComparing((Chef ch) -> !isChefLoggato(ch))
							.thenComparing(Chef::getCognome).thenComparing(Chef::getNome));

			chefListView.getItems().setAll(lista);
		});
	}

	private Label createFondatoreLabel() {
		boolean sonoIlFondatore = canDeleteCourse();

		Label label = new Label(
				sonoIlFondatore ? "👑 Sei il fondatore di questo corso" : "👤 Fondatore: " + getNomeFondatore());
		label.setMaxWidth(Double.MAX_VALUE);
		label.setAlignment(Pos.CENTER);
		label.setStyle(sonoIlFondatore
				? "-fx-background-color: linear-gradient(to right, #FFD700, #FFA500);"
						+ "-fx-text-fill: #4B2E2E; -fx-padding: 12; -fx-background-radius: 10;"
						+ "-fx-font-weight: bold; -fx-font-size: 14px;"
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
}