package org.kie.remote.services.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.Endpoint;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxws.EndpointImpl;
import org.junit.Test;
import org.kie.remote.client.api.RemoteRuntimeEngineBuilder;
import org.kie.remote.client.api.RemoteRuntimeEngineFactory;
import org.kie.remote.client.jaxb.ClientJaxbSerializationProvider;
import org.kie.remote.client.jaxb.JaxbCommandsRequest;
import org.kie.remote.jaxb.gen.JaxbStringObjectPairArray;
import org.kie.remote.jaxb.gen.StartProcessCommand;
import org.kie.remote.jaxb.gen.util.JaxbStringObjectPair;
import org.kie.remote.services.jaxb.ServerJaxbSerializationProvider;
import org.kie.remote.services.ws.command.CommandServiceTest;
import org.kie.remote.services.ws.command.CommandWebServiceImpl;
import org.kie.remote.services.ws.command.generated.CommandWebService;
import org.kie.services.client.builder.redirect.AvailablePortFinder;
import org.kie.services.shared.ServicesVersion;

public class WebserviceIntegrationTest {

    @Test
    public void webservicesTest() throws Exception {

        // replace "VERSION" with actual version
        URL url = CommandServiceTest.class.getResource("/wsdl/CommandService.wsdl");
        File file = new File(url.toURI());
        String content = IOUtils.toString(new FileInputStream(new File(url.toURI())));
        content = content.replaceAll("VERSION", ServicesVersion.VERSION);
        IOUtils.write(content,  new FileOutputStream(file), "UTF-8");

        CommandWebServiceImpl cmdWebService = new CommandWebServiceImpl();
        int port = AvailablePortFinder.getNextAvailable(1025);

        String serverAddress = "http://localhost:" + port +"/";
        String address = serverAddress + "ws/CommandService";
        EndpointImpl endpoint = (EndpointImpl) Endpoint.create(cmdWebService);
        endpoint.setAddress(address);
        endpoint.publish();

        assertTrue( "Endpoint was not published!", endpoint.isPublished() );
        assertEquals("http://schemas.xmlsoap.org/wsdl/soap/http", endpoint.getBinding().getBindingID());

        CommandWebService webServiceClient =
                RemoteRuntimeEngineFactory.newCommandWebServiceClientBuilder()
                    .addDeploymentId("org.test:kjar:1.0")
                    .addServerUrl(serverAddress)
                    .addUserName("test").addPassword("test")
                    .buildBasicAuthClient();

        StartProcessCommand spCmd = new StartProcessCommand();
        Float [] obj = new Float[] { 10.3f, 5.6f };
        JaxbStringObjectPairArray params = new JaxbStringObjectPairArray();
        String paramName = "myobject";
        JaxbStringObjectPair pair = new JaxbStringObjectPair(paramName, obj);
        params.getItems().add(pair);
        spCmd.setParameter(params);

        JaxbCommandsRequest request = new JaxbCommandsRequest("org.test:kjar:1.0", spCmd);
        webServiceClient.execute(request);
    }

    @Test
    public void  serializationTest() throws Exception {
        StartProcessCommand spCmd = new StartProcessCommand();
        Float [] obj = new Float[] { 10.3f, 5.6f };
        JaxbStringObjectPairArray params = new JaxbStringObjectPairArray();
        String paramName = "myobject";
        JaxbStringObjectPair pair = new JaxbStringObjectPair(paramName, obj);
        params.getItems().add(pair);
        spCmd.setParameter(params);

        String xmlStr = ClientJaxbSerializationProvider.newInstance().serialize(spCmd);
        org.drools.core.command.runtime.process.StartProcessCommand serverCmd
            = (org.drools.core.command.runtime.process.StartProcessCommand)
                ServerJaxbSerializationProvider.newInstance().deserialize(xmlStr);

        Object floatArrObj = serverCmd.getParameters().get(paramName);
        Float [] copyObj = (Float[]) floatArrObj;
        assertEquals( obj[0], copyObj[0] );
    }
}
