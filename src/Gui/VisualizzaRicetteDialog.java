package Gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import model.*;
import controller.RicettaController;
import controller.IngredienteController;
import exceptions.DataAccessException;
import helper.StyleHelper;

import java.util.List;
import java.util.ArrayList;

public class VisualizzaRicetteDialog extends Stage {

    private final RicettaController ricettaController;
    private final IngredienteController ingredienteController;
    private final ObservableList<Ricetta> ricetteDisponibili;
    private final ObservableList<Ricetta> ricetteSelezionate;
    private FilteredList<Ricetta> filteredRicette;

    private ListView<Ricetta> listaDisponibili;
    private ListView<Ricetta> listaSelezionate;
    private TextField searchField;
    private Label countDisponibiliLabel;
    private Label countSelezionateLabel;

    private List<Ricetta> risultato = new ArrayList<>();

    public VisualizzaRicetteDialog(RicettaController ricettaController, IngredienteController ingredienteController) {
        if (ricettaController == null || ingredienteController == null) {
            throw new IllegalArgumentException("I controller non possono essere null");
        }

        this.ricettaController = ricettaController;
        this.ingredienteController = ingredienteController;
        this.ricetteDisponibili = FXCollections.observableArrayList();
        this.ricetteSelezionate = FXCollections.observableArrayList();

        initStyle(StageStyle.UNDECORATED);
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);

        createLayout();
        caricaRicette();
    }

    private void createLayout() {
        StackPane root = new StackPane();
        root.setMinSize(1000, 750);

        Region bg = new Region();
        StyleHelper.applyBackgroundGradient(bg);

        VBox main = new VBox(25);
        main.setAlignment(Pos.TOP_CENTER);
        main.setPadding(new Insets(40, 35, 35, 35));

        Label title = new Label("📚 Seleziona Ricette per Sessione");
        title.setFont(Font.font("Roboto", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);

        Label subtitle = new Label("💡 Le sessioni in presenza richiedono almeno una ricetta associata");
        subtitle.setFont(Font.font("Roboto", FontWeight.SEMI_BOLD, 15));
        subtitle.setTextFill(Color.web("#FFFFFF", 0.9));
        subtitle.setAlignment(Pos.CENTER);

        VBox titleBox = new VBox(10, title, subtitle);
        titleBox.setAlignment(Pos.CENTER);

        VBox contentCard = new VBox(20);
        contentCard.setPadding(new Insets(30));
        contentCard.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: " + StyleHelper.PRIMARY_ORANGE + ";" +
            "-fx-border-width: 3;" +
            "-fx-border-radius: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 20, 0, 0, 8);"
        );

        HBox mainContainer = createMainContainerLayout();
        HBox footer = createFooter();

        contentCard.getChildren().addAll(mainContainer, footer);
        main.getChildren().addAll(titleBox, contentCard);

        HBox winBtns = buildWindowButtons();

        root.getChildren().addAll(bg, main, winBtns);
        StackPane.setAlignment(winBtns, Pos.TOP_RIGHT);
        StackPane.setMargin(winBtns, new Insets(10));

        makeDraggable(root);

        Scene scene = new Scene(root, 1050, 800);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);
    }

    private HBox createMainContainerLayout() {
        HBox container = new HBox(20);
        container.setAlignment(Pos.TOP_CENTER);

        VBox disponibiliSection = createDisponibiliSection();
        VBox buttonSection = createButtonSection();
        VBox selezionateSection = createSelezionateSection();

        HBox.setHgrow(disponibiliSection, Priority.ALWAYS);
        HBox.setHgrow(selezionateSection, Priority.ALWAYS);

        container.getChildren().addAll(disponibiliSection, buttonSection, selezionateSection);
        return container;
    }

    private VBox createDisponibiliSection() {
        VBox section = new VBox(15);
        section.setPrefWidth(370);

        Label titleLabel = new Label("🍽️ Ricette Disponibili");
        titleLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        searchField = StyleHelper.createTextField("🔍 Cerca per nome...");
        searchField.setPrefHeight(45);

        listaDisponibili = new ListView<>();
        listaDisponibili.setPrefHeight(350);
        listaDisponibili.setStyle(
            "-fx-background-color: " + StyleHelper.BG_LIGHT + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + StyleHelper.BORDER_LIGHT + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 12;"
        );
        listaDisponibili.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        filteredRicette = new FilteredList<>(ricetteDisponibili, p -> true);
        listaDisponibili.setItems(filteredRicette);

        setupCellFactory(listaDisponibili, false);
        setupSearchFilter();

        countDisponibiliLabel = new Label("📊 Disponibili: 0 ricette");
        countDisponibiliLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 13));
        countDisponibiliLabel.setTextFill(Color.web(StyleHelper.INFO_BLUE));

        section.getChildren().addAll(titleLabel, searchField, listaDisponibili, countDisponibiliLabel);
        return section;
    }

    private VBox createButtonSection() {
        VBox section = new VBox(15);
        section.setAlignment(Pos.CENTER);
        section.setPrefWidth(130);
        section.setPadding(new Insets(50, 0, 0, 0));

        Button addBtn = StyleHelper.createSuccessButton("➡️\nAggiungi");
        addBtn.setPrefSize(120, 55);
        addBtn.setOnAction(e -> aggiungiRicetteSelezionate());

        Button addAllBtn = StyleHelper.createInfoButton("⏩\nTutte");
        addAllBtn.setPrefSize(120, 55);
        addAllBtn.setOnAction(e -> aggiungiTutteRicette());

        Button removeBtn = StyleHelper.createDangerButton("⬅️\nRimuovi");
        removeBtn.setPrefSize(120, 55);
        removeBtn.setOnAction(e -> rimuoviRicetteSelezionate());

        Button removeAllBtn = StyleHelper.createSecondaryButton("⏪\nRimuovi\nTutte");
        removeAllBtn.setPrefSize(120, 60);
        removeAllBtn.setOnAction(e -> rimuoviTutteRicette());

        Separator sep = new Separator();
        sep.setPrefWidth(110);
        sep.setStyle("-fx-background-color: " + StyleHelper.BORDER_LIGHT + ";");

        Button createBtn = StyleHelper.createPrimaryButton("➕\nCrea\nNuova");
        createBtn.setPrefSize(120, 65);
        createBtn.setOnAction(e -> creaRicettaDialog());

        section.getChildren().addAll(addBtn, addAllBtn, removeBtn, removeAllBtn, sep, createBtn);
        return section;
    }

    private VBox createSelezionateSection() {
        VBox section = new VBox(15);
        section.setPrefWidth(370);

        Label titleLabel = new Label("✅ Ricette Selezionate");
        titleLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(StyleHelper.SUCCESS_GREEN));

        listaSelezionate = new ListView<>(ricetteSelezionate);
        listaSelezionate.setPrefHeight(350);
        listaSelezionate.setStyle(
            "-fx-background-color: #e8f5e9;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + StyleHelper.SUCCESS_GREEN + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 12;"
        );
        listaSelezionate.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        setupCellFactory(listaSelezionate, true);

        countSelezionateLabel = new Label("✅ Selezionate: 0 ricette");
        countSelezionateLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 13));
        countSelezionateLabel.setTextFill(Color.web(StyleHelper.SUCCESS_GREEN));

        ricetteSelezionate.addListener((javafx.collections.ListChangeListener<Ricetta>) c -> aggiornaContatori());

        section.getChildren().addAll(titleLabel, listaSelezionate, countSelezionateLabel);
        return section;
    }

    private HBox createFooter() {
        HBox footer = new HBox(20);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20, 0, 10, 0));

        Button annullaBtn = StyleHelper.createSecondaryButton("❌ Annulla");
        annullaBtn.setPrefSize(170, 50);
        annullaBtn.setOnAction(e -> {
            risultato.clear();
            close();
        });

        Button confermaBtn = StyleHelper.createSuccessButton("✅ Conferma Selezione");
        confermaBtn.setPrefSize(210, 50);
        confermaBtn.setOnAction(e -> {
            if (ricetteSelezionate.isEmpty()) {
                StyleHelper.showValidationDialog("Attenzione",
                    "Devi selezionare almeno una ricetta per le sessioni in presenza");
                return;
            }
            risultato.clear();
            risultato.addAll(ricetteSelezionate);
            close();
        });

        footer.getChildren().addAll(annullaBtn, confermaBtn);
        return footer;
    }

    private void setupCellFactory(ListView<Ricetta> listView, boolean isSelected) {
        listView.setCellFactory(lv -> new ListCell<Ricetta>() {
            @Override
            protected void updateItem(Ricetta ricetta, boolean empty) {
                super.updateItem(ricetta, empty);

                if (empty || ricetta == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    HBox cellContent = new HBox(12);
                    cellContent.setAlignment(Pos.CENTER_LEFT);
                    cellContent.setPadding(new Insets(12));

                    if (isSelected) {
                        cellContent.setStyle(
                            "-fx-background-color: white;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-color: " + StyleHelper.SUCCESS_GREEN + ";" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 10;"
                        );
                    } else {
                        cellContent.setStyle(
                            "-fx-background-color: white;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-color: " + StyleHelper.BORDER_LIGHT + ";" +
                            "-fx-border-width: 1.5;" +
                            "-fx-border-radius: 10;"
                        );
                    }

                    Label iconLabel = new Label("🍽️");
                    iconLabel.setFont(Font.font(20));

                    VBox infoBox = new VBox(4);

                    Label nameLabel = new Label(ricetta.getNome());
                    nameLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 15));
                    nameLabel.setTextFill(Color.BLACK);

                    Label timeLabel = new Label("⏱️ " + ricetta.getTempoPreparazione() + " min • 🥕 " + 
                                               ricetta.getNumeroIngredienti() + " ingredienti");
                    timeLabel.setFont(Font.font("Roboto", FontWeight.NORMAL, 12));
                    timeLabel.setTextFill(Color.web(StyleHelper.TEXT_GRAY));

                    infoBox.getChildren().addAll(nameLabel, timeLabel);
                    cellContent.getChildren().addAll(iconLabel, infoBox);

                    setGraphic(cellContent);
                    setText(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5;");

                    setOnMouseEntered(e -> {
                        if (!isEmpty()) {
                            if (isSelected) {
                                cellContent.setStyle(
                                    "-fx-background-color: #f1f8f4;" +
                                    "-fx-background-radius: 10;" +
                                    "-fx-border-color: " + StyleHelper.SUCCESS_GREEN + ";" +
                                    "-fx-border-width: 2.5;" +
                                    "-fx-border-radius: 10;"
                                );
                            } else {
                                cellContent.setStyle(
                                    "-fx-background-color: " + StyleHelper.BG_ORANGE_LIGHT + ";" +
                                    "-fx-background-radius: 10;" +
                                    "-fx-border-color: " + StyleHelper.PRIMARY_ORANGE + ";" +
                                    "-fx-border-width: 2;" +
                                    "-fx-border-radius: 10;"
                                );
                            }
                        }
                    });

                    setOnMouseExited(e -> {
                        if (!isEmpty()) {
                            if (isSelected) {
                                cellContent.setStyle(
                                    "-fx-background-color: white;" +
                                    "-fx-background-radius: 10;" +
                                    "-fx-border-color: " + StyleHelper.SUCCESS_GREEN + ";" +
                                    "-fx-border-width: 2;" +
                                    "-fx-border-radius: 10;"
                                );
                            } else {
                                cellContent.setStyle(
                                    "-fx-background-color: white;" +
                                    "-fx-background-radius: 10;" +
                                    "-fx-border-color: " + StyleHelper.BORDER_LIGHT + ";" +
                                    "-fx-border-width: 1.5;" +
                                    "-fx-border-radius: 10;"
                                );
                            }
                        }
                    });
                }
            }
        });
    }

    private void setupSearchFilter() {
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            String searchText = newValue == null ? "" : newValue.toLowerCase().trim();

            filteredRicette.setPredicate(ricetta -> {
                if (searchText.isEmpty()) return true;
                String nome = ricetta.getNome() != null ? ricetta.getNome().toLowerCase() : "";
                return nome.contains(searchText);
            });

            aggiornaContatori();
        });
    }

    private void aggiungiRicetteSelezionate() {
        List<Ricetta> selected = new ArrayList<>(listaDisponibili.getSelectionModel().getSelectedItems());

        if (selected.isEmpty()) {
            StyleHelper.showValidationDialog("Attenzione", "Seleziona almeno una ricetta da aggiungere");
            return;
        }

        for (Ricetta ricetta : selected) {
            if (!ricetteSelezionate.contains(ricetta)) {
                ricetteSelezionate.add(ricetta);
            }
        }

        listaDisponibili.getSelectionModel().clearSelection();
        aggiornaContatori();
    }

    private void aggiungiTutteRicette() {
        for (Ricetta ricetta : filteredRicette) {
            if (!ricetteSelezionate.contains(ricetta)) {
                ricetteSelezionate.add(ricetta);
            }
        }
        aggiornaContatori();
    }

    private void rimuoviRicetteSelezionate() {
        List<Ricetta> selected = new ArrayList<>(listaSelezionate.getSelectionModel().getSelectedItems());

        if (selected.isEmpty()) {
            StyleHelper.showValidationDialog("Attenzione", "Seleziona almeno una ricetta da rimuovere");
            return;
        }

        ricetteSelezionate.removeAll(selected);
        listaSelezionate.getSelectionModel().clearSelection();
        aggiornaContatori();
    }

    private void rimuoviTutteRicette() {
        if (ricetteSelezionate.isEmpty()) return;
        ricetteSelezionate.clear();
        aggiornaContatori();
    }

    private void creaRicettaDialog() {
        try {
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(this);
            dialogStage.initStyle(StageStyle.UNDECORATED);
            dialogStage.setTitle("Crea Nuova Ricetta");

            StackPane root = new StackPane();
            root.setMinSize(750, 750);

            Region bg = new Region();
            StyleHelper.applyBackgroundGradient(bg);

            VBox main = new VBox(20);
            main.setAlignment(Pos.TOP_CENTER);
            main.setPadding(new Insets(30));

            CreaRicettaGUI creaGUI = new CreaRicettaGUI(ricettaController, ingredienteController);

            creaGUI.setOnRicettaCreata(nuovaRicetta -> {
                if (nuovaRicetta == null) {
                    StyleHelper.showErrorDialog("Errore", "Ricetta non valida");
                    dialogStage.close();
                    return;
                }

                if (nuovaRicetta.getNome() == null || nuovaRicetta.getNome().trim().isEmpty()) {
                    StyleHelper.showValidationDialog("Attenzione", 
                        "Il nome della ricetta non può essere vuoto");
                    dialogStage.close();
                    return;
                }

                if (nuovaRicetta.getTempoPreparazione() <= 0) {
                    StyleHelper.showValidationDialog("Attenzione", 
                        "Il tempo di preparazione deve essere maggiore di zero");
                    dialogStage.close();
                    return;
                }

                try {
                    caricaRicette();

                    if (!ricetteSelezionate.contains(nuovaRicetta)) {
                        ricetteSelezionate.add(nuovaRicetta);
                    }

                    StyleHelper.showSuccessDialog("Successo", 
                        String.format("✅ Ricetta '%s' creata e aggiunta!\n\n⏱️ Tempo: %d min\n🥕 Ingredienti: %d",
                            nuovaRicetta.getNome(), nuovaRicetta.getTempoPreparazione(),
                            nuovaRicetta.getNumeroIngredienti()));

                    aggiornaContatori();
                    dialogStage.close();

                } catch (Exception e) {
                    StyleHelper.showErrorDialog("Errore", 
                        "Errore durante il salvataggio della ricetta: " + e.getMessage());
                    e.printStackTrace();
                    dialogStage.close();
                }
            });

            creaGUI.setOnAnnulla(dialogStage::close);

            VBox content = creaGUI.getContent();
            main.getChildren().add(content);

            HBox winBtns = new HBox(3);
            Button closeBtn = StyleHelper.createWindowButton("✕", dialogStage::close, "rgba(255,140,0,0.8)");
            winBtns.getChildren().add(closeBtn);
            winBtns.setAlignment(Pos.TOP_RIGHT);
            winBtns.setPickOnBounds(false);

            root.getChildren().addAll(bg, main, winBtns);
            StackPane.setAlignment(winBtns, Pos.TOP_RIGHT);
            StackPane.setMargin(winBtns, new Insets(8));

            makeDraggable(root, dialogStage);

            Scene scene = new Scene(root, 800, 800);
            scene.setFill(Color.TRANSPARENT);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore durante la creazione della ricetta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void caricaRicette() {
        try {
            List<Ricetta> ricette = ricettaController.visualizzaRicette();
            ricetteDisponibili.clear();

            if (ricette != null && !ricette.isEmpty()) {
                ricetteDisponibili.addAll(ricette);
            }

            aggiornaContatori();

        } catch (DataAccessException e) {
            StyleHelper.showErrorDialog("Errore Caricamento", 
                "Errore durante il caricamento delle ricette: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void aggiornaContatori() {
        int disponibili = filteredRicette.size();
        int selezionate = ricetteSelezionate.size();

        countDisponibiliLabel.setText(String.format("📊 Disponibili: %d ricette", disponibili));
        countSelezionateLabel.setText(String.format("✅ Selezionate: %d ricette", selezionate));

        if (selezionate == 0) {
            countSelezionateLabel.setTextFill(Color.web(StyleHelper.ERROR_RED));
        } else {
            countSelezionateLabel.setTextFill(Color.web(StyleHelper.SUCCESS_GREEN));
        }
    }

    private HBox buildWindowButtons() {
        Button close = StyleHelper.createWindowButton("✕", this::close, "rgba(255,140,0,0.8)");
        Button minimize = StyleHelper.createWindowButton("−", () -> setIconified(true), "rgba(255,140,0,0.8)");

        HBox box = new HBox(3, minimize, close);
        box.setAlignment(Pos.TOP_RIGHT);
        box.setPickOnBounds(false);
        return box;
    }

    private void makeDraggable(StackPane root) {
        final double[] xOffset = {0};
        final double[] yOffset = {0};
        
        root.setOnMousePressed(e -> {
            xOffset[0] = e.getSceneX();
            yOffset[0] = e.getSceneY();
        });
        
        root.setOnMouseDragged(e -> {
            setX(e.getScreenX() - xOffset[0]);
            setY(e.getScreenY() - yOffset[0]);
        });
    }

    private void makeDraggable(StackPane root, Stage stage) {
        final double[] xOffset = {0};
        final double[] yOffset = {0};
        
        root.setOnMousePressed(e -> {
            xOffset[0] = e.getSceneX();
            yOffset[0] = e.getSceneY();
        });
        
        root.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - xOffset[0]);
            stage.setY(e.getScreenY() - yOffset[0]);
        });
    }

    public List<Ricetta> showAndReturn() {
        showAndWait();
        return new ArrayList<>(risultato);
    }

    public void preSelezionaRicette(List<Ricetta> ricetteDaPreselezionare) {
        if (ricetteDaPreselezionare != null && !ricetteDaPreselezionare.isEmpty()) {
            for (Ricetta ricetta : ricetteDaPreselezionare) {
                Ricetta ricettaTrovata = ricetteDisponibili.stream()
                    .filter(r -> r.getIdRicetta() == ricetta.getIdRicetta())
                    .findFirst()
                    .orElse(null);

                if (ricettaTrovata != null && !ricetteSelezionate.contains(ricettaTrovata)) {
                    ricetteSelezionate.add(ricettaTrovata);
                }
            }
            aggiornaContatori();
        }
    }
}
