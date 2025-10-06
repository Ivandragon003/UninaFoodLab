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
import java.util.Optional;

public class CreaIngredientiGUI extends Stage {
    private IngredienteController ingredienteController;
    private Ingrediente ingredienteCreato = null;
    private TextField nomeField;
    private TextField tipoField; // Campo testo libero invece di ComboBox
    private Label errorLabel; // Errore inline rosso
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
        rootPane.setMinSize(550, 450);
        rootPane.setPrefSize(600, 480);

        Region background = new Region();
        StyleHelper.applyBackgroundGradient(background);

        VBox mainContainer = new VBox(25);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(40, 40, 30, 40));

        Label title = new Label("🥕 Crea Nuovo Ingrediente");
        title.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 26));
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);

        Label subtitle = new Label("Aggiungi un ingrediente alla tua dispensa");
        subtitle.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.NORMAL, 14));
        subtitle.setTextFill(Color.web("#FFFFFF", 0.9));
        subtitle.setAlignment(Pos.CENTER);

        VBox titleBox = new VBox(8, title, subtitle);
        titleBox.setAlignment(Pos.CENTER);

        VBox formCard = StyleHelper.createSection();
        formCard.setSpacing(25);
        formCard.setPadding(new Insets(30));

        // Label errore inline
        errorLabel = new Label();
        errorLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 13));
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);
        errorLabel.setAlignment(Pos.CENTER);
        errorLabel.setStyle("-fx-background-color: #ffe6e6; -fx-padding: 10; -fx-background-radius: 8;");

        VBox formSection = createFormSection();
        HBox buttonSection = createButtonSection();

        formCard.getChildren().addAll(errorLabel, formSection, new Separator(), buttonSection);
        mainContainer.getChildren().addAll(titleBox, formCard);
        rootPane.getChildren().addAll(background, mainContainer);

        HBox windowButtons = createWindowButtons();
        rootPane.getChildren().add(windowButtons);
        StackPane.setAlignment(windowButtons, Pos.TOP_RIGHT);
        StackPane.setMargin(windowButtons, new Insets(10));

        makeDraggable(rootPane);

        Scene scene = new Scene(rootPane, 600, 480);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);
    }

    private HBox createWindowButtons() {
        Button closeButton = new Button("✕");
        Button minimizeButton = new Button("−");

        Button[] buttons = { minimizeButton, closeButton };
        for (Button btn : buttons) {
            btn.setPrefSize(32, 32);
            btn.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 12));
            btn.setTextFill(Color.WHITE);
            btn.setStyle("-fx-background-color: rgba(255,140,0,0.85); -fx-background-radius: 16; -fx-cursor: hand;");
            btn.setFocusTraversable(false);
            
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(255,100,0,0.95); -fx-background-radius: 16; -fx-cursor: hand;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: rgba(255,140,0,0.85); -fx-background-radius: 16; -fx-cursor: hand;"));
        }

        closeButton.setOnAction(e -> {
            ingredienteCreato = null;
            close();
        });
        minimizeButton.setOnAction(e -> setIconified(true));

        HBox box = new HBox(5, minimizeButton, closeButton);
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

        Label sectionTitle = new Label("📝 Informazioni Base");
        sectionTitle.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(20);
        grid.setAlignment(Pos.CENTER_LEFT);

        Label nomeLabel = StyleHelper.createLabel("Nome Ingrediente:");
        nomeLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.SEMI_BOLD, 14));

        nomeField = StyleHelper.createTextField("Es. Pomodoro San Marzano");
        nomeField.setPrefWidth(350);
        nomeField.setPrefHeight(38);
        nomeField.setStyle("-fx-font-size: 14px; -fx-padding: 8 12;");
        
        // Nascondi errore quando l'utente inizia a digitare
        nomeField.textProperty().addListener((obs, old, val) -> {
            if (!val.trim().isEmpty()) {
                nomeField.setStyle("-fx-font-size: 14px; -fx-padding: 8 12;");
                nascondiErrore();
            }
        });

        Label tipoLabel = StyleHelper.createLabel("Tipo (es. Verdura, Carne, Spezie...):");
        tipoLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.SEMI_BOLD, 14));

        tipoField = StyleHelper.createTextField("Es. Verdura");
        tipoField.setPrefWidth(350);
        tipoField.setPrefHeight(38);
        tipoField.setStyle("-fx-font-size: 14px; -fx-padding: 8 12;");
        
        // Nascondi errore quando l'utente inizia a digitare
        tipoField.textProperty().addListener((obs, old, val) -> {
            if (!val.trim().isEmpty()) {
                tipoField.setStyle("-fx-font-size: 14px; -fx-padding: 8 12;");
                nascondiErrore();
            }
        });

        Label suggerimentiLabel = new Label("💡 Esempi: Verdura, Frutta, Carne, Pesce, Latticini, Cereali, Legumi, Spezie, Condimenti");
        suggerimentiLabel.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.NORMAL, 11));
        suggerimentiLabel.setTextFill(Color.web(StyleHelper.INFO_BLUE));
        suggerimentiLabel.setWrapText(true);
        suggerimentiLabel.setMaxWidth(350);

        grid.add(nomeLabel, 0, 0);
        grid.add(nomeField, 0, 1);

        grid.add(tipoLabel, 0, 2);
        grid.add(tipoField, 0, 3);
        grid.add(suggerimentiLabel, 0, 4);

        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }

    private HBox createButtonSection() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 5, 0));

        Button annullaBtn = new Button("❌ Annulla");
        annullaBtn.setPrefSize(140, 45);
        annullaBtn.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 14));
        annullaBtn.setStyle("-fx-background-color: " + StyleHelper.NEUTRAL_GRAY + "; " +
                "-fx-text-fill: white; -fx-background-radius: 22; -fx-cursor: hand;");
        annullaBtn.setOnMouseEntered(e -> annullaBtn.setStyle("-fx-background-color: #5A5A5A; " +
                "-fx-text-fill: white; -fx-background-radius: 22; -fx-cursor: hand;"));
        annullaBtn.setOnMouseExited(e -> annullaBtn.setStyle("-fx-background-color: " + StyleHelper.NEUTRAL_GRAY + "; " +
                "-fx-text-fill: white; -fx-background-radius: 22; -fx-cursor: hand;"));
        annullaBtn.setOnAction(e -> {
            ingredienteCreato = null;
            close();
        });

        Button salvaBtn = StyleHelper.createPrimaryButton("💾 Salva Ingrediente");
        salvaBtn.setPrefSize(180, 45);
        salvaBtn.setFont(javafx.scene.text.Font.font("Roboto", javafx.scene.text.FontWeight.BOLD, 14));
        salvaBtn.setOnAction(e -> salvaIngrediente());

        buttonBox.getChildren().addAll(annullaBtn, salvaBtn);
        return buttonBox;
    }

    private void salvaIngrediente() {
        try {
            nascondiErrore();
            
            String nome = nomeField.getText().trim();
            String tipo = tipoField.getText().trim();

            // Validazione nome
            if (nome.isEmpty()) {
                mostraErrore("❌ Il nome dell'ingrediente è obbligatorio");
                nomeField.setStyle("-fx-font-size: 14px; -fx-padding: 8 12; -fx-border-color: red; -fx-border-width: 2px;");
                nomeField.requestFocus();
                return;
            }

            // Validazione tipo
            if (tipo.isEmpty()) {
                mostraErrore("❌ Il tipo dell'ingrediente è obbligatorio");
                tipoField.setStyle("-fx-font-size: 14px; -fx-padding: 8 12; -fx-border-color: red; -fx-border-width: 2px;");
                tipoField.requestFocus();
                return;
            }

            if (ingredienteController != null) {
                try {
                    int idCreato = ingredienteController.creaIngrediente(nome, tipo);
                    Optional<Ingrediente> ingredienteOpt = ingredienteController.trovaIngredientePerId(idCreato);

                    if (ingredienteOpt.isPresent()) {
                        ingredienteCreato = ingredienteOpt.get();
                        StyleHelper.showSuccessDialog("Successo",
                                "Ingrediente creato con successo!\n\n" +
                                "📝 Nome: " + nome + "\n" +
                                "📂 Tipo: " + tipo + "\n" +
                                "🆔 ID: " + idCreato);
                    } else {
                        mostraErrore("❌ Ingrediente salvato ma non recuperato dal database");
                        return;
                    }

                } catch (Exception e) {
                    mostraErrore("❌ Errore database: " + e.getMessage());
                    return;
                }
            } else {
                ingredienteCreato = new Ingrediente(nome, tipo);
                StyleHelper.showSuccessDialog("Successo", "Ingrediente creato: " + nome + " (" + tipo + ")");
            }

            close();

        } catch (Exception e) {
            mostraErrore("❌ Errore imprevisto: " + e.getMessage());
        }
    }

    private void mostraErrore(String messaggio) {
        errorLabel.setText(messaggio);
        errorLabel.setVisible(true);
    }

    private void nascondiErrore() {
        errorLabel.setVisible(false);
    }

    public Ingrediente showAndReturn() {
        showAndWait();
        return ingredienteCreato;
    }
}
