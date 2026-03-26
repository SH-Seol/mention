package com.yellobook.mention.domain.team.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.yellobook.mention.common.BaseEntity;

@Entity
@Table(name = "teams",
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_team_name", columnNames = "name")
        }
)
public class TeamEntity extends BaseEntity {
    @Column(nullable = false, length = 40)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private boolean searchable;

    protected TeamEntity() {
    }


    public TeamEntity(String name, String description, String phoneNumber, String address, Boolean searchable) {
        this.name = name;
        this.description = description;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.searchable = searchable;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

}

