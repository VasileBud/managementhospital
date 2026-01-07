package server.repository;

import shared.dto.MedicalServiceDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConsultationRepository {
    private static final long CACHE_TTL_MS = Long.getLong("cache.services.ttl.ms", 300000L);
    private static volatile CacheEntry<List<MedicalServiceDTO>> CACHE_ALL;
    private static final Map<Long, CacheEntry<MedicalServiceDTO>> CACHE_BY_ID = new ConcurrentHashMap<>();

    public List<MedicalServiceDTO> findAllServices() throws SQLException {
        CacheEntry<List<MedicalServiceDTO>> cached = CACHE_ALL;
        if (cached != null && !cached.isExpired()) {
            return cached.value;
        }

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
        if (CACHE_TTL_MS > 0) {
            List<MedicalServiceDTO> snapshot = List.copyOf(list);
            long expiresAt = System.currentTimeMillis() + CACHE_TTL_MS;
            CACHE_ALL = new CacheEntry<>(snapshot, expiresAt);
            for (MedicalServiceDTO dto : snapshot) {
                CACHE_BY_ID.put(dto.getServiceId(), new CacheEntry<>(dto, expiresAt));
            }
            return snapshot;
        }
        return list;
    }

    public MedicalServiceDTO findServiceById(long id) throws SQLException {
        CacheEntry<MedicalServiceDTO> cached = CACHE_BY_ID.get(id);
        if (cached != null && !cached.isExpired()) {
            return cached.value;
        }

        CacheEntry<List<MedicalServiceDTO>> listCache = CACHE_ALL;
        if (listCache != null && !listCache.isExpired()) {
            for (MedicalServiceDTO dto : listCache.value) {
                if (dto.getServiceId() == id) {
                    CACHE_BY_ID.put(id, new CacheEntry<>(dto, listCache.expiresAt));
                    return dto;
                }
            }
            return null;
        }

        String sql = "SELECT service_id, name, price FROM medical_service WHERE service_id = ?";

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    MedicalServiceDTO dto = new MedicalServiceDTO(
                            rs.getLong("service_id"),
                            rs.getString("name"),
                            rs.getBigDecimal("price")
                    );
                    if (CACHE_TTL_MS > 0) {
                        CACHE_BY_ID.put(id, new CacheEntry<>(dto, System.currentTimeMillis() + CACHE_TTL_MS));
                    }
                    return dto;
                }
            }
        }
        return null;
    }

    private static final class CacheEntry<T> {
        private final T value;
        private final long expiresAt;

        private CacheEntry(T value, long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }

        private boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }
}
