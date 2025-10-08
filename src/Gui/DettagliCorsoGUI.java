package Gui;

import controller.GestioneCorsoController;
import controller.GestioneSessioniController;
import dao.*;
import service.*;
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
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.time.LocalDate;
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
        title.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        nomeField = StyleHelper.createTextField(safeString(corso.getNomeCorso()));
        nomeField.setText(safeString(corso.getNomeCorso()));
        
        prezzoField = StyleHelper.createTextField(String.valueOf(corso.getPrezzo()));
        prezzoField.setText(String.valueOf(corso.getPrezzo()));
        
        argomentoField = StyleHelper.createTextField(safeString(corso.getArgomento()));
        argomentoField.setText(safeString(corso.getArgomento()));

        frequenzaCombo = StyleHelper.createComboBox();
        frequenzaCombo.getItems().setAll(Frequenza.values());
        frequenzaCombo.setValue(corso.getFrequenzaCorso());
        frequenzaCombo.setOnAction(e -> onFrequenzaChange());

        numeroPostiField = StyleHelper.createTextField(String.valueOf(corso.getNumeroPosti()));
        numeroPostiField.setText(String.valueOf(corso.getNumeroPosti()));

        numeroSessioniField = StyleHelper.createTextField(
                corso.getSessioni() != null ? String.valueOf(corso.getSessioni().size()) : "0"
        );
        numeroSessioniField.setText(corso.getSessioni() != null ? String.valueOf(corso.getSessioni().size()) : "0");
        numeroSessioniField.setEditable(false);
        numeroSessioniField.setStyle("-fx-control-inner-background: #E9ECEF;");

        dataInizioPicker = StyleHelper.createDatePicker();
        dataInizioPicker.setValue(
                corso.getDataInizioCorso() != null ? corso.getDataInizioCorso().toLocalDate() : null
        );
        dataInizioPicker.setOnAction(e -> onDataInizioChange());
        
        dataFinePicker = StyleHelper.createDatePicker();
        dataFinePicker.setValue(
                corso.getDataFineCorso() != null ? corso.getDataFineCorso().toLocalDate() : null
        );
        dataFinePicker.setOnAction(e -> aggiornaFrequenzeDisponibili());

        chefListView = new ListView<>();
        chefListView.setPrefHeight(150);
        chefListView.setMinHeight(100);
        chefListView.setMaxHeight(250);
        chefListView.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: " + StyleHelper.BORDER_LIGHT + ";" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-border-width: 1;"
        );
        
        chefListView.setCellFactory(lv -> new ListCell<>() {
            private final HBox box = new HBox(8);
            private final Label nameLabel = new Label();
            private final Label meLabel = new Label(" (io)");
            private final Button removeBtn = new Button("Rimuovi");

            {
                meLabel.setStyle("-fx-text-fill: " + StyleHelper.PRIMARY_ORANGE + "; -fx-font-weight: bold;");
                removeBtn.setStyle(
                    "-fx-background-radius: 8;" +
                    "-fx-background-color: " + StyleHelper.ERROR_RED + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-cursor: hand;"
                );
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
                    nameLabel.setText(item.getNome() + " " + item.getCognome());
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
            if (newV == null) {
                selezionatoLabel.setText("Selezionato: nessuno");
            } else {
                selezionatoLabel.setText("Selezionato: " + newV.getNome() + " " + newV.getCognome() + 
                                        (isChefLoggato(newV) ? " (io)" : ""));
            }
        });

        addChefCombo = StyleHelper.createComboBox();
        addChefCombo.setPrefWidth(300);
        
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
        addChefBtn.setOnAction(e -> {
            Chef toAdd = addChefCombo.getValue();
            if (toAdd == null) {
                StyleHelper.showValidationDialog("Attenzione", "Seleziona uno chef dalla lista");
                return;
            }
            aggiungiChef(toAdd, null);
        });

        HBox addBox = new HBox(10, addChefCombo, addChefBtn);
        addBox.setAlignment(Pos.CENTER_LEFT);

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

        modificaBtn = StyleHelper.createInfoButton("✏️ Modifica");
        modificaBtn.setPrefWidth(160);
        
        salvaBtn = StyleHelper.createSuccessButton("💾 Salva");
        salvaBtn.setPrefWidth(160);
        salvaBtn.setDisable(true);
        
        Button visualizzaSessioniBtn = StyleHelper.createPrimaryButton("👁️ Sessioni");
        visualizzaSessioniBtn.setPrefWidth(160);
        
        Button chiudiBtn = StyleHelper.createStyledButton("❌ Chiudi", StyleHelper.NEUTRAL_GRAY);
        chiudiBtn.setPrefWidth(160);

        modificaBtn.setOnAction(e -> {
            setEditable(true);
            salvaBtn.setDisable(false);
            modificaBtn.setDisable(true);
        });

        salvaBtn.setOnAction(e -> salvaModifiche());
        visualizzaSessioniBtn.setOnAction(e -> apriVisualizzaSessioni());
        chiudiBtn.setOnAction(e -> tornaAllaListaCorsi());

        buttons.getChildren().addAll(modificaBtn, salvaBtn, visualizzaSessioniBtn, chiudiBtn);

        card.getChildren().addAll(
                title,
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
        refreshChefListView();
        aggiornaStatoDataFine();

        VBox wrapper = new VBox(card);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setPadding(new Insets(20));
        scrollPane.setContent(wrapper);

        mainContainer.getChildren().add(scrollPane);
        return mainContainer;
    }

    private void onDataInizioChange() {
        LocalDate inizio = dataInizioPicker.getValue();
        
        if (frequenzaCombo.getValue() == Frequenza.UNICA && inizio != null) {
            dataFinePicker.setValue(inizio);
        }
    }

    private void onFrequenzaChange() {
        if (!editable) return;
        
        Frequenza selezionata = frequenzaCombo.getValue();
        
        if (selezionata == Frequenza.UNICA) {
            if (dataInizioPicker.getValue() != null) {
                dataFinePicker.setValue(dataInizioPicker.getValue());
            }
            dataFinePicker.setDisable(true);
            dataFinePicker.setStyle("-fx-opacity: 0.6;");
        } else {
            dataFinePicker.setDisable(false);
            dataFinePicker.setStyle("");
        }
    }

    private void aggiornaFrequenzeDisponibili() {
        if (!editable) return;
        
        LocalDate inizio = dataInizioPicker.getValue();
        LocalDate fine = dataFinePicker.getValue();
        
        if (inizio != null && fine != null) {
            List<Frequenza> disponibili = FrequenzaHelper.getFrequenzeDisponibili(inizio, fine);
            
            Frequenza attuale = frequenzaCombo.getValue();
            frequenzaCombo.getItems().setAll(disponibili);
            
            if (disponibili.contains(attuale)) {
                frequenzaCombo.setValue(attuale);
            } else if (!disponibili.isEmpty()) {
                frequenzaCombo.setValue(disponibili.get(0));
            }
        }
    }

    private void aggiornaStatoDataFine() {
        if (corso.getFrequenzaCorso() == Frequenza.UNICA) {
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
            if (stage != null) stage.close();
        }
    }

    private void salvaModifiche() {
        try {
            double prezzo = Double.parseDouble(prezzoField.getText().replace(',', '.'));
            int posti = Integer.parseInt(numeroPostiField.getText());

            if (dataInizioPicker.getValue() != null && dataFinePicker.getValue() != null &&
                    dataInizioPicker.getValue().isAfter(dataFinePicker.getValue())) {
                StyleHelper.showValidationDialog("Errore", "La data di inizio deve precedere la data di fine");
                return;
            }

            Frequenza freqSelezionata = frequenzaCombo.getValue();
            if (dataInizioPicker.getValue() != null && dataFinePicker.getValue() != null && 
                freqSelezionata != null) {
                if (!FrequenzaHelper.isFrequenzaValida(
                        dataInizioPicker.getValue(), 
                        dataFinePicker.getValue(), 
                        freqSelezionata)) {
                    StyleHelper.showValidationDialog("Frequenza non valida", 
                        FrequenzaHelper.getMessaggioErroreFrequenza(
                            dataInizioPicker.getValue(), 
                            dataFinePicker.getValue(), 
                            freqSelezionata
                        ));
                    return;
                }
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

            StyleHelper.showSuccessDialog("Successo", "Corso modificato correttamente!");
            setEditable(false);
            salvaBtn.setDisable(true);
            modificaBtn.setDisable(false);
            refreshChefListView();

        } catch (ValidationException ex) {
            StyleHelper.showValidationDialog("Errore di validazione", ex.getMessage());
        } catch (DataAccessException ex) {
            StyleHelper.showErrorDialog("Errore Database", ex.getMessage());
        } catch (NumberFormatException ex) {
            StyleHelper.showValidationDialog("Errore", "Valori numerici non validi per prezzo o posti");
        } catch (Exception ex) {
            StyleHelper.showErrorDialog("Errore", "Errore nel salvataggio: " + ex.getMessage());
        }
    }

    private void apriVisualizzaSessioni() {
        try {
            CorsoCucina corsoCompleto = gestioneController.getCorsoCompleto(corso.getIdCorso());
            if (corsoCompleto != null && corsoCompleto.getSessioni() != null) {
                corso.setSessioni(corsoCompleto.getSessioni());
            }
            
            GestioneSessioniController sessioniController = creaSessioniController();
            
            VisualizzaSessioniGUI sessioniGUI = new VisualizzaSessioniGUI();
            sessioniGUI.setCorso(corso);
            sessioniGUI.setController(sessioniController);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("📅 Sessioni - " + safeString(corso.getNomeCorso()));
            stage.setScene(new Scene(sessioniGUI.getRoot(), 1000, 750));
            stage.showAndWait();

            CorsoCucina corsoAggiornato = gestioneController.getCorsoCompleto(corso.getIdCorso());
            if (corsoAggiornato != null && corsoAggiornato.getSessioni() != null) {
                corso.setSessioni(corsoAggiornato.getSessioni());
                corso.setNumeroSessioni(corsoAggiornato.getSessioni().size());
                numeroSessioniField.setText(String.valueOf(corso.getNumeroSessioni()));
            }
            
        } catch (Exception ex) {
            StyleHelper.showErrorDialog("Errore", "Impossibile aprire gestione sessioni: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private GestioneSessioniController creaSessioniController() {
        try {
            OnlineDAO onlineDAO = new OnlineDAO();
            InPresenzaDAO inPresenzaDAO = new InPresenzaDAO();
            RicettaDAO ricettaDAO = new RicettaDAO();
            CucinaDAO cucinaDAO = new CucinaDAO();
            AdesioneDAO adesioneDAO = new AdesioneDAO();
            UsaDAO usaDAO = new UsaDAO();
            IngredienteDAO ingredienteDAO = new IngredienteDAO();
            
            GestioneSessioni gestioneSessioni = new GestioneSessioni(
                inPresenzaDAO,
                onlineDAO,
                adesioneDAO,
                cucinaDAO
            );
            
            GestioneCucina gestioneCucina = new GestioneCucina(cucinaDAO);
            
            GestioneRicette gestioneRicette = new GestioneRicette(ricettaDAO);
            
            return new GestioneSessioniController(
                corso,              
                gestioneSessioni,   
                gestioneCucina,     
                gestioneRicette     
            );
            
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Impossibile creare il controller sessioni: " + e.getMessage());
            e.printStackTrace();
            return null;
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
            lista.sort(Comparator.comparing((Chef ch) -> !isChefLoggato(ch))
                                 .thenComparing(Chef::getCognome)
                                 .thenComparing(Chef::getNome));
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
            StyleHelper.showValidationDialog("Operazione non permessa", 
                "Non puoi rimuovere te stesso dall'elenco");
            return;
        }

        StyleHelper.showConfirmationDialog(
            "Conferma Rimozione",
            "Rimuovere " + chef.getNome() + " " + chef.getCognome() + " dal corso?",
            () -> {
                try {
                    gestioneController.rimuoviChefDaCorso(corso, chef);
                    corso.getChef().remove(chef);
                    refreshChefListView();
                    StyleHelper.showSuccessDialog("Rimosso", "Chef rimosso correttamente");
                } catch (ValidationException ex) {
                    StyleHelper.showValidationDialog("Errore", ex.getMessage());
                } catch (DataAccessException ex) {
                    StyleHelper.showErrorDialog("Errore Database", ex.getMessage());
                } catch (Exception ex) {
                    StyleHelper.showErrorDialog("Errore", ex.getMessage());
                }
            }
        );
    }

    private void aggiungiChef(Chef chef, String password) {
        if (!editable) return;
        try {
            gestioneController.aggiungiChefACorso(corso, chef, password);
            if (corso.getChef() == null) corso.setChef(new ArrayList<>());
            if (!corso.getChef().contains(chef)) corso.getChef().add(chef);
            refreshChefListView();
            StyleHelper.showSuccessDialog("Aggiunto", "Chef aggiunto correttamente");
        } catch (ValidationException ex) {
            StyleHelper.showValidationDialog("Attenzione", ex.getMessage());
        } catch (DataAccessException ex) {
            StyleHelper.showErrorDialog("Errore Database", ex.getMessage());
        } catch (Exception ex) {
            StyleHelper.showErrorDialog("Errore", ex.getMessage());
        }
    }

    private void setEditable(boolean edit) {
        this.editable = edit;
        LocalDate oggi = LocalDate.now();
        LocalDate dataInizio = corso.getDataInizioCorso() != null 
            ? corso.getDataInizioCorso().toLocalDate() 
            : null;
        
        boolean corsoGiaIniziato = dataInizio != null && dataInizio.isBefore(oggi);
        
        nomeField.setEditable(edit);
        prezzoField.setEditable(edit);
        argomentoField.setEditable(edit);
        frequenzaCombo.setDisable(!edit);
        numeroPostiField.setEditable(edit);
        
        dataInizioPicker.setDisable(!edit || corsoGiaIniziato);
        
        if (edit && frequenzaCombo.getValue() == Frequenza.UNICA) {
            dataFinePicker.setDisable(true);
        } else {
            dataFinePicker.setDisable(!edit);
        }
        
        addChefCombo.setDisable(!edit);
        addChefBtn.setDisable(!edit);

        String borderColor = edit ? StyleHelper.PRIMARY_ORANGE : StyleHelper.BORDER_LIGHT;
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16;" +
                "-fx-border-radius: 16; -fx-border-color: " + borderColor + "; -fx-border-width: 2;");

        refreshChefListView();
    }
}
