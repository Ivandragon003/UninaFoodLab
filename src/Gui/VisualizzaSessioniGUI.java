package Gui;

import controller.GestioneSessioniController;
import controller.RicettaController;
import controller.IngredienteController;
import model.*;
import guihelper.StyleHelper;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

public class VisualizzaSessioniGUI {

    private CorsoCucina corso;
    private GestioneSessioniController controller;
    private RicettaController ricettaController;
    private IngredienteController ingredienteController;
    
    private ListView<Sessione> sessioniList;
    private ComboBox<String> filtroTipo;
    private TextField filtroRicette;
    private Label numeroSessioniLabel;

    private VBox root;
    
    // ✅ Riferimento al contentPane per navigazione
    private StackPane contentPane;
    
    // ✅ Callback per tornare alla schermata precedente
    private Runnable onChiudiCallback;

    public void setController(GestioneSessioniController controller) {
        this.controller = controller;
    }

    public void setRicettaController(RicettaController ricettaController) {
        this.ricettaController = ricettaController;
    }

    public void setIngredienteController(IngredienteController ingredienteController) {
        this.ingredienteController = ingredienteController;
    }

    public void setCorso(CorsoCucina corso) {
        this.corso = corso;
    }
    
    // ✅ Setter per contentPane
    public void setContentPane(StackPane contentPane) {
        this.contentPane = contentPane;
    }
    
    // ✅ Setter per il callback
    public void setOnChiudiCallback(Runnable callback) {
        this.onChiudiCallback = callback;
    }

    public VBox getRoot() {
        if (corso == null) {
            throw new IllegalStateException("Corso non impostato!");
        }

        root = new VBox(20);
        root.setPadding(new Insets(30));
        
        StyleHelper.applyBackgroundGradient(root);

        Label titolo = StyleHelper.createTitleLabel("Sessioni del Corso: " + corso.getNomeCorso());
        titolo.setTextFill(Color.WHITE);

        VBox sezioneSessioni = StyleHelper.createSection();
        
        HBox filtriBox = createFiltriBox();
        numeroSessioniLabel = StyleHelper.createLabel("");
        
        sessioniList = new ListView<>();
        sessioniList.setPrefHeight(400);
        setupSessioniListConElimina();
        VBox.setVgrow(sessioniList, Priority.ALWAYS);
        
        sezioneSessioni.getChildren().addAll(filtriBox, numeroSessioniLabel, sessioniList);

        HBox pulsantiBox = createPulsantiBox();

        root.getChildren().addAll(titolo, sezioneSessioni, pulsantiBox);
        aggiornaLista();

        return root;
    }

    private HBox createFiltriBox() {
        HBox filtriBox = new HBox(12);
        filtriBox.setAlignment(Pos.CENTER_LEFT);
        filtriBox.setPadding(new Insets(15));
        filtriBox.setStyle(
            "-fx-background-color: " + StyleHelper.BG_WHITE + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + StyleHelper.BORDER_LIGHT + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 4, 0.0, 0.0, 2.0);"
        );

        Label filtroLabel = StyleHelper.createLabel("Filtri:");
        filtroLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        filtroTipo = StyleHelper.createComboBox();
        filtroTipo.getItems().addAll("Tutti", "Online", "In Presenza");
        filtroTipo.setValue("Tutti");
        filtroTipo.setPrefWidth(140);
        filtroTipo.setOnAction(e -> applicaFiltri());

        Label ricetteLabel = StyleHelper.createLabel("Min. Ricette:");
        filtroRicette = StyleHelper.createTextField("0");
        filtroRicette.setPrefWidth(70);
        filtroRicette.setOnKeyReleased(e -> applicaFiltri());

        Button resetBtn = StyleHelper.createInfoButton("Reset");
        resetBtn.setPrefWidth(100);
        resetBtn.setOnAction(e -> {
            filtroTipo.setValue("Tutti");
            filtroRicette.clear();
            applicaFiltri();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Separator sep = new Separator();
        sep.setStyle("-fx-padding: 0 10;");
        
        filtriBox.getChildren().addAll(
            filtroLabel, filtroTipo,
            sep,
            ricetteLabel, filtroRicette,
            spacer, resetBtn
        );

        return filtriBox;
    }

    private void setupSessioniListConElimina() {
        sessioniList.setCellFactory(lv -> new ListCell<Sessione>() {
            private final HBox container = new HBox(15);
            private final VBox infoBox = new VBox(6);
            private final Button eliminaBtn = StyleHelper.createDangerButton("Elimina");

            {
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                eliminaBtn.setPrefWidth(110);
                eliminaBtn.setOnAction(e -> {
                    Sessione s = getItem();
                    if (s != null) {
                        confermaEliminaSessione(s);
                    }
                });

                container.setAlignment(Pos.CENTER_LEFT);
                container.setPadding(new Insets(12));
                container.setStyle(
                    "-fx-background-color: " + StyleHelper.BG_WHITE + ";" +
                    "-fx-background-radius: 10;"
                );
            }

            @Override
            protected void updateItem(Sessione s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    infoBox.getChildren().clear();
                    
                    String tipo = (s instanceof Online) ? "Online" : "In Presenza";
                    String inizio = s.getDataInizioSessione().toLocalDate() + " ore " + 
                                   s.getDataInizioSessione().toLocalTime().toString();
                    
                    Label tipoLabel = new Label(tipo);
                    tipoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: " + StyleHelper.TEXT_BLACK + ";");
                    
                    Label inizioLabel = StyleHelper.createLabel("Inizio: " + inizio);
                    
                    infoBox.getChildren().addAll(tipoLabel, inizioLabel);
                    
                    if (s instanceof Online) {
                        Online online = (Online) s;
                        Label piattaformaLabel = StyleHelper.createCaptionLabel("Piattaforma: " + online.getPiattaformaStreaming());
                        infoBox.getChildren().add(piattaformaLabel);
                    } else if (s instanceof InPresenza) {
                        InPresenza ip = (InPresenza) s;
                        
                        Label luogoLabel = StyleHelper.createCaptionLabel("Luogo: " + ip.getVia() + ", " + ip.getCitta());
                        Label postiLabel = StyleHelper.createCaptionLabel("Posti: " + ip.getNumeroPosti());
                        
                        int ricette = ip.getRicette() != null ? ip.getRicette().size() : 0;
                        Label ricetteLabel = StyleHelper.createCaptionLabel("Ricette: " + ricette);
                        
                        infoBox.getChildren().addAll(luogoLabel, postiLabel, ricetteLabel);
                    }

                    aggiornaStatoBottoneElimina(eliminaBtn);

                    container.getChildren().clear();
                    
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    
                    container.getChildren().addAll(infoBox, spacer, eliminaBtn);
                    
                    setGraphic(container);
                    setText(null);
                    
                    String borderColor = (s instanceof Online) ? StyleHelper.INFO_BLUE : StyleHelper.SUCCESS_GREEN;
                    setStyle(
                        "-fx-background-color: transparent; " +
                        "-fx-border-color: " + borderColor + "; " +
                        "-fx-border-width: 0 0 1 0; " +
                        "-fx-padding: 8 0;"
                    );
                }
            }
        });
    }

    private void aggiornaStatoBottoneElimina(Button eliminaBtn) {
        boolean isUltimaSessione = corso.getSessioni() != null && corso.getSessioni().size() <= 1;
        
        eliminaBtn.setDisable(isUltimaSessione);
        
        if (isUltimaSessione) {
            Tooltip tooltip = new Tooltip("Non puoi eliminare l'unica sessione del corso");
            eliminaBtn.setTooltip(tooltip);
        }
    }

    private void confermaEliminaSessione(Sessione sessione) {
        if (sessione == null) return;
        
        String tipo = (sessione instanceof Online) ? "Online" : "In Presenza";
        String sessioneInfo = "Tipo: " + tipo + "\n" +
                             "Inizio: " + sessione.getDataInizioSessione().toLocalDate();
        
        StyleHelper.showConfirmationDialog(
            "Conferma Eliminazione",
            "Sei sicuro di voler eliminare questa sessione?\n\n" + sessioneInfo,
            () -> {
                try {
                    if (controller != null) {
                        controller.eliminaSessione(sessione);
                    }
                    
                    if (corso.getSessioni() != null) {
                        corso.getSessioni().remove(sessione);
                    }
                    
                    aggiornaLista();
                    StyleHelper.showSuccessDialog("Sessione Eliminata", "Eliminazione completata!");
                    
                } catch (IllegalStateException ex) {
                    StyleHelper.showValidationDialog("Errore", ex.getMessage());
                } catch (Exception ex) {
                    StyleHelper.showErrorDialog("Errore", "Errore durante l'eliminazione");
                }
            }
        );
    }

    private HBox createPulsantiBox() {
        HBox pulsantiBox = new HBox(12);
        pulsantiBox.setAlignment(Pos.CENTER);
        pulsantiBox.setPadding(new Insets(20, 0, 0, 0));

        Button creaBtn = StyleHelper.createSuccessButton("➕ Crea Sessione");
        creaBtn.setPrefWidth(180);
        creaBtn.setOnAction(e -> apriCreaSessioni());

        Button indietroBtn = StyleHelper.createPrimaryButton("⬅️ Indietro");
        indietroBtn.setPrefWidth(150);
        indietroBtn.setOnAction(e -> {
            // ✅ Usa il callback per tornare indietro
            if (onChiudiCallback != null) {
                onChiudiCallback.run();
            } else {
                // Fallback: chiudi lo stage se esiste
                if (root.getScene() != null && root.getScene().getWindow() != null) {
                    root.getScene().getWindow().hide();
                }
            }
        });

        pulsantiBox.getChildren().addAll(creaBtn, indietroBtn);
        return pulsantiBox;
    }
    
    private void apriCreaSessioni() {
        try {
            if (ricettaController == null || ingredienteController == null) {
                StyleHelper.showErrorDialog("Errore", "Controller non inizializzati");
                return;
            }
            
            Set<LocalDate> dateFineSessioni = new HashSet<>();
            LocalDate dataFineUltimaSessione = null;
            
            if (corso.getSessioni() != null && !corso.getSessioni().isEmpty()) {
                for (Sessione s : corso.getSessioni()) {
                    if (s.getDataFineSessione() != null) {
                        LocalDate dataFine = s.getDataFineSessione().toLocalDate();
                        dateFineSessioni.add(dataFine);
                        
                        if (dataFineUltimaSessione == null || dataFine.isAfter(dataFineUltimaSessione)) {
                            dataFineUltimaSessione = dataFine;
                        }
                    }
                }
            }
            
            LocalDate dataFineCorsoAttuale = corso.getDataFineCorso().toLocalDate();
            LocalDate nuovaDataFineCorso = dataFineCorsoAttuale;
            
            if (dataFineUltimaSessione != null) {
                LocalDate prossimaDataPossibile = switch (corso.getFrequenzaCorso()) {
                    case unica -> dataFineUltimaSessione;
                    case giornaliero -> dataFineUltimaSessione.plusDays(1);
                    case ogniDueGiorni -> dataFineUltimaSessione.plusDays(2);
                    case settimanale -> dataFineUltimaSessione.plusWeeks(1);
                    case mensile -> dataFineUltimaSessione.plusMonths(1);
                };
                
                if (dataFineCorsoAttuale.isBefore(prossimaDataPossibile)) {
                    nuovaDataFineCorso = prossimaDataPossibile;
                    corso.setDataFineCorso(nuovaDataFineCorso.atStartOfDay());
                }
            } else {
                nuovaDataFineCorso = corso.getDataInizioCorso().toLocalDate();
            }
            
            CreaSessioniGUI creaGUI = new CreaSessioniGUI(
                corso.getDataInizioCorso().toLocalDate(), 
                nuovaDataFineCorso,
                corso.getFrequenzaCorso(),
                dateFineSessioni,
                ricettaController,
                ingredienteController
            );
            
            Sessione nuovaSessione = creaGUI.showDialog();
            
            if (nuovaSessione != null) {
                List<Ricetta> ricette = null;
                if (nuovaSessione instanceof InPresenza) {
                    InPresenza ip = (InPresenza) nuovaSessione;
                    ricette = ip.getRicette() != null ? 
                             new ArrayList<>(ip.getRicette()) : 
                             new ArrayList<>();
                }
                
                controller.aggiungiSessione(nuovaSessione, ricette);
                aggiornaLista();
                StyleHelper.showSuccessDialog("Successo", "Sessione creata!");
            }
            
        } catch (Exception ex) {
            StyleHelper.showErrorDialog("Errore", ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void applicaFiltri() {
        ObservableList<Sessione> sessioniFiltrate = FXCollections.observableArrayList();
        String tipoFiltro = filtroTipo.getValue();
        String ricetteFiltro = filtroRicette.getText().trim();

        if (corso.getSessioni() == null || corso.getSessioni().isEmpty()) {
            sessioniList.setItems(sessioniFiltrate);
            numeroSessioniLabel.setText("Nessuna sessione disponibile");
            return;
        }

        for (Sessione s : corso.getSessioni()) {
            boolean passaTipoFiltro = tipoFiltro.equals("Tutti") ||
                    (tipoFiltro.equals("Online") && s instanceof Online) ||
                    (tipoFiltro.equals("In Presenza") && s instanceof InPresenza);

            boolean passaRicetteFiltro = true;
            if (!ricetteFiltro.isEmpty()) {
                try {
                    int minRicette = Integer.parseInt(ricetteFiltro);
                    int ricetteSessione = (s instanceof InPresenza) ? 
                            (((InPresenza) s).getRicette() != null ? ((InPresenza) s).getRicette().size() : 0) : 0;
                    passaRicetteFiltro = ricetteSessione >= minRicette;
                } catch (NumberFormatException e) {
                    passaRicetteFiltro = true;
                }
            }

            if (passaTipoFiltro && passaRicetteFiltro) {
                sessioniFiltrate.add(s);
            }
        }

        sessioniFiltrate.sort(Comparator.comparing(Sessione::getDataInizioSessione));
        sessioniList.setItems(sessioniFiltrate);
        numeroSessioniLabel.setText(String.format("Sessioni visualizzate: %d di %d", 
                sessioniFiltrate.size(), corso.getSessioni().size()));
    }

    public void aggiornaLista() {
        applicaFiltri();
    }
}
