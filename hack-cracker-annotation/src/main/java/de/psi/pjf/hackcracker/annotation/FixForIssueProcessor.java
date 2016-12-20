package de.psi.pjf.hackcracker.annotation;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Resolution;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import static javax.lang.model.SourceVersion.RELEASE_8;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.WARNING;

/**
 *
 * @author akedziora
 */
@SupportedSourceVersion(RELEASE_8)
@SupportedAnnotationTypes("de.psi.pjf.hackcracker.annotation.FixForIssue")
public class FixForIssueProcessor extends AbstractProcessor
{

    static final Logger log = getLogger(FixForIssueProcessor.class.getName());

    public FixForIssueProcessor()
    {
    }

    @Override
    public boolean process(
            Set<? extends TypeElement> aNnotations, RoundEnvironment aRoundEnv)
    {
        aRoundEnv.getElementsAnnotatedWith(FixForIssue.class).stream().forEach(this::processElement);
        return true;
    }

    private void processElement(Element e)
    {
        AnnotationMirror annotationMirror = e.getAnnotationMirrors().stream().filter(
                (AnnotationMirror aT) -> aT.getAnnotationType().asElement().getSimpleName().contentEquals(
                        FixForIssue.class.getSimpleName())).findAny().get();
        try
        {
            FixForIssue fixForIssue = e.getAnnotation(FixForIssue.class);
            if (checkIssueIsResolved(fixForIssue))
            {
                processingEnv.getMessager().printMessage(ERROR, constructIssueMessage(fixForIssue), e,
                                                         annotationMirror);
            }
        }
        catch (Exception ex)
        {
            processingEnv.getMessager().printMessage(WARNING, "there where problems when checking issue: "
                                                              + ex.getMessage(), e, annotationMirror);
        }
    }

    private String constructIssueMessage(FixForIssue fixInformation)
    {
        return "Issue " + fixInformation.url() + "browse/" + fixInformation.issue()
               + " has been already resolved - you should now remove your hack.";
    }

    private boolean checkIssueIsResolved(FixForIssue fixInformation) throws URISyntaxException
    {
        String jiraUrl = fixInformation.url();
        String issue = fixInformation.issue();
        return checkIssueResolvedStatus(getIssue(jiraUrl, issue));
    }

    private boolean checkIssueResolvedStatus(Issue issue)
    {
        return issue != null && resolutionInResolved(issue.getResolution());
    }

    private boolean resolutionInResolved(Resolution r)
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

    private Issue getIssue(String aJiraUrl, String aIssue) throws URISyntaxException
    {
        JiraRestClient connection = JiraConnectionsProvider.getConnection(aJiraUrl);
        if (connection != null)
        {
            return connection.getIssueClient().getIssue(aIssue).claim();
        }
        return null;
    }

}
