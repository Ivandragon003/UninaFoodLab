package Gui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Ingrediente;
import model.Ricetta;
import service.GestioneRicette;

import java.sql.SQLException;
import java.util.Map;

public class DettagliRicettaGUI {

    private final GestioneRicette gestioneRicette;
    private final Ricetta ricetta;

    public DettagliRicettaGUI(GestioneRicette gestioneRicette, Ricetta ricetta) {
        this.gestioneRicette = gestioneRicette;
        this.ricetta = ricetta;
    }

    public void start(Stage stage) {
        stage.setTitle("Dettagli Ricetta");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Label lblNome = new Label(ricetta.toStringNome());
        Label lblTempo = new Label(ricetta.toStringTempoPreparazione());

        VBox listaBox = new VBox(5);
        aggiornaListaIngredienti(listaBox);

        Button btnAggiungi = new Button("Aggiungi Ingrediente");
        btnAggiungi.setOnAction(e -> {
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Aggiungi Ingrediente");

            ComboBox<Ingrediente> comboEsistenti = new ComboBox<>();
            try {
                comboEsistenti.getItems().addAll(gestioneRicette.getAllIngredienti());
            } catch (SQLException ex) {
                showError("Errore caricamento ingredienti: " + ex.getMessage());
            }
            comboEsistenti.setPromptText("Seleziona ingrediente esistente");

            TextField nomeIng = new TextField();
            nomeIng.setPromptText("Nome nuovo ingrediente");
            TextField tipoIng = new TextField();
            tipoIng.setPromptText("Tipo nuovo ingrediente");
            TextField quantitaIng = new TextField();
            quantitaIng.setPromptText("Quantità");

            VBox box = new VBox(10, comboEsistenti, nomeIng, tipoIng, quantitaIng);
            dialog.getDialogPane().setContent(box);

            ButtonType conferma = new ButtonType("Conferma", ButtonBar.ButtonData.OK_DONE);
            ButtonType annulla = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(conferma, annulla);

            dialog.setResultConverter(bt -> {
                if (bt == conferma) {
                    try {
                        Ingrediente i;
                        if (comboEsistenti.getValue() != null) {
                            i = comboEsistenti.getValue();
                        } else {
                            i = new Ingrediente(nomeIng.getText(), tipoIng.getText());
                            gestioneRicette.creaIngrediente(i);
                        }
                        double q = Double.parseDouble(quantitaIng.getText());
                        gestioneRicette.aggiungiIngrediente(ricetta, i, q);
                        aggiornaListaIngredienti(listaBox);
                    } catch (Exception ex) {
                        showError("Errore: " + ex.getMessage());
                    }
                }
                return null;
            });

            dialog.showAndWait();
        });

        Button btnElimina = new Button("Elimina Ricetta");
        btnElimina.setOnAction(e -> {
            try {
                gestioneRicette.cancellaRicetta(ricetta.getIdRicetta());
                showInfo("Ricetta eliminata con successo!");
                stage.close();
            } catch (SQLException ex) {
                showError("Errore: " + ex.getMessage());
            }
        });

        root.getChildren().addAll(lblNome, lblTempo, new Label("Ingredienti:"), listaBox, btnAggiungi, btnElimina);

        Scene scene = new Scene(root, 500, 400);
        stage.setScene(scene);
        stage.show();
    }

    private void aggiornaListaIngredienti(VBox listaBox) {
        listaBox.getChildren().clear();
        for (Map.Entry<Ingrediente, Double> entry : ricetta.getIngredienti().entrySet()) {
            HBox row = new HBox(10);
            Label lbl = new Label(entry.getKey().getNome() + " (" + entry.getKey().getTipo() + ") - " + entry.getValue());
            Button btnRimuovi = new Button("X");
            btnRimuovi.setOnAction(e -> {
                try {
                    gestioneRicette.rimuoviIngrediente(ricetta, entry.getKey());
                    aggiornaListaIngredienti(listaBox);
                } catch (Exception ex) {
                    showError("Errore rimozione: " + ex.getMessage());
                }
            });
            row.getChildren().addAll(lbl, btnRimuovi);
            listaBox.getChildren().add(row);
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
