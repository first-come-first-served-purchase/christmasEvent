package com.doosan.christmas.member.dto.requestdto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberRequestDto {
    @NotBlank(message = "이메일을 입력해주세요")
    @Size(min=8,max=30, message= "8자리이상 30자리 미만 글자로 email를 만들어주세요")
    @Pattern(regexp = "^[0-9a-zA-Z]+@[a-zA-Z]+\\.[a-zA-Z]+$" , message = "이메일 형식을 확인해 주세요.")
    private String email; // 이메일

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min=1,max=40, message= "닉네임은 최소 1자이상 최대 40자미만으로 만들어주세요.")
    private String nickname; // 닉네임

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min=4,max=32, message= "비밀번호는 최소 4자이상 최대 32자미만으로 만들어주세요.")
    private String password; // 패스워드

    public String passwordConfirm; // 패스워드 확인

    private String address; // 주소


}