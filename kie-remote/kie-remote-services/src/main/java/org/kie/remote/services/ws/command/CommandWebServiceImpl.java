/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.remote.services.ws.command;

import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Source;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceProvider;

import org.kie.internal.identity.IdentityProvider;
import org.kie.remote.services.cdi.ProcessRequestBean;
import org.kie.remote.services.jaxb.JaxbCommandsRequest;
import org.kie.remote.services.jaxb.JaxbCommandsResponse;
import org.kie.remote.services.rest.ResourceBase;
import org.kie.remote.services.rest.jaxb.DynamicJaxbContext;
import org.kie.remote.services.util.ExecuteCommandUtil;
import org.kie.remote.services.ws.command.generated.Execute;
import org.kie.remote.services.ws.command.generated.ExecuteResponse;
import org.kie.remote.services.ws.command.generated.ObjectFactory;
import org.kie.remote.services.ws.common.ExceptionType;
import org.kie.remote.services.ws.common.KieRemoteWebServiceException;
import org.kie.remote.services.ws.common.WebServiceFaultInfo;
import org.kie.services.shared.ServicesVersion;

@WebServiceProvider(
        portName="CommandServiceBasicAuthPort",
        serviceName = "CommandServiceBasicAuth",
        wsdlLocation="wsdl/CommandService.wsdl",
        targetNamespace = CommandWebServiceImpl.NAMESPACE
        // endpointInterface = "org.kie.remote.services.ws.command.generated.CommandWebService" // (used with the @WebService anno)
        )
@RequestScoped
public class CommandWebServiceImpl extends ResourceBase implements Provider<Source> {

    public static final String NAMESPACE = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/command";

    @Inject
    private DynamicJaxbContext dynamicJaxbContext;

    @Inject
    private IdentityProvider identityProvider;

    @Inject
    private ProcessRequestBean processRequesBean;

    // Useful for a number of reasons: among others, user principal is available here
    @Resource
    private WebServiceContext context;
    
    @Override
    public Source invoke( Source requestSource ) {

        String corrId = null; // TODO
        
        JaxbCommandsRequest request = deserializeAndUnwrapRequest(requestSource, corrId);
        JaxbCommandsResponse response = ExecuteCommandUtil.restProcessJaxbCommandsRequest(request, identityProvider, processRequesBean);
        JAXBSource responseSource = wrapAndSerializeResponse(response, corrId); 
     
        return responseSource;
    }

    private JaxbCommandsRequest deserializeAndUnwrapRequest(Source requestSource, String corrId) { 
        
        Unmarshaller unmarshaller;
        try {
            unmarshaller = dynamicJaxbContext.createUnmarshaller();
        } catch( JAXBException e ) {
            throw new KieRemoteWebServiceException("Could not create unmarshaller: " + e.getMessage(), 
                    new WebServiceFaultInfo(corrId, ExceptionType.SYSTEM),
                    e);
        }

        JAXBElement<Execute> jaxbWrappedRequest;
        try {
            jaxbWrappedRequest = unmarshaller.unmarshal(requestSource, Execute.class);
        } catch( JAXBException e ) {
            throw new KieRemoteWebServiceException("Could not unmarshall request source: " + e.getMessage(),
                    new WebServiceFaultInfo(corrId, ExceptionType.SYSTEM),
                    e);
        }

        Execute wrappedRquest = jaxbWrappedRequest.getValue();
        if( wrappedRquest == null ) { 
            throw new KieRemoteWebServiceException("Execute request instance is null!",
                    new WebServiceFaultInfo(corrId, ExceptionType.SYSTEM));
        }

        JaxbCommandsRequest request = wrappedRquest.getRequest();
        if( request == null ) { 
            throw new KieRemoteWebServiceException("JaxbCommandsRequest instance is null!",
                    new WebServiceFaultInfo(corrId, ExceptionType.SYSTEM));
        }
        return request;
    }
    
    private JAXBSource wrapAndSerializeResponse(JaxbCommandsResponse response, String corrId) { 
        ExecuteResponse wrappedResponse = new ExecuteResponse();
        wrappedResponse.setReturn(response);

        JAXBElement<ExecuteResponse> jaxbWrappedResponse = new ObjectFactory().createExecuteResponse(wrappedResponse);

        JAXBSource responseSource;
        try {
            responseSource = new JAXBSource(dynamicJaxbContext.createMarshaller(), jaxbWrappedResponse);
        } catch( JAXBException e ) { 
            throw new KieRemoteWebServiceException("Could not serialize response to JAXBSource: "  + e.getMessage(), 
                    new WebServiceFaultInfo(corrId, ExceptionType.SYSTEM),
                    e);
        }

        return responseSource;
    }
}
