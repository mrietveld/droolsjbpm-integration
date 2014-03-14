package org.kie.services.client.serialization.jaxb.impl.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.jbpm.services.task.impl.model.xml.JaxbTaskSummary;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.kie.api.command.Command;
import org.kie.api.task.model.TaskSummary;
import org.kie.services.client.serialization.jaxb.impl.AbstractJaxbCommandResponse;

@XmlRootElement(name="task-summary-list")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbTaskSummaryListResponse extends AbstractJaxbCommandResponse<List<TaskSummary>> {

    @XmlElements({
        @XmlElement(name="task-summary",type=JaxbTaskSummary.class)
    })
    @JsonTypeInfo(defaultImpl=TaskSummaryImpl.class, use=Id.CLASS)
    private List<TaskSummary> taskSummaryList;

    public JaxbTaskSummaryListResponse() { 
        this.taskSummaryList = new ArrayList<TaskSummary>();
    }
    
    public JaxbTaskSummaryListResponse(Collection<TaskSummary> taskSummaryCollection) { 
       this.taskSummaryList = convertToJaxbTaskSummaryList(taskSummaryCollection);
    }
    
    public JaxbTaskSummaryListResponse(List<TaskSummary> taskSummaryCollection, int i, Command<?> cmd ) { 
        super(i, cmd);
       this.taskSummaryList = convertToJaxbTaskSummaryList(taskSummaryCollection);
    }
    
    private List<TaskSummary> convertToJaxbTaskSummaryList(Collection<TaskSummary> list) {
        if( list == null || list.isEmpty() ) { 
            return new ArrayList<TaskSummary>();
        }
        List<TaskSummary> newList = new ArrayList<TaskSummary>(list.size());
        Iterator<TaskSummary> iter = list.iterator();
        while(iter.hasNext()) { 
            TaskSummary taskSum = iter.next();
            if( taskSum instanceof JaxbTaskSummary ) { 
                newList.add(taskSum);
            } else { 
                newList.add(new JaxbTaskSummary(taskSum));
            }
        }
        return newList;
    }
    
    
    @Override
    public List<TaskSummary> getResult() {
        return taskSummaryList;
    }

    @Override
    public void setResult(List<TaskSummary> result) {
        this.taskSummaryList = result;
    }

    @JsonTypeInfo(defaultImpl=TaskSummaryImpl.class, use=Id.CLASS)
    public List<TaskSummary> getList() {
        return taskSummaryList;
    }

    @JsonTypeInfo(defaultImpl=TaskSummaryImpl.class, use=Id.CLASS)
    public void setList(List<TaskSummary> result) {
        this.taskSummaryList = result;
    }

}
