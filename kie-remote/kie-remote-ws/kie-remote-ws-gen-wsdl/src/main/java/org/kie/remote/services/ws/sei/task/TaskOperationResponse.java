package org.kie.remote.services.ws.sei.task;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.kie.api.task.model.Status;

/**
 * Only used for initial WSDL generation
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaskOperationResponse", propOrder = {
    "taskId", 
    "requestedOperation",
    "operationStatus",
    "statusDescription",
    "taskStatus"
})
public class TaskOperationResponse {

    @XmlElement(required=true)
    @XmlSchemaType(name="long")
    private Long taskId;
   
    @XmlElement(required=true)
    private TaskOperationType requestedOperation;
   
    @XmlElement(required=true)
    private TaskOperationStatus operationStatus;
   
    @XmlElement(required=false)
    private String statusDescription;
    
    @XmlElement(required=true)
    private Status taskStatus;
    
}
