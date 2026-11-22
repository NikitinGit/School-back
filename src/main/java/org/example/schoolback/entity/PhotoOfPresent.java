package org.example.schoolback.entity;

import jakarta.persistence.*;
import org.hibernate.Hibernate;
import lombok.*;

@Entity
@Table(name = "photos_presents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhotoOfPresent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "photo_data", nullable = false)
    private byte[] photo;

    // МНОГО фото -> ОДИН подарок
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "present_id", nullable = false)
    private Present present;

    /**
     * Сущности считаются одинаковыми только по идентификатору.
     * Несохранённые объекты (идентификатора ещё нет) равны лишь сами себе.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        return id != null && id.equals(((PhotoOfPresent) o).id);
    }

    /** Постоянное значение: отпечаток не меняется при изменении полей. */
    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
