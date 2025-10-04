package Gui;

import controller.GestioneCorsoController;
import model.CorsoCucina;
import model.Frequenza;
import model.Chef;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import exceptions.ValidationException;

public class DettagliCorsoGUI {
    private GestioneCorsoController gestioneController;
    private CorsoCucina corso;
    private VBox card;
    private boolean editable = false;
    private Runnable onChiudiCallback;

    private TextField nomeField;
    private TextField prezzoField;
    private TextField argomentoField;
    private ComboBox<Frequenza> frequenzaCombo;
    private TextField numeroPostiField;
    private TextField numeroSessioniField;
    private DatePicker dataInizioPicker;
    private DatePicker dataFinePicker;
    private ListView<Chef> chefListView;
    private ComboBox<Chef> addChefCombo;
    private Button addChefBtn;
    private Label selezionatoLabel;
    private Button modificaBtn;
    private Button salvaBtn;

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
        card.setStyle("-fx-background-color: white;" +
                "-fx-background-radius: 16;" +
                "-fx-border-radius: 16;" +
                "-fx-border-color: #FF9966;" +
                "-fx-border-width: 2;");

        DropShadow shadow = new DropShadow(12, Color.web("#000000", 0.15));
        shadow.setOffsetY(4);
        card.setEffect(shadow);

        Label title = new Label("📋 Dettagli Corso");
        title.setFont(Font.font("Roboto", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#FF6600"));

        nomeField = new TextField(safeString(corso.getNomeCorso()));
        nomeField.setFont(Font.font("Roboto", 14));
        
        prezzoField = new TextField(String.valueOf(corso.getPrezzo()));
        prezzoField.setFont(Font.font("Roboto", 14));
        
        argomentoField = new TextField(safeString(corso.getArgomento()));
        argomentoField.setFont(Font.font("Roboto", 14));

        frequenzaCombo = new ComboBox<>();
        frequenzaCombo.getItems().setAll(Frequenza.values());
        frequenzaCombo.setValue(corso.getFrequenzaCorso());

        numeroPostiField = new TextField(String.valueOf(corso.getNumeroPosti()));
        numeroPostiField.setFont(Font.font("Roboto", 14));

        numeroSessioniField = new TextField(
                corso.getSessioni() != null ? String.valueOf(corso.getSessioni().size()) : "0"
        );
        numeroSessioniField.setEditable(false);
        numeroSessioniField.setStyle("-fx-control-inner-background: #E9ECEF; -fx-font-size: 14;");

        dataInizioPicker = new DatePicker(
                corso.getDataInizioCorso() != null ? corso.getDataInizioCorso().toLocalDate() : null
        );
        dataFinePicker = new DatePicker(
                corso.getDataFineCorso() != null ? corso.getDataFineCorso().toLocalDate() : null
        );

        chefListView = new ListView<>();
        chefListView.setPrefHeight(150);
        chefListView.setMinHeight(100);
        chefListView.setMaxHeight(250);
        chefListView.setCellFactory(lv -> new ListCell<>() {
            private final HBox box = new HBox(8);
            private final Label nameLabel = new Label();
            private final Label meLabel = new Label(" (io)");
            private final Button removeBtn = new Button("Rimuovi");

            {
                meLabel.setStyle("-fx-text-fill: #FF6600; -fx-font-weight: bold;");
                removeBtn.setStyle("-fx-background-radius: 8; -fx-background-color: #FF6666; -fx-text-fill: white; -fx-cursor: hand;");
                removeBtn.setOnAction(e -> {
                    Chef it = getItem();
                    if (it != null) rimuoviChef(it);
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
                    nameLabel.setText(item.getUsername());
                    box.getChildren().clear();
                    box.getChildren().add(nameLabel);
                    if (isChefLoggato(item)) box.getChildren().add(meLabel);
                    if (editable) {
                        removeBtn.setDisable(isChefLoggato(item));
                        box.getChildren().add(removeBtn);
                    }
                    setGraphic(box);
                }
            }
        });

        selezionatoLabel = new Label("Selezionato: nessuno");
        selezionatoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
        chefListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) selezionatoLabel.setText("Selezionato: nessuno");
            else selezionatoLabel.setText("Selezionato: " + newV.getUsername() + (isChefLoggato(newV) ? " (io)" : ""));
        });

        addChefCombo = new ComboBox<>();
        addChefCombo.setPrefWidth(300);
        addChefBtn = new Button("➕ Aggiungi chef");
        addChefBtn.setStyle("-fx-cursor: hand; -fx-background-color: #28A745; -fx-text-fill: white; -fx-font-weight: bold;");
        addChefBtn.setOnAction(e -> {
            Chef toAdd = addChefCombo.getValue();
            if (toAdd == null) {
                showAlert("Attenzione", "Seleziona uno chef.");
                return;
            }
            TextInputDialog pwdDlg = new TextInputDialog();
            pwdDlg.setTitle("Password (opzionale)");
            pwdDlg.setHeaderText("Inserisci password per lo chef (se necessario) o lascia vuoto:");
            pwdDlg.setContentText("Password:");
            pwdDlg.showAndWait().ifPresent(pw -> aggiungiChef(toAdd, pw));
        });

        HBox addBox = new HBox(10, addChefCombo, addChefBtn);
        addBox.setAlignment(Pos.CENTER_LEFT);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        int row = 0;
        grid.add(createLabel("📚 Nome:"), 0, row);
        grid.add(nomeField, 1, row++);

        grid.add(createLabel("💰 Prezzo (€):"), 0, row);
        grid.add(prezzoField, 1, row++);

        grid.add(createLabel("📖 Argomento:"), 0, row);
        grid.add(argomentoField, 1, row++);

        grid.add(createLabel("📅 Frequenza:"), 0, row);
        grid.add(frequenzaCombo, 1, row++);

        grid.add(createLabel("🪑 Numero posti:"), 0, row);
        grid.add(numeroPostiField, 1, row++);

        grid.add(createLabel("⏰ Numero sessioni:"), 0, row);
        grid.add(numeroSessioniField, 1, row++);

        grid.add(createLabel("🕑 Data inizio:"), 0, row);
        grid.add(dataInizioPicker, 1, row++);

        grid.add(createLabel("🏁 Data fine:"), 0, row);
        grid.add(dataFinePicker, 1, row++);

        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(150);
        c0.setPrefWidth(150);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setMinWidth(350);
        grid.getColumnConstraints().addAll(c0, c1);

        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(20, 0, 0, 0));

        modificaBtn = createStylishButton("✏️ Modifica", "#FFC107");
        salvaBtn = createStylishButton("💾 Salva", "#28A745");
        Button creaSessioneBtn = createStylishButton("➕ Crea Sessione", "#007BFF");
        Button chiudiBtn = createStylishButton("❌ Chiudi", "#6C757D");

        salvaBtn.setDisable(true);

        modificaBtn.setOnAction(e -> {
            setEditable(true);
            salvaBtn.setDisable(false);
            modificaBtn.setDisable(true);
        });

        salvaBtn.setOnAction(e -> salvaModifiche());
        creaSessioneBtn.setOnAction(e -> apriGestioneSessioni());
        chiudiBtn.setOnAction(e -> tornaAllaListaCorsi());

        buttons.getChildren().addAll(modificaBtn, salvaBtn, creaSessioneBtn, chiudiBtn);

        card.getChildren().addAll(
                title,
                new Separator(),
                grid,
                new Separator(),
                createLabel("👥 Chef assegnati al corso:"),
                chefListView,
                selezionatoLabel,
                createLabel("➕ Aggiungi uno chef dal sistema:"),
                addBox,
                buttons
        );

        setEditable(false);
        refreshChefListView();

        VBox wrapper = new VBox(card);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setPadding(new Insets(20));
        scrollPane.setContent(wrapper);

        mainContainer.getChildren().add(scrollPane);
        return mainContainer;
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Roboto", FontWeight.SEMI_BOLD, 15));
        label.setTextFill(Color.web("#212529"));
        return label;
    }

    private Button createStylishButton(String text, String color) {
        Button b = new Button(text);
        b.setPrefWidth(160);
        b.setPrefHeight(42);
        b.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
        b.setTextFill(Color.WHITE);
        b.setStyle(String.format("-fx-background-radius: 10; -fx-background-color: %s; -fx-cursor: hand;", color));
        
        b.setOnMouseEntered(e -> b.setOpacity(0.8));
        b.setOnMouseExited(e -> b.setOpacity(1.0));
        
        return b;
    }

    private void tornaAllaListaCorsi() {
        if (onChiudiCallback != null) {
            onChiudiCallback.run();
        } else {
            Stage stage = getStage(card);
            if (stage != null) stage.close();
        }
    }

    private void salvaModifiche() {
        try {
            double prezzo = Double.parseDouble(prezzoField.getText().replace(',', '.'));
            int posti = Integer.parseInt(numeroPostiField.getText());

            if (dataInizioPicker.getValue() != null && dataFinePicker.getValue() != null &&
                    dataInizioPicker.getValue().isAfter(dataFinePicker.getValue())) {
                showAlert("Errore", "La data di inizio deve precedere la data di fine.");
                return;
            }

            corso.setNomeCorso(nomeField.getText());
            corso.setPrezzo(prezzo);
            corso.setArgomento(argomentoField.getText());
            corso.setFrequenzaCorso(frequenzaCombo.getValue());
            corso.setNumeroPosti(posti);

            if (dataInizioPicker.getValue() != null)
                corso.setDataInizioCorso(dataInizioPicker.getValue().atStartOfDay());
            if (dataFinePicker.getValue() != null)
                corso.setDataFineCorso(dataFinePicker.getValue().atStartOfDay());

            gestioneController.modificaCorso(corso);

            corso.setNumeroSessioni(corso.getSessioni() != null ? corso.getSessioni().size() : 0);
            numeroSessioniField.setText(String.valueOf(corso.getNumeroSessioni()));

            showAlert("Successo", "Corso modificato correttamente!");
            setEditable(false);
            salvaBtn.setDisable(true);
            modificaBtn.setDisable(false);
            refreshChefListView();

        } catch (ValidationException ex) {
            showAlert("Errore di validazione", ex.getMessage());
        } catch (NumberFormatException ex) {
            showAlert("Errore", "Valori numerici non validi per prezzo o posti.");
        } catch (Exception ex) {
            showAlert("Errore", "Errore nel salvataggio: " + ex.getMessage());
        }
    }

    private void apriGestioneSessioni() {
        GestioneSessioniGUI sessioniGUI = new GestioneSessioniGUI();
        sessioniGUI.setCorso(corso);
        Stage stage = new Stage();
        stage.setTitle("Gestione Sessioni - " + safeString(corso.getNomeCorso()));
        stage.setScene(new Scene(sessioniGUI.getRoot(), 800, 600));
        stage.showAndWait();

        if (corso.getSessioni() != null) {
            corso.setNumeroSessioni(corso.getSessioni().size());
            numeroSessioniField.setText(String.valueOf(corso.getNumeroSessioni()));
        }
    }

    private String safeString(String s) {
        return s == null ? "" : s;
    }

    private Stage getStage(Node node) {
        if (node == null) return null;
        Scene s = node.getScene();
        if (s == null) return null;
        return (s.getWindow() instanceof Stage) ? (Stage) s.getWindow() : null;
    }

    private boolean isChefLoggato(Chef c) {
        if (gestioneController == null) return false;
        Chef me = gestioneController.getChefLoggato();
        return me != null && c != null && me.getCodFiscale() != null && me.getCodFiscale().equals(c.getCodFiscale());
    }

    private void refreshChefListView() {
        Platform.runLater(() -> {
            List<Chef> lista = corso.getChef() != null ? new ArrayList<>(corso.getChef()) : new ArrayList<>();
            lista.sort(Comparator.comparing((Chef ch) -> !isChefLoggato(ch)).thenComparing(Chef::getUsername));
            chefListView.getItems().setAll(lista);

            try {
                List<Chef> all = gestioneController.getTuttiGliChef();
                List<Chef> avail = all.stream()
                        .filter(c -> !lista.contains(c))
                        .collect(Collectors.toList());
                addChefCombo.getItems().setAll(avail);
                if (!avail.isEmpty()) addChefCombo.setValue(avail.get(0));
            } catch (Exception ex) {
                addChefCombo.getItems().clear();
            }
        });
    }

    private void rimuoviChef(Chef chef) {
        if (!editable) return;
        if (isChefLoggato(chef)) {
            showAlert("Operazione non permessa", "Non puoi rimuovere te stesso dall'elenco.");
            return;
        }

        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                "Rimuovere " + chef.getUsername() + " dal corso?", ButtonType.OK, ButtonType.CANCEL);
        conf.showAndWait().ifPresent(b -> {
            if (b == ButtonType.OK) {
                try {
                    gestioneController.rimuoviChefDaCorso(corso, chef);
                    corso.getChef().remove(chef);
                    refreshChefListView();
                    showAlert("Rimosso", "Chef rimosso correttamente.");
                } catch (Exception ex) {
                    showAlert("Errore", ex.getMessage());
                }
            }
        });
    }

    private void aggiungiChef(Chef chef, String password) {
        if (!editable) return;
        try {
            gestioneController.aggiungiChefACorso(corso, chef, password);
            if (corso.getChef() == null) corso.setChef(new ArrayList<>());
            if (!corso.getChef().contains(chef)) corso.getChef().add(chef);
            refreshChefListView();
            showAlert("Aggiunto", "Chef aggiunto correttamente.");
        } catch (ValidationException ex) {
            showAlert("Attenzione", ex.getMessage());
        } catch (Exception ex) {
            showAlert("Errore", ex.getMessage());
        }
    }
    
    
    
    
    private void setEditable(boolean edit) {
        this.editable = edit;
        nomeField.setEditable(edit);
        prezzoField.setEditable(edit);
        argomentoField.setEditable(edit);
        frequenzaCombo.setDisable(!edit);
        numeroPostiField.setEditable(edit);
        dataInizioPicker.setDisable(!edit);
        dataFinePicker.setDisable(!edit);
        addChefCombo.setDisable(!edit);
        addChefBtn.setDisable(!edit);

        String borderColor = edit ? "#FF9966" : "#CED4DA";
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16;" +
                "-fx-border-radius: 16; -fx-border-color: " + borderColor + "; -fx-border-width: 2;");

        refreshChefListView();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
