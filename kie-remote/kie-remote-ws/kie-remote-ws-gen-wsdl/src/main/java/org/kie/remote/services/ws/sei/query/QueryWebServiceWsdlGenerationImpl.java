package org.kie.remote.services.ws.sei.query;

import javax.jws.WebService;

import org.kie.remote.services.ws.common.KieRemoteWebServiceException;
import org.kie.remote.services.ws.sei.query.ProcessInstanceAndTaskQueryResult;
import org.kie.remote.services.ws.sei.query.QueryRequest;
import org.kie.remote.services.ws.sei.query.QueryWebService;

/**
 * Only used for initial WSDL generation
 */
@WebService(
        serviceName = "QueryService", 
        portName = "QueryServicePort", 
        name = "QueryService", 
        targetNamespace = QueryWebService.NAMESPACE)
public class QueryWebServiceWsdlGenerationImpl implements QueryWebService {

    @Override
    public ProcessInstanceAndTaskQueryResult queryAll( QueryRequest queryRequest ) throws KieRemoteWebServiceException {
        return null;
    }


}