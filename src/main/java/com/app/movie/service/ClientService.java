/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.app.movie.service;

import com.app.movie.dto.ReportClientDto;
import com.app.movie.dto.ResponseDto;
import com.app.movie.entities.Client;

import com.app.movie.repository.ClientRepository;


import java.util.Optional;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ClientService {

    @Autowired
    ClientRepository repository;

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Iterable<Client> get() {
        Iterable<Client> response = repository.getAll();
        return response;
    }

    public Client getByCredential(String credential) {
        Client client = new Client();
        try {
            String pair = new String(Base64.decodeBase64(credential));
            String email = pair.split(":")[0];
            String pass = pair.split(":")[1];

            Optional<Client> saveClient = repository.findByEmail(email);
            if (saveClient.isPresent()){
                client = saveClient.get();
            }
            if(!matchPass(pass,saveClient.get().getPassword())){
                return null;
            }
        }catch (Exception e){
        }

        return client;
    }

    public ReportClientDto getReport() {
        Optional<Client> client = repository.findById("6380442df71ad74770fc57e1");
        ReportClientDto reportClientDto = new ReportClientDto();
        reportClientDto.birthDate = client.get().getBirthDate();
        reportClientDto.email = client.get().getEmail();
        reportClientDto.id = client.get().getId();
        return reportClientDto;
    }

    public ResponseDto create(Client request) {

        ResponseDto responseDto = new ResponseDto();
        Optional<Client> saveClient = repository.findByEmail(request.getEmail());
        if (saveClient.isEmpty()){
            request.setPassword(encrypt(request.getPassword()));
            responseDto.status=true;
            responseDto.message="cliente registrado con exito";
            Client resultClient= repository.save(request);
            responseDto.id = resultClient.getId();
        }
        else{
            responseDto.status=false;
            responseDto.message="cliente ya esta registrado";
        }
        return responseDto;
    }

    public Client update(Client client) {
        Client clientToUpdate = new Client();

        Optional<Client> currentClient = repository.findById(client.getId());
        if (!currentClient.isEmpty()) {
            clientToUpdate = client;
            clientToUpdate = repository.save(clientToUpdate);
        }
        return clientToUpdate;
    }

    public Boolean delete(String id) {
        repository.deleteById(id);
        Boolean deleted = true;
        return deleted;
    }

    private String encrypt(String pass){
        return this.passwordEncoder.encode(pass);
    }

    private Boolean matchPass(String pass,String dbPass){
        return this.passwordEncoder.matches(pass,dbPass);
    }
}
