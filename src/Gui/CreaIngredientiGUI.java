package Gui;

import controller.IngredienteController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Ingrediente;
import util.StyleHelper;


public class CreaIngredientiGUI extends Stage {

    private IngredienteController ingredienteController;
    private Ingrediente ingredienteCreato = null;

    private TextField nomeField;
    private ComboBox<String> tipoCombo;
    private TextField tipoPersonalizzatoField;

    // Drag support
    private double xOffset = 0;
    private double yOffset = 0;

    public CreaIngredientiGUI(IngredienteController controller) {
        this.ingredienteController = controller;
        setTitle("Crea Ingrediente");
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UNDECORATED);
        setResizable(false);

        createLayout();
    }

    private void createLayout() {
        StackPane rootPane = new StackPane();
        rootPane.setMinSize(500, 400);
        rootPane.setPrefSize(550, 450);

        Region background = new Region();
        StyleHelper.applyBackgroundGradient(background);

        VBox mainContainer = new VBox(25);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(40, 30, 30, 30));

        Label title = new Label("🥕 Crea Nuovo Ingrediente");
        title.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);

        VBox formCard = StyleHelper.createSection();
        formCard.setSpacing(20);

        VBox formSection = createFormSection();
        HBox buttonSection = createButtonSection();

        formCard.getChildren().addAll(formSection, new Separator(), buttonSection);

        mainContainer.getChildren().addAll(title, formCard);
        rootPane.getChildren().addAll(background, mainContainer);

        HBox windowButtons = createWindowButtons();
        rootPane.getChildren().add(windowButtons);
        StackPane.setAlignment(windowButtons, Pos.TOP_RIGHT);
        StackPane.setMargin(windowButtons, new Insets(8));

        makeDraggable(rootPane);

        Scene scene = new Scene(rootPane, 550, 450);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);
    }

    private HBox createWindowButtons() {
        Button closeButton = new Button("✕");
        Button minimizeButton = new Button("−");
        Button maximizeButton = new Button("○");

        Button[] buttons = { minimizeButton, maximizeButton, closeButton };
        for (Button btn : buttons) {
            btn.setPrefSize(28, 28);
            btn.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 11));
            btn.setTextFill(Color.WHITE);
            btn.setStyle("-fx-background-color: rgba(255,140,0,0.8); -fx-background-radius: 14; -fx-cursor: hand;");
            btn.setFocusTraversable(false);
        }

        closeButton.setOnAction(e -> {
            ingredienteCreato = null;
            close();
        });
        minimizeButton.setOnAction(e -> setIconified(true));
        maximizeButton.setOnAction(e -> setMaximized(!isMaximized()));

        HBox box = new HBox(3, minimizeButton, maximizeButton, closeButton);
        box.setAlignment(Pos.TOP_RIGHT);
        box.setPickOnBounds(false);
        return box;
    }

    private void makeDraggable(StackPane root) {
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        root.setOnMouseDragged(event -> {
            setX(event.getScreenX() - xOffset);
            setY(event.getScreenY() - yOffset);
        });
    }

    private VBox createFormSection() {
        VBox section = new VBox(20);

        Label sectionTitle = new Label("📝 Informazioni Ingrediente");
        sectionTitle.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(20);

        // campo nome
        nomeField = StyleHelper.createTextField("Es. Pomodoro San Marzano");
        nomeField.setPrefWidth(300);

        // combo tipi
        tipoCombo = StyleHelper.createComboBox();
        tipoCombo.setPrefWidth(200);
        tipoCombo.getItems().addAll(
            "Verdura","Frutta","Carne","Pesce","Latticini","Cereali",
            "Legumi","Spezie","Condimenti","Dolcificanti","Bevande",
            "Altro","Personalizzato"
        );
        tipoCombo.setPromptText("Seleziona tipo");

        // tipo personalizzato
        tipoPersonalizzatoField = StyleHelper.createTextField("Specifica il tipo...");
        tipoPersonalizzatoField.setPrefWidth(200);
        tipoPersonalizzatoField.setVisible(false);
        tipoPersonalizzatoField.setManaged(false);

        tipoCombo.setOnAction(e -> {
            boolean personalizzato = "Personalizzato".equals(tipoCombo.getValue());
            tipoPersonalizzatoField.setVisible(personalizzato);
            tipoPersonalizzatoField.setManaged(personalizzato);
        });

        grid.add(StyleHelper.createLabel("Nome Ingrediente:"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(StyleHelper.createLabel("Tipo:"), 0, 1);
        grid.add(tipoCombo, 1, 1);
        grid.add(StyleHelper.createLabel("Tipo Personalizzato:"), 0, 2);
        grid.add(tipoPersonalizzatoField, 1, 2);

        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }

    private HBox createButtonSection() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        Button annullaBtn = new Button("❌ Annulla");
        annullaBtn.setPrefSize(120, 40);
        annullaBtn.setStyle("-fx-background-color:" + StyleHelper.NEUTRAL_GRAY + "; -fx-text-fill:white; -fx-background-radius:20; -fx-cursor: hand; -fx-font-weight:bold;");
        annullaBtn.setOnAction(e -> {
            ingredienteCreato = null;
            close();
        });

        Button salvaBtn = StyleHelper.createPrimaryButton("💾 Salva");
        salvaBtn.setPrefSize(120, 40);
        salvaBtn.setOnAction(e -> salvaIngrediente());

        buttonBox.getChildren().addAll(annullaBtn, salvaBtn);
        return buttonBox;
    }

    // Fix: usa creaIngrediente(nome, tipo)
    private void salvaIngrediente() {
        try {
            if (!validateForm()) return;

            String nome = nomeField.getText().trim();
            String tipo = tipoCombo.getValue();
            if ("Personalizzato".equals(tipo)) {
                tipo = tipoPersonalizzatoField.getText().trim();
            }

            if (ingredienteController != null) {
                try {
                    int idCreato = ingredienteController.creaIngrediente(nome, tipo);
                    ingredienteCreato = new Ingrediente(nome, tipo);
                    showAlert("Successo",
                        "Ingrediente creato: " + nome + " (" + tipo + ")\nID: " + idCreato);
                } catch (Exception e) {
                    showAlert("Errore DB", e.getMessage());
                    return;
                }
            } else {
                ingredienteCreato = new Ingrediente(nome, tipo);
                showAlert("Successo",
                    "Ingrediente creato: " + nome + " (" + tipo + ")");
            }

            close();
        } catch (Exception e) {
            showAlert("Errore", e.getMessage());
        }
    }

    private boolean validateForm() {
        if (nomeField.getText().trim().isEmpty()) {
            showAlert("Campo obbligatorio", "Nome mancante");
            nomeField.requestFocus();
            return false;
        }
        if (tipoCombo.getValue() == null) {
            showAlert("Campo obbligatorio", "Tipo non selezionato");
            tipoCombo.requestFocus();
            return false;
        }
        if ("Personalizzato".equals(tipoCombo.getValue())
            && tipoPersonalizzatoField.getText().trim().isEmpty()) {
            showAlert("Campo obbligatorio", "Tipo personalizzato mancante");
            tipoPersonalizzatoField.requestFocus();
            return false;
        }
        return true;
    }

    // mostra alert base
    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.equals("Successo")
            ? Alert.AlertType.INFORMATION
            : Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Ingrediente showAndReturn() {
        showAndWait();
        return ingredienteCreato;
    }
}
