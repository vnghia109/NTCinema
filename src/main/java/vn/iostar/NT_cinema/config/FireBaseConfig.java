package vn.iostar.NT_cinema.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;

@Configuration
public class FireBaseConfig {
    @Value("${FIREBASE_CREDENTIALS}")
    private String firebaseConfig;
    @Bean
    FirebaseApp firebaseApp() throws Exception {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(firebaseConfig.getBytes())))
                .build();

        return FirebaseApp.initializeApp(options);
    }
}
