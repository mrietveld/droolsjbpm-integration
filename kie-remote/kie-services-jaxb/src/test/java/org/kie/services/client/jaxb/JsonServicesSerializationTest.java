package org.kie.services.client.jaxb;

import org.kie.services.client.AbstractServicesSerializationTest;
import org.kie.services.client.serialization.JsonSerializationProvider;

public class JsonServicesSerializationTest extends AbstractServicesSerializationTest {

    public TestType getType() { 
        return TestType.JSON;
    }
    
    protected JsonSerializationProvider jsonProvider = new JsonSerializationProvider();
    
    public <T> T testRoundTrip(T in) throws Exception {
        String jsonStr = jsonProvider.serialize(in);
        logger.debug(jsonStr);
        jsonProvider.setDeserializeOutputClass(in.getClass());
        return (T) jsonProvider.deserialize(jsonStr);
    }

    public void addClassesToSerializationProvider(Class<?>... extraClass) {
        // no-op
    }
 
}