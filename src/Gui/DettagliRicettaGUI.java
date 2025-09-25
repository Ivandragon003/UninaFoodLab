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

        ListView<String> ingredientiList = new ListView<>();
        ricetta.getIngredienti().forEach((i, q) ->
                ingredientiList.getItems().add(i.getNome() + " (" + i.getTipo() + ") - " + q)
        );

        Button btnAggiungi = new Button("Aggiungi Ingrediente");
        btnAggiungi.setOnAction(e -> {
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Aggiungi Ingrediente");

            TextField nomeIng = new TextField();
            nomeIng.setPromptText("Nome ingrediente");
            TextField tipoIng = new TextField();
            tipoIng.setPromptText("Tipo ingrediente");
            TextField quantitaIng = new TextField();
            quantitaIng.setPromptText("Quantità");

            VBox box = new VBox(10, nomeIng, tipoIng, quantitaIng);
            dialog.getDialogPane().setContent(box);

            ButtonType conferma = new ButtonType("Conferma", ButtonBar.ButtonData.OK_DONE);
            ButtonType annulla = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(conferma, annulla);

            dialog.setResultConverter(bt -> {
                if (bt == conferma) {
                    try {
                        Ingrediente i = new Ingrediente(nomeIng.getText(), tipoIng.getText());
                        double q = Double.parseDouble(quantitaIng.getText());
                        ricetta.getIngredienti().put(i, q);
                        gestioneRicette.aggiornaRicetta(ricetta.getIdRicetta(), ricetta);
                        ingredientiList.getItems().add(i.getNome() + " (" + i.getTipo() + ") - " + q);
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

        root.getChildren().addAll(lblNome, lblTempo, ingredientiList, btnAggiungi, btnElimina);

        Scene scene = new Scene(root, 400, 400);
        stage.setScene(scene);
        stage.show();
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
