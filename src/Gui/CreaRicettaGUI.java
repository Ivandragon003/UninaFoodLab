package Gui;

import controller.RicettaController;
import controller.IngredienteController;
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
    
    // ✅ Per gestire schermate continue
    private VBox mainContainer;
    private VBox creaView;
    private VBox selezionaView;

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

    // ==================== LAYOUT ====================

    private VBox buildMainLayout() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        StyleHelper.applyBackgroundGradient(container);

        Label title = StyleHelper.createTitleLabel("✨ Crea Nuova Ricetta");
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);

        ScrollPane scroll = new ScrollPane(new VBox(15,
            buildInfoSection(),
            new Separator(),
            buildIngredientiSection()
        ));
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Separator bottomSep = new Separator();
        HBox buttons = buildButtons();

        container.getChildren().addAll(title, scroll, bottomSep, buttons);
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

        Label suggerimento = new Label("💡 Clicca per aggiungere ingredienti. Puoi crearne di nuovi dalla finestra di selezione.");
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

        section.getChildren().addAll(
            title, 
            selezionaBtn, 
            suggerimento, 
            contatoreLabel,
            listaLabel, 
            listaIngredientiContainer
        );
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

    // ==================== SELEZIONE INGREDIENTE (SCHERMATA CONTINUA) ====================

    private void mostraSelezioneIngrediente() {
        selezionaView = new VBox(20);
        selezionaView.setPadding(new Insets(20));
        StyleHelper.applyBackgroundGradient(selezionaView);

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

        selezionaView.getChildren().addAll(titleBox, content, new Separator(), buttons);
        mainContainer.getChildren().setAll(selezionaView);
    }

    // ✅ DIALOG QUANTITÀ MIGLIORATO
    private void chiediQuantita(Ingrediente ing) {
        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Quantità Ingrediente");
        dialog.initModality(Modality.APPLICATION_MODAL);

        ButtonType aggiungiBtn = new ButtonType("✅ Aggiungi", ButtonBar.ButtonData.OK_DONE);
        ButtonType annullaBtn = new ButtonType("❌ Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(aggiungiBtn, annullaBtn);

        VBox content = new VBox(18);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.CENTER);
        content.setPrefWidth(450);

        // Icona grande
        Label icon = new Label("🥕");
        icon.setFont(Font.font("Segoe UI Emoji", 72));
        icon.setAlignment(Pos.CENTER);
        icon.setMaxWidth(Double.MAX_VALUE);

        // Nome ingrediente
        Label nomeLabel = new Label(ing.getNome());
        nomeLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 26));
        nomeLabel.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));
        nomeLabel.setAlignment(Pos.CENTER);
        nomeLabel.setMaxWidth(Double.MAX_VALUE);

        // Tipo ingrediente
        Label tipoLabel = new Label("📂 Tipo: " + ing.getTipo());
        tipoLabel.setFont(Font.font("Roboto", 15));
        tipoLabel.setTextFill(Color.GRAY);
        tipoLabel.setAlignment(Pos.CENTER);
        tipoLabel.setMaxWidth(Double.MAX_VALUE);

        Separator sep = new Separator();
        sep.setMaxWidth(350);
        sep.setPadding(new Insets(10, 0, 10, 0));

        // Istruzioni
        Label istr = new Label("Inserisci la quantità in grammi:");
        istr.setFont(Font.font("Roboto", FontWeight.SEMI_BOLD, 16));
        istr.setAlignment(Pos.CENTER);
        istr.setMaxWidth(Double.MAX_VALUE);

        // Campo quantità GRANDE E PULITO
        TextField field = new TextField();
        field.setPromptText("Es. 250");
        field.setPrefWidth(220);
        field.setPrefHeight(55);
        field.setAlignment(Pos.CENTER);
        field.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: " + StyleHelper.PRIMARY_ORANGE + ";" +
            "-fx-border-width: 3;" +
            "-fx-border-radius: 28;" +
            "-fx-background-radius: 28;" +
            "-fx-padding: 14 22;" +
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;" +
            "-fx-font-family: 'Roboto';"
        );
        
        field.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*")) field.setText(old);
        });

        Label unit = new Label("grammi (g)");
        unit.setFont(Font.font("Roboto", FontWeight.SEMI_BOLD, 14));
        unit.setTextFill(Color.web(StyleHelper.TEXT_GRAY));

        VBox fieldBox = new VBox(10, field, unit);
        fieldBox.setAlignment(Pos.CENTER);

        // Quantità comuni CON STILE MODERNO
        Label suggTitle = new Label("💡 Quantità comuni:");
        suggTitle.setFont(Font.font("Roboto", FontWeight.BOLD, 13));
        suggTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        HBox quickBtns = new HBox(12);
        quickBtns.setAlignment(Pos.CENTER);

        for (String q : new String[]{"100g", "250g", "500g"}) {
            Button btn = new Button(q);
            btn.setPrefSize(80, 35);
            btn.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: " + StyleHelper.PRIMARY_ORANGE + ";" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 13px;" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: " + StyleHelper.PRIMARY_ORANGE + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 18;" +
                "-fx-cursor: hand;"
            );
            btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + StyleHelper.PRIMARY_ORANGE + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 13px;" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: " + StyleHelper.PRIMARY_ORANGE + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 18;" +
                "-fx-cursor: hand;"
            ));
            btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: " + StyleHelper.PRIMARY_ORANGE + ";" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 13px;" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: " + StyleHelper.PRIMARY_ORANGE + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 18;" +
                "-fx-cursor: hand;"
            ));
            btn.setOnAction(e -> field.setText(q.replace("g", "")));
            quickBtns.getChildren().add(btn);
        }

        VBox suggBox = new VBox(10);
        suggBox.setAlignment(Pos.CENTER);
        suggBox.setPadding(new Insets(15));
        suggBox.setStyle(
            "-fx-background-color: " + StyleHelper.BG_ORANGE_LIGHT + ";" +
            "-fx-background-radius: 20;"
        );
        suggBox.getChildren().addAll(suggTitle, quickBtns);

        content.getChildren().addAll(
            icon, nomeLabel, tipoLabel, sep, 
            istr, fieldBox, suggBox
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle(
            "-fx-background-color: " + StyleHelper.BG_WHITE + ";" +
            "-fx-border-color: " + StyleHelper.PRIMARY_ORANGE + ";" +
            "-fx-border-width: 5;" +
            "-fx-border-radius: 25;" +
            "-fx-background-radius: 25;"
        );

        dialog.getDialogPane().setMinWidth(500);
        dialog.getDialogPane().setMinHeight(580);

        javafx.application.Platform.runLater(() -> field.requestFocus());

        dialog.setResultConverter(btn -> {
            if (btn == aggiungiBtn) {
                try {
                    String txt = field.getText().trim();
                    if (txt.isEmpty()) return null;
                    double q = Double.parseDouble(txt);
                    return q > 0 ? q : null;
                } catch (NumberFormatException e) { 
                    return null; 
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(q -> {
            ingredientiMap.put(ing, q);
            updateDisplay();
            mainContainer.getChildren().setAll(creaView);
            StyleHelper.showSuccessDialog("✅ Ingrediente Aggiunto",
                String.format("'%s' aggiunto con %.0fg", ing.getNome(), q));
        });
    }

    // ==================== LOGICA ====================

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
            if (tempo <= 0) {
                StyleHelper.showValidationDialog("Errore", "Il tempo deve essere maggiore di zero");
                tempoField.requestFocus();
                return;
            }
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
                if (dialog != null) {
                    StyleHelper.showSuccessDialog("✅ Successo",
                        String.format("Ricetta '%s' creata!\n\n⏱️ Tempo: %d min\n🥕 Ingredienti: %d",
                            nome.trim(), tempo, ingredientiMap.size()));
                    dialog.close();
                } else {
                    StyleHelper.showSuccessDialog("✅ Successo",
                        String.format("Ricetta '%s' creata!\n\n⏱️ Tempo: %d min\n🥕 Ingredienti: %d",
                            nome.trim(), tempo, ingredientiMap.size()));
                    clearForm();
                }
            }
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore durante il salvataggio: " + e.getMessage());
        }
    }

    private void updateDisplay() {
        listaIngredientiContainer.getChildren().clear();

        if (ingredientiMap.isEmpty()) {
            Label placeholder = new Label("Nessun ingrediente aggiunto\n\n💡 Clicca su 'Aggiungi Ingrediente' per iniziare");
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
        contatoreLabel.setStyle(count == 0
            ? "-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-padding: 10 15; -fx-background-radius: 8;"
            : "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-padding: 10 15; -fx-background-radius: 8;");
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

        Button removeBtn = new Button("✕");
        removeBtn.setStyle(
            "-fx-background-color: " + StyleHelper.ERROR_RED + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 20;" +
            "-fx-cursor: hand;" +
            "-fx-min-width: 35;" +
            "-fx-min-height: 35;" +
            "-fx-max-width: 35;" +
            "-fx-max-height: 35;" +
            "-fx-font-size: 16px;"
        );
        removeBtn.setOnAction(e -> {
            ingredientiMap.remove(ing);
            updateDisplay();
        });

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