package com.doosan.christmas.common.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Menu {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int price;
    private String category;
} 