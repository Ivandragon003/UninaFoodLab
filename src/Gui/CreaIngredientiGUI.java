package Gui;

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
import javafx.stage.StageStyle;
import model.Ingrediente;
import guihelper.StyleHelper;
import java.util.Optional;

public class CreaIngredientiGUI extends Stage {
    private final IngredienteController controller;
    private Ingrediente creato = null;
    private TextField nomeField, tipoField;
    private Label errorLabel;
    private double xOffset = 0, yOffset = 0;

    public CreaIngredientiGUI(IngredienteController controller) {
        this.controller = controller;
        init();
    }

    public Ingrediente showAndReturn() {
        showAndWait();
        return creato;
    }

    // ==================== INIT ====================

    private void init() {
        setTitle("Crea Ingrediente");
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UNDECORATED);
        setResizable(false);

        StackPane root = new StackPane();
        root.setMinSize(550, 450);
        root.setPrefSize(600, 500);

        Region bg = new Region();
        StyleHelper.applyBackgroundGradient(bg);

        VBox main = buildMain();
        HBox winBtns = buildWindowButtons();

        root.getChildren().addAll(bg, main, winBtns);
        StackPane.setAlignment(winBtns, Pos.TOP_RIGHT);
        StackPane.setMargin(winBtns, new Insets(10));

        makeDraggable(root);

        Scene scene = new Scene(root, 600, 500);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);
    }

    private VBox buildMain() {
        VBox container = new VBox(25);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(40, 40, 30, 40));

        // ✅ TITOLO BIANCO
        Label title = new Label("🥕 Crea Nuovo Ingrediente");
        title.setFont(Font.font("Roboto", FontWeight.BOLD, 26));
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);

        Label subtitle = new Label("Aggiungi un ingrediente alla tua dispensa");
        subtitle.setFont(Font.font("Roboto", 14));
        subtitle.setTextFill(Color.web("#FFFFFF", 0.9));
        subtitle.setAlignment(Pos.CENTER);

        VBox titleBox = new VBox(8, title, subtitle);
        titleBox.setAlignment(Pos.CENTER);

        // ✅ CARD BIANCA - CONTENUTO VISIBILE
        VBox card = new VBox(25);
        card.setPadding(new Insets(30));
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + StyleHelper.BORDER_LIGHT + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 5);"
        );

        errorLabel = new Label();
        errorLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 13));
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);
        errorLabel.setAlignment(Pos.CENTER);
        errorLabel.setStyle("-fx-background-color: #ffe6e6; -fx-padding: 10; -fx-background-radius: 8;");

        card.getChildren().addAll(errorLabel, buildForm(), new Separator(), buildButtons());
        container.getChildren().addAll(titleBox, card);
        return container;
    }

    // ==================== FORM ====================

    private VBox buildForm() {
        VBox section = new VBox(20);

        // ✅ TITOLO ARANCIONE (visibile su bianco)
        Label title = new Label("📝 Informazioni Base");
        title.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
        title.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        // ✅ LABEL NERA (visibile su bianco)
        Label nomeLabel = new Label("Nome Ingrediente:");
        nomeLabel.setFont(Font.font("Roboto", FontWeight.SEMI_BOLD, 14));
        nomeLabel.setTextFill(Color.BLACK);

        nomeField = StyleHelper.createTextField("Es. Pomodoro San Marzano");
        nomeField.setPrefWidth(400);
        nomeField.setPrefHeight(40);
        nomeField.textProperty().addListener((obs, old, val) -> {
            if (!val.trim().isEmpty()) {
                nomeField.setStyle("");
                hideError();
            }
        });

        // ✅ LABEL NERA (visibile su bianco)
        Label tipoLabel = new Label("Tipo (es. Verdura, Carne, Spezie...):");
        tipoLabel.setFont(Font.font("Roboto", FontWeight.SEMI_BOLD, 14));
        tipoLabel.setTextFill(Color.BLACK);

        tipoField = StyleHelper.createTextField("Es. Verdura");
        tipoField.setPrefWidth(400);
        tipoField.setPrefHeight(40);
        tipoField.textProperty().addListener((obs, old, val) -> {
            if (!val.trim().isEmpty()) {
                tipoField.setStyle("");
                hideError();
            }
        });

        Label hint = new Label("💡 Esempi: Verdura, Frutta, Carne, Pesce, Latticini, Cereali, Legumi, Spezie, Condimenti");
        hint.setFont(Font.font("Roboto", 11));
        hint.setTextFill(Color.web(StyleHelper.INFO_BLUE));
        hint.setWrapText(true);
        hint.setMaxWidth(400);

        VBox formFields = new VBox(15,
            nomeLabel, nomeField,
            tipoLabel, tipoField,
            hint
        );

        section.getChildren().addAll(title, formFields);
        return section;
    }

    private HBox buildButtons() {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15, 0, 5, 0));

        Button annulla = new Button("❌ Annulla");
        annulla.setPrefSize(140, 45);
        annulla.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
        annulla.setStyle("-fx-background-color: " + StyleHelper.NEUTRAL_GRAY + "; -fx-text-fill: white; -fx-background-radius: 22; -fx-cursor: hand;");
        annulla.setOnMouseEntered(e -> annulla.setStyle("-fx-background-color: #5A5A5A; -fx-text-fill: white; -fx-background-radius: 22; -fx-cursor: hand;"));
        annulla.setOnMouseExited(e -> annulla.setStyle("-fx-background-color: " + StyleHelper.NEUTRAL_GRAY + "; -fx-text-fill: white; -fx-background-radius: 22; -fx-cursor: hand;"));
        annulla.setOnAction(e -> { creato = null; close(); });

        Button salva = StyleHelper.createPrimaryButton("💾 Salva Ingrediente");
        salva.setPrefSize(180, 45);
        salva.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
        salva.setOnAction(e -> salva());

        box.getChildren().addAll(annulla, salva);
        return box;
    }

    // ==================== WINDOW CONTROLS ====================

    private HBox buildWindowButtons() {
        Button close = new Button("✕");
        Button minimize = new Button("−");

        for (Button btn : new Button[]{minimize, close}) {
            btn.setPrefSize(32, 32);
            btn.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
            btn.setTextFill(Color.WHITE);
            btn.setStyle("-fx-background-color: rgba(255,140,0,0.85); -fx-background-radius: 16; -fx-cursor: hand;");
            btn.setFocusTraversable(false);
            
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(255,100,0,0.95); -fx-background-radius: 16; -fx-cursor: hand;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: rgba(255,140,0,0.85); -fx-background-radius: 16; -fx-cursor: hand;"));
        }

        close.setOnAction(e -> { creato = null; close(); });
        minimize.setOnAction(e -> setIconified(true));

        HBox box = new HBox(5, minimize, close);
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

    // ==================== LOGICA ====================

    private void salva() {
        try {
            hideError();
            
            String nome = nomeField.getText().trim();
            String tipo = tipoField.getText().trim();

            // ✅ Validazione UI minima
            if (nome.isEmpty()) {
                showError("❌ Il nome dell'ingrediente è obbligatorio");
                nomeField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                nomeField.requestFocus();
                return;
            }

            if (tipo.isEmpty()) {
                showError("❌ Il tipo dell'ingrediente è obbligatorio");
                tipoField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                tipoField.requestFocus();
                return;
            }

            // ✅ Delega al controller/service
            int id = controller.creaIngrediente(nome, tipo);
            Optional<Ingrediente> opt = controller.trovaIngredientePerId(id);

            if (opt.isPresent()) {
                creato = opt.get();
                StyleHelper.showSuccessDialog("Successo",
                    String.format("✅ Ingrediente creato!\n\n📝 Nome: %s\n📂 Tipo: %s\n🆔 ID: %d", 
                        nome, tipo, id));
                close();
            } else {
                showError("❌ Ingrediente salvato ma non recuperato");
            }

        } catch (Exception e) {
            showError("❌ " + e.getMessage());
        }
    }

    // ==================== HELPER ====================

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
    }
}
