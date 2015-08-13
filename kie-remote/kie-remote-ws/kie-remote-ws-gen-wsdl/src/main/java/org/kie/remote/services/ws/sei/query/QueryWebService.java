
package org.kie.remote.services.ws.sei.query;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.kie.remote.services.ws.common.KieRemoteWebServiceException;
import org.kie.remote.services.ws.sei.ServicesVersion;

/**
 * Only used for initial WSDL generation
 */
@WebService(name = "QueryService", targetNamespace = QueryWebService.NAMESPACE)
public interface QueryWebService {

    static final String NAMESPACE = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/query";
    
    @WebMethod(action = "urn:Query")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "queryRequest", targetNamespace = QueryWebService.NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperQueryRequest")
    @ResponseWrapper(localName = "queryResult", targetNamespace = QueryWebService.NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperProcessInstanceAndTaskQueryResult")
    public ProcessInstanceAndTaskQueryResult queryAll(@WebParam(name = "request", targetNamespace = "") QueryRequest queryRequest) throws KieRemoteWebServiceException;

}
