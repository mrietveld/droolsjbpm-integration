package org.kie.remote.services.ws.sei.task;

import javax.xml.bind.annotation.XmlType;

/**
 * Only used for initial WSDL generation
 */
@XmlType
public enum ManageTaskContentOperationType {

    ADD_FAULT,
    DELETE_FAULT,
    GET_FAULT,
    
    ADD_OUTPUT,
    DELETE_OUTPUT,
    GET_OUTPUT,
    
    ADD_ATTACHMENT,
    DELETE_ATTACHMENT,
    GET_ATTACHMENT,
    
    ADD_CONTENT,
    DELETE_CONTENT,
    GET_CONTENT
    ;
}
