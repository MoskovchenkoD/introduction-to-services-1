package com.epam.resource.service;

import com.epam.resource.dto.ErrorResponse;
import com.epam.resource.dto.SongDto;
import com.epam.resource.entity.ResourceEntity;
import com.epam.resource.exception.ErrorCodeException;
import com.epam.resource.repository.ResourceRepository;
import com.epam.resource.util.MessageConstants;
import com.epam.resource.util.SongDtoMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourcesService {

  private static final int IDS_STRING_MAX_LENGTH = 200;
  private static final String CSV_STRING_LENGTH_EXCEEDED_ERROR =
      String.format("CSV string length must be less than %d characters.", IDS_STRING_MAX_LENGTH);
  private static final String SONG_SERVICE_URL = "http://localhost:8092/songs";
  private static final ErrorCodeException SOMETHING_WENT_WRONG_EXCEPTION = 
      new ErrorCodeException(HttpStatus.INTERNAL_SERVER_ERROR.value(), MessageConstants.SOMETHING_WENT_WRONG);
  private final Tika tika = new Tika();
  private final RestTemplate restTemplate = new RestTemplate();
  private final ObjectMapper objectMapper = new ObjectMapper();
  
  private final ResourceRepository resourceRepository;

  @Transactional
  public Map<String, Long> processAndSaveResource(HttpServletRequest request) {
    byte[] mp3file = extractBytesFromRequest(request);
    ResourceEntity savedEntity = saveNewResource(mp3file);
    Metadata metadata = extractMetadata(mp3file);
    SongDto parsedSongDto = SongDtoMapper.toDto(savedEntity.getId(), metadata);

    return saveResourceMetadataInSongService(parsedSongDto);
  }

  private byte[] extractBytesFromRequest(HttpServletRequest request) {
    try {
      return request.getInputStream().readAllBytes();
    } catch (IOException e) {
      log.error("Failed to process the file: {}", e.getMessage());
      throw SOMETHING_WENT_WRONG_EXCEPTION;
    }
  }

  private Map<String, Long> saveResourceMetadataInSongService(SongDto parsedSongDto) {
    try {
      ResponseEntity<String> songResponse = restTemplate.postForEntity(SONG_SERVICE_URL, parsedSongDto, String.class);

      if (songResponse.getStatusCode() == HttpStatus.OK) {
        return objectMapper.readValue(songResponse.getBody(), new TypeReference<>() {});
      } else {
        ErrorResponse errorResponse = objectMapper.readValue(songResponse.getBody(), ErrorResponse.class);
        log.error("Error saving song: {}", errorResponse.getErrorMessage());
        throw SOMETHING_WENT_WRONG_EXCEPTION;
//        throw new ErrorCodeException(Integer.parseInt(errorResponse.getErrorCode()), errorResponse.getErrorMessage());
      }
    } catch (HttpClientErrorException e) {
      log.error("Failed to send SongDto to song-service", e);
      throw SOMETHING_WENT_WRONG_EXCEPTION;
    } catch (JsonProcessingException e) {
      log.error("Failed to parse response from song-service", e);
      throw SOMETHING_WENT_WRONG_EXCEPTION;
    }
  }

  private ResourceEntity saveNewResource(byte[] mp3file) {
    ResourceEntity resourceEntity = new ResourceEntity();
    resourceEntity.setResource(mp3file);
    return resourceRepository.save(resourceEntity);
  }

  private Metadata extractMetadata(byte[] mp3file) {
    Metadata metadata = new Metadata();
    try (InputStream mp3FileData = new ByteArrayInputStream(mp3file)) {
      tika.parse(mp3FileData, metadata);
    } catch (IOException e) {
      log.error("Failed to extract metadata from resource: {}", e.getMessage());
      throw SOMETHING_WENT_WRONG_EXCEPTION;
    }
    return metadata;
  }

  public byte[] getResourceById(String id) {
    long parsedId = parseId(id);
    return resourceRepository.findById(parsedId)
        .map(ResourceEntity::getResource)
        .orElseThrow(() -> new ErrorCodeException(
            HttpStatus.NOT_FOUND.value(), 
            String.format("Resource with ID=%d not found", parsedId))
        );
  }

  private long parseId(String id) {
    try {
      int parsedId = Integer.parseInt(id);
      if (parsedId <= 0) {
        throw new ErrorCodeException(HttpStatus.BAD_REQUEST.value(), 
            String.format("'id' must be a positive number. Found '%s'", id));
      }
      return parsedId;
    } catch (NumberFormatException e) {
      log.error(e.getMessage());
      throw new ErrorCodeException(HttpStatus.BAD_REQUEST.value(), 
          String.format("Failed to parse '%s' value", id));
    }
  }

  @Transactional
  public Map<String, List<Long>> deleteSongsByIds(String ids) {
    validateResourceIds(ids);
    List<Long> idList = parseResourceIds(ids);
    try {
      List<Long> deletedIds = deleteExistingResourcesById(idList);

      sendRequestToDeleteSongsByIds(deletedIds);

      return Map.of("ids", deletedIds);
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new ErrorCodeException(HttpStatus.INTERNAL_SERVER_ERROR.value(), MessageConstants.SOMETHING_WENT_WRONG);
    }
  }

  private void sendRequestToDeleteSongsByIds(List<Long> deletedIds) {
    try {
      if (CollectionUtils.isNotEmpty(deletedIds)) {
        String deletedIdsParam = "?id=" + String.join(",", deletedIds.stream().map(String::valueOf).toList());
        ResponseEntity<String> songDeleteResponse = restTemplate.exchange(
            SONG_SERVICE_URL + deletedIdsParam, 
            HttpMethod.DELETE, 
            null, 
            String.class);

        if (songDeleteResponse.getStatusCode() != HttpStatus.OK) {
          ErrorResponse errorResponse = objectMapper.readValue(songDeleteResponse.getBody(), ErrorResponse.class);
          log.error("Error deleting songs: {}", errorResponse.getErrorMessage());
          throw SOMETHING_WENT_WRONG_EXCEPTION;
        }
      }
    } catch (HttpClientErrorException e) {
      log.error("Failed to send delete ids to song-service", e);
      throw SOMETHING_WENT_WRONG_EXCEPTION;
    } catch (JsonProcessingException e) {
      log.error("Failed to parse response from song-service", e);
      throw SOMETHING_WENT_WRONG_EXCEPTION;
    }
  }

  private List<Long> deleteExistingResourcesById(List<Long> idList) {
    List<Long> deletedIds = new ArrayList<>();
    for (Long id : idList) {
      if (resourceRepository.existsById(id)) {
        resourceRepository.deleteById(id);
        deletedIds.add(id);
      }
    }
    return deletedIds;
  }

  private List<Long> parseResourceIds(String ids) {
    try {
      return Arrays.stream(ids.split(","))
          .map(Long::parseLong)
          .toList();
    } catch (NumberFormatException e) {
      log.error(e.getMessage());
      throw new ErrorCodeException(HttpStatus.BAD_REQUEST.value(), "Failed to parse 'ids' parameter");
    }
  }

  private void validateResourceIds(String ids) {
    if (ids.length() >= IDS_STRING_MAX_LENGTH) {
      throw new ErrorCodeException(HttpStatus.BAD_REQUEST.value(), 
          CSV_STRING_LENGTH_EXCEEDED_ERROR + " Found " + ids.length() + " characters");
    }
  }
}
