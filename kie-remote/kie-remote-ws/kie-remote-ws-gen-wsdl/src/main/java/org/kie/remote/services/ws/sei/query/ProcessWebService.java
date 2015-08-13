
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
@WebService(name = "ProcessService", targetNamespace = ProcessWebService.NAMESPACE)
public interface ProcessWebService {

    static final String NAMESPACE = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/process";
    
    @WebMethod(action = "urn:QueryProcess")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "queryProcess", targetNamespace = ProcessWebService.NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperQueryProcessRequest")
    @ResponseWrapper(localName = "queryProcessResponse", targetNamespace = ProcessWebService.NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperProcessInstanceResponse")
    public void query(@WebParam(name = "request", targetNamespace = "") QueryRequest processQueryRequest) throws KieRemoteWebServiceException;

}
