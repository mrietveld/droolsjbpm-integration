package org.kie.remote.services.ws.sei.task;

import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.kie.internal.jaxb.StringKeyObjectValueMapXmlAdapter;
import org.kie.remote.services.ws.sei.task.dto.TaskAttachment;
import org.kie.remote.services.ws.sei.task.dto.TaskContent;
import org.kie.remote.services.ws.sei.task.dto.TaskFaultData;
import org.kie.remote.services.ws.sei.task.dto.TaskOutput;

public class ManageTaskContentOperationContent {

    @XmlElement
    @XmlSchemaType(name="string")
    private TaskFaultData faultData;
   
    @XmlElement
    @XmlSchemaType(name="string")
    private TaskOutput output;
   
    @XmlElement
    @XmlSchemaType(name="string")
    private TaskContent taskContent;
   
    @XmlElement
    @XmlSchemaType(name="string")
    private TaskAttachment attachment;
    
    // content
    
    @XmlElement
    private Long id;

    @XmlElement
    @XmlSchemaType(name="base64Binary")
    private byte[] content = null;
    
    @XmlJavaTypeAdapter(StringKeyObjectValueMapXmlAdapter.class)
    private Map<String, Object> contentMap = null;
    
    
}
