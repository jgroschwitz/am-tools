package de.saar.coli.amtools.decomposition.formalisms;

import de.up.ling.irtg.algebra.graph.GraphEdge;

public class DRTEdgeAttachmentHeuristic extends EdgeAttachmentHeuristic{
    public static final String[] OUTBOUND_EDGEPREFIXES = new String[]{
        "Agent",
            "Bearer",
        "Participant",
        "Creator",
            "Proposition",
                "Beneficiary",
                "Co-Agent",
                "Co-Patient",
                "Co-Theme",
                "Experiencer",
                "Patient",
                "Pivot",
                "Product",
                "Recipient",
                "Theme",
                "Owner",
            "User",
            "Role",
            "member",
            "ALTERNATION",
            "ATTRIBUTION",
            "CONDITION",
            "CONSEQUENCE",
            "CONTINUATION",
            "CONTRAST",
            "EXPLANATION",
            "NECESSITY",
            "NEGATION",
            "PRECONDITION",
            "RESULT",
            "SOURCE",
            "NEQ",
            "APX",
            "EQU",
            "TPR",
};

    @Override
    public boolean isOutbound(GraphEdge edge) {
        for (String pref : OUTBOUND_EDGEPREFIXES) {
            if (edge.getLabel().matches(pref+"[0-9]*")) {
                return true;
            }
        }
        return false;
    }

}