package cn.coderstory.springboot.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String name;
    private Integer gender;
    private String avatar;
    private String phone;
    private Long roleId;
    private String roleName;
    private String email;
    private String department;
    private String position;
    private Integer enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}