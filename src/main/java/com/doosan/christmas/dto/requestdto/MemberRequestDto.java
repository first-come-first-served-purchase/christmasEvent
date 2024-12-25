package com.doosan.christmas.dto.requestdto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberRequestDto {
    @NotBlank(message = "이름을 입력해주세요")
    private String name;
    
    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    private String email;
    
    private String authNum;
    private String address;
    
    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;
    
    private String passwordConfirm;
}