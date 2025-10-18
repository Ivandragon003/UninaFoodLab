package Gui;

import controller.IngredienteController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Ingrediente;
import guihelper.StyleHelper;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Componente VBox per visualizzare e gestire ingredienti.
 * NON è uno Stage, ma un componente riutilizzabile.
 */
public class VisualizzaIngredientiGUI {
    private final IngredienteController controller;
    private final ObservableList<Ingrediente> data;
    private ListView<Ingrediente> listView;
    private TextField filtroNome, filtroTipo;
    
    // ✅ FORM INLINE per creare ingredienti
    private VBox formCreazioneSection;
    private TextField formNomeField, formTipoField;
    private Label formErrorLabel;
    private boolean formVisible = false;
    
    // ✅ Per modalità selezione (usato da SelezionaIngredienteDialog)
    private boolean modalitaSelezione = false;
    private Consumer<Ingrediente> onIngredienteSelezionato;
    
    private VBox root;

    public VisualizzaIngredientiGUI(IngredienteController controller) {
        this.controller = controller;
        this.data = FXCollections.observableArrayList();
        carica();
    }

    public void setModalitaSelezione(boolean mode) {
        this.modalitaSelezione = mode;
    }

    public void setOnIngredienteSelezionato(Consumer<Ingrediente> callback) {
        this.onIngredienteSelezionato = callback;
    }

    // ✅ METODO PRINCIPALE: restituisce il contenuto VBox
    public VBox getContent() {
        if (root == null) {
            root = buildMain();
        }
        return root;
    }

    // ==================== BUILD ====================

    private VBox buildMain() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        VBox content = StyleHelper.createSection();
        content.setSpacing(20);
        
        // ✅ FORM CREAZIONE (inizialmente nascosto)
        formCreazioneSection = buildFormCreazione();
        formCreazioneSection.setVisible(false);
        formCreazioneSection.setManaged(false);
        
        content.getChildren().addAll(
            buildFiltri(),
            new Separator(),
            buildLista(),
            new Separator(),
            formCreazioneSection  // ✅ FORM INLINE
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        container.getChildren().add(scroll);
        return container;
    }

    private VBox buildFiltri() {
        VBox section = new VBox(15);

        Label title = createTitle("🔍 Filtri Ingredienti");

        filtroNome = StyleHelper.createTextField("Cerca per nome...");
        filtroNome.setPrefWidth(250);
        filtroNome.textProperty().addListener((obs, old, val) -> filtra());

        filtroTipo = StyleHelper.createTextField("Cerca per tipo...");
        filtroTipo.setPrefWidth(200);
        filtroTipo.textProperty().addListener((obs, old, val) -> filtra());

        Button resetBtn = StyleHelper.createInfoButton("🔄 Reset");
        resetBtn.setOnAction(e -> reset());

        HBox filtri = new HBox(15,
            StyleHelper.createLabel("Nome:"), filtroNome,
            StyleHelper.createLabel("Tipo:"), filtroTipo,
            resetBtn
        );
        filtri.setAlignment(Pos.CENTER_LEFT);

        section.getChildren().addAll(title, filtri);
        return section;
    }

    private VBox buildLista() {
        VBox section = new VBox(15);

        Label title = createTitle("📋 Lista Ingredienti");
        
        Label info = new Label("💡 Doppio click su un ingrediente per selezionarlo rapidamente");
        info.setFont(Font.font("Roboto", FontWeight.BOLD, 13));
        info.setTextFill(Color.WHITE);
        info.setStyle("-fx-background-color: " + StyleHelper.INFO_BLUE + "; -fx-padding: 10; -fx-background-radius: 8;");
        info.setWrapText(true);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(8, title, info);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button creaBtn = StyleHelper.createSuccessButton("➕ Crea Nuovo");
        creaBtn.setOnAction(e -> toggleFormCreazione());
        actions.getChildren().add(creaBtn);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(titleBox, spacer, actions);

        listView = new ListView<>(data);
        listView.setPrefHeight(300);
        listView.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: " + StyleHelper.BORDER_LIGHT + ";" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-border-width: 1;"
        );
        listView.setCellFactory(lv -> new IngredienteCell());

        section.getChildren().addAll(header, listView);
        return section;
    }

    // ✅ FORM CREAZIONE INLINE
    private VBox buildFormCreazione() {
        VBox section = new VBox(20);
        section.setPadding(new Insets(20));
        section.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + StyleHelper.PRIMARY_ORANGE + ";" +
            "-fx-border-width: 3;" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(255,140,0,0.3), 15, 0, 0, 5);"
        );

        Label title = new Label("🎨 Informazioni Base");
        title.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
        title.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        formErrorLabel = new Label();
        formErrorLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 13));
        formErrorLabel.setTextFill(Color.RED);
        formErrorLabel.setVisible(false);
        formErrorLabel.setWrapText(true);
        formErrorLabel.setStyle("-fx-background-color: #ffe6e6; -fx-padding: 10; -fx-background-radius: 8;");

        Label nomeLabel = new Label("Nome Ingrediente:");
        nomeLabel.setFont(Font.font("Roboto", FontWeight.SEMI_BOLD, 14));
        nomeLabel.setTextFill(Color.BLACK);

        formNomeField = StyleHelper.createTextField("Es. Pomodoro San Marzano");
        formNomeField.setPrefHeight(40);
        formNomeField.textProperty().addListener((obs, old, val) -> {
            if (!val.trim().isEmpty()) {
                formNomeField.setStyle("");
                formErrorLabel.setVisible(false);
            }
        });

        Label tipoLabel = new Label("Tipo (es. Verdura, Carne, Spezie...):");
        tipoLabel.setFont(Font.font("Roboto", FontWeight.SEMI_BOLD, 14));
        tipoLabel.setTextFill(Color.BLACK);

        formTipoField = StyleHelper.createTextField("Es. Verdura");
        formTipoField.setPrefHeight(40);
        formTipoField.textProperty().addListener((obs, old, val) -> {
            if (!val.trim().isEmpty()) {
                formTipoField.setStyle("");
                formErrorLabel.setVisible(false);
            }
        });

        Label hint = new Label("💡 Esempi: Verdura, Frutta, Carne, Pesce, Latticini, Cereali, Legumi, Spezie, Condimenti");
        hint.setFont(Font.font("Roboto", 11));
        hint.setTextFill(Color.web(StyleHelper.INFO_BLUE));
        hint.setWrapText(true);

        HBox buttonsBox = new HBox(15);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPadding(new Insets(10, 0, 0, 0));

        Button annullaBtn = new Button("✕ Annulla");
        annullaBtn.setPrefSize(120, 40);
        annullaBtn.setFont(Font.font("Roboto", FontWeight.BOLD, 13));
        annullaBtn.setStyle("-fx-background-color: " + StyleHelper.NEUTRAL_GRAY + "; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand;");
        annullaBtn.setOnAction(e -> toggleFormCreazione());

        Button salvaBtn = StyleHelper.createSuccessButton("💾 Salva Ingrediente");
        salvaBtn.setPrefSize(180, 40);
        salvaBtn.setFont(Font.font("Roboto", FontWeight.BOLD, 13));
        salvaBtn.setOnAction(e -> salvaIngrediente());

        buttonsBox.getChildren().addAll(annullaBtn, salvaBtn);

        section.getChildren().addAll(
            formErrorLabel,
            title,
            new Separator(),
            nomeLabel, formNomeField,
            tipoLabel, formTipoField,
            hint,
            buttonsBox
        );

        return section;
    }

    // ==================== LOGICA ====================

    private void carica() {
        try {
            data.setAll(controller.getAllIngredienti());
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore caricamento: " + e.getMessage());
        }
    }

    private void filtra() {
        try {
            String nome = filtroNome.getText();
            String tipo = filtroTipo.getText();

            List<Ingrediente> filtrati;

            if ((nome == null || nome.trim().isEmpty()) && (tipo == null || tipo.trim().isEmpty())) {
                filtrati = controller.getAllIngredienti();
            } else if (nome != null && !nome.trim().isEmpty() && (tipo == null || tipo.trim().isEmpty())) {
                filtrati = controller.cercaIngredientiPerNome(nome.trim());
            } else if ((nome == null || nome.trim().isEmpty()) && tipo != null && !tipo.trim().isEmpty()) {
                filtrati = controller.cercaIngredientiPerTipo(tipo.trim());
            } else {
                List<Ingrediente> perNome = controller.cercaIngredientiPerNome(nome.trim());
                String tipoLower = tipo.toLowerCase().trim();
                filtrati = perNome.stream()
                    .filter(i -> i.getTipo().toLowerCase().contains(tipoLower))
                    .toList();
            }

            data.setAll(filtrati);
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore filtro: " + e.getMessage());
        }
    }

    private void reset() {
        filtroNome.clear();
        filtroTipo.clear();
        carica();
    }

    private void toggleFormCreazione() {
        formVisible = !formVisible;
        formCreazioneSection.setVisible(formVisible);
        formCreazioneSection.setManaged(formVisible);
        
        if (formVisible) {
            formNomeField.clear();
            formTipoField.clear();
            formErrorLabel.setVisible(false);
            javafx.application.Platform.runLater(() -> formNomeField.requestFocus());
        }
    }

    private void salvaIngrediente() {
        try {
            String nome = formNomeField.getText().trim();
            String tipo = formTipoField.getText().trim();

            if (nome.isEmpty()) {
                formErrorLabel.setText("❌ Il nome è obbligatorio");
                formErrorLabel.setVisible(true);
                formNomeField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                return;
            }

            if (tipo.isEmpty()) {
                formErrorLabel.setText("❌ Il tipo è obbligatorio");
                formErrorLabel.setVisible(true);
                formTipoField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                return;
            }

            int id = controller.creaIngrediente(nome, tipo);
            Optional<Ingrediente> opt = controller.trovaIngredientePerId(id);

            if (opt.isPresent()) {
                carica();
                toggleFormCreazione();
                StyleHelper.showSuccessDialog("Successo", 
                    String.format("✅ Ingrediente '%s' creato!\n\n📂 Tipo: %s", nome, tipo));
            }

        } catch (Exception e) {
            formErrorLabel.setText("❌ " + e.getMessage());
            formErrorLabel.setVisible(true);
        }
    }

    private Label createTitle(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
        lbl.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));
        return lbl;
    }

    // ==================== CELL RENDERER ====================

    private class IngredienteCell extends ListCell<Ingrediente> {
        @Override
        protected void updateItem(Ingrediente item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                setOnMouseClicked(null);
            } else {
                setGraphic(buildCell(item));
                setText(null);
                
                if (modalitaSelezione) {
                    setOnMouseClicked(e -> {
                        if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                            if (onIngredienteSelezionato != null) {
                                onIngredienteSelezionato.accept(item);
                            }
                        }
                    });
                }
            }
        }

        private HBox buildCell(Ingrediente item) {
            HBox box = new HBox(15);
            box.setAlignment(Pos.CENTER_LEFT);
            box.setPadding(new Insets(12));

            Label nome = new Label("🥕 " + item.getNome());
            nome.setFont(Font.font("Roboto", FontWeight.BOLD, 15));
            nome.setTextFill(Color.BLACK);

            Label tipo = new Label("📂 Tipo: " + item.getTipo());
            tipo.setFont(Font.font("Roboto", 12));
            tipo.setTextFill(Color.GRAY);

            VBox info = new VBox(5, nome, tipo);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            box.getChildren().addAll(info, spacer);

            if (modalitaSelezione) {
                box.setStyle(
                    "-fx-background-color: #E8F5E9; " +
                    "-fx-border-color: " + StyleHelper.SUCCESS_GREEN + "; " +
                    "-fx-border-radius: 8; " +
                    "-fx-background-radius: 8; " +
                    "-fx-border-width: 2; " +
                    "-fx-cursor: hand;"
                );

                box.setOnMouseEntered(e -> box.setStyle(
                    "-fx-background-color: #C8E6C9; " +
                    "-fx-border-color: " + StyleHelper.SUCCESS_GREEN + "; " +
                    "-fx-border-radius: 8; " +
                    "-fx-background-radius: 8; " +
                    "-fx-border-width: 3; " +
                    "-fx-cursor: hand;"
                ));

                box.setOnMouseExited(e -> box.setStyle(
                    "-fx-background-color: #E8F5E9; " +
                    "-fx-border-color: " + StyleHelper.SUCCESS_GREEN + "; " +
                    "-fx-border-radius: 8; " +
                    "-fx-background-radius: 8; " +
                    "-fx-border-width: 2; " +
                    "-fx-cursor: hand;"
                ));
            }

            return box;
        }
    }
}
