package dev.fizlrock.ears.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@Table(name = "audio_record_info")
@NoArgsConstructor
@AllArgsConstructor
public class AudioRecordInfo {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "audio_info_id")
  UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false, updatable = false)
  User owner;

  @Column(name = "durability")
  Long durability;

  @Column(name = "record_date")
  LocalDateTime recordedDate;

  @Column(name = "upload_date")
  LocalDateTime uploadedDate;

  @Transient
  UploadStatus uploadStatus;

  public enum UploadStatus {
    Uploading, Failed, Done
  }

}
