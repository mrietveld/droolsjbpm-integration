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


package org.kie.remote.services.ws.sei.deployment;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.kie.remote.services.ws.sei.ServicesVersion;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnitList;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinitionList;

/**
 * Only used for initial WSDL generation
 */
@WebService(serviceName = "DeploymentService", targetNamespace = DeploymentWebService.NAMESPACE)
public interface DeploymentWebService {

    final static String NAMESPACE = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/deploy";
    
    @WebMethod(action = "urn:Manage")
    @WebResult(targetNamespace = "manage")
    @RequestWrapper(localName = "manage", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperDeploymentIdRequest")
    @ResponseWrapper(localName = "manageResponse", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperDeploymentInfoResponse")
    public DeploymentInfoResponse manage(@WebParam(name = "request", targetNamespace = "") DeploymentIdRequest request) throws DeploymentWebServiceException;

    @WebMethod(action = "urn:GetDeploymentInfo")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getDeploymentInfo", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperGetDeploymentInfo")
    @ResponseWrapper(localName = "deploymentInfoResponse", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperDeploymentInfoResponse")
    public JaxbDeploymentUnitList getDeploymentInfo(@WebParam(name = "request", targetNamespace = "") DeploymentIdRequest request) throws DeploymentWebServiceException;

    @WebMethod(action = "urn:GetProcessDefinitionIds")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getProcessDefinitionIds", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperGetProcessDefinitionIds")
    @ResponseWrapper(localName = "processDefinitionIdsResponse", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperProcessDefinitionIdsResponse")
    public ProcessIdsResponse getProcessDefinitionIds(@WebParam(name = "request", targetNamespace = "") DeploymentIdRequest request) throws DeploymentWebServiceException;

    @WebMethod(action = "urn:GetProcessDefinitionInfo")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getProcessDefinitionInfo", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperGetProcessDefinitionInfo")
    @ResponseWrapper(localName = "processDefinitionInfoResponse", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperProcessDefinitionInfoResponse")
    public JaxbProcessDefinitionList getProcessDefinitionInfo(@WebParam(name = "request", targetNamespace = "") DeploymentIdRequest request) throws DeploymentWebServiceException;

}
