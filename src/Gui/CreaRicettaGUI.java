package Gui;

import model.Ricetta;
import model.InPresenza;
import service.GestioneRicette;
import service.GestioneCucina;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.SQLException;

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
        stage.setTitle("Crea Ricetta");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(5);
        grid.setHgap(5);

        TextField nomeField = new TextField();
        TextField tempoField = new TextField();

        Button btnSalva = new Button("Salva");
        btnSalva.setOnAction(e -> {
            String nome = nomeField.getText().trim();
            int tempo;
            try {
                tempo = Integer.parseInt(tempoField.getText().trim());
                Ricetta r = new Ricetta(nome, tempo);
                gestioneRicette.creaRicetta(r);

                if (sessione != null) {
                    sessione.getRicette().add(r);
                    gestioneCucina.aggiungiSessioneARicetta(r, sessione);
                }
                stage.close();
            } catch (NumberFormatException ex) {
                showError("Tempo non valido");
            } catch (SQLException ex) {
                showError("Errore salvataggio ricetta: " + ex.getMessage());
            }
        });

        grid.add(new Label("Nome:"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(new Label("Tempo Preparazione:"), 0, 1);
        grid.add(tempoField, 1, 1);
        grid.add(btnSalva, 1, 2);

        Scene scene = new Scene(grid, 400, 200);
        stage.setScene(scene);
        stage.show();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
