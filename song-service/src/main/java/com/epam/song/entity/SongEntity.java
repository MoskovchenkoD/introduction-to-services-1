package com.epam.song.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "songs_metadata")
@Data
@NoArgsConstructor
public class SongEntity {

  public SongEntity(Long id, String name, String artist, String album, String duration, String year) {
    this.id = id;
    this.name = name;
    this.artist = artist;
    this.album = album;
    this.duration = duration;
    this.year = year;
  }

  @Id
  @Column(name = "id")
  private Long id;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "artist", nullable = false, length = 100)
  private String artist;

  @Column(name = "album", nullable = false, length = 100)
  private String album;

  @Column(name = "duration", nullable = false)
  private String duration;

  @Column(name = "year", nullable = false)
  private String year;
}
