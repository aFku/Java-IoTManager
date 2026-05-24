package org.rcbg.device_management_service.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "homes")
public class Home {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID homeId;

    @NotNull
    @Column(nullable = false, length = 100)
    private String name;

    @OneToMany(mappedBy = "home", fetch = FetchType.LAZY)
    private List<Device> devices;

    @OneToMany(mappedBy = "home")
    private List<HomeAccess> accesses;
}
