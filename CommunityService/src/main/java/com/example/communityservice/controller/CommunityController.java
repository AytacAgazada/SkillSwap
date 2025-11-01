package com.example.communityservice.controller;

import com.example.communityservice.dto.CreateGroupDto;
import com.example.communityservice.dto.CreateProblemDto;
import com.example.communityservice.dto.GroupDto;
import com.example.communityservice.dto.ProblemDto;
import com.example.communityservice.service.CommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
@Tag(name = "Community Service")
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping("/groups/create")
    @Operation(summary = "Create a new group")
    public GroupDto createGroup(@RequestBody CreateGroupDto createGroupDto) {
        return communityService.createGroup(createGroupDto);
    }

    @GetMapping("/groups")
    @Operation(summary = "Get all groups")
    public List<GroupDto> getGroups() {
        return communityService.getGroups();
    }

    @PostMapping("/groups/{groupId}/join")
    @Operation(summary = "Join a group")
    public void joinGroup(@PathVariable Long groupId, @RequestParam String userId) {
        communityService.joinGroup(groupId, userId);
    }

    @PostMapping("/problems/create")
    @Operation(summary = "Create a new problem")
    public ProblemDto createProblem(@RequestBody CreateProblemDto createProblemDto) {
        return communityService.createProblem(createProblemDto);
    }

    @GetMapping("/problems/{groupId}")
    @Operation(summary = "Get problems by group")
    public Page<ProblemDto> getProblems(@PathVariable Long groupId, Pageable pageable) {
        return communityService.getProblems(groupId, pageable);
    }

    @PostMapping("/problems/{problemId}/solve")
    @Operation(summary = "Mark a problem as solved")
    public void solveProblem(@PathVariable Long problemId, @RequestParam String userId) {
        communityService.solveProblem(problemId, userId);
    }
}
