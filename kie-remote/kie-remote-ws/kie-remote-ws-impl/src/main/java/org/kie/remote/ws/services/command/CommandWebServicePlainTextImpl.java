package org.kie.remote.ws.services.command;

import javax.jws.WebService;

import org.jboss.ws.api.annotation.EndpointConfig;
import org.kie.remote.services.jaxb.JaxbCommandsRequest;
import org.kie.remote.services.jaxb.JaxbCommandsResponse;
import org.kie.remote.services.rest.ResourceBase;
import org.kie.remote.services.ws.services.command.CommandWebService;
import org.kie.remote.services.ws.services.command.CommandWebServiceException;
import org.kie.services.shared.ServicesVersion;

@WebService(
        portName="CommandServicePlainTextPort", 
        serviceName="CommandService", 
        wsdlLocation="wsdl/CommandService.wsdl",
        targetNamespace=CommandWebServicePlainTextImpl.NAMESPACE,
        endpointInterface="org.kie.remote.services.ws.services.command.CommandWebService"
        )
@EndpointConfig(configFile = "WEB-INF/jaxws-endpoint-config.xml", configName = "Plain Text WS-Security Endpoint")
public class CommandWebServicePlainTextImpl extends ResourceBase implements CommandWebService {

    static final String NAMESPACE = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/command";
    
    @Override
    public JaxbCommandsResponse execute( JaxbCommandsRequest request ) throws CommandWebServiceException {
        return restProcessJaxbCommandsRequest(request);
    } 

}
