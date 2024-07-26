package dev.fizlrock.ears.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import dev.fizlrock.ears.domain.entities.AudioRecordInfo;

@Repository
public interface AudioRecordInfoRepository
    extends PagingAndSortingRepository<AudioRecordInfo, Long>, CrudRepository<AudioRecordInfo, Long> {

}
