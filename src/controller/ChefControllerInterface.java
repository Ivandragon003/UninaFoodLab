package controller;

import java.sql.SQLException;

public interface ChefControllerInterface {
    void aggiornaCredenziali(String nuovoUsername, String nuovaPassword) throws SQLException;
    void eliminaAccount() throws SQLException;
}
