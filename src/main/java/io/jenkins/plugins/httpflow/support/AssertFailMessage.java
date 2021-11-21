package io.jenkins.plugins.httpflow.support;

public class AssertFailMessage {

    public static final String ASSERTION_RULE_KEYWORD = " : Assertion rule was [";
    private String failReason;
    private String assertionRule;

    public AssertFailMessage(String message) {
        int ruleIndex = message.indexOf(ASSERTION_RULE_KEYWORD);
        if (ruleIndex != -1) {
            failReason = message.substring(0, ruleIndex);
            assertionRule = message.substring(ruleIndex + ASSERTION_RULE_KEYWORD.length());
            assertionRule = assertionRule.substring(0, assertionRule.lastIndexOf("]"));
        } else {
            failReason = message;
        }
    }

    public String toRunDescription() {
        if (assertionRule != null) {
            return failReason + "\nAssertion rule : " + assertionRule;
        }
        return failReason;
    }

}
