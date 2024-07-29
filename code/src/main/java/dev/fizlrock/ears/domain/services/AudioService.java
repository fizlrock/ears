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

  @Transactional
  public String createFileUploader(String username, Long file_size, LocalDateTime recordedDateTime) {

    if (file_size < MIN_FILE_SIZE)
      throw new IllegalArgumentException("Слишком маленький файл");

    var user = userRepo.findByUsername(username).get();
    // Тут проверка бизнес правил

    AudioRecordInfo info = AudioRecordInfo.builder()
        .recordedDate(recordedDateTime)
        .uploadedDate(LocalDateTime.now())
        .owner(user)
        .uploadStatus(UploadStatus.Waiting)
        .build();

    audioRepo.save(info);

    try {
      storage.createAndOpenFile(info.getId().toString(), file_size);
    } catch (Exception e) {
      info.setUploadStatus(UploadStatus.StorageError);
      throw e;
    }
      info.setUploadStatus(UploadStatus.Uploading);
    return info.getId().toString();
  }

  public void writeBytes(String file_identifier, byte[] bytes) {
    try {
      storage.writeBytes(file_identifier, bytes);
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      var audioInfo = audioRepo.findById(UUID.fromString(file_identifier)).get();
      audioInfo.setUploadStatus(UploadStatus.ConnectionFailed);
    }
  }

  @Transactional
  public void fileUploadedNotify(String uuid) {
    var audioInfo = audioRepo.findById(UUID.fromString(uuid)).get();
    try {
      storage.closeFile(uuid);
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      audioInfo.setUploadStatus(UploadStatus.ConnectionFailed);
    }
      audioInfo.setUploadStatus(UploadStatus.Uploaded);
  }

}
