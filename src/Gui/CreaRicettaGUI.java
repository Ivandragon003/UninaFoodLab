package Gui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Ingrediente;
import model.Ricetta;
import model.InPresenza;
import service.GestioneRicette;
import service.GestioneCucina;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class CreaRicettaGUI {

    private final GestioneRicette gestioneRicette;
    private final InPresenza sessione; 
    private final GestioneCucina gestioneCucina;

    public CreaRicettaGUI(GestioneRicette gestioneRicette, InPresenza sessione, GestioneCucina gestioneCucina) {
        this.gestioneRicette = gestioneRicette;
        this.sessione = sessione;
        this.gestioneCucina = gestioneCucina;
    }

    public void start(Stage stage) {
        stage.setTitle("Crea Nuova Ricetta");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome Ricetta");

        TextField tempoField = new TextField();
        tempoField.setPromptText("Tempo preparazione (minuti)");

        Map<Ingrediente, Double> ingredientiMap = new HashMap<>();
        ListView<String> ingredientiList = new ListView<>();

        Button btnAggiungiIngrediente = new Button("Aggiungi Ingrediente");
        btnAggiungiIngrediente.setOnAction(e -> {
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
            quantitaIng.setPromptText("Quantità (numero)");

            VBox box = new VBox(10,
                    new Label("Ingrediente esistente:"), comboEsistenti,
                    new Label("Oppure nuovo ingrediente:"), nomeIng, tipoIng,
                    new Label("Quantità:"), quantitaIng
            );
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
                        } else if (!nomeIng.getText().isEmpty() && !tipoIng.getText().isEmpty()) {
                            i = new Ingrediente(nomeIng.getText(), tipoIng.getText());
                            gestioneRicette.creaIngrediente(i); // salva nel DB se nuovo
                        } else {
                            throw new IllegalArgumentException("Devi selezionare o inserire un ingrediente");
                        }

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
                if (nome.isEmpty()) throw new IllegalArgumentException("Nome ricetta obbligatorio");

                int tempo = Integer.parseInt(tempoField.getText());
                Ricetta ricetta = new Ricetta(nome, tempo);
                ricetta.setIngredienti(ingredientiMap);

                gestioneRicette.creaRicetta(ricetta);

                if (sessione != null) {
                    gestioneCucina.aggiungiSessioneARicetta(ricetta, sessione);
                    sessione.getRicette().add(ricetta);
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
