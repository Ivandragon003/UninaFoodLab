package Gui;

import controller.IngredienteController;
import dao.IngredienteDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Ingrediente;
import model.Ricetta;
import service.GestioneIngrediente;
import service.GestioneRicette;
import util.StyleHelper;

import java.util.HashMap;
import java.util.Map;


public class CreaRicettaGUI extends Stage {

    private GestioneRicette gestioneRicette;
    private IngredienteController ingredienteController;
    private Ricetta ricettaCreata = null;

    private TextField nomeField;
    private TextField tempoField;


    private VBox ingredientiContainer;
    private ObservableList<IngredienteConPeso> ingredientiSelezionati = FXCollections.observableArrayList();
    private Button aggiungiIngredienteBtn;
    private Button creaIngredienteBtn;
    private Label contatoreIngredienti;

    private double xOffset = 0;
    private double yOffset = 0;

    private static class IngredienteConPeso {
        private final Ingrediente ingrediente;
        private Double peso;

        public IngredienteConPeso(Ingrediente ingrediente, Double peso) {
            this.ingrediente = ingrediente;
            this.peso = peso;
        }

        public Ingrediente getIngrediente() { return ingrediente; }
        public Double getPeso() { return peso; }
        public void setPeso(Double peso) { this.peso = peso; }
    }

    public CreaRicettaGUI(GestioneRicette gestioneRicette) {
        this.gestioneRicette = gestioneRicette;
        initializeIngredienteController();
        setTitle("Crea Ricetta");
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UNDECORATED);
        setResizable(true);
        createLayout();
    }

    public CreaRicettaGUI() {
        this.gestioneRicette = null;
        initializeIngredienteController();
        setTitle("Crea Ricetta");
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UNDECORATED);
        setResizable(true);
        createLayout();
    }

    //  Inizializza controller ingredienti 
    private void initializeIngredienteController() {
        try {
            IngredienteDAO ingredienteDAO = new IngredienteDAO();
            GestioneIngrediente gestioneIngrediente = new GestioneIngrediente(ingredienteDAO);
            this.ingredienteController = new IngredienteController(gestioneIngrediente);
        } catch (Exception e) {
            System.err.println("Errore init controller: " + e.getMessage());
        }
    }

    private void createLayout() {
        StackPane rootPane = new StackPane();
        rootPane.setMinSize(650, 700);
        rootPane.setPrefSize(800, 800);

        Region background = new Region();
        StyleHelper.applyBackgroundGradient(background);

        VBox mainContainer = new VBox(25);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(40, 30, 30, 30));

        Label title = new Label("✨ Crea la Tua Ricetta Speciale");
        title.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox formCard = StyleHelper.createSection();
        formCard.setSpacing(25);

        VBox infoSection = createInfoSection();
        VBox ingredientiSection = createIngredientiSection();
        HBox buttonSection = createButtonSection();

        formCard.getChildren().addAll(
            infoSection,
            new Separator(),
            ingredientiSection,
            new Separator(),
            buttonSection
        );

        scrollPane.setContent(formCard);
        mainContainer.getChildren().addAll(title, scrollPane);

        rootPane.getChildren().addAll(background, mainContainer);

        HBox windowButtons = createWindowButtons();
        rootPane.getChildren().add(windowButtons);
        StackPane.setAlignment(windowButtons, Pos.TOP_RIGHT);
        StackPane.setMargin(windowButtons, new Insets(8));

        makeDraggable(rootPane);

        Scene scene = new Scene(rootPane, 800, 800);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);
    }

    private HBox createWindowButtons() {
        Button closeButton = new Button("✕");
        Button minimizeButton = new Button("−");
        Button maximizeButton = new Button("○");

        Button[] buttons = { minimizeButton, maximizeButton, closeButton };
        for (Button btn : buttons) {
            btn.setPrefSize(30, 30);
            btn.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 12));
            btn.setTextFill(Color.WHITE);
            btn.setStyle("-fx-background-color: rgba(255,140,0,0.8); -fx-background-radius: 15; -fx-cursor: hand;");
            btn.setFocusTraversable(false);
        }

        closeButton.setOnAction(e -> close());
        minimizeButton.setOnAction(e -> setIconified(true));
        maximizeButton.setOnAction(e -> setMaximized(!isMaximized()));

        HBox box = new HBox(3, minimizeButton, maximizeButton, closeButton);
        box.setAlignment(Pos.TOP_RIGHT);
        box.setPickOnBounds(false);
        return box;
    }

    private void makeDraggable(StackPane root) {
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            setX(event.getScreenX() - xOffset);
            setY(event.getScreenY() - yOffset);
        });
    }

    
    private VBox createInfoSection() {
        VBox section = new VBox(20);

        Label sectionTitle = new Label("📝 Informazioni Base");
        sectionTitle.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(20);

        nomeField = StyleHelper.createTextField("Es. Pasta alla Carbonara Speciale");
        nomeField.setPrefWidth(400);

        tempoField = StyleHelper.createTextField("Es. 30");
        tempoField.setPrefWidth(120);

        grid.add(StyleHelper.createLabel("Nome della Ricetta:"), 0, 0);
        grid.add(nomeField, 1, 0, 2, 1);

        HBox tempoBox = new HBox(10);
        tempoBox.setAlignment(Pos.CENTER_LEFT);
        tempoBox.getChildren().addAll(tempoField, StyleHelper.createLabel("minuti"));

        grid.add(StyleHelper.createLabel("Tempo di Preparazione:"), 0, 1);
        grid.add(tempoBox, 1, 1);

        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }

    //  Sezione ingredienti con aggiunta e rimozione 
    private VBox createIngredientiSection() {
        VBox section = new VBox(20);

        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label sectionTitle = new Label("🥕 Ingredienti della Ricetta");
        sectionTitle.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        contatoreIngredienti = new Label("(0 ingredienti aggiunti)");
        contatoreIngredienti.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 14));
        contatoreIngredienti.setTextFill(Color.web(StyleHelper.SUCCESS_GREEN));

        headerBox.getChildren().addAll(sectionTitle, contatoreIngredienti);

        HBox buttonsBox = new HBox(15);
        buttonsBox.setAlignment(Pos.CENTER_LEFT);

        aggiungiIngredienteBtn = StyleHelper.createSuccessButton("📚 Scegli Esistente");
        aggiungiIngredienteBtn.setOnAction(e -> aggiungiIngredienteEsistente());

        creaIngredienteBtn = StyleHelper.createInfoButton("➕ Crea Nuovo");
        creaIngredienteBtn.setOnAction(e -> creaNuovoIngrediente());

        buttonsBox.getChildren().addAll(aggiungiIngredienteBtn, creaIngredienteBtn);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(250);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        ingredientiContainer = new VBox(10);
        ingredientiContainer.setStyle("-fx-background-color: white; -fx-border-color: " + StyleHelper.BORDER_LIGHT + "; " +
            "-fx-border-radius: 8; -fx-padding: 15; -fx-background-radius: 8;");
        ingredientiContainer.setOnMouseClicked(e -> ingredientiContainer.requestFocus());

        scrollPane.setContent(ingredientiContainer);

        updateIngredientiDisplay();

        section.getChildren().addAll(headerBox, buttonsBox, scrollPane);
        return section;
    }

    private void aggiungiIngredienteEsistente() {
        if (ingredienteController == null) {
            showAlert("Servizio non disponibile", "Controller ingredienti non disponibile.");
            return;
        }
        try {
            VisualizzaIngredientiGUI visualizzaGUI = new VisualizzaIngredientiGUI(ingredienteController);
            visualizzaGUI.setModalitaSelezione(true);
            Ingrediente ingredienteSelezionato = visualizzaGUI.showAndReturn();
            if (ingredienteSelezionato != null) {
                boolean giaEsiste = ingredientiSelezionati.stream()
                    .anyMatch(item -> item.getIngrediente().getNome().equals(ingredienteSelezionato.getNome()));
                if (giaEsiste) {
                    showAlert("Ingrediente già presente", 
                        "L'ingrediente '" + ingredienteSelezionato.getNome() + "' è già stato aggiunto.");
                    return;
                }
                Double quantita = chiediQuantitaUserFriendly(ingredienteSelezionato);
                if (quantita != null && quantita > 0) {
                    ingredientiSelezionati.add(new IngredienteConPeso(ingredienteSelezionato, quantita));
                    updateIngredientiDisplay();
                    showAlert("Successo", 
                        "Ingrediente aggiunto: " + ingredienteSelezionato.getNome() + " (" + quantita + "g)");
                }
            }
        } catch (Exception e) {
            showAlert("Errore", "Errore aggiunta ingrediente: " + e.getMessage());
        }
    }

    private void creaNuovoIngrediente() {
        if (ingredienteController == null) {
            showAlert("Servizio non disponibile", "Controller ingredienti non disponibile.");
            return;
        }
        try {
            CreaIngredientiGUI creaGUI = new CreaIngredientiGUI(ingredienteController);
            Ingrediente nuovoIngrediente = creaGUI.showAndReturn();
            if (nuovoIngrediente != null) {
                Double quantita = chiediQuantitaUserFriendly(nuovoIngrediente);
                if (quantita != null && quantita > 0) {
                    ingredientiSelezionati.add(new IngredienteConPeso(nuovoIngrediente, quantita));
                    updateIngredientiDisplay();
                    showAlert("Successo", 
                        "Ingrediente creato aggiunto: " + nuovoIngrediente.getNome() + " (" + quantita + "g)");
                }
            }
        } catch (Exception e) {
            showAlert("Errore", "Errore creazione ingrediente: " + e.getMessage());
        }
    }

    // Dialog per inserimento quantità 
    private Double chiediQuantitaUserFriendly(Ingrediente ingrediente) {
        TextInputDialog dialog = new TextInputDialog("100");
        dialog.setTitle("Quantità Ingrediente");
        dialog.setHeaderText("Ingrediente: " + ingrediente.getNome());
        dialog.setContentText("Inserisci la quantità (in grammi):");
        try {
            String result = dialog.showAndWait().orElse(null);
            if (result != null && !result.trim().isEmpty()) {
                double quantita = Double.parseDouble(result.trim());
                if (quantita <= 0) {
                    showAlert("Quantità non valida", "Deve essere > 0");
                    return null;
                }
                return quantita;
            }
        } catch (NumberFormatException e) {
            showAlert("Formato non valido", "Inserisci un numero valido");
        }
        return null;
    }

    // Aggiorna vista ingredienti 
    private void updateIngredientiDisplay() {
        ingredientiContainer.getChildren().clear();
        contatoreIngredienti.setText("(" + ingredientiSelezionati.size() + " aggiunti)");
        if (ingredientiSelezionati.isEmpty()) {
            VBox emptyBox = new VBox(15);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(30));
            Label emptyLabel = new Label("🍽️ Nessun ingrediente");
            emptyLabel.setFont(javafx.scene.text.Font.font("Roboto", 16));
            emptyLabel.setTextFill(Color.GRAY);
            Label istruzioniLabel = new Label("Usa i pulsanti per aggiungere");
            istruzioniLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));
            istruzioniLabel.setTextFill(Color.GRAY);
            emptyBox.getChildren().addAll(emptyLabel, istruzioniLabel);
            ingredientiContainer.getChildren().add(emptyBox);
        } else {
            for (IngredienteConPeso item : ingredientiSelezionati) {
                HBox ingredienteBox = new HBox(15);
                ingredienteBox.setAlignment(Pos.CENTER_LEFT);
                ingredienteBox.setPadding(new Insets(10));
                ingredienteBox.setStyle("-fx-background-color:#f8f9fa; -fx-border-color:" + StyleHelper.SUCCESS_GREEN + "; -fx-border-width:2; -fx-border-radius:8; -fx-background-radius:8;");
                ingredienteBox.setOnMouseClicked(e -> {
                    ingredienteBox.setStyle("-fx-background-color:#e3f2fd; -fx-border-color:" + StyleHelper.INFO_BLUE + "; -fx-border-width:2; -fx-border-radius:8; -fx-background-radius:8;");
                });
                VBox infoBox = new VBox(4);
                Label nomeLabel = new Label("🥕 " + item.getIngrediente().getNome());
                nomeLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 15));
                nomeLabel.setTextFill(Color.BLACK);
                Label tipoLabel = new Label("📂 " + item.getIngrediente().getTipo());
                tipoLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));
                tipoLabel.setTextFill(Color.GRAY);
                infoBox.getChildren().addAll(nomeLabel, tipoLabel);
                HBox pesoBox = new HBox(5);
                pesoBox.setAlignment(Pos.CENTER_LEFT);
                TextField pesoField = new TextField(String.valueOf(item.getPeso()));
                pesoField.setPrefWidth(80);
                pesoField.setStyle("-fx-background-color:white; -fx-border-color:" + StyleHelper.INFO_BLUE + "; -fx-border-radius:5;");
                pesoField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal) {
                        try {
                            double nuovoPeso = Double.parseDouble(pesoField.getText().trim());
                            if (nuovoPeso > 0) {
                                item.setPeso(nuovoPeso);
                            } else {
                                pesoField.setText(String.valueOf(item.getPeso()));
                                showAlert("Peso non valido", "Deve essere > 0");
                            }
                        } catch (NumberFormatException e) {
                            pesoField.setText(String.valueOf(item.getPeso()));
                            showAlert("Formato non valido", "Numero non valido");
                        }
                    }
                });
                pesoBox.getChildren().addAll(pesoField, new Label("g"));
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                Button removeBtn = new Button("🗑️");
                removeBtn.setStyle("-fx-background-color:" + StyleHelper.ERROR_RED + "; -fx-text-fill:white; -fx-background-radius:20; -fx-min-width:35; -fx-min-height:35; -fx-cursor:hand; -fx-font-weight:bold;");
                removeBtn.setTooltip(new Tooltip("Rimuovi ingrediente"));
                removeBtn.setOnAction(e -> {
                    Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
                    conferma.setTitle("Rimuovi Ingrediente");
                    conferma.setHeaderText("Conferma rimozione");
                    conferma.setContentText("Rimuovere '" + item.getIngrediente().getNome() + "'?");
                    conferma.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            ingredientiSelezionati.remove(item);
                            updateIngredientiDisplay();
                        }
                    });
                });
                ingredienteBox.getChildren().addAll(infoBox, spacer, pesoBox, removeBtn);
                ingredientiContainer.getChildren().add(ingredienteBox);
            }
        }
    }

    private HBox createButtonSection() {
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        Button annullaBtn = new Button("❌ Annulla");
        annullaBtn.setPrefSize(150, 45);
        annullaBtn.setStyle("-fx-background-color:" + StyleHelper.NEUTRAL_GRAY + "; -fx-text-fill:white; -fx-background-radius:20; -fx-cursor:hand; -fx-font-weight:bold; -fx-font-size:14px;");
        annullaBtn.setOnAction(e -> { ricettaCreata = null; close(); });
        Button salvaBtn = StyleHelper.createPrimaryButton("💾 Salva Ricetta");
        salvaBtn.setPrefSize(150, 45);
        salvaBtn.setStyle(salvaBtn.getStyle() + "; -fx-font-size:14px;");
        salvaBtn.setOnAction(e -> salvaRicetta());
        buttonBox.getChildren().addAll(annullaBtn, salvaBtn);
        return buttonBox;
    }

    // ** Salva ricetta su model/db **
    private void salvaRicetta() {
        try {
            if (!validateForm()) return;
            String nome = nomeField.getText().trim();
            int tempoPreparazione = Integer.parseInt(tempoField.getText().trim());
            ricettaCreata = new Ricetta(nome, tempoPreparazione);
            Map<Ingrediente, Double> ingredientiMap = new HashMap<>();
            for (IngredienteConPeso item : ingredientiSelezionati) {
                ingredientiMap.put(item.getIngrediente(), item.getPeso());
            }
            ricettaCreata.setIngredienti(ingredientiMap);
            if (gestioneRicette != null) {
                try {
                    gestioneRicette.creaRicetta(ricettaCreata);
                    showAlert("Successo", 
                        "Ricetta salvata: " + nome + ", " + tempoPreparazione + "min, " + ingredientiSelezionati.size() + " ingr.");
                } catch (Exception e) {
                    showAlert("Errore DB", e.getMessage());
                    return;
                }
            } else {
                showAlert("Successo", 
                    "Ricetta creata: " + nome + ", " + tempoPreparazione + "min, " + ingredientiSelezionati.size() + " ingr.");
            }
            close();
        } catch (Exception e) {
            showAlert("Errore", e.getMessage());
        }
    }

    // Validazione form 
    private boolean validateForm() {
        if (nomeField.getText().trim().isEmpty()) {
            showAlert("Campo obbligatorio", "Nome ricetta mancante");
            nomeField.requestFocus();
            return false;
        }
        try {
            int tempo = Integer.parseInt(tempoField.getText().trim());
            if (tempo <= 0) {
                showAlert("Tempo non valido", "Deve essere > 0");
                tempoField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Formato non valido", "Numero non valido");
            tempoField.requestFocus();
            return false;
        }
        if (ingredientiSelezionati.isEmpty()) {
            showAlert("Ingredienti obbligatori", "Aggiungi almeno un ingrediente");
            return false;
        }
        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.equals("Successo") ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Ricetta showAndReturn() {
        showAndWait();
        return ricettaCreata;
    }
}
