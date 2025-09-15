import dao.InPresenzaDAO;
import dao.OnlineDAO;
import model.InPresenza;
import model.Online;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Main {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static void main(String[] args) {
        OnlineDAO onlineDAO = new OnlineDAO();
        InPresenzaDAO inPresenzaDAO = new InPresenzaDAO();

        List<Integer> idOnlineCreate = new ArrayList<>();
        List<Integer> idInPresenzaCreate = new ArrayList<>();

        try {
            System.out.println("=== TEST SESSIONI DAO ===\n");

            LocalDateTime ora = LocalDateTime.now();

            // --- CREAZIONE SESSIONI ONLINE ---
            Online online1 = new Online(ora.plusDays(10), ora.plusDays(10).plusHours(2), "Zoom");
            Online online2 = new Online(ora.plusDays(20), ora.plusDays(20).plusHours(3), "Teams");

            System.out.println("1. Salvataggio sessioni online");
            int idOnline1 = onlineDAO.save(online1); // senza idCorso
            int idOnline2 = onlineDAO.save(online2); // senza idCorso
            online1.setIdSessione(idOnline1); // settiamo ID nell'oggetto
            online2.setIdSessione(idOnline2);
            idOnlineCreate.add(idOnline1);
            idOnlineCreate.add(idOnline2);

            System.out.println("ID Online salvate: " + idOnlineCreate);

            // --- CREAZIONE SESSIONI IN PRESENZA ---
            InPresenza inPres1 = new InPresenza(ora.plusDays(5), ora.plusDays(5).plusHours(3), "Via Roma 1", "Napoli", 20, 80100);
            InPresenza inPres2 = new InPresenza(ora.plusDays(15), ora.plusDays(15).plusHours(2), "Via Milano 10", "Napoli", 15, 80121);

            System.out.println("2. Salvataggio sessioni in presenza");
            int idInPres1 = inPresenzaDAO.save(inPres1); // senza idCorso
            int idInPres2 = inPresenzaDAO.save(inPres2); // senza idCorso
            inPres1.setIdSessione(idInPres1);
            inPres2.setIdSessione(idInPres2);
            idInPresenzaCreate.add(idInPres1);
            idInPresenzaCreate.add(idInPres2);

            System.out.println("ID InPresenza salvate: " + idInPresenzaCreate);

            // --- LETTURA TUTTE LE SESSIONI ---
            System.out.println("\n3. Recupero tutte le sessioni online");
            for (Online o : onlineDAO.getAll()) {
                System.out.println("- Online piattaforma: " + o.getPiattaformaStreaming() +
                        ", inizio: " + o.getDataInizioSessione().format(FORMATTER));
            }

            System.out.println("\n4. Recupero tutte le sessioni in presenza");
            for (InPresenza i : inPresenzaDAO.getAll()) {
                System.out.println("- InPresenza: " + i.getVia() + ", " + i.getCitta() +
                        ", posti: " + i.getNumeroPosti() +
                        ", inizio: " + i.getDataInizioSessione().format(FORMATTER));
            }

            // --- UPDATE DI UNA SESSIONE ---
            System.out.println("\n5. Update sessione online");
            online1.setPiattaformaStreaming("Google Meet");
            onlineDAO.update(idOnline1, online1); // passa ID separato
            Optional<Online> onlineAggiornata = onlineDAO.findById(idOnline1);
            onlineAggiornata.ifPresent(o -> System.out.println("Piattaforma aggiornata: " + o.getPiattaformaStreaming()));

            System.out.println("\n6. Update sessione in presenza");
            inPres1.setNumeroPosti(25);
            inPresenzaDAO.update(idInPres1, inPres1); // Passa l'ID al DAO
            Optional<InPresenza> inPresAggiornata = inPresenzaDAO.findById(idInPres1);
            inPresAggiornata.ifPresent(i -> System.out.println("Numero posti aggiornato: " + i.getNumeroPosti()));

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // --- PULIZIA ---
            System.out.println("\n7. Eliminazione sessioni appena create");

            for (int id : idOnlineCreate) {
                try {
                    onlineDAO.delete(id);
                    System.out.println("Online ID " + id + " eliminata");
                } catch (SQLException e) {
                    System.err.println("Errore eliminazione Online ID " + id + ": " + e.getMessage());
                }
            }

            for (int id : idInPresenzaCreate) {
                try {
                    inPresenzaDAO.delete(id);
                    System.out.println("InPresenza ID " + id + " eliminata");
                } catch (SQLException e) {
                    System.err.println("Errore eliminazione InPresenza ID " + id + ": " + e.getMessage());
                }
            }
        }

        System.out.println("\n=== TEST COMPLETATI ===");
    }
}
