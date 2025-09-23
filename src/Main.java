
import Gui.LoginChefGUI;
import controller.ChefController;
import dao.*;
import service.GestioneChef;
import service.GestioneCorsiCucina;
import javafx.application.Application;
import util.DBConnection;

public class Main {
	public static void main(String[] args) {
		try {

			ChefDAO chefDAO = new ChefDAO();
			TieneDAO tieneDAO = new TieneDAO();
			LavoraDAO lavoraDAO = new LavoraDAO();
			CorsoCucinaDAO corsoDAO = new CorsoCucinaDAO();
			IscrizioneDAO iscrizioneDAO = new IscrizioneDAO();
			OnlineDAO onlineDAO = new OnlineDAO();
			InPresenzaDAO inPresenzaDAO = new InPresenzaDAO();

			GestioneChef chefService = new GestioneChef(chefDAO, tieneDAO, lavoraDAO);
			GestioneCorsiCucina corsiService = new GestioneCorsiCucina(corsoDAO, chefDAO, tieneDAO, iscrizioneDAO,
					onlineDAO, inPresenzaDAO);

			ChefController chefController = new ChefController(chefService);

			LoginChefGUI.setController(chefController, corsiService);

			Application.launch(LoginChefGUI.class, args);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		} finally {

			DBConnection.closeDataSource();
		}
	}
}
