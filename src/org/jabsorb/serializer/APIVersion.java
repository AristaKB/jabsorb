package org.jabsorb.serializer;

/**
 * API version enum and its mapping with the marshalling modes
 * v1 -> JABSORB
 * v2 -> STANDARD_REST
 */
public enum APIVersion {
    V1("v1", MarshallingMode.JABSORB),
    V2("v2", MarshallingMode.STANDARD_REST);

    private final String versionString;
    private final MarshallingMode marshallingMode;

    APIVersion(String versionString, MarshallingMode marshallingMode) {
        this.versionString = versionString;
        this.marshallingMode = marshallingMode;
    }

    public MarshallingMode getMarshallingMode() {
        return marshallingMode;
    }

    public static APIVersion fromString(String value) {
        for (APIVersion version : values()) {
            if (version.versionString.equalsIgnoreCase(value)) {
                return version;
            }
        }
        throw new IllegalArgumentException("Unknown API version: " + value);
    }

    /**
     * Returns mapped MarshallingMode for the given API version value.
     * Throws IllegalArgumentException if API version value is unknown.
     * @param versionValue
     * @return
     */
    public static MarshallingMode getMarshallingMode(String versionValue) {
        return fromString(versionValue).getMarshallingMode();
    }
}