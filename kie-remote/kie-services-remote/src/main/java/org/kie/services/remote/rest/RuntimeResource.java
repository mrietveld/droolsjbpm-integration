package org.kie.services.remote.rest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import org.drools.core.command.runtime.process.AbortProcessInstanceCommand;
import org.drools.core.command.runtime.process.AbortWorkItemCommand;
import org.drools.core.command.runtime.process.CompleteWorkItemCommand;
import org.drools.core.command.runtime.process.GetProcessInstanceCommand;
import org.drools.core.command.runtime.process.GetWorkItemCommand;
import org.drools.core.command.runtime.process.SignalEventCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.process.instance.WorkItem;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.process.audit.command.FindProcessInstanceCommand;
import org.jbpm.process.audit.command.FindVariableInstancesCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceListResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceWithVariablesResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbWorkItem;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;
import org.kie.services.remote.cdi.ProcessRequestBean;
import org.kie.services.remote.rest.exception.RestOperationException;

/**
 * If a method in this class is annotated by a @Path annotation, 
 * then the name of the method should match the URL specified in the @Path, 
 * where "_" characters should be used for all "/" characters in the path. 
 * <p>
 * For example: 
 * <pre>
 * @Path("/begin/{varOne: [_a-zA-Z0-9-:\\.]+}/midddle/{varTwo: [a-z]+}")
 * public void begin_varOne_middle_varTwo() { 
 * </pre>
 * 
 * If the method is annotated by the @Path anno, but is the "root", then
 * give it a name that explains it's function.
 */
@Path("/runtime/{deploymentId: [\\w\\.-]+(:[\\w\\.-]+){2,2}(:[\\w\\.-]*){0,2}}")
@RequestScoped
@SuppressWarnings("unchecked")
public class RuntimeResource extends ResourceBase {

    /* REST information */
    @Context
    protected HttpHeaders headers;
    
    @Context
    protected UriInfo uriInfo;
    
    @Context
    private Request restRequest;

    /* KIE information and processing */
    @Inject
    protected ProcessRequestBean processRequestBean;
    
    @PathParam("deploymentId")
    protected String deploymentId;

    // Backwards compatability
    
    @Inject
    private HistoryResource historyResource; 
    
    // Rest methods --------------------------------------------------------------------------------------------------------------

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("/execute")
    public JaxbCommandsResponse execute(JaxbCommandsRequest cmdsRequest) {
        return restProcessJaxbCommandsRequest(cmdsRequest, processRequestBean);
    } 

    @POST
    @Path("/process/{processDefId: [_a-zA-Z0-9-:\\.]+}/start")
    public Response process_defId_start(@PathParam("processDefId") String processId) {
        Map<String, List<String>> requestParams = getRequestParams(uriInfo);
        String oper = getRelativePath(uriInfo);
        
        Map<String, Object> params = extractMapFromParams(requestParams, oper);
        Command<?> cmd = new StartProcessCommand(processId, params);

        Object result = processRequestBean.doKieSessionOperation(
                cmd, 
                deploymentId, 
                null,
                "Unable to start process with process definition id '" + processId + "'");

        JaxbProcessInstanceResponse responseObj = new JaxbProcessInstanceResponse((ProcessInstance) result, uriInfo.getRequestUri().toString());
        return createCorrectVariant(responseObj, headers);
    }

    @GET
    @Path("/process/instance/{procInstId: [0-9]+}")
    public Response process_instance_procInstId(@PathParam("procInstId") Long procInstId) {
        Command<?> cmd = new GetProcessInstanceCommand(procInstId);
        ((GetProcessInstanceCommand) cmd).setReadOnly(true);
        
        Object result = processRequestBean.doKieSessionOperation(
                cmd, 
                deploymentId, 
                procInstId, 
                "Unable to get process instance " + procInstId);
        
        Object responseObj = null;
        if (result != null) {
            responseObj = new JaxbProcessInstanceResponse((ProcessInstance) result);
            return createCorrectVariant(responseObj, headers);
        } else {
            throw RestOperationException.notFound("Unable to retrieve process instance " + procInstId
                    + " which may have been completed. Please see the history operations.");
        }
    }

    @POST
    @Path("/process/instance/{procInstId: [0-9]+}/abort")
    public Response process_instance_procInstId_abort(@PathParam("procInstId") Long procInstId) {
        Command<?> cmd = new AbortProcessInstanceCommand();
        ((AbortProcessInstanceCommand) cmd).setProcessInstanceId(procInstId);
        
        processRequestBean.doKieSessionOperation(
                cmd, 
                deploymentId, 
                procInstId,
                "Unable to abort process instance " + procInstId);
                
        return createCorrectVariant(new JaxbGenericResponse(uriInfo.getRequestUri().toString()), headers);
    }

    @POST
    @Path("/process/instance/{procInstId: [0-9]+}/signal")
    public Response process_instance_procInstId_signal(@PathParam("procInstId") Long procInstId) {
        String oper = getRelativePath(uriInfo);
        Map<String, List<String>> params = getRequestParams(uriInfo);
        String eventType = getStringParam("signal", true, params, oper);
        Object event = getObjectParam("event", false, params, oper);
        Command<?> cmd = new SignalEventCommand(procInstId, eventType, event);
        
        String errorMsg = "Unable to signal process instance";
        if( eventType == null ) { 
            errorMsg += " with empty signal";
        } else { 
            errorMsg += " with signal type '" + eventType + "'";
        }
        if( event != null ) { 
            errorMsg += " and event '" + event + "'";
        }

        processRequestBean.doKieSessionOperation(cmd, deploymentId, procInstId, errorMsg);
        return createCorrectVariant(new JaxbGenericResponse(uriInfo.getRequestUri().toString()), headers);

    }

    @GET
    @Path("/process/instance/{procInstId: [0-9]+}/variable/{varName: [\\w\\.-]+}")
    public Response process_instance_procInstId_variable_varName(@PathParam("procInstId") Long procInstId,
            @PathParam("varName") String varName) {
        Object procVar =  processRequestBean.getVariableObjectInstanceFromRuntime(deploymentId, procInstId, varName);
     
        // serialize
        QName rootElementName = getRootElementName(procVar); 
        @SuppressWarnings("rawtypes") // unknown at compile time, dynamic from deployment
        JAXBElement<?> jaxbElem = new JAXBElement(rootElementName, procVar.getClass(), procVar) ;
        
        // return
        return createCorrectVariant(jaxbElem, headers);
    }
  
    protected QName getRootElementName(Object object) { 
        boolean xmlRootElemAnnoFound = false;
        Class<?> objClass = object.getClass();
        
        // This usually doesn't work in the kie-wb/bpms environment, see comment below
        XmlRootElement xmlRootElemAnno = objClass.getAnnotation(XmlRootElement.class);
        logger.debug("Getting XML root element annotation for " + object.getClass().getName());
        if( xmlRootElemAnno != null ) { 
            xmlRootElemAnnoFound = true;
            return new QName(xmlRootElemAnno.name());
        } else { 
            /**
             * There seem to be weird classpath issues going on here, probably related
             * to the fact that kjar's have their own classloader..
             * (The XmlRootElement class can't be found in the same classpath as the
             * class from the Kjar)
             */
            for( Annotation anno : objClass.getAnnotations() ) { 
                Class<?> annoClass = anno.annotationType();
                // we deliberately compare *names* and not classes because it's on a different classpath!
                if( XmlRootElement.class.getName().equals(annoClass.getName()) ) { 
                    xmlRootElemAnnoFound = true;
                    try {
                        Method nameMethod = annoClass.getMethod("name");
                        Object nameVal = nameMethod.invoke(anno);
                        if( nameVal instanceof String ) { 
                            return new QName((String) nameVal); 
                        }
                    } catch (Exception e) {
                        throw RestOperationException.internalServerError("Unable to retrieve XmlRootElement info via reflection", e);
                    } 
                }
            }
            if( ! xmlRootElemAnnoFound ) { 
                String errorMsg = "Unable to serialize " + object.getClass().getName() + " instance "
                        + "because it is missing a " + XmlRootElement.class.getName() + " annotation with a name value.";
                throw RestOperationException.internalServerError(errorMsg);
            }
            return null;
        }
    }
   
    @POST
    @Path("/signal")
    public Response signal() {
        String oper = getRelativePath(uriInfo);
        Map<String, List<String>> requestParams = getRequestParams(uriInfo);
        String eventType = getStringParam("signal", true, requestParams, oper);
        Object event = getObjectParam("event", false, requestParams, oper);
        String errorMsg = "Unable to send signal '" + eventType + "'";
        if( event != null ) { 
            errorMsg += " with event '" + event + "'";
        }

        processRequestBean.doKieSessionOperation(
                new SignalEventCommand(eventType, event),
                deploymentId, 
                (Long) getNumberParam(PROC_INST_ID_PARAM_NAME, false, requestParams, oper, true),
                errorMsg);
        
        return createCorrectVariant(new JaxbGenericResponse(uriInfo.getRequestUri().toString()), headers);
    }

    @GET
    @Path("/workitem/{workItemId: [0-9-]+}")
    public Response workitem_workItemId(@PathParam("workItemId") Long workItemId) { 
        String oper = getRelativePath(uriInfo);
        WorkItem workItem = (WorkItem) processRequestBean.doKieSessionOperation(
                new GetWorkItemCommand(workItemId),
                deploymentId, 
                (Long) getNumberParam(PROC_INST_ID_PARAM_NAME, false, getRequestParams(uriInfo), oper, true),
                "Unable to get work item " +  workItemId);
        return createCorrectVariant(new JaxbWorkItem(workItem), headers);
    }
    
    @POST
    @Path("/workitem/{workItemId: [0-9-]+}/{oper: [a-zA-Z]+}")
    public Response worktiem_workItemId_oper(@PathParam("workItemId") Long workItemId, @PathParam("oper") String operation) {
        String oper = getRelativePath(uriInfo);
        Map<String, List<String>> params = getRequestParams(uriInfo);
        Command<?> cmd = null;
        if ("complete".equalsIgnoreCase((operation.trim()))) {
            Map<String, Object> results = extractMapFromParams(params, operation);
            cmd = new CompleteWorkItemCommand(workItemId, results);
        } else if ("abort".equalsIgnoreCase(operation.toLowerCase())) {
            cmd = new AbortWorkItemCommand(workItemId);
        } else {
            throw RestOperationException.badRequest("Unsupported operation: " + oper);
        }
        
        processRequestBean.doKieSessionOperation(
                cmd, 
                deploymentId, 
                (Long) getNumberParam(PROC_INST_ID_PARAM_NAME, false, params, oper, true),
                "Unable to " + operation + " work item " +  workItemId);
        return createCorrectVariant(new JaxbGenericResponse(uriInfo.getRequestUri().toString()), headers);
    }

    /**
     * History methods
     */
    
    @POST
    @Path("/history/clear")
    @Deprecated
    public Response history_clear() {
        return historyResource.clear();
    }

    @GET
    @Path("/history/instances")
    @Deprecated
    public Response history_instances() {
        return historyResource.instances();
    }

    @GET
    @Path("/history/instance/{procInstId: [0-9]+}")
    @Deprecated // Delete with 6.1.0
    public Response history_instance_procInstId(@PathParam("procInstId") long procInstId) {
        return historyResource.instance_procInstId(procInstId);
    }

    @GET
    @Path("/history/instance/{procInstId: [0-9]+}/{oper: [a-zA-Z]+}")
    @Deprecated // Delete with 6.1.0
    public Response history_instance_procInstid_oper(@PathParam("procInstId") Long procInstId, @PathParam("oper") String operation) {
        return historyResource.instance_procInstid_oper(procInstId, operation);
    }

    @GET
    @Path("/history/instance/{procInstId: [0-9]+}/{oper: [a-zA-Z]+}/{logId: [a-zA-Z0-9-:\\._]+}")
    @Deprecated // Delete with 6.1.0
    public Response history_instance_procInstId_oper_logId(@PathParam("procInstId") Long procInstId,
            @PathParam("oper") String operation, @PathParam("logId") String logId) {
        return historyResource.instance_procInstId_oper_logId(procInstId, operation, logId);
    }

    @GET
    @Path("/history/process/{processDefId: [a-zA-Z0-9-:\\._]+}")
    @Deprecated // Delete with 6.1.0
    public Response history_process_procDefId(@PathParam("processDefId") String processId) {
       return historyResource.process_procDefId(processId); 
    }

    @GET
    @Path("/history/variable/{varId: [a-zA-Z0-9-:\\._]+}")
    @Deprecated // Delete with 6.1.0
    public Response history_variable_varId(@PathParam("varId") String variableId) {
        return historyResource.variable_varId(variableId);
    }
    
    @GET
    @Path("/history/variable/{varId: [a-zA-Z0-9-:\\._]+}/value/{value: [a-zA-Z0-9-:\\._]+}")
    @Deprecated // Delete with 6.1.0
    public Response history_variable_varId_value_valueVal(@PathParam("varId") String variableId, @PathParam("value") String value) {
       return historyResource.variable_varId_value_valueVal(variableId, value);
    }
    
    @GET
    @Path("/history/variable/{varId: [a-zA-Z0-9-:\\._]+}/instances")
    @Deprecated // Delete with 6.1.0
    public Response history_variable_varId_instances(@PathParam("varId") String variableId) {
        return historyResource.variable_varId_instances(variableId);
    }
    
    @GET
    @Path("/history/variable/{varId: [a-zA-Z0-9-:\\.]+}/value/{value: [a-zA-Z0-9-:\\._]+}/instances")
    @Deprecated // Delete with 6.1.0
    public Response history_variable_varId_value_valueVal_instances(@PathParam("procId") String variableId, @PathParam("value") String value) {
        return historyResource.variable_varId_value_valueVal_instances(variableId, value);
    }

    /**
     * WithVars methods
     */
    
    @POST
    @Path("/withvars/process/{processDefId: [_a-zA-Z0-9-:\\.]+}/start")
    public Response withvars_process_processDefId_start(@PathParam("processDefId") String processId) {
        Map<String, List<String>> requestParams = getRequestParams(uriInfo);
        String oper = getRelativePath(uriInfo);
        Map<String, Object> params = extractMapFromParams(requestParams, oper );

        Object result = processRequestBean.doKieSessionOperation(
                new StartProcessCommand(processId, params),
                deploymentId, 
                (Long) getNumberParam(PROC_INST_ID_PARAM_NAME, false, requestParams, oper, true),
                "Unable to get process instance logs for process '" + processId + "'");
        
        ProcessInstance procInst = (ProcessInstance) result;
        
        Map<String, String> vars = getVariables(procInst.getId());
        JaxbProcessInstanceWithVariablesResponse resp = new JaxbProcessInstanceWithVariablesResponse(procInst, vars, uriInfo.getRequestUri().toString());
        
        return createCorrectVariant(resp, headers);
    }

    @GET
    @Path("/withvars/process/instance/{procInstId: [0-9]+}")
    public Response withvars_process_instance_procInstId(@PathParam("procInstId") Long procInstId) {
        
        ProcessInstance procInst = getProcessInstance(procInstId);
        Map<String, String> vars = getVariables(procInstId);
        JaxbProcessInstanceWithVariablesResponse responseObj = new JaxbProcessInstanceWithVariablesResponse(procInst, vars, uriInfo.getRequestUri().toString());
        
        return createCorrectVariant(responseObj, headers);
    }

    @POST
    @Path("/withvars/process/instance/{procInstId: [0-9]+}/signal")
    public Response withvars_process_instance_procInstid_signal(@PathParam("procInstId") Long procInstId) {
        String oper = getRelativePath(uriInfo);
        Map<String, List<String>> params = getRequestParams(uriInfo);
        String eventType = getStringParam("signal", true, params, oper);
        Object event = getObjectParam("event", false, params, oper);
        String errorMsg = "Unable to signal process instance " + procInstId;
        if( eventType == null ) { 
            errorMsg += " with empty signal";
        } else { 
            errorMsg += " with signal type '" + eventType + "'";
        }
        if( event != null ) { 
            errorMsg += " and event '" + event + "'";
        }
        
        processRequestBean.doKieSessionOperation(
                new SignalEventCommand(procInstId, eventType, event),
                deploymentId, 
                procInstId,
                errorMsg);
        
        ProcessInstance processInstance = getProcessInstance(procInstId);
        Map<String, String> vars = getVariables(processInstance.getId());
        
        return createCorrectVariant(new JaxbProcessInstanceWithVariablesResponse(processInstance, vars), headers);
    }

    // Helper methods --------------------------------------------------------------------------------------------------------------

    private JaxbProcessInstanceListResponse getProcessInstanceListResponse(List<VariableInstanceLog> varLogList, int [] pageInfo) { 
        JaxbProcessInstanceListResponse response = new JaxbProcessInstanceListResponse();
        response.setCommandName(FindProcessInstanceCommand.class.getSimpleName());
        
        int numVarLogs = varLogList.size();
        int numResults = pageInfo[PAGE_NUM]*pageInfo[PAGE_SIZE];
        int numProcInsts = 0;
        
        for( int i = 0; i < numVarLogs && numProcInsts < numResults; ++i ) { 
            long procInstId = varLogList.get(i).getProcessInstanceId();
            Object procInstResult = getProcessInstance(procInstId);
            if( procInstResult != null ) { 
                response.getResult().add(new JaxbProcessInstanceResponse((ProcessInstance) procInstResult));
                ++numProcInsts;
            }
        }
        return response;
    }

    private ProcessInstance getProcessInstance(long procInstId) { 
        Command<?> cmd = new GetProcessInstanceCommand(procInstId);
        ((GetProcessInstanceCommand) cmd).setReadOnly(true);
        Object procInstResult = processRequestBean.doKieSessionOperation(
                cmd,
                deploymentId, 
                procInstId,
                "Unable to get process instance with id id '" + procInstId + "'");
        
        if( procInstResult == null ) { 
            throw RestOperationException.notFound("This method can only be used on processes that are still active.");
        }
        return (ProcessInstance) procInstResult;
    }
    
    private Map<String, String> getVariables(long processInstanceId) {
        Object result = processRequestBean.doKieSessionOperation(
                new FindVariableInstancesCommand(processInstanceId),
                deploymentId, 
                processInstanceId,
                "Unable to retrieve process variables from process instance " + processInstanceId);
        List<VariableInstanceLog> varInstLogList = (List<VariableInstanceLog>) result;
        
        Map<String, String> vars = new HashMap<String, String>();
        if( varInstLogList.isEmpty() ) { 
            return vars;
        }
        
        Map<String, VariableInstanceLog> varLogMap = new HashMap<String, VariableInstanceLog>();
        for( VariableInstanceLog varLog: varInstLogList ) {
            String varId = varLog.getVariableId();
            VariableInstanceLog prevVarLog = varLogMap.put(varId, varLog);
            if( prevVarLog != null ) { 
                if( prevVarLog.getDate().after(varLog.getDate()) ) { 
                  varLogMap.put(varId, prevVarLog);
                } 
            }
        }
        
        for( Entry<String, VariableInstanceLog> varEntry : varLogMap.entrySet() ) { 
            vars.put(varEntry.getKey(), varEntry.getValue().getValue());
        }
            
        return vars;
    }

}
