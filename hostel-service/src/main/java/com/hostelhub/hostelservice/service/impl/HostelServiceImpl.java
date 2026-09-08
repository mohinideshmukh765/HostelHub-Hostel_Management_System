package com.hostelhub.hostelservice.service.impl;

import com.hostelhub.hostelservice.entity.HostelStatus;
import com.hostelhub.hostelservice.entity.HostelType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.hostelhub.hostelservice.dto.request.CreateHostelRequest;
import com.hostelhub.hostelservice.dto.request.UpdateHostelRequest;
import com.hostelhub.hostelservice.dto.response.HostelResponse;
import com.hostelhub.hostelservice.entity.Hostel;
import com.hostelhub.hostelservice.exception.ResourceNotFoundException;
import com.hostelhub.hostelservice.mapper.HostelMapper;
import com.hostelhub.hostelservice.repository.HostelRepository;
import com.hostelhub.hostelservice.service.HostelService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HostelServiceImpl implements HostelService {

    private final HostelRepository hostelRepository;
    private final HostelMapper hostelMapper;

    @Override
    @Transactional
    public HostelResponse createHostel(
            CreateHostelRequest request
    ) {

        if (hostelRepository.existsByNameIgnoreCase(
                request.name().trim()
        )) {
            throw new IllegalArgumentException(
                    "Hostel already exists with name: "
                            + request.name()
            );
        }

        Hostel hostel = hostelMapper.toEntity(request);

        Hostel savedHostel =
                hostelRepository.save(hostel);

        return hostelMapper.toResponse(savedHostel);
    }

    @Override
    public HostelResponse getHostel(UUID hostelId) {

        Hostel hostel = findHostel(hostelId);

        return hostelMapper.toResponse(hostel);
    }

    @Override
    public Page<HostelResponse> searchHostels(
            String search,
            String city,
            HostelType type,
            HostelStatus status,
            Pageable pageable
    ) {

        boolean hasSearch =
                search != null && !search.isBlank();

        boolean hasCity =
                city != null && !city.isBlank();

        if (hasSearch && hasCity) {

            return hostelRepository
                    .findByNameContainingIgnoreCaseAndCityIgnoreCase(
                            search.trim(),
                            city.trim(),
                            pageable
                    )
                    .map(hostelMapper::toResponse);
        }

        if (hasSearch) {

            return hostelRepository
                    .findByNameContainingIgnoreCase(
                            search.trim(),
                            pageable
                    )
                    .map(hostelMapper::toResponse);
        }

        if (hasCity && type != null && status != null) {

            return hostelRepository
                    .findByStatusAndTypeAndCityIgnoreCase(
                            status,
                            type,
                            city.trim(),
                            pageable
                    )
                    .map(hostelMapper::toResponse);
        }

        if (hasCity && type != null) {

            return hostelRepository
                    .findByTypeAndCityIgnoreCase(
                            type,
                            city.trim(),
                            pageable
                    )
                    .map(hostelMapper::toResponse);
        }

        if (hasCity && status != null) {

            return hostelRepository
                    .findByStatusAndCityIgnoreCase(
                            status,
                            city.trim(),
                            pageable
                    )
                    .map(hostelMapper::toResponse);
        }

        if (hasCity) {

            return hostelRepository
                    .findByCityIgnoreCase(
                            city.trim(),
                            pageable
                    )
                    .map(hostelMapper::toResponse);
        }

        if (type != null) {

            return hostelRepository
                    .findByType(
                            type,
                            pageable
                    )
                    .map(hostelMapper::toResponse);
        }

        if (status != null) {

            return hostelRepository
                    .findByStatus(
                            status,
                            pageable
                    )
                    .map(hostelMapper::toResponse);
        }

        return hostelRepository
                .findAll(pageable)
                .map(hostelMapper::toResponse);
    }

    @Override
    @Transactional
    public HostelResponse updateHostel(
            UUID hostelId,
            UpdateHostelRequest request
    ) {

        Hostel hostel = findHostel(hostelId);

        hostelMapper.updateEntity(hostel, request);

        Hostel updatedHostel =
                hostelRepository.save(hostel);

        return hostelMapper.toResponse(updatedHostel);
    }

    @Override
    @Transactional
    public void deleteHostel(UUID hostelId) {

        Hostel hostel = findHostel(hostelId);

        hostelRepository.delete(hostel);
    }

    private Hostel findHostel(UUID hostelId) {

        return hostelRepository.findById(hostelId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Hostel not found with id: "
                                        + hostelId
                        ));
    }
}