package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Server {
    private final int THREADS = 64;
    private final String FILES_DIR = "public";
    private final List<String> validPaths;
    private final ExecutorService pool;
    protected final Map<String, Handler> handlers;

    public Server() {
        validPaths = getFileList();
        pool = Executors.newFixedThreadPool(THREADS);
        handlers = new ConcurrentSkipListMap<>();
    }

    public void listen(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            System.out.println("\tServer started");
            while (true) {
                newConnection(serverSocket.accept());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void newConnection(Socket socket) {
        pool.submit(new ServerTask(this, socket));
    }

    private List<String> getFileList() {
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

    protected List<String> getValidPaths() {
        return new ArrayList<>(validPaths);
    }

    public void addHandler(String method, String header, Handler handler) {
        final String requestLine = method + " " + header;
        System.out.println("Handler for request \"" + requestLine + "\" added");
        handlers.put(requestLine, handler);
    }
}
