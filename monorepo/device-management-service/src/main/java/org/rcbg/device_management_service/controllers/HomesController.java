package org.rcbg.device_management_service.controllers;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/homes")
public class HomesController {

    @GetMapping
    List<String> getAllAvailableHomes() {
        List<String> result = new ArrayList<>();
        result.add("Not Implemented");
        return result;
    }

    @PostMapping
    String createNewHome() {
        return "Not Implemented";
    }

    @GetMapping("/{homeId}")
    String getHomeByHomeId(@PathVariable String homeId) {
        return "Not Implemented";
    }

    @PatchMapping("/{homeId}")
    String patchHomeByHomeId(@PathVariable String homeId, @RequestBody String updateContent) {
        return "Not Implemented";
    }

    @DeleteMapping("/{homeId}")
    String deleteHomeByHomeId(@PathVariable String homeId) {
        return "Not Implemented";
    }
}
