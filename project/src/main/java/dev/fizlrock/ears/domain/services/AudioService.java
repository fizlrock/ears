package dev.fizlrock.ears.domain.services;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.fizlrock.ears.domain.entities.AudioRecordInfo;
import dev.fizlrock.ears.domain.entities.AudioRecordInfo.UploadStatus;
import dev.fizlrock.ears.repository.AudioRecordInfoRepository;
import dev.fizlrock.ears.repository.UserRepository;
import jakarta.transaction.Transactional;

/**
 * AudioService
 */
@Service
public class AudioService {

  @Autowired
  FileStorage storage;

  @Autowired
  UserRepository userRepo;

  @Autowired
  AudioRecordInfoRepository audioRepo;

  private static final Long MIN_FILE_SIZE = 5l;

  public FileUploader getFileUploader(String username, Long file_size, LocalDateTime recordedDateTime) {

    if (file_size < MIN_FILE_SIZE)
      throw new IllegalArgumentException("Слишком маленький файл");

    var user = userRepo.findByUsername(username).get();
    // Тут проверка бизнес правил

    AudioRecordInfo info = AudioRecordInfo.builder()
        .id(UUID.randomUUID())
        .recordedDate(recordedDateTime)
        .uploadedDate(LocalDateTime.now())
        .owner(user)
        .uploadStatus(UploadStatus.Uploading)
        .build();

    audioRepo.save(info);

    return storage.getFileUploader(info.getId().toString(), file_size);
  }

  @Transactional
  public void fileUploadFailedNotify(String uuid) {
    var id = UUID.fromString(uuid);
    var audio = audioRepo.findById(id).get();
    audio.setUploadStatus(UploadStatus.ConnectionFailed);
  }

  @Transactional
  public void fileUploadedNotify(String uuid) {
    var id = UUID.fromString(uuid);
    var audio = audioRepo.findById(id).get();
    audio.setUploadStatus(UploadStatus.Uploaded);
  }

}
