package dev.rono.permissions.bootstrap;

/** Immutable banner line data produced after runtime probing. */
public record PlatformDescriptor(
        String implementationLabel,
        String versionLabel,
        PlatformFamily family,
        String vendorDetails) {

    /**
     * Banner fragment after the {@code Runtime: } prefix, e.g. {@code Paper 1.21.1 (Bukkit family)}.
     */
    public String runtimeBannerLine() {
        String ver = nz(versionLabel);
        String base = nz(implementationLabel) + (ver.isEmpty() ? "" : " " + ver);
        String vd = nz(vendorDetails);

        if (!vd.isEmpty()) {
            base = base + " (" + vd + ")";
        }

        String fam = switch (family) {
            case BUKKIT -> "Bukkit";
            case BUNGEECORD -> "BungeeCord";
            case SPONGE -> "Sponge";
            case VELOCITY -> "Velocity";
            case UNKNOWN -> "Unknown";
        };

        return base + " (" + fam + " family)";
    }

    private static String nz(String s) {
        return s == null ? "" : s.trim();
    }
}
