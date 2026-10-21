package org.example.schoolback.entity;

import jakarta.persistence.*;
import org.hibernate.Hibernate;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "presents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Present {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price_coins", nullable = false)
    private Long priceCoins;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    // ОДИН подарок -> МНОГО фото
    @OneToMany(mappedBy = "present", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PhotoOfPresent> photos = new ArrayList<>();


    public void removePhoto(PhotoOfPresent photo) {
        this.photos.remove(photo);
        photo.setPresent(null); // разрываем обратную связь
    }

    // Хелпер-метод для добавления фото
    public void addPhoto(PhotoOfPresent photo) {
        photos.add(photo);
        photo.setPresent(this);
    }

    /**
     * Сущности считаются одинаковыми только по идентификатору.
     * Несохранённые объекты (идентификатора ещё нет) равны лишь сами себе.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        return id != null && id.equals(((Present) o).id);
    }

    /** Постоянное значение: отпечаток не меняется при изменении полей. */
    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
