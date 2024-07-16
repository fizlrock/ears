package dev.fizlrock.ears.controllers;

import org.springframework.stereotype.Component;

import dev.fizlrock.ears.proto.LoginProtos.LoginRequest;
import dev.fizlrock.ears.proto.LoginProtos.LoginResponse;
import dev.fizlrock.ears.proto.LoginProtos.RegistrationRequest;
import dev.fizlrock.ears.proto.RegistrationServiceGrpc.RegistrationServiceImplBase;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class RegistrationController extends RegistrationServiceImplBase {

  public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
    String token = request.getUsername() + request.getUsername();

    var loginResponse = LoginResponse.newBuilder()
        .setToken(token).build();
    responseObserver.onNext(loginResponse);
    responseObserver.onCompleted();
  }

  public void register(RegistrationRequest request, StreamObserver<LoginResponse> responseObserver) {
  }

}
