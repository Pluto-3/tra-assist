package com.traassist.tra_assist.rag;

import lombok.Data;

@Data
public class RetrievedChunk {

    private String content;
    private String sourceFile;
    private int chunkIndex;
    private double similarity;
}
