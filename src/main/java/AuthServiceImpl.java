import auth.AuthRequest;
import auth.AuthResponse;
import auth.AuthServiceGrpc;
import io.grpc.stub.StreamObserver;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {
    public static final String SECRET_KEY = "my_secret_key";

    @Override
    public void auth(AuthRequest request, StreamObserver<AuthResponse> responseObserver) {
        Instant now = Instant.now();
        String jwtToken = Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(5L, ChronoUnit.MINUTES)))
                .compact();
        AuthResponse response = AuthResponse.newBuilder().setToken(jwtToken).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
