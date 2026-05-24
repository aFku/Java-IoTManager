package org.rcbg.device_management_service.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.rcbg.device_management_service.enums.HomeAccessRole;

import java.util.UUID;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "home_access")
public class HomeAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne
    @JoinColumn(name = "home_id", nullable = false)
    private Home home;
    private UUID userId;

    @Enumerated(EnumType.STRING)
    private HomeAccessRole role;
}
