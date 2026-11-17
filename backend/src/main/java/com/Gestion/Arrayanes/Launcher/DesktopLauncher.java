package com.Gestion.Arrayanes.Launcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class DesktopLauncher {

    private static final DateTimeFormatter LOG_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private Environment environment;

    @Autowired
    private ShutdownInvoker shutdownInvoker;

    @EventListener(ApplicationReadyEvent.class)
    public void launchApp() {
        if (!environment.getProperty("arrayanes.desktop.enabled", Boolean.class, Boolean.TRUE)) {
            log("Modo escritorio desactivado; no se abre el navegador.");
            return;
        }

        String puerto = environment.getProperty("local.server.port", "8080");

        if (puerto.isBlank() || "0".equals(puerto)) {
            puerto = "8080";
        }

        String url = "http://127.0.0.1:" + puerto;
        String os = System.getProperty("os.name").toLowerCase();

        if (!os.contains("win")) {
            abrirNavegadorDelSistema(url, os);
            return;
        }

        Navegador navegador = buscarNavegador();

        if (navegador == null) {
            log("No se encontró ningún navegador conocido. Se abre el predeterminado del sistema.");
            abrirNavegadorDelSistema(url, os);
            return;
        }

        try {
            Process proceso = new ProcessBuilder(construirComando(navegador, url)).start();
            log("Navegador iniciado: " + navegador.nombre() + " (" + navegador.ruta() + ")");
            vigilarNavegador(proceso);
        } catch (IOException e) {
            log("No se pudo abrir " + navegador.nombre() + ": " + e.getMessage());
            abrirNavegadorDelSistema(url, os);
        }
    }

    /**
     * Abre el navegador Chromium en modo app con un perfil propio. Así la ventana
     * no tiene pestañas ni barra de direcciones y, al cerrarla, se apaga la API.
     */
    private List<String> construirComando(Navegador navegador, String url) {
        List<String> comando = new ArrayList<>();
        comando.add(navegador.ruta());

        if (navegador.chromium()) {
            comando.add("--app=" + url);
            comando.add("--user-data-dir=" + perfilNavegador());
            comando.add("--no-first-run");
            comando.add("--no-default-browser-check");
            comando.add("--disable-background-mode");
            comando.add("--disable-session-crashed-bubble");
            comando.add("--start-maximized");
        } else {
            comando.add("-new-window");
            comando.add(url);
        }

        return comando;
    }

    /**
     * Espera a que termine el proceso del navegador para apagar la API.
     *
     * Si el proceso termina enseguida significa que delegó en una instancia ya
     * abierta (por ejemplo, con el mismo perfil); en ese caso no apagamos nada
     * para no cortar una ventana que sigue en uso.
     */
    private void vigilarNavegador(Process proceso) {
        Thread watcher = new Thread(() -> {
            try {
                if (proceso.waitFor(3, TimeUnit.SECONDS)) {
                    log("El navegador delegó en una instancia existente; no se apaga la API.");
                    return;
                }

                int codigo = proceso.waitFor();
                log("Navegador cerrado (código " + codigo + "). Apagando la API.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log("Vigilancia del navegador interrumpida; no se apaga la API.");
                return;
            }

            // Pasa por el apagado común para guardar la copia al cerrar la ventana.
            shutdownInvoker.shutdown();
        }, "browser-watcher");

        watcher.setDaemon(true);
        watcher.start();
    }

    private Navegador buscarNavegador() {
        // Orden de preferencia. Brave primero porque es el predeterminado del equipo.
        List<NavegadorCandidato> candidatos = List.of(
                new NavegadorCandidato("Brave", rutasChromium("BraveSoftware\\Brave-Browser\\Application\\brave.exe"), true),
                new NavegadorCandidato("Chrome", rutasChromium("Google\\Chrome\\Application\\chrome.exe"), true),
                new NavegadorCandidato("Edge", rutasEdge(), true),
                new NavegadorCandidato("Opera", rutasOpera(), true),
                new NavegadorCandidato("Firefox", rutasFirefox(), false)
        );

        for (NavegadorCandidato candidato : candidatos) {
            for (String ruta : candidato.rutas()) {
                if (new File(ruta).isFile()) {
                    return new Navegador(candidato.nombre(), ruta, candidato.chromium());
                }
            }
        }

        return null;
    }

    private Path perfilNavegador() {
        String localAppData = System.getenv("LOCALAPPDATA");
        Path base = (localAppData != null && !localAppData.isBlank())
                ? Paths.get(localAppData, "GestionArrayanes")
                : Paths.get(System.getProperty("java.io.tmpdir"), "GestionArrayanes");

        Path perfil = base.resolve("browser-profile");

        try {
            Files.createDirectories(perfil);
        } catch (IOException e) {
            log("No se pudo crear el perfil del navegador: " + e.getMessage());
        }

        return perfil;
    }

    private static List<String> rutasChromium(String rutaRelativa) {
        List<String> rutas = new ArrayList<>();
        agregarRuta(rutas, System.getenv("ProgramFiles"), rutaRelativa);
        agregarRuta(rutas, System.getenv("ProgramFiles(x86)"), rutaRelativa);
        agregarRuta(rutas, System.getenv("LOCALAPPDATA"), rutaRelativa);
        return rutas;
    }

    private static List<String> rutasEdge() {
        List<String> rutas = new ArrayList<>();
        agregarRuta(rutas, System.getenv("ProgramFiles(x86)"), "Microsoft\\Edge\\Application\\msedge.exe");
        agregarRuta(rutas, System.getenv("ProgramFiles"), "Microsoft\\Edge\\Application\\msedge.exe");
        agregarRuta(rutas, System.getenv("LOCALAPPDATA"), "Microsoft\\Edge\\Application\\msedge.exe");
        return rutas;
    }

    private static List<String> rutasOpera() {
        List<String> rutas = new ArrayList<>();
        String localAppData = System.getenv("LOCALAPPDATA");

        if (localAppData != null && !localAppData.isBlank()) {
            Path base = Paths.get(localAppData, "Programs", "Opera");
            rutas.add(base.resolve("opera.exe").toString());

            if (Files.isDirectory(base)) {
                try (var carpetas = Files.list(base)) {
                    carpetas.filter(Files::isDirectory)
                            .map(carpeta -> carpeta.resolve("opera.exe"))
                            .filter(Files::isRegularFile)
                            .forEach(archivo -> rutas.add(archivo.toString()));
                } catch (IOException e) {
                    // Si no se puede listar, quedan las rutas fijas.
                }
            }

            rutas.add(base.resolve("launcher.exe").toString());
        }

        agregarRuta(rutas, System.getenv("ProgramFiles"), "Opera\\opera.exe");
        agregarRuta(rutas, System.getenv("ProgramFiles(x86)"), "Opera\\opera.exe");
        agregarRuta(rutas, System.getenv("ProgramFiles"), "Opera\\launcher.exe");
        agregarRuta(rutas, System.getenv("ProgramFiles(x86)"), "Opera\\launcher.exe");

        return rutas;
    }

    private static List<String> rutasFirefox() {
        List<String> rutas = new ArrayList<>();
        agregarRuta(rutas, System.getenv("ProgramFiles"), "Mozilla Firefox\\firefox.exe");
        agregarRuta(rutas, System.getenv("ProgramFiles(x86)"), "Mozilla Firefox\\firefox.exe");
        return rutas;
    }

    private static void agregarRuta(List<String> rutas, String base, String rutaRelativa) {
        if (base != null && !base.isBlank()) {
            rutas.add(Paths.get(base, rutaRelativa).toString());
        }
    }

    private void abrirNavegadorDelSistema(String url, String os) {
        try {
            if (os.contains("win")) {
                new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", url).start();
            } else {
                new ProcessBuilder("xdg-open", url).start();
            }
        } catch (IOException e) {
            log("No se pudo abrir el navegador del sistema: " + e.getMessage());
        }
    }

    private void log(String mensaje) {
        String linea = "[" + LocalDateTime.now().format(LOG_FORMAT) + "] " + mensaje;
        System.out.println(linea);

        try {
            String localAppData = System.getenv("LOCALAPPDATA");
            Path base = (localAppData != null && !localAppData.isBlank())
                    ? Paths.get(localAppData, "GestionArrayanes")
                    : Paths.get(System.getProperty("java.io.tmpdir"), "GestionArrayanes");
            Path logs = base.resolve("logs");
            Files.createDirectories(logs);
            Files.writeString(
                    logs.resolve("launcher.log"),
                    linea + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (Exception e) {
            // El log es solo para diagnóstico: si falla, no interrumpimos el arranque.
        }
    }

    private record Navegador(String nombre, String ruta, boolean chromium) {
        List<String> rutas() {
            return List.of(ruta);
        }
    }

    private record NavegadorCandidato(String nombre, List<String> rutas, boolean chromium) {
    }
}
