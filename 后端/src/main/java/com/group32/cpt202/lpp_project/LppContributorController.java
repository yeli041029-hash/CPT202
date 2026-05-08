package com.group32.cpt202.lpp_project;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/api/lpp/resources")
public class LppContributorController {
    private final DraftService draftService;

    public LppContributorController(DraftService draftService) {
        this.draftService = draftService;
    }

    @PostMapping("/drafts")
    public Result saveDraft(@RequestBody ResourceDraft draft) {
        return draftService.saveDraft(draft);
    }

    @PutMapping("/drafts/{draftId}/submit")
    public Result submitDraft(@PathVariable Long draftId) {
        return draftService.submitDraft(draftId);
    }

    @GetMapping("/users/{userId}/drafts")
    public Result getMyDrafts(@PathVariable Long userId) {
        return draftService.getMyDrafts(userId);
    }

    @GetMapping("/users/{userId}")
    public Result getMyResources(@PathVariable Long userId) {
        return draftService.getMyResources(userId);
    }

    @DeleteMapping("/drafts/{draftId}")
    public Result deleteDraft(@PathVariable Long draftId, @RequestParam Long userId) {
        return draftService.deleteDraft(draftId, userId);
    }

    @DeleteMapping("/{resourceId}")
    public Result deleteResource(@PathVariable Long resourceId, @RequestParam Long userId) {
        return draftService.deleteResource(resourceId, userId);
    }

    @PutMapping("/{resourceId}/upload")
    public Result uploadApprovedResource(@PathVariable Long resourceId, @RequestParam Long userId) {
        return draftService.uploadApprovedResource(resourceId, userId);
    }
}
