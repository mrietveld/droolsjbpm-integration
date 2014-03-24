package org.kie.services.remote;

import static org.kie.services.remote.StartProcessEveryStrategyTest.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.ArrayList;

import javax.management.RuntimeErrorException;

import org.drools.core.command.runtime.process.StartCorrelatedProcessCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.jbpm.kie.services.impl.event.DeploymentEvent;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.runtime.manager.impl.PerProcessInstanceRuntimeManager;
import org.jbpm.runtime.manager.impl.PerRequestRuntimeManager;
import org.jbpm.runtime.manager.impl.RuntimeEngineImpl;
import org.jbpm.runtime.manager.impl.SingletonRuntimeManager;
import org.jbpm.services.task.commands.ClaimTaskCommand;
import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.ExitTaskCommand;
import org.jbpm.services.task.commands.GetTaskCommand;
import org.jbpm.services.task.commands.ReleaseTaskCommand;
import org.jbpm.services.task.commands.SkipTaskCommand;
import org.jbpm.services.task.commands.StartTaskCommand;
import org.jbpm.services.task.impl.model.TaskDataImpl;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.deployment.DeployedUnit;
import org.kie.internal.deployment.DeploymentUnit;
import org.kie.internal.deployment.DeploymentUnit.RuntimeStrategy;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.internal.task.api.model.InternalTask;
import org.kie.internal.task.api.model.InternalTaskData;
import org.kie.services.remote.cdi.DeploymentInfoBean;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

public class MockSetupTestHelper {

    public final static String USER = "user";
    public final static long TASK_ID = 1;
    public final static String DEPLOYMENT_ID = "deployment";
    
    public final static boolean FOR_INDEPENDENT_TASKS = true;
    public final static boolean FOR_PROCESS_TASKS = false;

    public static void setupTaskMocks(TaskDeploymentIdTest test, boolean independentTask) {
        // DeploymentInfoBean
        DeploymentInfoBean runtimeMgrMgrMock = spy(new DeploymentInfoBean());
        test.setRuntimeMgrMgrMock(runtimeMgrMgrMock);

        RuntimeEngine runtimeEngineMock = mock(RuntimeEngine.class);
        doReturn(runtimeEngineMock).when(runtimeMgrMgrMock).getRuntimeEngine(anyString(), anyLong());
        // - return mock of TaskService
        InternalTaskService runtimeTaskServiceMock = mock(InternalTaskService.class);
        test.setRuntimeTaskServiceMock(runtimeTaskServiceMock);
        // - throw exception if if you try to get a KieSession via RuntimeEngine
        doThrow(new IllegalStateException("ksession")).when(runtimeEngineMock).getKieSession();
        // - let disposeRuntimeEngine() happen.
        doNothing().when(runtimeMgrMgrMock).disposeRuntimeEngine(any(RuntimeEngine.class));

        // TaskService
        InternalTaskService injectedTaskServiceMock = mock(InternalTaskService.class);
        test.setInjectedTaskServiceMock(injectedTaskServiceMock);

        // - set task instance
        InternalTask task = new TaskImpl();
        task.setId(TASK_ID);
        task.setTaskData(new TaskDataImpl());

        doThrow(new IllegalStateException("Use the getTaskById() method, which should already have been called!"))
            .when(injectedTaskServiceMock).execute(any(GetTaskCommand.class));
        doReturn(task).when(injectedTaskServiceMock).getTaskById(TASK_ID);

        // task is independent
        if (independentTask) {
            // no deployment id == independent task
            ((InternalTaskData) task.getTaskData()).setDeploymentId(null);
            
            // runtime task engine should not be used
            doThrow(new IllegalStateException("The runtime engine TaskService should not be used here!")).when(runtimeEngineMock).getTaskService();

            // - injected task service should execute commands
            doReturn(null).when(injectedTaskServiceMock).execute(any(ClaimTaskCommand.class));
            doReturn(null).when(injectedTaskServiceMock).execute(any(StartTaskCommand.class));
            doReturn(null).when(injectedTaskServiceMock).execute(any(CompleteTaskCommand.class));
            doReturn(null).when(injectedTaskServiceMock).execute(any(ReleaseTaskCommand.class));
            doReturn(null).when(injectedTaskServiceMock).execute(any(ExitTaskCommand.class));
            doReturn(null).when(injectedTaskServiceMock).execute(any(SkipTaskCommand.class));
        } else {
            // deployment id available == process task
            ((InternalTaskData) task.getTaskData()).setDeploymentId(DEPLOYMENT_ID);
            
            // - injected task service should only be used to retrieve task
            doReturn(runtimeTaskServiceMock).when(runtimeEngineMock).getTaskService();
            
            // - runtime engine should execute commands affecting kiesssions
            doReturn(null).when(injectedTaskServiceMock).execute(any(ClaimTaskCommand.class));
            doReturn(null).when(runtimeTaskServiceMock).execute(any(StartTaskCommand.class));
            doReturn(null).when(runtimeTaskServiceMock).execute(any(CompleteTaskCommand.class));
            doReturn(null).when(runtimeTaskServiceMock).execute(any(ReleaseTaskCommand.class));
            doReturn(null).when(runtimeTaskServiceMock).execute(any(ExitTaskCommand.class));
            doReturn(null).when(runtimeTaskServiceMock).execute(any(SkipTaskCommand.class));
        }

        test.setupTestMocks();
    }

    public static void setupProcessMocks(StartProcessEveryStrategyTest test, RuntimeStrategy strategy) {
        // DeploymentInfoBean, runtime engine
        DeploymentInfoBean runtimeMgrMgr = new DeploymentInfoBean();
        test.setRuntimeMgrMgrMock(runtimeMgrMgr);
      
        // dep unit (with runtime mgr): 
        // - deployed classes
        DeployedUnit depUnitMock = mock(DeployedUnit.class);
        doReturn(new ArrayList<Class<?>>()).when(depUnitMock).getDeployedClasses();
        // - deployment unit
        DeploymentUnit realDepUnitMock = mock(DeploymentUnit.class);
        doReturn(realDepUnitMock).when(depUnitMock).getDeploymentUnit();
        // - runtime engine
        RuntimeEngineImpl runtimeEngineMock = mock(RuntimeEngineImpl.class);
        RuntimeManager runtimeMgrMock;
        switch(strategy) { 
        case PER_PROCESS_INSTANCE: 
            runtimeMgrMock = mock(PerProcessInstanceRuntimeManager.class);
            // this doesn't really do anything since there is no class/cast checking by mockito
            doReturn(runtimeEngineMock).when(runtimeMgrMock).getRuntimeEngine(any(ProcessInstanceIdContext.class));
            // throw exception is EmptyContext.get()
            mockStatic( EmptyContext.class );
            Mockito.when(EmptyContext.get()).thenThrow(new IllegalStateException("A ProcessInstanceIdContext is expected to be used here!"));
            break;
        case PER_REQUEST:
            runtimeMgrMock = mock(PerRequestRuntimeManager.class);
            doReturn(runtimeEngineMock).when(runtimeMgrMock).getRuntimeEngine(any(EmptyContext.class));
            break;
        case SINGLETON:
            runtimeMgrMock = mock(SingletonRuntimeManager.class);
            doReturn(runtimeEngineMock).when(runtimeMgrMock).getRuntimeEngine(any(EmptyContext.class));
            break;
        default: 
            throw new IllegalStateException("Unknown runtime strategy: " + strategy );
        }
        doReturn(runtimeMgrMock).when(depUnitMock).getRuntimeManager();
        doReturn(runtimeMgrMock).when(runtimeEngineMock).getManager();
        
        // add deployment unit
        runtimeMgrMgr.addOnDeploy(new DeploymentEvent(DEPLOYMENT_ID, depUnitMock));
        
        // ksession setup
        KieSession kieSessionMock = mock(KieSession.class);
        test.setKieSessionMock(kieSessionMock);
        doReturn(kieSessionMock).when(runtimeEngineMock).getKieSession();

        // start process
        RuleFlowProcessInstance procInst = new RuleFlowProcessInstance();
        procInst.setId(TEST_PROCESS_INST_ID);
        
        ProcessInstance procInstMock = spy(procInst);
        doReturn(procInstMock).when(kieSessionMock).execute(any(StartProcessCommand.class));
        
        // have test setup mocks
        test.setupTestMocks();
    }
}
