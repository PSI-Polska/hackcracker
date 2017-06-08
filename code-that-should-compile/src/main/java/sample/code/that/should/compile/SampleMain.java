package sample.code.that.should.compile;

import de.psi.pjf.hackcracker.annotation.FixForIssue;
import de.psi.pjf.hackcracker.annotation.FixForIssues;
import de.psi.pjf.hackcracker.annotation.IgnoreIssueResolved;

@IgnoreIssueResolved(
        versionUsed = "0.1.0", 
        versionResolved = "1.36.0", 
        reasoningWhyHackCannotBeRemoved = "This is only a sample ! normally one "
                + "would write here why he cannot use the newer version. but this "
                + "time there is only some blabering from me")
@FixForIssue(url = "http://jira-bld-ppl.psi.de:8080/", issue = "CMDTF-10")
@FixForIssue(url = "http://jira-bld-ppl.psi.de:8080/", issue = "CMDTF-11")
public class SampleMain
{

    @FixForIssue(url = "http://jira-bld-ppl.psi.de:8080/", issue = "CMDTF-36")
    public static void main(String[] args)
    {
        System.out.println("Hello world");
    }
    @FixForIssues(value = {
        @FixForIssue(url = "http://jira-bld-ppl.psi.de:8080/", issue = "CMDTF-10"),
        @FixForIssue(url = "http://jira-bld-ppl.psi.de:8080/", issue = "CMDTF-200")
    },needsAllIssuesResolved = true)
    private void test()
    {
        System.out.println("Hello world");
    }
    
}
