package model.repository;

import model.Specialization;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpecializationRepository {
    private static final long CACHE_TTL_MS = Long.getLong("cache.specializations.ttl.ms", 300000L);
    private static volatile CacheEntry<List<Specialization>> CACHE_ALL;
    private static final Map<Long, CacheEntry<Specialization>> CACHE_BY_ID = new ConcurrentHashMap<>();

    public List<Specialization> findAll() throws SQLException {
        CacheEntry<List<Specialization>> cached = CACHE_ALL;
        if (cached != null && !cached.isExpired()) {
            return cached.value;
        }

        String sql = "SELECT specialization_id, name FROM specialization ORDER BY name";
        List<Specialization> list = new ArrayList<>();

        try (Connection conn = Repository.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Specialization(
                        rs.getLong("specialization_id"),
                        rs.getString("name")
                ));
            }
        }
        if (CACHE_TTL_MS > 0) {
            List<Specialization> snapshot = List.copyOf(list);
            long expiresAt = System.currentTimeMillis() + CACHE_TTL_MS;
            CACHE_ALL = new CacheEntry<>(snapshot, expiresAt);
            for (Specialization spec : snapshot) {
                CACHE_BY_ID.put(spec.getSpecializationId(), new CacheEntry<>(spec, expiresAt));
            }
            return snapshot;
        }
        return list;
    }

    public Specialization findById(long id) throws SQLException {
        CacheEntry<Specialization> cached = CACHE_BY_ID.get(id);
        if (cached != null && !cached.isExpired()) {
            return cached.value;
        }

        CacheEntry<List<Specialization>> listCache = CACHE_ALL;
        if (listCache != null && !listCache.isExpired()) {
            for (Specialization spec : listCache.value) {
                if (spec.getSpecializationId() == id) {
                    CACHE_BY_ID.put(id, new CacheEntry<>(spec, listCache.expiresAt));
                    return spec;
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
                    Specialization spec = new Specialization(
                            rs.getLong("specialization_id"),
                            rs.getString("name")
                    );
                    if (CACHE_TTL_MS > 0) {
                        CACHE_BY_ID.put(id, new CacheEntry<>(spec, System.currentTimeMillis() + CACHE_TTL_MS));
                    }
                    return spec;
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
