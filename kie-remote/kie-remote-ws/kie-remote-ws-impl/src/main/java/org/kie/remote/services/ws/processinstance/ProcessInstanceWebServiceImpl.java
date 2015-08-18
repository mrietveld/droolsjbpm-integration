package org.kie.remote.services.ws.processinstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;

import org.apache.commons.lang3.StringUtils;
import org.drools.core.command.runtime.process.AbortProcessInstanceCommand;
import org.drools.core.command.runtime.process.GetProcessInstanceByCorrelationKeyCommand;
import org.drools.core.command.runtime.process.GetProcessInstanceCommand;
import org.drools.core.command.runtime.process.SignalEventCommand;
import org.drools.core.command.runtime.process.StartCorrelatedProcessCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.KieInternalServices;
import org.kie.internal.process.CorrelationKeyFactory;
import org.kie.remote.jaxb.JaxbUnknownAdapter;
import org.kie.remote.jaxb.StringStringEntry;
import org.kie.remote.jaxb.StringStringEntryList;
import org.kie.remote.services.rest.ResourceBase;
import org.kie.remote.services.ws.common.ExceptionType;
import org.kie.remote.services.ws.common.WebServiceFaultInfo;
import org.kie.remote.services.ws.processinstance.generated.CorrelationKey;
import org.kie.remote.services.ws.processinstance.generated.ManageProcessInstanceRequest;
import org.kie.remote.services.ws.processinstance.generated.ManageProcessInstanceResponse;
import org.kie.remote.services.ws.processinstance.generated.ManageWorkItemRequest;
import org.kie.remote.services.ws.processinstance.generated.ProcessInstanceOperationType;
import org.kie.remote.services.ws.processinstance.generated.ProcessInstanceResponse;
import org.kie.remote.services.ws.processinstance.generated.ProcessInstanceServicePortType;
import org.kie.remote.services.ws.processinstance.generated.ProcessInstanceState;
import org.kie.remote.services.ws.processinstance.generated.ProcessInstanceVariableMessage;
import org.kie.remote.services.ws.processinstance.generated.ProcessInstanceWebServiceException;
import org.kie.services.shared.ServicesVersion;


@WebService(
        portName = "ProcessInstanceServicePort", 
        serviceName = "ProcessInstanceService", 
        name = "ProcessInstanceService", 
        endpointInterface = "org.kie.remote.services.ws.processInstance.generated.ProcessInstanceService",
        targetNamespace = ProcessInstanceWebServiceImpl.NAMESPACE)
public class ProcessInstanceWebServiceImpl extends ResourceBase implements ProcessInstanceServicePortType {
   
    final static String NAMESPACE = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/processInstance";

    private static final JaxbUnknownAdapter jaxbAdapter = new JaxbUnknownAdapter();
    
    @Override
    @SuppressWarnings("unchecked")
    public ProcessInstanceResponse manageProcessInstance( ManageProcessInstanceRequest request )
            throws ProcessInstanceWebServiceException {
        if( request == null ) { 
            throwException("Null request received", null, ExceptionType.VALIDATION);
        }
        String correlationId = null;
      
        ProcessInstanceOperationType oper = request.getOperation();
        if( oper == null ) { 
            throwException("No oper received", correlationId, ExceptionType.VALIDATION);
        }
        String deploymentId = request.getDeploymentId();
        if( deploymentId == null ) { 
            throwException("No deployment id specified", correlationId, ExceptionType.VALIDATION);
        }
        Long procInstId = request.getProcessInstanceId();
      
        CorrelationKey corrKey = request.getCorrelationKey();
        if( corrKey != null ) { 
           
        }
        
        Command cmd = null;
        switch(oper) { 
        case ABORT:
            checkProcessInstanceId(correlationId, procInstId);
            AbortProcessInstanceCommand abortCmd = new AbortProcessInstanceCommand();
            abortCmd.setProcessInstanceId(procInstId);
            cmd = abortCmd;
            break;
        case GET:
            if( corrKey != null ) { 
                GetProcessInstanceByCorrelationKeyCommand getCmd = new GetProcessInstanceByCorrelationKeyCommand();
                getCmd.setCorrelationKey(convertWebServiceCorrelationKeyToKeyCorrelationKey(correlationId, corrKey));
                cmd = getCmd;
            } else { 
                checkProcessInstanceId(correlationId, procInstId);
                GetProcessInstanceCommand getCmd = new GetProcessInstanceCommand();
                getCmd.setProcessInstanceId(procInstId); 
                getCmd.setReadOnly(true);
                cmd = getCmd;
            }
            break;
        case SIGNAL:
            SignalEventCommand signalCmd = new SignalEventCommand();
            if( corrKey != null ) { 
                signalCmd.setCorrelationKey(convertWebServiceCorrelationKeyToKeyCorrelationKey(correlationId, corrKey));
            } else { 
                checkProcessInstanceId(correlationId, procInstId);
                signalCmd.setProcessInstanceId(procInstId);
            }
            String eventType = request.getEventType();
            if( eventType == null || eventType.isEmpty() ) { 
                throwException("Invalid event type when signalling process instance " + procInstId +": " + eventType, 
                        correlationId, ExceptionType.VALIDATION);
            }
            signalCmd.setEventType(eventType);
            try {
                Object event = jaxbAdapter.unmarshal(request.getEvent());
                signalCmd.setEvent(event);
                cmd = signalCmd;
            } catch( Exception e ) {
                throwException("Unable to unmarshal event in request: " + e.getMessage(), correlationId, ExceptionType.VALIDATION, e);
            }
            break;
        case START:
            String procDefId = request.getProcessDefinitionId();
            if( StringUtils.isEmpty(procDefId) ) { 
               throwException("Empty process definition id", correlationId, ExceptionType.VALIDATION); 
            }
            Map<String, Object> params = null;
            if( request.getParameters() != null ) { 
                try {
                    params = (Map<String, Object>) jaxbAdapter.unmarshal(request.getParameters());
                } catch( Exception e ) {
                    throwException("Unable to unmarshal event in request: " + e.getMessage(), correlationId, ExceptionType.VALIDATION, e);
                }
            }
            if( corrKey != null ) { 
               StartCorrelatedProcessCommand startCmd = new StartCorrelatedProcessCommand();
               startCmd.setCorrelationKey(convertWebServiceCorrelationKeyToKeyCorrelationKey(correlationId, corrKey));
               startCmd.setParameters(params);
               cmd = startCmd;
            } else { 
                StartProcessCommand startCmd = new StartProcessCommand();
                startCmd.setProcessId(procDefId);
                startCmd.setParameters(params);
                cmd = startCmd;
            }
        break;
        default:
            throw new ProcessInstanceWebServiceException("Unknown operation type: " + oper.toString(), 
                    createFaultInfo(null, ExceptionType.VALIDATION));
        }
       
        Object result = null;
        try { 
            result = this.processRequestBean.doKieSessionOperation(cmd, deploymentId, 
                    getCorrelationKeyProperties(correlationId, corrKey), 
                    procInstId);
        } catch( Exception e ) { 
            throwException("Unable to do operation " + oper.toString(), correlationId, ExceptionType.SYSTEM, e);
        }
       
        ProcessInstanceResponse response = new ProcessInstanceResponse();
        if( (oper.equals(ProcessInstanceOperationType.GET) || oper.equals(ProcessInstanceOperationType.START)) && result != null ) { 
            ProcessInstance procInst = (ProcessInstance) result; 
            if( corrKey != null ) { 
               response.setCorrelationKey(corrKey); 
            }
            response.setProcessInstanceId(procInst.getId());
            response.setProcessId(procInst.getProcessId());
            response.setState(convertStateIntToState(correlationId, procInst.getState()));
            if( procInst.getEventTypes() != null ) { 
                for( String eventType : procInst.getEventTypes() ) { 
                    response.getEventTypes().add(eventType);
                }
            }
        } 
        return response;
    }

    private ProcessInstanceState convertStateIntToState(String correlationId, int state) throws ProcessInstanceWebServiceException{ 
       switch(state) { 
       case ProcessInstance.STATE_PENDING:
           return ProcessInstanceState.PENDING;
       case ProcessInstance.STATE_ACTIVE:
           return ProcessInstanceState.ACTIVE;
       case ProcessInstance.STATE_COMPLETED:
           return ProcessInstanceState.ACTIVE;
       case ProcessInstance.STATE_ABORTED:
           return ProcessInstanceState.ACTIVE;
       case ProcessInstance.STATE_SUSPENDED:
           return ProcessInstanceState.ACTIVE;
       default: 
           throwException("Unknown process instance state: " + state, correlationId, ExceptionType.SYSTEM);
           return null;
       }
    }
    
    private List<String> getCorrelationKeyProperties(String correlationId, CorrelationKey corrKey)  {
        if( corrKey == null || corrKey.getProperties() == null || corrKey.getProperties().getEntries() == null ) { 
            return null;
        }
        List<StringStringEntry> entries = corrKey.getProperties().getEntries();
        List<String> props = new ArrayList<String>(entries.size());
        for( StringStringEntry entry : entries ) { 
           props.add(entry.getValue());
        }
        return props;
    }
    
    private org.kie.internal.process.CorrelationKey convertWebServiceCorrelationKeyToKeyCorrelationKey(String correlationId, CorrelationKey corrKey) 
            throws ProcessInstanceWebServiceException { 
       CorrelationKeyFactory factory = KieInternalServices.Factory.get().newCorrelationKeyFactory(); 
       StringStringEntryList corrKeyProps = corrKey.getProperties();
       if( corrKeyProps == null ) { 
           throwException("A correlation key must contain at least 1 property", correlationId, ExceptionType.VALIDATION);
       }
       if( corrKeyProps.getEntries() == null || corrKeyProps.getEntries().isEmpty() ) { 
           throwException("A correlation key must contain at least 1 property", correlationId, ExceptionType.VALIDATION);
       }
       List<String> props = new ArrayList<String>(corrKeyProps.getEntries().size());
       for( StringStringEntry entry : corrKeyProps.getEntries() ) { 
          props.add(entry.getValue());
       }
       return factory.newCorrelationKey(props);
    }
        
    private void throwException(String message, String correlationId, ExceptionType type, Exception... e) throws ProcessInstanceWebServiceException { 
        WebServiceFaultInfo faultInfo = createFaultInfo(correlationId, type);
        if( e == null || e.length == 0 ) { 
            throw new ProcessInstanceWebServiceException(message, faultInfo);
        } 
        throw new ProcessInstanceWebServiceException(message, faultInfo, e[0]);
        
    }
    
    private void checkProcessInstanceId(String correlationId, Long procInstId) throws ProcessInstanceWebServiceException { 
       if( procInstId == null || procInstId.longValue() < 1 ) { 
           throw new ProcessInstanceWebServiceException("Invalid process instance id: " + procInstId, createFaultInfo(correlationId, ExceptionType.VALIDATION));
       }
    }
    
    @Override
    public void manageWorkItem( ManageWorkItemRequest request ) throws ProcessInstanceWebServiceException {
        // DBG Auto-generated method stub
        
    }

    @Override
    public ProcessInstanceVariableMessage manageProcessInstanceVariables( ProcessInstanceVariableMessage request )
            throws ProcessInstanceWebServiceException {
        // DBG Auto-generated method stub
        return null;
    }

    private WebServiceFaultInfo createFaultInfo(String correlationId, ExceptionType type) { 
       return new WebServiceFaultInfo(correlationId, type);
    }
}