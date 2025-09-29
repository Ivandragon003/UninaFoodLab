package Gui;

import controller.GestioneCorsoController;
import model.CorsoCucina;
import model.Frequenza;
import model.Chef;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DettagliCorsoGUI {

    private GestioneCorsoController gestioneController;
    private CorsoCucina corso;

    // Componenti riutilizzati
    private TextField nomeField;
    private TextField prezzoField;
    private TextField argomentoField;
    private ComboBox<Frequenza> frequenzaCombo;
    private TextField numeroPostiField;
    private DatePicker dataInizioPicker;
    private DatePicker dataFinePicker;

    private ListView<Chef> chefListView;
    private ComboBox<Chef> addChefCombo;
    private Button addChefBtn;
    private Label selezionatoLabel;

    private VBox card;
    private boolean editable = false;

    public void setController(GestioneCorsoController controller) {
        this.gestioneController = controller;
    }

    public void setCorso(CorsoCucina corso) {
        this.corso = corso;
    }

    /**
     * Restituisce la card principale (VBox) da inserire nel layout chiamante.
     * Questo permette a VisualizzaCorsiGUI di riusare lo sfondo/StackPane esterno.
     */
    public VBox getRoot() {
        if (gestioneController == null || corso == null) {
            throw new IllegalStateException("Controller o corso non impostati!");
        }

        // Card centrale (stile originale)
        card = new VBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(25));
        card.setMaxWidth(420);
        card.setMinWidth(320);
        card.setStyle("-fx-background-color: white;" +
                "-fx-background-radius: 20;" +
                "-fx-border-radius: 20;" +
                "-fx-border-color: #FF9966;" +
                "-fx-border-width: 2;");

        DropShadow shadow = new DropShadow(10, Color.web("#000000", 0.2));
        shadow.setOffsetY(3);
        card.setEffect(shadow);

        // Header (titolo)
        Label title = new Label("Dettagli corso");
        title.setFont(Font.font("Roboto", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#FF6600"));

        // Pulsanti finestra (x, _, ☐) - usa Node per trovare lo stage eventualmente
        HBox windowButtons = createWindowButtons(card);
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(headerBox, Priority.ALWAYS);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerBox.getChildren().addAll(title, spacer, windowButtons);

        // Campi
        nomeField = new TextField(corso.getNomeCorso());
        prezzoField = new TextField(String.valueOf(corso.getPrezzo()));
        argomentoField = new TextField(corso.getArgomento());

        frequenzaCombo = new ComboBox<>();
        frequenzaCombo.getItems().setAll(Frequenza.values());
        frequenzaCombo.setValue(corso.getFrequenzaCorso());

        numeroPostiField = new TextField(String.valueOf(corso.getNumeroPosti()));
        TextField numeroSessioniField = new TextField(
                corso.getSessioni() != null ? String.valueOf(corso.getSessioni().size()) : "0"
        );
        numeroSessioniField.setEditable(false);
        numeroSessioniField.setStyle("-fx-control-inner-background: #E0E0E0;");

        dataInizioPicker = new DatePicker(
                corso.getDataInizioCorso() != null ? corso.getDataInizioCorso().toLocalDate() : null
        );
        dataFinePicker = new DatePicker(
                corso.getDataFineCorso() != null ? corso.getDataFineCorso().toLocalDate() : null
        );

        // Lista chef assegnati
        chefListView = new ListView<>();
        chefListView.setPrefHeight(140);
        chefListView.setMinHeight(100);
        chefListView.setMaxHeight(220);
        chefListView.setCellFactory(lv -> new ListCell<>() {
            private final HBox box = new HBox(8);
            private final Label nameLabel = new Label();
            private final Label meLabel = new Label(" (io)");
            private final Button removeBtn = new Button("Rimuovi");

            {
                meLabel.setStyle("-fx-text-fill: #FF6600; -fx-font-weight: bold;");
                removeBtn.setStyle("-fx-background-radius: 8; -fx-background-color: #FF6666; -fx-text-fill: white;");
                removeBtn.setOnAction(e -> {
                    Chef it = getItem();
                    if (it != null) {
                        rimuoviChef(it);
                    }
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
                        // se siamo in modifica, mostriamo il bottone rimuovi (ma disabilitiamo se è lo chef loggato)
                        removeBtn.setDisable(isChefLoggato(item));
                        box.getChildren().add(removeBtn);
                    }
                    setGraphic(box);
                }
            }
        });

        // Label che mostra la selezione corrente
        selezionatoLabel = new Label("Selezionato: nessuno");
        selezionatoLabel.setStyle("-fx-font-weight: bold;");
        chefListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) selezionatoLabel.setText("Selezionato: nessuno");
            else selezionatoLabel.setText("Selezionato: " + newV.getUsername() + (isChefLoggato(newV) ? " (io)" : ""));
        });

        // Combo + bottone per aggiungere chef
        addChefCombo = new ComboBox<>();
        addChefCombo.setPrefWidth(260);
        addChefBtn = new Button("Aggiungi chef");
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

        HBox addBox = new HBox(8, addChefCombo, addChefBtn);
        addBox.setAlignment(Pos.CENTER_LEFT);

        // Pulsanti
        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);

        Button modificaBtn = createStylishButton("✏ Modifica");
        Button salvaBtn = createStylishButton("💾 Salva");
        Button sessioniBtn = createStylishButton("📅 Sessioni");
        Button indietroBtn = createStylishButton("⬅ Indietro");

        salvaBtn.setDisable(true);

        // Eventi pulsanti
        modificaBtn.setOnAction(e -> {
            setEditable(true);
            salvaBtn.setDisable(false);
        });

        salvaBtn.setOnAction(e -> {
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
                corso.setNumeroSessioni(corso.getSessioni() != null ? corso.getSessioni().size() : 0);
                if (dataInizioPicker.getValue() != null) corso.setDataInizioCorso(dataInizioPicker.getValue().atStartOfDay());
                if (dataFinePicker.getValue() != null) corso.setDataFineCorso(dataFinePicker.getValue().atStartOfDay());

                gestioneController.modificaCorso(corso);
                numeroSessioniField.setText(String.valueOf(corso.getNumeroSessioni()));

                showAlert("Successo", "Corso modificato correttamente!");

                setEditable(false);
                salvaBtn.setDisable(true);
                refreshChefListView();
            } catch (NumberFormatException ex) {
                showAlert("Errore", "Valori numerici non validi per prezzo o posti.");
            } catch (SQLException ex) {
                showAlert("Errore DB", ex.getMessage());
            } catch (Exception ex) {
                showAlert("Errore", "Errore nel salvataggio: " + ex.getMessage());
            }
        });

        sessioniBtn.setOnAction(e -> {
            GestioneSessioniGUI sessioniGUI = new GestioneSessioniGUI();
            sessioniGUI.setCorso(corso);
            if (card.getScene() != null) {
                card.getScene().setRoot(sessioniGUI.getRoot());
            }
        });

        indietroBtn.setOnAction(e -> {
            // Se la card è mostrata in uno Stage (dettagli in una finestra), chiudiamo lo stage.
            Stage stage = getStage(card);
            if (stage != null) {
                stage.close();
                return;
            }
            // Altrimenti, se la card è dentro un Pane, la rimuoviamo dal padre.
            Parent parent = card.getParent();
            if (parent instanceof Pane) {
                ((Pane) parent).getChildren().remove(card);
            }
        });

        buttons.getChildren().addAll(modificaBtn, salvaBtn, sessioniBtn, indietroBtn);

        // Montaggio card
        card.getChildren().addAll(
                headerBox,
                new Label("Nome:"), nomeField,
                new Label("Prezzo:"), prezzoField,
                new Label("Argomento:"), argomentoField,
                new Label("Frequenza:"), frequenzaCombo,
                new Label("Numero posti:"), numeroPostiField,
                new Label("Numero sessioni:"), numeroSessioniField,
                new Label("Data inizio:"), dataInizioPicker,
                new Label("Data fine:"), dataFinePicker,
                new Separator(),
                new Label("Chef assegnati:"),
                chefListView,
                selezionatoLabel,
                new Label("Aggiungi uno chef dal sistema:"),
                addBox,
                buttons
        );

        // inizializza campi e lista chef
        setEditable(false);
        refreshChefListView();

        return card;
    }

    // ===== Helpers =====

    private HBox createWindowButtons(Node rootNode) {
        HBox controls = new HBox(6);
        controls.setAlignment(Pos.CENTER_RIGHT);

        Button minimizeBtn = createWindowBtn("_", Color.WHITE);
        Button maximizeBtn = createWindowBtn("⬜", Color.WHITE);
        Button closeBtn = createWindowBtn("✖", Color.web("#FF4444"));

        minimizeBtn.setOnAction(e -> {
            Stage stage = getStage(rootNode);
            if (stage != null) stage.setIconified(true);
        });
        maximizeBtn.setOnAction(e -> {
            Stage stage = getStage(rootNode);
            if (stage != null) stage.setMaximized(!stage.isMaximized());
        });
        closeBtn.setOnAction(e -> {
            Stage stage = getStage(rootNode);
            if (stage != null) stage.close();
        });

        controls.getChildren().addAll(minimizeBtn, maximizeBtn, closeBtn);
        return controls;
    }

    private Button createWindowBtn(String symbol, Color color) {
        Button btn = new Button(symbol);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        btn.setTextFill(color);
        btn.setStyle("-fx-background-color: transparent;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(0,0,0,0.06);"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent;"));
        btn.setPrefSize(30, 26);
        return btn;
    }

    private Stage getStage(Node node) {
        if (node == null) return null;
        Scene s = node.getScene();
        if (s == null) return null;
        if (s.getWindow() instanceof Stage) return (Stage) s.getWindow();
        return null;
    }

    private boolean isChefLoggato(Chef c) {
        Chef me = gestioneController.getChefLoggato();
        return me != null && c != null && me.getCodFiscale() != null && me.getCodFiscale().equals(c.getCodFiscale());
    }

    private void refreshChefListView() {
        Platform.runLater(() -> {
            // ordina chef in modo che lo chef loggato (se presente) sia primo
            List<Chef> lista = corso.getChef() != null ? new ArrayList<>(corso.getChef()) : new ArrayList<>();
            lista.sort(Comparator.comparing((Chef ch) -> !isChefLoggato(ch)).thenComparing(Chef::getUsername));
            chefListView.getItems().setAll(lista);

            // combo con chef non ancora assegnati
            try {
                List<Chef> all = gestioneController.getTuttiGliChef();
                List<Chef> avail = all.stream()
                        .filter(c -> !lista.contains(c))
                        .collect(Collectors.toList());
                addChefCombo.getItems().setAll(avail);
                if (!avail.isEmpty()) addChefCombo.setValue(avail.get(0));
            } catch (SQLException ex) {
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
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION, "Rimuovere " + chef.getUsername() + " dal corso?", ButtonType.OK, ButtonType.CANCEL);
        conf.showAndWait().ifPresent(b -> {
            if (b == ButtonType.OK) {
                try {
                    gestioneController.rimuoviChefDaCorso(corso, chef);
                    corso.getChef().remove(chef);
                    refreshChefListView();
                    showAlert("Rimosso", "Chef rimosso correttamente.");
                } catch (SQLException ex) {
                    showAlert("Errore DB", ex.getMessage());
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
        } catch (SQLException ex) {
            showAlert("Errore DB", ex.getMessage());
        } catch (IllegalArgumentException ex) {
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

        // aspetto "grigio" se non editabile
        if (!edit) {
            card.setOpacity(0.9);
            card.setStyle("-fx-background-color: white;" +
                    "-fx-background-radius: 20;" +
                    "-fx-border-radius: 20;" +
                    "-fx-border-color: #DDDDDD;" +
                    "-fx-border-width: 1;");
        } else {
            card.setOpacity(1.0);
            card.setStyle("-fx-background-color: white;" +
                    "-fx-background-radius: 20;" +
                    "-fx-border-radius: 20;" +
                    "-fx-border-color: #FF9966;" +
                    "-fx-border-width: 2;");
        }

        // ricarica lista per mostrare/nascondere i pulsanti rimuovi
        refreshChefListView();
    }

    private Button createStylishButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: linear-gradient(to right, #FF9966, #FF5E62);" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 20;" +
                "-fx-padding: 8 16;");
        return btn;
    }

    private void showAlert(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
