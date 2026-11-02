package com.example.phuongstore.order.controller;

import com.example.phuongstore.order.dto.PickupProductCard;
import com.example.phuongstore.order.service.PickupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pickup")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PickupController {

    private final PickupService pickupService;

    @GetMapping("/checklist")
    public ResponseEntity<List<PickupProductCard>> getChecklist() {
        return ResponseEntity.ok(pickupService.getPickupChecklist());
    }
}
