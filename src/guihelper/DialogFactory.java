package guihelper;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class DialogFactory {

    private final Stage parentStage;

    public DialogFactory(Stage parentStage) {
        this.parentStage = parentStage;
    }

    public void showSuccess(String message) {
        showDialog("Operazione completata", message, StyleHelper.SUCCESS_GREEN);
    }

    public void showError(String message) {
        showDialog("Errore", message, StyleHelper.ERROR_RED);
    }

    public void showWarning(String message) {
        showDialog("Attenzione", message, StyleHelper.SECONDARY_BEIGE);
    }

    private void showDialog(String title, String message, String color) {
        Stage dialog = new Stage(StageStyle.UTILITY);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);
        dialog.setTitle(title);

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495E; -fx-alignment: center;");

        Button closeButton = StyleHelper.createStyledButton("Chiudi", color);
        closeButton.setOnAction(e -> dialog.close());

        VBox box = new VBox(15, messageLabel, closeButton);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));

        dialog.setScene(new Scene(box, 320, 150));
        dialog.showAndWait();
    }
}
