package server;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    private static final String LINE_SEPARATOR = "\r\n";
    private static final String HEADER_SEPARATOR = ": ";
    private static final String METHOD_SEPARATOR = " ";
    private static final String EMPTY_PART_SYMBOL = "ND";
    private final String method;
    private final String requestLine;
    private final String path;
    private final String httpVersion;
    private final Map<String, String> headers;
    private final InputStream body;
    private final List<NameValuePair> queryParams;

    private Request(String requestLine, Map<String, String> headers, InputStream body) {
        this.requestLine = requestLine;
        this.headers = headers == null ? new HashMap<>() : headers;
        this.body = body;
        final String[] parts = requestLine.split(METHOD_SEPARATOR);
        if (parts.length == 3) {
            method = parts[0];
            if (!parts[1].contains("?")) {
                path = parts[1];
                queryParams = new ArrayList<>();
            } else {
                String temp = parts[1];
                path = temp.substring(0, temp.indexOf("?"));
                temp = temp.substring(temp.indexOf("?") + 1);
                queryParams = parseQueryParams(temp);
            }
            httpVersion = parts[2];
        } else {
            this.method = EMPTY_PART_SYMBOL;
            this.path = EMPTY_PART_SYMBOL;
            httpVersion = EMPTY_PART_SYMBOL;
            queryParams = new ArrayList<>();
        }
    }

    public String getMethod() {
        return method;
    }

    public Map<String, String> getHeaders() {
        return headers;
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

    public List<NameValuePair> getQueryParams() {
        return new ArrayList<>(queryParams);
    }

    public static Request fromInputStream(InputStream inputStream) throws IOException {
        final BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        final String requestLine = in.readLine();
        System.out.println(">> " + requestLine);
        if (requestLine == null)
            throw new IOException("Null requested!");
        final String[] parts = requestLine.split(METHOD_SEPARATOR);
        if (parts.length != 3) throw new IOException("Invalid request!");
        final Map<String, String> headers = new HashMap<>();
        String line;
        while (!(line = in.readLine()).isEmpty()) {
            final int index = line.indexOf(": ");
            final String key = line.substring(0, index);
            final String value = line.substring(index + 2);
            headers.put(key, value);
        }
        return new Request(requestLine, headers, inputStream);
    }

//    public static Request parseRequest(String requestStr) throws IOException {
//        System.out.println(">> " + requestStr);
//        if (requestStr == null)
//            throw new IOException("Null requested!");
//        final String[] lines = requestStr.split(LINE_SEPARATOR);
//        final String requestLine = lines[0];
//        if (requestStr.contains(METHOD_SEPARATOR)) {
//            final List<String> keys = new ArrayList<>();
//            final List<String> values = new ArrayList<>();
//            if (requestStr.length() > requestLine.length() + 1) {
//                for (int i = 1; i < lines.length; i++) {
//                    if (lines[i].contains(HEADER_SEPARATOR)) {
//                        keys.add(lines[i].substring(0, lines[i].indexOf(HEADER_SEPARATOR)).trim());
//                        values.add(lines[i].substring(lines[i].indexOf(HEADER_SEPARATOR) + 1).trim());
//                    }
//                }
//            }
//            final Map<String, String> headers = new HashMap<>();
//            for (int i = 0; i < keys.size(); i++) {
//                headers.put(keys.get(i), values.get(i));
//            }
//            int lastHeaderIndex = -1;
//            for (int i = 0; i < lines.length; i++) {
//                if (i != 0 && !lines[i].contains(HEADER_SEPARATOR)) {
//                    lastHeaderIndex = i;
//                    break;
//                }
//            }
//            final String body;
//            if (lastHeaderIndex == -1) {
//                body = "";
//            } else {
//                final StringBuilder sb = new StringBuilder();
//                for (int i = lastHeaderIndex; i < lines.length; i++) {
//                    sb.append(lines[i]);
//                    if (i < lines.length - 1) sb.append(LINE_SEPARATOR);
//                }
//                body = sb.toString();
//            }
//            return new Request(requestLine, headers, body);
//        } else
//            return null;
//    }

    public static List<NameValuePair> parseQueryParams(String query) {
        if (query.contains("?"))
            query = query.substring(query.indexOf("?") + 1);
        return URLEncodedUtils.parse(query, StandardCharsets.UTF_8);
    }
}
