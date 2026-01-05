package server.repository;

import shared.dto.SpecializationDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SpecializationRepository {

    public List<SpecializationDTO> findAll() throws SQLException {
        String sql = "SELECT specialization_id, name FROM specialization ORDER BY name";
        List<SpecializationDTO> list = new ArrayList<>();

        try (Connection conn = Repository.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new SpecializationDTO(
                        rs.getLong("specialization_id"),
                        rs.getString("name")
                ));
            }
        }
        return list;
    }

    public SpecializationDTO findById(long id) throws SQLException {
        String sql = "SELECT specialization_id, name FROM specialization WHERE specialization_id = ?";

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new SpecializationDTO(
                            rs.getLong("specialization_id"),
                            rs.getString("name")
                    );
                }
            }
        }
        return null;
    }
}