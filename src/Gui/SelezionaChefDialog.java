package Gui;

import controller.ChefController;
import exceptions.DataAccessException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Chef;
import util.StyleHelper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SelezionaChefDialog {
private final ChefController chefController;
private final Stage stage;


private final ObservableList<Chef> chefData = FXCollections.observableArrayList();
private ListView<Chef> chefListView;
private TextField searchField;
private ComboBox<String> filtroDisponibilita;
private Chef selectedChef = null;

public SelezionaChefDialog(ChefController chefController) {
    this.chefController = chefController;
    this.stage = new Stage();
    stage.initModality(Modality.APPLICATION_MODAL);
    stage.setTitle("Seleziona Chef");
}

/**
 * Mostra il dialog e ritorna l'oggetto Chef scelto, oppure null.
 */
public Chef showAndReturn() {
    VBox root = new VBox(12);
    root.setPadding(new Insets(15));
    root.setStyle("-fx-background-color: white;");

    // Header
    Label title = new Label("Seleziona Chef");
    title.setFont(Font.font("Roboto", FontWeight.BOLD, 16));

    // Search + filter row
    HBox searchRow = new HBox(8);
    searchField = new TextField();
    searchField.setPromptText("Cerca per nome, cognome o username...");
    searchField.setPrefWidth(300);

    filtroDisponibilita = new ComboBox<>();
    filtroDisponibilita.getItems().addAll("Tutti", "Solo Disponibili", "Solo Non Disponibili");
    filtroDisponibilita.setValue("Tutti");
    filtroDisponibilita.setPrefWidth(160);

    Button refreshBtn = new Button("↻");
    refreshBtn.setOnAction(e -> caricaChef());

    searchRow.getChildren().addAll(searchField, filtroDisponibilita, refreshBtn);

    // listview
    chefListView = new ListView<>(chefData);
    chefListView.setPrefSize(480, 280);
    chefListView.setCellFactory(lv -> new ListCell<>() {
        @Override
        protected void updateItem(Chef item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                String disponibilita = Boolean.TRUE.equals(item.getDisponibilita()) ? "✅" : "❌";
                String nome = Optional.ofNullable(item.getNome()).orElse("");
                String cognome = Optional.ofNullable(item.getCognome()).orElse("");
                String username = Optional.ofNullable(item.getUsername()).orElse("");
                setText(String.format("%s %s %s (%s)", disponibilita, nome, cognome, username));
            }
        }
    });

    // buttons
    Button selectBtn = StyleHelper.createPrimaryButton("Seleziona");
    Button cancelBtn = StyleHelper.createSecondaryButton("Annulla");
    HBox btnBox = new HBox(10, selectBtn, cancelBtn);
    btnBox.setAlignment(Pos.CENTER_RIGHT);

    root.getChildren().addAll(title, searchRow, chefListView, btnBox);

    // Events
    selectBtn.setOnAction(e -> {
        Chef sel = chefListView.getSelectionModel().getSelectedItem();
        if (sel == null) {
            StyleHelper.showValidationDialog("Selezione", "Seleziona uno chef prima di confermare.");
            return;
        }
        selectedChef = sel;
        stage.close();
    });

    cancelBtn.setOnAction(e -> {
        chefListView.getSelectionModel().clearSelection();
        selectedChef = null;
        stage.close();
    });

    // doppio click per selezionare
    chefListView.setOnMouseClicked(e -> {
        if (e.getClickCount() == 2) {
            Chef sel = chefListView.getSelectionModel().getSelectedItem();
            if (sel != null) {
                selectedChef = sel;
                stage.close();
            }
        }
    });

    // enter su search applica filtri
    searchField.setOnKeyPressed(e -> {
        if (e.getCode() == KeyCode.ENTER) {
            applicaFiltri();
        }
    });

    filtroDisponibilita.setOnAction(e -> applicaFiltri());

    Scene scene = new Scene(root);
    stage.setScene(scene);

    // carica iniziale
    caricaChef();

    stage.showAndWait();

    return selectedChef;
}

// Carica tutti gli chef -> aggiorna chefData
private void caricaChef() {
    try {
        List<Chef> tuttiChef = chefController.getAllChef();
        if (tuttiChef == null) {
            chefData.clear();
        } else {
            chefData.setAll(tuttiChef);
        }

        if (chefData.isEmpty()) {
            StyleHelper.showInfoDialog("Avviso", "Nessun chef disponibile nel sistema.");
        }
    } catch (DataAccessException e) {
        StyleHelper.showErrorDialog("Errore", "Errore nel caricamento chef: " + e.getMessage());
        e.printStackTrace();
    } catch (Exception e) {
        StyleHelper.showErrorDialog("Errore", "Errore imprevisto nel caricamento chef: " + e.getMessage());
        e.printStackTrace();
    }
}

// Applicazione filtri (ricerca testuale + disponibilità)
private void applicaFiltri() {
    try {
        List<Chef> tuttiChef = chefController.getAllChef();
        if (tuttiChef == null) {
            chefData.clear();
            return;
        }

        String searchText = Optional.ofNullable(searchField.getText()).orElse("").toLowerCase().trim();
        String filtroDisp = Optional.ofNullable(filtroDisponibilita.getValue()).orElse("Tutti");

        List<Chef> chefFiltrati = tuttiChef.stream()
                .filter(chef -> {
                    String nome = Optional.ofNullable(chef.getNome()).orElse("").toLowerCase();
                    String cognome = Optional.ofNullable(chef.getCognome()).orElse("").toLowerCase();
                    String username = Optional.ofNullable(chef.getUsername()).orElse("").toLowerCase();

                    boolean matchSearch = searchText.isEmpty()
                            || nome.contains(searchText)
                            || cognome.contains(searchText)
                            || username.contains(searchText);

                    boolean matchDisponibilita = true;
                    if ("Solo Disponibili".equals(filtroDisp)) {
                        matchDisponibilita = Boolean.TRUE.equals(chef.getDisponibilita());
                    } else if ("Solo Non Disponibili".equals(filtroDisp)) {
                        matchDisponibilita = !Boolean.TRUE.equals(chef.getDisponibilita());
                    }

                    return matchSearch && matchDisponibilita;
                })
                .collect(Collectors.toList());

        chefData.setAll(chefFiltrati);

        if (chefFiltrati.isEmpty()) {
            StyleHelper.showInfoDialog("Nessun risultato", "Nessun chef corrisponde ai filtri selezionati.");
        }
    } catch (DataAccessException e) {
        StyleHelper.showErrorDialog("Errore", "Errore nell'applicazione dei filtri: " + e.getMessage());
        e.printStackTrace();
    } catch (Exception e) {
        StyleHelper.showErrorDialog("Errore", "Errore imprevisto durante i filtri: " + e.getMessage());
        e.printStackTrace();
    }
}


}
