package Gui;

import controller.ReportMensileController;
import controller.ReportMensileController.DatiReportMensile;
import exceptions.DataAccessException;
import guihelper.StyleHelper;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ReportMensileGUI {

    private final ReportMensileController reportController;
    private VBox root;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private ScrollPane scrollPane;
    private VBox reportContainer;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ReportMensileGUI(ReportMensileController reportController) {
        this.reportController = reportController;
        inizializzaGUI();
    }

    private void inizializzaGUI() {
        root = new VBox(0);
        root.setStyle("-fx-background-color: " + StyleHelper.BG_LIGHT + ";");

        // Header con gradiente e animazione
        VBox header = createModernHeader();

        // Pannello controlli con design card
        VBox controlsCard = createControlsPanel();

        // Container report scrollabile
        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-background: transparent;" +
            "-fx-border-color: transparent;"
        );
        
        reportContainer = new VBox(20);
        reportContainer.setPadding(new Insets(30));
        reportContainer.setAlignment(Pos.TOP_CENTER);
        reportContainer.setStyle("-fx-background-color: transparent;");
        
        // Messaggio iniziale
        VBox emptyState = createEmptyState();
        reportContainer.getChildren().add(emptyState);
        
        scrollPane.setContent(reportContainer);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        root.getChildren().addAll(header, controlsCard, scrollPane);
    }

    private VBox createModernHeader() {
        VBox header = new VBox(8);
        header.setPadding(new Insets(35, 40, 35, 40));
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Gradiente moderno con effetto diagonale
        StyleHelper.applyBackgroundGradient(header);
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.15));
        shadow.setRadius(15);
        shadow.setOffsetY(5);
        header.setEffect(shadow);

        HBox titleBox = new HBox(15);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Label icon = new Label("📊");
        icon.setStyle("-fx-font-size: 42px;");
        
        VBox textBox = new VBox(5);
        Label titolo = new Label("Report Periodico");
        titolo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 34));
        titolo.setTextFill(Color.WHITE);
        
        Label sotto = new Label("Chef: " + reportController.getChefLoggato().getUsername());
        sotto.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        sotto.setTextFill(Color.rgb(255, 255, 255, 0.9));
        
        textBox.getChildren().addAll(titolo, sotto);
        titleBox.getChildren().addAll(icon, textBox);
        header.getChildren().add(titleBox);

        return header;
    }

    private VBox createControlsPanel() {
        VBox card = new VBox(20);
        card.setPadding(new Insets(25, 35, 25, 35));
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 0 0 20 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0.0, 0.0, 5.0);"
        );

        Label sectionTitle = new Label("⚙️ Configurazione Periodo");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 18));
        sectionTitle.setTextFill(Color.web(StyleHelper.TEXT_BLACK));

        HBox controls = new HBox(25);
        controls.setAlignment(Pos.CENTER_LEFT);

        startDatePicker = StyleHelper.createDatePicker();
        startDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
        startDatePicker.setPrefWidth(200);

        endDatePicker = StyleHelper.createDatePicker();
        endDatePicker.setValue(LocalDate.now());
        endDatePicker.setPrefWidth(200);

        Button generaBtn = StyleHelper.createPrimaryButton("📈 Genera Report");
        generaBtn.setPrefWidth(180);
        generaBtn.setPrefHeight(45);
        generaBtn.setOnAction(e -> generaReport());

        controls.getChildren().addAll(
            creaDatePickerBox("📅 Data Inizio", startDatePicker),
            creaDatePickerBox("📅 Data Fine", endDatePicker),
            createSpacer(),
            generaBtn
        );

        card.getChildren().addAll(sectionTitle, controls);
        return card;
    }

    private Region createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private VBox creaDatePickerBox(String caption, DatePicker picker) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        
        Label label = new Label(caption);
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        label.setTextFill(Color.web(StyleHelper.TEXT_GRAY));
        
        box.getChildren().addAll(label, picker);
        return box;
    }

    private VBox createEmptyState() {
        VBox empty = new VBox(20);
        empty.setAlignment(Pos.CENTER);
        empty.setPadding(new Insets(80));
        empty.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: " + StyleHelper.BORDER_LIGHT + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 20;"
        );

        Label icon = new Label("📊");
        icon.setStyle("-fx-font-size: 64px; -fx-opacity: 0.6;");
        
        Label msg = new Label("Nessun Report Generato");
        msg.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        msg.setTextFill(Color.web(StyleHelper.TEXT_BLACK));
        
        Label hint = new Label("Seleziona un intervallo di date e premi 'Genera Report'");
        hint.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        hint.setTextFill(Color.web(StyleHelper.TEXT_GRAY));
        hint.setWrapText(true);
        hint.setMaxWidth(400);
        hint.setAlignment(Pos.CENTER);
        hint.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        empty.getChildren().addAll(icon, msg, hint);
        return empty;
    }

    private void generaReport() {
        LocalDate inizio = startDatePicker.getValue();
        LocalDate fine = endDatePicker.getValue();
        
        if (inizio == null || fine == null) {
            StyleHelper.showValidationDialog("Validazione", "Seleziona entrambe le date");
            return;
        }
        
        if (inizio.isAfter(fine)) {
            StyleHelper.showValidationDialog("Validazione", "La data di inizio deve essere precedente alla data di fine");
            return;
        }
        
        try {
            DatiReportMensile dati = reportController.generaReport(inizio, fine);
            Map<LocalDate, Integer> ricPerGiorno = reportController.getRicettePerGiorno(inizio, fine);
            mostraReport(dati, ricPerGiorno);
        } catch (DataAccessException e) {
            StyleHelper.showErrorDialog("Errore Database",
                "Impossibile generare il report: " + e.getMessage());
        } catch (Exception e) {
            StyleHelper.showErrorDialog("Errore", "Si è verificato un errore imprevisto");
            e.printStackTrace();
        }
    }

    private void mostraReport(DatiReportMensile d, Map<LocalDate, Integer> ricPerGiorno) {
        reportContainer.getChildren().clear();

        // Header del report con animazione
        VBox reportHeader = createReportHeader(d);
        
        // Tiles statistiche principali
        HBox tilesBox = createStatisticsTiles(d);
        
        // Grafici
        HBox chartsBox = createChartsSection(d, ricPerGiorno);
        
        // Riepilogo testuale
        VBox summaryBox = createSummarySection(d);

        reportContainer.getChildren().addAll(reportHeader, tilesBox, chartsBox, summaryBox);
        
        // Animazione fade-in
        animateFadeIn(reportContainer);
    }

    private VBox createReportHeader(DatiReportMensile d) {
        VBox header = new VBox(12);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(25));
        header.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0.0, 0.0, 4.0);"
        );

        Label icon = new Label("📈");
        icon.setStyle("-fx-font-size: 48px;");

        Label titolo = new Label("Report Generato");
        titolo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titolo.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        Label periodo = new Label(
            d.getInizio().format(DATE_FORMATTER) + " → " + d.getFine().format(DATE_FORMATTER)
        );
        periodo.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        periodo.setTextFill(Color.web(StyleHelper.TEXT_GRAY));

        header.getChildren().addAll(icon, titolo, periodo);
        return header;
    }

    private HBox createStatisticsTiles(DatiReportMensile d) {
        HBox tiles = new HBox(20);
        tiles.setAlignment(Pos.CENTER);
        tiles.setPadding(new Insets(10, 0, 10, 0));

        tiles.getChildren().addAll(
            creaTileModerna("📚", "Corsi Totali", String.valueOf(d.getNumeroCorsi()), 
                StyleHelper.PRIMARY_ORANGE, "#FFE8D6"),
            creaTileModerna("💻", "Sessioni Online", String.valueOf(d.getSessioniOnline()), 
                StyleHelper.INFO_BLUE, "#E6F9F8"),
            creaTileModerna("🍳", "Sessioni Pratiche", String.valueOf(d.getSessioniPratiche()), 
                StyleHelper.SUCCESS_GREEN, "#E8FFF5")
        );

        return tiles;
    }

    private VBox creaTileModerna(String emoji, String titolo, String valore, String colorAccent, String bgColor) {
        VBox tile = new VBox(12);
        tile.setAlignment(Pos.CENTER);
        tile.setPadding(new Insets(25, 35, 25, 35));
        tile.setPrefWidth(220);
        tile.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: " + colorAccent + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0.0, 0.0, 4.0);"
        );

        // Hover effect
        tile.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), tile);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });

        tile.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), tile);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        Label iconLabel = new Label(emoji);
        iconLabel.setStyle("-fx-font-size: 42px;");

        Label titleLabel = new Label(titolo);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        titleLabel.setTextFill(Color.web(StyleHelper.TEXT_GRAY));
        titleLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Label valueLabel = new Label(valore);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        valueLabel.setTextFill(Color.web(colorAccent));

        tile.getChildren().addAll(iconLabel, titleLabel, valueLabel);
        return tile;
    }

    private HBox createChartsSection(DatiReportMensile d, Map<LocalDate, Integer> ricPerGiorno) {
        HBox chartsBox = new HBox(25);
        chartsBox.setAlignment(Pos.CENTER);
        chartsBox.setPadding(new Insets(10, 0, 10, 0));

        VBox leftColumn = new VBox(20);
        leftColumn.setAlignment(Pos.TOP_CENTER);
        leftColumn.setPrefWidth(450);

        // PieChart moderno
        VBox pieCard = createPieChartCard(d);
        
        // Statistiche ricette
        VBox statsCard = creaStatisticheRicetteCard(d);

        leftColumn.getChildren().addAll(pieCard, statsCard);

        // BarChart moderno
        VBox barCard = createBarChartCard(ricPerGiorno);
        barCard.setPrefWidth(550);

        chartsBox.getChildren().addAll(leftColumn, barCard);
        return chartsBox;
    }

    private VBox createPieChartCard(DatiReportMensile d) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0.0, 0.0, 4.0);"
        );

        Label title = new Label("📊 Distribuzione Sessioni");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        title.setTextFill(Color.web(StyleHelper.TEXT_BLACK));

        PieChart pie = new PieChart();
        pie.getData().addAll(
            new PieChart.Data("Online (" + d.getSessioniOnline() + ")", d.getSessioniOnline()),
            new PieChart.Data("Pratiche (" + d.getSessioniPratiche() + ")", d.getSessioniPratiche())
        );
        pie.setLabelsVisible(true);
        pie.setLegendVisible(true);
        pie.setPrefSize(400, 300);
        pie.setStyle("-fx-font-size: 12px;");

        // Colori personalizzati
        pie.getData().get(0).getNode().setStyle("-fx-pie-color: " + StyleHelper.INFO_BLUE + ";");
        pie.getData().get(1).getNode().setStyle("-fx-pie-color: " + StyleHelper.SUCCESS_GREEN + ";");

        card.getChildren().addAll(title, pie);
        return card;
    }

    private VBox createBarChartCard(Map<LocalDate, Integer> ricPerGiorno) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0.0, 0.0, 4.0);"
        );

        Label title = new Label("📈 Ricette per Giorno");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        title.setTextFill(Color.web(StyleHelper.TEXT_BLACK));

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Giorno del Mese");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Numero Ricette");
        
        BarChart<String, Number> bar = new BarChart<>(xAxis, yAxis);
        bar.setLegendVisible(false);
        bar.setPrefSize(500, 350);
        bar.setStyle("-fx-bar-fill: " + StyleHelper.PRIMARY_ORANGE + ";");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ricette");

        if (ricPerGiorno != null && !ricPerGiorno.isEmpty()) {
            ricPerGiorno.forEach((giorno, count) -> {
                XYChart.Data<String, Number> data = new XYChart.Data<>(
                    String.valueOf(giorno.getDayOfMonth()), count
                );
                series.getData().add(data);
            });
        } else {
            series.getData().add(new XYChart.Data<>("N/A", 0));
        }

        bar.getData().add(series);

        // Tooltip personalizzati
        for (XYChart.Data<String, Number> data : series.getData()) {
            Tooltip tooltip = new Tooltip("Giorno " + data.getXValue() + ": " + data.getYValue() + " ricette");
            tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: " + StyleHelper.TEXT_BLACK + "; -fx-text-fill: white;");
            Tooltip.install(data.getNode(), tooltip);
            
            // Colore barra
            data.getNode().setStyle("-fx-bar-fill: " + StyleHelper.PRIMARY_ORANGE + ";");
        }

        card.getChildren().addAll(title, bar);
        return card;
    }

    private VBox creaStatisticheRicetteCard(DatiReportMensile d) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setAlignment(Pos.CENTER);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0.0, 0.0, 4.0);"
        );

        Label title = new Label("📖 Statistiche Ricette");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        title.setTextFill(Color.web(StyleHelper.PRIMARY_ORANGE));

        if (d.getSessioniPratiche() > 0) {
            HBox statsRow = new HBox(15);
            statsRow.setAlignment(Pos.CENTER);
            
            statsRow.getChildren().addAll(
                creaStatMiniCard("📊", "Media", String.format("%.1f", d.getMediaRicette()), StyleHelper.INFO_BLUE),
                creaStatMiniCard("📉", "Minimo", String.valueOf(d.getMinRicette()), StyleHelper.SECONDARY_BEIGE),
                creaStatMiniCard("📈", "Massimo", String.valueOf(d.getMaxRicette()), StyleHelper.SUCCESS_GREEN)
            );
            
            card.getChildren().addAll(title, statsRow);
        } else {
            Label noData = new Label("Nessuna sessione pratica nel periodo");
            noData.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
            noData.setTextFill(Color.web(StyleHelper.TEXT_GRAY));
            card.getChildren().addAll(title, noData);
        }

        return card;
    }

    private VBox creaStatMiniCard(String emoji, String label, String value, String color) {
        VBox miniCard = new VBox(8);
        miniCard.setAlignment(Pos.CENTER);
        miniCard.setPadding(new Insets(15));
        miniCard.setPrefWidth(120);
        miniCard.setStyle(
            "-fx-background-color: " + StyleHelper.BG_LIGHT + ";" +
            "-fx-background-radius: 15;" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 15;"
        );

        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 24px;");

        Label labelText = new Label(label);
        labelText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        labelText.setTextFill(Color.web(StyleHelper.TEXT_GRAY));

        Label valueText = new Label(value);
        valueText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        valueText.setTextFill(Color.web(color));

        miniCard.getChildren().addAll(emojiLabel, labelText, valueText);
        return miniCard;
    }

    private VBox createSummarySection(DatiReportMensile d) {
        VBox summaryCard = new VBox(15);
        summaryCard.setPadding(new Insets(30));
        summaryCard.setAlignment(Pos.CENTER_LEFT);
        summaryCard.setMaxWidth(1000);
        summaryCard.setStyle(
            "-fx-background-color: linear-gradient(to right, #FFE8D6, #FFF4E6);" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: " + StyleHelper.PRIMARY_LIGHT + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0.0, 0.0, 4.0);"
        );

        Label icon = new Label("💡");
        icon.setStyle("-fx-font-size: 32px;");

        Label summaryTitle = new Label("Riepilogo Periodo");
        summaryTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        summaryTitle.setTextFill(Color.web(StyleHelper.TEXT_BLACK));

        Label summaryText = new Label(generaMessaggioRiepilogo(d));
        summaryText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        summaryText.setTextFill(Color.web(StyleHelper.TEXT_BLACK));
        summaryText.setWrapText(true);
        summaryText.setMaxWidth(900);

        summaryCard.getChildren().addAll(icon, summaryTitle, summaryText);
        return summaryCard;
    }

    private String generaMessaggioRiepilogo(DatiReportMensile d) {
        StringBuilder sb = new StringBuilder();
        
        if (d.getNumeroCorsi() == 0) {
            sb.append("Nel periodo selezionato non sono stati tenuti corsi. ");
            sb.append("Considera di pianificare nuove sessioni per aumentare l'attività didattica.");
        } else {
            sb.append("Ottimo lavoro! ");
            sb.append("Nel periodo analizzato hai gestito ");
            sb.append(d.getNumeroCorsi()).append(d.getNumeroCorsi() == 1 ? " corso" : " corsi");
            sb.append(", per un totale di ");
            sb.append(d.getSessioniOnline() + d.getSessioniPratiche()).append(" sessioni. ");
            
            if (d.getSessioniOnline() > 0 && d.getSessioniPratiche() > 0) {
                double percentualeOnline = (d.getSessioniOnline() * 100.0) / (d.getSessioniOnline() + d.getSessioniPratiche());
                sb.append(String.format("La distribuzione è: %.0f%% online e %.0f%% pratiche. ", 
                    percentualeOnline, 100 - percentualeOnline));
            }
            
            if (d.getSessioniPratiche() > 0 && d.getMediaRicette() > 0) {
                sb.append("Nelle sessioni pratiche hai insegnato in media ");
                sb.append(String.format("%.1f", d.getMediaRicette())).append(" ricette, ");
                sb.append("con un minimo di ").append(d.getMinRicette());
                sb.append(" e un massimo di ").append(d.getMaxRicette()).append(" ricette.");
            }
        }
        
        return sb.toString();
    }

    private void animateFadeIn(VBox container) {
        container.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(600), container);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();

        TranslateTransition translate = new TranslateTransition(Duration.millis(600), container);
        translate.setFromY(20);
        translate.setToY(0);
        translate.play();
    }

    public VBox getRoot() {
        return root;
    }
}