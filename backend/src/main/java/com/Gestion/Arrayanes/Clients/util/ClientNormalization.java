package com.Gestion.Arrayanes.Clients.util;

/** Normaliza los datos de contacto antes de compararlos o persistirlos. */
public final class ClientNormalization {

    private ClientNormalization() {
    }

    /**
     * El DNI se guarda sin espacios y en mayúsculas para que la unicidad no
     * dependa de cómo lo haya escrito el usuario.
     */
    public static String normalizeDni(String dni) {
        return dni == null ? null : dni.trim().toUpperCase();
    }
}
