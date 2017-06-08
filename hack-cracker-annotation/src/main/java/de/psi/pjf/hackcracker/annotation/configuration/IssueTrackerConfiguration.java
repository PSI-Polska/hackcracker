package de.psi.pjf.hackcracker.annotation.configuration;

import de.psi.pjf.hackcracker.jira.JiraIssueChecker;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name="configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class IssueTrackerConfiguration
{
    @XmlTransient
    public static final IssueTrackerConfiguration CONFIGURATION = retrieveConfiguration();
    
    @XmlElement(name="jira")
    private List<JiraInstance> jiraInstances = new ArrayList<>();
    
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class JiraInstance{
        private String url;
        private String password;
        private String user;

        public String getUrl()
        {
            return url;
        }

        public String getPassword()
        {
            return password;
        }

        public String getUser()
        {
            return user;
        }
    }
    
    @XmlAttribute
    private boolean switchOff = false;
    
    public JiraInstance getForName(String name){
        return jiraInstances.stream().filter(i -> name.equals(i.getUrl())).findAny().orElse(null);
    }
    
    private static IssueTrackerConfiguration retrieveConfiguration(){
        IssueTrackerConfiguration configuration;
        try
        {
            JAXBContext jc = JAXBContext.newInstance(IssueTrackerConfiguration.class);
            String cfg = System.getProperty("hackcracker.configuration");
            cfg = cfg == null ? System.getProperty("user.home")+"/.hackcracker/issue_trackers.xml" : cfg;
            File configurationFile = new File(cfg);
            configuration = (IssueTrackerConfiguration) jc.createUnmarshaller().unmarshal(configurationFile);
        }
        catch (JAXBException ex)
        {
            Logger.getLogger(JiraIssueChecker.class.getName())
                    .log(Level.WARNING,"there was an exception when reading user issue tracker configuration",ex);
            configuration = new IssueTrackerConfiguration();
        }
        return configuration;
    }

    public boolean isSwitchOff() {
        return switchOff;
    }
    
}
