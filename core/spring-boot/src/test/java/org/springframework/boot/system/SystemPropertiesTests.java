package org.springframework.boot.system;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SystemPropertiesTests {

    @Test
    void getShouldReturnNullWhenPropertiesAreNotFound() {
        // Test du cas où aucune des propriétés n'existe
        String result = SystemProperties.get("non.existent.property.12345");
        assertThat(result).isNull();
    }

    @Test
    void getShouldReturnSystemPropertyWhenPresent() {
        System.setProperty("test.prop", "value1");
        try {
            String result = SystemProperties.get("test.prop");
            assertThat(result).isEqualTo("value1");
        } finally {
            System.clearProperty("test.prop");
        }
    }
}