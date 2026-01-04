package util;

public class PasswordHasher {


    public String hash(String raw) {
        return Integer.toHexString(raw.hashCode());
    }

    public boolean verify(String raw, String hashed) {
        return hash(raw).equals(hashed);
    }
}
