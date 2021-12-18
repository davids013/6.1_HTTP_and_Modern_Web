package server;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Request {
    private static final String LINE_SEPARATOR = "\r\n";
    private static final String HEADER_SEPARATOR = ": ";
    private static final String METHOD_SEPARATOR = " ";
    private static final String QUERY_SEPARATOR = "?";
    private static final String EMPTY_PART_SYMBOL = "ND";
    private final String method;
    private final String requestLine;
    private final String path;
    private final String httpVersion;
    private final Map<String, String> headers;
    private final String body;
    private final List<NameValuePair> queryParams;
    private final List<NameValuePair> postParams;

    private Request(String requestLine, Map<String, String> headers, String body) {
        this.requestLine = requestLine;
        this.headers = headers == null ? new HashMap<>() : headers;
        this.body = body;
        if (headers.containsKey("Content-Type")) {
            if (headers.get("Content-Type").equals("application/x-www-form-urlencoded")) {
                postParams = parsePostParams(body);
                System.out.println("Body of x-www-form -> " + body);
            } else postParams = new ArrayList<>();
        } else postParams = new ArrayList<>();
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

    public List<NameValuePair> getPostParams() {
        return new ArrayList<>(postParams);
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
        headers.keySet().forEach((key) -> System.out.println(key + " -> " + headers.get(key)));
        String body = "";
        char[] chars;
        if (headers.containsKey("Content-Length")) {
            final int size = Integer.parseInt(headers.get("Content-Length"));
            chars = new char[size];
            in.read(chars, 0, size);
            body = new String(chars);
        }
        return new Request(requestLine, headers, body);
    }

    public static List<NameValuePair> parseQueryParams(String query) {
        if (query.contains(QUERY_SEPARATOR))
            query = query.substring(query.indexOf(QUERY_SEPARATOR) + 1);
        return URLEncodedUtils.parse(query, StandardCharsets.UTF_8);
    }

    public static List<NameValuePair> parsePostParams(String post) {
        return parseQueryParams(post);
    }
}
