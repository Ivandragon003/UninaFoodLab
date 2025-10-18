package Gui;

import controller.RicettaController;
import controller.IngredienteController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.util.List;
import java.util.Map;

public class VisualizzaRicetteGUI {
    private final RicettaController ricettaController;
    private final IngredienteController ingredienteController;
    private final ObservableList<Ricetta> ricetteData;
    private ListView<Ricetta> ricetteListView;
    private TextField filtroNome, filtroTempoMin, filtroTempoMax;
    private TextField filtroIngMin, filtroIngMax;
    private boolean modalitaSelezione = false;
    private Ricetta ricettaSelezionata = null;
    private VBox root;

    public VisualizzaRicetteGUI(RicettaController ricettaController, IngredienteController ingredienteController) {
        if (ricettaController == null || ingredienteController == null) {
            throw new IllegalArgumentException("I controller non possono essere null");
        }
        this.ricettaController = ricettaController;
        this.ingredienteController = ingredienteController;
        this.ricetteData = FXCollections.observableArrayList();
        carica();
    }

    public void setSelectionMode(boolean mode) { 
        this.modalitaSelezione = mode; 
    }
    
    public Ricetta showAndReturn() { 
        return ricettaSelezionata; 
    }

    public VBox getRoot() {
        if (root == null) {
            root = buildLayout();
            setupListeners();
        }
        return root;
    }

    // ==================== LAYOUT ====================

    private VBox buildLayout() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        StyleHelper.applyBackgroundGradient(container);

        Label title = StyleHelper.createTitleLabel("📖 Gestione Ricette");
        title.setAlignment(Pos.CENTER);
        title.setTextFill(Color.WHITE);

        VBox scrollContent = new VBox(15,
            buildFiltri(),
            new Separator(),
            buildLista()
        );

        ScrollPane scroll = new ScrollPane(scrollContent);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Separator bottomSep = new Separator();
        HBox buttons = buildButtons();

        container.getChildren().addAll(title, scroll, bottomSep, buttons);
        return container;
    }

    private VBox buildFiltri() {
        VBox section = StyleHelper.createSection();
        Label title = createTitle("🔍 Filtri Avanzati");

        filtroNome = StyleHelper.createTextField("Cerca per nome...");
        filtroNome.setPrefWidth(300);

        filtroTempoMin = StyleHelper.createTextField("Min");
        filtroTempoMax = StyleHelper.createTextField("Max");
        filtroIngMin = StyleHelper.createTextField("Min");
        filtroIngMax = StyleHelper.createTextField("Max");

        for (TextField f : new TextField[]{filtroTempoMin, filtroTempoMax, filtroIngMin, filtroIngMax}) {
            f.setPrefWidth(80);
        }

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.add(StyleHelper.createLabel("Nome:"), 0, 0);
        grid.add(filtroNome, 1, 0, 3, 1);
        grid.add(StyleHelper.createLabel("Tempo:"), 0, 1);
        grid.add(createRange(filtroTempoMin, filtroTempoMax, "min"), 1, 1);
        grid.add(StyleHelper.createLabel("Ingredienti:"), 2, 1);
        grid.add(createRange(filtroIngMin, filtroIngMax, ""), 3, 1);

        Button resetBtn = StyleHelper.createInfoButton("🔄 Reset");
        resetBtn.setOnAction(e -> reset());
        grid.add(resetBtn, 4, 1);

        section.getChildren().addAll(title, grid);
        return section;
    }

    private VBox buildLista() {
        VBox section = StyleHelper.createSection();

        Label title = createTitle("📋 Lista Ricette");
        Label info = new Label("💡 Doppio click per vedere/modificare ingredienti");
        info.setFont(Font.font("Roboto", 12));
        info.setTextFill(Color.web(StyleHelper.INFO_BLUE));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(new VBox(5, title, info));

        if (!modalitaSelezione) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Button creaBtn = StyleHelper.createSuccessButton("➕ Crea Nuova");
            creaBtn.setOnAction(e -> crea());
            header.getChildren().addAll(spacer, creaBtn);
        }

        ricetteListView = new ListView<>(ricetteData);
        ricetteListView.setPrefHeight(350);
        ricetteListView.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: " + StyleHelper.BORDER_LIGHT + ";" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-border-width: 1;"
        );
        ricetteListView.setCellFactory(lv -> new RicettaCell());
        ricetteListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Ricetta sel = ricetteListView.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    if (modalitaSelezione) {
                        seleziona();
                    } else {
                        mostraModificaIngredienti(sel);
                    }
                }
            }
        });

        section.getChildren().addAll(header, ricetteListView);
        return section;
    }

    private HBox buildButtons() {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15, 0, 5, 0));

        if (modalitaSelezione) {
            Button annulla = StyleHelper.createDangerButton("❌ Annulla");
            annulla.setOnAction(e -> ricettaSelezionata = null);
            Button seleziona = StyleHelper.createPrimaryButton("✅ Seleziona");
            seleziona.setOnAction(e -> seleziona());
            box.getChildren().addAll(annulla, seleziona);
        } else {
            Button aggiorna = StyleHelper.createInfoButton("🔄 Aggiorna");
            aggiorna.setOnAction(e -> ricarica());
            box.getChildren().add(aggiorna);
        }
        return box;
    }

    // ==================== LOGICA ====================

    private void setupListeners() {
        filtroNome.textProperty().addListener((obs, old, val) -> filtra());
        filtroTempoMin.textProperty().addListener((obs, old, val) -> filtra());
        filtroTempoMax.textProperty().addListener((obs, old, val) -> filtra());
        filtroIngMin.textProperty().addListener((obs, old, val) -> filtra());
        filtroIngMax.textProperty().addListener((obs, old, val) -> filtra());
    }

    private void filtra() {
        try {
            List<Ricetta> filtrate = ricettaController.filtraCombinato(
                filtroNome.getText(),
                parseIntSafe(filtroTempoMin.getText()),
                parseIntSafe(filtroTempoMax.getText()),
                parseIntSafe(filtroIngMin.getText()),
                parseIntSafe(filtroIngMax.getText())
            );
            ricetteData.setAll(filtrate);
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore filtri: " + e.getMessage());
        }
    }

    private void carica() {
        try {
            ricetteData.setAll(ricettaController.getAllRicette());
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore caricamento: " + e.getMessage());
        }
    }

    private void reset() {
        filtroNome.clear();
        filtroTempoMin.clear();
        filtroTempoMax.clear();
        filtroIngMin.clear();
        filtroIngMax.clear();
        carica();
    }

    private void ricarica() {
        try {
            ricettaController.ricaricaCache();
            carica();
            StyleHelper.showSuccessDialog("✅ Successo", "Ricette ricaricate");
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore ricaricamento: " + e.getMessage());
        }
    }

    private void crea() {
        try {
            CreaRicettaGUI gui = new CreaRicettaGUI(ricettaController, ingredienteController);
            Ricetta nuova = gui.showAndReturn();
            if (nuova != null) {
                carica();
            }
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore creazione: " + e.getMessage());
        }
    }

    private void mostraModificaIngredienti(Ricetta ricetta) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Modifica Ricetta - " + ricetta.getNome());
        dialog.initModality(Modality.APPLICATION_MODAL);

        ButtonType salvaBtn = new ButtonType("💾 Salva Modifiche", ButtonBar.ButtonData.OK_DONE);
        ButtonType annullaBtn = new ButtonType("❌ Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType eliminaBtn = new ButtonType("🗑️ Elimina Ricetta", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(salvaBtn, eliminaBtn, annullaBtn);

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setPrefWidth(650);
        content.setPrefHeight(550);

        // Header
        Label titleLabel = new Label("📖 " + ricetta.getNome());
        titleLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        // Info base
        VBox infoSection = StyleHelper.createSection();
        infoSection.setSpacing(12);
        
        Label infoTitle = new Label("📝 Informazioni Base");
        infoTitle.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
        infoTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        TextField nomeField = StyleHelper.createTextField("Nome ricetta");
        nomeField.setText(ricetta.getNome());
        nomeField.setPrefHeight(40);

        TextField tempoField = StyleHelper.createTextField("Tempo (min)");
        tempoField.setText(String.valueOf(ricetta.getTempoPreparazione()));
        tempoField.setPrefHeight(40);
        tempoField.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*")) tempoField.setText(old);
        });

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(15);
        infoGrid.setVgap(12);
        infoGrid.add(StyleHelper.createLabel("Nome:"), 0, 0);
        infoGrid.add(nomeField, 1, 0);
        infoGrid.add(StyleHelper.createLabel("Tempo (min):"), 0, 1);
        infoGrid.add(tempoField, 1, 1);

        infoSection.getChildren().addAll(infoTitle, infoGrid);

        // Sezione ingredienti
        VBox ingredientiSection = StyleHelper.createSection();
        ingredientiSection.setSpacing(12);

        Label ingTitle = new Label("🥕 Ingredienti");
        ingTitle.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
        ingTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        Button aggiungiIngBtn = StyleHelper.createSuccessButton("➕ Aggiungi Ingrediente");
        aggiungiIngBtn.setPrefWidth(200);

        VBox listaIngredienti = new VBox(10);
        listaIngredienti.setPadding(new Insets(10));
        listaIngredienti.setStyle(
            "-fx-background-color: " + StyleHelper.BG_LIGHT + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + StyleHelper.BORDER_LIGHT + ";" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 1;"
        );

        ScrollPane ingScroll = new ScrollPane(listaIngredienti);
        ingScroll.setFitToWidth(true);
        ingScroll.setPrefHeight(200);
        ingScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        Map<Ingrediente, Double> ingredientiMap = new HashMap<>(ricetta.getIngredienti());

        Runnable aggiornaLista = () -> {
            listaIngredienti.getChildren().clear();
            if (ingredientiMap.isEmpty()) {
                Label empty = new Label("Nessun ingrediente");
                empty.setFont(Font.font("Roboto", 13));
                empty.setTextFill(Color.GRAY);
                empty.setStyle("-fx-font-style: italic;");
                listaIngredienti.getChildren().add(empty);
            } else {
                ingredientiMap.forEach((ing, q) -> {
                    HBox box = createModificaIngredienteBox(ing, q, ingredientiMap, listaIngredienti);
                    listaIngredienti.getChildren().add(box);
                });
            }
        };

        aggiornaLista.run();

        aggiungiIngBtn.setOnAction(e -> {
            // ✅ USA VisualizzaIngredientiGUI invece di SelezionaIngredienteDialog
            Stage selStage = new Stage();
            selStage.initModality(Modality.APPLICATION_MODAL);
            selStage.initStyle(StageStyle.UNDECORATED);
            
            StackPane selRoot = new StackPane();
            selRoot.setMinSize(800, 700);
            
            Region selBg = new Region();
            StyleHelper.applyBackgroundGradient(selBg);
            
            VBox selMain = new VBox(20);
            selMain.setAlignment(Pos.TOP_CENTER);
            selMain.setPadding(new Insets(30, 25, 25, 25));
            
            Label selTitle = new Label("🥕 Aggiungi Ingrediente");
            selTitle.setFont(Font.font("Roboto", FontWeight.BOLD, 24));
            selTitle.setTextFill(Color.WHITE);
            
            VisualizzaIngredientiGUI selGUI = new VisualizzaIngredientiGUI(ingredienteController);
            selGUI.setModalitaSelezione(true);
            selGUI.setOnIngredienteSelezionato(nuovoIng -> {
                if (ingredientiMap.containsKey(nuovoIng)) {
                    StyleHelper.showValidationDialog("Attenzione", "Ingrediente già presente");
                } else {
                    selStage.close();
                    chiediQuantitaPerModifica(nuovoIng, q -> {
                        ingredientiMap.put(nuovoIng, q);
                        aggiornaLista.run();
                    });
                }
            });
            
            VBox selContent = selGUI.getContent();
            VBox.setVgrow(selContent, Priority.ALWAYS);
            
            selMain.getChildren().addAll(selTitle, selContent);
            
            HBox selWinBtns = new HBox(3);
            Button selClose = new Button("✕");
            selClose.setPrefSize(30, 30);
            selClose.setStyle("-fx-background-color: rgba(255,140,0,0.8); -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand;");
            selClose.setOnAction(ev -> selStage.close());
            selWinBtns.getChildren().add(selClose);
            selWinBtns.setAlignment(Pos.TOP_RIGHT);
            selWinBtns.setPickOnBounds(false);
            
            selRoot.getChildren().addAll(selBg, selMain, selWinBtns);
            StackPane.setAlignment(selWinBtns, Pos.TOP_RIGHT);
            StackPane.setMargin(selWinBtns, new Insets(8));
            
            final double[] xOff = {0}, yOff = {0};
            selRoot.setOnMousePressed(ev -> { xOff[0] = ev.getSceneX(); yOff[0] = ev.getSceneY(); });
            selRoot.setOnMouseDragged(ev -> { selStage.setX(ev.getScreenX() - xOff[0]); selStage.setY(ev.getScreenY() - yOff[0]); });
            
            Scene selScene = new Scene(selRoot, 850, 750);
            selScene.setFill(Color.TRANSPARENT);
            selStage.setScene(selScene);
            selStage.showAndWait();
        });

        ingredientiSection.getChildren().addAll(ingTitle, aggiungiIngBtn, ingScroll);

        content.getChildren().addAll(titleLabel, infoSection, ingredientiSection);
        dialog.getDialogPane().setContent(content);

        dialog.getDialogPane().setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: " + StyleHelper.PRIMARY_ORANGE + ";" +
            "-fx-border-width: 3;" +
            "-fx-border-radius: 20;" +
            "-fx-background-radius: 20;"
        );

        // Gestione bottoni
        Button eliminaButton = (Button) dialog.getDialogPane().lookupButton(eliminaBtn);
        eliminaButton.setStyle(
            "-fx-background-color: " + StyleHelper.ERROR_RED + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 20;" +
            "-fx-padding: 10 20;"
        );

        eliminaButton.setOnAction(e -> {
            StyleHelper.showConfirmationDialog(
                "Conferma Eliminazione",
                "Sei sicuro di voler eliminare la ricetta '" + ricetta.getNome() + "'?",
                () -> {
                    try {
                        ricettaController.eliminaRicetta(ricetta.getIdRicetta());
                        dialog.setResult(true);
                        dialog.close();
                        carica();
                        StyleHelper.showSuccessDialog("✅ Successo", "Ricetta eliminata");
                    } catch (Exception ex) {
                        StyleHelper.showErrorDialog("Errore", "Errore eliminazione: " + ex.getMessage());
                    }
                }
            );
            e.consume();
        });

        dialog.setResultConverter(btn -> {
            if (btn == salvaBtn) {
                String nuovoNome = nomeField.getText().trim();
                String nuovoTempoStr = tempoField.getText().trim();

                if (nuovoNome.isEmpty()) {
                    StyleHelper.showValidationDialog("Errore", "Inserisci il nome");
                    return null;
                }

                if (nuovoTempoStr.isEmpty()) {
                    StyleHelper.showValidationDialog("Errore", "Inserisci il tempo");
                    return null;
                }

                int nuovoTempo;
                try {
                    nuovoTempo = Integer.parseInt(nuovoTempoStr);
                    if (nuovoTempo <= 0) {
                        StyleHelper.showValidationDialog("Errore", "Il tempo deve essere > 0");
                        return null;
                    }
                } catch (NumberFormatException ex) {
                    StyleHelper.showValidationDialog("Errore", "Tempo non valido");
                    return null;
                }

                if (ingredientiMap.isEmpty()) {
                    StyleHelper.showValidationDialog("Errore", "Aggiungi almeno un ingrediente");
                    return null;
                }

                try {
                    ricettaController.aggiornaRicetta(
                        ricetta.getIdRicetta(),
                        nuovoNome,
                        nuovoTempo,
                        ingredientiMap
                    );
                    carica();
                    StyleHelper.showSuccessDialog("✅ Successo", "Ricetta aggiornata!");
                    return true;
                } catch (Exception ex) {
                    StyleHelper.showErrorDialog("Errore", "Errore salvataggio: " + ex.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private HBox createModificaIngredienteBox(Ingrediente ing, double quantita, 
                                              Map<Ingrediente, Double> map, VBox container) {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(10));
        box.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + StyleHelper.PRIMARY_ORANGE + ";" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 2;"
        );

        Label icon = new Label("🥕");
        icon.setFont(Font.font("Segoe UI Emoji", 18));

        VBox info = new VBox(3);
        Label nome = new Label(ing.getNome());
        nome.setFont(Font.font("Roboto", FontWeight.BOLD, 13));
        Label tipo = new Label(ing.getTipo());
        tipo.setFont(Font.font("Roboto", 11));
        tipo.setTextFill(Color.GRAY);
        info.getChildren().addAll(nome, tipo);

        TextField quantField = new TextField(String.format("%.0f", quantita));
        quantField.setPrefWidth(70);
        quantField.setAlignment(Pos.CENTER);
        quantField.setStyle(
            "-fx-background-color: " + StyleHelper.BG_ORANGE_LIGHT + ";" +
            "-fx-border-color: " + StyleHelper.PRIMARY_ORANGE + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-font-weight: bold;"
        );
        quantField.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*")) quantField.setText(old);
        });

        Label unitLabel = new Label("g");
        unitLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 12));

        Button aggiornaBtn = new Button("✓");
        aggiornaBtn.setStyle(
            "-fx-background-color: " + StyleHelper.SUCCESS_GREEN + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 18;" +
            "-fx-min-width: 32;" +
            "-fx-min-height: 32;" +
            "-fx-font-size: 14px;" +
            "-fx-cursor: hand;"
        );
        aggiornaBtn.setOnAction(e -> {
            String val = quantField.getText().trim();
            if (!val.isEmpty()) {
                try {
                    double nuovaQ = Double.parseDouble(val);
                    if (nuovaQ > 0) {
                        map.put(ing, nuovaQ);
                        StyleHelper.showSuccessDialog("✅ Aggiornato", 
                            String.format("Quantità aggiornata: %.0fg", nuovaQ));
                    }
                } catch (NumberFormatException ex) {
                    StyleHelper.showValidationDialog("Errore", "Valore non valido");
                }
            }
        });

        Button rimuoviBtn = new Button("✕");
        rimuoviBtn.setStyle(
            "-fx-background-color: " + StyleHelper.ERROR_RED + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 18;" +
            "-fx-min-width: 32;" +
            "-fx-min-height: 32;" +
            "-fx-font-size: 14px;" +
            "-fx-cursor: hand;"
        );
        rimuoviBtn.setOnAction(e -> {
            map.remove(ing);
            container.getChildren().remove(box);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        box.getChildren().addAll(icon, info, spacer, quantField, unitLabel, aggiornaBtn, rimuoviBtn);
        return box;
    }

    private void chiediQuantitaPerModifica(Ingrediente ing, java.util.function.Consumer<Double> onSuccess) {
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
            btn.setOnMouseEntered(ev -> btn.setStyle(
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
            btn.setOnMouseExited(ev -> btn.setStyle(
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
            btn.setOnAction(ev -> field.setText(q.replace("g", "")));
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

        dialog.showAndWait().ifPresent(onSuccess);
    }

    private void seleziona() {
        Ricetta sel = ricetteListView.getSelectionModel().getSelectedItem();
        if (sel == null) {
            StyleHelper.showValidationDialog("Validazione", "Seleziona una ricetta");
        } else {
            ricettaSelezionata = sel;
        }
    }

    // ==================== HELPER ====================

    private Label createTitle(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
        lbl.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));
        return lbl;
    }

    private HBox createRange(TextField min, TextField max, String unit) {
        HBox box = new HBox(5, min, new Label("-"), max);
        if (!unit.isEmpty()) box.getChildren().add(new Label(unit));
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Integer parseIntSafe(String txt) {
        if (txt == null || txt.trim().isEmpty()) return null;
        try { 
            return Integer.parseInt(txt.trim()); 
        } catch (NumberFormatException e) { 
            return null; 
        }
    }

    // ==================== CELL RENDERER ====================

    private static class RicettaCell extends ListCell<Ricetta> {
        @Override
        protected void updateItem(Ricetta item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(buildCell(item));
            }
        }

        private HBox buildCell(Ricetta r) {
            Label nome = new Label("📖 " + r.getNome());
            nome.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
            nome.setTextFill(Color.BLACK);

            Label tempo = new Label("⏱️ " + r.getTempoPreparazione() + " min");
            tempo.setFont(Font.font("Roboto", 12));
            tempo.setTextFill(Color.web(StyleHelper.INFO_BLUE));

            Label ing = new Label("🥕 " + r.getNumeroIngredienti() + " ingredienti");
            ing.setFont(Font.font("Roboto", 12));
            ing.setTextFill(Color.web(StyleHelper.SUCCESS_GREEN));

            HBox details = new HBox(20, tempo, ing);
            details.setAlignment(Pos.CENTER_LEFT);

            VBox info = new VBox(5, nome, details);
            HBox cell = new HBox(info);
            cell.setPadding(new Insets(12));
            cell.setAlignment(Pos.CENTER_LEFT);
            cell.setStyle(
                "-fx-background-color: #f8f9fa;" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " + StyleHelper.BORDER_LIGHT + ";" +
                "-fx-border-radius: 8;" +
                "-fx-border-width: 1;"
            );
            return cell;
        }
    }
}