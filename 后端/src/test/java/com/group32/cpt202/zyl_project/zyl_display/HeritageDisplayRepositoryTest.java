package com.group32.cpt202.zyl_project.zyl_display;

import com.group32.cpt202.LY_heritage.entity.HeritageItem;
import com.group32.cpt202.LY_heritage.repository.HeritageItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HeritageDisplayRepositoryTest {

    @Mock
    private HeritageItemRepository delegate;

    private HeritageDisplayRepository repository;

    @BeforeEach
    void setUp() {
        repository = new HeritageDisplayRepository(delegate);
    }

    @Test
    void findAllApprovedMapsDelegateItems() {
        HeritageItem item = heritageItem(10L, "APPROVED", true, false);
        item.setTitle("Community Post");
        when(delegate.findByStatusIgnoreCaseAndCommunityPostTrueOrderByUpdatedAtDesc("APPROVED"))
                .thenReturn(List.of(item));

        List<HeritageDisplay> result = repository.findAllApproved();

        assertThat(result).singleElement().satisfies(display -> {
            assertThat(display.getId()).isEqualTo(10L);
            assertThat(display.getTitle()).isEqualTo("Community Post");
            assertThat(display.getPlatformPublished()).isFalse();
        });
    }

    @Test
    void findApprovedByIdReturnsMappedDisplayWhenEligible() {
        HeritageItem item = heritageItem(10L, "APPROVED", true, false);
        when(delegate.findById(10L)).thenReturn(Optional.of(item));

        HeritageDisplay result = repository.findApprovedById(10L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    void findApprovedByIdReturnsNullWhenItemIsNotCommunityApproved() {
        HeritageItem item = heritageItem(10L, "PENDING", false, false);
        when(delegate.findById(10L)).thenReturn(Optional.of(item));

        assertThat(repository.findApprovedById(10L)).isNull();
    }

    @Test
    void findApprovedByCategoryMapsItems() {
        HeritageItem item = heritageItem(10L, "APPROVED", true, false);
        item.setCategory("relics");
        when(delegate.findByCategoryIgnoreCaseAndStatusIgnoreCaseAndCommunityPostTrueOrderByUpdatedAtDesc("relics", "APPROVED"))
                .thenReturn(List.of(item));

        assertThat(repository.findApprovedByCategory("relics")).singleElement()
                .extracting(HeritageDisplay::getCategory)
                .isEqualTo("relics");
    }

    @Test
    void findRecentApprovedRespectsLimit() {
        HeritageItem first = heritageItem(10L, "APPROVED", true, false);
        HeritageItem second = heritageItem(11L, "APPROVED", true, false);
        when(delegate.findTop10ByStatusIgnoreCaseOrderByUpdatedAtDesc("APPROVED"))
                .thenReturn(List.of(first, second));

        List<HeritageDisplay> result = repository.findRecentApproved(1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
    }

    @Test
    void findAllPlatformApprovedMapsItems() {
        HeritageItem item = heritageItem(10L, "APPROVED", false, true);
        when(delegate.findByStatusIgnoreCaseAndPlatformPublishedTrueOrderByUpdatedAtDesc("APPROVED"))
                .thenReturn(List.of(item));

        assertThat(repository.findAllPlatformApproved()).singleElement()
                .extracting(HeritageDisplay::getPlatformPublished)
                .isEqualTo(true);
    }

    @Test
    void findPlatformApprovedByIdReturnsMappedDisplayWhenEligible() {
        HeritageItem item = heritageItem(10L, "APPROVED", false, true);
        when(delegate.findById(10L)).thenReturn(Optional.of(item));

        HeritageDisplay result = repository.findPlatformApprovedById(10L);

        assertThat(result).isNotNull();
        assertThat(result.getPlatformPublished()).isTrue();
    }

    @Test
    void findPlatformApprovedByCategoryMapsItems() {
        HeritageItem item = heritageItem(10L, "APPROVED", false, true);
        item.setCategory("architecture");
        when(delegate.findByCategoryIgnoreCaseAndStatusIgnoreCaseAndPlatformPublishedTrueOrderByUpdatedAtDesc("architecture", "APPROVED"))
                .thenReturn(List.of(item));

        assertThat(repository.findPlatformApprovedByCategory("architecture")).singleElement()
                .extracting(HeritageDisplay::getCategory)
                .isEqualTo("architecture");
    }

    private HeritageItem heritageItem(Long id, String status, boolean communityPost, boolean platformPublished) {
        HeritageItem item = new HeritageItem();
        item.setId(id);
        item.setStatus(status);
        item.setCommunityPost(communityPost);
        item.setPlatformPublished(platformPublished);
        item.setTitle("Title-" + id);
        item.setDescription("Description-" + id);
        item.setCategory("crafts");
        item.setLocation("Suzhou");
        item.setContributorId(1L);
        item.setViewCount(3);
        return item;
    }
}
