package Gui;

import controller.IngredienteController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Ingrediente;
import util.StyleHelper;
import java.util.List;

public class VisualizzaIngredientiGUI extends Stage {
    private IngredienteController ingredienteController;
    private ObservableList<Ingrediente> ingredientiData;
    private ListView<Ingrediente> ingredientiListView;
    private TextField filtroNomeField;
    private TextField filtroTipoField;
    private Button creaIngredientiBtn;
    private boolean modalitaSelezione = false;
    private Ingrediente ingredienteSelezionato = null;
    private double xOffset = 0;
    private double yOffset = 0;

    public VisualizzaIngredientiGUI(IngredienteController controller) {
        this.ingredienteController = controller;
        this.ingredientiData = FXCollections.observableArrayList();
        initializeGUI();
        caricaIngredienti();
    }

    public void setModalitaSelezione(boolean modalitaSelezione) {
        this.modalitaSelezione = modalitaSelezione;
        if (modalitaSelezione) {
            setTitle("Seleziona Ingrediente");
        }
    }

    private void initializeGUI() {
        setTitle("Gestione Ingredienti");
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UNDECORATED);
        setResizable(true);
        createLayout();
    }

    private void createLayout() {
        StackPane rootPane = new StackPane();
        rootPane.setMinSize(700, 600);
        rootPane.setPrefSize(800, 700);

        Region background = new Region();
        StyleHelper.applyBackgroundGradient(background);

        VBox mainContainer = new VBox(25);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(40, 30, 30, 30));

        Label title = new Label("🥕 Gestione Ingredienti");
        title.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox contentCard = StyleHelper.createSection();
        contentCard.setSpacing(20);

        VBox filtriSection = createFiltriSection();
        VBox listaSection = createListaSection();
        HBox buttonsSection = createButtonsSection();

        contentCard.getChildren().addAll(
                filtriSection,
                new Separator(),
                listaSection,
                new Separator(),
                buttonsSection
        );

        scrollPane.setContent(contentCard);
        mainContainer.getChildren().addAll(title, scrollPane);
        rootPane.getChildren().addAll(background, mainContainer);

        HBox windowButtons = createWindowButtons();
        rootPane.getChildren().add(windowButtons);
        StackPane.setAlignment(windowButtons, Pos.TOP_RIGHT);
        StackPane.setMargin(windowButtons, new Insets(8));

        makeDraggable(rootPane);

        Scene scene = new Scene(rootPane, 800, 700);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);
    }

    private HBox createWindowButtons() {
        Button closeButton = new Button("✕");
        Button minimizeButton = new Button("−");
        Button maximizeButton = new Button("○");

        Button[] buttons = { minimizeButton, maximizeButton, closeButton };
        for (Button btn : buttons) {
            btn.setPrefSize(30, 30);
            btn.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 12));
            btn.setTextFill(Color.WHITE);
            btn.setStyle("-fx-background-color: rgba(255,140,0,0.8); -fx-background-radius: 15; -fx-cursor: hand;");
            btn.setFocusTraversable(false);
        }

        closeButton.setOnAction(e -> {
            ingredienteSelezionato = null;
            close();
        });
        minimizeButton.setOnAction(e -> setIconified(true));
        maximizeButton.setOnAction(e -> setMaximized(!isMaximized()));

        HBox box = new HBox(3, minimizeButton, maximizeButton, closeButton);
        box.setAlignment(Pos.TOP_RIGHT);
        box.setPickOnBounds(false);
        return box;
    }

    private void makeDraggable(StackPane root) {
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        root.setOnMouseDragged(event -> {
            setX(event.getScreenX() - xOffset);
            setY(event.getScreenY() - yOffset);
        });
    }

    private VBox createFiltriSection() {
        VBox section = new VBox(15);

        Label sectionTitle = new Label("🔍 Filtri Ingredienti");
        sectionTitle.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        HBox filtriBox = new HBox(15);
        filtriBox.setAlignment(Pos.CENTER_LEFT);

        filtroNomeField = StyleHelper.createTextField("Cerca per nome...");
        filtroNomeField.setPrefWidth(250);
        filtroNomeField.textProperty().addListener((obs, oldVal, newVal) -> applicaFiltri());

        filtroTipoField = StyleHelper.createTextField("Cerca per tipo...");
        filtroTipoField.setPrefWidth(200);
        filtroTipoField.textProperty().addListener((obs, oldVal, newVal) -> applicaFiltri());

        Button resetFiltriBtn = StyleHelper.createInfoButton("🔄 Reset");
        resetFiltriBtn.setOnAction(e -> resetFiltri());

        filtriBox.getChildren().addAll(
                StyleHelper.createLabel("Nome:"), filtroNomeField,
                StyleHelper.createLabel("Tipo:"), filtroTipoField,
                resetFiltriBtn
        );

        section.getChildren().addAll(sectionTitle, filtriBox);
        return section;
    }

    private VBox createListaSection() {
        VBox section = new VBox(15);

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label sectionTitle = new Label("📋 Lista Ingredienti");
        sectionTitle.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        // Spiegazione doppio click più visibile
        Label istruzioniLabel = new Label("💡 Doppio click su un ingrediente per selezionarlo rapidamente");
        istruzioniLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 13));
        istruzioniLabel.setTextFill(Color.WHITE);
        istruzioniLabel.setStyle("-fx-background-color: " + StyleHelper.INFO_BLUE + "; -fx-padding: 10; -fx-background-radius: 8;");
        istruzioniLabel.setWrapText(true);

        VBox titleBox = new VBox(8);
        titleBox.getChildren().addAll(sectionTitle, istruzioniLabel);

        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);

        if (modalitaSelezione) {
            Button selezionaBtn = StyleHelper.createPrimaryButton("✅ Seleziona");
            selezionaBtn.setOnAction(e -> selezionaIngrediente());
            actionButtons.getChildren().add(selezionaBtn);
        }

        creaIngredientiBtn = StyleHelper.createSuccessButton("➕ Crea Nuovo");
        creaIngredientiBtn.setOnAction(e -> apriCreaIngrediente());
        actionButtons.getChildren().add(creaIngredientiBtn);

        headerBox.getChildren().addAll(titleBox, new Region(), actionButtons);
        HBox.setHgrow(headerBox.getChildren().get(1), Priority.ALWAYS);

        ingredientiListView = new ListView<>();
        ingredientiListView.setPrefHeight(300);
        ingredientiListView.setItems(ingredientiData);
        StyleHelper.applyListViewStyle(ingredientiListView);

        ingredientiListView.setCellFactory(listView -> new ListCell<Ingrediente>() {
            @Override
            protected void updateItem(Ingrediente item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setOnMouseClicked(null);
                } else {
                    HBox cellBox = new HBox(15);
                    cellBox.setAlignment(Pos.CENTER_LEFT);
                    cellBox.setPadding(new Insets(12));

                    VBox infoBox = new VBox(5);

                    Label nomeLabel = new Label("🥕 " + item.getNome());
                    nomeLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 15));
                    nomeLabel.setTextFill(Color.BLACK);

                    Label tipoLabel = new Label("📂 Tipo: " + item.getTipo());
                    tipoLabel.setFont(javafx.scene.text.Font.font("Roboto", 12));
                    tipoLabel.setTextFill(Color.GRAY);

                    infoBox.getChildren().addAll(nomeLabel, tipoLabel);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    cellBox.getChildren().addAll(infoBox, spacer);

                    if (modalitaSelezione) {
                        cellBox.setStyle("-fx-background-color: #E8F5E9; -fx-border-color: " + StyleHelper.SUCCESS_GREEN + "; " +
                                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 2; -fx-cursor: hand;");

                        cellBox.setOnMouseEntered(e ->
                                cellBox.setStyle("-fx-background-color: #C8E6C9; -fx-border-color: " + StyleHelper.SUCCESS_GREEN + "; " +
                                        "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 3; -fx-cursor: hand;"));

                        cellBox.setOnMouseExited(e ->
                                cellBox.setStyle("-fx-background-color: #E8F5E9; -fx-border-color: " + StyleHelper.SUCCESS_GREEN + "; " +
                                        "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 2; -fx-cursor: hand;"));
                    }

                    setGraphic(cellBox);
                    setText(null);

                    if (modalitaSelezione) {
                        setOnMouseClicked(event -> {
                            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                                ingredientiListView.getSelectionModel().select(item);
                                selezionaIngrediente();
                            }
                        });
                    }
                }
            }
        });

        section.getChildren().addAll(headerBox, ingredientiListView);
        return section;
    }

    private void selezionaIngrediente() {
        Ingrediente selezionato = ingredientiListView.getSelectionModel().getSelectedItem();
        if (selezionato == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attenzione");
            alert.setHeaderText(null);
            alert.setContentText("⚠️ Seleziona un ingrediente dalla lista oppure fai doppio click su un ingrediente");
            alert.showAndWait();
            return;
        }
        ingredienteSelezionato = selezionato;
        close();
    }

    private HBox createButtonsSection() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        if (modalitaSelezione) {
            Button annullaBtn = new Button("❌ Annulla");
            annullaBtn.setPrefWidth(130);
            annullaBtn.setPrefHeight(40);
            annullaBtn.setStyle("-fx-background-color: " + StyleHelper.NEUTRAL_GRAY + "; " +
                    "-fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-font-weight: bold;");
            annullaBtn.setOnAction(e -> {
                ingredienteSelezionato = null;
                close();
            });

            buttonBox.getChildren().add(annullaBtn);
        } else {
            Button aggiornaBtn = StyleHelper.createInfoButton("🔄 Aggiorna");
            aggiornaBtn.setOnAction(e -> caricaIngredienti());

            Button chiudiBtn = StyleHelper.createPrimaryButton("🚪 Chiudi");
            chiudiBtn.setOnAction(e -> close());

            buttonBox.getChildren().addAll(aggiornaBtn, chiudiBtn);
        }

        return buttonBox;
    }

    private void caricaIngredienti() {
        try {
            List<Ingrediente> ingredienti = ingredienteController.getAllIngredienti();
            ingredientiData.setAll(ingredienti);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText(null);
            alert.setContentText("❌ Errore nel caricamento ingredienti: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void applicaFiltri() {
        try {
            String nomeRicerca = filtroNomeField.getText();
            String tipoRicerca = filtroTipoField.getText();

            List<Ingrediente> ingredientiFiltrati;

            if ((nomeRicerca == null || nomeRicerca.trim().isEmpty()) &&
                    (tipoRicerca == null || tipoRicerca.trim().isEmpty())) {
                ingredientiFiltrati = ingredienteController.getAllIngredienti();
            } else if (nomeRicerca != null && !nomeRicerca.trim().isEmpty() &&
                    (tipoRicerca == null || tipoRicerca.trim().isEmpty())) {
                ingredientiFiltrati = ingredienteController.cercaIngredientiPerNome(nomeRicerca.trim());
            } else if ((nomeRicerca == null || nomeRicerca.trim().isEmpty()) &&
                    tipoRicerca != null && !tipoRicerca.trim().isEmpty()) {
                ingredientiFiltrati = ingredienteController.cercaIngredientiPerTipo(tipoRicerca.trim());
            } else {
                List<Ingrediente> perNome = ingredienteController.cercaIngredientiPerNome(nomeRicerca.trim());
                ingredientiFiltrati = perNome.stream()
                        .filter(ing -> ing.getTipo().toLowerCase().contains(tipoRicerca.toLowerCase().trim()))
                        .toList();
            }

            ingredientiData.setAll(ingredientiFiltrati);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText(null);
            alert.setContentText("❌ Errore nell'applicazione filtri: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void resetFiltri() {
        filtroNomeField.clear();
        filtroTipoField.clear();
        caricaIngredienti();
    }

    private void apriCreaIngrediente() {
        try {
            CreaIngredientiGUI creaGUI = new CreaIngredientiGUI(ingredienteController);
            Ingrediente nuovoIngrediente = creaGUI.showAndReturn();
            if (nuovoIngrediente != null) {
                caricaIngredienti();
                StyleHelper.showSuccessDialog("Successo", "Ingrediente '" + nuovoIngrediente.getNome() + "' creato con successo!");
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText(null);
            alert.setContentText("❌ Errore nell'apertura creazione ingrediente: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public Ingrediente showAndReturn() {
        showAndWait();
        return ingredienteSelezionato;
    }
}
