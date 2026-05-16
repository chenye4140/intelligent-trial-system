#!/bin/bash
BASE="/home/chenye/intelligent-trial-system/auth/src/main/java/com/intelligent/trial/auth"
RES="/home/chenye/intelligent-trial-system/auth/src/main/resources"
mkdir -p $BASE/{entity,mapper,service/impl,controller,dto,vo,annotation,config,interceptor,aop,util,context}
mkdir -p $RES/mapper

########################################
# ENTITY FILES
########################################
cat > $BASE/entity/SysUser.java << 'EOF'
package com.intelligent.trial.auth.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@TableName("sys_user")
public class SysUser implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    private Long id;
    private String username;
    private String password;
    private String realName;
    private Long deptId;
    private String phone;
    private String email;
    private Integer status;
    private Date lastLoginTime;
    private Date createTime;
    private Date updateTime;
    @TableField(exist = false)
    private String deptName;
    @TableField(exist = false)
    private List<Long> roleIds;
}
EOF

cat > $BASE/entity/SysRole.java << 'EOF'
package com.intelligent.trial.auth.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@TableName("sys_role")
public class SysRole implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    private Long id;
    private String roleName;
    private String roleCode;
    private String description;
    private Integer status;
    private Date createTime;
    private Date updateTime;
    @TableField(exist = false)
    private List<Long> menuIds;
}
EOF

cat > $BASE/entity/SysMenu.java << 'EOF'
package com.intelligent.trial.auth.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@TableName("sys_menu")
public class SysMenu implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    private Long id;
    private Long parentId;
    private String name;
    private String path;
    private String component;
    private String perms;
    private Integer type;
    private String icon;
    private Integer sort;
    private Integer visible;
    private Integer status;
    private Date createTime;
    private Date updateTime;
    @TableField(exist = false)
    private List<SysMenu> children;
}
EOF

cat > $BASE/entity/SysDept.java << 'EOF'
package com.intelligent.trial.auth.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@TableName("sys_dept")
public class SysDept implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    private Long id;
    private Long parentId;
    private String deptName;
    private String leader;
    private String phone;
    private Integer sort;
    private Integer status;
    private Date createTime;
    private Date updateTime;
    @TableField(exist = false)
    private List<SysDept> children;
}
EOF

cat > $BASE/entity/SysAuditLog.java << 'EOF'
package com.intelligent.trial.auth.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_audit_log")
public class SysAuditLog implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    private Long id;
    private Long userId;
    private String module;
    private String action;
    private String description;
    private String ip;
    private String userAgent;
    private String requestUrl;
    private String requestMethod;
    private String params;
    private Integer result;
    private Integer duration;
    private Date createTime;
    private Date updateTime;
}
EOF

cat > $BASE/entity/SysClassificationLevel.java << 'EOF'
package com.intelligent.trial.auth.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_classification_level")
public class SysClassificationLevel implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    private Long id;
    private String levelCode;
    private String levelName;
    private Integer sort;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
EOF

########################################
# DTO FILES
########################################
cat > $BASE/dto/LoginDTO.java << 'EOF'
package com.intelligent.trial.auth.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class LoginDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
}
EOF

cat > $BASE/dto/UserDTO.java << 'EOF'
package com.intelligent.trial.auth.dto;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

@Data
public class UserDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    @NotBlank(message = "用户名不能为空")
    private String username;
    private String password;
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    private Long deptId;
    private String phone;
    @Email(message = "邮箱格式不正确")
    private String email;
    private Integer status;
    private List<Long> roleIds;
}
EOF

cat > $BASE/dto/RoleDTO.java << 'EOF'
package com.intelligent.trial.auth.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

@Data
public class RoleDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    @NotBlank(message = "角色名称不能为空")
    private String roleName;
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;
    private String description;
    private Integer status;
    private List<Long> menuIds;
}
EOF

cat > $BASE/dto/MenuDTO.java << 'EOF'
package com.intelligent.trial.auth.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class MenuDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long parentId;
    @NotBlank(message = "菜单名称不能为空")
    private String name;
    private String path;
    private String component;
    private String perms;
    private Integer type;
    private String icon;
    private Integer sort;
    private Integer visible;
    private Integer status;
}
EOF

cat > $BASE/dto/DeptDTO.java << 'EOF'
package com.intelligent.trial.auth.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class DeptDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long parentId;
    @NotBlank(message = "部门名称不能为空")
    private String deptName;
    private String leader;
    private String phone;
    private Integer sort;
    private Integer status;
}
EOF

cat > $BASE/dto/ClassificationLevelDTO.java << 'EOF'
package com.intelligent.trial.auth.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class ClassificationLevelDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    @NotBlank(message = "密级编码不能为空")
    private String levelCode;
    @NotBlank(message = "密级名称不能为空")
    private String levelName;
    private Integer sort;
    private Integer status;
}
EOF

cat > $BASE/dto/ResetPasswordDTO.java << 'EOF'
package com.intelligent.trial.auth.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class ResetPasswordDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
EOF

########################################
# VO FILES
########################################
cat > $BASE/vo/LoginVO.java << 'EOF'
package com.intelligent.trial.auth.vo;

import lombok.Data;
import java.io.Serializable;

@Data
public class LoginVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private UserInfo userInfo;

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String realName;
        private String deptName;
        private String phone;
        private String email;
    }
}
EOF

cat > $BASE/vo/UserVO.java << 'EOF'
package com.intelligent.trial.auth.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class UserVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String username;
    private String realName;
    private Long deptId;
    private String deptName;
    private String phone;
    private String email;
    private Integer status;
    private Date lastLoginTime;
    private Date createTime;
    private List<Long> roleIds;
    private List<String> roles;
}
EOF

cat > $BASE/vo/RoleVO.java << 'EOF'
package com.intelligent.trial.auth.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class RoleVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String roleName;
    private String roleCode;
    private String description;
    private Integer status;
    private Date createTime;
    private List<Long> menuIds;
}
EOF

cat > $BASE/vo/MenuTreeVO.java << 'EOF'
package com.intelligent.trial.auth.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class MenuTreeVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long parentId;
    private String name;
    private String path;
    private String component;
    private String perms;
    private Integer type;
    private String icon;
    private Integer sort;
    private Integer visible;
    private Integer status;
    private Date createTime;
    private List<MenuTreeVO> children;
}
EOF

cat > $BASE/vo/DeptTreeVO.java << 'EOF'
package com.intelligent.trial.auth.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class DeptTreeVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long parentId;
    private String deptName;
    private String leader;
    private String phone;
    private Integer sort;
    private Integer status;
    private Date createTime;
    private List<DeptTreeVO> children;
}
EOF

cat > $BASE/vo/ClassificationLevelVO.java << 'EOF'
package com.intelligent.trial.auth.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class ClassificationLevelVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String levelCode;
    private String levelName;
    private Integer sort;
    private Integer status;
    private Date createTime;
}
EOF

cat > $BASE/vo/AuditLogVO.java << 'EOF'
package com.intelligent.trial.auth.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class AuditLogVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private String username;
    private String module;
    private String action;
    private String description;
    private String ip;
    private String userAgent;
    private String requestUrl;
    private String requestMethod;
    private String params;
    private Integer result;
    private Integer duration;
    private Date createTime;
}
EOF

cat > $BASE/vo/ClassificationAccessVO.java << 'EOF'
package com.intelligent.trial.auth.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class ClassificationAccessVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long userId;
    private Integer maxLevelSort;
    private List<ClassificationLevelInfo> accessibleLevels;

    @Data
    public static class ClassificationLevelInfo {
        private Long id;
        private String levelCode;
        private String levelName;
        private Integer sort;
    }
}
EOF

echo "Phase 2 (entities, dtos, vos) done"
