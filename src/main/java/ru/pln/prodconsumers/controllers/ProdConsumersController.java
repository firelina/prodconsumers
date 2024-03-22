package ru.pln.prodconsumers.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.pln.prodconsumers.dto.StartDTO;
import ru.pln.prodconsumers.dto.StatisticsDTO;
import ru.pln.prodconsumers.service.IProdConsumers;

@RestController
public class ProdConsumersController {
    private final IProdConsumers prodConsumers;
    @Autowired
    public ProdConsumersController(IProdConsumers prodConsumers) {
        this.prodConsumers = prodConsumers;
    }
    @GetMapping("/stats")
    public ResponseEntity<StatisticsDTO> getStats(){
        return ResponseEntity.ok(prodConsumers.getStats());
    }
    @GetMapping("/test")
    public ResponseEntity<String> test(){
        return ResponseEntity.ok(prodConsumers.start(null));
    }
    @GetMapping("/stop/{guid}")
    public ResponseEntity<String> stop(@PathVariable(name = "guid") String guid){
        return ResponseEntity.ok(prodConsumers.stop(guid));
    }
    @PostMapping("/start")
    public ResponseEntity<String> start(@RequestBody StartDTO startDTO) {return ResponseEntity.ok(prodConsumers.start(startDTO));}
}
