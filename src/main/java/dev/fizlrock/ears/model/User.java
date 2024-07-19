package dev.fizlrock.ears.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@Builder
@Table(name = "app_user")
@NoArgsConstructor
@AllArgsConstructor
public class User {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  protected Long id;

  @Column(name = "username")
  @NotBlank
  protected String username;

  @Column(name = "password_hash")
  @NotBlank
  protected String passwordHash;

  @ToString.Exclude
  @Builder.Default
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
  protected Set<AudioRecordInfo> records = new HashSet<>();

}
