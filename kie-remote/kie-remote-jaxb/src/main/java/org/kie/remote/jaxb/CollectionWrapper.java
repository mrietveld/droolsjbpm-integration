package org.kie.remote.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "CollectionWrapper", propOrder = {
        "type",
        "elements"
    })
public class CollectionWrapper {

    @XmlElement
    private CollectionType type;
  
    @XmlElement(name="element")
    private Object[] elements;
    
    @XmlType
    public enum CollectionType { 
        LIST, SET, QUEUE;
    }
    
    public CollectionWrapper() {
        // JAXB default constructor
    }
    
    public CollectionWrapper(Object[] elements, CollectionType type) {
        this.elements = elements;
        this.type = type;
    }
    
    @XmlElement(name="element")
    public Object[] getElements() {
        return elements;
    }

    public void setElements(Object[] elements) {
        this.elements = elements;
    }

    public CollectionType getType() {
        return type;
    }

    public void setType( CollectionType type ) {
        this.type = type;
    }
}
