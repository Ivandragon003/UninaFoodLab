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
import model.Ricetta;
import model.Ingrediente;
import guihelper.StyleHelper;
import exceptions.ValidationException;
import exceptions.DataAccessException;
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

	// ✅ Per showAndReturn()
	private Ricetta ricettaCreata = null;
	private Stage dialog;

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
		return buildMainLayout();
	}

	// ==================== LAYOUT ====================

	private VBox buildMainLayout() {
    VBox container = new VBox(15);
    container.setPadding(new Insets(20));
    StyleHelper.applyBackgroundGradient(container);

    Label title = StyleHelper.createTitleLabel("Crea Nuova Ricetta");
    title.setTextFill(Color.WHITE);
    title.setAlignment(Pos.CENTER);

    // ✅ ScrollPane SENZA i bottoni
    ScrollPane scroll = new ScrollPane(new VBox(15,
        buildInfoSection(),
        new Separator(),
        buildIngredientiSection()
       
    ));
    scroll.setFitToWidth(true);
    scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
    
    // ✅ Imposta altezza massima per scroll
    VBox.setVgrow(scroll, Priority.ALWAYS);

    // ✅ Separator prima dei bottoni
    Separator bottomSep = new Separator();
    
    // ✅ Bottoni FUORI dallo scroll, fissi in basso
    HBox buttons = buildButtons();

    container.getChildren().addAll(title, scroll, bottomSep, buttons);
    return container;
}


	private VBox buildInfoSection() {
		VBox section = StyleHelper.createSection();
		Label title = createSectionTitle("Informazioni Ricetta");

		nomeField = StyleHelper.createTextField("Es. Carbonara Classica");
		tempoField = StyleHelper.createTextField("Es. 30");
		tempoField.textProperty().addListener((obs, old, val) -> {
			if (!val.matches("\\d*"))
				tempoField.setText(old);
		});

		GridPane grid = new GridPane();
		grid.setHgap(15);
		grid.setVgap(15);
		grid.add(StyleHelper.createLabel("Nome Ricetta:"), 0, 0);
		grid.add(nomeField, 1, 0);
		grid.add(StyleHelper.createLabel("Tempo preparazione (min):"), 0, 1);
		grid.add(tempoField, 1, 1);

		section.getChildren().addAll(title, grid);
		return section;
	}

	private VBox buildIngredientiSection() {
		VBox section = StyleHelper.createSection();
		Label title = createSectionTitle("Selezione Ingredienti");

		Button selezionaBtn = StyleHelper.createPrimaryButton("🔍 Seleziona Ingredienti");
		selezionaBtn.setOnAction(e -> selezionaIngrediente());

		Label suggerimento = new Label(
				"💡 Seleziona ingredienti dalla lista. Puoi crearne di nuovi dalla finestra di selezione.");
		suggerimento.setFont(Font.font("Roboto", 11));
		suggerimento.setTextFill(Color.web(StyleHelper.INFO_BLUE));
		suggerimento.setStyle("-fx-font-style: italic;");
		suggerimento.setWrapText(true);

		contatoreLabel = new Label("📊 Ingredienti aggiunti: 0");
		contatoreLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 13));
		updateContatore();

		listaIngredientiContainer = createListContainer();
		updateDisplay();

		section.getChildren().addAll(title, selezionaBtn, suggerimento, contatoreLabel,
				StyleHelper.createLabel("Ingredienti Selezionati:"), listaIngredientiContainer);
		return section;
	}

	private HBox buildButtons() {
		Button resetBtn = StyleHelper.createSecondaryButton("🔄 Reset Form");
		resetBtn.setPrefWidth(150);
		resetBtn.setOnAction(e -> handleReset());

		Button salvaBtn = StyleHelper.createSuccessButton("✅ Salva Ricetta");
		salvaBtn.setPrefWidth(150);
		salvaBtn.setOnAction(e -> salva());

		HBox box = new HBox(15, resetBtn, salvaBtn);
		box.setAlignment(Pos.CENTER);
		box.setPadding(new Insets(20, 0, 0, 0));
		return box;
	}

	// ==================== LOGICA ====================

	private void handleReset() {
		if (onAnnulla != null) {
			onAnnulla.run();
		} else {
			// Se chiamato da showAndReturn, chiudi dialog
			if (dialog != null) {
				ricettaCreata = null;
				dialog.close();
			} else {
				clearForm();
			}
		}
	}

private void selezionaIngrediente() {
    try {
        // ✅ USA IL DIALOG DEDICATO
        SelezionaIngredienteDialog dialog = new SelezionaIngredienteDialog(ingredienteController);
        Ingrediente ing = dialog.showAndReturn();

        if (ing != null) {
            if (ingredientiMap.containsKey(ing)) {
                StyleHelper.showValidationDialog("Attenzione", "Ingrediente già presente");
                return;
            }
            chiediQuantita(ing);
        }
    } catch (Exception e) {
        StyleHelper.showErrorDialog("Errore", "Errore selezione: " + e.getMessage());
    }
}


	private void chiediQuantita(Ingrediente ing) {
    Dialog<Double> dialog = new Dialog<>();
    dialog.setTitle("📊 Quantità Ingrediente");
    dialog.initModality(Modality.APPLICATION_MODAL);

    ButtonType aggiungiBtn = new ButtonType("✅ Aggiungi", ButtonBar.ButtonData.OK_DONE);
    ButtonType annullaBtn = new ButtonType("❌ Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
    dialog.getDialogPane().getButtonTypes().addAll(aggiungiBtn, annullaBtn);

    VBox content = new VBox(15);
    content.setPadding(new Insets(25));
    content.setAlignment(Pos.CENTER_LEFT);
    
   
    Label icon = new Label("🥕");
    icon.setFont(Font.font("Segoe UI", 48));
    icon.setAlignment(Pos.CENTER);
    icon.setMaxWidth(Double.MAX_VALUE);
    
   
    Label nome = new Label(ing.getNome());
    nome.setFont(Font.font("Roboto", FontWeight.BOLD, 20));
    nome.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));
    nome.setAlignment(Pos.CENTER);
    nome.setMaxWidth(Double.MAX_VALUE);

    // ✅ TIPO GRIGIO
    Label tipo = new Label("📂 Tipo: " + ing.getTipo());
    tipo.setFont(Font.font("Roboto", 13));
    tipo.setTextFill(Color.GRAY);

    Separator sep = new Separator();

    // ✅ ISTRUZIONI
    Label istr = new Label("Inserisci la quantità in grammi per questa ricetta:");
    istr.setFont(Font.font("Roboto", 13));
    istr.setWrapText(true);

    // ✅ CAMPO INPUT ARANCIONE ARROTONDATO
    TextField field = new TextField();
    field.setPromptText("");
    field.setPrefWidth(180);
    field.setPrefHeight(45);
    field.setStyle(
        "-fx-background-color: white;" +
        "-fx-border-color: " + StyleHelper.PRIMARY_ORANGE + ";" +
        "-fx-border-width: 3;" +
        "-fx-border-radius: 20;" +
        "-fx-background-radius: 20;" +
        "-fx-padding: 12 18;" +
        "-fx-font-size: 15px;"
    );
    
    // Solo numeri
    field.textProperty().addListener((obs, old, val) -> {
        if (!val.matches("\\d*")) field.setText(old);
    });

    Label unit = new Label("grammi (g)");
    unit.setFont(Font.font("Roboto", 13));

    HBox quantBox = new HBox(12, field, unit);
    quantBox.setAlignment(Pos.CENTER_LEFT);

    // ✅ SUGGERIMENTI CELESTI
    Label sugg = new Label("💡 Suggerimenti: 100g, 250g, 500g, 1000g");
    sugg.setFont(Font.font("Roboto", 11));
    sugg.setTextFill(Color.web(StyleHelper.INFO_BLUE));
    sugg.setStyle(
        "-fx-background-color: #e3f2fd;" +
        "-fx-padding: 10 15;" +
        "-fx-background-radius: 8;"
    );
    sugg.setMaxWidth(Double.MAX_VALUE);

    content.getChildren().addAll(icon, nome, tipo, sep, istr, quantBox, sugg);

    dialog.getDialogPane().setContent(content);
    
    // ✅ BORDO ARANCIONE DIALOG
    dialog.getDialogPane().setStyle(
        "-fx-background-color: white;" +
        "-fx-border-color: " + StyleHelper.PRIMARY_ORANGE + ";" +
        "-fx-border-width: 4;" +
        "-fx-border-radius: 20;" +
        "-fx-background-radius: 20;"
    );

    dialog.getDialogPane().setMinWidth(420);
    dialog.getDialogPane().setMinHeight(450);

    javafx.application.Platform.runLater(() -> field.requestFocus());

    dialog.setResultConverter(btn -> {
        if (btn == aggiungiBtn) {
            try {
                String txt = field.getText().trim();
                if (txt.isEmpty()) return null;
                double q = Double.parseDouble(txt);
                return q > 0 ? q : null;
            } catch (NumberFormatException e) { return null; }
        }
        return null;
    });

    dialog.showAndWait().ifPresent(q -> {
        ingredientiMap.put(ing, q);
        updateDisplay();
        StyleHelper.showSuccessDialog("Ingrediente Aggiunto",
                String.format("✅ '%s' aggiunto con %.0fg", ing.getNome(), q));
    });
}

	private void salva() {
		try {
			String nome = nomeField.getText();
			String tempoStr = tempoField.getText();

			if (nome == null || nome.trim().isEmpty())
				throw new ValidationException("Inserisci il nome della ricetta");
			if (tempoStr == null || tempoStr.trim().isEmpty())
				throw new ValidationException("Inserisci il tempo di preparazione");

			int tempo = Integer.parseInt(tempoStr.trim());
			if (tempo <= 0)
				throw new ValidationException("Il tempo deve essere maggiore di zero");
			if (ingredientiMap.isEmpty())
				throw new ValidationException("Aggiungi almeno un ingrediente");

			Ricetta ricetta = ricettaController.creaRicetta(nome.trim(), tempo, ingredientiMap);

			// ✅ LOGICA IBRIDA
			if (onRicettaCreata != null) {
				// Modalità callback
				onRicettaCreata.accept(ricetta);
			} else {
				// Modalità showAndReturn
				ricettaCreata = ricetta;
				if (dialog != null) {
					StyleHelper.showSuccessDialog("Successo",
							String.format("✅ Ricetta '%s' creata!\n\n⏱️ Tempo: %d min\n🥕 Ingredienti: %d", nome.trim(),
									tempo, ingredientiMap.size()));
					dialog.close();
				} else {
					StyleHelper.showSuccessDialog("Successo",
							String.format("✅ Ricetta '%s' creata!\n\n⏱️ Tempo: %d min\n🥕 Ingredienti: %d", nome.trim(),
									tempo, ingredientiMap.size()));
					clearForm();
				}
			}
		} catch (NumberFormatException e) {
			StyleHelper.showValidationDialog("Errore", "Inserisci un numero valido per il tempo");
		} catch (ValidationException e) {
			StyleHelper.showValidationDialog("Validazione", e.getMessage());
		} catch (DataAccessException e) {
			StyleHelper.showErrorDialog("Errore Database", e.getMessage());
		} catch (Exception e) {
			StyleHelper.showErrorDialog("Errore", "Errore: " + e.getMessage());
		}
	}

	private void updateDisplay() {
		listaIngredientiContainer.getChildren().clear();

		if (ingredientiMap.isEmpty()) {
			Label ph = new Label(
					"Nessun ingrediente aggiunto\n\nUsa il pulsante '🔍 Seleziona Ingredienti' per iniziare");
			ph.setTextFill(Color.GRAY);
			ph.setFont(Font.font("Roboto", 13));
			ph.setStyle("-fx-font-style: italic;");
			ph.setAlignment(Pos.CENTER);
			ph.setMaxWidth(Double.MAX_VALUE);
			listaIngredientiContainer.getChildren().add(ph);
		} else {
			ingredientiMap.forEach((ing, q) -> listaIngredientiContainer.getChildren().add(createBox(ing, q)));
		}
		updateContatore();
	}

	private void updateContatore() {
		int count = ingredientiMap.size();
		contatoreLabel.setText("📊 Ingredienti aggiunti: " + count);
		contatoreLabel.setStyle(count == 0
				? "-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-padding: 8 12; -fx-background-radius: 6; -fx-font-weight: bold;"
				: "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-padding: 8 12; -fx-background-radius: 6; -fx-font-weight: bold;");
	}

	private HBox createBox(Ingrediente ing, double q) {
		HBox box = new HBox(10);
		box.setAlignment(Pos.CENTER_LEFT);
		box.setPadding(new Insets(8));
		box.setStyle("-fx-background-color: white; " + "-fx-background-radius: 6; " + "-fx-border-color: #FF6600; "
				+ "-fx-border-radius: 6; " + "-fx-border-width: 1.5;");

		Label lbl = new Label(String.format("🥕 %s - %.0fg (%s)", ing.getNome(), q, ing.getTipo()));
		lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Button btn = new Button("X");
		btn.setStyle("-fx-background-color: #e74c3c; " + "-fx-text-fill: white; " + "-fx-font-weight: bold; "
				+ "-fx-background-radius: 15; " + "-fx-cursor: hand; " + "-fx-min-width: 25; -fx-min-height: 25; "
				+ "-fx-max-width: 25; -fx-max-height: 25; " + "-fx-font-size: 11px;");
		btn.setOnAction(e -> {
			ingredientiMap.remove(ing);
			updateDisplay();
		});

		box.getChildren().addAll(lbl, spacer, btn);
		return box;
	}

	// ==================== HELPER ====================

	private VBox createListContainer() {
		VBox box = new VBox(10);
		box.setPadding(new Insets(10));
		box.setStyle("-fx-background-color: #f5f5f5; " + "-fx-background-radius: 8; " + "-fx-border-color: #ddd; "
				+ "-fx-border-width: 1; " + "-fx-border-radius: 8;");
		return box;
	}

	private Label createSectionTitle(String text) {
		Label lbl = new Label(text);
		lbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));
		lbl.setTextFill(Color.web("#FF6F00"));
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

		// ✅ Ridimensionabile con dimensioni minime
		dialog.setResizable(true);
		dialog.setMinWidth(650);
		dialog.setMinHeight(700);

		// ✅ Altezza iniziale maggiore
		Scene scene = new Scene(buildMainLayout(), 650, 750);
		dialog.setScene(scene);
		dialog.showAndWait();

		return ricettaCreata;
	}

}
