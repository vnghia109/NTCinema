package vn.iostar.NT_cinema;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@EnableScheduling
@SpringBootApplication
public class NtCinemaApplication {

    public static void main(String[] args) {
        SpringApplication.run(NtCinemaApplication.class, args);
    }

}
