package org.rcbg.device_management_service.services.unit_tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rcbg.device_management_service.enums.HomeAccessRole;
import org.rcbg.device_management_service.exceptions.AccessDeniedException;
import org.rcbg.device_management_service.exceptions.InvalidMembersRequestException;
import org.rcbg.device_management_service.exceptions.ObjectDoesNotExistException;
import org.rcbg.device_management_service.models.dto.home_access.MembersPostRequestDto;
import org.rcbg.device_management_service.models.dto.home_access.MembersPostResponseDto;
import org.rcbg.device_management_service.models.dto.home_access.RoleGetResponseDto;
import org.rcbg.device_management_service.models.entities.Home;
import org.rcbg.device_management_service.models.entities.HomeAccess;
import org.rcbg.device_management_service.repositories.HomeAccessRepository;
import org.rcbg.device_management_service.services.ResourceAccessManagementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ResourceAccessManagementServiceTest {

    @Mock
    private HomeAccessRepository repository;

    @InjectMocks
    private ResourceAccessManagementService resourceAccessManagementService;

    @Captor
    private ArgumentCaptor<List<HomeAccess>> saveAllCaptor;


    @Test
    void testCheckIfUserHasAccessSuccessfulExactRole() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        Home home = new Home();
        home.setHomeId(UUID.randomUUID());
        HomeAccess access = new HomeAccess(1, home, userId, HomeAccessRole.MANAGER);

        when(repository.findByHomeAndUserId(home, userId)).thenReturn(Optional.of(access));

        // WHEN - THEN
        assertDoesNotThrow(() ->
                resourceAccessManagementService.checkIfUserHasAccess(home, userId, HomeAccessRole.MANAGER)
        );
    }

    @Test
    void testCheckIfUserHasAccessSuccessfulHigherRole() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        Home home = new Home();
        home.setHomeId(UUID.randomUUID());
        HomeAccess access = new HomeAccess(1, home, userId, HomeAccessRole.MANAGER);

        when(repository.findByHomeAndUserId(home, userId)).thenReturn(Optional.of(access));

        // WHEN - THEN
        assertDoesNotThrow(() ->
                resourceAccessManagementService.checkIfUserHasAccess(home, userId, HomeAccessRole.VIEWER)
        );
    }

    @Test
    void testCheckIfUserHasAccessUserHasNoRoleAtAll() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        Home home = new Home();
        home.setHomeId(UUID.randomUUID());

        when(repository.findByHomeAndUserId(home, userId)).thenReturn(Optional.empty());

        // WHEN - THEN
        assertThrows(ObjectDoesNotExistException.class, () ->
                        resourceAccessManagementService.checkIfUserHasAccess(home, userId, HomeAccessRole.VIEWER),
                "Expected exception when user does not exist in home access list (masks home existence)"
        );
    }

    @Test
    void testCheckIfUserHasAccessTooLowRole() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        Home home = new Home();
        home.setHomeId(UUID.randomUUID());

        HomeAccess access = new HomeAccess(1, home, userId, HomeAccessRole.VIEWER);

        when(repository.findByHomeAndUserId(home, userId)).thenReturn(Optional.of(access));

        // WHEN - THEN
        assertThrows(AccessDeniedException.class, () ->
                        resourceAccessManagementService.checkIfUserHasAccess(home, userId, HomeAccessRole.MANAGER),
                "Expected exception when user has access, but role is too low"
        );
    }

    @Test
    void testHandleMembersPostRequestFullScenario() {
        // GIVEN
        UUID requesterId = UUID.randomUUID();
        Home home = new Home();
        home.setHomeId(UUID.randomUUID());

        UUID userToUpdate = UUID.randomUUID();
        UUID userToAdd = UUID.randomUUID();
        UUID userToDelete = UUID.randomUUID();

        MembersPostRequestDto requestDto = new MembersPostRequestDto();
        requestDto.setAdd(Map.of(
                userToUpdate, HomeAccessRole.MANAGER,
                userToAdd, HomeAccessRole.VIEWER
        ));
        requestDto.setDelete(List.of(userToDelete));

        HomeAccess existingAccessToUpdate = new HomeAccess(1, home, userToUpdate, HomeAccessRole.VIEWER);
        when(repository.findByHome_HomeIdAndUserIdIn(eq(home.getHomeId()), eq(Set.of(userToUpdate, userToAdd))))
                .thenReturn(List.of(existingAccessToUpdate));

        HomeAccess existingAccessToDelete = new HomeAccess(2, home, userToDelete, HomeAccessRole.VIEWER);
        when(repository.findByHome_HomeIdAndUserIdIn(eq(home.getHomeId()), eq(Set.of(userToDelete))))
                .thenReturn(List.of(existingAccessToDelete));

        // WHEN
        MembersPostResponseDto response = resourceAccessManagementService.handleMembersPostRequest(requestDto, home, requesterId);

        // THEN
        assertNotNull(response);
        assertEquals(1, response.getAdded().size());
        assertEquals(1, response.getUpdated().size());
        assertEquals(1, response.getRemoved().size());

        verify(repository).deleteByHomeIdAndUserIds(home.getHomeId(), List.of(userToDelete));

        verify(repository, times(2)).saveAll(saveAllCaptor.capture());

        List<List<HomeAccess>> savedBatches = saveAllCaptor.getAllValues();
        assertEquals(2, savedBatches.size());
    }

    @Test
    void testHandleMembersPostRequestNullAddAndEmptyDelete() {
        // GIVEN
        UUID requesterId = UUID.randomUUID();
        Home home = new Home();
        home.setHomeId(UUID.randomUUID());

        MembersPostRequestDto requestDto = new MembersPostRequestDto();

        requestDto.setAdd(Collections.emptyMap());
        requestDto.setDelete(Collections.emptyList());

        when(repository.findByHome_HomeIdAndUserIdIn(home.getHomeId(), Collections.emptySet()))
                .thenReturn(Collections.emptyList());

        // WHEN
        MembersPostResponseDto response = resourceAccessManagementService.handleMembersPostRequest(requestDto, home, requesterId);

        // THEN
        assertNotNull(response);
        assertTrue(response.getAdded().isEmpty());
        assertTrue(response.getUpdated().isEmpty());
        assertTrue(response.getRemoved().isEmpty());

        verify(repository).deleteByHomeIdAndUserIds(home.getHomeId(), Collections.emptyList());
    }

    @Test
    void testHandleMembersPostRequestDuplicateInDeleteThrowsException() {
        // GIVEN
        UUID requesterId = UUID.randomUUID();
        Home home = new Home();
        UUID duplicatedUserId = UUID.randomUUID();

        MembersPostRequestDto requestDto = new MembersPostRequestDto();
        requestDto.setDelete(List.of(duplicatedUserId, duplicatedUserId));

        // WHEN - THEN
        assertThrows(InvalidMembersRequestException.class, () ->
                        resourceAccessManagementService.handleMembersPostRequest(requestDto, home, requesterId),
                "Expected exception when there are duplicate UUIDs in delete list"
        );
        verify(repository, never()).saveAll(any());
        verify(repository, never()).deleteByHomeIdAndUserIds(any(), any());
    }

    @Test
    void testHandleMembersPostRequestCollidingIdsThrowsException() {
        // GIVEN
        UUID requesterId = UUID.randomUUID();
        Home home = new Home();
        UUID collidingUserId = UUID.randomUUID();

        MembersPostRequestDto requestDto = new MembersPostRequestDto();
        requestDto.setAdd(Map.of(collidingUserId, HomeAccessRole.VIEWER));
        requestDto.setDelete(List.of(collidingUserId));

        // WHEN - THEN
        assertThrows(InvalidMembersRequestException.class, () ->
                        resourceAccessManagementService.handleMembersPostRequest(requestDto, home, requesterId),
                "Expected exception when the same UUID is in both add and delete sections"
        );
        verify(repository, never()).saveAll(any());
        verify(repository, never()).deleteByHomeIdAndUserIds(any(), any());
    }

    @Test
    void testGetMembersByHomeSuccessful() {
        // GIVEN
        Home home = new Home();
        home.setHomeId(UUID.randomUUID());

        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        List<HomeAccess> mockedAccessList = List.of(
                new HomeAccess(1, home, userId1, HomeAccessRole.MANAGER),
                new HomeAccess(2, home, userId2, HomeAccessRole.VIEWER)
        );

        Pageable pageable = PageRequest.of(0, 20);
        Page<HomeAccess> mockedPage = new PageImpl<>(mockedAccessList);

        when(repository.findAllByHome_HomeId(home.getHomeId(), pageable)).thenReturn(mockedPage);

        // WHEN
        Page<RoleGetResponseDto> result = resourceAccessManagementService.getMembersByHome(home, pageable);

        // THEN
        assertNotNull(result, "Result page should not be null");
        assertNotNull(result.getContent(), "Page content should not be null");
        assertEquals(2, result.getContent().size(), "Should contain exactly 2 members");
        assertEquals(userId1, result.getContent().get(0).getUserId());
        assertEquals(HomeAccessRole.MANAGER, result.getContent().get(0).getRole());
    }

    @Test
    void testGetMembersByHomeEmptyList() {
        // GIVEN
        Home home = new Home();
        home.setHomeId(UUID.randomUUID());

        Pageable pageable = PageRequest.of(0, 20);
        Page<HomeAccess> emptyPage = new PageImpl<>(Collections.emptyList());

        when(repository.findAllByHome_HomeId(home.getHomeId(), pageable)).thenReturn(emptyPage);

        // WHEN
        Page<RoleGetResponseDto> result = resourceAccessManagementService.getMembersByHome(home, pageable);

        // THEN
        assertNotNull(result, "Result page should not be null");
        assertTrue(result.isEmpty(), "Result page should be empty");
        assertTrue(result.getContent().isEmpty(), "Page content list should be empty");
    }
}