package com.movieflix.movieApi.service;

import com.movieflix.movieApi.dto.MovieDto;
import com.movieflix.movieApi.entities.Movie;
import com.movieflix.movieApi.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovieServiceImpl implements MovieService{

    private final MovieRepository movieRepository;

    private final FileService fileService;

    public MovieServiceImpl(MovieRepository movieRepository, FileService fileService) {
        this.movieRepository = movieRepository;
        this.fileService = fileService;
    }

    @Value("${project.poster}")
    private String path;

    @Value("${base.url}")
    private String baseUrl;

    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException {

        // 1. upload the file

        if(Files.exists(Paths.get(path + File.separator + file.getOriginalFilename()))){
            throw new RuntimeException("File already exists! Please enter another file name!");
        }
        String uploadedFileName = fileService.uploadFile(path, file);

        // 2. set the value of field 'poster' as filename
        movieDto.setPoster(uploadedFileName);

        // 3. map dto to Movie object
        Movie movie = new Movie(
                null,
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );

        // 4. save the movie object -> saved Movie object
        Movie savedMovie = movieRepository.save(movie);

        // 5. generate the posterUrl
        String posterUrl = baseUrl + "/file/" + uploadedFileName;

        // 6. map Movie object to DTO object and return it
        MovieDto response = new MovieDto(
                savedMovie.getMovieId(),
                savedMovie.getTitle(),
                savedMovie.getDirector(),
                savedMovie.getStudio(),
                savedMovie.getMovieCast(),
                savedMovie.getReleaseYear(),
                savedMovie.getPoster(),
                posterUrl
        );

        return response;
    }

    @Override
    public MovieDto getMovie(Integer movieId) {

        // 1. check the data in DB and if exists, fetch the data of given ID
        Movie movie = movieRepository.findById(movieId).orElseThrow(()-> new RuntimeException("Movie not found!"));

        // 2. generate posterUrl
        String posterUrl = baseUrl + "/files/" + movie.getPoster();

        // 3. map to MovieDto object and return it
        MovieDto responseMovieDto = new MovieDto(
                movie.getMovieId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getStudio(),
                movie.getMovieCast(),
                movie.getReleaseYear(),
                movie.getPoster(),
                posterUrl
        );

        return responseMovieDto;
    }

    @Override
    public List<MovieDto> getAllMovies() {

        // 1. fetch all data from DB
        List<Movie> movies = movieRepository.findAll();

        List<MovieDto> movieDtos = new ArrayList<>();

        // 2. iterate through the list, generate posterUrl for each movie obj,
        // and map to MovieDto obj
        for(Movie movie : movies){
            String posterUrl = baseUrl + "/files/" + movie.getPoster();
            MovieDto movieDto = new MovieDto(
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            );
            movieDtos.add(movieDto);
        }

        return movieDtos;
    }

    @Override
    public MovieDto updateMovie(Integer movieId, MovieDto movieDto, MultipartFile file) throws IOException {
        // 1. check if movie object exists with given movieId
        Movie mv = movieRepository.findById(movieId).orElseThrow(
                        () -> new RuntimeException("Movie doesn't exist!"));

        // 2. if file is null, do nothing
        // if file is not null, then delete existing file associated with the record,
        // and upload the new file
        String posterFileName = mv.getPoster();
        if(file != null){
            Files.deleteIfExists(Paths.get(path + File.separator + posterFileName));
            posterFileName = fileService.uploadFile(path,file);
        }


        // 3. set movieDto's poster value, according to step2
        movieDto.setPoster(posterFileName);

        // 4. map it to Movie object
        Movie movie = new Movie(
                mv.getMovieId(),
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );

        // 5. save the movie object -> return saved movie object
        Movie updatedMovie = movieRepository.save(movie);

        // 6. generate posterUrl for it
        String posterUrl = baseUrl + "/files/" + posterFileName;

        // 7. map to MovieDto and return it
        MovieDto responseDto = new MovieDto(
                movie.getMovieId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getStudio(),
                movie.getMovieCast(),
                movie.getReleaseYear(),
                movie.getPoster(),
                posterUrl
        );

        return responseDto;
    }

    @Override
    public String deleteMovie(Integer movieId) throws IOException {
        // 1. check if movie object exists in DB
        Movie mv = movieRepository.findById(movieId).orElseThrow(
                () -> new RuntimeException("Movie not found with Id: " + movieId));
        Integer id = mv.getMovieId();

        // 2. delete the file associated with this object
        String posterFileName = mv.getPoster();
        Files.deleteIfExists(Paths.get(path + File.separator + posterFileName));


        // 3. delete the movie object
        movieRepository.delete(mv);

        return "Movie deleted with id = " + id;
    }
}
