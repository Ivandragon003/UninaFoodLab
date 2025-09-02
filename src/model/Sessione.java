package model;

import java.time.LocalDateTime;

public class Sessione {

private int idSessione;
private LocalDateTime dataInizioSessione;
private LocalDateTime dataFineSessione;

//VA CREATA LA ENUM TIPO



public Sessione(LocalDateTime dataInizioSessione, LocalDateTime dataFineSessione) {
	super();
	this.dataInizioSessione = dataInizioSessione;
	this.dataFineSessione = dataFineSessione;
}

public LocalDateTime getDataInizioSessione() {
	return dataInizioSessione;
}

public void setDataInizioSessione(LocalDateTime dataInizioSessione) {
	this.dataInizioSessione = dataInizioSessione;
}

public LocalDateTime getDataFineSessione() {
	return dataFineSessione;
}

public void setDataFineSessione(LocalDateTime dataFineSessione) {
	this.dataFineSessione = dataFineSessione;
}

}
