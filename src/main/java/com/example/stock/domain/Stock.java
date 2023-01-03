package com.example.stock.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long quantity;

    @Version
    private Long version;

    @Builder
    public Stock(Long id, Long quantity) {
        this.id = id;
        this.quantity = quantity;
    }

    public void decrease(Long quantity) {
        if (this.quantity - quantity < 0) {
            throw new RuntimeException("재고가 부족합니다");
        }
        this.quantity = this.quantity - quantity;
    }
}
