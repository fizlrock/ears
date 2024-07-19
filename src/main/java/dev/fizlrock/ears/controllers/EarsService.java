package dev.fizlrock.ears.controllers;

import static io.grpc.Status.ALREADY_EXISTS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.google.protobuf.Empty;

import dev.fizlrock.ears.model.User;
import dev.fizlrock.ears.proto.EarsServiceGrpc.EarsServiceImplBase;
import dev.fizlrock.ears.proto.LoginProtos.RegistrationRequest;
import dev.fizlrock.ears.repository.UserRepository;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;;

@GrpcService
@Slf4j
public class EarsService extends EarsServiceImplBase {

  @Autowired
  UserRepository userRepo;

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
  public StreamObserver<RegistrationRequest> uploadAudio(
      io.grpc.stub.StreamObserver<dev.fizlrock.ears.proto.LoginProtos.AudioUploadResponse> responseObserver) {
    return null;
  }

}
