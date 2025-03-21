package com.epam.resource.controller;

import com.epam.resource.service.ResourcesService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourcesController {
  
  private final ResourcesService resourcesService;

  @PostMapping(consumes = "audio/mpeg")
  public ResponseEntity<Map<String, Long>> saveNewSongResource(HttpServletRequest request) {
    return new ResponseEntity<>(resourcesService.processAndSaveResource(request), HttpStatus.OK);
  }
  
  @GetMapping(value = "/{id}", produces = "audio/mpeg")
  public ResponseEntity<byte[]> getSongResourceById(@PathVariable String id) {
    byte[] retrievedResource = resourcesService.getResourceById(id);
    return ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=\"resource_" + id + ".mp3\"")
        .body(retrievedResource);
  }

  @DeleteMapping()
  public ResponseEntity<Map<String, List<Long>>> deleteSongsByIds(@RequestParam String id) {
    return new ResponseEntity<>(resourcesService.deleteSongsByIds(id), HttpStatus.OK);
  }
}
