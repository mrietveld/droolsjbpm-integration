package org.kie.services.remote.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.drools.core.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.persistence.SingleSessionCommandService;
import org.jboss.resteasy.spi.InternalServerErrorException;
import org.jboss.resteasy.spi.UnauthorizedException;
import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.exception.PermissionDeniedException;
import org.kie.api.command.Command;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.TaskService;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.services.remote.cdi.DeploymentInfoBean;
import org.kie.services.remote.cdi.TransactionalExecutor;

/**
 * This class is used by both the {@link RuntimeResource} and {@link TaskResource} to do the core operations on
 * the Deployment/Runtime's {@link KieSession} and {@link TaskService}.
 * </p>
 * It contains the necessary logic to do the following:
 * <ul>
 * <li>Retrieve the KieSession or TaskService</li>
 * <li>Execute the submitted command</li>
 * </ul>
 * Transactional operations are delegated to <code>TransactionalExecutor</code> that will ensure transaction is
 * opened and closed within boundary of the executor invocation.
 */
@RequestScoped
public class RestProcessRequestBean {

    /* KIE processing */
    @Inject
    private DeploymentInfoBean runtimeMgrMgr;

    @Inject
    private TaskService taskService;

    @Inject
    private TransactionalExecutor executor;

    /**
     * Executes a command on the {@link KieSession} from the proper {@link RuntimeManager}. This method
     * ends up synchronizing around the retrieved {@link KieSession} in order to avoid race-conditions.
     * 
     * @param cmd The command to be executed.
     * @param deploymentId The id of the runtime.
     * @param processInstanceId The process instance id, if available.
     * @return The result of the {@link Command}.
     */
    public Object doKieSessionOperation(Command<?> cmd, String deploymentId, Long processInstanceId, String errorMsg) {
        Object result = null;
        try {
            RuntimeEngine runtimeEngine = runtimeMgrMgr.getRuntimeEngine(deploymentId, processInstanceId);
            KieSession kieSession = runtimeEngine.getKieSession();
            SingleSessionCommandService sscs 
                = (SingleSessionCommandService) ((CommandBasedStatefulKnowledgeSession) kieSession).getCommandService();
            synchronized (sscs) { 
                result = executor.execute(kieSession, cmd);
            }
        } catch (Exception e) {
            if( e instanceof RuntimeException ) { 
                throw (RuntimeException) e;
            } else {
                throw new InternalServerErrorException(errorMsg, e);
            }
        }
        return result;
    }

    
    /**
     * Executes a command on the injected {@link TaskService} instance.
     * </p>
     * Should be used only for commands that end up affecting the associated {@link KieSession}.
     * Since the {@link CompleteTaskCommand} ends up signalling the {@link KieSession}, we make sure to
     * first retrieve the proper {@link KieSession} in order to synchronize around it.
     * 
     * @param cmd The command to be executed.
     * @param errorMsg The message to be added to any (non-runtime) exceptions thrown. 
     * @param deploymentId The deployment id of the runtime. 
     * @return The result of the completed command.
     */
    public Object doTaskOperationOnDeployment(TaskCommand<?> cmd, String deploymentId, Long processInstanceId, String errorMsg) {
        Object result = null;
        try {
            if( deploymentId != null ) { 
                RuntimeEngine runtimeEngine = runtimeMgrMgr.getRuntimeEngine(deploymentId, processInstanceId);
                KieSession kieSession = runtimeEngine.getKieSession();
                SingleSessionCommandService sscs 
                    = (SingleSessionCommandService) ((CommandBasedStatefulKnowledgeSession) kieSession).getCommandService();
                synchronized (sscs) {
                    result = executor.execute((InternalTaskService) taskService, cmd);
                }
            } else {
                result = executor.execute((InternalTaskService) taskService, cmd);
            }
        } catch (PermissionDeniedException pde) {
            throw new UnauthorizedException(pde.getMessage(), pde);
        } catch (RuntimeException re) {
            throw re;
        } catch( Exception e ) { 
            throw new InternalServerErrorException(errorMsg, e);
        } 
        return result;
    }
    
    /**
     * Executes a command on the {@link TaskService} (without synchronizing around the {@link KieSession})
     * @param cmd The command to be executed. 
     * @param errorMsg The error message to be attached to any exceptions thrown. 
     * @return The result of the completed command. 
     */
    public Object doTaskOperation(TaskCommand<?> cmd, String errorMsg) {
        return doTaskOperationOnDeployment(cmd, null, null, errorMsg);
    }


    /**
     * Executes a {@link TaskCommand} on the {@link TaskService}: this should be used when the {@link TaskCommand}
     * returns an object instance that is also a (persistent) entity, and thus should also be serialized within a
     * transaction. 
     * 
     * @param cmd The {@link TaskCommand} to be executed. 
     * @param errorMsg The error message that should be associated with any eventual errors or exceptions. 
     * @return The result of the {@link TaskCommand}, possibly null.
     */
    public Object doTaskOperationAndSerializeResult(TaskCommand<?> cmd, String errorMsg) {
        Object result = null;
        try {
            result = executor.executeAndSerialize((InternalTaskService) taskService, cmd);
        } catch (PermissionDeniedException pde) {
            throw new UnauthorizedException(pde.getMessage(), pde);
        } catch (RuntimeException re) {
            throw re;
        } catch( Exception e ) { 
            throw new InternalServerErrorException(errorMsg, e);
        } 
        return result;
    }
}
