package dao;

import model.Ricetta;
import model.Ingrediente;
import util.DBConnection;

import java.sql.*;
import java.util.*;

public class RicettaDAO {

	public void save(Ricetta r) throws SQLException {
		String sql = "INSERT INTO ricetta (nome, tempoPreparazione) VALUES (?, ?)";
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			ps.setString(1, r.getNome());
			ps.setInt(2, r.getTempoPreparazione());
			ps.executeUpdate();

			try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					r.setIdRicetta(generatedKeys.getInt(1));
				}
			}
		}
	}

	public Optional<Ricetta> findById(int id) throws SQLException {
		String sql = "SELECT * FROM ricetta WHERE idRicetta = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					Ricetta r = mapResultSetToRicetta(rs);
					return Optional.of(r);
				}
			}
		}
		return Optional.empty();
	}

	public List<Ricetta> getAll() throws SQLException {
		List<Ricetta> list = new ArrayList<>();
		String sql = "SELECT * FROM ricetta ORDER BY nome";
		try (Connection conn = DBConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				Ricetta r = mapResultSetToRicetta(rs);
				list.add(r);
			}
		}
		return list;
	}

	public List<Ricetta> getByNome(String nome) throws SQLException {
		List<Ricetta> list = new ArrayList<>();
		String sql = "SELECT * FROM ricetta WHERE nome = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, nome);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Ricetta r = mapResultSetToRicetta(rs);
					list.add(r);
				}
			}
		}
		return list;
	}

	public List<Ricetta> searchByNome(String partialNome) throws SQLException {
		List<Ricetta> list = new ArrayList<>();
		String sql = "SELECT * FROM ricetta WHERE nome ILIKE ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, "%" + partialNome + "%");
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Ricetta r = mapResultSetToRicetta(rs);
					list.add(r);
				}
			}
		}
		return list;
	}

	public void update(int id, Ricetta r) throws SQLException {
		String sql = "UPDATE ricetta SET nome = ?, tempoPreparazione = ? WHERE idRicetta = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, r.getNome());
			ps.setInt(2, r.getTempoPreparazione());
			ps.setInt(3, id);
			ps.executeUpdate();
		}
	}

	public void delete(int id) throws SQLException {
		String sql = "DELETE FROM ricetta WHERE idRicetta = ?";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, id);
			ps.executeUpdate();
		}
	}

	private Ricetta mapResultSetToRicetta(ResultSet rs) throws SQLException {
		String nome = rs.getString("nome");
		int tempo = rs.getInt("tempoPreparazione");
		int idRicetta = rs.getInt("idRicetta");

		Ricetta r = new Ricetta(nome, tempo);
		r.setIdRicetta(idRicetta);

		Map<Ingrediente, Double> ingredienti = getIngredientiPerRicetta(idRicetta);
		r.setIngredienti(ingredienti);

		return r;
	}

	private Map<Ingrediente, Double> getIngredientiPerRicetta(int idRicetta) throws SQLException {
		Map<Ingrediente, Double> map = new HashMap<>();
		String sql = "SELECT i.idIngrediente, i.nome, i.tipo, u.quantita " + "FROM Usa u "
				+ "JOIN Ingrediente i ON u.idIngrediente = i.idIngrediente " + "WHERE u.idRicetta = ?";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idRicetta);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String nomeIngrediente = rs.getString("nome");
					String tipoIngrediente = rs.getString("tipo");
					int idIngrediente = rs.getInt("idIngrediente");

					Ingrediente ing = new Ingrediente(nomeIngrediente, tipoIngrediente);
					ing.setIdIngrediente(idIngrediente);

					double quantita = rs.getDouble("quantita");
					map.put(ing, quantita);
				}
			}
		}
		return map;
	}

	public List<Ricetta> filtraRicette(String nome, Integer tempoMin, Integer tempoMax, Integer ingredientiMin,
			Integer ingredientiMax) throws SQLException {
		StringBuilder sql = new StringBuilder(
				"SELECT r.idRicetta, r.nome, r.tempoPreparazione, COUNT(u.idIngrediente) AS numIng " + "FROM ricetta r "
						+ "LEFT JOIN Usa u ON r.idRicetta = u.idRicetta " + "WHERE 1=1 ");
		List<Object> params = new ArrayList<>();
		if (nome != null && !nome.trim().isEmpty()) {
			sql.append("AND r.nome ILIKE ? ");
			params.add("%" + nome.trim() + "%");
		}
		if (tempoMin != null) {
			sql.append("AND r.tempoPreparazione >= ? ");
			params.add(tempoMin);
		}
		if (tempoMax != null) {
			sql.append("AND r.tempoPreparazione <= ? ");
			params.add(tempoMax);
		}
		sql.append("GROUP BY r.idRicetta, r.nome, r.tempoPreparazione ");
		boolean hasHaving = false;
		if (ingredientiMin != null) {
			sql.append("HAVING COUNT(u.idIngrediente) >= ? ");
			params.add(ingredientiMin);
			hasHaving = true;
		}
		if (ingredientiMax != null) {
			sql.append(hasHaving ? "AND " : "HAVING ");
			sql.append("COUNT(u.idIngrediente) <= ? ");
			params.add(ingredientiMax);
		}

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			for (int i = 0; i < params.size(); i++) {
				ps.setObject(i + 1, params.get(i));
			}

			ResultSet rs = ps.executeQuery();
			List<Ricetta> result = new ArrayList<>();
			while (rs.next()) {
				Ricetta r = new Ricetta(rs.getString("nome"), rs.getInt("tempoPreparazione"));
				r.setIdRicetta(rs.getInt("idRicetta"));

			
				Map<Ingrediente, Double> ingredienti = getIngredientiPerRicetta(r.getIdRicetta());
				r.setIngredienti(ingredienti);

				result.add(r);
			}

			return result;
		}
	}

}
