package com.Gestion.Arrayanes.Config;

import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Fija Java 25 LTS como versión declarada y como target real de compilación. */
class JavaVersionTest {

    private static final int JAVA_25_CLASS_MAJOR = 69;

    @Test
    void elPomApuntaAJava25() throws IOException {
        String pom = Files.readString(Path.of("pom.xml"));
        assertTrue(pom.contains("<java.version>25</java.version>"));
    }

    @Test
    void lasClasesSeCompilanParaJava25() throws IOException {
        try (InputStream classFile = JavaVersionTest.class
                .getClassLoader()
                .getResourceAsStream("com/Gestion/Arrayanes/ArrayanesApplication.class")) {
            assertNotNull(classFile, "No se encontró la clase compilada de la aplicación");
            try (DataInputStream in = new DataInputStream(classFile)) {
                assertEquals(0xCAFEBABE, in.readInt());
                in.readUnsignedShort(); // versión minor
                assertEquals(JAVA_25_CLASS_MAJOR, in.readUnsignedShort()); // versión major
            }
        }
    }
}
