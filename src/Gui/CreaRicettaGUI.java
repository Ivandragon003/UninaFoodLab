package Gui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Ingrediente;
import model.Ricetta;
import service.GestioneRicette;
import service.GestioneCucina;
import model.InPresenza;

import java.util.HashMap;
import java.util.Map;

public class CreaRicettaGUI {

    private final GestioneRicette gestioneRicette;
    private final InPresenza sessione; // se null -> caso 1, se non null -> caso 2
    private final GestioneCucina gestioneCucina;

    public CreaRicettaGUI(GestioneRicette gestioneRicette, InPresenza sessione) {
        this.gestioneRicette = gestioneRicette;
        this.sessione = sessione;
        this.gestioneCucina = (sessione != null) ? new GestioneCucina(null) : null; // pass DAO reale
    }

    public void start(Stage stage) {
        stage.setTitle("Crea Nuova Ricetta");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome Ricetta");

        TextField tempoField = new TextField();
        tempoField.setPromptText("Tempo preparazione (minuti)");

        // ingredienti
        Map<Ingrediente, Double> ingredientiMap = new HashMap<>();
        ListView<String> ingredientiList = new ListView<>();

        Button btnAggiungiIngrediente = new Button("Aggiungi Ingrediente");
        btnAggiungiIngrediente.setOnAction(e -> {
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
                        ingredientiMap.put(i, q);
                        ingredientiList.getItems().add(i.getNome() + " (" + i.getTipo() + ") - " + q);
                    } catch (Exception ex) {
                        showError("Errore: " + ex.getMessage());
                    }
                }
                return null;
            });

            dialog.showAndWait();
        });

        Button btnConferma = new Button("Conferma Ricetta");
        btnConferma.setOnAction(e -> {
            try {
                String nome = nomeField.getText();
                int tempo = Integer.parseInt(tempoField.getText());
                Ricetta ricetta = new Ricetta(nome, tempo);
                ricetta.setIngredienti(ingredientiMap);

                gestioneRicette.creaRicetta(ricetta);

                if (sessione != null) {
                    gestioneCucina.aggiungiSessioneARicetta(ricetta, sessione);
                }

                showInfo("Ricetta creata con successo!");
                stage.close();
            } catch (Exception ex) {
                showError("Errore: " + ex.getMessage());
            }
        });

        root.getChildren().addAll(new Label("Nome:"), nomeField,
                new Label("Tempo (min):"), tempoField,
                new Label("Ingredienti:"), ingredientiList,
                btnAggiungiIngrediente, btnConferma);

        Scene scene = new Scene(root, 400, 500);
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
