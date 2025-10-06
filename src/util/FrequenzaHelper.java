package util;

import model.Frequenza;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class FrequenzaHelper {

    public static List<Frequenza> getFrequenzeDisponibili(LocalDate dataInizio, LocalDate dataFine) {
        List<Frequenza> disponibili = new ArrayList<>();
        
        if (dataInizio == null || dataFine == null) {
            return disponibili;
        }
        
        long giorniDurata = ChronoUnit.DAYS.between(dataInizio, dataFine);
        
        for (Frequenza f : Frequenza.values()) {
            if (giorniDurata >= f.getDurataMinima()) {
                disponibili.add(f);
            }
        }
        
        return disponibili;
    }

    
    public static int calcolaNumeroSessioni(LocalDate dataInizio, LocalDate dataFine, Frequenza frequenza) {
        if (dataInizio == null || dataFine == null || dataInizio.isAfter(dataFine)) {
            throw new IllegalArgumentException("Date non valide");
        }
        
        if (frequenza == null) {
            throw new IllegalArgumentException("Frequenza non specificata");
        }
        
        long giorniTotali = ChronoUnit.DAYS.between(dataInizio, dataFine);
        
        return switch (frequenza) {
            case unica -> 1;
            case giornaliero -> (int) giorniTotali + 1;
            case ogniDueGiorni -> (int) (giorniTotali / 2) + 1;
            case settimanale -> (int) (giorniTotali / 7) + 1;
            case mensile -> (int) (giorniTotali / 30) + 1;
        };
    }


    public static boolean isFrequenzaValida(LocalDate dataInizio, LocalDate dataFine, Frequenza frequenza) {
        if (dataInizio == null || dataFine == null || frequenza == null) {
            return false;
        }
        
        long giorniDurata = ChronoUnit.DAYS.between(dataInizio, dataFine);
        return giorniDurata >= frequenza.getDurataMinima();
    }

  
    public static String getMessaggioErroreFrequenza(LocalDate dataInizio, LocalDate dataFine, Frequenza frequenza) {
        long giorniDurata = ChronoUnit.DAYS.between(dataInizio, dataFine);
        int durataMinima = frequenza.getDurataMinima();
        
        return String.format(
            "❌ La frequenza '%s' richiede una durata minima di %d giorni.\n" +
            "📊 Durata attuale: %d giorni.\n" +
            "💡 Aumenta la durata del corso o scegli una frequenza diversa.",
            frequenza.getDescrizione(),
            durataMinima,
            giorniDurata
        );
    }
}
