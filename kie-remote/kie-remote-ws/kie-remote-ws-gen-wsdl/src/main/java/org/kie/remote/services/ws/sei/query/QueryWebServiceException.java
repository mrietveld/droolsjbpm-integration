package org.kie.remote.services.ws.sei.query;

import org.kie.remote.services.ws.common.KieRemoteWebServiceException;
import org.kie.remote.services.ws.common.WebServiceFaultInfo;

/**
 * Only used for initial WSDL generation
 */
public class QueryWebServiceException extends KieRemoteWebServiceException {

    /** default serial version UID */
    private static final long serialVersionUID = 2301L;

    public QueryWebServiceException(String message, WebServiceFaultInfo faultInfo) {
        super(message, faultInfo);
    }

}
