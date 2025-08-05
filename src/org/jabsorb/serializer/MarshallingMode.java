package org.jabsorb.serializer;

/**
 * MarshallingMode enum
 * JABSORB -> legacy jabsorb styled marshall/unmarshall where nodes like 'list', 'set', 'map' are honoured.
 * STANDARD_REST -> plain REST styled marshall/unmarshall where no additional nodes are honoured apart from the object structure.
 */
public enum MarshallingMode {
    JABSORB,
    STANDARD_REST;
}