package dev.fizlrock.ears;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

@SpringBootApplication
public class EarsApplication {

	public static void main(String[] args) {
		SpringApplication.run(EarsApplication.class, args);

		new ServerInterceptor() {

			@Override
			public <ReqT, RespT> Listener<ReqT> interceptCall(
					ServerCall<ReqT, RespT> call,
					Metadata headers,
					ServerCallHandler<ReqT, RespT> next) {
				throw new UnsupportedOperationException("Unimplemented method 'interceptCall'");
			}

		};

	}

}
