package com.pm.patientservice.service;

import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.exception.EmailAlreadyExistsException;
import com.pm.patientservice.exception.PatientNotFoundException;
import com.pm.patientservice.grpc.BillingServiceGrpcClient;
import com.pm.patientservice.kafka.KafkaProducer;
import com.pm.patientservice.mapper.PatientMapper;
import com.pm.patientservice.model.Patient;
import com.pm.patientservice.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;

    public PatientService(PatientRepository patientRepository,
            BillingServiceGrpcClient billingServiceGrpcClient,
            KafkaProducer kafkaProducer){
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.patientRepository = patientRepository;
        this.kafkaProducer = kafkaProducer;
    }

    public List<PatientResponseDTO> findAll() {
        return patientRepository.findAll()
                .stream().map(PatientMapper::toPatientResponseDTO).toList();
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
        if(patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException("Patient with email " + patientRequestDTO.getEmail() + " already exists");
        }

        Patient patient = patientRepository.save(PatientMapper.toPatient(patientRequestDTO));
        billingServiceGrpcClient.createBillingAccount(patient.getId().toString(), patient.getName(), patient.getEmail(), patient.getDateOfBirth().toString());
        kafkaProducer.sendPatientEvent(patient);
        return PatientMapper.toPatientResponseDTO(patient);
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient with id :" + id + " not found"));
        patient.setName(patientRequestDTO.getName());

        if(patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(), id)) {
            throw new EmailAlreadyExistsException("Patient with email " + patientRequestDTO.getEmail() + " already exists");
        }

        patient.setName(patientRequestDTO.getName());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setDateOfBirth(patient.getDateOfBirth());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getBirthDate()));

        return PatientMapper.toPatientResponseDTO(patientRepository.save(patient));
    }

    public void deletePatient(UUID id) {
        patientRepository.deleteById(id);
    }
}
