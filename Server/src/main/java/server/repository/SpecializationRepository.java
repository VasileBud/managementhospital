package server.repository;

import shared.dto.SpecializationDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpecializationRepository {
    private static final long CACHE_TTL_MS = Long.getLong("cache.specializations.ttl.ms", 300000L);
    private static volatile CacheEntry<List<SpecializationDTO>> CACHE_ALL;
    private static final Map<Long, CacheEntry<SpecializationDTO>> CACHE_BY_ID = new ConcurrentHashMap<>();

    public List<SpecializationDTO> findAll() throws SQLException {
        CacheEntry<List<SpecializationDTO>> cached = CACHE_ALL;
        if (cached != null && !cached.isExpired()) {
            return cached.value;
        }

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
        if (CACHE_TTL_MS > 0) {
            List<SpecializationDTO> snapshot = List.copyOf(list);
            long expiresAt = System.currentTimeMillis() + CACHE_TTL_MS;
            CACHE_ALL = new CacheEntry<>(snapshot, expiresAt);
            for (SpecializationDTO dto : snapshot) {
                CACHE_BY_ID.put(dto.getSpecializationId(), new CacheEntry<>(dto, expiresAt));
            }
            return snapshot;
        }
        return list;
    }

    public SpecializationDTO findById(long id) throws SQLException {
        CacheEntry<SpecializationDTO> cached = CACHE_BY_ID.get(id);
        if (cached != null && !cached.isExpired()) {
            return cached.value;
        }

        CacheEntry<List<SpecializationDTO>> listCache = CACHE_ALL;
        if (listCache != null && !listCache.isExpired()) {
            for (SpecializationDTO dto : listCache.value) {
                if (dto.getSpecializationId() == id) {
                    CACHE_BY_ID.put(id, new CacheEntry<>(dto, listCache.expiresAt));
                    return dto;
                }
            }
            return null;
        }

        String sql = "SELECT specialization_id, name FROM specialization WHERE specialization_id = ?";

        try (Connection conn = Repository.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SpecializationDTO dto = new SpecializationDTO(
                            rs.getLong("specialization_id"),
                            rs.getString("name")
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
