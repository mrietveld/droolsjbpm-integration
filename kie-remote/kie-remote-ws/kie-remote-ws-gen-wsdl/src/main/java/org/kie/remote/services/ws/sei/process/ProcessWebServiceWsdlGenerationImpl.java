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

package org.kie.remote.services.ws.sei.process;

import javax.jws.WebService;

/**
 * Only used for initial WSDL generation
 */
@WebService(
        serviceName = "ProcessService", 
        portName = "ProcessServicePort", 
        name = "ProcessService", 
        targetNamespace = ProcessWebService.NAMESPACE)
public class ProcessWebServiceWsdlGenerationImpl implements ProcessWebService {

    @Override
    public ProcessInstanceResponse manageProcess( ManageProcessInstanceRequest processInstanceRequest )
            throws ProcessWebServiceException {
        return null;
    }

    @Override
    public void manageWorkItem( ManageWorkItemRequest workItemRequest ) throws ProcessWebServiceException {
    }

    @Override
    public ProcessInstanceVariableMessage manageProcessInstanceVariables( ProcessInstanceVariableMessage processInstanceRequest )
            throws ProcessWebServiceException {
        // DBG Auto-generated method stub
        return null;
    }

}