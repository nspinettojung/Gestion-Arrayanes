package com.Gestion.Arrayanes.Launcher;

import com.Gestion.Arrayanes.GlobalHandlerException.GlobalCustomException;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    public static final String TOKEN_HEADER = "X-Shutdown-Token";

    private final ShutdownToken shutdownToken;
    private final ShutdownInvoker shutdownInvoker;

    public SystemController(ShutdownToken shutdownToken, ShutdownInvoker shutdownInvoker) {
        this.shutdownToken = shutdownToken;
        this.shutdownInvoker = shutdownInvoker;
    }

    /**
     * Entrega el token al frontend de la propia app. Los navegadores marcan como
     * cross-site las peticiones iniciadas desde otra página: esas no pueden leerlo.
     */
    @GetMapping("/shutdown-token")
    public ShutdownTokenResponse shutdownToken(
            @RequestHeader(value = "Sec-Fetch-Site", required = false) String fetchSite) {
        if (fetchSite != null && !fetchSite.equals("same-origin") && !fetchSite.equals("none")) {
            throw new GlobalCustomException(
                    "El token de apagado solo está disponible para la propia aplicación.",
                    HttpStatus.FORBIDDEN);
        }
        return new ShutdownTokenResponse(shutdownToken.value());
    }

    @PostMapping("/shutdown")
    public ResponseEntity<String> shutdownApp(
            @RequestHeader(value = TOKEN_HEADER, required = false) String token) {
        if (!shutdownToken.matches(token)) {
            throw new GlobalCustomException("Token de apagado inválido.", HttpStatus.FORBIDDEN);
        }

        ResponseEntity<String> response = ResponseEntity.ok("La aplicación se está cerrando...");
        shutdownInvoker.schedule();
        return response;
    }

    public record ShutdownTokenResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String token) {
    }
}
