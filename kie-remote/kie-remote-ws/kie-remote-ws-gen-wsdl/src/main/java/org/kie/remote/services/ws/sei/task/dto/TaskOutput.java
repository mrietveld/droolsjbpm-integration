package org.kie.remote.services.ws.sei.task.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class TaskOutput {

    @XmlElement
    @XmlSchemaType(name = "string")
    private String contentType;
    
    @XmlElement
    @XmlSchemaType(name="base64Binary")
    private byte[] content = null;

    @XmlElement(required=true)
    @XmlSchemaType(name="base64Binary")
    Boolean useByteArrayContent;
    
    @XmlAnyElement(lax=false)
    private Object contentbject;
    
}
