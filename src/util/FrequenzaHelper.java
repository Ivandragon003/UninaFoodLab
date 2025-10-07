package util;

import model.Frequenza;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Set;

public class FrequenzaHelper {

 
    public static List<Frequenza> getFrequenzeDisponibili(LocalDate dataInizio, LocalDate dataFine) {
        List<Frequenza> disponibili = new ArrayList<>();

        if (dataInizio == null || dataFine == null) {
            return List.of(Frequenza.UNICA);
        }

        // Gestisci date inverse o uguali
        if (dataInizio.isAfter(dataFine)) {
            return List.of(Frequenza.UNICA);
        }

        long giorniDurata = ChronoUnit.DAYS.between(dataInizio, dataFine);

        for (Frequenza f : Frequenza.values()) {
            if (giorniDurata >= f.getDurataMinima()) {
                disponibili.add(f);
            }
        }

        return disponibili.isEmpty() ? List.of(Frequenza.UNICA) : disponibili;
    }

  
    public static int calcolaNumeroSessioni(LocalDate dataInizio, LocalDate dataFine, Frequenza frequenza) {
        if (dataInizio == null || dataFine == null) {
            throw new IllegalArgumentException("Date non possono essere null");
        }

        if (dataInizio.isAfter(dataFine)) {
            throw new IllegalArgumentException("Data inizio deve precedere data fine");
        }

        if (frequenza == null) {
            throw new IllegalArgumentException("Frequenza non specificata");
        }

        long giorniTotali = ChronoUnit.DAYS.between(dataInizio, dataFine);

        return switch (frequenza) {
            case UNICA -> 1;
            
            case GIORNALIERO -> (int) giorniTotali + 1;
            
            case OGNI_DUE_GIORNI -> (int) Math.ceil((giorniTotali + 1) / 2.0);
            
            case SETTIMANALE -> (int) Math.ceil((giorniTotali + 1) / 7.0);
            
            case MENSILE -> (int) Math.ceil((giorniTotali + 1) / 30.0);
        };
    }

  
    public static boolean isFrequenzaValida(LocalDate dataInizio, LocalDate dataFine, Frequenza frequenza) {
        if (dataInizio == null || dataFine == null || frequenza == null) {
            return false;
        }

        if (dataInizio.isAfter(dataFine)) {
            return false;
        }

        long giorniDurata = ChronoUnit.DAYS.between(dataInizio, dataFine);
        return giorniDurata >= frequenza.getDurataMinima();
    }

  
    public static String getMessaggioErroreFrequenza(LocalDate dataInizio, LocalDate dataFine, Frequenza frequenza) {
        if (frequenza == null) {
            return "⚠️ Seleziona una frequenza valida";
        }

        if (dataInizio == null || dataFine == null) {
            return "⚠️ Specifica prima le date del corso";
        }

        long giorniDurata = ChronoUnit.DAYS.between(dataInizio, dataFine);
        int durataMinima = frequenza.getDurataMinima();

        return String.format(
            "❌ La frequenza '%s' richiede una durata minima di %d giorni.\n" +
            "📊 Durata attuale: %d giorni.\n" +
            "💡 Aumenta la durata del corso o scegli una frequenza diversa.",
            frequenza.getDescrizione(), durataMinima, giorniDurata
        );
    }  

public static boolean isDataDisponibile(LocalDate data, Frequenza frequenza, Set<LocalDate> dateOccupate, LocalDate corsoInizio) {
    if (frequenza == null || data == null || dateOccupate == null) return false;

    switch (frequenza) {
        case UNICA:
            return dateOccupate.isEmpty();

        case GIORNALIERO:
            return !dateOccupate.contains(data);

        case OGNI_DUE_GIORNI:
            LocalDate riferimento = dateOccupate.stream().min(LocalDate::compareTo).orElse(corsoInizio);
            long giorniDiff = java.time.temporal.ChronoUnit.DAYS.between(riferimento, data);
            return giorniDiff >= 2 && giorniDiff % 2 == 0;

        case SETTIMANALE:
            int settimanaData = data.get(WeekFields.ISO.weekOfWeekBasedYear());
            int annoData = data.getYear();
            for (LocalDate d : dateOccupate) {
                if (d.getYear() == annoData && d.get(WeekFields.ISO.weekOfWeekBasedYear()) == settimanaData) {
                    return false;
                }
            }
            return true;

        case MENSILE:
            int meseData = data.getMonthValue();
            annoData = data.getYear();
            for (LocalDate d : dateOccupate) {
                if (d.getYear() == annoData && d.getMonthValue() == meseData) {
                    return false;
                }
            }
            return true;

        default:
            return true;
    }
}

    private FrequenzaHelper() {
        throw new AssertionError("Utility class - non istanziabile");
    }
}
