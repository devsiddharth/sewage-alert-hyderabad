package com.sewagealert.community.service.impl;

import com.sewagealert.community.dto.NgoRequest;
import com.sewagealert.community.dto.NgoResponse;
import com.sewagealert.community.model.Ngo;
import com.sewagealert.community.repository.NgoRepository;
import com.sewagealert.community.service.NgoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
// NgoServiceImpl: Core business logic for NGO information — managed by administrators
public class NgoServiceImpl implements NgoService {

    private final NgoRepository ngoRepository;

    @Override
    // createNgo: Creates a new NGO record
    public NgoResponse createNgo(NgoRequest request) {
        Ngo ngo = new Ngo();
        ngo.setName(request.getName());
        ngo.setContactPerson(request.getContactPerson());
        ngo.setEmail(request.getEmail());
        ngo.setPhone(request.getPhone());
        ngo.setWebsite(request.getWebsite());
        ngo.setDescription(request.getDescription());

        ngo = ngoRepository.save(ngo);
        log.info("NGO created: {}", ngo.getName());

        return NgoResponse.fromEntity(ngo);
    }

    @Override
    // getNgo: Retrieves a single NGO by its ID
    public NgoResponse getNgo(Long ngoId) {
        Ngo ngo = ngoRepository.findById(ngoId)
                .orElseThrow(() -> new RuntimeException("NGO not found with id: " + ngoId));
        return NgoResponse.fromEntity(ngo);
    }

    @Override
    // getAllNgos: Returns all NGOs in the system
    public List<NgoResponse> getAllNgos() {
        return ngoRepository.findAll().stream()
                .map(NgoResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    // updateNgo: Updates an existing NGO's details
    public NgoResponse updateNgo(Long ngoId, NgoRequest request) {
        Ngo ngo = ngoRepository.findById(ngoId)
                .orElseThrow(() -> new RuntimeException("NGO not found with id: " + ngoId));

        ngo.setName(request.getName());
        ngo.setContactPerson(request.getContactPerson());
        ngo.setEmail(request.getEmail());
        ngo.setPhone(request.getPhone());
        ngo.setWebsite(request.getWebsite());
        ngo.setDescription(request.getDescription());

        ngo = ngoRepository.save(ngo);
        log.info("NGO updated: {}", ngoId);

        return NgoResponse.fromEntity(ngo);
    }

    @Override
    // deleteNgo: Removes an NGO by its ID
    public void deleteNgo(Long ngoId) {
        ngoRepository.deleteById(ngoId);
        log.info("NGO deleted: {}", ngoId);
    }
}
