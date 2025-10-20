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

public class CreaIngredientiGUI {

	private final IngredienteController controller;
	private Ingrediente creato;
	private TextField nomeField, tipoField;
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
		VBox container = new VBox(20);
		container.setPadding(new Insets(20));

		VBox card = StyleHelper.createSection();
		card.setSpacing(20);

		Label sectionTitle = new Label("📝 Informazioni Base");
		sectionTitle.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
		sectionTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

		Label nomeLabel = new Label("Nome Ingrediente:");
		nomeLabel.setFont(Font.font("Roboto", FontWeight.SEMI_BOLD, 14));
		nomeLabel.setTextFill(Color.BLACK);

		nomeField = StyleHelper.createTextField("Es. Pomodoro San Marzano");
		nomeField.setPrefWidth(450);
		nomeField.setPrefHeight(45);

		Label tipoLabel = new Label("Tipo (es. Verdura, Carne, Spezie...):");
		tipoLabel.setFont(Font.font("Roboto", FontWeight.SEMI_BOLD, 14));
		tipoLabel.setTextFill(Color.BLACK);

		tipoField = StyleHelper.createTextField("Es. Verdura");
		tipoField.setPrefWidth(450);
		tipoField.setPrefHeight(45);

		Label hint = new Label(
				"💡 Esempi: Verdura, Frutta, Carne, Pesce, Latticini, Cereali, Legumi, Spezie, Condimenti");
		hint.setFont(Font.font("Roboto", 12));
		hint.setTextFill(Color.web(StyleHelper.INFO_BLUE));
		hint.setWrapText(true);
		hint.setMaxWidth(450);

		GridPane grid = new GridPane();
		grid.setVgap(15);
		grid.setHgap(15);
		grid.add(nomeLabel, 0, 0);
		grid.add(nomeField, 0, 1);
		grid.add(tipoLabel, 0, 2);
		grid.add(tipoField, 0, 3);
		grid.add(hint, 0, 4);

		HBox buttons = buildButtons();

		card.getChildren().addAll(sectionTitle, grid, new Separator(), buttons);

		ScrollPane scroll = new ScrollPane(card);
		scroll.setFitToWidth(true);
		scroll.setFitToHeight(true);
		scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
		VBox.setVgrow(scroll, Priority.ALWAYS);

		container.getChildren().add(scroll);
		VBox.setVgrow(scroll, Priority.ALWAYS);

		return container;
	}

	private HBox buildButtons() {
		HBox box = new HBox(15);
		box.setAlignment(Pos.CENTER);

		Button annullaBtn = StyleHelper.createSecondaryButton("← Indietro");
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
		String nome = nomeField.getText().trim();
		String tipo = tipoField.getText().trim();

		if (nome.isEmpty()) {
			StyleHelper.showValidationDialog("Errore", "Il nome dell'ingrediente è obbligatorio");
			nomeField.requestFocus();
			return;
		}
		if (tipo.isEmpty()) {
			StyleHelper.showValidationDialog("Errore", "Il tipo dell'ingrediente è obbligatorio");
			tipoField.requestFocus();
			return;
		}

		try {
			int id = controller.creaIngrediente(nome, tipo);
			Optional<Ingrediente> opt = controller.trovaIngredientePerId(id);
			if (opt.isPresent()) {
				creato = opt.get();
				StyleHelper.showSuccessDialog("✅ Successo", String.format("Ingrediente '%s' creato!", nome));
				if (onIngredienteCreato != null)
					onIngredienteCreato.accept(creato);
				clearForm();
			} else {
				StyleHelper.showErrorDialog("Errore", "Ingrediente salvato ma non recuperato");
			}
		} catch (Exception e) {
			StyleHelper.showErrorDialog("Errore", e.getMessage());
		}
	}

	private void clearForm() {
		nomeField.clear();
		tipoField.clear();
	}

	public Ingrediente getCreato() {
		return creato;
	}
}