package com.epam.song.util;

import com.epam.song.dto.SongDto;
import com.epam.song.entity.SongEntity;

public class SongMapper {
  
  public static SongDto toDto(SongEntity songEntity) {
    return new SongDto(
        songEntity.getId(), 
        songEntity.getName(), 
        songEntity.getArtist(), 
        songEntity.getAlbum(), 
        songEntity.getDuration(), 
        songEntity.getYear());
  }
  
  public static SongEntity toEntity(SongDto songDto) {
    return new SongEntity(
        songDto.getId(), 
        songDto.getName(), 
        songDto.getArtist(), 
        songDto.getAlbum(), 
        songDto.getDuration(), 
        songDto.getYear());
  }
}
