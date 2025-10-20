package util;

import model.Frequenza;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.time.temporal.WeekFields;
import java.util.Set;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;

public class FrequenzaHelper {

    public static List<Frequenza> getFrequenzeDisponibili(LocalDate dataInizio, LocalDate dataFine) {
        List<Frequenza> disponibili = new ArrayList<>();

        if (dataInizio == null || dataFine == null) {
            return List.of(Frequenza.unica);
        }

        if (dataInizio.isAfter(dataFine)) {
            return List.of(Frequenza.unica);
        }

        long giorniDurata = ChronoUnit.DAYS.between(dataInizio, dataFine);

        for (Frequenza f : Frequenza.values()) {
            if (giorniDurata >= f.getDurataMinima()) {
                disponibili.add(f);
            }
        }

        return disponibili.isEmpty() ? List.of(Frequenza.unica) : disponibili;
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
            case unica -> 1;

            case giornaliero -> (int) giorniTotali + 1;

            case ogniDueGiorni -> (int) Math.ceil((giorniTotali + 1) / 2.0);

            case settimanale -> (int) Math.ceil((giorniTotali + 1) / 7.0);

            case mensile -> (int) Math.ceil((giorniTotali + 1) / 30.0);
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
            case unica:
                return dateOccupate.isEmpty();

            case giornaliero:
                return !dateOccupate.contains(data);

            case ogniDueGiorni:
                LocalDate riferimento = dateOccupate.stream().min(LocalDate::compareTo).orElse(corsoInizio);
                long giorniDiff = java.time.temporal.ChronoUnit.DAYS.between(riferimento, data);
                return giorniDiff >= 2 && giorniDiff % 2 == 0;

            case settimanale:
                // se non ci sono date occupate, il comportamento è: non prima della data di inizio corso
                if (dateOccupate.isEmpty()) {
                    return !data.isBefore(corsoInizio);
                }

                // troviamo l'ultima data occupata (massima)
                LocalDate ultima = dateOccupate.stream().max(LocalDate::compareTo).orElse(corsoInizio);
                LocalDate primoLunediSuccessivo = ultima.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
                LocalDate fineSettimana = primoLunediSuccessivo.plusDays(6);

                int targetWeek = primoLunediSuccessivo.get(WeekFields.ISO.weekOfWeekBasedYear());
                int targetYear = primoLunediSuccessivo.get(WeekFields.ISO.weekBasedYear());

                int selWeek = data.get(WeekFields.ISO.weekOfWeekBasedYear());
                int selYear = data.get(WeekFields.ISO.weekBasedYear());

                // disponibile solo se la data appartiene ESATTAMENTE alla settimana immediatamente successiva
                return !data.isBefore(primoLunediSuccessivo) && !data.isAfter(fineSettimana);

            case mensile:
                for (LocalDate d : dateOccupate) {
                    if (d.getMonthValue() == data.getMonthValue() && d.getYear() == data.getYear()) {
                        return false;
                    }
                }
                return true;
        }
        return false;
    }

    private FrequenzaHelper() {
        throw new AssertionError("Utility class - non istanziabile");
    }
}