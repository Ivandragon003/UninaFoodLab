package Gui;

import controller.GestioneCorsoController;
import model.CorsoCucina;
import model.Frequenza;
import model.Chef;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;


import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CreaCorsoGUI {

    private GestioneCorsoController gestioneController;


    // Manteniamo riferimento al corso salvato (se salvato)
    private CorsoCucina corsoSalvato = null;

    public void setController(GestioneCorsoController controller) {
        this.gestioneController = controller;
    }

    public void start(Stage stage) {
        if (gestioneController == null) {
            throw new IllegalStateException("Controller non impostato!");
        }

        stage.setTitle("Crea Nuovo Corso");

        // Campi del form
        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome corso");

        TextField argomentoField = new TextField();
        argomentoField.setPromptText("Argomento");

        TextField prezzoField = new TextField();
        prezzoField.setPromptText("Prezzo (es: 150,00)");

        // Pulizia input prezzo (lasciamo all'utente inserire virgola o punto)
        prezzoField.textProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) return;
            String cleaned = newV.replaceAll("[^0-9,\\.]", "");
            if (!cleaned.equals(newV)) prezzoField.setText(cleaned);
        });

        ComboBox<Frequenza> frequenzaBox = new ComboBox<>();
        frequenzaBox.getItems().addAll(Frequenza.values());

        TextField postiField = new TextField();
        postiField.setPromptText("Numero posti");

        TextField numSessioniField = new TextField();
        numSessioniField.setPromptText("Numero sessioni");

        DatePicker dataInizioPicker = new DatePicker();
        DatePicker dataFinePicker = new DatePicker();

        // ComboBox chef
        ComboBox<Chef> chefComboBox = new ComboBox<>();
        try {
            List<Chef> chefDisponibili = gestioneController.getTuttiGliChef();
            chefComboBox.getItems().addAll(chefDisponibili);

            // Se il controller ha un chef loggato, impostalo come default
            Chef chefLoggato = gestioneController.getChefLoggato();
            if (chefLoggato != null && !chefComboBox.getItems().contains(chefLoggato)) {
                chefComboBox.getItems().add(0, chefLoggato);
            }
            if (chefLoggato != null) chefComboBox.setValue(chefLoggato);

            chefComboBox.setConverter(new StringConverter<Chef>() {
                @Override
                public String toString(Chef chef) {
                    return chef != null ? chef.getUsername() : "";
                }

                @Override
                public Chef fromString(String string) {
                    return chefComboBox.getItems().stream()
                            .filter(c -> c.getUsername().equals(string))
                            .findFirst().orElse(null);
                }
            });

        } catch (Exception ex) {
            showAlert("Errore", "Impossibile caricare la lista chef: " + ex.getMessage());
        }

        Button salvaBtn = new Button("Salva corso");
        Button gestisciSessioniBtn = new Button("Gestisci sessioni");
        Button annullaBtn = new Button("Annulla");

        ListView<String> sessioniList = new ListView<>();
        sessioniList.setPrefHeight(120);

        VBox formBox = new VBox(10);
        formBox.setPadding(new Insets(16));
        formBox.getChildren().addAll(
                new Label("Nome:"), nomeField,
                new Label("Argomento:"), argomentoField,
                new Label("Prezzo:"), prezzoField,
                new Label("Frequenza:"), frequenzaBox,
                new Label("Numero posti:"), postiField,
                new Label("Numero sessioni:"), numSessioniField,
                new Label("Data inizio:"), dataInizioPicker,
                new Label("Data fine:"), dataFinePicker,
                new Label("Chef responsabile:"), chefComboBox,
                new Label("Sessioni create:"), sessioniList,
                gestisciSessioniBtn,
                salvaBtn, annullaBtn
        );

        ScrollPane sp = new ScrollPane(formBox);
        sp.setFitToWidth(true);

        stage.setScene(new Scene(sp, 480, 640));
        stage.show();

        // inizializza lista sessioni vuota
        aggiornaListaSessioni(sessioniList, null);

        // Gestisci sessioni: salva corso (se non salvato) -> apri GestioneSessioniGUI
        gestisciSessioniBtn.setOnAction(e -> {
            if (!validaCampiBase(nomeField, argomentoField, prezzoField, frequenzaBox,
                    postiField, numSessioniField, dataInizioPicker, dataFinePicker, chefComboBox)) {
                return;
            }

            try {
                // Se non abbiamo già corso salvato, crealo e salvalo
                if (corsoSalvato == null) {
                    CorsoCucina corso = creaCorsoDaCampi(nomeField, argomentoField, prezzoField, frequenzaBox,
                            postiField, numSessioniField, dataInizioPicker, dataFinePicker, chefComboBox);

                    // salva corso
                    gestioneController.creaCorso(corso);

                    // associa chef se selezionato
                    Chef selezionato = chefComboBox.getValue();
                    if (selezionato != null) {
                        gestioneController.aggiungiChefACorso(corso, selezionato, selezionato.getPassword());
                    }

                    // salva riferimento al corso persistito (l'oggetto corso ora contiene sessioni vuote)
                    corsoSalvato = corso;
                }

                // Apri GUI gestione sessioni (la tua GestioneSessioniGUI)
                GestioneSessioniGUI sessioniGUI = new GestioneSessioniGUI();
                sessioniGUI.setCorso(corsoSalvato);
                sessioniGUI.start(new Stage());

                // Aggiorna vista sessioni appena ritorna
                aggiornaListaSessioni(sessioniList, corsoSalvato);

            } catch (Exception ex) {
                showAlert("Errore", "Impossibile aprire gestione sessioni: " + ex.getMessage());
            }
        });

        // Salva corso (richiesta finale) — controlli e conferma
        salvaBtn.setOnAction(e -> {
            if (!validaCampiCompleti(nomeField, argomentoField, prezzoField, frequenzaBox,
                    postiField, numSessioniField, dataInizioPicker, dataFinePicker, chefComboBox)) {
                return;
            }

            try {
                // Se non era già salvato, crealo e salvalo
                if (corsoSalvato == null) {
                    CorsoCucina corso = creaCorsoDaCampi(nomeField, argomentoField, prezzoField, frequenzaBox,
                            postiField, numSessioniField, dataInizioPicker, dataFinePicker, chefComboBox);

                    gestioneController.creaCorso(corso);

                    Chef selezionato = chefComboBox.getValue();
                    if (selezionato != null) {
                        gestioneController.aggiungiChefACorso(corso, selezionato, selezionato.getPassword());
                    }
                    corsoSalvato = corso;
                }

                // Verifica numero sessioni: dichiarate vs create
                int dichiarate = Integer.parseInt(numSessioniField.getText().trim());
                int create = gestioneController.contaSessioniCorso(corsoSalvato);

                if (create != dichiarate) {
                    showAlert("Errore di validazione",
                            String.format("Numero sessioni non corrispondente:\nDichiarate: %d\nCreate: %d\n\nUsa 'Gestisci sessioni' per aggiungere/eliminare sessioni.",
                                    dichiarate, create));
                    return;
                }

                // Conferma finale
                Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
                conferma.setTitle("Conferma creazione");
                conferma.setHeaderText("Sei sicuro di voler creare questo corso?");
                String dettagli = String.format("Nome: %s\nArgomento: %s\nPrezzo: %s\nPosti: %s\nSessioni: %d\nChef: %s",
                        nomeField.getText().trim(),
                        argomentoField.getText().trim(),
                        formatPrezzo(parsePrezzo(prezzoField.getText())),
                        postiField.getText().trim(),
                        dichiarate,
                        (chefComboBox.getValue() != null ? chefComboBox.getValue().getUsername() : "Nessuno"));
                conferma.setContentText(dettagli);

                conferma.showAndWait().ifPresent(resp -> {
                    if (resp == ButtonType.OK) {
                        // messaggio di successo e chiusura
                        showAlert("Successo", "Corso creato correttamente!");
                        stage.close();
                    }
                });

            } catch (NumberFormatException ex) {
                showAlert("Errore", "Valori numerici non validi: " + ex.getMessage());
            } catch (Exception ex) {
                showAlert("Errore", "Errore durante il salvataggio: " + ex.getMessage());
            }
        });

        annullaBtn.setOnAction(e -> stage.close());
    }

    // ---------------- helper ----------------

    private CorsoCucina creaCorsoDaCampi(TextField nomeField, TextField argomentoField, TextField prezzoField,
                                        ComboBox<Frequenza> frequenzaBox, TextField postiField, TextField numSessioniField,
                                        DatePicker dataInizioPicker, DatePicker dataFinePicker, ComboBox<Chef> chefComboBox) throws Exception {

        double prezzo = parsePrezzo(prezzoField.getText());
        CorsoCucina corso = new CorsoCucina(
                nomeField.getText().trim(),
                prezzo,
                argomentoField.getText().trim(),
                frequenzaBox.getValue(),
                Integer.parseInt(postiField.getText().trim())
        );
        if (dataInizioPicker.getValue() != null)
            corso.setDataInizioCorso(dataInizioPicker.getValue().atStartOfDay());
        if (dataFinePicker.getValue() != null)
            corso.setDataFineCorso(dataFinePicker.getValue().atStartOfDay());

        // se esiste chef selezionato, aggiungilo all'oggetto corso (lista in memoria)
        Chef selezionato = chefComboBox.getValue();
        if (selezionato != null) corso.getChef().add(selezionato);

        return corso;
    }

    private double parsePrezzo(String txt) throws NumberFormatException {
        if (txt == null) throw new NumberFormatException("Prezzo vuoto");
        // normalizza: virgola -> punto, rimuovi tutto eccetto 0-9 e '.'
        String s = txt.replace(',', '.').replaceAll("[^0-9.]", "");
        return Double.parseDouble(s);
    }

    private String formatPrezzo(double prezzo) {
        // ritorna con virgola come in Italia
        NumberFormat nf = NumberFormat.getInstance(Locale.ITALY);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(prezzo);
    }

    private boolean validaCampiBase(TextField nome, TextField argomento, TextField prezzo,
                                    ComboBox<Frequenza> frequenza, TextField posti, TextField sessioni,
                                    DatePicker dataInizio, DatePicker dataFine, ComboBox<Chef> chef) {

        if (nome.getText().trim().isEmpty() ||
                argomento.getText().trim().isEmpty() ||
                prezzo.getText().trim().isEmpty() ||
                posti.getText().trim().isEmpty() ||
                sessioni.getText().trim().isEmpty() ||
                frequenza.getValue() == null ||
                dataInizio.getValue() == null ||
                dataFine.getValue() == null ||
                chef.getValue() == null) {
            showAlert("Errore di validazione", "Compila tutti i campi obbligatori.");
            return false;
        }

        try {
            parsePrezzo(prezzo.getText());
            Integer.parseInt(posti.getText().trim());
            Integer.parseInt(sessioni.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Errore", "Inserisci valori numerici validi per prezzo, posti e sessioni.");
            return false;
        }

        return true;
    }

    private boolean validaCampiCompleti(TextField nome, TextField argomento, TextField prezzo,
                                        ComboBox<Frequenza> frequenza, TextField posti, TextField sessioni,
                                        DatePicker dataInizio, DatePicker dataFine, ComboBox<Chef> chef) {
        if (!validaCampiBase(nome, argomento, prezzo, frequenza, posti, sessioni, dataInizio, dataFine, chef)) return false;
        if (dataInizio.getValue().isAfter(dataFine.getValue())) {
            showAlert("Errore", "La data di inizio deve precedere la data di fine.");
            return false;
        }
        try {
            double p = parsePrezzo(prezzo.getText());
            if (p <= 0) { showAlert("Errore", "Prezzo deve essere > 0"); return false; }
            if (Integer.parseInt(posti.getText().trim()) <= 0) { showAlert("Errore", "Posti > 0"); return false; }
            if (Integer.parseInt(sessioni.getText().trim()) <= 0) { showAlert("Errore", "Sessioni > 0"); return false; }
        } catch (Exception ex) {
            showAlert("Errore", "Valori numerici non validi.");
            return false;
        }
        return true;
    }

    private void aggiornaListaSessioni(ListView<String> listView, CorsoCucina corso) {
        listView.getItems().clear();
        if (corso == null) {
            listView.getItems().add("Nessuna sessione creata");
            return;
        }
        var descr = gestioneController.getDescrizioniSessioni(corso);
        if (descr.isEmpty()) listView.getItems().add("Nessuna sessione creata");
        else listView.getItems().setAll(descr);
    }

    private void showAlert(String titolo, String messaggio) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titolo);
        a.setHeaderText(null);
        a.setContentText(messaggio);
        a.showAndWait();
    }
}
