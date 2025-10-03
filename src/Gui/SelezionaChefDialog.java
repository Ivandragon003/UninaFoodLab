package Gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Chef;
import service.GestioneChef;
import util.StyleHelper;

import java.util.List;

public class SelezionaChefDialog extends Stage {

    private GestioneChef gestioneChef;
    private Chef chefSelezionato = null;
    private ObservableList<Chef> chefData;
    private ListView<Chef> chefListView;

    public SelezionaChefDialog(GestioneChef gestioneChef) {
        this.gestioneChef = gestioneChef;
        this.chefData = FXCollections.observableArrayList();

        setTitle("Seleziona Chef");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(true);

        createLayout();
        caricaChef();
    }

    private void createLayout() {
        // ROOT con SFONDO ARANCIONE usando StyleHelper
        StackPane rootPane = new StackPane();
        rootPane.setMinSize(700, 600);
        rootPane.setPrefSize(800, 700);

        // Sfondo arancione usando StyleHelper
        Region background = new Region();
        StyleHelper.applyBackgroundGradient(background);

        VBox mainContainer = new VBox(25);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(30));

        Label title = new Label("👨‍🍳 Seleziona Chef");
        title.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox contentCard = StyleHelper.createSection();
        contentCard.setSpacing(20);

        VBox listaSection = createListaSection();

        HBox buttonsSection = createButtonsSection();

        contentCard.getChildren().addAll(
            listaSection,
            new Separator(),
            buttonsSection
        );

        scrollPane.setContent(contentCard);
        mainContainer.getChildren().addAll(title, scrollPane);

        rootPane.getChildren().addAll(background, mainContainer);

        Scene scene = new Scene(rootPane, 800, 700);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);
    }

    private VBox createListaSection() {
        VBox section = new VBox(15);

        Label sectionTitle = new Label("👨‍🍳 Lista Chef");
        sectionTitle.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        chefListView = new ListView<>();
        chefListView.setPrefHeight(350);
        chefListView.setItems(chefData);

        StyleHelper.applyListViewStyle(chefListView);

        chefListView.setCellFactory(listView -> new ListCell<Chef>() {
            @Override
            protected void updateItem(Chef chef, boolean empty) {
                super.updateItem(chef, empty);
                if (empty || chef == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox cellBox = new HBox(15);
                    cellBox.setAlignment(Pos.CENTER_LEFT);
                    cellBox.setPadding(new Insets(10));

                    VBox infoBox = new VBox(5);

                    
                    Label nomeLabel = new Label("👨‍🍳 " + chef.getNome() + " " + chef.getCognome());
                    nomeLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 16));

                    Label usernameLabel = new Label("👤 " + chef.getUsername());
                    usernameLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));
                    usernameLabel.setTextFill(Color.GRAY);

              
                    Label disponibilitaLabel;
                    if (chef.getDisponibilita()) {
                        disponibilitaLabel = new Label("✅ Disponibile");
                        disponibilitaLabel.setTextFill(Color.web(StyleHelper.SUCCESS_GREEN));
                    } else {
                        disponibilitaLabel = new Label("❌ Non disponibile");
                        disponibilitaLabel.setTextFill(Color.web(StyleHelper.ERROR_RED));
                    }
                    disponibilitaLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 12));

                   
                    Label esperienzaLabel = new Label("🎓 " + chef.getAnniEsperienza() + " anni");
                    esperienzaLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));
                    esperienzaLabel.setTextFill(Color.web(StyleHelper.INFO_BLUE));

                    HBox detailsBox = new HBox(15);
                    detailsBox.setAlignment(Pos.CENTER_LEFT);
                    detailsBox.getChildren().addAll(disponibilitaLabel, esperienzaLabel);

                    infoBox.getChildren().addAll(nomeLabel, usernameLabel, detailsBox);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    cellBox.getChildren().addAll(infoBox, spacer);

                    // Colore sfondo in base alla disponibilità
                    if (chef.getDisponibilita()) {
                        cellBox.setStyle("-fx-background-color: #f0fff0; -fx-border-color: #90ee90; " +
                            "-fx-border-radius: 5; -fx-background-radius: 5;");
                    } else {
                        cellBox.setStyle("-fx-background-color: #fff0f0; -fx-border-color: #ffcccb; " +
                            "-fx-border-radius: 5; -fx-background-radius: 5;");
                    }

                    setGraphic(cellBox);
                    setText(null);
                }
            }
        });

        // Double click per selezione
        chefListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                selezionaChef();
            }
        });

        section.getChildren().addAll(sectionTitle, chefListView);
        return section;
    }

    private HBox createButtonsSection() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        Button annullaBtn = new Button("❌ Annulla");
        annullaBtn.setPrefWidth(130);
        annullaBtn.setStyle("-fx-background-color: " + StyleHelper.NEUTRAL_GRAY + "; " +
            "-fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-font-weight: bold;");
        annullaBtn.setOnAction(e -> {
            chefSelezionato = null;
            close();
        });

        Button selezionaBtn = StyleHelper.createPrimaryButton("✅ Seleziona Chef");
        selezionaBtn.setPrefWidth(150);
        selezionaBtn.setOnAction(e -> selezionaChef());

        buttonBox.getChildren().addAll(annullaBtn, selezionaBtn);
        return buttonBox;
    }

    private void caricaChef() {
        try {
            List<Chef> tuttiChef = gestioneChef.getAll();
            chefData.setAll(tuttiChef);

        } catch (Exception e) {
            showAlert("Errore", "Errore nel caricamento chef: " + e.getMessage());
        }
    }

    private void selezionaChef() {
        Chef selezionato = chefListView.getSelectionModel().getSelectedItem();
        if (selezionato == null) {
            showAlert("Selezione", "Seleziona un chef dalla lista");
            return;
        }

        // Conferma selezione
        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
        conferma.setTitle("Conferma Selezione");
        conferma.setHeaderText("Conferma selezione chef");

        String disponibilitaText = selezionato.getDisponibilita() ? "Disponibile" : "Non disponibile";
        String messaggioConferma = "Chef: " + selezionato.getNome() + " " + selezionato.getCognome() + "\n" +
            "Username: " + selezionato.getUsername() + "\n" +
            "Disponibilità: " + disponibilitaText + "\n" +
            "Esperienza: " + selezionato.getAnniEsperienza() + " anni\n\n" +
            "Confermi la selezione di questo chef?";

        conferma.setContentText(messaggioConferma);

        conferma.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                chefSelezionato = selezionato;
                close();
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    
     // Mostra il dialog e restituisce il chef selezionato
     
    public Chef showAndReturn() {
        showAndWait();
        return chefSelezionato;
    }
}
