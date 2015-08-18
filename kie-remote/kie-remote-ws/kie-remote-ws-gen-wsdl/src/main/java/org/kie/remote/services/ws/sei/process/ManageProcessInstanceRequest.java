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

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.kie.remote.jaxb.JaxbUnknownAdapter;

/**
 * Only used for initial WSDL generation
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ManageProcessInstanceRequest", propOrder = {
    "deploymentId",
    "operation",
    "processInstanceId",
    "correlationKey",
    "processDefinitionId",
    "parameters",
    "event",
})
public class ManageProcessInstanceRequest {

    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String deploymentId;
    
    @XmlElement
    private ProcessInstanceOperationType operation;
    
    @XmlElement
    @XmlSchemaType(name="long")
    private Long processInstanceId;
    
    @XmlElement
    private CorrelationKey correlationKey;

    @XmlElement
    @XmlSchemaType(name="string")
    private String processDefinitionId;

    @XmlElement
    @XmlJavaTypeAdapter(JaxbUnknownAdapter.class)
    private Map<String, Object> parameters;
   
    @XmlElement
    @XmlJavaTypeAdapter(JaxbUnknownAdapter.class)
    private Object event;
    
}
