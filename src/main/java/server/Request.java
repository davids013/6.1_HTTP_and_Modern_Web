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
    private static final String HEADER_SEPARATOR = ": ";
    private static final String METHOD_SEPARATOR = " ";
    private static final String EMPTY_PART_SYMBOL = "ND";
    private static final String QUERY_SEPARATOR = "?";
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
            if (!parts[1].contains(QUERY_SEPARATOR)) {
                path = parts[1];
                queryParams = new ArrayList<>();
            } else {
                String temp = parts[1];
                path = temp.substring(0, temp.indexOf(QUERY_SEPARATOR));
                temp = temp.substring(temp.indexOf(QUERY_SEPARATOR) + 1);
                queryParams = parseQueryParams(temp);
                System.out.println("Query list: " + queryParams);
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

    public String getQueryParam(String name) {
        if (queryParams == null || queryParams.isEmpty()) return null;
        for (NameValuePair pair : queryParams) {
            if (pair.getName().equals(name))
                return pair.getValue();
        }
        return null;
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
            final int index = line.indexOf(HEADER_SEPARATOR);
            final String key = line.substring(0, index);
            final String value = line.substring(index + 2);
            headers.put(key, value);
        }
        return new Request(requestLine, headers, inputStream);
    }

    public static List<NameValuePair> parseQueryParams(String query) {
        if (query.contains(QUERY_SEPARATOR))
            query = query.substring(query.indexOf(QUERY_SEPARATOR) + 1);
        return URLEncodedUtils.parse(query, StandardCharsets.UTF_8);
    }
}
