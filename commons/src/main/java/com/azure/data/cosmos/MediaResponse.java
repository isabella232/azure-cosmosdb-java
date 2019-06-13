/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.RxDocumentServiceResponse;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * Response associated with retrieving attachment content in the Azure Cosmos DB database service.
 */
public final class MediaResponse {
    private InputStream media = null;
    private Map<String, String> responseHeaders = null;

    MediaResponse(RxDocumentServiceResponse response, boolean willBuffer) {
        this.media = response.getContentStream();
        if (willBuffer) {
            this.media = new BufferedInputStream(this.media);
        }

        this.responseHeaders = response.getResponseHeaders();
    }

    /**
     * Gets the attachment content stream.
     *
     * @return the attachment content stream.
     */
    public InputStream getMedia() {
        return this.media;
    }

    /**
     * Gets the headers associated with the response.
     *
     * @return the response headers.
     */
    public Map<String, String> getResponseHeaders() {
        return this.responseHeaders;
    }
}
