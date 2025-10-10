package Gui;

import controller.ReportMensileController;
import controller.ReportMensileController.DatiReportMensile;
import exceptions.DataAccessException;
import guihelper.StyleHelper;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.util.Map;

/**
 * ReportMensileGUI — grafica migliorata e user-friendly.
 * Usa chart JavaFX (PieChart, BarChart, LineChart) e colori da StyleHelper.
 */
public class ReportMensileGUI {

    private final ReportMensileController reportController;
    private VBox root;
    private ComboBox<Integer> meseCombo;
    private ComboBox<Integer> annoCombo;
    private VBox reportContainer;

    public ReportMensileGUI(ReportMensileController reportController) {
        this.reportController = reportController;
        inizializzaGUI();
    }

    private void inizializzaGUI() {
        root = new VBox(18);
        root.setPadding(new Insets(28));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: " + StyleHelper.BG_WHITE + ";");

        // Header card
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18));
        header.setStyle(
            "-fx-background-color: linear-gradient(to right, " + StyleHelper.BG_ORANGE_START + ", " + StyleHelper.BG_ORANGE_LIGHT + ");" +
            "-fx-background-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 8, 0, 0, 4);"
        );
        Label titolo = new Label("📊 Report Mensile");
        titolo.setFont(Font.font("Roboto", FontWeight.BOLD, 26));
        titolo.setTextFill(Color.web(StyleHelper.BG_WHITE));
        Label sotto = new Label("Chef: " + reportController.getChefLoggato().getUsername());
        sotto.setFont(Font.font("Roboto", FontWeight.NORMAL, 14));
        sotto.setTextFill(Color.web(StyleHelper.BG_WHITE));
        VBox titles = new VBox(6, titolo, sotto);
        header.getChildren().addAll(titles);

        // Controlli periodo
        HBox controls = new HBox(12);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(12));
        controls.setStyle("-fx-background-color: " + StyleHelper.BG_WHITE + ";");

        meseCombo = new ComboBox<>();
        for (int i = 1; i <= 12; i++) meseCombo.getItems().add(i);
        meseCombo.setValue(LocalDate.now().getMonthValue());
        StyleHelper.styleComboBox(meseCombo);

        annoCombo = new ComboBox<>();
        int annoCorrente = LocalDate.now().getYear();
        for (int i = annoCorrente - 2; i <= annoCorrente + 1; i++) annoCombo.getItems().add(i);
        annoCombo.setValue(annoCorrente);
        StyleHelper.styleComboBox(annoCombo);

        Button generaBtn = StyleHelper.createPrimaryButton("📈 Visualizza");
        generaBtn.setOnAction(e -> generaReport());

        controls.getChildren().addAll(
            creaVBoxLabel("Mese", meseCombo),
            creaVBoxLabel("Anno", annoCombo),
            generaBtn
        );

        // Report container
        reportContainer = new VBox(14);
        reportContainer.setPadding(new Insets(16));
        reportContainer.setAlignment(Pos.TOP_CENTER);

        // Messaggio iniziale
        Label msg = new Label("Seleziona mese e anno e premi Visualizza per generare il report");
        msg.setFont(Font.font("Roboto", FontWeight.NORMAL, 13));
        msg.setTextFill(Color.web(StyleHelper.NEUTRAL_GRAY));
        reportContainer.getChildren().add(msg);

        root.getChildren().addAll(header, controls, new Separator(), reportContainer);
    }

    private VBox creaVBoxLabel(String caption, Control control) {
        Label l = new Label(caption);
        l.setFont(Font.font("Roboto", FontWeight.NORMAL, 12));
        l.setTextFill(Color.web(StyleHelper.NEUTRAL_GRAY));
        VBox box = new VBox(6, l, control);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private void generaReport() {
        Integer mese = meseCombo.getValue();
        Integer anno = annoCombo.getValue();
        if (mese == null || anno == null) {
            StyleHelper.showValidationDialog("Errore", "Seleziona sia il mese che l'anno");
            return;
        }
        try {
            DatiReportMensile dati = reportController.generaReport(mese, anno);
            Map<Integer, Integer> ricPerGiorno = reportController.getRicettePerGiorno(mese, anno);
            mostraReport(dati, ricPerGiorno);
        } catch (DataAccessException e) {
            StyleHelper.showErrorDialog("Errore Database",
                "Errore durante la generazione del report: " + e.getMessage());
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Errore imprevisto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostraReport(DatiReportMensile d, Map<Integer, Integer> ricPerGiorno) {
        reportContainer.getChildren().clear();

        // titolo
        Label titoloReport = new Label("Report — " + d.getNomeMese() + " " + d.getAnno());
        titoloReport.setFont(Font.font("Roboto", FontWeight.BOLD, 20));
        titoloReport.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        // tiles statistiche
        HBox tiles = new HBox(14);
        tiles.setAlignment(Pos.CENTER);
        tiles.getChildren().addAll(
            creaTile("📚 Corsi", String.valueOf(d.getNumeroCorsi()), StyleHelper.PRIMARY_LIGHT),
            creaTile("💻 Online", String.valueOf(d.getSessioniOnline()), StyleHelper.INFO_BLUE),
            creaTile("🍳 Pratiche", String.valueOf(d.getSessioniPratiche()), StyleHelper.SUCCESS_GREEN)
        );

        // charts container
        HBox charts = new HBox(18);
        charts.setAlignment(Pos.CENTER);
        charts.setPadding(new Insets(10));

        // PIE: online vs pratiche
        PieChart pie = new PieChart();
        PieChart.Data sliceOnline = new PieChart.Data("Online", d.getSessioniOnline());
        PieChart.Data slicePratiche = new PieChart.Data("Pratiche", d.getSessioniPratiche());
        pie.getData().addAll(sliceOnline, slicePratiche);
        pie.setLabelsVisible(true);
        pie.setLegendVisible(true);
        pie.setPrefSize(380, 280);

        // BAR: ricette per giorno (se ci sono dati)
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> bar = new BarChart<>(xAxis, yAxis);
        bar.setTitle("Ricette per giorno");
        bar.setLegendVisible(false);
        bar.setPrefSize(520, 280);

        XYChart.Series<String, Number> seriesRic = new XYChart.Series<>();
        if (ricPerGiorno != null && !ricPerGiorno.isEmpty()) {
            ricPerGiorno.forEach((giorno, count) -> seriesRic.getData().add(new XYChart.Data<>(String.valueOf(giorno), count)));
        } else {
            // aggiungo un valore zero per mostrare grafico vuoto elegante
            seriesRic.getData().add(new XYChart.Data<>("0", 0));
        }
        bar.getData().add(seriesRic);

        // LineChart: trend (utilizzo se vuoi un'alternativa)
        NumberAxis y2 = new NumberAxis();
        CategoryAxis x2 = new CategoryAxis();
        LineChart<String, Number> line = new LineChart<>(x2, y2);
        line.setTitle("Trend ricette (giorni)");
        line.setLegendVisible(false);
        line.setPrefSize(520, 220);
        XYChart.Series<String, Number> seriesLine = new XYChart.Series<>();
        ricPerGiorno.forEach((g, c) -> seriesLine.getData().add(new XYChart.Data<>(String.valueOf(g), c)));
        line.getData().add(seriesLine);

        // styling dei chart (applico colori usando Platform.runLater per assicurarmi che i nodes siano creati)
        Platform.runLater(() -> {
            // Pie colors
            if (!pie.getData().isEmpty()) {
                if (pie.getData().size() > 0) pie.getData().get(0).getNode().setStyle("-fx-pie-color: " + StyleHelper.INFO_BLUE + ";");
                if (pie.getData().size() > 1) pie.getData().get(1).getNode().setStyle("-fx-pie-color: " + StyleHelper.SUCCESS_GREEN + ";");
            }
            // Bar color
            for (XYChart.Series<String, Number> s : bar.getData()) {
                for (XYChart.Data<String, Number> data : s.getData()) {
                    if (data.getNode() != null) {
                        data.getNode().setStyle("-fx-bar-fill: " + StyleHelper.PRIMARY_ORANGE + ";");
                    }
                }
            }
            // Line series color
            for (XYChart.Series<String, Number> s : line.getData()) {
                for (XYChart.Data<String, Number> data : s.getData()) {
                    if (data.getNode() != null) {
                        data.getNode().setStyle("-fx-stroke: " + StyleHelper.INFO_BLUE + ";");
                    }
                }
            }
        });

        // ricette stats card
        VBox ricStats = creaStatisticheRicette(d);

        // layout charts: left pie + right bar
        VBox leftCol = new VBox(12, pie, ricStats);
        leftCol.setAlignment(Pos.TOP_CENTER);

        VBox rightCol = new VBox(12, bar, line);
        rightCol.setAlignment(Pos.TOP_CENTER);

        charts.getChildren().addAll(leftCol, rightCol);

        // riepilogo
        Label riepilogo = new Label(generaMessaggioRiepilogo(d));
        riepilogo.setFont(Font.font("Roboto", FontWeight.NORMAL, 13));
        riepilogo.setTextFill(Color.web(StyleHelper.NEUTRAL_GRAY));
        riepilogo.setWrapText(true);
        riepilogo.setMaxWidth(980);

        // Azioni utili (export, refresh)
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button refresh = StyleHelper.createInfoButton("Aggiorna");
        refresh.setOnAction(e -> generaReport());
        Button export = StyleHelper.createSecondaryButton("Esporta PDF");
        // export action: lasciato come hook (implementazione export a parte)
        export.setOnAction(e -> StyleHelper.showInfoDialog("Non implementato", "Funzione Esporta PDF non ancora implementata"));

        actions.getChildren().addAll(refresh, export);

        reportContainer.getChildren().addAll(
            titoloReport,
            tiles,
            new Separator(),
            charts,
            new Separator(),
            actions,
            riepilogo
        );
    }

    private VBox creaTile(String titolo, String valore, String colore) {
        VBox tile = new VBox(6);
        tile.setAlignment(Pos.CENTER);
        tile.setPadding(new Insets(14));
        tile.setPrefWidth(200);
        tile.setStyle(
            "-fx-background-color: " + StyleHelper.BG_WHITE + ";" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: rgba(0,0,0,0.04);" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 3);"
        );

        Label t = new Label(titolo);
        t.setFont(Font.font("Roboto", FontWeight.MEDIUM, 13));
        t.setTextFill(Color.web(StyleHelper.NEUTRAL_GRAY));

        Label v = new Label(valore);
        v.setFont(Font.font("Roboto", FontWeight.BOLD, 28));
        v.setTextFill(Color.web(colore));

        tile.getChildren().addAll(t, v);
        return tile;
    }

    private VBox creaStatisticheRicette(DatiReportMensile d) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        if (d.getSessioniPratiche() > 0) {
            Label lbl = new Label("📖 Statistiche Ricette");
            lbl.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
            lbl.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

            HBox stats = new HBox(12);
            stats.setAlignment(Pos.CENTER);
            stats.getChildren().addAll(
                creaStatCard("Media", String.format("%.1f", d.getMediaRicette()), StyleHelper.INFO_BLUE),
                creaStatCard("Minimo", String.valueOf(d.getMinRicette()), StyleHelper.PRIMARY_LIGHT),
                creaStatCard("Massimo", String.valueOf(d.getMaxRicette()), StyleHelper.SUCCESS_GREEN)
            );
            box.getChildren().addAll(lbl, stats);
        } else {
            Label none = new Label("📖 Nessuna sessione pratica nel periodo selezionato");
            none.setFont(Font.font("Roboto", FontWeight.NORMAL, 13));
            none.setTextFill(Color.web(StyleHelper.NEUTRAL_GRAY));
            box.getChildren().add(none);
        }
        return box;
    }

    private VBox creaStatCard(String titolo, String valore, String colore) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12));
        card.setPrefWidth(120);
        card.setStyle(
            "-fx-background-color: " + StyleHelper.BG_WHITE + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: rgba(0,0,0,0.04);" +
            "-fx-border-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 6, 0, 0, 2);"
        );

        Label t = new Label(titolo);
        t.setFont(Font.font("Roboto", FontWeight.NORMAL, 12));
        t.setTextFill(Color.web(StyleHelper.NEUTRAL_GRAY));

        Label v = new Label(valore);
        v.setFont(Font.font("Roboto", FontWeight.BOLD, 20));
        v.setTextFill(Color.web(colore));

        card.getChildren().addAll(t, v);
        return card;
    }

    private String generaMessaggioRiepilogo(DatiReportMensile d) {
        StringBuilder sb = new StringBuilder("💡 Riepilogo: ");
        if (d.getNumeroCorsi() == 0) {
            sb.append("Non hai tenuto corsi in questo mese.");
        } else {
            sb.append("Hai tenuto ").append(d.getNumeroCorsi())
              .append(d.getNumeroCorsi() > 1 ? " corsi" : " corso")
              .append(" con ").append(d.getSessioniOnline())
              .append(" online e ").append(d.getSessioniPratiche()).append(" pratiche.");
            if (d.getSessioniPratiche() > 0 && d.getMediaRicette() > 0) {
                sb.append(" Media ricette: ").append(String.format("%.1f", d.getMediaRicette())).append(".");
            }
        }
        return sb.toString();
    }

    public VBox getRoot() {
        return root;
    }
}
