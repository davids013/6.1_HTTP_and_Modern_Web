package server;

import org.apache.commons.fileupload.*;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import javax.servlet.http.HttpServletRequest;
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
    private static final String QUERY_SEPARATOR = "?";
    private static final String EMPTY_PART_SYMBOL = "ND";
    private final String method;
    private final String requestLine;
    private final String path;
    private final String httpVersion;
    private final Map<String, String> headers;
    private final List<NameValuePair> queryParams;
    private final List<NameValuePair> postParams;
    private final List<FileItem> fileItems;

    private Request(String requestLine, Map<String, String> headers,
                    List<NameValuePair> postParams, List<FileItem> fileItems, InputStream inputStream) {
        this.requestLine = requestLine;
        this.headers = headers == null ? new HashMap<>() : headers;
        this.postParams = postParams == null ? new ArrayList<>() : postParams;
        this.fileItems = fileItems == null ? new ArrayList<>() : fileItems;
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

    public List<NameValuePair> getPostParams() {
        return new ArrayList<>(postParams);
    }

    public String getPostParam(String name) {
        for (NameValuePair nvp : postParams)
            if (nvp.getName().equals(name)) return nvp.getValue();
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
//        headers.keySet().forEach((key) -> System.out.println(key + " -> " + headers.get(key)));
        List<NameValuePair> postParameters = null;
        List<FileItem> multipart = null;
        if (headers.containsKey("Content-Type")) {
            final String contentType = headers.get("Content-Type");
            System.out.println("Content-Type -> " + contentType);
            int size = 0;
            if (headers.containsKey("Content-Length")) {
                size = Integer.parseInt(headers.get("Content-Length"));
                System.out.println("Content-Length -> " + size);
            }
            if (contentType.equals("application/x-www-form-urlencoded")) {
                char[] chars = new char[size];
                try {
                    in.read(chars, 0, size);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final String body = new String(chars);
                postParameters = parsePostParams(body);
                System.out.println("Post params x-www-form: " + postParameters);
//            } else if (contentType.contains("multipart/form-data")) {
//                char[] chars = new char[size];
//                try {
//                    in.read(chars, 0, size);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                final String body = new String(chars);
//                System.out.println(body);
////                multipart = parseMultipart(contentType, size, inputStream);
            }
        }
        return new Request(requestLine, headers, postParameters, multipart, inputStream);
    }

    public static List<NameValuePair> parseQueryParams(String query) {
        if (query.contains(QUERY_SEPARATOR))
            query = query.substring(query.indexOf(QUERY_SEPARATOR) + 1);
        return URLEncodedUtils.parse(query, StandardCharsets.UTF_8);
    }

    public static List<NameValuePair> parsePostParams(String post) {
        return parseQueryParams(post);
    }

//    private static List<FileItem> parseMultipart(
//            String contentType, int contentLength, InputStream inputStream) {
//        System.out.println("\t\tIT'S MULTIPART !!!");
//        final RequestContext context = new Context(contentType, contentLength, inputStream);
//        System.out.println("\tContext created");
//        final FileUploadBase fileUpload = new FileUpload();
//        System.out.println("\tFileUpload created");
//        List<FileItem> files = null;
//        try {
//            files = fileUpload.parseRequest(context);
//        } catch (FileUploadException e) {
//            System.err.println("FileUploadException");
//            e.printStackTrace();
//        }
//        System.out.println("\t\tFILEITEMS:");
//        files.forEach(System.out::println);
//        return files;
//    }
}
