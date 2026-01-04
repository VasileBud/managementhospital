package repository;

import dto.MedicalServiceDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConsultationRepository {

    public List<MedicalServiceDTO> findAllServices() throws SQLException {
        String sql = "SELECT service_id, name, price FROM medical_service ORDER BY name";
        List<MedicalServiceDTO> list = new ArrayList<>();

        try (Connection conn = Repository.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new MedicalServiceDTO(
                        rs.getLong("service_id"),
                        rs.getString("name"),
                        rs.getBigDecimal("price")
                ));
            }
        }
        return list;
    }

    public MedicalServiceDTO findServiceById(long id) throws SQLException {
        String sql = "SELECT service_id, name, price FROM medical_service WHERE service_id = ?";

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new MedicalServiceDTO(
                            rs.getLong("service_id"),
                            rs.getString("name"),
                            rs.getBigDecimal("price")
                    );
                }
            }
        }
        return null;
    }
}