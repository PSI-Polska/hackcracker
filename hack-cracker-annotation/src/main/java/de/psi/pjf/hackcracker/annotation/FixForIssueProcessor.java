package de.psi.pjf.hackcracker.annotation;

import de.psi.pjf.hackcracker.jira.JiraIssueChecker;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
import static javax.tools.Diagnostic.Kind.MANDATORY_WARNING;
import static javax.tools.Diagnostic.Kind.WARNING;

/**
 *
 * @author akedziora
 */
@SupportedSourceVersion(RELEASE_8)
@SupportedAnnotationTypes({
    "de.psi.pjf.hackcracker.annotation.FixForIssue",
    "de.psi.pjf.hackcracker.annotation.FixForIssues",
    "de.psi.pjf.hackcracker.annotation.IgnoreIssueResolved"})
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
                Stream.concat(
                        aRoundEnv.getElementsAnnotatedWith(FixForIssue.class).stream(),
                        aRoundEnv.getElementsAnnotatedWith(IgnoreIssueResolved.class).stream()
                )
        ).distinct().forEach(this::processElement);
        return true;
    }

    private void processElement(Element e)
    {
        try
        {
            if(e.getAnnotation(IgnoreIssueResolved.class) == null){
                processElementWithoutIgnore(e);
            }else{
                processElementWithIgnore(e);
            }
        }
        catch (Exception ex)
        {
            processingEnv.getMessager().printMessage(
                    WARNING, "there where problems when checking issue: " + ex.getMessage(), e, getCorrectAnnotationMirror(e));
        }
    }

    private void processElementWithoutIgnore(Element e) throws URISyntaxException {
        FixForIssues fixForIssues = e.getAnnotation(FixForIssues.class);
        if(fixForIssues != null){
            if(checkIfMultipleIssuesAreResolved(fixForIssues)){
                processingEnv.getMessager().printMessage(
                        ERROR, constructIssueMessage(fixForIssues), e, getCorrectAnnotationMirror(e));
            }
        }else{
            FixForIssue fixForIssue = e.getAnnotation(FixForIssue.class);
            if(fixForIssue != null){
                if (checkIssueIsResolved(fixForIssue))
                {
                    processingEnv.getMessager().printMessage(
                            ERROR, constructIssueMessage(fixForIssue), e, getCorrectAnnotationMirror(e));
                }
            }
        }
    }
    
    private void processElementWithIgnore(Element e){
        IgnoreIssueResolved ignore = e.getAnnotation(IgnoreIssueResolved.class);
        FixForIssue[] fixForIssues = e.getAnnotationsByType(FixForIssue.class);
        checkIfFixForIssueIsPresent(fixForIssues, e);
        checkReasonForIgnore(ignore, e);
        String msg = constructMessageForIgnore(fixForIssues, ignore);
        processingEnv.getMessager().printMessage(MANDATORY_WARNING, msg, e, getOptionalIgnoreIssueResolvedAnnotationMirror(e).get());
    }

    private String constructMessageForIgnore(FixForIssue[] fixForIssues, IgnoreIssueResolved ignore) {
        List<FixForIssue> fixedIssues = Stream.of(fixForIssues).filter(this::checkIssueIsResolved).collect(Collectors.toList());
        String msg = fixedIssues.isEmpty() ? 
                "There is an IgnoreIssueResolved annotation placed but no declared issues are resolved, this indicates that someone put the IgnoreIssueResolved has been placed prematurely!":
                "There is an IgnoreIssueResolved annotation placed over following resolved issues:";
        for (FixForIssue fixedIssue : fixedIssues) {
            msg = msg + "\n" + constructIssueMessage(fixedIssue);
        }
        msg = msg + "\n" + "indicated used version is: " + ignore.versionUsed();
        msg = msg + "\n" + "indicated fixed version is: " + ignore.versionResolved();
        msg = msg + "\n" + "reason for not removing the hack is: " + ignore.reasoningWhyHackCannotBeRemoved();
        msg = msg + "\n";
        return msg;
    }

    private void checkIfFixForIssueIsPresent(FixForIssue[] fixForIssues, Element e) {
        if(fixForIssues.length == 0){
            processingEnv.getMessager().printMessage( ERROR,
                    "IgnoreIssueResolved can be used only with FixForIssue or nonempty FixForIssues present !",
                    e , getOptionalIgnoreIssueResolvedAnnotationMirror(e).get() );
        }
    }

    private void checkReasonForIgnore(IgnoreIssueResolved ignore, Element e) {
        if(ignore.reasoningWhyHackCannotBeRemoved() == null || ignore.reasoningWhyHackCannotBeRemoved().trim().length() < 128){
            processingEnv.getMessager().printMessage( ERROR,
                    "reasoning for why hack cannot be removed has to be present and at least reasonable 128 characters long!",
                    e , getOptionalIgnoreIssueResolvedAnnotationMirror(e).get() );
        }
    }

    private AnnotationMirror getCorrectAnnotationMirror(Element e) {
        return getOptionalFixForIssuesAnnotationMirror(e)
                .orElseGet(() -> getOptionalFixForIssueAnnotationMirror(e)
                        .orElseGet(() -> getOptionalIgnoreIssueResolvedAnnotationMirror(e).get())
                );
    }
    
    private Optional<AnnotationMirror> getOptionalIgnoreIssueResolvedAnnotationMirror(Element e) {
        return e.getAnnotationMirrors().stream().filter(
                (AnnotationMirror aT) -> 
                        aT.getAnnotationType().asElement().getSimpleName()
                                .contentEquals(IgnoreIssueResolved.class.getSimpleName())
        ).map(AnnotationMirror.class::cast).findAny();
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
        switch (fixInformation.trackerType()){
            case JIRA :
                return JiraIssueChecker.checkIssueResolved(fixInformation);
            default: 
                return false;
        }
    }

}
