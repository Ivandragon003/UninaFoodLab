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
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.ui.RectangleInsets;
import java.awt.Color;
import java.awt.Font;
import java.awt.BasicStroke;
import java.awt.GradientPaint;
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

		VBox header = createModernHeader();
		VBox controlsCard = createControlsPanel();

		scrollPane = new ScrollPane();
		scrollPane.setFitToWidth(true);
		scrollPane.setStyle("-fx-background-color: transparent;" + "-fx-background: transparent;"
				+ "-fx-border-color: transparent;");

		reportContainer = new VBox(20);
		reportContainer.setPadding(new Insets(30));
		reportContainer.setAlignment(Pos.TOP_CENTER);
		reportContainer.setStyle("-fx-background-color: transparent;");

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

		StyleHelper.applyBackgroundGradient(header);

		DropShadow shadow = new DropShadow();
		shadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.15));
		shadow.setRadius(15);
		shadow.setOffsetY(5);
		header.setEffect(shadow);

		HBox titleBox = new HBox(15);
		titleBox.setAlignment(Pos.CENTER_LEFT);

		Label icon = new Label("📊");
		icon.setStyle("-fx-font-size: 42px;");

		VBox textBox = new VBox(5);
		Label titolo = new Label("Report Periodico");
		titolo.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 34));
		titolo.setTextFill(javafx.scene.paint.Color.WHITE);

		Label sotto = new Label("Chef: " + reportController.getChefLoggato().getUsername());
		sotto.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.NORMAL, 16));
		sotto.setTextFill(javafx.scene.paint.Color.rgb(255, 255, 255, 0.9));

		textBox.getChildren().addAll(titolo, sotto);
		titleBox.getChildren().addAll(icon, textBox);
		header.getChildren().add(titleBox);

		return header;
	}

	private VBox createControlsPanel() {
		VBox card = new VBox(20);
		card.setPadding(new Insets(25, 35, 25, 35));
		card.setStyle("-fx-background-color: white;" + "-fx-background-radius: 0 0 20 20;"
				+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0.0, 0.0, 5.0);");

		Label sectionTitle = new Label("Configurazione Periodo");
		sectionTitle.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.SEMI_BOLD, 18));
		sectionTitle.setTextFill(javafx.scene.paint.Color.web(StyleHelper.TEXT_BLACK));

		HBox controls = new HBox(25);
		controls.setAlignment(Pos.CENTER_LEFT);

		startDatePicker = StyleHelper.createDatePicker();
		startDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
		startDatePicker.setPrefWidth(200);

		endDatePicker = StyleHelper.createDatePicker();
		endDatePicker.setValue(LocalDate.now());
		endDatePicker.setPrefWidth(200);

		Button generaBtn = StyleHelper.createPrimaryButton("Genera Report");
		generaBtn.setPrefWidth(180);
		generaBtn.setPrefHeight(45);
		generaBtn.setOnAction(e -> generaReport());

		controls.getChildren().addAll(creaDatePickerBox("Data Inizio", startDatePicker),
				creaDatePickerBox("Data Fine", endDatePicker), createSpacer(), generaBtn);

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
		label.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.SEMI_BOLD, 13));
		label.setTextFill(javafx.scene.paint.Color.web(StyleHelper.TEXT_GRAY));

		box.getChildren().addAll(label, picker);
		return box;
	}

	private VBox createEmptyState() {
		VBox empty = new VBox(20);
		empty.setAlignment(Pos.CENTER);
		empty.setPadding(new Insets(80));
		empty.setStyle("-fx-background-color: white;" + "-fx-background-radius: 20;" + "-fx-border-color: "
				+ StyleHelper.BORDER_LIGHT + ";" + "-fx-border-width: 2;" + "-fx-border-radius: 20;");

		Label icon = new Label("📊");
		icon.setStyle("-fx-font-size: 64px; -fx-opacity: 0.6;");

		Label msg = new Label("Nessun Report Generato");
		msg.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 20));
		msg.setTextFill(javafx.scene.paint.Color.web(StyleHelper.TEXT_BLACK));

		Label hint = new Label("Seleziona un intervallo di date e premi 'Genera Report'");
		hint.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.NORMAL, 14));
		hint.setTextFill(javafx.scene.paint.Color.web(StyleHelper.TEXT_GRAY));
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
			StyleHelper.showValidationDialog("Validazione",
					"La data di inizio deve essere precedente alla data di fine");
			return;
		}

		try {
			DatiReportMensile dati = reportController.generaReport(inizio, fine);
			Map<LocalDate, Integer> ricPerGiorno = reportController.getRicettePerGiorno(inizio, fine);
			mostraReport(dati, ricPerGiorno);
		} catch (DataAccessException e) {
			StyleHelper.showErrorDialog("Errore Database", "Impossibile generare il report: " + e.getMessage());
		} catch (Exception e) {
			StyleHelper.showErrorDialog("Errore", "Si è verificato un errore imprevisto");
			e.printStackTrace();
		}
	}

	private void mostraReport(DatiReportMensile d, Map<LocalDate, Integer> ricPerGiorno) {
		reportContainer.getChildren().clear();

		VBox reportHeader = createReportHeader(d);
		HBox tilesBox = createStatisticsTiles(d);
		HBox chartsBox = createChartsSection(d, ricPerGiorno);
		VBox summaryBox = createSummarySection(d);

		reportContainer.getChildren().addAll(reportHeader, tilesBox, chartsBox, summaryBox);

		animateFadeIn(reportContainer);
	}

	private VBox createReportHeader(DatiReportMensile d) {
		VBox header = new VBox(12);
		header.setAlignment(Pos.CENTER);
		header.setPadding(new Insets(25));
		header.setStyle("-fx-background-color: white;" + "-fx-background-radius: 20;"
				+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0.0, 0.0, 4.0);");

		Label icon = new Label("📈");
		icon.setStyle("-fx-font-size: 48px;");

		Label titolo = new Label("Report Generato");
		titolo.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 28));
		titolo.setTextFill(javafx.scene.paint.Color.web(StyleHelper.PRIMARY_ORANGE));

		Label periodo = new Label(d.getInizio().format(DATE_FORMATTER) + " → " + d.getFine().format(DATE_FORMATTER));
		periodo.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.NORMAL, 16));
		periodo.setTextFill(javafx.scene.paint.Color.web(StyleHelper.TEXT_GRAY));

		header.getChildren().addAll(icon, titolo, periodo);
		return header;
	}

	private HBox createStatisticsTiles(DatiReportMensile d) {
		HBox tiles = new HBox(20);
		tiles.setAlignment(Pos.CENTER);
		tiles.setPadding(new Insets(10, 0, 10, 0));

		tiles.getChildren()
				.addAll(creaTileModerna("Corsi Totali", String.valueOf(d.getNumeroCorsi()), StyleHelper.PRIMARY_ORANGE,
						"#FFE8D6"),
						creaTileModerna("Sessioni Online", String.valueOf(d.getSessioniOnline()), StyleHelper.INFO_BLUE,
								"#E6F9F8"),
						creaTileModerna("Sessioni Pratiche", String.valueOf(d.getSessioniPratiche()),
								StyleHelper.SUCCESS_GREEN, "#E8FFF5"));

		return tiles;
	}

	private VBox creaTileModerna(String titolo, String valore, String colorAccent, String bgColor) {
		VBox tile = new VBox(12);
		tile.setAlignment(Pos.CENTER);
		tile.setPadding(new Insets(25, 35, 25, 35));
		tile.setPrefWidth(220);
		tile.setStyle("-fx-background-color: " + bgColor + ";" + "-fx-background-radius: 20;" + "-fx-border-color: "
				+ colorAccent + ";" + "-fx-border-width: 2;" + "-fx-border-radius: 20;"
				+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0.0, 0.0, 4.0);");

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

		Label titleLabel = new Label(titolo);
		titleLabel.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.MEDIUM, 13));
		titleLabel.setTextFill(javafx.scene.paint.Color.web(StyleHelper.TEXT_GRAY));
		titleLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

		Label valueLabel = new Label(valore);
		valueLabel.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 48));
		valueLabel.setTextFill(javafx.scene.paint.Color.web(colorAccent));

		tile.getChildren().addAll(titleLabel, valueLabel);
		return tile;
	}

	private HBox createChartsSection(DatiReportMensile d, Map<LocalDate, Integer> ricPerGiorno) {
		HBox chartsBox = new HBox(25);
		chartsBox.setAlignment(Pos.CENTER);
		chartsBox.setPadding(new Insets(10, 0, 10, 0));

		VBox leftColumn = new VBox(20);
		leftColumn.setAlignment(Pos.TOP_CENTER);
		leftColumn.setPrefWidth(450);

		VBox pieCard = createPieChartCard(d);
		VBox statsCard = creaStatisticheRicetteCard(d);

		leftColumn.getChildren().addAll(pieCard, statsCard);

		VBox barCard = createBarChartCard(ricPerGiorno);
		barCard.setPrefWidth(600);

		chartsBox.getChildren().addAll(leftColumn, barCard);
		return chartsBox;
	}

	private VBox createPieChartCard(DatiReportMensile d) {
		VBox card = new VBox(15);
		card.setPadding(new Insets(25));
		card.setAlignment(Pos.TOP_CENTER);
		card.setStyle("-fx-background-color: white;" + "-fx-background-radius: 20;"
				+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0.0, 0.0, 4.0);");

		Label title = new Label("Distribuzione Sessioni");
		title.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 16));
		title.setTextFill(javafx.scene.paint.Color.web(StyleHelper.TEXT_BLACK));

		DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
		dataset.setValue("Online", d.getSessioniOnline());
		dataset.setValue("In Presenza", d.getSessioniPratiche());

		JFreeChart pieChart = ChartFactory.createPieChart3D(null, dataset, true, true, false);

		pieChart.setBackgroundPaint(null);
		pieChart.setBorderVisible(false);
		pieChart.setAntiAlias(true);

		org.jfree.chart.plot.PiePlot3D plot = (org.jfree.chart.plot.PiePlot3D) pieChart.getPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setOutlineVisible(false);
		plot.setShadowPaint(null);
		plot.setDepthFactor(0.08);
		plot.setStartAngle(290);
		plot.setCircular(true);
		plot.setLabelGap(0.02);

		plot.setLabelFont(new Font("Segoe UI", Font.BOLD, 13));
		plot.setLabelBackgroundPaint(new Color(255, 255, 255, 200));
		plot.setLabelOutlinePaint(null);
		plot.setLabelShadowPaint(null);

		plot.setSectionPaint("Online", new Color(33, 150, 243));
		plot.setSectionPaint("In Presenza", new Color(76, 175, 80));

		plot.setSectionOutlinesVisible(true);
		plot.setSectionOutlinePaint("Online", Color.WHITE);
		plot.setSectionOutlinePaint("In Presenza", Color.WHITE);
		plot.setSectionOutlineStroke("Online", new BasicStroke(3.0f));
		plot.setSectionOutlineStroke("In Presenza", new BasicStroke(3.0f));

		org.jfree.chart.title.LegendTitle legend = pieChart.getLegend();
		legend.setFrame(org.jfree.chart.block.BlockBorder.NONE);
		legend.setBackgroundPaint(Color.WHITE);
		legend.setItemFont(new Font("Segoe UI", Font.PLAIN, 13));
		legend.setPosition(org.jfree.chart.ui.RectangleEdge.BOTTOM);
		legend.setPadding(10, 10, 5, 10);

		ChartViewer chartViewer = new ChartViewer(pieChart);
		chartViewer.setPrefSize(400, 350);

		chartViewer.getCanvas().setOnScroll(scrollEvent -> {
			scrollPane.fireEvent(new javafx.scene.input.ScrollEvent(javafx.scene.input.ScrollEvent.SCROLL,
					scrollEvent.getX(), scrollEvent.getY(), scrollEvent.getScreenX(), scrollEvent.getScreenY(),
					scrollEvent.isShiftDown(), scrollEvent.isControlDown(), scrollEvent.isAltDown(),
					scrollEvent.isMetaDown(), scrollEvent.isDirect(), scrollEvent.isInertia(), scrollEvent.getDeltaX(),
					scrollEvent.getDeltaY(), scrollEvent.getTotalDeltaX(), scrollEvent.getTotalDeltaY(),
					scrollEvent.getMultiplierX(), scrollEvent.getMultiplierY(), scrollEvent.getTextDeltaXUnits(),
					scrollEvent.getTextDeltaX(), scrollEvent.getTextDeltaYUnits(), scrollEvent.getTextDeltaY(),
					scrollEvent.getTouchCount(), scrollEvent.getPickResult()));
		});

		card.getChildren().addAll(title, chartViewer);
		return card;
	}

	private VBox createBarChartCard(Map<LocalDate, Integer> ricPerGiorno) {
		VBox card = new VBox(15);
		card.setPadding(new Insets(25));
		card.setAlignment(Pos.TOP_CENTER);
		card.setStyle("-fx-background-color: white;" + "-fx-background-radius: 20;"
				+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0.0, 0.0, 4.0);");

		Label title = new Label("📅 Ricette Preparate nel Periodo"); // ✅ TITOLO MIGLIORATO
		title.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 16));
		title.setTextFill(javafx.scene.paint.Color.web(StyleHelper.TEXT_BLACK));

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		if (ricPerGiorno != null && !ricPerGiorno.isEmpty()) {
			// ✅ DETERMINA SE IL REPORT COPRE PIÙ ANNI
			boolean multiAnno = ricPerGiorno.keySet().stream().map(LocalDate::getYear).distinct().count() > 1;

			// ✅ ORDINA LE DATE
			ricPerGiorno.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
				LocalDate giorno = entry.getKey();
				Integer count = entry.getValue();

				// ✅ FORMATO DATA INTELLIGENTE
				String etichetta;
				if (multiAnno) {
					// Se copre più anni: "27/09/24"
					etichetta = String.format("%02d/%02d/%02d", giorno.getDayOfMonth(), giorno.getMonthValue(),
							giorno.getYear() % 100); // Solo ultime 2 cifre anno
				} else {
					// Se stesso anno: "27 Set" o "16 Gen"
					String meseAbbr = getMeseAbbreviato(giorno.getMonthValue());
					etichetta = String.format("%02d %s", giorno.getDayOfMonth(), meseAbbr);
				}

				dataset.addValue(count, "Ricette", etichetta);
			});
		} else {
			dataset.addValue(0, "Ricette", "N/A");
		}

		JFreeChart barChart = ChartFactory.createBarChart(null, "Data", // ✅ ETICHETTA GENERICA
				"Numero Ricette", dataset, PlotOrientation.VERTICAL, false, true, false);

		barChart.setBackgroundPaint(null);
		barChart.setBorderVisible(false);
		barChart.setAntiAlias(true);
		barChart.setPadding(new RectangleInsets(15, 15, 15, 15));

		CategoryPlot plot = (CategoryPlot) barChart.getPlot();
		plot.setBackgroundPaint(new Color(252, 252, 252));
		plot.setOutlineVisible(false);
		plot.setRangeGridlinePaint(new Color(230, 230, 230));
		plot.setRangeGridlineStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		plot.setDomainGridlinesVisible(false);
		plot.setAxisOffset(new RectangleInsets(10, 10, 10, 10));

		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		GradientPaint gradient = new GradientPaint(0.0f, 0.0f, new Color(255, 138, 101), 0.0f, 300.0f,
				new Color(255, 87, 34));
		renderer.setSeriesPaint(0, gradient);
		renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
		renderer.setDrawBarOutline(true);
		renderer.setSeriesOutlinePaint(0, new Color(230, 230, 230));
		renderer.setSeriesOutlineStroke(0, new BasicStroke(1.5f));
		renderer.setShadowVisible(true);
		renderer.setShadowPaint(new Color(0, 0, 0, 40));
		renderer.setShadowXOffset(4.0);
		renderer.setShadowYOffset(4.0);
		renderer.setItemMargin(0.15);
		renderer.setMaximumBarWidth(0.08);

		// ✅ TOOLTIP CON DATA COMPLETA
		renderer.setDefaultToolTipGenerator(new org.jfree.chart.labels.StandardCategoryToolTipGenerator(
				"{1}: {2} ricette", java.text.NumberFormat.getInstance()));

		org.jfree.chart.axis.CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setLabel("📅 Data");
		domainAxis.setLabelFont(new Font("Segoe UI", Font.BOLD, 15));
		domainAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11)); // ✅ Font più piccolo per date lunghe
		domainAxis.setTickLabelPaint(new Color(100, 100, 100));
		domainAxis.setAxisLinePaint(new Color(200, 200, 200));
		domainAxis.setTickMarkPaint(new Color(200, 200, 200));
		domainAxis.setLowerMargin(0.02);
		domainAxis.setUpperMargin(0.02);
		domainAxis.setCategoryMargin(0.25);

		// ✅ ROTAZIONE ETICHETTE SE TROPPE DATE
		if (ricPerGiorno != null && ricPerGiorno.size() > 10) {
			domainAxis.setCategoryLabelPositions(org.jfree.chart.axis.CategoryLabelPositions.UP_45 // Ruota 45° se
																									// troppe barre
			);
		}

		org.jfree.chart.axis.NumberAxis rangeAxis = (org.jfree.chart.axis.NumberAxis) plot.getRangeAxis();
		rangeAxis.setLabel("📊 Numero Ricette");
		rangeAxis.setLabelFont(new Font("Segoe UI", Font.BOLD, 15));
		rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
		rangeAxis.setTickLabelPaint(new Color(100, 100, 100));
		rangeAxis.setAxisLinePaint(new Color(200, 200, 200));
		rangeAxis.setTickMarkPaint(new Color(200, 200, 200));
		rangeAxis.setStandardTickUnits(org.jfree.chart.axis.NumberAxis.createIntegerTickUnits());
		rangeAxis.setLowerMargin(0.10);
		rangeAxis.setUpperMargin(0.10);

		ChartViewer chartViewer = new ChartViewer(barChart);
		chartViewer.setPrefSize(650, 420);

		chartViewer.getCanvas().setOnScroll(scrollEvent -> {
			scrollPane.fireEvent(new javafx.scene.input.ScrollEvent(javafx.scene.input.ScrollEvent.SCROLL,
					scrollEvent.getX(), scrollEvent.getY(), scrollEvent.getScreenX(), scrollEvent.getScreenY(),
					scrollEvent.isShiftDown(), scrollEvent.isControlDown(), scrollEvent.isAltDown(),
					scrollEvent.isMetaDown(), scrollEvent.isDirect(), scrollEvent.isInertia(), scrollEvent.getDeltaX(),
					scrollEvent.getDeltaY(), scrollEvent.getTotalDeltaX(), scrollEvent.getTotalDeltaY(),
					scrollEvent.getMultiplierX(), scrollEvent.getMultiplierY(), scrollEvent.getTextDeltaXUnits(),
					scrollEvent.getTextDeltaX(), scrollEvent.getTextDeltaYUnits(), scrollEvent.getTextDeltaY(),
					scrollEvent.getTouchCount(), scrollEvent.getPickResult()));
		});

		card.getChildren().addAll(title, chartViewer);
		return card;
	}


	private String getMeseAbbreviato(int mese) {
		switch (mese) {
		case 1:
			return "Gen";
		case 2:
			return "Feb";
		case 3:
			return "Mar";
		case 4:
			return "Apr";
		case 5:
			return "Mag";
		case 6:
			return "Giu";
		case 7:
			return "Lug";
		case 8:
			return "Ago";
		case 9:
			return "Set";
		case 10:
			return "Ott";
		case 11:
			return "Nov";
		case 12:
			return "Dic";
		default:
			return "";
		}
	}

	private VBox creaStatisticheRicetteCard(DatiReportMensile d) {
		VBox card = new VBox(15);
		card.setPadding(new Insets(25));
		card.setAlignment(Pos.CENTER);
		card.setStyle("-fx-background-color: white;" + "-fx-background-radius: 20;"
				+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0.0, 0.0, 4.0);");

		Label title = new Label("Statistiche Ricette");
		title.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 16));
		title.setTextFill(javafx.scene.paint.Color.web(StyleHelper.PRIMARY_ORANGE));

		Label sottotitolo = new Label("(Solo Sessioni in Presenza)");
		sottotitolo.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.NORMAL, 12));
		sottotitolo.setTextFill(javafx.scene.paint.Color.web(StyleHelper.TEXT_GRAY));

		VBox titleBox = new VBox(3);
		titleBox.getChildren().addAll(title, sottotitolo);

		if (d.getSessioniPratiche() > 0) {
			HBox statsRow = new HBox(15);
			statsRow.setAlignment(Pos.CENTER);

			statsRow.getChildren().addAll(
					creaStatMiniCard("Media", String.format("%.1f", d.getMediaRicette()), StyleHelper.INFO_BLUE),
					creaStatMiniCard("Minimo", String.valueOf(d.getMinRicette()), StyleHelper.SECONDARY_BEIGE),
					creaStatMiniCard("Massimo", String.valueOf(d.getMaxRicette()), StyleHelper.SUCCESS_GREEN));

			card.getChildren().addAll(titleBox, statsRow);
		} else {
			Label noData = new Label("Nessuna sessione pratica nel periodo");
			noData.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.NORMAL, 13));
			noData.setTextFill(javafx.scene.paint.Color.web(StyleHelper.TEXT_GRAY));
			card.getChildren().addAll(titleBox, noData);
		}

		return card;
	}

	private VBox creaStatMiniCard(String label, String value, String color) {
		VBox miniCard = new VBox(8);
		miniCard.setAlignment(Pos.CENTER);
		miniCard.setPadding(new Insets(15));
		miniCard.setPrefWidth(120);
		miniCard.setStyle("-fx-background-color: " + StyleHelper.BG_LIGHT + ";" + "-fx-background-radius: 15;"
				+ "-fx-border-color: " + color + ";" + "-fx-border-width: 2;" + "-fx-border-radius: 15;");

		Label labelText = new Label(label);
		labelText.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.NORMAL, 11));
		labelText.setTextFill(javafx.scene.paint.Color.web(StyleHelper.TEXT_GRAY));

		Label valueText = new Label(value);
		valueText.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 28));
		valueText.setTextFill(javafx.scene.paint.Color.web(color));

		miniCard.getChildren().addAll(labelText, valueText);
		return miniCard;
	}

	private VBox createSummarySection(DatiReportMensile d) {
		VBox summaryCard = new VBox(15);
		summaryCard.setPadding(new Insets(30));
		summaryCard.setAlignment(Pos.CENTER_LEFT);
		summaryCard.setMaxWidth(1000);
		summaryCard.setStyle("-fx-background-color: linear-gradient(to right, #FFE8D6, #FFF4E6);"
				+ "-fx-background-radius: 20;" + "-fx-border-color: " + StyleHelper.PRIMARY_LIGHT + ";"
				+ "-fx-border-width: 2;" + "-fx-border-radius: 20;"
				+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0.0, 0.0, 4.0);");

		Label summaryTitle = new Label("Riepilogo Periodo");
		summaryTitle.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 18));
		summaryTitle.setTextFill(javafx.scene.paint.Color.web(StyleHelper.TEXT_BLACK));

		Label summaryText = new Label(generaMessaggioRiepilogo(d));
		summaryText.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.NORMAL, 14));
		summaryText.setTextFill(javafx.scene.paint.Color.web(StyleHelper.TEXT_BLACK));
		summaryText.setWrapText(true);
		summaryText.setMaxWidth(900);

		summaryCard.getChildren().addAll(summaryTitle, summaryText);
		return summaryCard;
	}

	private String generaMessaggioRiepilogo(DatiReportMensile d) {
		StringBuilder sb = new StringBuilder();

		if (d.getNumeroCorsi() == 0) {
			sb.append("Nel periodo selezionato non sono stati tenuti corsi. ");
			sb.append("Considera di pianificare nuove sessioni per aumentare l'attività didattica.");
		} else {
			sb.append("Nel periodo analizzato hai gestito ");
			sb.append(d.getNumeroCorsi()).append(d.getNumeroCorsi() == 1 ? " corso" : " corsi");
			sb.append(", per un totale di ");
			sb.append(d.getSessioniOnline() + d.getSessioniPratiche()).append(" sessioni. ");

			if (d.getSessioniOnline() > 0 && d.getSessioniPratiche() > 0) {
				double percentualeOnline = (d.getSessioniOnline() * 100.0)
						/ (d.getSessioniOnline() + d.getSessioniPratiche());
				sb.append(String.format("La distribuzione è: %.0f%% online e %.0f%% pratiche. ", percentualeOnline,
						100 - percentualeOnline));
			}

			if (d.getSessioniPratiche() > 0 && d.getMediaRicette() > 0) {
				sb.append("Nelle sessioni pratiche hai insegnato in media ");
				sb.append(String.format("%.1f", d.getMediaRicette())).append(" ricette, ");
				sb.append("con un minimo di ").append(d.getMinRicette());
				sb.append(" e un massimo di ").append(d.getMaxRicette()).append(" ricette. ");
				sb.append("(Le sessioni online non sono conteggiate nel calcolo della media).");
			} else if (d.getSessioniOnline() > 0) {
				sb.append("Tutte le sessioni del periodo sono state online, quindi non ci sono dati sulle ricette.");
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
