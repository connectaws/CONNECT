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

import gov.hhs.fha.nhinc.callback.opensaml.CertificateManager;
import gov.hhs.fha.nhinc.callback.opensaml.CertificateManagerImpl;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.properties.PropertyAccessException;
import gov.hhs.fha.nhinc.properties.PropertyAccessor;
import gov.hhs.fha.nhinc.util.StreamUtils;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

/**
 *
 * @author tjafri
 */
public class FhirClient {

    private static final Logger LOG = LoggerFactory.getLogger(FhirClient.class);

    public String sendRequest(HttpUriRequest request) throws FhirClientException {
        LOG.info("sendRequest: {}", request);
        HttpClientBuilder client = HttpClientBuilder.create();
        try (CloseableHttpClient httpClient = client.setSSLSocketFactory(createSSLConnectionSocketFactory()).build()) {
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                return unmarshallResponse(response);
            }
        } catch (IOException | NoSuchAlgorithmException | KeyManagementException ex) {
            throw new FhirClientException("Error sending Http Request: " + ex.getLocalizedMessage(), ex);
        }
    }

    private static String getTLSVersionsFromProperties() {
        try {
            return PropertyAccessor.getInstance().getProperty(NhincConstants.GATEWAY_PROPERTY_FILE,
                NhincConstants.FHIR_TLS);
        } catch (PropertyAccessException ex) {
            LOG.error("Unable to retrieve {} from {}: {}", NhincConstants.FHIR_TLS,
                NhincConstants.GATEWAY_PROPERTY_FILE, ex.getLocalizedMessage(), ex);
        }
        return null;
    }

    private static SSLConnectionSocketFactory createSSLConnectionSocketFactory() throws NoSuchAlgorithmException,
        KeyManagementException {
        String tls = getTLSVersionsFromProperties();
        SSLContext sslContext;
        if (StringUtils.isNotEmpty(tls)) {
            sslContext = SSLContext.getInstance(tls);
        } else {
            sslContext = SSLContext.getDefault();
        }
        CertificateManager cm = CertificateManagerImpl.getInstance();
        sslContext.init(getKeyManager(cm), getTrustManager(cm), null);
        return new SSLConnectionSocketFactory(sslContext);
    }

    private static KeyManager[] getKeyManager(CertificateManager cm) {
        try {
            KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyFactory.init(cm.getKeyStore(), getKeystorePassword());
            return keyFactory.getKeyManagers();
        } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
            LOG.error("Could not initialize key and trust managers: {} ", e.getLocalizedMessage(), e);
        }
        return new KeyManager[0];
    }

    private static TrustManager[] getTrustManager(CertificateManager cm) {
        try {
            TrustManagerFactory trustFactory = TrustManagerFactory.
                getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustFactory.init(cm.getTrustStore());
            return trustFactory.getTrustManagers();
        } catch (KeyStoreException | NoSuchAlgorithmException e) {
            LOG.error("Could not initialize key and trust managers: {} ", e.getLocalizedMessage(), e);
        }
        return new TrustManager[0];
    }

    private static char[] getKeystorePassword() throws UnrecoverableKeyException {
        String keystorePassword = System.getProperty("javax.net.ssl.keyStorePassword");
        if (keystorePassword == null || keystorePassword.isEmpty()) {
            throw new UnrecoverableKeyException("Password for key is null or empty.");
        }

        return keystorePassword.toCharArray();
    }

    private static String unmarshallResponse(CloseableHttpResponse response) throws FhirClientException {
        if (null != response) {
            if ((null != response.getStatusLine())
                && (response.getStatusLine().getStatusCode() == HttpStatus.OK.value())) {
                return unmarshall(response.getEntity());
            } else {
                throw new FhirClientException("Unsuccessful repsonse returned by the server");
            }
        }
        throw new FhirClientException("Null response returned by the server");
    }

    private static String unmarshall(HttpEntity entity) throws FhirClientException {
        InputStream instream = null;
        try {
            if (null != entity && null != entity.getContent()) {
                instream = entity.getContent();
                return IOUtils.toString(instream, FhirConstants.DEFAULT_CHARSET);
            }
        } catch (IOException ex) {
            throw new FhirClientException("Unable to read response, " + ex.getLocalizedMessage(), ex);
        } finally {
            StreamUtils.closeStreamSilently(instream);
        }
        return null;
    }
}
