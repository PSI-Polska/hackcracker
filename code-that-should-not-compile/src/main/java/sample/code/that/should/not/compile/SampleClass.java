package sample.code.that.should.not.compile;

import de.psi.pjf.hackcracker.annotation.FixForIssue;
import de.psi.pjf.hackcracker.annotation.FixForIssues;
import de.psi.pjf.hackcracker.annotation.IgnoreIssueResolved;


@FixForIssues(
value = {
    @FixForIssue(url = "http://jira-bld-ppl.psi.de:8080/", issue = "CMDTF-10"),
    @FixForIssue(url = "http://jira-bld-ppl.psi.de:8080/", issue = "CMDTF-11")
},
needsAllIssuesResolved = true
)
public class SampleClass
{
    
    @FixForIssue(url = "http://jira-bld-ppl.psi.de:8080/", issue = "CMDTF-10")
    @FixForIssue(url = "http://jira-bld-ppl.psi.de:8080/", issue = "CMDTF-200")
    public static void main(String ... args){
        
    }
    
//    @IgnoreIssueResolved(
//            versionUsed = "test",
//            versionResolved = "test",
//            reasoningWhyHackCannotBeRemoved = "short description"
//    )
//    @FixForIssue(url = "http://jira-bld-ppl.psi.de:8080/", issue = "CMDTF-10")
//    private void testTooShortDescription(){
//        
//    }
//    
//    @IgnoreIssueResolved(
//            versionUsed = "test",
//            versionResolved = "test",
//            reasoningWhyHackCannotBeRemoved = "short description"
//    )
//    private void testNoFixForIssue(){
//        
//    }
    
    
    
}
