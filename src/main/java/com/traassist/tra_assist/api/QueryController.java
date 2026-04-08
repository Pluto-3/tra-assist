package com.traassist.tra_assist.api;

import com.traassist.tra_assist.rag.RagResponse;
import com.traassist.tra_assist.rag.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QueryController {

    private final RagService ragService;

    @PostMapping("/query")
    public RagResponse query(@RequestBody QueryRequest request) {
        return ragService.query(request.getQuery());
    }
}
