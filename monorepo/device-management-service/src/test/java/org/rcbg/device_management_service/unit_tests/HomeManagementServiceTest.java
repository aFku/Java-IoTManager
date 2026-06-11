package org.rcbg.device_management_service.unit_tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.any;

import org.mockito.Spy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.rcbg.device_management_service.enums.HomeAccessRole;
import org.rcbg.device_management_service.exceptions.AccessDeniedException;
import org.rcbg.device_management_service.exceptions.InvalidMembersRequestException;
import org.rcbg.device_management_service.exceptions.ObjectDoesNotExistException;
import org.rcbg.device_management_service.mappers.HomeMapper;
import org.rcbg.device_management_service.models.dto.home_access.MembersPostRequestDto;
import org.rcbg.device_management_service.models.dto.home_access.MembersPostResponseDto;
import org.rcbg.device_management_service.models.dto.home_access.RoleGetResponseDto;
import org.rcbg.device_management_service.models.dto.homes.RequestHomeDto;
import org.rcbg.device_management_service.models.dto.homes.ResponseHomeDto;
import org.rcbg.device_management_service.models.entities.Home;
import org.rcbg.device_management_service.repositories.HomeAccessRepository;
import org.rcbg.device_management_service.repositories.HomeRepository;
import org.rcbg.device_management_service.services.HomeManagementService;
import org.rcbg.device_management_service.services.ResourceAccessManagementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

@ExtendWith(MockitoExtension.class)
public class HomeManagementServiceTest {

    @Mock
    private HomeRepository homeRepository;

    @Mock
    private HomeAccessRepository homeAccessRepository;

    @Mock
    private ResourceAccessManagementService resourceAccessManagementService;

    @Spy
    private HomeMapper homeMapper = org.mapstruct.factory.Mappers.getMapper(HomeMapper.class);

    @InjectMocks
    private HomeManagementService homeManagementService;

    @Test
    void testGetHomeSuccessful() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Home home = new Home();
        home.setHomeId(homeId);
        home.setName("My Home");
        when(homeRepository.findById(homeId)).thenReturn(Optional.of(home));

        //WHEN
        ResponseHomeDto dto = homeManagementService.getHome(homeId, userId);

        // THEN
        assertEquals(homeId, dto.getHomeId(), "Home ID should be the same");
        assertEquals("My Home", dto.getName() , "Home name should be the same");
    }

    @Test
    void testGetHomeThatDoesNotExist() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(homeRepository.findById(homeId)).thenReturn(Optional.empty());

        //WHEN - THEN
        assertThrows(
                ObjectDoesNotExistException.class,
                () -> homeManagementService.getHome(homeId, userId),
                "Service should throw exception when no home"
        );
    }

    @Test
    void testGetHomeWithoutAccess() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Home home = new Home();
        home.setHomeId(homeId);
        home.setName("My Home");
        when(homeRepository.findById(homeId)).thenReturn(Optional.of(home));
        doThrow(new ObjectDoesNotExistException("", userId))
                .when(resourceAccessManagementService)
                .checkIfUserHasAccess(home,userId,HomeAccessRole.VIEWER);

        //WHEN - THEN
        assertThrows(
                ObjectDoesNotExistException.class,
                () -> homeManagementService.getHome(homeId, userId),
                "Service should throw exception when user does not have access"
        );
    }

    @Test
    void testGetListOfHomesSuccessful() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        Home home1 = new Home();
        home1.setHomeId(UUID.randomUUID());
        home1.setName("Home 1");

        Home home2 = new Home();
        home2.setHomeId(UUID.randomUUID());
        home2.setName("Home 2");
        Pageable page = PageRequest.of(0, 20);

        when(homeRepository.findAllByUserId(userId, page)).thenReturn(new PageImpl<>(List.of(home1, home2)));

        // WHEN
        Page<ResponseHomeDto> result = homeManagementService.getListOfHomes(userId, page);

        // THEN
        assertEquals(2, result.getTotalElements(), "List should contain exactly 2 elements");
        assertEquals("Home 1", result.getContent().get(0).getName(), "First element name should match");
        assertEquals("Home 2", result.getContent().get(1).getName(), "Second element name should match");
    }

    @Test
    void testGetListOfHomesEmpty() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        Pageable page = PageRequest.of(0, 20);
        when(homeRepository.findAllByUserId(userId, page)).thenReturn(new PageImpl<>(Collections.emptyList()));


        // WHEN
        Page<ResponseHomeDto> result = homeManagementService.getListOfHomes(userId, page);

        // THEN
        assertTrue(result.getContent().isEmpty(), "List should be empty when no homes found");
        assertEquals(0, result.getTotalElements(), "Total elements should be 0");
    }

    @Test
    void testGetListOfHomesOnlyOwnedByUser() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        Pageable page = PageRequest.of(0, 20);
        when(homeRepository.findAllByUserId(userId, page)).thenReturn(new PageImpl<>(Collections.emptyList()));

        // WHEN
        homeManagementService.getListOfHomes(userId, page);

        // THEN
        verify(homeRepository).findAllByUserId(userId, page);
    }

    @Test
    void testCreateHomeSuccessful() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        UUID homeId = UUID.randomUUID();

        RequestHomeDto requestDto = new RequestHomeDto();
        requestDto.setName("New Home");

        Home savedHome = new Home();
        savedHome.setHomeId(homeId);
        savedHome.setName("New Home");

        when(homeRepository.save(any(Home.class))).thenReturn(savedHome);

        // WHEN
        ResponseHomeDto result = homeManagementService.createHome(requestDto, userId);

        // THEN
        assertEquals(homeId, result.getHomeId(), "Returned DTO should have correct ID");
        assertEquals("New Home", result.getName(), "Returned DTO should have correct Name");

        verify(homeRepository).save(any(Home.class));
        verify(homeRepository).flush();

        ArgumentCaptor<MembersPostRequestDto> captor = ArgumentCaptor.forClass(MembersPostRequestDto.class);
        verify(resourceAccessManagementService).handleMembersPostRequest(captor.capture(), eq(savedHome), eq(userId));

        MembersPostRequestDto accessRequest = captor.getValue();
        assertEquals(HomeAccessRole.MANAGER, accessRequest.getAdd().get(userId), "Creator should be added as MANAGER");
        assertTrue(accessRequest.getDelete().isEmpty(), "Delete list should be empty");
    }

    @Test
    void testCreateHomeIncorrectValues() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        RequestHomeDto requestDto = new RequestHomeDto();

        when(homeRepository.save(any(Home.class))).thenThrow(new IllegalArgumentException("Invalid data"));

        // WHEN - THEN
        assertThrows(
                IllegalArgumentException.class,
                () -> homeManagementService.createHome(requestDto, userId),
                "Service should propagate exception from repository/database"
        );
    }

    @Test
    void testUpdateHomeSuccessful() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        RequestHomeDto requestDto = new RequestHomeDto();
        requestDto.setName("Updated Home");

        Home existingHome = new Home();
        existingHome.setHomeId(homeId);
        existingHome.setName("Old Home");

        when(homeRepository.findById(homeId)).thenReturn(Optional.of(existingHome));

        // WHEN
        ResponseHomeDto result = homeManagementService.updateHome(homeId, userId, requestDto);

        // THEN
        assertEquals("Updated Home", result.getName(), "Home name should be updated by mapper");
        verify(resourceAccessManagementService).checkIfUserHasAccess(existingHome, userId, HomeAccessRole.MANAGER);
    }

    @Test
    void testUpdateHomeIncorrectValues() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Home existingHome = new Home();
        existingHome.setHomeId(homeId);

        when(homeRepository.findById(homeId)).thenReturn(Optional.of(existingHome));

        doThrow(new IllegalArgumentException("Bad DTO")).when(homeMapper).updateHomeFromDto(any(), any());

        // WHEN - THEN
        assertThrows(
                IllegalArgumentException.class,
                () -> homeManagementService.updateHome(homeId, userId, new RequestHomeDto()),
                "Service should propagate mapping exception"
        );
    }

    @Test
    void testUpdateHomeWithoutAccess() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Home existingHome = new Home();
        existingHome.setHomeId(homeId);

        when(homeRepository.findById(homeId)).thenReturn(Optional.of(existingHome));
        doThrow(new ObjectDoesNotExistException("No access", userId))
                .when(resourceAccessManagementService)
                .checkIfUserHasAccess(existingHome, userId, HomeAccessRole.MANAGER);

        // WHEN - THEN
        assertThrows(
                ObjectDoesNotExistException.class,
                () -> homeManagementService.updateHome(homeId, userId, new RequestHomeDto()),
                "Should throw exception when user lacks basic access"
        );
    }

    @Test
    void testUpdateHomeWithToLowAccess() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Home existingHome = new Home();
        existingHome.setHomeId(homeId);

        when(homeRepository.findById(homeId)).thenReturn(Optional.of(existingHome));
        doThrow(new AccessDeniedException("Access Denied - role too low", userId))
                .when(resourceAccessManagementService)
                .checkIfUserHasAccess(existingHome, userId, HomeAccessRole.MANAGER);

        // WHEN - THEN
        assertThrows(
                RuntimeException.class,
                () -> homeManagementService.updateHome(homeId, userId, new RequestHomeDto()),
                "Should throw exception when role is lower than MANAGER"
        );
    }

    @Test
    void testUpdateHomeDoesNotExist() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(homeRepository.findById(homeId)).thenReturn(Optional.empty());

        // WHEN - THEN
        assertThrows(
                ObjectDoesNotExistException.class,
                () -> homeManagementService.updateHome(homeId, userId, new RequestHomeDto()),
                "Service should throw exception when no home to update"
        );
    }

    @Test
    void testDeleteHomeSuccessful() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Home home = new Home();
        home.setHomeId(homeId);

        when(homeRepository.findById(homeId)).thenReturn(Optional.of(home));

        // WHEN
        homeManagementService.deleteHome(homeId, userId);

        // THEN
        verify(resourceAccessManagementService).checkIfUserHasAccess(home, userId, HomeAccessRole.MANAGER);
        verify(homeRepository).delete(home);
    }

    @Test
    void testDeleteHomeWithoutAccess() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Home home = new Home();
        home.setHomeId(homeId);

        when(homeRepository.findById(homeId)).thenReturn(Optional.of(home));
        doThrow(new ObjectDoesNotExistException("No access", userId))
                .when(resourceAccessManagementService)
                .checkIfUserHasAccess(home, userId, HomeAccessRole.MANAGER);

        // WHEN - THEN
        assertThrows(
                ObjectDoesNotExistException.class,
                () -> homeManagementService.deleteHome(homeId, userId),
                "Should throw exception before deletion"
        );
        verify(homeRepository, never()).delete(any());
    }

    @Test
    void testDeleteHomeWithToLowAccess() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Home home = new Home();
        home.setHomeId(homeId);

        when(homeRepository.findById(homeId)).thenReturn(Optional.of(home));
        doThrow(new AccessDeniedException("Access Denied - role too low", userId))
                .when(resourceAccessManagementService)
                .checkIfUserHasAccess(home, userId, HomeAccessRole.MANAGER);

        // WHEN - THEN
        assertThrows(
                RuntimeException.class,
                () -> homeManagementService.deleteHome(homeId, userId),
                "Should throw exception before deletion due to low role"
        );
        verify(homeRepository, never()).delete(any());
    }

    @Test
    void testDeleteHomeDoesNotExist() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(homeRepository.findById(homeId)).thenReturn(Optional.empty());

        // WHEN - THEN
        assertThrows(
                ObjectDoesNotExistException.class,
                () -> homeManagementService.deleteHome(homeId, userId),
                "Service should throw exception when no home to delete"
        );
        verify(homeRepository, never()).delete(any());
    }

    @Test
    void testFindHomeSuccessful() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Home home = new Home();
        home.setHomeId(homeId);

        when(homeRepository.findById(homeId)).thenReturn(Optional.of(home));

        // WHEN
        Home foundHome = homeManagementService.findHome(homeId, userId);

        // THEN
        assertNotNull(foundHome, "Found home should not be null");
        assertEquals(homeId, foundHome.getHomeId(), "IDs should match");
    }

    @Test
    void testFindHomeDoesNotExist() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(homeRepository.findById(homeId)).thenReturn(Optional.empty());

        // WHEN - THEN
        assertThrows(
                ObjectDoesNotExistException.class,
                () -> homeManagementService.findHome(homeId, userId),
                "Should throw exception when home not found in DB"
        );
    }

    @Test
    void testGetHomeMembersSuccessful() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Home home = new Home();
        home.setHomeId(homeId);
        Pageable pageable = PageRequest.of(0, 20);

        Page<RoleGetResponseDto> expectedResponse = new PageImpl<>(Collections.emptyList());

        when(homeRepository.findById(homeId)).thenReturn(Optional.of(home));
        when(resourceAccessManagementService.getMembersByHome(home, pageable)).thenReturn(expectedResponse);

        // WHEN
        Page<RoleGetResponseDto> result = homeManagementService.getHomeMembers(homeId, userId, pageable);

        // THEN
        assertNotNull(result, "Response should not be null");
        assertEquals(expectedResponse, result, "Returned response should match the one from access service");
        verify(resourceAccessManagementService).checkIfUserHasAccess(home, userId, HomeAccessRole.VIEWER);
        verify(resourceAccessManagementService).getMembersByHome(home, pageable);
    }

    @Test
    void testGetHomeMembersWithoutAccess() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Home home = new Home();
        home.setHomeId(homeId);
        Pageable pageable = PageRequest.of(0, 20);

        when(homeRepository.findById(homeId)).thenReturn(Optional.of(home));
        doThrow(new ObjectDoesNotExistException("No access", userId))
                .when(resourceAccessManagementService)
                .checkIfUserHasAccess(home, userId, HomeAccessRole.VIEWER);

        // WHEN - THEN
        assertThrows(
                ObjectDoesNotExistException.class,
                () -> homeManagementService.getHomeMembers(homeId, userId, pageable),
                "Should throw exception when user lacks VIEWER access"
        );
        verify(resourceAccessManagementService, never()).getMembersByHome(any(Home.class), any(Pageable.class));
    }

    @Test
    void testGetHomeMembersHomeDoesNotExist() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);

        when(homeRepository.findById(homeId)).thenReturn(Optional.empty());

        // WHEN - THEN
        assertThrows(
                ObjectDoesNotExistException.class,
                () -> homeManagementService.getHomeMembers(homeId, userId, pageable),
                "Should throw exception when home is not found"
        );
        verify(resourceAccessManagementService, never()).getMembersByHome(any(Home.class), any(Pageable.class));
    }

    @Test
    void testUpdateHomeMembersSuccessful() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Home home = new Home();
        home.setHomeId(homeId);

        MembersPostRequestDto requestDto = new MembersPostRequestDto();
        MembersPostResponseDto expectedResponse = new MembersPostResponseDto(Collections.emptyList(), Collections.emptyList());

        when(homeRepository.findById(homeId)).thenReturn(Optional.of(home));
        when(resourceAccessManagementService.handleMembersPostRequest(requestDto, home, userId)).thenReturn(expectedResponse);

        // WHEN
        MembersPostResponseDto result = homeManagementService.updateHomeMembers(homeId, userId, requestDto);

        // THEN
        assertNotNull(result, "Response should not be null");
        assertEquals(expectedResponse, result, "Returned response should match the one from access service");
        verify(resourceAccessManagementService).checkIfUserHasAccess(home, userId, HomeAccessRole.MANAGER);
        verify(resourceAccessManagementService).handleMembersPostRequest(requestDto, home, userId);
    }

    @Test
    void testUpdateHomeMembersWithoutAccess() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Home home = new Home();
        home.setHomeId(homeId);

        MembersPostRequestDto requestDto = new MembersPostRequestDto();

        when(homeRepository.findById(homeId)).thenReturn(Optional.of(home));
        doThrow(new ObjectDoesNotExistException("No access", userId))
                .when(resourceAccessManagementService)
                .checkIfUserHasAccess(home, userId, HomeAccessRole.MANAGER);

        // WHEN - THEN
        assertThrows(
                ObjectDoesNotExistException.class,
                () -> homeManagementService.updateHomeMembers(homeId, userId, requestDto),
                "Should throw exception when user lacks MANAGER access"
        );
        verify(resourceAccessManagementService, never()).handleMembersPostRequest(any(), any(), any());
    }

    @Test
    void testUpdateHomeMembersInvalidRequest() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Home home = new Home();
        home.setHomeId(homeId);

        MembersPostRequestDto requestDto = new MembersPostRequestDto();

        when(homeRepository.findById(homeId)).thenReturn(Optional.of(home));

        doThrow(new InvalidMembersRequestException("Duplicate UUIDs", userId))
                .when(resourceAccessManagementService)
                .handleMembersPostRequest(requestDto, home, userId);

        // WHEN - THEN
        assertThrows(
                InvalidMembersRequestException.class,
                () -> homeManagementService.updateHomeMembers(homeId, userId, requestDto),
                "Should propagate InvalidMembersRequestException from access management service"
        );
    }

    @Test
    void testUpdateHomeMembersHomeDoesNotExist() {
        // GIVEN
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        MembersPostRequestDto requestDto = new MembersPostRequestDto();

        when(homeRepository.findById(homeId)).thenReturn(Optional.empty());

        // WHEN - THEN
        assertThrows(
                ObjectDoesNotExistException.class,
                () -> homeManagementService.updateHomeMembers(homeId, userId, requestDto),
                "Should throw exception when home is not found"
        );
        verify(resourceAccessManagementService, never()).handleMembersPostRequest(any(), any(), any());
    }
}
