package de.psi.pjf.hackcracker.jira;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Resolution;
import com.atlassian.jira.rest.client.auth.AnonymousAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.google.common.base.Preconditions;
import de.psi.pjf.hackcracker.annotation.FixForIssue;
import static de.psi.pjf.hackcracker.annotation.FixForIssue.IssueTrackerType.JIRA;
import de.psi.pjf.hackcracker.annotation.configuration.IssueTrackerConfiguration;
import de.psi.pjf.hackcracker.annotation.configuration.IssueTrackerConfiguration.JiraInstance;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.slf4j.LoggerFactory;

/**
 *
 * @author akedziora
 */
public class JiraIssueChecker
{

    private static final Map<String, JiraRestClient> CONNECTION_CACHE = new HashMap<>();
    private static final JiraRestClientFactory FACTORY = new AsynchronousJiraRestClientFactory();
    private static final IssueTrackerConfiguration CONFIGURATION = IssueTrackerConfiguration.CONFIGURATION;
    
    public static boolean checkIssueResolved(FixForIssue issueInformation)
    {
        Preconditions.checkArgument(issueInformation.trackerType().equals(JIRA));
        return runWithLoggersOff(() -> {
            Optional<Issue> issue = getIssueOptional(issueInformation);
            return issue.isPresent() && resolutionInResolved(issue.get().getResolution());
        });
    }

    private static Optional<Issue> getIssueOptional(FixForIssue issueInformation) throws URISyntaxException {
        return getConnection(issueInformation.url())
                .map((JiraRestClient c) -> c.getIssueClient().getIssue(issueInformation.issue()).claim());
    }
    
    private static <T> T runWithLoggersOff(Callable<T> toRun){
        return runWithLoggersOff(
                toRun, 
                "com.atlassian.jira.rest.client.internal.async.AsynchronousHttpClientFactory$MavenUtils",
                "com.atlassian.httpclient.apache.httpcomponents.cache.FlushableHttpCacheStorageImpl");
    }
    
    private static <T> T runWithLoggersOff(Callable<T> toRun, String ... loggers){
        if(loggers.length > 0){
            Logger logger = (Logger) LoggerFactory.getLogger(loggers[0]);        
            Level oldLevel = logger.getLevel();
            logger.setLevel(Level.OFF);
            try {
                return runWithLoggersOff(toRun, Arrays.copyOfRange(loggers, 1, loggers.length));
            } finally {
                logger.setLevel(oldLevel);
            }
        }else{
            try {
                return toRun.call();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private static  boolean resolutionInResolved(Resolution r)
    {
        if (r == null)
        {
            return false;
        }
        switch (r.getName())
        {
            case "Done":
            case "Fixed": return true;
            default: return false;
        }
    }

    private static Optional<JiraRestClient> getConnection(String jiraUrl) throws URISyntaxException
    {
        JiraRestClient toReturn = CONNECTION_CACHE.get(jiraUrl);
        if (toReturn == null)
        {
            toReturn = connectTo(jiraUrl);
            CONNECTION_CACHE.put(jiraUrl, toReturn);
        }
        return Optional.ofNullable(toReturn);
    }

    private static JiraRestClient connectTo(String aJiraUrl) throws URISyntaxException {
        JiraInstance instance = CONFIGURATION.getForName(aJiraUrl);
        return instance == null
                ? connectToAnonymously(aJiraUrl)
                : connectToWithBasicAuthentication(instance.getUrl(), instance.getUser(), instance.getPassword());
    }

    private static JiraRestClient connectToWithBasicAuthentication(String aJiraUrl, String aUser,
                                                                   String aPassword) throws URISyntaxException
    {
        URI uri = new URI(aJiraUrl);
        JiraRestClient client = FACTORY.createWithBasicHttpAuthentication(uri, aUser, aPassword);
        return client;
    }

    private static JiraRestClient connectToAnonymously(String aJiraUrl) throws URISyntaxException
    {
        URI uri = new URI(aJiraUrl);
        JiraRestClient client = FACTORY.create(uri, new AnonymousAuthenticationHandler());
        return client;
    }

    public static String constructVerboseMessageForIssue(FixForIssue issueInformation) {
        Preconditions.checkArgument(issueInformation.trackerType().equals(JIRA));
        return runWithLoggersOff(() -> {
            Optional<Issue> issue = getIssueOptional(issueInformation);
            return issue.isPresent() ? constructVerboseMessageForIssue(issue.get()) : constructFailMessageForIssue(issueInformation) ;
        });
    }
    
    private static String constructVerboseMessageForIssue(Issue issue){
        return "Issue: "+issue.getKey()+" retrieved successfully \n"+
                "summary: "+issue.getSummary()+"\n"+
                "description: "+issue.getDescription()+"\n"+
                "resolution: "+(issue.getResolution() != null ? issue.getResolution().getName() : null)+"\n"+
                "created: "+issue.getCreationDate()+"\n"+
                "due date: "+issue.getDueDate()+"\n"+
                constructMessageForComments(issue.getComments())+"\n";
    }

    private static String constructFailMessageForIssue(FixForIssue issueInformation) {
        return "failed to retrieve issue: "
                +issueInformation.issue()
                +" form jira: "
                +issueInformation.url()
                +" most likely issue doesn't exist on it";
    }

    private static String constructMessageForComments(Iterable<Comment> comments) {
        String toReturn = "comments: \n";
        for (Comment comment : comments) {
            toReturn += "date: " + comment.getCreationDate() +  
                    (comment.getAuthor() != null ? " author: "+ comment.getAuthor().getDisplayName() : "")+"\n";
            toReturn += "comment: "+comment.getBody()+"\n\n";
        }
        return toReturn;
    }

}
