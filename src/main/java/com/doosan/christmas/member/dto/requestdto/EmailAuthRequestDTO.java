package com.doosan.christmas.member.dto.requestdto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailAuthRequestDTO {
    private String email;
    
    @JsonProperty("authCode")
    private String authCode;
}

