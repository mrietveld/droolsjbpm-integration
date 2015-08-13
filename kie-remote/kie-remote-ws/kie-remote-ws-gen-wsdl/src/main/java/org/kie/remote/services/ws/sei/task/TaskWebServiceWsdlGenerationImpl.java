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

import javax.jws.WebService;

/**
 * Only used for WSDL generation
 */
@WebService(
        serviceName = "TaskService", 
        portName = "TaskServicePort", 
        name = "TaskService", 
        targetNamespace = TaskWebService.NAMESPACE)
public class TaskWebServiceWsdlGenerationImpl implements TaskWebService {

    @Override
    public TaskOperationResponse taskOperation( TaskOperationRequest arg0 ) throws TaskWebServiceException {
        return null;
    }

    @Override
    public ModifyTaskResponse modifyTask( ModifyTaskRequest arg0 ) throws TaskWebServiceException {
        return null;
    }

    @Override
    public ManageTaskContentResponse manageTaskContent( ManageTaskContentRequest arg0 ) throws TaskWebServiceException {
        return null;
    }

    @Override
    public TaskResponse getTask( GetTaskRequest arg0 ) throws TaskWebServiceException {
        return null;
    }

    @Override
    public TaskSummaryResponse getTaskSummary( GetTaskSummaryRequest arg0 ) throws TaskWebServiceException {
        return null;
    }

}