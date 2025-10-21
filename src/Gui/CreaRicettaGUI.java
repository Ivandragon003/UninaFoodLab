package Gui;

import controller.RicettaController;
import controller.IngredienteController;
import exceptions.ValidationException;
import exceptions.DataAccessException;
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
import model.Ricetta;
import model.Ingrediente;
import guihelper.StyleHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CreaRicettaGUI {
    private final RicettaController ricettaController;
    private final IngredienteController ingredienteController;

    private Consumer<Ricetta> onRicettaCreata;
    private Runnable onAnnulla;

    private TextField nomeField;
    private TextField tempoField;
    private VBox listaIngredientiContainer;
    private Map<Ingrediente, Double> ingredientiMap;
    private Label contatoreLabel;

    private Ricetta ricettaCreata = null;
    private Stage dialog;

    private VBox mainContainer;
    private VBox creaView;
    private VBox selezionaIngView;

    public CreaRicettaGUI(RicettaController ricettaController, IngredienteController ingredienteController) {
        if (ricettaController == null || ingredienteController == null) {
            throw new IllegalArgumentException("I controller non possono essere null");
        }
        this.ricettaController = ricettaController;
        this.ingredienteController = ingredienteController;
        this.ingredientiMap = new HashMap<>();
    }

    public void setOnRicettaCreata(Consumer<Ricetta> callback) {
        this.onRicettaCreata = callback;
    }

    public void setOnAnnulla(Runnable callback) {
        this.onAnnulla = callback;
    }

    public VBox getContent() {
        if (mainContainer == null) {
            mainContainer = new VBox();
            creaView = buildMainLayout();
            mainContainer.getChildren().add(creaView);
        }
        return mainContainer;
    }

    private VBox buildMainLayout() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        StyleHelper.applyBackgroundGradient(container);

        Label title = StyleHelper.createTitleLabel("✨ Crea Nuova Ricetta");
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);

        ScrollPane scroll = new ScrollPane(
            new VBox(15, buildInfoSection(), new Separator(), buildIngredientiSection())
        );
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        HBox buttons = buildButtons();

        container.getChildren().addAll(title, scroll, new Separator(), buttons);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return container;
    }

    private VBox buildInfoSection() {
        VBox section = StyleHelper.createSection();
        Label title = createSectionTitle("📝 Informazioni Base");

        nomeField = StyleHelper.createTextField("Es. Spaghetti alla Carbonara");
        nomeField.setPrefHeight(45);

        tempoField = StyleHelper.createTextField("Es. 30");
        tempoField.setPrefHeight(45);
        tempoField.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*")) tempoField.setText(old);
        });

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        Label nomeLabel = StyleHelper.createLabel("Nome Ricetta:");
        nomeLabel.setFont(Font.font("Roboto", FontWeight.SEMI_BOLD, 14));

        Label tempoLabel = StyleHelper.createLabel("Tempo preparazione (min):");
        tempoLabel.setFont(Font.font("Roboto", FontWeight.SEMI_BOLD, 14));

        grid.add(nomeLabel, 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(tempoLabel, 0, 1);
        grid.add(tempoField, 1, 1);

        section.getChildren().addAll(title, grid);
        return section;
    }

    private VBox buildIngredientiSection() {
        VBox section = StyleHelper.createSection();
        Label title = createSectionTitle("🥕 Gestione Ingredienti");

        Button selezionaBtn = StyleHelper.createPrimaryButton("➕ Aggiungi Ingrediente");
        selezionaBtn.setPrefWidth(200);
        selezionaBtn.setOnAction(e -> mostraSelezioneIngrediente());

        Label suggerimento = new Label(
            "💡 Clicca per aggiungere ingredienti. Puoi crearne di nuovi dalla finestra di selezione."
        );
        suggerimento.setFont(Font.font("Roboto", 12));
        suggerimento.setTextFill(Color.web(StyleHelper.INFO_BLUE));
        suggerimento.setWrapText(true);

        contatoreLabel = new Label("📊 Ingredienti aggiunti: 0");
        contatoreLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
        updateContatore();

        Label listaLabel = StyleHelper.createLabel("Ingredienti Selezionati:");
        listaLabel.setFont(Font.font("Roboto", FontWeight.SEMI_BOLD, 14));

        listaIngredientiContainer = createListContainer();
        updateDisplay();

        section.getChildren().addAll(title, selezionaBtn, suggerimento, contatoreLabel, listaLabel, listaIngredientiContainer);
        return section;
    }

    private HBox buildButtons() {
        Button resetBtn = StyleHelper.createSecondaryButton("🔄 Reset Form");
        resetBtn.setPrefWidth(150);
        resetBtn.setOnAction(e -> handleReset());

        Button salvaBtn = StyleHelper.createSuccessButton("💾 Salva Ricetta");
        salvaBtn.setPrefWidth(150);
        salvaBtn.setOnAction(e -> salva());

        HBox box = new HBox(15, resetBtn, salvaBtn);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15, 0, 5, 0));
        return box;
    }

    private void mostraSelezioneIngrediente() {
        selezionaIngView = new VBox(20);
        selezionaIngView.setPadding(new Insets(20));
        StyleHelper.applyBackgroundGradient(selezionaIngView);

        Label title = StyleHelper.createTitleLabel("🥕 Seleziona Ingrediente");
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);

        Label subtitle = new Label("Scegli un ingrediente dalla lista oppure creane uno nuovo");
        subtitle.setFont(Font.font("Roboto", 14));
        subtitle.setTextFill(Color.web("#FFFFFF", 0.9));
        subtitle.setAlignment(Pos.CENTER);

        VBox titleBox = new VBox(8, title, subtitle);
        titleBox.setAlignment(Pos.CENTER);

        VisualizzaIngredientiGUI visualizzaGUI = new VisualizzaIngredientiGUI(ingredienteController);
        visualizzaGUI.setModalitaSelezione(true);
        visualizzaGUI.setOnIngredienteSelezionato(ing -> {
            if (ingredientiMap.containsKey(ing)) {
                StyleHelper.showValidationDialog("Attenzione", "Ingrediente già presente nella ricetta");
                return;
            }
            chiediQuantita(ing);
        });

        VBox content = visualizzaGUI.getContent();
        VBox.setVgrow(content, Priority.ALWAYS);

        Button indietroBtn = StyleHelper.createSecondaryButton("← Indietro");
        indietroBtn.setPrefWidth(150);
        indietroBtn.setOnAction(e -> mainContainer.getChildren().setAll(creaView));

        HBox buttons = new HBox(indietroBtn);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(15, 0, 5, 0));

        selezionaIngView.getChildren().addAll(titleBox, content, new Separator(), buttons);
        mainContainer.getChildren().setAll(selezionaIngView);
    }

    // ✅ OTTIMIZZATO: Rimossi controlli ridondanti
    private void chiediQuantita(Ingrediente ing) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        dialogStage.setResizable(false);

        VBox content = new VBox(20);
        content.setMaxWidth(600);
        content.setMinHeight(350);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.CENTER);
        content.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + StyleHelper.PRIMARY_ORANGE + ";" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 3;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.20), 15, 0, 0, 6);"
        );

        Label nomeLabel = new Label(ing.getNome());
        nomeLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 24));
        nomeLabel.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));
        nomeLabel.setAlignment(Pos.CENTER);
        nomeLabel.setMaxWidth(Double.MAX_VALUE);

        Label tipoLabel = new Label("🍴 " + ing.getTipo());
        tipoLabel.setFont(Font.font("Roboto", 13));
        tipoLabel.setTextFill(Color.GRAY);
        tipoLabel.setAlignment(Pos.CENTER);
        tipoLabel.setMaxWidth(Double.MAX_VALUE);

        Separator sep = new Separator();

        Label istruzioni = new Label("Inserisci la quantità in grammi:");
        istruzioni.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
        istruzioni.setAlignment(Pos.CENTER);
        istruzioni.setMaxWidth(Double.MAX_VALUE);

        TextField quantField = new TextField();
        quantField.setPromptText("Es. 250");
        quantField.setMaxWidth(220);
        quantField.setPrefHeight(50);
        quantField.setAlignment(Pos.CENTER);
        quantField.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: " + StyleHelper.PRIMARY_ORANGE + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10;"
        );

        quantField.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*")) quantField.setText(old);
        });

        Label unitLabel = new Label("grammi (g)");
        unitLabel.setFont(Font.font("Roboto", 12));
        unitLabel.setTextFill(Color.web(StyleHelper.TEXT_GRAY));
        unitLabel.setAlignment(Pos.CENTER);

        VBox fieldContainer = new VBox(8, quantField, unitLabel);
        fieldContainer.setAlignment(Pos.CENTER);

        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);

        Button annullaBtn = StyleHelper.createSecondaryButton("✖ Annulla");
        annullaBtn.setPrefWidth(150);
        annullaBtn.setOnAction(e -> dialogStage.close());

        Button confermaBtn = StyleHelper.createSuccessButton("✓ Conferma");
        confermaBtn.setPrefWidth(150);
        confermaBtn.setOnAction(e -> {
            String text = quantField.getText().trim();

            try {
                double q = Double.parseDouble(text);
                ingredientiMap.put(ing, q);
                updateDisplay();
                dialogStage.close();
                mainContainer.getChildren().setAll(creaView);

                javafx.application.Platform.runLater(() -> 
                    StyleHelper.showSuccessDialog(
                        "Ingrediente Aggiunto",
                        String.format("%s aggiunto con %.0fg", ing.getNome(), q)
                    )
                );
            } catch (NumberFormatException ex) {
                StyleHelper.showValidationDialog("Errore", "Inserisci un numero valido");
            }
        });

        buttons.getChildren().addAll(annullaBtn, confermaBtn);
        content.getChildren().addAll(nomeLabel, tipoLabel, sep, istruzioni, fieldContainer, buttons);

        StackPane root = new StackPane(content);
        root.setStyle("-fx-background-color: transparent;");
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialogStage.setScene(scene);

        javafx.application.Platform.runLater(() -> quantField.requestFocus());
        dialogStage.show();
    }

    private void handleReset() {
        if (onAnnulla != null) {
            onAnnulla.run();
        } else {
            if (dialog != null) {
                ricettaCreata = null;
                dialog.close();
            } else {
                clearForm();
            }
        }
    }

    // ✅ OTTIMIZZATO: Validazione delegata al service
    private void salva() {
        String nome = nomeField.getText();
        String tempoStr = tempoField.getText();

        if (nome == null || nome.trim().isEmpty()) {
            StyleHelper.showValidationDialog("Errore", "Inserisci il nome della ricetta");
            nomeField.requestFocus();
            return;
        }

        if (tempoStr == null || tempoStr.trim().isEmpty()) {
            StyleHelper.showValidationDialog("Errore", "Inserisci il tempo di preparazione");
            tempoField.requestFocus();
            return;
        }

        int tempo;
        try {
            tempo = Integer.parseInt(tempoStr.trim());
        } catch (NumberFormatException e) {
            StyleHelper.showValidationDialog("Errore", "Inserisci un numero valido per il tempo");
            tempoField.requestFocus();
            return;
        }

        if (ingredientiMap.isEmpty()) {
            StyleHelper.showValidationDialog("Errore", "Aggiungi almeno un ingrediente");
            return;
        }

        try {
            Ricetta ricetta = ricettaController.creaRicetta(nome.trim(), tempo, ingredientiMap);

            if (onRicettaCreata != null) {
                onRicettaCreata.accept(ricetta);
            } else {
                ricettaCreata = ricetta;
                String messaggio = String.format(
                    "Ricetta '%s' creata!\n\n⏱️ Tempo: %d min\n🥕 Ingredienti: %d",
                    nome.trim(), tempo, ingredientiMap.size()
                );
                
                StyleHelper.showSuccessDialog("✅ Successo", messaggio);
                
                if (dialog != null) {
                    dialog.close();
                } else {
                    clearForm();
                }
            }
        } catch (ValidationException e) {
            StyleHelper.showValidationDialog("Validazione", e.getMessage());
        } catch (DataAccessException e) {
            StyleHelper.showErrorDialog("Database", "Errore di accesso ai dati: " + e.getMessage());
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore imprevisto: " + e.getMessage());
        }
    }

    private void updateDisplay() {
        listaIngredientiContainer.getChildren().clear();

        if (ingredientiMap.isEmpty()) {
            Label placeholder = new Label(
                "Nessun ingrediente aggiunto\n\n💡 Clicca su 'Aggiungi Ingrediente' per iniziare"
            );
            placeholder.setTextFill(Color.GRAY);
            placeholder.setFont(Font.font("Roboto", 14));
            placeholder.setStyle("-fx-font-style: italic;");
            placeholder.setAlignment(Pos.CENTER);
            placeholder.setMaxWidth(Double.MAX_VALUE);
            listaIngredientiContainer.getChildren().add(placeholder);
        } else {
            ingredientiMap.forEach((ing, q) -> 
                listaIngredientiContainer.getChildren().add(createIngredienteBox(ing, q))
            );
        }
        updateContatore();
    }

    private void updateContatore() {
        int count = ingredientiMap.size();
        contatoreLabel.setText("📊 Ingredienti aggiunti: " + count);
        contatoreLabel.setStyle(
            count == 0 
                ? "-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-padding: 10 15; -fx-background-radius: 8;"
                : "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-padding: 10 15; -fx-background-radius: 8;"
        );
    }

    private HBox createIngredienteBox(Ingrediente ing, double q) {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(12));
        box.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + StyleHelper.PRIMARY_ORANGE + ";" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 2;"
        );

        Label iconLabel = new Label("🥕");
        iconLabel.setFont(Font.font("Segoe UI Emoji", 20));

        VBox info = new VBox(4);
        Label nomeLabel = new Label(ing.getNome());
        nomeLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
        nomeLabel.setTextFill(Color.BLACK);

        Label dettagli = new Label(String.format("%.0fg • %s", q, ing.getTipo()));
        dettagli.setFont(Font.font("Roboto", 12));
        dettagli.setTextFill(Color.GRAY);

        info.getChildren().addAll(nomeLabel, dettagli);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button removeBtn = new Button("✕ Rimuovi");
        removeBtn.setFont(Font.font("Roboto", FontWeight.BOLD, 13));
        removeBtn.setStyle(
            "-fx-background-color: " + StyleHelper.ERROR_RED + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 8 18;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;"
        );

        removeBtn.setOnMouseEntered(e -> removeBtn.setStyle(
            "-fx-background-color: #c0392b;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 8 18;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 2);"
        ));

        removeBtn.setOnMouseExited(e -> removeBtn.setStyle(
            "-fx-background-color: " + StyleHelper.ERROR_RED + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 8 18;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;"
        ));

        removeBtn.setOnAction(e -> StyleHelper.showCustomConfirmationDialog(
            "Rimuovi Ingrediente",
            String.format("Sei sicuro di voler rimuovere '%s' dalla ricetta?", ing.getNome()),
            () -> {
                ingredientiMap.remove(ing);
                updateDisplay();
            }
        ));

        box.getChildren().addAll(iconLabel, info, spacer, removeBtn);
        return box;
    }

    private VBox createListContainer() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle(
            "-fx-background-color: " + StyleHelper.BG_LIGHT + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + StyleHelper.BORDER_LIGHT + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12;"
        );
        return box;
    }

    private Label createSectionTitle(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
        lbl.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));
        return lbl;
    }

    private void clearForm() {
        nomeField.clear();
        tempoField.clear();
        ingredientiMap.clear();
        updateDisplay();
    }

    public Ricetta showAndReturn() {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Crea Nuova Ricetta");
        dialog.setResizable(true);
        dialog.setMinWidth(700);
        dialog.setMinHeight(750);

        Scene scene = new Scene(getContent(), 700, 800);
        dialog.setScene(scene);
        dialog.showAndWait();

        return ricettaCreata;
    }
}
