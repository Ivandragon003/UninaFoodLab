package Gui;

import controller.GestioneCorsoController;
import controller.VisualizzaCorsiController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import model.CorsoCucina;

import java.sql.SQLException;
import java.util.List;

public class VisualizzaCorsiGUI {

    private VisualizzaCorsiController visualizzaController;
    private GestioneCorsoController gestioneCorsoController;
    private Pane menuRoot;
    private ObservableList<CorsoCucina> corsiData = FXCollections.observableArrayList();

    public void setControllers(VisualizzaCorsiController visualizzaController,
                               GestioneCorsoController gestioneCorsoController,
                               Pane menuRoot) {
        this.visualizzaController = visualizzaController;
        this.gestioneCorsoController = gestioneCorsoController;
        this.menuRoot = menuRoot;
    }

    public VBox getRoot() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        TextField nomeField = new TextField();
        nomeField.setPromptText("Cerca corso per nome");
        TextField argomentoField = new TextField();
        argomentoField.setPromptText("Cerca per argomento");

        TableView<CorsoCucina> table = new TableView<>();

        TableColumn<CorsoCucina, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getIdCorso()).asObject());
        TableColumn<CorsoCucina, String> nomeCol = new TableColumn<>("Nome Corso");
        nomeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNomeCorso()));
        TableColumn<CorsoCucina, Integer> iscrittiCol = new TableColumn<>("Iscritti");
        iscrittiCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(
                c.getValue().getIscrizioni() != null ? c.getValue().getIscrizioni().size() : 0
        ).asObject());
        TableColumn<CorsoCucina, String> argomentoCol = new TableColumn<>("Argomento");
        argomentoCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getArgomento()));
        TableColumn<CorsoCucina, Double> prezzoCol = new TableColumn<>("Prezzo");
        prezzoCol.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getPrezzo()).asObject());
        TableColumn<CorsoCucina, Integer> sessioniCol = new TableColumn<>("Sessioni");
        sessioniCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(
                c.getValue().getSessioni() != null ? c.getValue().getSessioni().size() : 0
        ).asObject());

        table.getColumns().addAll(idCol, nomeCol, iscrittiCol, argomentoCol, prezzoCol, sessioniCol);
        table.setItems(corsiData);

        Button mostraTuttiBtn = new Button("Mostra tutti i corsi");
        Button mieiBtn = new Button("I miei corsi");
        Button tornaIndietroBtn = new Button("Torna indietro");

        root.getChildren().addAll(nomeField, argomentoField, mostraTuttiBtn, mieiBtn, table, tornaIndietroBtn);

        caricaCorsi();

        nomeField.setOnKeyReleased(e -> filtraCorsi(nomeField.getText(), argomentoField.getText(), false));
        argomentoField.setOnKeyReleased(e -> filtraCorsi(nomeField.getText(), argomentoField.getText(), false));

        mostraTuttiBtn.setOnAction(e -> {
            nomeField.clear();
            argomentoField.clear();
            filtraCorsi("", "", false);
        });

        mieiBtn.setOnAction(e -> filtraCorsi("", "", true));

        tornaIndietroBtn.setOnAction(e -> root.getScene().setRoot(menuRoot));

        return root;
    }

    private void caricaCorsi() {
        try {
            corsiData.clear();
            corsiData.addAll(gestioneCorsoController.getTuttiICorsiCompleti());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void filtraCorsi(String nomeFiltro, String argomentoFiltro, boolean soloChefLoggato) {
        caricaCorsi();
        if (soloChefLoggato) {
            try {
                List<CorsoCucina> miei = visualizzaController.getCorsiChefLoggato();
                corsiData.retainAll(miei);
            } catch (SQLException ignored) {}
        }
        if (nomeFiltro != null && !nomeFiltro.isEmpty()) {
            corsiData.removeIf(c -> !c.getNomeCorso().toLowerCase().contains(nomeFiltro.toLowerCase()));
        }
        if (argomentoFiltro != null && !argomentoFiltro.isEmpty()) {
            corsiData.removeIf(c -> !c.getArgomento().toLowerCase().contains(argomentoFiltro.toLowerCase()));
        }
    }
}
