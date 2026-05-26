package ai.sinthanai.docstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DocstoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocstoreApplication.class, args);
    }
}
