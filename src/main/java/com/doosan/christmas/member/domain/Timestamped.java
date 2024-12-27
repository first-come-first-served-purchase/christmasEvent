package com.doosan.christmas.member.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass // 공통 필드를 상속받는 엔티티에 추가
@EntityListeners(AuditingEntityListener.class) // 생성/수정 시간 자동 관리
public abstract class Timestamped {

    @JsonSerialize(using = LocalDateTimeSerializer.class) // LocalDateTime 직렬화 설정
    @JsonDeserialize(using = LocalDateTimeDeserializer.class) // LocalDateTime 역직렬화 설정
    @CreatedDate // 생성 시간 자동 저장
    private LocalDateTime createdAt;

    @JsonSerialize(using = LocalDateTimeSerializer.class) // LocalDateTime 직렬화 설정
    @JsonDeserialize(using = LocalDateTimeDeserializer.class) // LocalDateTime 역직렬화 설정
    @LastModifiedDate // 수정 시간 자동 저장
    private LocalDateTime modifiedAt;

}
