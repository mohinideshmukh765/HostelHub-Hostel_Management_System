package com.hostelhub.hostelservice.controller;

import com.hostelhub.hostelservice.dto.request.CreateRoomRequest;
import com.hostelhub.hostelservice.dto.request.UpdateRoomRequest;
import com.hostelhub.hostelservice.dto.response.RoomResponse;
import com.hostelhub.hostelservice.entity.HostelStatus;
import com.hostelhub.hostelservice.entity.RoomType;
import com.hostelhub.hostelservice.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hostels/{hostelId}/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(

            @PathVariable UUID hostelId,

            @Valid
            @RequestBody
            CreateRoomRequest request

    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        roomService.createRoom(
                                hostelId,
                                request
                        )
                );
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> searchRooms(

            @PathVariable UUID hostelId,

            @RequestParam(required = false)
            HostelStatus status,

            @RequestParam(required = false)
            RoomType type,

            @RequestParam(required = false)
            Integer floor

    ) {

        return ResponseEntity.ok(
                roomService.searchRooms(
                        hostelId,
                        status,
                        type,
                        floor
                )
        );
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> getRoom(

            @PathVariable UUID hostelId,

            @PathVariable UUID roomId

    ) {

        return ResponseEntity.ok(
                roomService.getRoom(
                        hostelId,
                        roomId
                )
        );
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(

            @PathVariable UUID hostelId,

            @PathVariable UUID roomId,

            @Valid
            @RequestBody
            UpdateRoomRequest request

    ) {

        return ResponseEntity.ok(
                roomService.updateRoom(
                        hostelId,
                        roomId,
                        request
                )
        );
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(

            @PathVariable UUID hostelId,

            @PathVariable UUID roomId

    ) {

        roomService.deleteRoom(
                hostelId,
                roomId
        );

        return ResponseEntity.noContent().build();
    }
}