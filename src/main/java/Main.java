import server.Server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();


        server.addHandler("GET", "/messages", (request, responseStream) -> {
            System.out.println("Header for \"GET /messages\" called");
            try {
                final Path filePath = Path.of(".", "public", "/messages.html");
                final String mimeType = Files.probeContentType(filePath);
                final long length = Files.size(filePath);
                final String response = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";
                responseStream.write(response.getBytes());
                Files.copy(filePath, responseStream);
                responseStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        server.addHandler("POST", "/messages", (request, responseStream) -> {
            // TODO: handlers code
            System.out.println("Header for \"POST /messages\" called");
            try {
                final Path filePath = Path.of(".", "public", "/post.html");
                final String mimeType = Files.probeContentType(filePath);
                final long length = Files.size(filePath);
                final String response = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";
                responseStream.write(response.getBytes());
                Files.copy(filePath, responseStream);
                responseStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        server.listen(9999);
    }

}
