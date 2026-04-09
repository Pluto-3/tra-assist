package com.traassist.tra_assist.rag;

import lombok.Data;

import java.util.List;

@Data
public class RagResponse {
    private String answer;
    private List<String> sources;
    private double topSimilarity;
    private String confidence;
}
