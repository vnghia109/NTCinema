package vn.iostar.NT_cinema.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;

@Configuration
public class FireBaseConfig {
    @Bean
    FirebaseApp firebaseApp() throws Exception {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(new FileInputStream("src/main/resources/nt-cinema-firebase-adminsdk-qqxnf-bbd0a156f7.json")))
                .build();

        return FirebaseApp.initializeApp(options);
    }
}
