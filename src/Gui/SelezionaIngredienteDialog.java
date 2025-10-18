package Gui;

import controller.IngredienteController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Ingrediente;
import guihelper.StyleHelper;

public class SelezionaIngredienteDialog extends Stage {
    private final IngredienteController controller;
    private Ingrediente selezionato = null;
    private double xOffset = 0, yOffset = 0;

    public SelezionaIngredienteDialog(IngredienteController controller) {
        this.controller = controller;
        init();
    }

    public Ingrediente showAndReturn() {
        showAndWait();
        return selezionato;
    }

    // ==================== INIT ====================

    private void init() {
        setTitle("Seleziona Ingrediente");
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UNDECORATED);
        setResizable(true);

        StackPane root = new StackPane();
        root.setMinSize(700, 600);
        root.setPrefSize(800, 700);

        // ✅ BACKGROUND GRADIENT
        Region bg = new Region();
        StyleHelper.applyBackgroundGradient(bg);

        // ✅ MAIN CONTENT
        VBox main = buildMain();
        
        // ✅ WINDOW BUTTONS
        HBox winBtns = buildWindowButtons();

        root.getChildren().addAll(bg, main, winBtns);
        StackPane.setAlignment(winBtns, Pos.TOP_RIGHT);
        StackPane.setMargin(winBtns, new Insets(8));

        makeDraggable(root);

        Scene scene = new Scene(root, 800, 700);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);
    }

    private VBox buildMain() {
        VBox container = new VBox(25);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(40, 30, 30, 30));

        // ✅ TITOLO CON FONT ROBOTO
        Label title = new Label("🥕 Seleziona Ingrediente");
        title.setFont(Font.font("Roboto", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);

        // ✅ SOTTOTITOLO
        Label subtitle = new Label("Scegli un ingrediente dalla lista oppure creane uno nuovo");
        subtitle.setFont(Font.font("Roboto", 14));
        subtitle.setTextFill(Color.web("#FFFFFF", 0.9));
        subtitle.setAlignment(Pos.CENTER);

        VBox titleBox = new VBox(8, title, subtitle);
        titleBox.setAlignment(Pos.CENTER);

        // ✅ USA IL COMPONENTE VisualizzaIngredientiGUI
        VisualizzaIngredientiGUI gui = new VisualizzaIngredientiGUI(controller);
        gui.setModalitaSelezione(true);
        gui.setOnIngredienteSelezionato(ing -> {
            selezionato = ing;
            close();
        });

        VBox content = gui.getContent();
        VBox.setVgrow(content, Priority.ALWAYS);

        container.getChildren().addAll(titleBox, content);
        return container;
    }

    // ==================== WINDOW CONTROLS ====================

    private HBox buildWindowButtons() {
        Button close = new Button("✕");
        Button minimize = new Button("−");
        Button maximize = new Button("○");

        for (Button btn : new Button[]{minimize, maximize, close}) {
            btn.setPrefSize(30, 30);
            btn.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
            btn.setTextFill(Color.WHITE);
            btn.setStyle("-fx-background-color: rgba(255,140,0,0.8); -fx-background-radius: 15; -fx-cursor: hand;");
            btn.setFocusTraversable(false);
        }

        close.setOnAction(e -> { selezionato = null; close(); });
        minimize.setOnAction(e -> setIconified(true));
        maximize.setOnAction(e -> setMaximized(!isMaximized()));

        HBox box = new HBox(3, minimize, maximize, close);
        box.setAlignment(Pos.TOP_RIGHT);
        box.setPickOnBounds(false);
        return box;
    }

    private void makeDraggable(StackPane root) {
        root.setOnMousePressed(e -> {
            xOffset = e.getSceneX();
            yOffset = e.getSceneY();
        });
        root.setOnMouseDragged(e -> {
            setX(e.getScreenX() - xOffset);
            setY(e.getScreenY() - yOffset);
        });
    }
}
