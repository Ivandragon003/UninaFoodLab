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
import util.StyleHelper;
import exceptions.ValidationException;

import java.util.List;
import java.util.Map;

public class VisualizzaRicetteGUI {
	private final RicettaController ricettaController;
	private final IngredienteController ingredienteController;
	private final ObservableList<Ricetta> ricetteData;
	private ListView<Ricetta> ricetteListView;
	private TextField filtroNomeField, filtroTempoMinField, filtroTempoMaxField;
	private TextField filtroIngredientiMinField, filtroIngredientiMaxField;

	private boolean modalitaSelezione = false;
	private Ricetta ricettaSelezionata = null;
	private VBox root;

	public VisualizzaRicetteGUI(RicettaController ricettaController, IngredienteController ingredienteController) {
		this.ricettaController = ricettaController;
		this.ingredienteController = ingredienteController;
		this.ricetteData = FXCollections.observableArrayList();
		caricaRicette();
	}

	public void setSelectionMode(boolean modalitaSelezione) {
		this.modalitaSelezione = modalitaSelezione;
	}

	public VBox getRoot() {
		if (root == null) {
			root = createMainLayout();
			setupListeners();
		}
		return root;
	}

	// ==================== LAYOUT ====================

	private VBox createMainLayout() {
		VBox container = new VBox(15);
		container.setPadding(new Insets(20));
		StyleHelper.applyOrangeBackground(container);

		Label titleLabel = StyleHelper.createTitleLabel("📖 Gestione Ricette");
		titleLabel.setAlignment(Pos.CENTER);
		titleLabel.setTextFill(Color.WHITE);

		ScrollPane scrollPane = new ScrollPane(new VBox(15, createFiltriSection(), new Separator(),
				createListaSection(), new Separator(), createButtonSection()));
		scrollPane.setFitToWidth(true);
		scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

		container.getChildren().addAll(titleLabel, scrollPane);
		return container;
	}

	private VBox createFiltriSection() {
		VBox section = StyleHelper.createSection();

		Label sectionTitle = new Label("🔍 Filtri Avanzati");
		sectionTitle.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
		sectionTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

		filtroNomeField = StyleHelper.createTextField("Cerca per nome...");
		filtroNomeField.setPrefWidth(300);

		filtroTempoMinField = StyleHelper.createTextField("Min");
		filtroTempoMaxField = StyleHelper.createTextField("Max");
		filtroIngredientiMinField = StyleHelper.createTextField("Min");
		filtroIngredientiMaxField = StyleHelper.createTextField("Max");

		setFieldWidth(80, filtroTempoMinField, filtroTempoMaxField, filtroIngredientiMinField,
				filtroIngredientiMaxField);

		GridPane grid = new GridPane();
		grid.setHgap(15);
		grid.setVgap(10);

		grid.add(StyleHelper.createLabel("Nome:"), 0, 0);
		grid.add(filtroNomeField, 1, 0, 3, 1);
		grid.add(StyleHelper.createLabel("Tempo:"), 0, 1);
		grid.add(createRangeBox(filtroTempoMinField, filtroTempoMaxField, "min"), 1, 1);
		grid.add(StyleHelper.createLabel("Ingredienti:"), 2, 1);
		grid.add(createRangeBox(filtroIngredientiMinField, filtroIngredientiMaxField, ""), 3, 1);

		Button resetBtn = StyleHelper.createInfoButton("🔄 Reset");
		resetBtn.setOnAction(e -> resetFiltri());
		grid.add(resetBtn, 4, 1);

		section.getChildren().addAll(sectionTitle, grid);
		return section;
	}

	private VBox createListaSection() {
		VBox section = StyleHelper.createSection();

		Label sectionTitle = new Label("📋 Lista Ricette");
		sectionTitle.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
		sectionTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

		Label infoLabel = new Label("💡 Doppio click per vedere ingredienti");
		infoLabel.setFont(Font.font("Roboto", 12));
		infoLabel.setTextFill(Color.web(StyleHelper.INFO_BLUE));

		VBox titleBox = new VBox(5, sectionTitle, infoLabel);

		HBox headerBox = new HBox(10);
		headerBox.setAlignment(Pos.CENTER_LEFT);
		headerBox.getChildren().add(titleBox);

		if (!modalitaSelezione) {
			Region spacer = new Region();
			HBox.setHgrow(spacer, Priority.ALWAYS);
			Button creaBtn = StyleHelper.createSuccessButton("➕ Crea Nuova");
			creaBtn.setOnAction(e -> apriCreaRicetta());
			headerBox.getChildren().addAll(spacer, creaBtn);
		}

		ricetteListView = new ListView<>();
		ricetteListView.setPrefHeight(350);
		ricetteListView.setItems(ricetteData);
		StyleHelper.applyListViewStyle(ricetteListView);
		ricetteListView.setCellFactory(lv -> new RicettaCell());
		ricetteListView.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2) {
				Ricetta sel = ricetteListView.getSelectionModel().getSelectedItem();
				if (sel != null) {
					if (modalitaSelezione)
						selezionaRicetta();
					else
						mostraIngredienti(sel);
				}
			}
		});

		section.getChildren().addAll(headerBox, ricetteListView);
		return section;
	}

	private HBox createButtonSection() {
		HBox box = new HBox(15);
		box.setAlignment(Pos.CENTER);
		box.setPadding(new Insets(20, 0, 0, 0));

		if (modalitaSelezione) {
			Button annullaBtn = StyleHelper.createDangerButton("❌ Annulla");
			Button selezionaBtn = StyleHelper.createPrimaryButton("✅ Seleziona");
			selezionaBtn.setOnAction(e -> selezionaRicetta());
			box.getChildren().addAll(annullaBtn, selezionaBtn);
		} else {
			Button aggiornaBtn = StyleHelper.createInfoButton("🔄 Aggiorna");
			aggiornaBtn.setOnAction(e -> ricaricaRicette());
			box.getChildren().add(aggiornaBtn);
		}

		return box;
	}

	// ==================== LISTENERS ====================

	private void setupListeners() {
		filtroNomeField.textProperty().addListener((obs, old, val) -> applicaFiltri());
		filtroTempoMinField.textProperty().addListener((obs, old, val) -> applicaFiltri());
		filtroTempoMaxField.textProperty().addListener((obs, old, val) -> applicaFiltri());
		filtroIngredientiMinField.textProperty().addListener((obs, old, val) -> applicaFiltri());
		filtroIngredientiMaxField.textProperty().addListener((obs, old, val) -> applicaFiltri());
	}

	private void applicaFiltri() {
		try {
			List<Ricetta> filtrate = ricettaController.filtraCombinato(filtroNomeField.getText(),
					parseIntSafe(filtroTempoMinField.getText()), parseIntSafe(filtroTempoMaxField.getText()),
					parseIntSafe(filtroIngredientiMinField.getText()),
					parseIntSafe(filtroIngredientiMaxField.getText()));
			ricetteData.setAll(filtrate);
		} catch (ValidationException e) {
			StyleHelper.showValidationDialog("Validazione", e.getMessage());
		} catch (Exception e) {
			StyleHelper.showErrorDialog("Errore", "Errore filtri: " + e.getMessage());
		}
	}

	// ==================== AZIONI ====================

	private void caricaRicette() {
		try {
			ricetteData.setAll(ricettaController.getAllRicette());
		} catch (Exception e) {
			StyleHelper.showErrorDialog("Errore", "Errore caricamento: " + e.getMessage());
		}
	}

	private void resetFiltri() {
		filtroNomeField.clear();
		filtroTempoMinField.clear();
		filtroTempoMaxField.clear();
		filtroIngredientiMinField.clear();
		filtroIngredientiMaxField.clear();
		caricaRicette();
	}

	private void ricaricaRicette() {
		try {
			ricettaController.ricaricaCache();
			caricaRicette();
			StyleHelper.showSuccessDialog("Successo", "Ricette ricaricate");
		} catch (Exception e) {
			StyleHelper.showErrorDialog("Errore", "Errore ricaricamento: " + e.getMessage());
		}
	}

	private void apriCreaRicetta() {
		CreaRicettaGUI creaGUI = new CreaRicettaGUI(ricettaController, ingredienteController);
		Ricetta nuova = creaGUI.showAndReturn();
		if (nuova != null) {
			caricaRicette();
			StyleHelper.showSuccessDialog("Successo", "Ricetta '" + nuova.getNome() + "' creata!");
		}
	}

	private void mostraIngredienti(Ricetta ricetta) {
		StringBuilder sb = new StringBuilder();
		sb.append("⏱️ Tempo: ").append(ricetta.getTempoPreparazione()).append(" minuti\n");
		sb.append("🥕 Totale: ").append(ricetta.getNumeroIngredienti()).append(" ingredienti\n\n");

		Map<Ingrediente, Double> ingredienti = ricetta.getIngredienti();
		if (ingredienti.isEmpty()) {
			sb.append("Nessun ingrediente trovato");
		} else {
			ingredienti.forEach((ing, qnt) -> sb.append("🥕 ").append(ing.getNome()).append(" (").append(ing.getTipo())
					.append(") - ").append(qnt).append("g\n"));
		}

		Alert dialog = new Alert(Alert.AlertType.INFORMATION);
		dialog.setTitle("Ingredienti - " + ricetta.getNome());
		dialog.setHeaderText("🥕 " + ricetta.getNome());
		dialog.setContentText(sb.toString());
		dialog.showAndWait();
	}

	private void selezionaRicetta() {
		Ricetta sel = ricetteListView.getSelectionModel().getSelectedItem();
		if (sel == null) {
			StyleHelper.showValidationDialog("Validazione", "Seleziona una ricetta dalla lista");
		} else {
			ricettaSelezionata = sel;
		}
	}

	// ==================== HELPER ====================

	private HBox createRangeBox(TextField min, TextField max, String unit) {
		HBox box = new HBox(5, min, new Label("-"), max);
		if (!unit.isEmpty())
			box.getChildren().add(new Label(unit));
		box.setAlignment(Pos.CENTER_LEFT);
		return box;
	}

	private void setFieldWidth(int width, TextField... fields) {
		for (TextField field : fields)
			field.setPrefWidth(width);
	}

	private Integer parseIntSafe(String text) {
		if (text == null || text.trim().isEmpty())
			return null;
		try {
			return Integer.parseInt(text.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public Ricetta showAndReturn() {
		return ricettaSelezionata;
	}

	// ==================== CELL RENDERER ====================

	private static class RicettaCell extends ListCell<Ricetta> {
		@Override
		protected void updateItem(Ricetta item, boolean empty) {
			super.updateItem(item, empty);
			if (empty || item == null) {
				setGraphic(null);
			} else {
				Label nomeLabel = new Label("📖 " + item.getNome());
				nomeLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
				nomeLabel.setTextFill(Color.BLACK);

				Label tempoLabel = new Label("⏱️ " + item.getTempoPreparazione() + " min");
				tempoLabel.setFont(Font.font("Roboto", 12));
				tempoLabel.setTextFill(Color.web(StyleHelper.INFO_BLUE));

				Label ingLabel = new Label("🥕 " + item.getNumeroIngredienti() + " ingredienti");
				ingLabel.setFont(Font.font("Roboto", 12));
				ingLabel.setTextFill(Color.web(StyleHelper.SUCCESS_GREEN));

				HBox details = new HBox(20, tempoLabel, ingLabel);
				details.setAlignment(Pos.CENTER_LEFT);

				VBox info = new VBox(5, nomeLabel, details);
				HBox cell = new HBox(info);
				cell.setPadding(new Insets(12));
				cell.setAlignment(Pos.CENTER_LEFT);

				setGraphic(cell);
			}
		}
	}
}
