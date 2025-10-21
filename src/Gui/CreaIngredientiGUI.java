package Gui;

import controller.IngredienteController;
import guihelper.StyleHelper;
import guihelper.ValidationHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Ingrediente;

import java.util.Optional;
import java.util.function.Consumer;

public class CreaIngredientiGUI {

	private final IngredienteController controller;
	private Ingrediente ingredienteCreato;
	private TextField nomeField, tipoField;
	private Consumer<Ingrediente> onIngredienteSelezionato;
	private Runnable onAnnulla;
	private VBox content;
	private Label errorLabel;

	public CreaIngredientiGUI(IngredienteController controller) {
		this.controller = controller;
	}

	public void setOnIngredienteSelezionato(Consumer<Ingrediente> callback) {
		this.onIngredienteSelezionato = callback;
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
		VBox container = new VBox(20);
		container.setPadding(new Insets(20));

		Label mainTitle = StyleHelper.createTitleLabel("🔧 Crea Ingrediente");
		mainTitle.setAlignment(Pos.CENTER);
		mainTitle.setTextFill(Color.WHITE);

		VBox card = StyleHelper.createSection();
		card.setSpacing(20);
		card.setPadding(new Insets(24));

		nomeField = createFieldWithLabel(card, "Nome Ingrediente:", "Es. Pomodoro San Marzano");
		tipoField = createFieldWithLabel(card, "Tipo (es. Verdura, Carne, Spezie...):", "Es. Verdura");

		Label hint = new Label(
				"💡 Esempi: Verdura, Frutta, Carne, Pesce, Latticini, Cereali, Legumi, Spezie, Condimenti");
		hint.setFont(Font.font("Roboto", 12));
		hint.setTextFill(Color.web(StyleHelper.INFO_BLUE));
		hint.setWrapText(true);

		errorLabel = new Label();
		errorLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
		errorLabel.setTextFill(Color.RED);
		errorLabel.setVisible(false);
		errorLabel.setWrapText(true);

		ValidationHelper.addErrorLabelResetListener(nomeField, errorLabel);
		ValidationHelper.addErrorLabelResetListener(tipoField, errorLabel);

		Button salvaBtn = StyleHelper.createSuccessButton("💾 Salva Ingrediente");
		salvaBtn.setPrefSize(200, 45);
		salvaBtn.setOnAction(e -> salva());

		card.getChildren().addAll(hint, errorLabel, salvaBtn);

		ScrollPane scroll = new ScrollPane(card);
		scroll.setFitToWidth(true);
		scroll.setFitToHeight(true);
		scroll.setPannable(true);
		scroll.setStyle("-fx-background-color: transparent;");
		VBox.setVgrow(scroll, Priority.ALWAYS);

		Separator bottomSep = new Separator();
		HBox buttons = buildButtons();

		container.getChildren().addAll(mainTitle, scroll, bottomSep, buttons);
		return container;
	}

	private TextField createFieldWithLabel(VBox parent, String labelText, String placeholder) {
		Label label = new Label(labelText);
		label.setFont(Font.font("Roboto", FontWeight.SEMI_BOLD, 14));
		label.setTextFill(Color.BLACK);

		TextField field = StyleHelper.createTextField(placeholder);
		field.setPrefWidth(500);
		field.setPrefHeight(45);

		parent.getChildren().addAll(label, field);
		return field;
	}

	private HBox buildButtons() {
		Button annullaBtn = StyleHelper.createSecondaryButton("← Indietro");
		annullaBtn.setPrefSize(150, 45);
		annullaBtn.setOnAction(e -> {
			if (onAnnulla != null)
				onAnnulla.run();
		});

		HBox box = new HBox(15, annullaBtn);
		box.setAlignment(Pos.CENTER);
		box.setPadding(new Insets(15, 0, 5, 0));
		return box;
	}

	private void salva() {
		errorLabel.setVisible(false);

		try {
			String nome = ValidationHelper.validateString(nomeField.getText(), "nome");
			String tipo = ValidationHelper.validateString(tipoField.getText(), "tipo");

			int id = controller.creaIngrediente(nome, tipo);
			Optional<Ingrediente> opt = controller.trovaIngredientePerId(id);

			if (opt.isPresent()) {
				ingredienteCreato = opt.get();
				StyleHelper.showSuccessDialog("✅ Successo",
						String.format("Ingrediente '%s' creato con successo!", nome));

				if (onIngredienteSelezionato != null) {
					onIngredienteSelezionato.accept(ingredienteCreato);
				}
			} else {
				throw new IllegalStateException("Ingrediente salvato ma non recuperato");
			}
		} catch (IllegalArgumentException e) {
			errorLabel.setText("❌ " + e.getMessage());
			errorLabel.setVisible(true);
			if (nomeField.getText().trim().isEmpty()) {
				nomeField.requestFocus();
			} else {
				tipoField.requestFocus();
			}
		} catch (Exception e) {
			errorLabel.setText("❌ " + e.getMessage());
			errorLabel.setVisible(true);
		}
	}

	public Ingrediente getIngredienteCreato() {
		return ingredienteCreato;
	}
}