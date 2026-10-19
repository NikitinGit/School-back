package org.example.schoolback.entity;

import jakarta.persistence.*;
import org.hibernate.Hibernate;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "login", nullable = false, unique = true)
	private String login;

	@Column(name = "password", nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	private Role role;

	@Column(name = "first_name", nullable = false)
	private String firstName;

	@Column(name = "second_name", nullable = false)
	private String secondName;

	@Column(name = "patronymic_name")
	private String patronymicName;

	@Column(name = "email")
	private String email;

	@Column(name = "birth_date")
	private LocalDate birthDate;

	@Column(name = "balance_coins")
	private Long coins;

	@Column(name = "balance_exp", nullable = false)
	private Long experience = 0L;

	@Column(name = "rank_level", nullable = false)
	private Integer rankLevel = 1;

	@OneToMany(mappedBy = "teacher")
	private Set<Group> teachingGroups = new HashSet<>();

	@ManyToMany(mappedBy = "participants")
	private Set<Group> groups = new HashSet<>();

	public String getFullName() {
		return firstName + " " + secondName + (StringUtils.isBlank(patronymicName) ? "" : " " + patronymicName);
	}

	/**
	 * Сущности считаются одинаковыми только по идентификатору.
	 * Несохранённые объекты (идентификатора ещё нет) равны лишь сами себе.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		return id != null && id.equals(((User) o).id);
	}

	/** Постоянное значение: отпечаток не меняется при изменении полей. */
	@Override
	public int hashCode() {
		return Hibernate.getClass(this).hashCode();
	}
}
