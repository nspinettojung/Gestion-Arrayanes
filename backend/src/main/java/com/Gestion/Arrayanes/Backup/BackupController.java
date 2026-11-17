package com.Gestion.Arrayanes.Backup;

import com.Gestion.Arrayanes.GlobalHandlerException.GlobalCustomException;
import com.Gestion.Arrayanes.Launcher.ShutdownToken;
import com.Gestion.Arrayanes.Launcher.SystemController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints de copias, protegidos con el mismo token local que el apagado. */
@RestController
@RequestMapping("/api/v1/system/backups")
public class BackupController {

    private final BackupService backupService;
    private final ShutdownToken shutdownToken;

    public BackupController(BackupService backupService, ShutdownToken shutdownToken) {
        this.backupService = backupService;
        this.shutdownToken = shutdownToken;
    }

    @GetMapping
    public BackupListResponse list(
            @RequestHeader(value = SystemController.TOKEN_HEADER, required = false) String token) {
        requireToken(token);
        return backupService.list();
    }

    @PostMapping
    public ResponseEntity<BackupInfo> create(
            @RequestHeader(value = SystemController.TOKEN_HEADER, required = false) String token) {
        requireToken(token);
        return ResponseEntity.status(HttpStatus.CREATED).body(backupService.createManual());
    }

    @PostMapping("/{name}/restore")
    public RestoreResponse restore(
            @PathVariable String name,
            @RequestHeader(value = SystemController.TOKEN_HEADER, required = false) String token) {
        requireToken(token);
        BackupInfo backup = backupService.requestRestore(name);
        return new RestoreResponse("La copia se restaurará al reiniciar la aplicación.", backup.name());
    }

    private void requireToken(String token) {
        if (!shutdownToken.matches(token)) {
            throw new GlobalCustomException("Token de apagado inválido.", HttpStatus.FORBIDDEN);
        }
    }
}
