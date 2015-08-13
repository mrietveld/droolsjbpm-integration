/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

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
public interface QueryWebService {

    static final String NAMESPACE = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/query";
    
    @WebMethod(action = "urn:Query")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "queryRequest", targetNamespace = QueryWebService.NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperQueryRequest")
    @ResponseWrapper(localName = "queryResult", targetNamespace = QueryWebService.NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperProcessInstanceAndTaskQueryResult")
    public ProcessInstanceAndTaskQueryResult queryAll(@WebParam(name = "request", targetNamespace = "") QueryRequest queryRequest) throws KieRemoteWebServiceException;

    // query proc inst variables
    // query tasks
    // query proc inst + variables
    // query tasks + variables
    
    // query executor jobs
    // query task audit history

}
