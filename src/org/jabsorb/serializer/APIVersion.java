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

    public static APIVersion fromString(String methodName) {
        if (methodName == null) {
            return V1;
        }
        for (APIVersion version : values()) {
            // case insensitive comparision
            if (methodName.toLowerCase().endsWith(version.versionString)) {
                return version;
            }
        }
        // API version not found in the methodName, default to v1
        return V1;
    }

    /**
     * Returns mapped MarshallingMode for the given methodName.
     * Lookup for API version (case insensitive) at the end of the methodName.
     * Returns V1 as default if methodName is null or does not have any valid version string.
     * @param methodName
     * @return
     */
    public static MarshallingMode getMarshallingMode(String methodName) {
        return fromString(methodName).getMarshallingMode();
    }
}