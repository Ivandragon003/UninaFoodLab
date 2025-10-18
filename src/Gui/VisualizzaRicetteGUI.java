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
import exceptions.ValidationException;
import exceptions.DataAccessException;
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

    public void setSelectionMode(boolean mode) { this.modalitaSelezione = mode; }
    public Ricetta showAndReturn() { return ricettaSelezionata; }

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

        // ✅ ScrollPane SENZA i bottoni
        VBox scrollContent = new VBox(15,
            buildFiltri(),
            new Separator(),
            buildLista()
            // ❌ NON includere buildButtons() qui
        );

        ScrollPane scroll = new ScrollPane(scrollContent);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        // ✅ Scroll prende tutto lo spazio disponibile
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // ✅ Separator prima dei bottoni
        Separator bottomSep = new Separator();

        // ✅ Bottoni FUORI dallo scroll, fissi in basso
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
        Label info = new Label("💡 Doppio click per vedere ingredienti");
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
                    if (modalitaSelezione) seleziona();
                    else mostraIngredienti(sel);
                }
            }
        });

        section.getChildren().addAll(header, ricetteListView);
        return section;
    }

    private HBox buildButtons() {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER);
        
        // ✅ Padding ridotto per evitare troppo spazio
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
        } catch (ValidationException e) {
            StyleHelper.showValidationDialog("Validazione", e.getMessage());
        } catch (DataAccessException e) {
            StyleHelper.showErrorDialog("Errore Database", e.getMessage());
        } catch (Exception e) {
            error("Errore filtri", e);
        }
    }

    private void carica() {
        try {
            ricetteData.setAll(ricettaController.getAllRicette());
        } catch (DataAccessException e) {
            StyleHelper.showErrorDialog("Errore Database", "Errore caricamento: " + e.getMessage());
        } catch (Exception e) {
            error("Errore caricamento", e);
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
            StyleHelper.showSuccessDialog("Successo", "Ricette ricaricate");
        } catch (DataAccessException e) {
            StyleHelper.showErrorDialog("Errore Database", "Errore ricaricamento: " + e.getMessage());
        } catch (Exception e) {
            error("Errore ricaricamento", e);
        }
    }

    private void crea() {
        try {
            CreaRicettaGUI gui = new CreaRicettaGUI(ricettaController, ingredienteController);
            Ricetta nuova = gui.showAndReturn();
            if (nuova != null) {
                carica();
                StyleHelper.showSuccessDialog("Successo", "Ricetta '" + nuova.getNome() + "' creata!");
            }
        } catch (Exception e) {
            error("Errore creazione", e);
        }
    }

    private void mostraIngredienti(Ricetta r) {
        StringBuilder sb = new StringBuilder();
        sb.append("⏱️ Tempo: ").append(r.getTempoPreparazione()).append(" minuti\n");
        sb.append("🥕 Totale: ").append(r.getNumeroIngredienti()).append(" ingredienti\n\n");

        Map<Ingrediente, Double> ing = r.getIngredienti();
        if (ing == null || ing.isEmpty()) {
            sb.append("Nessun ingrediente trovato");
        } else {
            ing.forEach((i, q) -> sb.append(String.format("🥕 %s (%s) - %.0fg\n", i.getNome(), i.getTipo(), q)));
        }

        StyleHelper.showInfoDialog("Ingredienti - " + r.getNome(), sb.toString());
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
        try { return Integer.parseInt(txt.trim()); } 
        catch (NumberFormatException e) { return null; }
    }

    private void error(String msg, Exception e) {
        System.err.println("ERROR: " + msg + ": " + e.getMessage());
        e.printStackTrace();
        StyleHelper.showErrorDialog("Errore", msg + ": " + e.getMessage());
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
