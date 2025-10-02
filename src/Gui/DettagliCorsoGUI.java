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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DettagliCorsoGUI {
    private GestioneCorsoController gestioneController;
    private CorsoCucina corso;
    private ScrollPane scrollPane;
    private VBox card;
    private boolean editable = false;

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

    public ScrollPane getRoot() {
        if (gestioneController == null || corso == null) {
            throw new IllegalStateException("Controller o corso non impostati!");
        }

        // Card principale
        card = new VBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(25));
        card.setMaxWidth(760);
        card.setMinWidth(500);
        card.setStyle("-fx-background-color: white;" +
                "-fx-background-radius: 20;" +
                "-fx-border-radius: 20;" +
                "-fx-border-color: #FF9966;" +
                "-fx-border-width: 2;");

        DropShadow shadow = new DropShadow(10, Color.web("#000000", 0.16));
        shadow.setOffsetY(3);
        card.setEffect(shadow);

        // Header con bottoni finestra
        Label title = new Label("Dettagli corso");
        title.setFont(Font.font("Roboto", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#FF6600"));

        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox windowButtons = createWindowButtons(card);
        headerBox.getChildren().addAll(title, spacer, windowButtons);

        // Campi
        nomeField = new TextField(safeString(corso.getNomeCorso()));
        prezzoField = new TextField(String.valueOf(corso.getPrezzo()));
        argomentoField = new TextField(safeString(corso.getArgomento()));

        frequenzaCombo = new ComboBox<>();
        frequenzaCombo.getItems().setAll(Frequenza.values());
        frequenzaCombo.setValue(corso.getFrequenzaCorso());

        numeroPostiField = new TextField(String.valueOf(corso.getNumeroPosti()));

        numeroSessioniField = new TextField(
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

        // Lista chef
        chefListView = new ListView<>();
        chefListView.setPrefHeight(160);
        chefListView.setMinHeight(100);
        chefListView.setMaxHeight(280);
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

        // Label selezione
        selezionatoLabel = new Label("Selezionato: nessuno");
        selezionatoLabel.setStyle("-fx-font-weight: bold;");
        chefListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) selezionatoLabel.setText("Selezionato: nessuno");
            else selezionatoLabel.setText("Selezionato: " + newV.getUsername() + (isChefLoggato(newV) ? " (io)" : ""));
        });

        // Combo + bottone aggiungi chef
        addChefCombo = new ComboBox<>();
        addChefCombo.setPrefWidth(320);
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

        // Pulsanti principali
        HBox buttons = new HBox(12);
        buttons.setAlignment(Pos.CENTER);

        modificaBtn = createStylishButton("✏ Modifica");
        salvaBtn = createStylishButton("💾 Salva");
        Button sessioniBtn = createStylishButton("📅 Sessioni");
        Button chiudiBtn = createStylishButton("✖ Chiudi");

        salvaBtn.setDisable(true);

        modificaBtn.setOnAction(e -> {
            setEditable(true);
            salvaBtn.setDisable(false);
            modificaBtn.setDisable(true);
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

            Stage sessioniStage = new Stage();
            sessioniStage.setTitle("Gestione Sessioni - " + safeString(corso.getNomeCorso()));
            Scene scene = new Scene(sessioniGUI.getRoot(), 800, 600);
            sessioniStage.setScene(scene);

            Stage currentStage = getStage(card);
            if (currentStage != null) {
                sessioniStage.initOwner(currentStage);
            }
            
            sessioniStage.setOnHidden(event -> {
                if (corso.getSessioni() != null) {
                    corso.setNumeroSessioni(corso.getSessioni().size());
                    numeroSessioniField.setText(String.valueOf(corso.getNumeroSessioni()));
                }
            });
            
            sessioniStage.showAndWait();
        });

        chiudiBtn.setOnAction(e -> {
            Stage stage = getStage(card);
            if (stage != null) stage.close();
        });

        buttons.getChildren().addAll(modificaBtn, salvaBtn, sessioniBtn, chiudiBtn);

        // Montaggio card
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        card.getChildren().clear();
        card.getChildren().add(headerBox);

        grid.add(new Label("Nome:"), 0, 0);
        grid.add(nomeField, 1, 0);

        grid.add(new Label("Prezzo:"), 0, 1);
        grid.add(prezzoField, 1, 1);

        grid.add(new Label("Argomento:"), 0, 2);
        grid.add(argomentoField, 1, 2);

        grid.add(new Label("Frequenza:"), 0, 3);
        grid.add(frequenzaCombo, 1, 3);

        grid.add(new Label("Numero posti:"), 0, 4);
        grid.add(numeroPostiField, 1, 4);

        grid.add(new Label("Numero sessioni:"), 0, 5);
        grid.add(numeroSessioniField, 1, 5);

        grid.add(new Label("Data inizio:"), 0, 6);
        grid.add(dataInizioPicker, 1, 6);

        grid.add(new Label("Data fine:"), 0, 7);
        grid.add(dataFinePicker, 1, 7);

        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(140);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c0, c1);

        card.getChildren().addAll(grid, new Separator(), new Label("Chef assegnati:"), chefListView,
                selezionatoLabel, new Label("Aggiungi uno chef dal sistema:"), addBox, buttons);

        setEditable(false);
        refreshChefListView();

        scrollPane = new ScrollPane(card);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPadding(new Insets(12));
        return scrollPane;
    }

    private String safeString(String s) {
        return s == null ? "" : s;
    }

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

        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                "Rimuovere " + chef.getUsername() + " dal corso?", ButtonType.OK, ButtonType.CANCEL);
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

        String borderColor = edit ? "#FF9966" : "#DDDDDD";
        String borderWidth = edit ? "2" : "1";

        card.setOpacity(edit ? 1.0 : 0.95);
        card.setStyle("-fx-background-color: white;" +
                "-fx-background-radius: 20;" +
                "-fx-border-radius: 20;" +
                "-fx-border-color: " + borderColor + ";" +
                "-fx-border-width: " + borderWidth + ";");

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

    public void start(Stage stage) {
        ScrollPane root = getRoot();
        Scene scene = new Scene(root, 900, 700);
        stage.setTitle("Dettagli Corso - " + safeString(corso.getNomeCorso()));
        stage.setScene(scene);
        stage.show();
    }
}