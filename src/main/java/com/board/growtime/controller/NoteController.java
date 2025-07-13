package com.board.growtime.controller;

import com.board.growtime.entity.Note;
import com.board.growtime.service.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
@Slf4j
public class NoteController {

    private final NoteService noteService;

    /**
     * 회고 작성
     */
    @PostMapping("/{githubId}")
    public ResponseEntity<Map<String, Object>> createNote(
            @PathVariable String githubId,
            @RequestBody Map<String, String> request) {
        
        try {
            String title = request.get("title");
            String content = request.get("content");
            
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "제목은 필수입니다."));
            }
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "내용은 필수입니다."));
            }
            
            Note note = noteService.createNote(githubId, title, content);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "회고가 성공적으로 작성되었습니다.");
            response.put("note", Map.of(
                "id", note.getId(),
                "title", note.getTitle(),
                "content", note.getContent(),
                "createdAt", note.getCreatedAt()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("회고 작성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("회고 작성 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 회고 목록 조회
     */
    @GetMapping("/{githubId}")
    public ResponseEntity<Map<String, Object>> getNotes(
            @PathVariable String githubId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Note> notesPage = noteService.getNotesWithPaging(githubId, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("notes", notesPage.getContent().stream().map(note -> Map.of(
                "id", note.getId(),
                "title", note.getTitle(),
                "content", note.getContent(),
                "createdAt", note.getCreatedAt(),
                "updatedAt", note.getUpdatedAt()
            )).toList());
            response.put("totalElements", notesPage.getTotalElements());
            response.put("totalPages", notesPage.getTotalPages());
            response.put("currentPage", notesPage.getNumber());
            response.put("size", notesPage.getSize());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("회고 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("회고 목록 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 회고 상세 조회
     */
    @GetMapping("/{githubId}/{noteId}")
    public ResponseEntity<Map<String, Object>> getNote(
            @PathVariable String githubId,
            @PathVariable Long noteId) {
        
        try {
            Note note = noteService.getNote(githubId, noteId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("note", Map.of(
                "id", note.getId(),
                "title", note.getTitle(),
                "content", note.getContent(),
                "createdAt", note.getCreatedAt(),
                "updatedAt", note.getUpdatedAt()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("회고 상세 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("회고 상세 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 회고 수정
     */
    @PutMapping("/{githubId}/{noteId}")
    public ResponseEntity<Map<String, Object>> updateNote(
            @PathVariable String githubId,
            @PathVariable Long noteId,
            @RequestBody Map<String, String> request) {
        
        try {
            String title = request.get("title");
            String content = request.get("content");
            
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "제목은 필수입니다."));
            }
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "내용은 필수입니다."));
            }
            
            Note note = noteService.updateNote(githubId, noteId, title, content);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "회고가 성공적으로 수정되었습니다.");
            response.put("note", Map.of(
                "id", note.getId(),
                "title", note.getTitle(),
                "content", note.getContent(),
                "updatedAt", note.getUpdatedAt()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("회고 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("회고 수정 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 회고 삭제
     */
    @DeleteMapping("/{githubId}/{noteId}")
    public ResponseEntity<Map<String, Object>> deleteNote(
            @PathVariable String githubId,
            @PathVariable Long noteId) {
        
        try {
            noteService.deleteNote(githubId, noteId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "회고가 성공적으로 삭제되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("회고 삭제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("회고 삭제 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 회고 검색
     */
    @GetMapping("/{githubId}/search")
    public ResponseEntity<Map<String, Object>> searchNotes(
            @PathVariable String githubId,
            @RequestParam String keyword) {
        
        try {
            List<Note> notes = noteService.searchNotes(githubId, keyword);
            
            Map<String, Object> response = new HashMap<>();
            response.put("notes", notes.stream().map(note -> Map.of(
                "id", note.getId(),
                "title", note.getTitle(),
                "content", note.getContent(),
                "createdAt", note.getCreatedAt()
            )).toList());
            response.put("totalCount", notes.size());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("회고 검색 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("회고 검색 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 회고 개수 조회
     */
    @GetMapping("/{githubId}/count")
    public ResponseEntity<Map<String, Object>> getNoteCount(@PathVariable String githubId) {
        try {
            long count = noteService.getNoteCount(githubId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("회고 개수 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("회고 개수 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }
} 