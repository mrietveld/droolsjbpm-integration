package org.kie.remote.services.ws.sei.task;

import javax.xml.bind.annotation.XmlType;

@XmlType
public enum TaskOperationStatus {
    SUCCESS,
    USER_NOT_ALLOWED,
    INTERNAL_SERVER_ERROR
}
