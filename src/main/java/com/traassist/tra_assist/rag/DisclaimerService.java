package com.traassist.tra_assist.rag;

import org.springframework.stereotype.Service;

@Service
public class DisclaimerService {

    private static final String SHORT = "\n\n⚠️ Verify with TRA before acting: www.tra.go.tz";
    private static final String FULL = "\n\n⚠️ DISCLAIMER: This is guidance only. TRA rules change regularly. Always verify with an official TRA office or www.tra.go.tz before making any tax decisions.";

    public String attach(String response, ConfidenceLevel confidence) {
        return response + (confidence == ConfidenceLevel.LOW ? FULL : SHORT);
    }
}
