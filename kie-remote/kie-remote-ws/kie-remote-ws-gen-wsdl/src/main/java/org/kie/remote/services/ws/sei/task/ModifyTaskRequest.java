package org.kie.remote.services.ws.sei.task;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.kie.remote.services.ws.sei.StringObjectEntryList;

/**
 * Only used for initial WSDL generation
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaskOperationRequest", propOrder = {
    "taskId", 
    "operation",
    "userId",
    "targetUserOrGroupId",
    "data"
})
public class ModifyTaskRequest {

    @XmlElement(required=true)
    @XmlSchemaType(name="long")
    private Long taskId;
    
    @XmlElement(required=true)
    private ModifyTaskOperationType operation;
    
    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String requestingUserId;
   
    // For complete or fail
    @XmlElement(required=false)
    private StringObjectEntryList data;
    
}
