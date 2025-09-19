package Gui;

import controller.CorsiController;
import controller.VisualizzaCorsiController;
import model.CorsoCucina;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class VisualizzaCorsiGUI {

    private CorsiController corsiController;
    private VisualizzaCorsiController visualizzaController;

    public void setControllers(CorsiController corsiController, VisualizzaCorsiController visualizzaController) {
        this.corsiController = corsiController;
        this.visualizzaController = visualizzaController;
    }

    public void start(Stage stage) {
        if (corsiController == null || visualizzaController == null) {
            throw new IllegalStateException("Controllers non impostati!");
        }

        stage.setTitle("Gestione Corsi");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);

        Button tuttiBtn = new Button("Mostra tutti i corsi");
        Button mieiBtn = new Button("Corsi miei");
        Button cercaBtn = new Button("Cerca corso");
        TextArea risultatiArea = new TextArea();
        risultatiArea.setPrefHeight(300);

        grid.add(tuttiBtn, 0, 0);
        grid.add(mieiBtn, 1, 0);
        grid.add(cercaBtn, 2, 0);
        grid.add(risultatiArea, 0, 1, 3, 1);

        // MOSTRA TUTTI I CORSI
        tuttiBtn.setOnAction(e -> {
            try {
                List<CorsoCucina> corsi = visualizzaController.getTuttiICorsi();
                risultatiArea.clear();
                corsi.forEach(c -> risultatiArea.appendText(c.toStringNomeCorso() + " | ID: " + c.getIdCorso() + "\n"));
            } catch (Exception ex) {
                risultatiArea.setText("Errore nel caricamento dei corsi: " + ex.getMessage());
            }
        });

        // MOSTRA CORSI MIEI
        mieiBtn.setOnAction(e -> {
            try {
                List<CorsoCucina> corsi = visualizzaController.getCorsiChefLoggato();
                risultatiArea.clear();
                corsi.forEach(c -> risultatiArea.appendText(c.toStringNomeCorso() + " | ID: " + c.getIdCorso() + "\n"));
            } catch (Exception ex) {
                risultatiArea.setText("Errore nel caricamento dei corsi: " + ex.getMessage());
            }
        });

        // CERCA CORSO
        cercaBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setHeaderText("Cerca corso per nome, chef o categoria");
            dialog.setContentText("Inserisci il testo da cercare:");
            dialog.showAndWait().ifPresent(query -> {
                try {
                    Set<CorsoCucina> risultati = new LinkedHashSet<>();
                    risultati.addAll(visualizzaController.cercaPerNomeCorso(query));
                    risultati.addAll(visualizzaController.cercaPerNomeChef(query));
                    risultati.addAll(visualizzaController.cercaPerCategoria(query));

                    risultatiArea.clear();
                    if (risultati.isEmpty()) {
                        risultatiArea.setText("Nessun corso trovato.");
                    } else {
                        risultati.forEach(c -> risultatiArea.appendText(c.toStringNomeCorso() + " | ID: " + c.getIdCorso() + "\n"));
                    }
                } catch (Exception ex) {
                    risultatiArea.setText("Errore nella ricerca: " + ex.getMessage());
                }
            });
        });

        // Selezione corso tramite ID
        TextField idField = new TextField();
        Button dettagliBtn = new Button("Mostra dettagli / Modifica / Elimina");
        grid.add(new Label("ID corso:"), 0, 2);
        grid.add(idField, 1, 2);
        grid.add(dettagliBtn, 2, 2);

        dettagliBtn.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText());
                CorsoCucina corso = corsiController.getCorsoById(id);
                if (corso == null) {
                    risultatiArea.setText("Corso non trovato.");
                    return;
                }

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Dettagli corso");
                alert.setHeaderText(corso.toStringNomeCorso() + "\nCategoria: " + corso.getCategoria());
                alert.setContentText("Vuoi modificare o eliminare questo corso?");

                ButtonType modifica = new ButtonType("Modifica");
                ButtonType elimina = new ButtonType("Elimina");
                ButtonType cancella = new ButtonType("Annulla");
                alert.getButtonTypes().setAll(modifica, elimina, cancella);

                alert.showAndWait().ifPresent(response -> {
                    try {
                        if (response == modifica) {
                            corso.setNomeCorso(corso.getNomeCorso() + " (modificato)");
                            corsiController.modificaCorso(corso);
                            risultatiArea.setText("Corso modificato con successo!");
                        } else if (response == elimina) {
                            corsiController.eliminaCorso(corso.getIdCorso());
                            risultatiArea.setText("Corso eliminato con successo!");
                        }
                    } catch (Exception ex) {
                        risultatiArea.setText("Errore: " + ex.getMessage());
                    }
                });

            } catch (NumberFormatException ex) {
                risultatiArea.setText("Inserisci un ID valido!");
            } catch (Exception ex) {
                risultatiArea.setText("Errore: " + ex.getMessage());
            }
        });

        Scene scene = new Scene(grid, 700, 500);
        stage.setScene(scene);
        stage.show();
    }
}
