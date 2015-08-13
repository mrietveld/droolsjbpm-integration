/*
 * Copyright 2015 JBoss Inc
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

package org.kie.remote.services.ws.sei.query;

import javax.jws.WebService;
import org.kie.remote.services.ws.common.KieRemoteWebServiceException;
import org.kie.remote.services.ws.sei.query.ProcessInstanceAndTaskQueryResult;
import org.kie.remote.services.ws.sei.query.QueryRequest;
import org.kie.remote.services.ws.sei.query.QueryWebService;

/**
 * Only used for WSDL generation
 */

/**
 * Only used for initial WSDL generation
 */
@WebService(
        serviceName = "QueryService", 
        portName = "QueryServicePort", 
        name = "QueryService", 
        targetNamespace = QueryWebService.NAMESPACE)
public class QueryWebServiceWsdlGenerationImpl implements QueryWebService {

    @Override
    public ProcessInstanceAndTaskQueryResult queryAll( QueryRequest queryRequest ) throws KieRemoteWebServiceException {
        return null;
    }


}
