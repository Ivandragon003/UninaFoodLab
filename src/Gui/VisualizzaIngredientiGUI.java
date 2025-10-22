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
		if (root == null)
			root = buildMain();
		return root;
	}

	private VBox buildMain() {
		VBox container = new VBox(20);
		container.setPadding(new Insets(20));

		VBox content = StyleHelper.createSection();
		content.setSpacing(20);
		content.getChildren().addAll(buildFiltri(), new Separator(), buildLista());

		ScrollPane scroll = scroller(content);
		VBox.setVgrow(scroll, Priority.ALWAYS);

		container.getChildren().add(scroll);
		VBox.setVgrow(scroll, Priority.ALWAYS);
		return container;
	}

	private VBox buildFiltri() {
		VBox section = new VBox(15);

		Label title = createTitle("🔍 Filtri Ingredienti");

		filtroNome = tf("Cerca per nome...", 250);
		filtroTipo = tf("Cerca per tipo...", 200);

		Button resetBtn = StyleHelper.createInfoButton("🔄 Reset");
		resetBtn.setOnAction(e -> reset());

		HBox filtri = rowLeft(15, StyleHelper.createLabel("Nome:"), filtroNome, StyleHelper.createLabel("Tipo:"),
				filtroTipo, resetBtn);

		section.getChildren().addAll(title, filtri);
		return section;
	}

	private VBox buildLista() {
		VBox section = new VBox(15);

		Label title = createTitle("📋 Lista Ingredienti");
		Label info = infoBadge("💡 Doppio click su un ingrediente per selezionarlo rapidamente", StyleHelper.INFO_BLUE);

		HBox header = new HBox(10);
		header.setAlignment(Pos.CENTER_LEFT);
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Button creaBtn = StyleHelper.createSuccessButton("➕ Crea Nuovo");
		creaBtn.setOnAction(e -> apriCreaIngrediente());

		header.getChildren().addAll(new VBox(8, title, info), spacer, rightBox(10, creaBtn));

		listView = createListView();
		VBox.setVgrow(listView, Priority.ALWAYS);

		section.getChildren().addAll(header, listView);
		VBox.setVgrow(listView, Priority.ALWAYS);
		return section;
	}

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
			boolean nomeVuoto = nome == null || nome.trim().isEmpty();
			boolean tipoVuoto = tipo == null || tipo.trim().isEmpty();

			if (nomeVuoto && tipoVuoto) {
				filtrati = controller.getAllIngredienti();
			} else if (!nomeVuoto && tipoVuoto) {
				filtrati = controller.cercaIngredientiPerNome(nome.trim());
			} else if (nomeVuoto) {
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
		Stage dialogStage = new Stage();
		dialogStage.initModality(Modality.APPLICATION_MODAL);
		dialogStage.initOwner(root.getScene().getWindow());
		dialogStage.initStyle(StageStyle.UNDECORATED);
		dialogStage.setResizable(true);

		CreaIngredientiGUI creaGUI = new CreaIngredientiGUI(controller);
		creaGUI.setOnIngredienteSelezionato(ingrediente -> {
			carica();
			dialogStage.close();
		});
		creaGUI.setOnAnnulla(dialogStage::close);

		StackPane dialogRoot = new StackPane();
		Region bg = new Region();
		StyleHelper.applyBackgroundGradient(bg);

		VBox content = creaGUI.getContent();
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

	private TextField tf(String prompt, int width) {
		TextField f = StyleHelper.createTextField(prompt);
		f.setPrefWidth(width);
		f.textProperty().addListener((obs, old, val) -> filtra());
		return f;
	}

	private Label infoBadge(String text, String bg) {
		Label l = new Label(text);
		l.setFont(Font.font("Roboto", FontWeight.BOLD, 13));
		l.setTextFill(Color.WHITE);
		l.setStyle("-fx-background-color: " + bg + "; -fx-padding: 10; -fx-background-radius: 8;");
		l.setWrapText(true);
		return l;
	}

	private ScrollPane scroller(Region content) {
		ScrollPane sp = new ScrollPane(content);
		sp.setFitToWidth(true);
		sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
		return sp;
	}

	private HBox rowLeft(double spacing, javafx.scene.Node... nodes) {
		HBox h = new HBox(spacing, nodes);
		h.setAlignment(Pos.CENTER_LEFT);
		return h;
	}

	private HBox rightBox(double spacing, javafx.scene.Node... nodes) {
		HBox h = new HBox(spacing, nodes);
		h.setAlignment(Pos.CENTER_RIGHT);
		return h;
	}

	private ListView<Ingrediente> createListView() {
		ListView<Ingrediente> lv = new ListView<>(data);
		lv.setMinHeight(300);
		lv.setPrefHeight(Region.USE_COMPUTED_SIZE);
		lv.setStyle("-fx-background-color: white; -fx-border-color: " + StyleHelper.BORDER_LIGHT
				+ "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1;");
		lv.setCellFactory(lv2 -> new IngredienteCell());
		return lv;
	}
}
