package Gui;

import model.CorsoCucina;
import model.Sessione;
import model.Online;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VisualizzaSessioniGUI {

    private CorsoCucina corso;

    public void setCorso(CorsoCucina corso) {
        this.corso = corso;
    }

    public void start(Stage stage) {
        stage.setTitle("Sessioni del corso: " + corso.getNomeCorso());

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        ListView<String> lista = new ListView<>();
        aggiornaLista(lista);

        Button gestisciBtn = new Button("Gestisci sessioni");
        Button chiudiBtn = new Button("Chiudi");

        root.getChildren().addAll(new Label("Elenco sessioni:"), lista, gestisciBtn, chiudiBtn);

        gestisciBtn.setOnAction(e -> {
            GestioneSessioniGUI gestisciGUI = new GestioneSessioniGUI();
            gestisciGUI.setCorso(corso);
            gestisciGUI.start(new Stage());
            aggiornaLista(lista);
        });

        chiudiBtn.setOnAction(e -> stage.close());

        stage.setScene(new Scene(root, 400, 500));
        stage.show();
    }

    private void aggiornaLista(ListView<String> lista) {
        lista.getItems().clear();
        int i = 1;
        for (Sessione s : corso.getSessioni()) {
            String tipo = s instanceof Online ? "Online" : "In Presenza";
            lista.getItems().add("Sessione " + i++ + " (" + tipo + "): " + s.getDataInizioSessione());
        }
    }
}
