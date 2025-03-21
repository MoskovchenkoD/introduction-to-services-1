package com.epam.song.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SongDto {

  @NotNull
  @Min(1)
  private Long id;

  @NotBlank
  @Size(min = 1, max = 100)
  private String name;

  @NotBlank
  @Size(min = 1, max = 100)
  private String artist;

  @NotBlank
  @Size(min = 1, max = 100)
  private String album;

  @NotBlank
  @Pattern(regexp = "^[012345]\\d:[0-5]\\d$", message = "Duration must be in the format 'MM:SS' and between 00:00 and 59:59")
  private String duration;

  @NotBlank
  @Pattern(regexp = "^(19|20)\\d{2}$", message = "Year must be in the format YYYY and between 1900 and 2099")
  private String year;

  public SongDto(Long id, String name, String artist, String album, String duration, String year) {
    this.id = id;
    this.name = name;
    this.artist = artist;
    this.album = album;
    this.duration = duration;
    this.year = year;
  }
}
