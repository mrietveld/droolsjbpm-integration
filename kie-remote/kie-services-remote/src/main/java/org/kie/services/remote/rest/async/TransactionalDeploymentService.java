package org.kie.services.remote.rest.async;

import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.jboss.seam.transaction.TransactionInterceptor;
import org.jboss.seam.transaction.Transactional;
import org.jbpm.kie.services.api.DeployedUnit;
import org.jbpm.kie.services.api.DeploymentUnit;
import org.jbpm.kie.services.api.Kjar;
import org.jbpm.kie.services.impl.KModuleDeploymentService;

@ApplicationScoped
public class TransactionalDeploymentService {

    @Inject
    @Kjar
    private KModuleDeploymentService delegate;
    
    @Transactional
    @Interceptors({TransactionInterceptor.class})
    public synchronized void deploy(DeploymentUnit unit) {
        delegate.deploy(unit);
    }

    @Transactional
    @Interceptors({TransactionInterceptor.class})
    public synchronized void undeploy(DeploymentUnit unit) {
        delegate.undeploy(unit);
    }

    public synchronized DeployedUnit getDeployedUnit(String deploymentUnitId) {
        return delegate.getDeployedUnit(deploymentUnitId);
    }

    public synchronized Collection<DeployedUnit> sgetDeployedUnits() {
        return delegate.getDeployedUnits();
    }

}
