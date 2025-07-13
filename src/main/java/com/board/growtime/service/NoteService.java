package com.board.growtime.service;

import com.board.growtime.entity.Note;
import com.board.growtime.entity.User;
import com.board.growtime.repository.NoteRepository;
import com.board.growtime.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    /**
     * 회고 작성
     */
    public Note createNote(String githubId, String title, String content) {
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + githubId));
        
        Note note = new Note(title, content, user);
        Note savedNote = noteRepository.save(note);
        
        log.info("회고 작성 완료: 사용자={}, 제목={}, ID={}", githubId, title, savedNote.getId());
        return savedNote;
    }

    /**
     * 회고 목록 조회 (최신순)
     */
    @Transactional(readOnly = true)
    public List<Note> getNotes(String githubId) {
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + githubId));
        
        return noteRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * 회고 페이징 조회
     */
    @Transactional(readOnly = true)
    public Page<Note> getNotesWithPaging(String githubId, Pageable pageable) {
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + githubId));
        
        return noteRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    /**
     * 회고 상세 조회
     */
    @Transactional(readOnly = true)
    public Note getNote(String githubId, Long noteId) {
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + githubId));
        
        return noteRepository.findByIdAndUser(noteId, user)
                .orElseThrow(() -> new IllegalArgumentException("회고를 찾을 수 없습니다: " + noteId));
    }

    /**
     * 회고 수정
     */
    public Note updateNote(String githubId, Long noteId, String title, String content) {
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + githubId));
        
        Note note = noteRepository.findByIdAndUser(noteId, user)
                .orElseThrow(() -> new IllegalArgumentException("회고를 찾을 수 없습니다: " + noteId));
        
        note.updateNote(title, content);
        Note updatedNote = noteRepository.save(note);
        
        log.info("회고 수정 완료: 사용자={}, 제목={}, ID={}", githubId, title, noteId);
        return updatedNote;
    }

    /**
     * 회고 삭제
     */
    public void deleteNote(String githubId, Long noteId) {
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + githubId));
        
        Note note = noteRepository.findByIdAndUser(noteId, user)
                .orElseThrow(() -> new IllegalArgumentException("회고를 찾을 수 없습니다: " + noteId));
        
        noteRepository.delete(note);
        log.info("회고 삭제 완료: 사용자={}, ID={}", githubId, noteId);
    }

    /**
     * 회고 검색 (제목 또는 내용)
     */
    @Transactional(readOnly = true)
    public List<Note> searchNotes(String githubId, String keyword) {
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + githubId));
        
        return noteRepository.findByUserAndTitleOrContentContaining(user, keyword);
    }

    /**
     * 사용자의 회고 개수 조회
     */
    @Transactional(readOnly = true)
    public long getNoteCount(String githubId) {
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + githubId));
        
        return noteRepository.countByUser(user);
    }
} 