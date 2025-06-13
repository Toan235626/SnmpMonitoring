import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

        // ===== Backend (Spring Boot with Maven Wrapper) =====
        ProcessBuilder backend = isWindows
                ? new ProcessBuilder("cmd", "/c", "mvnw.cmd", "spring-boot:run")
                : new ProcessBuilder("./mvnw", "spring-boot:run");
        backend.directory(new File("backend"));
        backend.inheritIO().start();
        
        // ===== Frontend (Vue CLI or Vite) =====
        ProcessBuilder frontend = isWindows
                ? new ProcessBuilder("cmd", "/c", "npm", "run", "dev")
                : new ProcessBuilder("npm", "run", "dev");

        frontend.directory(new File("frontend/client")); // ⚠ chỉnh nếu bạn dùng frontend trực tiếp
        frontend.inheritIO().start();

        System.out.println(" Backend & frontend is running!");
    }
}
