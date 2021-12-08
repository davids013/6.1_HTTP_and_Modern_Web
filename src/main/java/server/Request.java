package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    private static final String LINE_SEPARATOR = "\r\n";
    private static final String HEADER_SEPARATOR = ": ";
    private static final String METHOD_SEPARATOR = " ";
    private final String method;
    private final String requestLine;
    private final String path;
    private final String httpVersion;
    private final Map<String, String> headers;
    private final String body;

    public Request(String requestLine, Map<String, String> headers, String body) {
        this.requestLine = requestLine;
        final String[] parts = requestLine.split(METHOD_SEPARATOR);
        if (parts.length == 3) {
            method = parts[0];
            path = parts[1];
            httpVersion = parts[2];
        } else {
            this.method = null;
            this.path = null;
            httpVersion = "HTTP/1.1";
        }
        this.headers = headers;
        this.body = body;
    }

    public Request(String method, String path, Map<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        httpVersion = "HTTP/1.1";
        requestLine = method + METHOD_SEPARATOR + path + METHOD_SEPARATOR + httpVersion;
        this.headers = headers;
        this.body = body;
    }

    public Request(String requestStr) {
        if (requestStr.contains(METHOD_SEPARATOR)) {
            final int requestLastIndex = (!requestStr.contains(LINE_SEPARATOR))
                    ? requestStr.length()
                    : requestStr.indexOf(LINE_SEPARATOR);
            requestLine = requestStr.substring(0, requestLastIndex);
        } else requestLine = null;
        final String[] parts = requestLine.split(METHOD_SEPARATOR);
        if (parts.length == 3) {
            this.method = parts[0];
            this.path = parts[1];
            this.httpVersion = parts[2];
        } else {
            method = null;
            path = null;
            httpVersion = null;
        }
        this.headers = null;
        this.body = null;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public String getRequestLine() {
        return requestLine;
    }

    public String getPath() {
        return path;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public static Request parseRequest(String requestStr) {
        if (requestStr.contains(METHOD_SEPARATOR)) {
            final String[] lines = requestStr.split(LINE_SEPARATOR);
            final String requestLine = lines[0];
            final List<String> keys = new ArrayList<>();
            final List<String> values = new ArrayList<>();
            if (requestStr.length() > requestLine.length() + 1) {
                for (int i = 1; i < lines.length; i++) {
                    if (lines[i].contains(HEADER_SEPARATOR)) {
                        keys.add(lines[i].substring(0, lines[i].indexOf(HEADER_SEPARATOR)).trim());
                        values.add(lines[i].substring(lines[i].indexOf(HEADER_SEPARATOR) + 1).trim());
                    }
                }
            }
            final Map<String, String> headers = new HashMap<>();
            for (int i = 0; i < keys.size(); i++) {
                headers.put(keys.get(i), values.get(i));
            }
            int lastHeaderIndex = -1;
            for (int i = 0; i < lines.length; i++) {
                if (i != 0 && !lines[i].contains(HEADER_SEPARATOR)) {
                    lastHeaderIndex = i;
                    break;
                }
            }
            final String body;
            if (lastHeaderIndex == -1) {
                body = null;
            } else {
                final StringBuilder sb = new StringBuilder();
                for (int i = lastHeaderIndex; i < lines.length; i++) {
                    sb.append(lines[i]);
                    if (i < lines.length - 1) sb.append(LINE_SEPARATOR);
                }
                body = sb.toString();
            }
            return new Request(requestLine, headers, body);
        } else
            return null;
    }
}
