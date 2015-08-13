package org.kie.remote.services.ws.sei.process;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="correlation-key")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CorrelationKey", propOrder = {
        "name",
        "properties"
    })
public class CorrelationKey {

        @XmlElement
        @XmlSchemaType(name="string")
        private String name;

        @XmlElement(required=true)
        private Map<String, String> properties;
        
}
