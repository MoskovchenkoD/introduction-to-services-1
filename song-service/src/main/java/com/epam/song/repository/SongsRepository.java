package com.epam.song.repository;

import com.epam.song.entity.SongEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SongsRepository extends JpaRepository<SongEntity, Long> {
  
}
