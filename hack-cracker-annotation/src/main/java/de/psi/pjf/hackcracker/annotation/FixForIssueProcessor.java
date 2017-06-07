package de.psi.pjf.hackcracker.annotation;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Resolution;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
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
@SupportedAnnotationTypes({
    "de.psi.pjf.hackcracker.annotation.FixForIssue",
    "de.psi.pjf.hackcracker.annotation.FixForIssues"})
public class FixForIssueProcessor extends AbstractProcessor
{
    public FixForIssueProcessor()
    {
    }

    @Override
    public boolean process(
            Set<? extends TypeElement> aNnotations, RoundEnvironment aRoundEnv)
    {
        Stream.concat(
                aRoundEnv.getElementsAnnotatedWith(FixForIssues.class).stream(), 
                aRoundEnv.getElementsAnnotatedWith(FixForIssue.class).stream()
        ).forEach(this::processElement);
        return true;
    }

    private void processElement(Element e)
    {
        AnnotationMirror annotationMirror = getCorrectAnnotationMirror(e);
        try
        {
            FixForIssues fixForIssues = e.getAnnotation(FixForIssues.class);
            if(fixForIssues != null){
                if(checkIfMultipleIssuesAreResolved(fixForIssues)){
                    processingEnv.getMessager().printMessage(
                            ERROR, constructIssueMessage(fixForIssues), e, annotationMirror);
                }
            }else{
                FixForIssue fixForIssue = e.getAnnotation(FixForIssue.class);
                if (checkIssueIsResolved(fixForIssue))
                {
                    processingEnv.getMessager().printMessage(
                            ERROR, constructIssueMessage(fixForIssue), e, annotationMirror);
                }   
            }
        }
        catch (Exception ex)
        {
            processingEnv.getMessager().printMessage(
                    WARNING, "there where problems when checking issue: " + ex.getMessage(), e, annotationMirror);
        }
    }

    private AnnotationMirror getCorrectAnnotationMirror(Element e) {
        return getOptionalFixForIssuesAnnotationMirror(e).orElseGet(() -> getOptionalFixForIssueAnnotationMirror(e).get());
    }
    
    private Optional<AnnotationMirror> getOptionalFixForIssuesAnnotationMirror(Element e) {
        return e.getAnnotationMirrors().stream().filter(
                (AnnotationMirror aT) -> 
                        aT.getAnnotationType().asElement().getSimpleName()
                                .contentEquals(FixForIssues.class.getSimpleName())
        ).map(AnnotationMirror.class::cast).findAny();
    }

    private Optional<AnnotationMirror> getOptionalFixForIssueAnnotationMirror(Element e) {
        return e.getAnnotationMirrors().stream().filter(
                (AnnotationMirror aT) ->
                        aT.getAnnotationType().asElement().getSimpleName()
                                .contentEquals(FixForIssue.class.getSimpleName())
        ).map(AnnotationMirror.class::cast).findAny();
    }
    
    private String constructIssueMessage(FixForIssue fixInformation)
    {
        return "Issue " + fixInformation.url() + "browse/" + fixInformation.issue()
               + " has been already resolved - you should now remove your hack.";
    }
    
    private String constructIssueMessage(FixForIssues fixInformation) {
        String toReturn = fixInformation.needsAllIssuesResolved() ? "All issues :" : "At least one of issues: ";
        for (FixForIssue fixForIssue : fixInformation.value()) {
            toReturn = toReturn + "\n browse/" + fixForIssue.issue();
        }
        toReturn = toReturn + "\n has been already resolved - you should now remove your hack.";
        return toReturn;
    }
    
    private boolean checkIfMultipleIssuesAreResolved(FixForIssues fixInformation) throws URISyntaxException
    {
        if(fixInformation.needsAllIssuesResolved()){
            return Stream.of(fixInformation.value()).allMatch(this::checkIssueIsResolved);
        }else{
            return Stream.of(fixInformation.value()).anyMatch(this::checkIssueIsResolved);
        }
    }

    private boolean checkIssueIsResolved(FixForIssue fixInformation)
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

    private Issue getIssue(String aJiraUrl, String aIssue)
    {
        try {
            JiraRestClient connection = JiraConnectionsProvider.getConnection(aJiraUrl);
            if (connection != null)
            {
                return connection.getIssueClient().getIssue(aIssue).claim();
            }
            return null;
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex.getMessage(),ex);
        }
    }


}
