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


package org.kie.remote.services.ws.sei.server;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.kie.remote.services.ws.common.KieRemoteWebServiceException;
import org.kie.remote.services.ws.sei.ServicesVersion;


@WebService(name = "KnowledgeStoreService", targetNamespace = CommonServerWebService.NAMESPACE)
public interface CommonServerWebService {

    static final String NAMESPACE = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/knowledge";

    @WebMethod(action = "urn:ModifyConfiguration")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "modifyServerConfiguration", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.ModifyServerConfigurationRequest")
    @ResponseWrapper(localName = "modifyServerConfiguration", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.ModifyServerConfigurationResponse")
    public ServerConfigurationResponse modifyServerConfiguration(@WebParam(name = "arg0", targetNamespace = "") ServerConfigurationRequest arg0) throws KieRemoteWebServiceException;

    @WebMethod(action = "urn:GetServerInformation")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "serverInformationRequest", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperManageRepositoriesRequest")
    @ResponseWrapper(localName = "serverInformationResponse", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperManageRepositoriesResponse")
    public void manageRepositories(@WebParam(name = "arg0", targetNamespace = "") RepositoryOperationRequest arg0) throws KieRemoteWebServiceException;

    @WebMethod(action = "urn:ManagerServerContainer")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "manage", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperManageProjectsRequest")
    @ResponseWrapper(localName = "manageProjects", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperManageProjectsResponse")
    public ProjectsResponse getProjects(@WebParam(name = "arg0", targetNamespace = "") ProjectsResponse arg0) throws KieRemoteWebServiceException;

    @WebMethod(action = "urn:ManageProjects")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "manageProjects", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperManageProjectsRequest")
    @ResponseWrapper(localName = "manageProjects", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperManageProjectsResponse")
    public void manageProjects(@WebParam(name = "arg0", targetNamespace = "") ProjectOperationRequest arg0) throws KieRemoteWebServiceException;

}
