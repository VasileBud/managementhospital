package model.repository;

import model.MedicalService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConsultationRepository {
    private static final long CACHE_TTL_MS = Long.getLong("cache.services.ttl.ms", 300000L);
    private static volatile CacheEntry<List<MedicalService>> CACHE_ALL;
    private static final Map<Long, CacheEntry<MedicalService>> CACHE_BY_ID = new ConcurrentHashMap<>();

    public List<MedicalService> findAllServices() throws SQLException {
        CacheEntry<List<MedicalService>> cached = CACHE_ALL;
        if (cached != null && !cached.isExpired()) {
            return cached.value;
        }

        String sql = "SELECT service_id, name, price FROM medical_service ORDER BY name";
        List<MedicalService> list = new ArrayList<>();

        try (Connection conn = Repository.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new MedicalService(
                        rs.getLong("service_id"),
                        rs.getString("name"),
                        rs.getBigDecimal("price")
                ));
            }
        }
        if (CACHE_TTL_MS > 0) {
            List<MedicalService> snapshot = List.copyOf(list);
            long expiresAt = System.currentTimeMillis() + CACHE_TTL_MS;
            CACHE_ALL = new CacheEntry<>(snapshot, expiresAt);
            for (MedicalService service : snapshot) {
                CACHE_BY_ID.put(service.getServiceId(), new CacheEntry<>(service, expiresAt));
            }
            return snapshot;
        }
        return list;
    }

    public MedicalService findServiceById(long id) throws SQLException {
        CacheEntry<MedicalService> cached = CACHE_BY_ID.get(id);
        if (cached != null && !cached.isExpired()) {
            return cached.value;
        }

        CacheEntry<List<MedicalService>> listCache = CACHE_ALL;
        if (listCache != null && !listCache.isExpired()) {
            for (MedicalService service : listCache.value) {
                if (service.getServiceId() == id) {
                    CACHE_BY_ID.put(id, new CacheEntry<>(service, listCache.expiresAt));
                    return service;
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
                    MedicalService service = new MedicalService(
                            rs.getLong("service_id"),
                            rs.getString("name"),
                            rs.getBigDecimal("price")
                    );
                    if (CACHE_TTL_MS > 0) {
                        CACHE_BY_ID.put(id, new CacheEntry<>(service, System.currentTimeMillis() + CACHE_TTL_MS));
                    }
                    return service;
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
