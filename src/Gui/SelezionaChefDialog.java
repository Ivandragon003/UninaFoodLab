package Gui;

import controller.GestioneCorsoController;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Chef;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
public class SelezionaChefDialog extends Stage {
    private ListView<Chef> listaChef;
    private List<Chef> selected = new ArrayList<>();

    public SelezionaChefDialog(List<Chef> chefDisponibili) {
        setTitle("Seleziona Chef");
        initModality(Modality.APPLICATION_MODAL);

        listaChef = new ListView<>(FXCollections.observableArrayList(chefDisponibili));
        listaChef.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Button confermaBtn = new Button("Conferma");
        confermaBtn.setOnAction(e -> {
            selected.addAll(listaChef.getSelectionModel().getSelectedItems());
            close();
        });

        VBox layout = new VBox(10, new Label("Seleziona uno o più chef:"), listaChef, confermaBtn);
        layout.setPadding(new Insets(15));
        setScene(new Scene(layout, 400, 300));
    }

    public List<Chef> showAndReturn() {
        showAndWait();
        return selected;
    }
}

