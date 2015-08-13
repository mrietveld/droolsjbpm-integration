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


package org.kie.remote.services.ws.sei.task;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.kie.remote.services.ws.sei.ServicesVersion;

/**
 * Only used for initial WSDL generation
 */
@WebService(name = "ProcessService", targetNamespace = TaskWebService.NAMESPACE)
public interface TaskWebService {

    final static String NAMESPACE = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/task";
    
    @WebMethod(action = "urn:TaskOperation")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "taskOperation", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperTaskOperation")
    @ResponseWrapper(localName = "taskOperationResponse", targetNamespace = "http://services.remote.kie.org/process", className = "org.kie.remote.services.ws.wsdl.generated.WrapperTaskOperationResponse")
    public TaskOperationResponse taskOperation(@WebParam(name = "arg0", targetNamespace = "") TaskOperationRequest arg0) throws TaskWebServiceException;

    @WebMethod(action = "urn:ModifyTask")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "modifyTask", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperModifyTask")
    @ResponseWrapper(localName = "modifyTaskResponse", targetNamespace = "http://services.remote.kie.org/process", className = "org.kie.remote.services.ws.wsdl.generated.WrapperModifyTaskResponse")
    public ModifyTaskResponse modifyTask(@WebParam(name = "arg0", targetNamespace = "") ModifyTaskRequest arg0) throws TaskWebServiceException;

    @WebMethod(action = "urn:ManageTaskContent")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "manageTaskContent", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperManageTaskContent")
    @ResponseWrapper(localName = "manageTaskContentResponse", targetNamespace = "http://services.remote.kie.org/process", className = "org.kie.remote.services.ws.wsdl.generated.WrapperManageTaskContentResponse")
    public ManageTaskContentMessage manageTaskContent(@WebParam(name = "arg0", targetNamespace = "") ManageTaskContentMessage arg0) throws TaskWebServiceException;

    @WebMethod(action = "urn:GetTask")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getTask", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperGetTask")
    @ResponseWrapper(localName = "taskResponse", targetNamespace = "http://services.remote.kie.org/process", className = "org.kie.remote.services.ws.wsdl.generated.WrapperGetTaskResponse")
    public TaskResponse getTask(@WebParam(name = "arg0", targetNamespace = "") GetTaskRequest arg0) throws TaskWebServiceException;

    @WebMethod(action = "urn:GetTaskSummary")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getTaskSummary", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperGetTaskSummary")
    @ResponseWrapper(localName = "taskSummaryResponse", targetNamespace = "http://services.remote.kie.org/process", className = "org.kie.remote.services.ws.wsdl.generated.WrapperGetTaskSummaryResponse")
    public TaskSummaryResponse getTaskSummary(@WebParam(name = "arg0", targetNamespace = "") GetTaskRequest arg0) throws TaskWebServiceException;

}
