package Gui;

import controller.IngredienteController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Ingrediente;
import guihelper.StyleHelper;

import java.util.List;
import java.util.function.Consumer;

public class VisualizzaIngredientiGUI {
	private final IngredienteController controller;
	private final ObservableList<Ingrediente> data;
	private ListView<Ingrediente> listView;
	private TextField filtroNome, filtroTipo;
	private boolean modalitaSelezione = false;
	private Consumer<Ingrediente> onIngredienteSelezionato;
	private VBox root;

	public VisualizzaIngredientiGUI(IngredienteController controller) {
		this.controller = controller;
		this.data = FXCollections.observableArrayList();
		carica();
	}

	public void setModalitaSelezione(boolean mode) {
		this.modalitaSelezione = mode;
	}

	public void setOnIngredienteSelezionato(Consumer<Ingrediente> callback) {
		this.onIngredienteSelezionato = callback;
	}

	public VBox getContent() {
		if (root == null) {
			root = buildMain();
		}
		return root;
	}

	// ==================== BUILD ====================

	private VBox buildMain() {
		VBox container = new VBox(20);
		container.setPadding(new Insets(20));

		VBox content = StyleHelper.createSection();
		content.setSpacing(20);

		content.getChildren().addAll(buildFiltri(), new Separator(), buildLista());

		ScrollPane scroll = new ScrollPane(content);
		scroll.setFitToWidth(true);
		scroll.setFitToHeight(true);
		scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
		VBox.setVgrow(scroll, Priority.ALWAYS);

		container.getChildren().add(scroll);
		VBox.setVgrow(scroll, Priority.ALWAYS);
		return container;
	}

	private VBox buildFiltri() {
		VBox section = new VBox(15);

		Label title = createTitle("🔍 Filtri Ingredienti");

		filtroNome = StyleHelper.createTextField("Cerca per nome...");
		filtroNome.setPrefWidth(250);
		filtroNome.textProperty().addListener((obs, old, val) -> filtra());

		filtroTipo = StyleHelper.createTextField("Cerca per tipo...");
		filtroTipo.setPrefWidth(200);
		filtroTipo.textProperty().addListener((obs, old, val) -> filtra());

		Button resetBtn = StyleHelper.createInfoButton("🔄 Reset");
		resetBtn.setOnAction(e -> reset());

		HBox filtri = new HBox(15, StyleHelper.createLabel("Nome:"), filtroNome, StyleHelper.createLabel("Tipo:"),
				filtroTipo, resetBtn);
		filtri.setAlignment(Pos.CENTER_LEFT);

		section.getChildren().addAll(title, filtri);
		return section;
	}

	private VBox buildLista() {
		VBox section = new VBox(15);

		Label title = createTitle("📋 Lista Ingredienti");

		Label info = new Label("💡 Doppio click su un ingrediente per selezionarlo rapidamente");
		info.setFont(Font.font("Roboto", FontWeight.BOLD, 13));
		info.setTextFill(Color.WHITE);
		info.setStyle(
				"-fx-background-color: " + StyleHelper.INFO_BLUE + "; -fx-padding: 10; -fx-background-radius: 8;");
		info.setWrapText(true);

		HBox header = new HBox(10);
		header.setAlignment(Pos.CENTER_LEFT);
		VBox titleBox = new VBox(8, title, info);

		HBox actions = new HBox(10);
		actions.setAlignment(Pos.CENTER_RIGHT);

		Button creaBtn = StyleHelper.createSuccessButton("➕ Crea Nuovo");
		creaBtn.setOnAction(e -> apriCreaIngrediente());
		actions.getChildren().add(creaBtn);

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		header.getChildren().addAll(titleBox, spacer, actions);

		listView = new ListView<>(data);
		listView.setMinHeight(300);
		listView.setPrefHeight(Region.USE_COMPUTED_SIZE);
		listView.setStyle("-fx-background-color: white;" + "-fx-border-color: " + StyleHelper.BORDER_LIGHT + ";"
				+ "-fx-border-radius: 8;" + "-fx-background-radius: 8;" + "-fx-border-width: 1;");
		listView.setCellFactory(lv -> new IngredienteCell());
		VBox.setVgrow(listView, Priority.ALWAYS);

		section.getChildren().addAll(header, listView);
		VBox.setVgrow(listView, Priority.ALWAYS);
		return section;
	}

	// ==================== LOGICA ====================

	private void carica() {
		try {
			data.setAll(controller.getAllIngredienti());
		} catch (Exception e) {
			StyleHelper.showErrorDialog("Errore", "Errore caricamento: " + e.getMessage());
		}
	}

	private void filtra() {
		try {
			String nome = filtroNome.getText();
			String tipo = filtroTipo.getText();

			List<Ingrediente> filtrati;

			if ((nome == null || nome.trim().isEmpty()) && (tipo == null || tipo.trim().isEmpty())) {
				filtrati = controller.getAllIngredienti();
			} else if (!nome.trim().isEmpty() && tipo.trim().isEmpty()) {
				filtrati = controller.cercaIngredientiPerNome(nome.trim());
			} else if (nome.trim().isEmpty() && !tipo.trim().isEmpty()) {
				filtrati = controller.cercaIngredientiPerTipo(tipo.trim());
			} else {
				List<Ingrediente> perNome = controller.cercaIngredientiPerNome(nome.trim());
				String tipoLower = tipo.toLowerCase().trim();
				filtrati = perNome.stream().filter(i -> i.getTipo().toLowerCase().contains(tipoLower)).toList();
			}

			data.setAll(filtrati);
		} catch (Exception e) {
			StyleHelper.showErrorDialog("Errore", "Errore filtro: " + e.getMessage());
		}
	}

	private void reset() {
		filtroNome.clear();
		filtroTipo.clear();
		carica();
	}

	private void apriCreaIngrediente() {
		// ✅ Crea uno Stage UNDECORATED (senza barra grigia)
		Stage dialogStage = new Stage();
		dialogStage.initModality(Modality.APPLICATION_MODAL);
		dialogStage.initOwner(root.getScene().getWindow());
		dialogStage.initStyle(StageStyle.UNDECORATED); // ✅ Rimuove la barra grigia
		dialogStage.setResizable(true);

		CreaIngredientiGUI creaGUI = new CreaIngredientiGUI(controller);

		creaGUI.setOnIngredienteSelezionato(ingrediente -> {
			carica(); // Ricarica la lista
			dialogStage.close(); // Chiudi il dialog
		});

		creaGUI.setOnAnnulla(() -> {
			dialogStage.close(); // Chiudi il dialog
		});

		// ✅ Crea il layout completo con bottoni finestra
		StackPane dialogRoot = new StackPane();

		// Sfondo arancione
		Region bg = new Region();
		StyleHelper.applyBackgroundGradient(bg);

		// Contenuto
		VBox content = creaGUI.getContent();

		// ✅ Bottoni finestra (X, O, -)
		HBox windowButtons = new HBox(5,
				StyleHelper.createWindowButtonByType("minimize", () -> dialogStage.setIconified(true)),
				StyleHelper.createWindowButtonByType("maximize",
						() -> dialogStage.setMaximized(!dialogStage.isMaximized())),
				StyleHelper.createWindowButtonByType("close", dialogStage::close));
		windowButtons.setAlignment(Pos.TOP_RIGHT);
		windowButtons.setPickOnBounds(false);

		dialogRoot.getChildren().addAll(bg, content, windowButtons);
		StackPane.setAlignment(windowButtons, Pos.TOP_RIGHT);
		StackPane.setMargin(windowButtons, new Insets(10));

		// ✅ Rendi draggable la finestra
		final double[] xOffset = { 0 };
		final double[] yOffset = { 0 };

		dialogRoot.setOnMousePressed(event -> {
			xOffset[0] = event.getSceneX();
			yOffset[0] = event.getSceneY();
		});

		dialogRoot.setOnMouseDragged(event -> {
			dialogStage.setX(event.getScreenX() - xOffset[0]);
			dialogStage.setY(event.getScreenY() - yOffset[0]);
		});

		Scene scene = new Scene(dialogRoot, 900, 700);
		scene.setFill(Color.TRANSPARENT);
		dialogStage.setScene(scene);
		dialogStage.showAndWait();
	}

	private Label createTitle(String text) {
		Label lbl = new Label(text);
		lbl.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
		lbl.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));
		return lbl;
	}

	// ==================== CELL RENDERER ====================

	private class IngredienteCell extends ListCell<Ingrediente> {
		@Override
		protected void updateItem(Ingrediente item, boolean empty) {
			super.updateItem(item, empty);
			if (empty || item == null) {
				setGraphic(null);
				setText(null);
				setOnMouseClicked(null);
			} else {
				setGraphic(buildCell(item));
				setText(null);

				if (modalitaSelezione) {
					setOnMouseClicked(e -> {
						if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
							if (onIngredienteSelezionato != null)
								onIngredienteSelezionato.accept(item);
						}
					});
				}
			}
		}

		private HBox buildCell(Ingrediente item) {
			HBox box = new HBox(15);
			box.setAlignment(Pos.CENTER_LEFT);
			box.setPadding(new Insets(12));

			Label nome = new Label("🥕 " + item.getNome());
			nome.setFont(Font.font("Roboto", FontWeight.BOLD, 15));
			nome.setTextFill(Color.BLACK);

			Label tipo = new Label("📂 Tipo: " + item.getTipo());
			tipo.setFont(Font.font("Roboto", 12));
			tipo.setTextFill(Color.GRAY);

			VBox info = new VBox(5, nome, tipo);
			Region spacer = new Region();
			HBox.setHgrow(spacer, Priority.ALWAYS);

			box.getChildren().addAll(info, spacer);
			return box;
		}
	}
}