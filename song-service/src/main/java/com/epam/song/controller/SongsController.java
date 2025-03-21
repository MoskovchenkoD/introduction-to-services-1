package com.epam.song.controller;

import com.epam.song.dto.SongDto;
import com.epam.song.service.SongsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Controller
@RequestMapping("/songs")
public class SongsController {

  private final SongsService songsService;
  
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, Long>> saveNewSong(@RequestBody @Valid SongDto songDto) {
    return new ResponseEntity<>(songsService.saveNewSong(songDto), HttpStatus.OK);
  }
  
  @GetMapping("/{id}")
  public ResponseEntity<SongDto> getSongById(@PathVariable String id) {
    return new ResponseEntity<>(songsService.getSongById(id), HttpStatus.OK);
  }

  @DeleteMapping()
  public ResponseEntity<Map<String, List<Long>>> deleteSongsByIds(@RequestParam String id) {
    return new ResponseEntity<>(songsService.deleteSongsByIds(id), HttpStatus.OK);
  }
}
