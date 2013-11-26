package org.kie.services.client.serialization.jaxb.impl.deploy;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.drools.core.util.StringUtils;


@XmlRootElement(name="primitive")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbDeploymentUnit {

    @XmlElement
    @XmlSchemaType(name="string")
    private String groupId;
    
    @XmlElement
    @XmlSchemaType(name="string")
    private String artifactId;
    
    @XmlElement
    @XmlSchemaType(name="string")
    private String version;
    
    @XmlElement
    @XmlSchemaType(name="string")
    private String kbaseName;
    
    @XmlElement
    @XmlSchemaType(name="string")
    private String ksessionName;

    @XmlElement(name = "deployment-strategy", type = JaxbRuntimeStrategy.class)
    private JaxbRuntimeStrategy strategy;
    
    @XmlEnum
    public static enum JaxbRuntimeStrategy { 
        SINGLETON,
        PER_REQUEST,
        PER_PROCESS_INSTANCE;
    }
    
    @XmlElement(name = "deployment-status", type = JaxbDeploymentStatus.class)
    private JaxbDeploymentStatus status;
    
    @XmlEnum
    public static enum JaxbDeploymentStatus { 
        DEPLOYING,
        DEPLOYED,
        UNDEPLOYING,
        NONEXISTENT;
    }
    
    public JaxbDeploymentUnit() { 
        // default for JAXB
    }
    
    public JaxbDeploymentUnit(String groupId, String artifactId, String version) { 
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }
    
    public JaxbDeploymentUnit(String groupId, String artifactId, String version, String kbaseName, String ksessionName) { 
        this(groupId, artifactId, version);
        this.kbaseName = kbaseName;
        this.ksessionName = ksessionName;
    }
    
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getKbaseName() {
        return kbaseName;
    }

    public void setKbaseName(String kbaseName) {
        this.kbaseName = kbaseName;
    }

    public String getKsessionName() {
        return ksessionName;
    }

    public void setKsessionName(String ksessionName) {
        this.ksessionName = ksessionName;
    }

    public JaxbRuntimeStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(JaxbRuntimeStrategy strategy) {
        this.strategy = strategy;
    }
    
    public JaxbDeploymentStatus getStatus() {
        return status;
    }

    public void setStatus(JaxbDeploymentStatus status) {
        this.status = status;
    }

    public String getIdentifier() {
        String id = getGroupId() + ":" + getArtifactId() + ":" + getVersion();
        boolean kbaseFilled = !StringUtils.isEmpty(kbaseName);
        boolean ksessionFilled = !StringUtils.isEmpty(ksessionName);
        if( kbaseFilled || ksessionFilled) {
            id = id.concat(":");
            if( kbaseFilled ) {
                id = id.concat(kbaseName);
            }
            if( ksessionFilled ) {
                id = id.concat(":" + ksessionName);
            }
        }
        return id;
    }
}
