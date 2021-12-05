package server;

import java.util.ArrayList;
import java.util.List;

public class Request {
    private final String method;
    private final List<String> headers;
    private final List<String> bodies;

    public Request(String method, List<String> headers, List<String> bodies) {
        this.method = method;
        this.headers = headers;
        this.bodies = bodies;
    }

    public String getMethod() {
        return method;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<String> getBodies() {
        return bodies;
    }

    public static Request parseRequest(String requestStr) {
        final String LINE_SEPARATOR = "\r\n";
        final String HEADER_SEPARATOR = ": ";
        final String METHOD_SEPARATOR = " ";
        if (requestStr.contains(METHOD_SEPARATOR)) {
            final int requestLastIndex = (!requestStr.contains(LINE_SEPARATOR))
                    ? requestStr.length()
                    : requestStr.indexOf(LINE_SEPARATOR);
            final String requestLine = requestStr.substring(0, requestLastIndex);
            final String method = requestLine.substring(0, requestLine.indexOf(METHOD_SEPARATOR));
            final List<String> headers = new ArrayList<>();
            final List<String> bodies = new ArrayList<>();
            final String target = requestLine.substring(method.length() + 1);
            headers.add(target.substring(0, target.indexOf(" ")).trim());
            if (method.contains("GET")) bodies.add("empty");
            if (requestStr.length() > requestLine.length() + 1) {
                final String[] otherLines = requestStr
                        .substring(requestLine.length() + 1).trim()
                        .split(LINE_SEPARATOR);
                List.of(otherLines).stream()
                        .filter((s) -> s.length() > 3 && s.contains(HEADER_SEPARATOR))
                        .map((s) -> s.substring(0, s.indexOf(HEADER_SEPARATOR)).trim())
                        .forEach(headers::add);
                List.of(otherLines).stream()
                        .filter((s) -> s.length() > 3 && s.contains(HEADER_SEPARATOR))
                        .map((s) -> s.substring(s.indexOf(HEADER_SEPARATOR) + 1).trim())
                        .forEach(bodies::add);
            }
            return new Request(method, headers, bodies);
        } else
            return null;
    }
}
