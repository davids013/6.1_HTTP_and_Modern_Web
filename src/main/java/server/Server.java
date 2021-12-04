package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Server {
    private static final int PORT = 9999;
    private static final int THREADS = 64;
    private static final String FILES_DIR = "public";
    protected static final List<String> validPaths = getValidPaths();
    private static final ExecutorService pool = Executors.newFixedThreadPool(THREADS);

    public static void main(String[] args) {
        start();
    }
    public static void start() {
        try (final var serverSocket = new ServerSocket(PORT)) {
            System.out.println("\tServer started");
            while (true) {
                newConnection(serverSocket.accept());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void newConnection(Socket socket) {
        pool.submit(new ServerTask(socket));
    }

    private static List<String> getValidPaths() {
        try {

            final List<String> list = Files.walk(Paths.get(FILES_DIR))
                    .filter(Files::isRegularFile)
                    .map((path) -> "/" + path.getFileName())
                    .collect(Collectors.toList());
            return list;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
