package Gui;

import controller.IngredienteController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Ingrediente;
import guihelper.StyleHelper;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * GUI per creazione ingredienti - VERSIONE SCHERMATA CONTINUA - Integrabile
 * come VBox in altre GUI - Adattabile e Scrollabile - Sfondo arancione uniforme
 * tramite StyleHelper
 */
public class CreaIngredientiGUI {

	private final IngredienteController controller;
	private Ingrediente creato;
	private TextField nomeField, tipoField;
	private Label errorLabel;
	private Consumer<Ingrediente> onIngredienteCreato;
	private Runnable onAnnulla;
	private VBox content;

	public CreaIngredientiGUI(IngredienteController controller) {
		this.controller = controller;
	}

	public void setOnIngredienteCreato(Consumer<Ingrediente> callback) {
		this.onIngredienteCreato = callback;
	}

	public void setOnAnnulla(Runnable callback) {
		this.onAnnulla = callback;
	}

	public VBox getContent() {
		if (content == null)
			content = buildLayout();
		return content;
	}

	private VBox buildLayout() {
		VBox container = new VBox();
		container.setPadding(new Insets(20));
		container.setSpacing(15);
		container.setAlignment(Pos.TOP_CENTER);

		// Sfondo arancione uniforme tramite StyleHelper
		StyleHelper.applyBackgroundGradient(container);

		// Titolo principale
		Label title = StyleHelper.createTitleLabel("🥕 Crea Nuovo Ingrediente");
		Label subtitle = new Label("Aggiungi un ingrediente alla tua dispensa");
		subtitle.setFont(Font.font("Roboto", 14));
		subtitle.setTextFill(Color.WHITE);
		subtitle.setAlignment(Pos.CENTER);

		VBox titleBox = new VBox(5, title, subtitle);
		titleBox.setAlignment(Pos.CENTER);

		// Card bianca con form
		VBox card = StyleHelper.createSection();
		card.setSpacing(20);

		errorLabel = new Label();
		errorLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 13));
		errorLabel.setTextFill(Color.RED);
		errorLabel.setWrapText(true);
		errorLabel.setVisible(false);
		errorLabel.setStyle("-fx-background-color: #ffe6e6; -fx-padding: 12; -fx-background-radius: 10;");

		VBox formSection = buildForm();
		HBox buttons = buildButtons();

		card.getChildren().addAll(errorLabel, formSection, new Separator(), buttons);

		// ScrollPane per adattabilità verticale
		ScrollPane scroll = new ScrollPane(card);
		scroll.setFitToWidth(true);
		scroll.setFitToHeight(true);
		scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
		VBox.setVgrow(scroll, Priority.ALWAYS);

		container.getChildren().addAll(titleBox, scroll);
		return container;
	}

	private VBox buildForm() {
		VBox section = new VBox(15);

		Label sectionTitle = new Label("📝 Informazioni Base");
		sectionTitle.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
		sectionTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

		Label nomeLabel = new Label("Nome Ingrediente:");
		nomeLabel.setFont(Font.font("Roboto", FontWeight.SEMI_BOLD, 14));
		nomeLabel.setTextFill(Color.BLACK);

		nomeField = StyleHelper.createTextField("Es. Pomodoro San Marzano");
		nomeField.setPrefWidth(450);
		nomeField.setPrefHeight(45);
		nomeField.textProperty().addListener((obs, old, val) -> {
			if (!val.trim().isEmpty())
				hideError();
		});

		Label tipoLabel = new Label("Tipo (es. Verdura, Carne, Spezie...):");
		tipoLabel.setFont(Font.font("Roboto", FontWeight.SEMI_BOLD, 14));
		tipoLabel.setTextFill(Color.BLACK);

		tipoField = StyleHelper.createTextField("Es. Verdura");
		tipoField.setPrefWidth(450);
		tipoField.setPrefHeight(45);
		tipoField.textProperty().addListener((obs, old, val) -> {
			if (!val.trim().isEmpty())
				hideError();
		});

		Label hint = new Label(
				"💡 Esempi: Verdura, Frutta, Carne, Pesce, Latticini, Cereali, Legumi, Spezie, Condimenti");
		hint.setFont(Font.font("Roboto", 12));
		hint.setTextFill(Color.web(StyleHelper.INFO_BLUE));
		hint.setWrapText(true);
		hint.setMaxWidth(450);

		section.getChildren().addAll(sectionTitle, nomeLabel, nomeField, tipoLabel, tipoField, hint);
		return section;
	}

	private HBox buildButtons() {
		HBox box = new HBox(15);
		box.setAlignment(Pos.CENTER);

		Button annullaBtn = StyleHelper.createSecondaryButton("❌ Annulla");
		annullaBtn.setPrefSize(150, 45);
		annullaBtn.setOnAction(e -> {
			if (onAnnulla != null)
				onAnnulla.run();
		});

		Button salvaBtn = StyleHelper.createSuccessButton("💾 Salva Ingrediente");
		salvaBtn.setPrefSize(200, 45);
		salvaBtn.setOnAction(e -> salva());

		box.getChildren().addAll(annullaBtn, salvaBtn);
		return box;
	}

	private void salva() {
		hideError();
		String nome = nomeField.getText().trim();
		String tipo = tipoField.getText().trim();

		if (nome.isEmpty()) {
			showError("❌ Il nome dell'ingrediente è obbligatorio");
			nomeField.requestFocus();
			return;
		}
		if (tipo.isEmpty()) {
			showError("❌ Il tipo dell'ingrediente è obbligatorio");
			tipoField.requestFocus();
			return;
		}

		try {
			int id = controller.creaIngrediente(nome, tipo);
			Optional<Ingrediente> opt = controller.trovaIngredientePerId(id);
			if (opt.isPresent()) {
				creato = opt.get();
				StyleHelper.showSuccessDialog("✅ Successo",
						String.format("Ingrediente creato!\n\n📝 Nome: %s\n📂 Tipo: %s", nome, tipo));
				if (onIngredienteCreato != null)
					onIngredienteCreato.accept(creato);
				clearForm();
			} else
				showError("❌ Ingrediente salvato ma non recuperato");
		} catch (Exception e) {
			showError("❌ " + e.getMessage());
		}
	}

	private void clearForm() {
		nomeField.clear();
		tipoField.clear();
		hideError();
	}

	private void showError(String msg) {
		errorLabel.setText(msg);
		errorLabel.setVisible(true);
	}

	private void hideError() {
		errorLabel.setVisible(false);
	}

	public Ingrediente getCreato() {
		return creato;
	}
}
