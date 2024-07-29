package dev.fizlrock.ears.repository;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import dev.fizlrock.ears.domain.entities.AudioRecordInfo;

@Repository
public interface AudioRecordInfoRepository
    extends PagingAndSortingRepository<AudioRecordInfo, UUID>, CrudRepository<AudioRecordInfo, UUID> {

}
