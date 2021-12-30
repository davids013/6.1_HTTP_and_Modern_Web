package server;

import org.apache.commons.fileupload.RequestContext;

import java.io.InputStream;

public class Context implements RequestContext {
    private final String contentType;
    private final int contentLength;
    private final InputStream inputStream;

    public Context(String contentType, int contentLength, InputStream inputStream) {
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.inputStream = inputStream;
    }

    @Override
    public String getCharacterEncoding() {
        return "UTF-8";
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public int getContentLength() {
        return contentLength;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }
}
