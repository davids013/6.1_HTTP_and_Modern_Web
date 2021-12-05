package server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class ServerTask implements Runnable {
    private final Server server;
    private final Socket socket;

    public ServerTask(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        System.out.println("\tNew connection");
    }

    @Override
    public void run() {
        try (
                final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            final String requestLine = in.readLine();
            if (requestLine == null) {
                System.out.println("Null requested");
                return;
            }

            System.out.println(requestLine);
            final Request request = Request.parseRequest(requestLine);
            if (server.handlers.containsKey(request.getMethod() + " " + request.getHeaders().get(0))) {
                server.handlers.get(request.getMethod() + " " + request.getHeaders().get(0))
                        .handle(request, out);
                return;
            }

//            final String[] parts = requestLine.split(" ");
//
//            if (parts.length != 3) {
//                // just close socket
//                System.out.println("Not 3 request parts");
//                return;
//            }

            final String path = request.getHeaders().get(0);
            if (!server.getValidPaths().contains(path)) {
                final String response = "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";
                out.write(response.getBytes());
                out.flush();
                return;
            }

            final Path filePath = Path.of(".", "public", path);
            final String mimeType = Files.probeContentType(filePath);

            // special case for classic
            if (path.equals("/classic.html")) {
                final String template = Files.readString(filePath);
                final byte[] content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                final String response = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + content.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";
                out.write(response.getBytes());
                out.write(content);
                out.flush();
                return;
            }

            final long length = Files.size(filePath);
            final String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + mimeType + "\r\n" +
                    "Content-Length: " + length + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";
            out.write(response.getBytes());
            Files.copy(filePath, out);
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
