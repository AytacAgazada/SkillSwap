package com.example.communityservice.service;

import com.example.communityservice.dto.CreateGroupDto;
import com.example.communityservice.dto.CreateProblemDto;
import com.example.communityservice.dto.GroupDto;
import com.example.communityservice.dto.ProblemDto;
import com.example.communityservice.entity.Group;
import com.example.communityservice.entity.Problem;
import com.example.communityservice.event.ProblemSolvedEvent;
import com.example.communityservice.mapper.GroupMapper;
import com.example.communityservice.mapper.ProblemMapper;
import com.example.communityservice.repository.GroupRepository;
import com.example.communityservice.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityService {

    private final GroupRepository groupRepository;
    private final ProblemRepository problemRepository;
    private final GroupMapper groupMapper;
    private final ProblemMapper problemMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public GroupDto createGroup(CreateGroupDto createGroupDto) {
        Group group = groupMapper.toEntity(createGroupDto);
        group.setCreatedAt(LocalDateTime.now());
        group.setMembers(Collections.singletonList(createGroupDto.getCreatedByUserId()));
        Group savedGroup = groupRepository.save(group);
        log.info("Group created: {}", savedGroup);
        return groupMapper.toDto(savedGroup);
    }

    public List<GroupDto> getGroups() {
        return groupRepository.findAll().stream().map(groupMapper::toDto).toList();
    }

    public void joinGroup(Long groupId, String userId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        group.getMembers().add(userId);
        groupRepository.save(group);
        log.info("User {} joined group {}", userId, groupId);
    }

    public ProblemDto createProblem(CreateProblemDto createProblemDto) {
        Problem problem = problemMapper.toEntity(createProblemDto);
        problem.setCreatedAt(LocalDateTime.now());
        problem.setSolved(false);
        Problem savedProblem = problemRepository.save(problem);
        log.info("Problem created: {}", savedProblem);
        return problemMapper.toDto(savedProblem);
    }

    public Page<ProblemDto> getProblems(Long groupId, Pageable pageable) {
        return problemRepository.findByGroupId(groupId, pageable).map(problemMapper::toDto);
    }

    public void solveProblem(Long problemId, String userId) {
        Problem problem = problemRepository.findById(problemId).orElseThrow(() -> new RuntimeException("Problem not found"));
        problem.setSolved(true);
        problem.setSolvedByUserId(userId);
        problem.setSolvedAt(LocalDateTime.now());
        problemRepository.save(problem);
        log.info("Problem {} solved by user {}", problemId, userId);

        ProblemSolvedEvent event = new ProblemSolvedEvent(problemId, userId, problem.getSolvedAt());
        kafkaTemplate.send("problem-solved-topic", event);
        log.info("Sent ProblemSolvedEvent: {}", event);
    }
}
