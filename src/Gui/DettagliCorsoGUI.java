package Gui;

import dao.InPresenzaDAO;
import dao.OnlineDAO;
import dao.CucinaDAO;
import service.GestioneCucina;
import service.GestioneRicette;
import service.GestioneSessioni;
import controller.GestioneSessioniController;
import controller.GestioneCorsoController;
import controller.IngredienteController;
import controller.RicettaController;
import controller.ChefController;
import model.CorsoCucina;
import model.Frequenza;
import model.Chef;
import guihelper.StyleHelper;
import exceptions.ValidationException;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DettagliCorsoGUI {
    private GestioneCorsoController gestioneController;
    private ChefController chefController;
    private RicettaController ricettaController;
    private IngredienteController ingredienteController;
    private CorsoCucina corso;
    private VBox card;
    private boolean editable = false;
    private Runnable onChiudiCallback;

    private boolean hasUnsavedChanges = false;
    
    private boolean isUpdating = false;

    private TextField nomeField, prezzoField, argomentoField, numeroPostiField, numeroSessioniField;
    private ComboBox<Frequenza> frequenzaCombo;
    private DatePicker dataInizioPicker, dataFinePicker;
    private ListView<Chef> chefListView;
    private Button addChefBtn, modificaBtn, salvaBtn, eliminaCorsoBtn;
    private Label selezionatoLabel, avisoCorsoFinitoLabel;

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

        card = new VBox(18);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(30));
        card.setMaxWidth(900);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: " + StyleHelper.PRIMARY_ORANGE + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 20"
        );

        Label title = StyleHelper.createTitleLabel("📋 Dettagli Corso");

        Label fondatoreLabel = createFondatoreLabel();

        avisoCorsoFinitoLabel = new Label("⚠️ CORSO TERMINATO - Solo visualizzazione");
        avisoCorsoFinitoLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
        avisoCorsoFinitoLabel.setStyle(
            "-fx-background-color: #FFF3CD;" +
            "-fx-text-fill: #856404;" +
            "-fx-padding: 12;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: #FFEAA7;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 8;"
        );
        avisoCorsoFinitoLabel.setVisible(false);
        avisoCorsoFinitoLabel.setManaged(false);

        createFormFields();

        chefListView = createChefListView();
        selezionatoLabel = new Label("Selezionato: nessuno");
        selezionatoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");

        createChefSelection();

        GridPane grid = createFormGrid();
        HBox addBox = new HBox(10, addChefBtn);
        addBox.setAlignment(Pos.CENTER_LEFT);

        HBox buttons = createButtonsBox();

        card.getChildren().addAll(
            title,
            fondatoreLabel,
            avisoCorsoFinitoLabel,
            new Separator(),
            grid,
            new Separator(),
            StyleHelper.createLabel("👥 Chef assegnati al corso:"),
            chefListView,
            selezionatoLabel,
            StyleHelper.createLabel("➕ Aggiungi uno chef dal sistema:"),
            addBox,
            buttons
        );

        setEditable(false);
        applicaRestrizioniCorsoFinito();
        refreshChefListView();

        VBox wrapper = new VBox(card);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setPadding(new Insets(30));
        wrapper.setStyle("-fx-background-color: transparent;");
        scrollPane.setContent(wrapper);

        mainContainer.getChildren().add(scrollPane);
        return mainContainer;
    }

    private void createFormFields() {
        nomeField = new TextField(safeString(corso.getNomeCorso()));
        prezzoField = new TextField(String.valueOf(corso.getPrezzo()));
        argomentoField = new TextField(safeString(corso.getArgomento()));
        numeroPostiField = new TextField(String.valueOf(corso.getNumeroPosti()));

        String readOnlyStyle = 
            "-fx-text-fill: #000000;" +
            "-fx-background-color: white;" +
            "-fx-border-color: " + StyleHelper.BORDER_LIGHT + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 10 15;" +
            "-fx-font-size: 14px;";

        nomeField.setStyle(readOnlyStyle);
        prezzoField.setStyle(readOnlyStyle);
        argomentoField.setStyle(readOnlyStyle);
        numeroPostiField.setStyle(readOnlyStyle);

        nomeField.setEditable(false);
        prezzoField.setEditable(false);
        argomentoField.setEditable(false);
        numeroPostiField.setEditable(false);

        nomeField.setFocusTraversable(false);
        prezzoField.setFocusTraversable(false);
        argomentoField.setFocusTraversable(false);
        numeroPostiField.setFocusTraversable(false);

        frequenzaCombo = StyleHelper.createComboBox();
        frequenzaCombo.getItems().setAll(Frequenza.values());
        frequenzaCombo.setValue(corso.getFrequenzaCorso());
        frequenzaCombo.setDisable(true);

        numeroSessioniField = new TextField(
            corso.getSessioni() != null ? String.valueOf(corso.getSessioni().size()) : "0"
        );
        numeroSessioniField.setEditable(false);
        numeroSessioniField.setFocusTraversable(false);
        numeroSessioniField.setMouseTransparent(true);
        numeroSessioniField.setStyle(
            "-fx-text-fill: #000000;" +
            "-fx-control-inner-background: #E9ECEF;" +
            "-fx-border-color: " + StyleHelper.BORDER_LIGHT + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 10 15;" +
            "-fx-font-size: 14px;"
        );

        // Data inizio: sempre non modificabile
        dataInizioPicker = StyleHelper.createDatePicker();
        dataInizioPicker.setValue(corso.getDataInizioCorso() != null ? corso.getDataInizioCorso().toLocalDate() : null);
        dataInizioPicker.setDisable(true);
        dataInizioPicker.setEditable(false);
        dataInizioPicker.setMouseTransparent(true);
        dataInizioPicker.setStyle("-fx-opacity: 1.0;" + "-fx-control-inner-background: #E9ECEF;");

        // Data fine: automatica (solo lettura)
        dataFinePicker = StyleHelper.createDatePicker();
        dataFinePicker.setValue(corso.getDataFineCorso() != null ? corso.getDataFineCorso().toLocalDate() : null);
        dataFinePicker.setDisable(true);
        dataFinePicker.setEditable(false);
        dataFinePicker.setMouseTransparent(true);
        dataFinePicker.setStyle("-fx-opacity: 1.0;" + "-fx-control-inner-background: #E9ECEF;");
    }

    private ListView<Chef> createChefListView() {
        ListView<Chef> list = new ListView<>();
        list.setPrefHeight(150);
        list.setMinHeight(100);
        list.setMaxHeight(250);
        list.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: " + StyleHelper.BORDER_LIGHT + ";" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-border-width: 1;"
        );

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
                removeBtn.setStyle(
                    "-fx-background-radius: 8;" +
                    "-fx-background-color: " + StyleHelper.ERROR_RED + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-cursor: hand;" +
                    "-fx-font-size: 11px;" +
                    "-fx-padding: 4 8 4 8;"
                );
                removeBtn.setOnAction(e -> {
                    Chef item = getItem();
                    if (item != null) rimuoviChef(item);
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
                    if (isFondatore(item)) box.getChildren().add(foundLabel);
                    if (isChefLoggato(item)) box.getChildren().add(meLabel);
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
        addChefBtn = StyleHelper.createSuccessButton("➕ Seleziona e Aggiungi Chef");
        addChefBtn.setDisable(true);
        addChefBtn.setOnAction(e -> apriDialogSelezionaChef());

        chefListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                selezionatoLabel.setText("Selezionato: nessuno");
            } else {
                String suffix = "";
                if (isFondatore(newV)) suffix += " 👑";
                if (isChefLoggato(newV)) suffix += " (io)";
                selezionatoLabel.setText("Selezionato: " + newV.getNome() + " " + newV.getCognome() + suffix);
            }
        });
    }

    private void apriDialogSelezionaChef() {
        if (!editable || isCorsoFinito()) return;

        if (chefController == null) {
            StyleHelper.showErrorDialog("Errore", "ChefController non inizializzato");
            return;
        }

        SelezionaChefDialog dialog = new SelezionaChefDialog(chefController);
        Chef chefSelezionato = dialog.showAndReturn();

        if (chefSelezionato != null) {
            aggiungiChef(chefSelezionato, null);
        }
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
        chiudiBtn.setOnAction(e -> {
            if (editable && hasUnsavedChanges) {
                mostraDialogModificheNonSalvate();
            } else {
                tornaAllaListaCorsi();
            }
        });
        
        buttons.getChildren().addAll(modificaBtn, salvaBtn, visualizzaSessioniBtn, eliminaCorsoBtn, chiudiBtn);
        return buttons;
    }

    private Label createFondatoreLabel() {
        Chef chefLoggato = gestioneController.getChefLoggato();
        boolean sonoIlFondatore = chefLoggato != null && corso.getCodfiscaleFondatore() != null
                && chefLoggato.getCodFiscale().equals(corso.getCodfiscaleFondatore());

        Label label = new Label(
            sonoIlFondatore 
                ? "👑 Sei il fondatore di questo corso" 
                : "👤 Fondatore: " + getNomeFondatore()
        );
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        label.setStyle(
            sonoIlFondatore
                ? "-fx-background-color: linear-gradient(to right, #FFD700, #FFA500);" +
                  "-fx-text-fill: #4B2E2E; -fx-padding: 12; -fx-background-radius: 10;" +
                  "-fx-font-weight: bold; -fx-font-size: 14px;"
                : "-fx-background-color: #E3F2FD; -fx-text-fill: #1565C0;" +
                  "-fx-padding: 10; -fx-background-radius: 8; -fx-font-size: 13px;"
        );
        return label;
    }

    private String getNomeFondatore() {
        if (corso.getChef() != null && corso.getCodfiscaleFondatore() != null) {
            return corso.getChef().stream()
                .filter(c -> c.getCodFiscale().equals(corso.getCodfiscaleFondatore()))
                .map(c -> c.getNome() + " " + c.getCognome())
                .findFirst()
                .orElse("Sconosciuto");
        }
        return "Sconosciuto";
    }

    private boolean isFondatore(Chef chef) {
        return chef != null && corso.getCodfiscaleFondatore() != null
                && chef.getCodFiscale().equals(corso.getCodfiscaleFondatore());
    }

    private boolean isChefLoggato(Chef c) {
        Chef me = gestioneController.getChefLoggato();
        return me != null && c != null && me.getCodFiscale() != null 
                && me.getCodFiscale().equals(c.getCodFiscale());
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
            addChefBtn.setVisible(false);
            addChefBtn.setManaged(false);
            editable = false;
        }
        eliminaCorsoBtn.setDisable(!canDeleteCourse());
    }

    private void tornaAllaListaCorsi() {
        if (onChiudiCallback != null) {
            onChiudiCallback.run();
        } else {
            Stage stage = getStage(card);
            if (stage != null) stage.close();
        }
    }

    private void richiediEliminazione() {
        if (!canDeleteCourse()) {
            StyleHelper.showErrorDialog(
                "🔒 Permessi Insufficienti",
                "Solo il fondatore del corso può eliminarlo.\n\n👑 Fondatore: " + getNomeFondatore()
            );
            return;
        }

        StyleHelper.showConfirmationDialog(
            "⚠️ Conferma Eliminazione",
            "Eliminare il corso '" + corso.getNomeCorso() + "'?\n\n⚠️ Questa azione è IRREVERSIBILE!",
            this::eliminaCorso
        );
    }


    private void eliminaCorso() {
        int corsoId = corso.getIdCorso();
        gestioneController.eliminaCorso(corsoId);
        StyleHelper.showSuccessDialog("✅ Successo", "Il corso è stato eliminato con successo!");
        tornaAllaListaCorsi();
    }

    private void salvaModifiche() {
        try {
            validateAndSave();
        } catch (ValidationException ex) {
            StyleHelper.showValidationDialog("⚠️ Errore di Validazione", ex.getMessage());
        } catch (DataAccessException ex) {
            StyleHelper.showErrorDialog("❌ Errore Database", ex.getMessage());
        }
    }

    private void validateAndSave() throws ValidationException {
        String nome = nomeField.getText().trim();
        if (nome.isEmpty()) throw new ValidationException("Il campo 'Nome Corso' è obbligatorio.");

        String argomento = argomentoField.getText().trim();
        if (argomento.isEmpty()) throw new ValidationException("Il campo 'Argomento' è obbligatorio.");

        double prezzo;
        try {
            prezzo = Double.parseDouble(prezzoField.getText().trim().replace(',', '.'));
            if (prezzo < 0) throw new ValidationException("Il prezzo non può essere negativo.");
        } catch (NumberFormatException e) {
            throw new ValidationException("Il prezzo deve essere un numero valido (es: 50.00).");
        }

        int posti;
        try {
            posti = Integer.parseInt(numeroPostiField.getText().trim());
            if (posti <= 0) throw new ValidationException("Il numero di posti deve essere > 0.");
        } catch (NumberFormatException e) {
            throw new ValidationException("Il numero di posti deve essere un numero intero.");
        }

        corso.setNomeCorso(nome);
        corso.setPrezzo(prezzo);
        corso.setArgomento(argomento);
        corso.setNumeroPosti(posti);

        gestioneController.modificaCorso(corso);
        StyleHelper.showSuccessDialog("✅ Successo", "Il corso è stato modificato correttamente!");

        hasUnsavedChanges = false;

        setEditable(false);
        salvaBtn.setDisable(true);
        modificaBtn.setDisable(false);
        refreshChefListView();
    }

    private void mostraDialogModificheNonSalvate() {
        StyleHelper.showUnsavedChangesDialog(
            "⚠️ Modifiche Non Salvate",
            "Hai effettuato delle modifiche che non sono state salvate.\n\n" + "Cosa desideri fare?",
            () -> {
                salvaModifiche();
                if (!hasUnsavedChanges) {
                    tornaAllaListaCorsi();
                }
            },
            () -> {
                hasUnsavedChanges = false;
                tornaAllaListaCorsi();
            }
        );
    }

    private void addChangeListeners() {
        nomeField.textProperty().addListener((obs, oldVal, newVal) -> hasUnsavedChanges = true);
        prezzoField.textProperty().addListener((obs, oldVal, newVal) -> hasUnsavedChanges = true);
        argomentoField.textProperty().addListener((obs, oldVal, newVal) -> hasUnsavedChanges = true);
        numeroPostiField.textProperty().addListener((obs, oldVal, newVal) -> hasUnsavedChanges = true);
    }


private void apriVisualizzaSessioni() {
    if (ricettaController == null || ingredienteController == null) {
        StyleHelper.showErrorDialog(
            "Errore",
            "Controller non inizializzati.\n\nImpossibile aprire la gestione sessioni."
        );
        return;
    }

    try {
        CucinaDAO cucinaDAO = new CucinaDAO();
        InPresenzaDAO inPresenzaDAO = new InPresenzaDAO(cucinaDAO);
        OnlineDAO onlineDAO = new OnlineDAO();
        GestioneSessioni gestioneSessioni = new GestioneSessioni(inPresenzaDAO, onlineDAO, cucinaDAO);
        GestioneCucina gestioneCucina = new GestioneCucina(cucinaDAO);
        GestioneRicette gestioneRicette = ricettaController.getGestioneRicette();
        GestioneSessioniController sessioniController = new GestioneSessioniController(
            corso,
            gestioneSessioni,
            gestioneCucina,
            gestioneRicette
        );

        VisualizzaSessioniGUI visualizzaSessioniGUI = new VisualizzaSessioniGUI();
        visualizzaSessioniGUI.setCorso(corso);
        visualizzaSessioniGUI.setController(sessioniController);
        visualizzaSessioniGUI.setRicettaController(ricettaController);
        visualizzaSessioniGUI.setIngredienteController(ingredienteController);

        StackPane contentPane = trovaContentPane();
        
        if (contentPane == null) {
            StyleHelper.showErrorDialog("Errore", "Impossibile trovare il contenitore principale");
            return;
        }

        visualizzaSessioniGUI.setContentPane(contentPane);

        visualizzaSessioniGUI.setOnChiudiCallback(() -> {
            System.out.println("🔙 Tornando a DettagliCorsoGUI...");
            
            try {
                CorsoCucina corsoAggiornato = gestioneController.getCorsoCompleto(corso.getIdCorso());
                
                DettagliCorsoGUI dettagliGUI = new DettagliCorsoGUI();
                dettagliGUI.setController(gestioneController);
                dettagliGUI.setChefController(chefController);
                dettagliGUI.setRicettaController(ricettaController);
                dettagliGUI.setIngredienteController(ingredienteController);
                dettagliGUI.setCorso(corsoAggiornato);
                dettagliGUI.setOnChiudiCallback(onChiudiCallback);
                
                contentPane.getChildren().setAll(dettagliGUI.getRoot());
                System.out.println("✅ Tornato a DettagliCorsoGUI con successo");
                
            } catch (Exception ex) {
                System.err.println("❌ Errore nel tornare a DettagliCorso: " + ex.getMessage());
                ex.printStackTrace();
                StyleHelper.showErrorDialog("Errore", "Impossibile ricaricare i dettagli del corso");
            }
        });

        contentPane.getChildren().setAll(visualizzaSessioniGUI.getRoot());
        System.out.println("✅ VisualizzaSessioniGUI aperto");

    } catch (Exception ex) {
        StyleHelper.showErrorDialog(
            "Errore",
            "Impossibile inizializzare la gestione sessioni: " + ex.getMessage()
        );
        System.err.println("Errore inizializzazione sessioni: " + ex.getMessage());
        ex.printStackTrace();
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
        System.err.println("⚠️ Errore nel trovare contentPane: " + ex.getMessage());
    }
    
    return null;
}

    private void setEditable(boolean edit) {
        if (isCorsoFinito()) {
            this.editable = false;
            return;
        }

        this.editable = edit;

        if (edit) {
            addChangeListeners();
        } else {
            hasUnsavedChanges = false;
        }

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

        frequenzaCombo.setDisable(true);
        dataInizioPicker.setDisable(true);
        dataFinePicker.setDisable(true);

        addChefBtn.setDisable(!edit);

        String borderColor = edit ? StyleHelper.PRIMARY_ORANGE : StyleHelper.BORDER_LIGHT;

        String fieldStyle = 
            "-fx-text-fill: " + StyleHelper.TEXT_BLACK + ";" +
            "-fx-background-color: white;" +
            "-fx-opacity: 1.0;" +
            "-fx-border-color: " + borderColor + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 10 15;" +
            "-fx-font-size: 14px;";

        nomeField.setStyle(fieldStyle);
        prezzoField.setStyle(fieldStyle);
        argomentoField.setStyle(fieldStyle);
        numeroPostiField.setStyle(fieldStyle);

        refreshChefListView();
    }

    private void refreshChefListView() {
        Platform.runLater(() -> {
            List<Chef> lista = corso.getChef() != null ? new ArrayList<>(corso.getChef()) : new ArrayList<>();
            lista.sort(
                Comparator.comparing((Chef ch) -> !isFondatore(ch))
                    .thenComparing((Chef ch) -> !isChefLoggato(ch))
                    .thenComparing(Chef::getCognome)
                    .thenComparing(Chef::getNome)
            );

            chefListView.getItems().setAll(lista);
        });
    }

    private void rimuoviChef(Chef chef) {
    if (!editable || isCorsoFinito()) return;

    if (isChefLoggato(chef)) {
        StyleHelper.showValidationDialog(
            "⚠️ Operazione Non Permessa",
            "Non puoi rimuovere te stesso dall'elenco del corso."
        );
        return;
    }

    if (isFondatore(chef)) {
        StyleHelper.showErrorDialog(
            "❌ Operazione Non Permessa",
            "Non è possibile rimuovere il fondatore del corso.\n\n👑 " + 
            chef.getNome() + " " + chef.getCognome() + " ha creato questo corso."
        );
        return;
    }

    Chef chefLoggato = gestioneController.getChefLoggato();
    if (chefLoggato == null || corso.getCodfiscaleFondatore() == null
            || !chefLoggato.getCodFiscale().equals(corso.getCodfiscaleFondatore())) {
        StyleHelper.showErrorDialog(
            "🔒 Permessi Insufficienti",
            "Solo il fondatore del corso può rimuovere altri chef.\n\n" + 
            "👑 Fondatore: " + getNomeFondatore()
        );
        return;
    }

    // ✅ USA LA NUOVA DIALOG CUSTOM
    StyleHelper.showCustomConfirmationDialog(
        "Rimuovi Chef",
        "Rimuovere " + chef.getNome() + " " + chef.getCognome() + " dal corso?\n\n⚠️ Questa azione è irreversibile.",
        () -> {
            try {
                gestioneController.rimuoviChefDaCorso(corso, chef);
                corso.getChef().remove(chef);
                chefListView.getItems().remove(chef);
                refreshChefListView();
                StyleHelper.showSuccessDialog(
                    "✅ Chef Rimosso",
                    chef.getNome() + " " + chef.getCognome() + " è stato rimosso con successo."
                );
            } catch (ValidationException ex) {
                StyleHelper.showValidationDialog("⚠️ Errore Validazione", ex.getMessage());
            } catch (DataAccessException ex) {
                StyleHelper.showErrorDialog("❌ Errore Database", ex.getMessage());
            }
        }
    );
    }



    private void aggiungiChef(Chef chef, String password) {
        if (!editable || isCorsoFinito()) return;

        if (corso.getChef() != null && corso.getChef().contains(chef)) {
            StyleHelper.showValidationDialog(
                "⚠️ Chef già Presente",
                chef.getNome() + " " + chef.getCognome() + " è già assegnato a questo corso."
            );
            return;
        }

        try {
            gestioneController.aggiungiChefACorso(corso, chef, password);
            refreshChefListView();
            StyleHelper.showSuccessDialog(
                "✅ Chef Aggiunto",
                chef.getNome() + " " + chef.getCognome() + " è stato aggiunto con successo!"
            );
        } catch (ValidationException ex) {
            StyleHelper.showValidationDialog("⚠️ Errore Validazione", ex.getMessage());
        } catch (DataAccessException ex) {
            StyleHelper.showErrorDialog("❌ Errore Database", ex.getMessage());
        }
    }

    private String safeString(String s) {
        return s == null ? "" : s;
    }

    private Stage getStage(Node node) {
        if (node == null) return null;
        javafx.scene.Scene s = node.getScene();
        if (s == null) return null;
        return (s.getWindow() instanceof Stage) ? (Stage) s.getWindow() : null;
    }
}
