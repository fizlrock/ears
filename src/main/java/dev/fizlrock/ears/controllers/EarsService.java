package dev.fizlrock.ears.controllers;

import static io.grpc.Status.ALREADY_EXISTS;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.google.protobuf.Empty;

import dev.fizlrock.ears.model.AudioRecordInfo;
import dev.fizlrock.ears.model.User;
import dev.fizlrock.ears.model.AudioRecordInfo.UploadStatus;
import dev.fizlrock.ears.proto.EarsServiceGrpc.EarsServiceImplBase;
import dev.fizlrock.ears.proto.LoginProtos.AudioUploadRequest;
import dev.fizlrock.ears.proto.LoginProtos.AudioUploadResponse;
import dev.fizlrock.ears.repository.AudioRecordInfoRepository;
import dev.fizlrock.ears.repository.UserRepository;
import dev.fizlrock.ears.services.LocalAudioStorage;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;;

@GrpcService
@Slf4j
public class EarsService extends EarsServiceImplBase {

  @Autowired
  UserRepository userRepo;
  @Autowired
  AudioRecordInfoRepository audioInfoRepo;

  @Autowired
  LocalAudioStorage storage;

  @Autowired
  PasswordEncoder encoder;

  protected Exception duplicateUserNameResponse = ALREADY_EXISTS
      .withDescription("Такое имя пользователя занято")
      .asException();

  @Override
  public void register(dev.fizlrock.ears.proto.LoginProtos.RegistrationRequest request,
      io.grpc.stub.StreamObserver<Empty> responseObserver) {

    log.debug("Запрос на регистрацию: {}", request);
    var user = userRepo.findByUsername(request.getUsername());

    if (user.isEmpty()) {

      var newUser = User.builder()
          .username(request.getUsername())
          .passwordHash(encoder.encode(request.getPassword()))
          .build();
      userRepo.save(newUser);
      log.debug("Успешно зарегистрирован пользователь: {}", newUser);
      responseObserver.onNext(Empty.getDefaultInstance());
      responseObserver.onCompleted();
    } else {
      responseObserver.onError(duplicateUserNameResponse);
    }
  }

  @Override
  public StreamObserver<AudioUploadRequest> uploadAudio(
      StreamObserver<AudioUploadResponse> responseObserver) {

    // TODO Вот это прям ужасно, с этим нужно что-то сделать...
    var auth = (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext()
        .getAuthentication().getPrincipal();
    String username = auth.getUsername();

    return new StreamObserver<AudioUploadRequest>() {

      boolean metadataReceived = false;
      Long bytesTotal = 0l;
      Long bytesRecieved = 0l;
      LocalDateTime recordedDate;

      AudioRecordInfo audioInfo;

      @Override
      public void onNext(AudioUploadRequest request) {
        // Сюда приходит новая порция данных

        if (metadataReceived) {
          if (request.hasMetadata())
            throw new RuntimeException("Кривой запрос, низя два раза метадату отправлять");
          if (!request.hasBatch())
            throw new RuntimeException("Ожидаются данные");

          var data = request.getBatch().getData();
          bytesRecieved += data.size();

          if (bytesRecieved > bytesTotal)
            throw new RuntimeException("Лишние данные");

          log.info("received {}/{} bytes", bytesRecieved, bytesTotal);
          storage.writeBytes(audioInfo.getId().toString(), data.toByteArray());

        } else {
          if (request.hasBatch())
            throw new RuntimeException("Первым сообщением необходимо передать метадату");
          if (!request.hasMetadata())
            throw new RuntimeException("Ожидаются метаданные");

          log.info("received metadata: {}", request.getMetadata());
          metadataReceived = true;
          var metadata = request.getMetadata();
          var instant = Instant.ofEpochSecond(metadata.getRecordStartTime().getSeconds());
          // Тут вероятна фатальная ошибка...
          recordedDate = LocalDateTime.ofInstant(instant, TimeZone.getDefault().toZoneId());
          bytesTotal = metadata.getTotalFileSize();

          var user = userRepo.findByUsername(username).get();

          audioInfo = AudioRecordInfo.builder()
              .id(UUID.randomUUID())
              .recordedDate(recordedDate)
              .uploadStatus(UploadStatus.Uploading)
              .owner(user)
              .build();

          audioInfoRepo.save(audioInfo);
          storage.openAudio(audioInfo.getId().toString());

        }

      }

      @Override
      public void onError(Throwable t) {
        // TODO Auto-generated method stub
        storage.closeAudio(audioInfo.getId().toString());

        throw new UnsupportedOperationException("Unimplemented method 'onError'");
      }

      @Override
      public void onCompleted() {
        storage.closeAudio(audioInfo.getId().toString());

        responseObserver.onNext(
            AudioUploadResponse.newBuilder()
                .setAudioId(audioInfo.getId().toString())
                .build());
        responseObserver.onCompleted();
      }

    };
  }

}
