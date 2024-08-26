import io.grpc.stub.StreamObserver;
import movie.Movie;
import movie.MovieServiceGrpc;
import movie.MovieFilter;
import movie.GetMovieRequest;
import movie.GetAllMoviesResponse;
import movie.CreateMovieRequest;
import movie.UpdateMovieRequest;
import movie.DeleteMovieRequest;
import movie.DeleteMovieResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MovieServiceImpl extends MovieServiceGrpc.MovieServiceImplBase {

    protected List<Movie> movieList = new ArrayList<>();
    protected int movieIdCounter = 0;

    protected Movie findMovie(Integer id) {
        return movieList.stream().filter(v -> v.getId() == id).findFirst().orElse(null);
    }

    @Override
    public void createMovie(CreateMovieRequest request, StreamObserver<Movie> responseObserver) {
        if (request.getTitle().isEmpty()
                || request.getDesc().isEmpty()
                || request.getGenre().isEmpty()
                || request.getReleaseYear() <= 0) {
            responseObserver.onError(
                    io.grpc.Status.INVALID_ARGUMENT.withDescription("Не заданы все необходимые поля").asRuntimeException()
            );
            return;
        }
        Movie movie = Movie.newBuilder()
                .setId(++movieIdCounter)
                .setTitle(request.getTitle())
                .setDesc(request.getDesc())
                .setGenre(request.getGenre())
                .setReleaseYear(request.getReleaseYear())
                .build();
        movieList.add(movie);

        responseObserver.onNext(movie);
        responseObserver.onCompleted();
    }

    @Override
    public void getMovie(GetMovieRequest request, StreamObserver<Movie> responseObserver) {
        Movie movie = this.findMovie(request.getId());
        if (movie == null) {
            responseObserver.onError(
                    io.grpc.Status.INVALID_ARGUMENT.withDescription("Фильм не найден").asRuntimeException()
            );
            return;
        }
        responseObserver.onNext(movie);
        responseObserver.onCompleted();
    }

    @Override
    public void getAllMovies(MovieFilter request, StreamObserver<GetAllMoviesResponse> responseObserver) {
        GetAllMoviesResponse response = GetAllMoviesResponse.newBuilder()
                .addAllMovies(movieList.stream()
                        .filter(movie -> request.getIdsList().isEmpty() || request.getIdsList().contains(movie.getId()))
                        .filter(movie -> request.getGenre().isEmpty() || request.getGenre().equals(movie.getGenre()))
                        .filter(movie -> (request.getReleaseYear() <= 0) || request.getReleaseYear() == movie.getReleaseYear())
                        .collect(Collectors.toList()))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateMovie(UpdateMovieRequest request, StreamObserver<Movie> responseObserver) {
        Movie movie = this.findMovie(request.getId());
        if (movie == null) {
            responseObserver.onError(
                    io.grpc.Status.INVALID_ARGUMENT.withDescription("Фильм не найден").asRuntimeException()
            );
            return;
        }
        int movieIndex = movieList.indexOf(movie);
        movie = movie.toBuilder()
                .setTitle(request.getTitle().isEmpty() ? movie.getTitle() : request.getTitle())
                .setDesc(request.getDesc().isEmpty() ? movie.getDesc() : request.getDesc())
                .setGenre(request.getGenre().isEmpty() ? movie.getGenre() : request.getGenre())
                .setReleaseYear((request.getReleaseYear() <= 0) ? movie.getReleaseYear() : request.getReleaseYear())
                .build();
        movieList.set(movieIndex, movie);
        responseObserver.onNext(movie);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteMovie(DeleteMovieRequest request, StreamObserver<DeleteMovieResponse> responseObserver) {
        Movie movie = this.findMovie(request.getId());
        if (movie == null) {
            responseObserver.onError(
                    io.grpc.Status.INVALID_ARGUMENT.withDescription("Фильм не найден").asRuntimeException()
            );
            return;
        }
        DeleteMovieResponse response = DeleteMovieResponse.newBuilder()
                .setResult(movieList.remove(movie))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
