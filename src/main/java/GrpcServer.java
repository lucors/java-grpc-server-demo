import io.grpc.*;
import io.jsonwebtoken.Jwts;

import java.io.IOException;

public class GrpcServer {

    static class AuthInterceptor implements ServerInterceptor {
        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
            // Разрешаем доступ к AuthService.Auth
            String methodName = call.getMethodDescriptor().getFullMethodName();
            if (methodName.equalsIgnoreCase("auth.AuthService/Auth")) return next.startCall(call, headers);

            String token = headers.get(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER));
            if (token == null || token.isEmpty()) {
                call.close(Status.UNAUTHENTICATED.withDescription("No token provided"), new Metadata());
                return new ServerCall.Listener<>() {};
            }
            try {
                Jwts.parser()
                        .setSigningKey(AuthServiceImpl.SECRET_KEY)
                        .parseClaimsJws(token)
                        .getBody();
            } catch (Exception e) {
                call.close(Status.UNAUTHENTICATED.withDescription("Invalid token provided"), new Metadata());
                return new ServerCall.Listener<>() {};
            }
            return next.startCall(call, headers);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(50052)
                .addService(new AuthServiceImpl())
                .addService(new MovieServiceImpl())
                .intercept(new AuthInterceptor())
                .build();

        server.start();
        System.out.println("Server started, listening on " + server.getPort());
        server.awaitTermination();
    }
}
