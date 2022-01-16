package server;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemHeaders;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MyFileItem implements FileItem {
    final private String contentType;
    private String fieldName;
    final private String name;
    final private Map<String, String> headers;
    final private String data;
    boolean isFormField;

    public MyFileItem(String contentType, String fieldName, String name, Map<String, String> headers, String data) {
        this.contentType = contentType;
        this.fieldName = fieldName;
        this.name = name;
        this.headers = headers;
        this.data = data;
        isFormField = true;
    }

    public static List<FileItem> parseFileItems(String contentTypeLine, String body) {
        final String boundary = "--" + contentTypeLine
                .substring(contentTypeLine.indexOf("boundary=") + "boundary=".length());
        String[] temp = body.split(boundary);
        String[] filesData = Arrays.copyOfRange(temp, 1, temp.length - 1);
        for (int i = 0; i < filesData.length; i++)
            filesData[i] = filesData[i].trim();
        final List<FileItem> fileItems = new ArrayList<>();
        for (String str : filesData) {
            final Map<String, String> headers = parseHeaderField(str);
            String contentType,
                    fieldName = null,
                    name = null,
                    data;
            data = headers.getOrDefault("Data", null);
            contentType = headers.getOrDefault("Content-Type", null);
            if (headers.containsKey("Content-Disposition")) {
                final String val = headers.get("Content-Disposition");
                final String[] parts = val.split("; ");
                for (final String s : parts) {
                    if (s.startsWith("name=")) {
                        fieldName = s.substring("name=".length());
                    } else if (s.startsWith("filename=")) {
                        name = s.substring("filename=".length());
                    }
                }
            }
            fileItems.add(new MyFileItem(contentType, fieldName, name, headers, data));
        }
        return fileItems;
    }

    private static Map<String, String> parseHeaderField(String headerFieldName) {
        final String END_OF_LINE = "\r\n";
        final String HEAD_SEP = ": ";
        final Map<String, String> headers = new HashMap<>();
        String[] parts = headerFieldName.split(END_OF_LINE);
        int dataIndex = parts.length;
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isBlank()) dataIndex = i + 1;
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            final String str = parts[i].trim();
            if (i < dataIndex && str.contains(HEAD_SEP)) {
                final int index = str.indexOf(HEAD_SEP);
                final String key = str.substring(0, index);
                final String value = str.substring(index + 2);
                headers.put(key, value);
            } else if (i == dataIndex) {
                sb.append(str);
                headers.put("Data", str);
            } else if (i > dataIndex)
                sb.append("\r\n").append(str);
        }
        headers.put("Data", sb.toString());
        return headers;
    }

    public Map<String, String> getMyHeaders() { return headers; }

    @Override
    public InputStream getInputStream() {
        return null;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isInMemory() {
        return false;
    }

    @Override
    public long getSize() {
        return data.getBytes().length;
    }

    @Override
    public byte[] get() {
        return data.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getString(String s) {
        return headers.getOrDefault(s, null);
    }

    @Override
    public String getString() {
        return data;
    }

    @Override
    public void write(File file) { }

    @Override
    public void delete() { }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public void setFieldName(String s) { fieldName = s; }

    @Override
    public boolean isFormField() {
        return isFormField;
    }

    @Override
    public void setFormField(boolean b) { isFormField = b; }

    @Override
    public OutputStream getOutputStream() {
        return null;
    }

    @Override
    public FileItemHeaders getHeaders() {
        return null;
    }

    @Override
    public void setHeaders(FileItemHeaders fileItemHeaders) { }
}
