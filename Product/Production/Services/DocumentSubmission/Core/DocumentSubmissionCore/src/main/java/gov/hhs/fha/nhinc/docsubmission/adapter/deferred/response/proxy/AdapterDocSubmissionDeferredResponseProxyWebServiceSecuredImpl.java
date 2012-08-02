/*
 * Copyright (c) 2012, United States Government, as represented by the Secretary of Health and Human Services. 
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
package gov.hhs.fha.nhinc.docsubmission.adapter.deferred.response.proxy;

import gov.hhs.fha.nhinc.adapterxdrresponsesecured.AdapterXDRResponseSecuredPortType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.docsubmission.adapter.deferred.response.proxy.service.AdapterDocSubmissionDeferredResponseSecuredServicePortDescriptor;
import gov.hhs.fha.nhinc.messaging.client.CONNECTClient;
import gov.hhs.fha.nhinc.messaging.client.CONNECTClientFactory;
import gov.hhs.fha.nhinc.messaging.service.port.ServicePortDescriptor;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.webserviceproxy.WebServiceProxyHelper;
import gov.hhs.healthit.nhin.XDRAcknowledgementType;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * 
 * @author Visu Patlolla
 */
public class AdapterDocSubmissionDeferredResponseProxyWebServiceSecuredImpl implements
        AdapterDocSubmissionDeferredResponseProxy {
    private Log log = null;
    private WebServiceProxyHelper oProxyHelper = null;

    public AdapterDocSubmissionDeferredResponseProxyWebServiceSecuredImpl() {
        log = createLogger();
        oProxyHelper = createWebServiceProxyHelper();
    }

    protected Log createLogger() {
        return LogFactory.getLog(getClass());
    }

    protected WebServiceProxyHelper createWebServiceProxyHelper() {
        return new WebServiceProxyHelper();
    }

    protected CONNECTClient<AdapterXDRResponseSecuredPortType> getCONNECTClientSecured(
            ServicePortDescriptor<AdapterXDRResponseSecuredPortType> portDescriptor, String url, AssertionType assertion) {
        
        return CONNECTClientFactory.getInstance().getCONNECTClientSecured(portDescriptor,
                url, assertion);
    }

    public XDRAcknowledgementType provideAndRegisterDocumentSetBResponse(RegistryResponseType regResponse,
            AssertionType assertion) {
        log.debug("Begin AdapterDocSubmissionDeferredResponseProxyWebServiceSecuredImpl.provideAndRegisterDocumentSetBResponse");
        XDRAcknowledgementType response = null;
        String serviceName = NhincConstants.ADAPTER_XDR_RESPONSE_SECURED_SERVICE_NAME;

        try {
            String url = oProxyHelper.getAdapterEndPointFromConnectionManager(serviceName);

            if (NullChecker.isNotNullish(url)) {
                ServicePortDescriptor<AdapterXDRResponseSecuredPortType> portDescriptor = new AdapterDocSubmissionDeferredResponseSecuredServicePortDescriptor();

                CONNECTClient<AdapterXDRResponseSecuredPortType> client = getCONNECTClientSecured(portDescriptor, url, assertion);

                response = (XDRAcknowledgementType) client.invokePort(AdapterXDRResponseSecuredPortType.class,
                        "provideAndRegisterDocumentSetBResponse", regResponse);
            } else {
                log.error("Failed to call the web service (" + serviceName + ").  The URL is null.");
            }
        } catch (Exception ex) {
            log.error("Error: Failed to retrieve url for service: " + serviceName + " for local home community");
            log.error(ex.getMessage(), ex);
        }

        log.debug("End AdapterDocSubmissionDeferredResponseProxyWebServiceSecuredImpl.provideAndRegisterDocumentSetBResponse");
        return response;
    }
}