/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.app.movie.service;

import com.app.movie.dto.ResponseDto;
import com.app.movie.dto.ScoreDto;
import com.app.movie.entities.Client;
import com.app.movie.entities.Movie;
import com.app.movie.entities.Score;
import com.app.movie.repository.ClientRepository;
import com.app.movie.repository.MovieRepository;
import com.app.movie.repository.ScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class ScoreService {

    @Autowired
    ScoreRepository repository;

    @Autowired
    ClientService clientService;


    @Autowired
    MovieRepository movieRepository;

    @Autowired
    ClientRepository clientRepository;
    @Autowired
    ScoreRepository scoreRepository;
    public Iterable<Score> get() {
        Iterable<Score> response = repository.getAll();
        return response;
    }

    public Score check(String movieId,String authorization) {

        Score score = new Score();
        Optional<Movie> movie = movieRepository.findById(movieId);
        Client client = clientService.getByCredential(authorization);
        if(movie.isPresent() && !client.getId().isEmpty()){
            List<Score> scores = repository.findByMovieAndClient(movie.get().getId(),client.getId());
            if(scores.size()>0){
                score = scores.get(scores.size()-1);
            }
        }

        return score;
    }

    public ResponseDto create(ScoreDto request,String authorization) {
        ResponseDto response = new ResponseDto();
        response.status=false;
        if(request.score<0 || request.score>10){
            response.message="La calificación enviada no está dentro de los valores esperados";
        }else{
            Score score = new Score();
            Optional<Movie> movie = movieRepository.findById(request.movieId);
            Client clientToken = clientService.getByCredential(authorization);
            Optional<Client> client = clientService.repository.findById(request.clientId);
            List<Score> verifyScore = scoreRepository.findByMovieAndClient(request.movieId,request.clientId);
            if (clientToken == null || clientToken.getId().isEmpty()){
                response.status=false;
                response.message="Token no valido";
            }
            else if(verifyScore.stream().count()>0){
                response.status=false;
                response.message="Ya registro su calificacion";
            }
            else if(movie.isPresent() && client.isPresent()){
                //realizar validación de si ya existe la calificación...
                score.setState("activo");
                score.setScore(request.score);
                score.setMovie(movie.get());
                score.setClient(client.get());
                repository.save(score);
                response.status=true;
                response.message="Calificación guardada correctamente";
                response.id= score.getId();
            }
            else{
                response.message="La calificación no puede ser registrada verifique el id del cliente y la pelicula";
                response.status=false;
            }
        }
        return response;
    }

    public ResponseDto update(Score score,String scoreId) {

        ResponseDto response = new ResponseDto();
        Optional<Score> currentScore = repository.findById(scoreId);
        if (!currentScore.isEmpty()) {
            Score scoreToUpdate = new Score();
            scoreToUpdate = currentScore.get();
            scoreToUpdate.setScore(score.getScore());
            repository.save(scoreToUpdate);
            response.status=true;
            response.message="Se actualizó correctamente";
            response.id=scoreId;
        }else{
            response.status=false;
            response.message="No se logró la actualización";
        }
        return response;
    }

    public Boolean delete(String id) {
        repository.deleteById(id);
        Boolean deleted = true;
        return deleted;
    }
}
