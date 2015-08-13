package org.kie.remote.services.ws.sei.task.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.kie.internal.task.api.model.AccessType;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class TaskFaultData {

    @XmlElement
    private AccessType accessType; 

    @XmlElement
    @XmlSchemaType(name="string")
    private String type;
  
    @XmlElement(name="fault-name")
    @XmlSchemaType(name="string")
    private String faultName;

    @XmlElement
    @XmlSchemaType(name = "string")
    private String contentType;
    
    @XmlElement(required=true)
    @XmlSchemaType(name="base64Binary")
    private Boolean useByteArrayContent;
    
    @XmlElement
    @XmlSchemaType(name="base64Binary")
    private byte[] content;
    
    @XmlAnyElement
    private Object contentObject;
    
}
