package com.Gestion.Arrayanes;

import com.Gestion.Arrayanes.Backup.RestoreOnStartup;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ArrayanesApplication {

	public static void main(String[] args) {
		// La copia pendiente se aplica con la app detenida, antes de abrir H2.
		RestoreOnStartup.restorePendingBackup(args);
		SpringApplication.run(ArrayanesApplication.class, args);
	}

}
