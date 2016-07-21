/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.remote.ws.integration.cxf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessBean;
import javax.jws.WebService;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;

/**
 * Apache CXF portable CDI extension to support initialization of JAX-WS resources.  
 */
public class JAXWSCdiResourceExtension implements Extension {    
    private Bean< ? > busBean;
    private Bus bus;
    
    private final List< Bean< ? > > serviceBeans = new ArrayList< Bean< ? > >();
        
    public <T> void collect(@Observes final ProcessBean< T > event) {
        if (event.getAnnotated().getAnnotations().contains(WebService.class) ) { 
            serviceBeans.add(event.getBean());
        } else if (CdiBusBean.CXF.equals(event.getBean().getName()) 
                && Bus.class.isAssignableFrom(event.getBean().getBeanClass())) {
            busBean = event.getBean();  
        }
    }
    
    public void load(@Observes final AfterDeploymentValidation event, final BeanManager beanManager) {
        bus = (Bus)beanManager.getReference(
            busBean, 
            busBean.getBeanClass(), 
            beanManager.createCreationalContext(busBean)
        );
        
        for (final Bean< ? > application: serviceBeans) {
            final WebService instance = (WebService)beanManager.getReference(
                application, 
                application.getBeanClass(), 
                beanManager.createCreationalContext(application) 
            );
            
            // If there is an application without any singletons and classes defined, we will
            // create a server factory bean with all services and providers discovered. 
            if (instance.
                    .getSingletons().isEmpty() && instance.getClasses().isEmpty()) {                            
                final JaxWsServerFactoryBean factory = createFactoryInstance(instance,         
                    loadServices(beanManager), loadProviders(beanManager));
                factory.init();   
            } else {
                // If there is an application with any singletons or classes defined, we will
                // create a server factory bean with only application singletons and classes.
                final JaxWsServerFactoryBean factory = createFactoryInstance(instance);
                factory.init();  
            }
        }
    }

    public void injectBus(@Observes final AfterBeanDiscovery event, final BeanManager beanManager) {
        if (busBean == null) {
            final AnnotatedType< ExtensionManagerBus > busAnnotatedType = 
                beanManager.createAnnotatedType(ExtensionManagerBus.class);
               
            final InjectionTarget<ExtensionManagerBus> busInjectionTarget = 
                beanManager.createInjectionTarget(busAnnotatedType);
               
            busBean = new CdiBusBean(busInjectionTarget);
            event.addBean(busBean);
        } 
    }
    
    /**
     * Create the JAXRSServerFactoryBean from the application and all discovered service and provider instances.
     * @param application application instance
     * @param services all discovered services
     * @param providers all discovered providers
     * @return JAXRSServerFactoryBean instance
     */
    private JaxWsServerFactoryBean createFactoryInstance(final Application application, final List< ? > services,
            final List< ? > providers) {        
        
        final JaxWsServerFactoryBean instance = ResourceUtils.createApplication(application, false, false);          

        instance.setServiceBeans(new ArrayList< Object >(services));
        instance.setProviders(providers);
        instance.setProviders(loadExternalProviders());
        instance.setBus(bus);                  
        
        return instance; 
    }
    
    /**
     * Create the JAXRSServerFactoryBean from the objects declared by application itself.
     * @param application application instance
     * @return JAXRSServerFactoryBean instance
     */
    private JAXRSServerFactoryBean createFactoryInstance(final Application application) {
        
        final JAXRSServerFactoryBean instance = ResourceUtils.createApplication(application, false, false);
        final Map< Class< ? >, List< Object > > classified = classifySingletons(application.getSingletons());
        instance.setServiceBeans(classified.get(Path.class));
        instance.setProviders(classified.get(Provider.class));
        instance.setFeatures(CastUtils.cast(classified.get(Feature.class), Feature.class));
        instance.setBus(bus);

        return instance; 
    }
    
    /**
     * JAX-RS application has defined singletons as being instances of any providers, resources and features.
     * In the JAXRSServerFactoryBean, those should be split around several method calls depending on instance
     * type. At the moment, only the Feature is CXF-specific and should be replaced by JAX-RS Feature implementation.
     * @param singletons application singletons
     * @return classified singletons by instance types
     */
    private Map< Class< ? >, List< Object > > classifySingletons(final Collection< Object > singletons) {
        final Map< Class< ? >, List< Object > > classified = 
            new HashMap< Class< ? >, List< Object > >();
        
        classified.put(Feature.class, new ArrayList< Object >());
        classified.put(Provider.class, new ArrayList< Object >());
        classified.put(Path.class, new ArrayList< Object >());
        
        for (final Object singleton: singletons) {
            if (singleton instanceof Feature) {
                classified.get(Feature.class).add(singleton);
            } else if (singleton.getClass().isAnnotationPresent(Provider.class)) {
                classified.get(Provider.class).add(singleton);
            } else if (singleton.getClass().isAnnotationPresent(Path.class)) {
                classified.get(Path.class).add(singleton);
            }
        }
        
        return classified;
    }
    
    /**
     * Gets the references for all discovered JAX-WS providers 
     * @param beanManager bean manager instance
     * @return the references for all discovered JAX-WS providers 
     */
    private List< Object > loadServices(final BeanManager beanManager) {
        final List< Object > services = new ArrayList< Object >();
        
        for (final Bean< ? > bean: serviceBeans) {
            services.add(
                beanManager.getReference(
                    bean, 
                    bean.getBeanClass(), 
                    beanManager.createCreationalContext(bean) 
                )
            );    
        }
        
        return services;
    }
}
