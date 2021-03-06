/*
 * Copyright (c) 2009-2018, United States Government, as represented by the Secretary of Health and Human Services.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above
 *       copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the United States Government nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.hhs.fha.nhinc.exchangemgr.fhir;

import gov.hhs.fha.nhinc.exchangemgr.fhir.MimeType;
import gov.hhs.fha.nhinc.exchangemgr.fhir.RequestBuilder;
import gov.hhs.fha.nhinc.exchangemgr.fhir.FhirClient;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tjafri
 */
public class RequestBuilderTest {

    private static final Logger LOG = LoggerFactory.getLogger(FhirClient.class);
    private static final String ACCEPT = "Accept";

    @Test
    public void testGetWithXMLMimeType() {
        assertGetWithCorrectMimeType("testGetWithXMLMimeType", MimeType.XML);
    }

    @Test
    public void testGetWithJSONMimeType() {
        assertGetWithCorrectMimeType("testGetWithJSONMimeType", MimeType.JSON);
    }

    @Test
    public void testGetWithQueryParam() {
        try {
            Map<String, String> queryParam = getQueryParam();
            RequestBuilder builder = new RequestBuilder();
            HttpGet request = builder.get("http://localhost:8080/testServer/resource", queryParam,
                MimeType.XML);
            String[] params = request.getURI().getQuery().split("&");
            assertEquals("Query Parameters not appended correctly", 3, params.length);
        } catch (URISyntaxException ex) {
            LOG.error("Unable to create HTTP Request {}", ex.getLocalizedMessage(), ex);
            fail("testGetWithQueryParam failed with exception: " + ex.getLocalizedMessage());
        }
    }

    private void assertGetWithCorrectMimeType(String methodName, MimeType mimeType) {
        try {
            RequestBuilder builder = new RequestBuilder();
            HttpGet request = builder.get("http://localhost:8080/testServer/resource", mimeType);
            Header[] header = request.getHeaders(ACCEPT);
            assertTrue(header.length == 1);
            assertEquals("Accept header not present", header[0].getName(), ACCEPT);
            assertEquals("Accept header value does not match", header[0].getValue(), mimeType.getMimeType());
        } catch (URISyntaxException ex) {
            LOG.error("Unable to create HTTP Request {}", ex.getLocalizedMessage(), ex);
            fail(methodName + " failed with exception: " + ex.getLocalizedMessage());
        }
    }

    private Map<String, String> getQueryParam() {
        Map<String, String> map = new HashMap<>();
        map.put("_count", "3");
        map.put("apiKey", "testApiKey");
        map.put("_format", "XML");
        return map;
    }
}
