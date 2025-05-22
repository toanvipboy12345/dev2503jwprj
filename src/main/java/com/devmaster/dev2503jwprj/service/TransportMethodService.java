package com.devmaster.dev2503jwprj.service;

import com.devmaster.dev2503jwprj.entity.TransportMethod;
import com.devmaster.dev2503jwprj.repository.TransportMethodRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Validated
public class TransportMethodService {

    @Autowired
    private TransportMethodRepository transportMethodRepository;

    public List<TransportMethod> getAllTransportMethods() {
        return transportMethodRepository.findByIsDeleteFalse();
    }

    public Optional<TransportMethod> getTransportMethodById(Long id) {
        return transportMethodRepository.findById(id)
                .filter(transport -> transport.getIsDelete() == null || transport.getIsDelete() == 0);
    }

    public void createTransportMethod(@Valid TransportMethod transportMethod) {
        if (transportMethodRepository.findByNameAndIsDeleteFalse(transportMethod.getName()).isPresent()) {
            throw new IllegalArgumentException("Tên phương thức vận chuyển đã tồn tại");
        }

        transportMethod.setCreatedDate(LocalDateTime.now());
        transportMethod.setIsDelete((byte) 0);
        transportMethod.setIsActive((byte) 1);
        transportMethodRepository.save(transportMethod);
    }

    public void updateTransportMethod(@Valid TransportMethod transportMethod) {
        Optional<TransportMethod> existingTransportOpt = transportMethodRepository.findById(transportMethod.getId());
        if (!existingTransportOpt.isPresent()) {
            throw new IllegalArgumentException("Phương thức vận chuyển không tồn tại");
        }

        TransportMethod existingTransport = existingTransportOpt.get();

        Optional<TransportMethod> transportWithSameName = transportMethodRepository.findByNameAndIsDeleteFalse(transportMethod.getName());
        if (transportWithSameName.isPresent() && !transportWithSameName.get().getId().equals(transportMethod.getId())) {
            throw new IllegalArgumentException("Tên phương thức vận chuyển đã tồn tại");
        }

        transportMethod.setCreatedDate(existingTransport.getCreatedDate());
        transportMethod.setIsDelete(existingTransport.getIsDelete());
        transportMethod.setUpdatedDate(LocalDateTime.now());
        transportMethodRepository.save(transportMethod);
    }

    public void deleteTransportMethod(Long id) {
        Optional<TransportMethod> transportOpt = transportMethodRepository.findById(id);
        if (!transportOpt.isPresent()) {
            throw new IllegalArgumentException("Phương thức vận chuyển không tồn tại");
        }
        TransportMethod transport = transportOpt.get();
        if (transport.getIsDelete() == 1) {
            throw new IllegalArgumentException("Phương thức vận chuyển đã bị xóa trước đó");
        }
        transport.setIsDelete((byte) 1);
        transport.setUpdatedDate(LocalDateTime.now());
        transportMethodRepository.save(transport);
    }

    public List<TransportMethod> searchTransportMethods(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return transportMethodRepository.findByIsDeleteFalse();
        }
        return transportMethodRepository.searchByName(keyword);
    }
}