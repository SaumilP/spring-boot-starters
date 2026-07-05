/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.idempotency.web;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * A {@link HttpServletResponseWrapper} that captures the response body written by the
 * downstream handler so that it can be stored in the idempotency cache.
 *
 * <p>Both {@link #getWriter()} and {@link #getOutputStream()} are intercepted to tee
 * all output into an internal {@link ByteArrayOutputStream}. The captured bytes can be
 * retrieved via {@link #getCapturedBody()} after the handler has completed.
 *
 * @since 1.0.0
 */
public class IdempotencyResponseWrapper extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream capture = new ByteArrayOutputStream();
    private PrintWriter writer;
    private ServletOutputStream outputStream;

    /**
     * Constructs a wrapper around the given response.
     *
     * @param response the original HTTP servlet response; must not be {@code null}
     */
    public IdempotencyResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    /**
     * Returns a {@link PrintWriter} that writes to both the original response and the internal
     * capture buffer, enabling the body to be read after the handler completes.
     *
     * @return the capturing writer; never {@code null}
     * @throws IOException if an I/O error occurs creating the writer
     */
    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new PrintWriter(new TeeOutputStream(super.getResponse().getOutputStream(), capture));
        }
        return writer;
    }

    /**
     * Returns a {@link ServletOutputStream} that writes to both the original response and the
     * internal capture buffer.
     *
     * @return the capturing output stream; never {@code null}
     * @throws IOException if an I/O error occurs creating the stream
     */
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (outputStream == null) {
            outputStream = new CaptureServletOutputStream(super.getResponse().getOutputStream(), capture);
        }
        return outputStream;
    }

    /**
     * Returns the response body captured so far as a UTF-8 string.
     *
     * @return the captured response body; never {@code null}
     */
    public String getCapturedBody() {
        return capture.toString(StandardCharsets.UTF_8);
    }

    private static class TeeOutputStream extends OutputStream {
        private final OutputStream primary;
        private final OutputStream secondary;

        TeeOutputStream(OutputStream primary, OutputStream secondary) {
            this.primary   = primary;
            this.secondary = secondary;
        }

        @Override
        public void write(int b) throws IOException {
            primary.write(b);
            secondary.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            primary.write(b, off, len);
            secondary.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            primary.flush();
            secondary.flush();
        }
    }

    private static class CaptureServletOutputStream extends ServletOutputStream {
        private final OutputStream primary;
        private final OutputStream secondary;

        CaptureServletOutputStream(OutputStream primary, OutputStream secondary) {
            this.primary   = primary;
            this.secondary = secondary;
        }

        @Override public boolean isReady() { return true; }
        @Override public void setWriteListener(WriteListener listener) {}

        @Override
        public void write(int b) throws IOException {
            primary.write(b);
            secondary.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            primary.write(b, off, len);
            secondary.write(b, off, len);
        }
    }
}
