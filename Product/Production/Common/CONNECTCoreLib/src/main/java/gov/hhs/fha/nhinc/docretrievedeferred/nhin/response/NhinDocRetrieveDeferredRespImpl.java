package gov.hhs.fha.nhinc.docretrievedeferred.nhin.response;

import gov.hhs.fha.nhinc.common.nhinccommon.*;
import gov.hhs.fha.nhinc.common.nhinccommonproxy.RespondingGatewayCrossGatewayRetrieveSecuredResponseType;
import gov.hhs.fha.nhinc.docretrieve.DocRetrieveDeferredAuditLogger;
import gov.hhs.fha.nhinc.docretrieve.DocRetrieveDeferredPolicyChecker;
import gov.hhs.fha.nhinc.docretrievedeferred.adapter.proxy.response.AdapterDocRetrieveDeferredRespObjectFactory;
import gov.hhs.fha.nhinc.saml.extraction.SamlTokenExtractor;
import gov.hhs.healthit.nhin.DocRetrieveAcknowledgementType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.ws.WebServiceContext;

/**
 * Created by
 * User: ralph
 * Date: Aug 2, 2010
 * Time: 1:50:15 PM
 */
public class NhinDocRetrieveDeferredRespImpl {

    private static Log log = LogFactory.getLog(NhinDocRetrieveDeferredRespImpl.class);

    DocRetrieveAcknowledgementType sendToRespondingGateway(RespondingGatewayCrossGatewayRetrieveSecuredResponseType body, WebServiceContext context) {
        AdapterDocRetrieveDeferredRespObjectFactory  objectFactory;
        DocRetrieveDeferredAuditLogger auditLog = new DocRetrieveDeferredAuditLogger();
        NhinTargetSystemType        targetSystem;
        RetrieveDocumentSetResponseType nhinResponse;
        DocRetrieveDeferredPolicyChecker policyCheck = new DocRetrieveDeferredPolicyChecker();
        gov.hhs.fha.nhinc.common.nhinccommonadapter.RespondingGatewayCrossGatewayRetrieveSecuredResponseType      bodyToSend;

        nhinResponse = body.getRetrieveDocumentSetResponse();
        targetSystem = body.getNhinTargetSystem();

        DocRetrieveAcknowledgementType response = null;
        AssertionType assertion = SamlTokenExtractor.GetAssertion(context);

        objectFactory = new AdapterDocRetrieveDeferredRespObjectFactory();

        auditLog.auditDocRetrieveDeferredResponse(nhinResponse, assertion);

        bodyToSend = new gov.hhs.fha.nhinc.common.nhinccommonadapter.RespondingGatewayCrossGatewayRetrieveSecuredResponseType();
        bodyToSend.setRetrieveDocumentSetResponse(nhinResponse);

        if(policyCheck.checkOutgoingPolicy(nhinResponse, assertion, targetSystem.getHomeCommunity().getHomeCommunityId())) {
            response = objectFactory.getDocumentDeferredRequestProxy().sendToAdapter(bodyToSend, assertion);
        }
        else {
            //
            // Call error interface on recieving adapter here.
            //
        }

        return response;
    }

//    protected String getHomeCommFromTarget(NhinTargetCommunitiesType target) {
//        String sHomComm = null;
//        if (target.getNhinTargetCommunity() != null &&
//                target.getNhinTargetCommunity().size() > 0) {
//            NhinTargetCommunityType comm = target.getNhinTargetCommunity().get(0);
//            if (comm.getHomeCommunity() != null) {
//                sHomComm = comm.getHomeCommunity().getHomeCommunityId();
//            }
//        }
//        return sHomComm;
//    }

//    private boolean isPolicyValid(RetrieveDocumentSetResponseType oEachNhinRequest, AssertionType oAssertion, HomeCommunityType targetCommunity) {
//        boolean isValid = false;
//        DocRetrieveEventType checkPolicy = new DocRetrieveEventType();
//        DocRetrieveMessageType checkPolicyMessage = new DocRetrieveMessageType();
//        checkPolicyMessage.setRetrieveDocumentSetRequest(oEachNhinRequest);
//        checkPolicyMessage.setAssertion(oAssertion);
//        checkPolicy.setMessage(checkPolicyMessage);
//        checkPolicy.setDirection(NhincConstants.POLICYENGINE_OUTBOUND_DIRECTION);
//        checkPolicy.setInterface(NhincConstants.AUDIT_LOG_ENTITY_INTERFACE);
//        checkPolicy.setReceivingHomeCommunity(targetCommunity);
//        PolicyEngineChecker policyChecker = new PolicyEngineChecker();
//        CheckPolicyRequestType policyReq = policyChecker.checkPolicyDocRetrieve(checkPolicy);
//        PolicyEngineProxyObjectFactory policyEngFactory = new PolicyEngineProxyObjectFactory();
//        PolicyEngineProxy policyProxy = policyEngFactory.getPolicyEngineProxy();
//        CheckPolicyResponseType policyResp = policyProxy.checkPolicy(policyReq);
//        /* if response='permit' */
//        if (policyResp.getResponse().getResult().get(0).getDecision().value().equals(NhincConstants.POLICY_PERMIT)) {
//            isValid = true;
//        }
//        return isValid;
//    }

}
