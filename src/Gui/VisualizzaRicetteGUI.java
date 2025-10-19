package Gui;

import controller.RicettaController;
import controller.IngredienteController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Ricetta;
import model.Ingrediente;
import guihelper.StyleHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ✅ SCHERMATA CONTINUA per visualizzare e modificare ricette
 * - Doppio click su ricetta → Mostra form modifica nella stessa schermata
 * - Nomi ingredienti sempre visibili
 * - Pulsante verde solo per "Salva" finale
 * - Sfondo uniforme arancione e GUI adattabile
 */
public class VisualizzaRicetteGUI {
    private final RicettaController ricettaController;
    private final IngredienteController ingredienteController;
    private final ObservableList<Ricetta> ricetteData;
    private ListView<Ricetta> ricetteListView;
    private TextField filtroNome, filtroTempoMin, filtroTempoMax;
    private TextField filtroIngMin, filtroIngMax;

    private VBox mainContainer;
    private VBox listaView;
    private VBox modificaView;

    public VisualizzaRicetteGUI(RicettaController ricettaController, IngredienteController ingredienteController) {
        if (ricettaController == null || ingredienteController == null) {
            throw new IllegalArgumentException("I controller non possono essere null");
        }
        this.ricettaController = ricettaController;
        this.ingredienteController = ingredienteController;
        this.ricetteData = FXCollections.observableArrayList();
        carica();
    }

    public VBox getRoot() {
        if (mainContainer == null) {
            mainContainer = new VBox();
            listaView = buildListaLayout();
            mainContainer.getChildren().add(listaView);
            VBox.setVgrow(listaView, Priority.ALWAYS);
        }
        return mainContainer;
    }

    // ==================== LAYOUT LISTA ====================

    private VBox buildListaLayout() {
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
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Separator bottomSep = new Separator();
        HBox buttons = buildButtons();

        container.getChildren().addAll(title, scroll, bottomSep, buttons);
        setupListeners();
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
        Label info = new Label("💡 Doppio click per modificare una ricetta");
        info.setFont(Font.font("Roboto", 12));
        info.setTextFill(Color.web(StyleHelper.INFO_BLUE));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(new VBox(5, title, info));

        ricetteListView = new ListView<>(ricetteData);
        ricetteListView.setPrefHeight(400);
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
                    mostraModifica(sel);
                }
            }
        });

        section.getChildren().addAll(header, ricetteListView);
        VBox.setVgrow(ricetteListView, Priority.ALWAYS);
        return section;
    }

    private HBox buildButtons() {
        Button aggiornaBtn = StyleHelper.createInfoButton("🔄 Aggiorna");
        aggiornaBtn.setPrefWidth(150);
        aggiornaBtn.setOnAction(e -> ricarica());

        HBox box = new HBox(15, aggiornaBtn);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15, 0, 5, 0));
        return box;
    }

    // ==================== LAYOUT MODIFICA (SCHERMATA CONTINUA) ====================

    private VBox buildModificaLayout(Ricetta ricetta) {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        StyleHelper.applyBackgroundGradient(container);

        Label title = StyleHelper.createTitleLabel("📖 Modifica: " + ricetta.getNome());
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);

        Map<Ingrediente, Double> ingredientiMap = new HashMap<>(ricetta.getIngredienti());

        ScrollPane scroll = new ScrollPane(new VBox(15,
                buildModificaInfoSection(ricetta),
                new Separator(),
                buildModificaIngredientiSection(ricetta, ingredientiMap)
        ));
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Separator bottomSep = new Separator();
        HBox buttons = buildModificaButtons(ricetta, ingredientiMap);

        container.getChildren().addAll(title, scroll, bottomSep, buttons);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return container;
    }

    private VBox buildModificaInfoSection(Ricetta ricetta) {
        VBox section = StyleHelper.createSection();
        Label title = createTitle("📝 Informazioni Base");

        TextField nomeField = StyleHelper.createTextField("Nome ricetta");
        nomeField.setText(ricetta.getNome());
        nomeField.setPrefHeight(45);
        nomeField.setUserData("nomeField");

        TextField tempoField = StyleHelper.createTextField("Tempo (min)");
        tempoField.setText(String.valueOf(ricetta.getTempoPreparazione()));
        tempoField.setPrefHeight(45);
        tempoField.setUserData("tempoField");
        tempoField.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*")) tempoField.setText(old);
        });

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.add(StyleHelper.createLabel("Nome:"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(StyleHelper.createLabel("Tempo (min):"), 0, 1);
        grid.add(tempoField, 1, 1);

        section.getChildren().addAll(title, grid);
        section.setUserData("infoSection");
        return section;
    }

    private VBox buildModificaIngredientiSection(Ricetta ricetta, Map<Ingrediente, Double> ingredientiMap) {
        VBox section = StyleHelper.createSection();
        Label title = createTitle("🥕 Gestione Ingredienti");

        Button aggiungiBtn = StyleHelper.createSuccessButton("➕ Aggiungi Ingrediente");
        aggiungiBtn.setPrefWidth(220);

        Label info = new Label("💡 Modifica la quantità direttamente. Clicca ✕ per rimuovere.");
        info.setFont(Font.font("Roboto", 12));
        info.setTextFill(Color.web(StyleHelper.INFO_BLUE));
        info.setWrapText(true);

        VBox listaIngredienti = new VBox(12);
        listaIngredienti.setPadding(new Insets(15));
        listaIngredienti.setStyle(
                "-fx-background-color: " + StyleHelper.BG_LIGHT + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: " + StyleHelper.BORDER_LIGHT + ";" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;"
        );

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
                    HBox row = createIngredienteRow(ing, q, ingredientiMap, listaIngredienti);
                    listaIngredienti.getChildren().add(row);
                });
            }
        };

        aggiornaLista.run();

        aggiungiBtn.setOnAction(e -> mostraSelezionaIngrediente(ingredientiMap, aggiornaLista));

        section.getChildren().addAll(title, aggiungiBtn, info, listaIngredienti);
        VBox.setVgrow(listaIngredienti, Priority.ALWAYS);
        return section;
    }

    private HBox createIngredienteRow(Ingrediente ing, double quantita,
                                       Map<Ingrediente, Double> map, VBox container) {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(12));
        box.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: " + StyleHelper.PRIMARY_ORANGE + ";" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 2;"
        );

        Label icon = new Label("🥕");
        icon.setFont(Font.font("Segoe UI Emoji", 20));

        VBox info = new VBox(4);
        Label nomeLabel = new Label(ing.getNome());
        nomeLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
        nomeLabel.setTextFill(Color.BLACK);

        Label tipoLabel = new Label("📂 " + ing.getTipo());
        tipoLabel.setFont(Font.font("Roboto", 12));
        tipoLabel.setTextFill(Color.GRAY);

        info.getChildren().addAll(nomeLabel, tipoLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField quantField = StyleHelper.createTextField(String.format("%.0f", quantita));
        quantField.setPrefWidth(80);
        quantField.setPrefHeight(35);
        quantField.setAlignment(Pos.CENTER);
        quantField.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*")) {
                quantField.setText(old);
            } else if (!val.isEmpty()) {
                try {
                    double nuovaQ = Double.parseDouble(val);
                    if (nuovaQ > 0) {
                        map.put(ing, nuovaQ);
                    }
                } catch (NumberFormatException e) {
                }
            }
        });

        Label unit = new Label("g");
        unit.setFont(Font.font("Roboto", FontWeight.SEMI_BOLD, 13));
        unit.setTextFill(Color.web(StyleHelper.TEXT_GRAY));

        Button rimuoviBtn = StyleHelper.createDangerButton("✕");
        rimuoviBtn.setPrefSize(40, 40);
        rimuoviBtn.setOnAction(e -> {
            map.remove(ing);
            container.getChildren().remove(box);
        });

        box.getChildren().addAll(icon, info, spacer, quantField, unit, rimuoviBtn);
        return box;
    }

    private HBox buildModificaButtons(Ricetta ricetta, Map<Ingrediente, Double> ingredientiMap) {
        Button indietroBtn = StyleHelper.createSecondaryButton("← Indietro");
        indietroBtn.setPrefWidth(140);
        indietroBtn.setOnAction(e -> mostraLista());

        Button eliminaBtn = StyleHelper.createDangerButton("🗑️ Elimina");
        eliminaBtn.setPrefWidth(140);
        eliminaBtn.setOnAction(e -> StyleHelper.showConfirmationDialog(
                "Conferma Eliminazione",
                "Sei sicuro di voler eliminare '" + ricetta.getNome() + "'?",
                () -> {
                    try {
                        ricettaController.eliminaRicetta(ricetta.getIdRicetta());
                        carica();
                        StyleHelper.showSuccessDialog("✅ Successo", "Ricetta eliminata");
                        mostraLista();
                    } catch (Exception ex) {
                        StyleHelper.showErrorDialog("Errore", ex.getMessage());
                    }
                }
        ));

        Button salvaBtn = StyleHelper.createSuccessButton("💾 Salva");
        salvaBtn.setPrefWidth(140);
        salvaBtn.setOnAction(e -> salvaModifica(ricetta, ingredientiMap));

        HBox box = new HBox(15, indietroBtn, eliminaBtn, salvaBtn);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15, 0, 5, 0));
        return box;
    }

    // ==================== SELEZIONE INGREDIENTE (SCHERMATA CONTINUA) ====================

    private void mostraSelezionaIngrediente(Map<Ingrediente, Double> ingredientiMap, Runnable aggiornaLista) {
        VBox selezionaView = new VBox(20);
        selezionaView.setPadding(new Insets(20));
        StyleHelper.applyBackgroundGradient(selezionaView);

        Label title = StyleHelper.createTitleLabel("🥕 Seleziona Ingrediente");
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);

        VisualizzaIngredientiGUI selGUI = new VisualizzaIngredientiGUI(ingredienteController);
        selGUI.setModalitaSelezione(true);
        selGUI.setOnIngredienteSelezionato(ing -> {
            if (ingredientiMap.containsKey(ing)) {
                StyleHelper.showValidationDialog("Attenzione", "Ingrediente già presente");
            } else {
                chiediQuantitaInline(ing, q -> {
                    ingredientiMap.put(ing, q);
                    aggiornaLista.run();
                    mainContainer.getChildren().setAll(modificaView);
                });
            }
        });

        VBox content = selGUI.getContent();
        VBox.setVgrow(content, Priority.ALWAYS);

        Button indietroBtn = StyleHelper.createSecondaryButton("← Indietro");
        indietroBtn.setPrefWidth(150);
        indietroBtn.setOnAction(e -> mainContainer.getChildren().setAll(modificaView));

        HBox buttons = new HBox(indietroBtn);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(15, 0, 5, 0));

        selezionaView.getChildren().addAll(title, content, new Separator(), buttons);
        mainContainer.getChildren().setAll(selezionaView);
    }

    private void chiediQuantitaInline(Ingrediente ing, java.util.function.Consumer<Double> onSuccess) {
        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Quantità");
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        ButtonType aggiungiBtn = new ButtonType("Aggiungi", ButtonBar.ButtonData.OK_DONE);
        ButtonType annullaBtn = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(aggiungiBtn, annullaBtn);

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.CENTER);

        Label nomeLabel = new Label(ing.getNome());
        nomeLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 20));
        nomeLabel.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        TextField field = StyleHelper.createTextField("Es. 250");
        field.setPrefWidth(200);
        field.setPrefHeight(45);
        field.setAlignment(Pos.CENTER);
        field.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*")) field.setText(old);
        });

        Label unit = new Label("grammi (g)");

        content.getChildren().addAll(nomeLabel, field, unit);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == aggiungiBtn && !field.getText().isEmpty()) {
                try {
                    double q = Double.parseDouble(field.getText());
                    return q > 0 ? q : null;
                } catch (NumberFormatException e) {
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(onSuccess);
    }

    // ==================== NAVIGAZIONE ====================

    private void mostraLista() {
        carica();
        mainContainer.getChildren().setAll(listaView);
    }

    private void mostraModifica(Ricetta ricetta) {
        modificaView = buildModificaLayout(ricetta);
        mainContainer.getChildren().setAll(modificaView);
    }

    private void salvaModifica(Ricetta ricetta, Map<Ingrediente, Double> ingredientiMap) {
        VBox infoSection = (VBox) modificaView.lookup("[userData='infoSection']");
        if (infoSection == null) return;

        TextField nomeField = null;
        TextField tempoField = null;

        for (javafx.scene.Node node : infoSection.getChildren()) {
            if (node instanceof GridPane) {
                for (javafx.scene.Node child : ((GridPane) node).getChildren()) {
                    if (child instanceof TextField tf) {
                        if ("nomeField".equals(tf.getUserData())) nomeField = tf;
                        if ("tempoField".equals(tf.getUserData())) tempoField = tf;
                    }
                }
            }
        }

        if (nomeField == null || tempoField == null) return;

        String nome = nomeField.getText();
        String tempoStr = tempoField.getText();

        if (nome == null || nome.trim().isEmpty()) {
            StyleHelper.showValidationDialog("Errore", "Inserisci il nome");
            return;
        }

        int tempo;
        try {
            tempo = Integer.parseInt(tempoStr.trim());
        } catch (NumberFormatException e) {
            StyleHelper.showValidationDialog("Errore", "Tempo non valido");
            return;
        }

        if (ingredientiMap.isEmpty()) {
            StyleHelper.showValidationDialog("Errore", "Aggiungi almeno un ingrediente");
            return;
        }

        try {
            ricettaController.aggiornaRicetta(
                    ricetta.getIdRicetta(),
                    nome.trim(),
                    tempo,
                    ingredientiMap
            );
            StyleHelper.showSuccessDialog("✅ Successo", "Ricetta aggiornata!");
            mostraLista();
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", e.getMessage());
        }
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
            StyleHelper.showErrorDialog("Errore", e.getMessage());
        }
    }

    private void carica() {
        try {
            ricetteData.setAll(ricettaController.getAllRicette());
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", e.getMessage());
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
            StyleHelper.showErrorDialog("Errore", e.getMessage());
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
