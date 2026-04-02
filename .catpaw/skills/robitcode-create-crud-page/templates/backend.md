# 后端代码模板

## 通用类（生成前检查是否存在）

### OptionVO（通用下拉选项）- 【必须】

**路径**：`service/src/main/java/com/robitcode/vo/OptionVO.java`

⚠️ **重要**：Service 层的 `getOptions()` 方法依赖此类。**生成任何代码前必须先检查此类是否存在，不存在则立即创建**。

```java
package com.robitcode.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptionVO {
    private Long value;
    private String label;
}
```

## Entity

```java
package com.robitcode.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * {中文名称}实体类
 */
@Data
@TableName("sys_{table_name}")
public class {EntityName} implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    // ========== 业务字段 ==========
    
    /** {字段中文名} */
    private String name;
    
    /** 编码 */
    private String code;
    
    /** 排序 */
    private Integer sort;
    
    /** 状态：0-禁用，1-启用 */
    private Integer status;
    
    /** 备注 */
    private String remark;
    
    // ========== 关联字段 ==========
    
    /** {关联实体中文名}ID */
    private Long {refEntity}Id;

    // ========== 标准字段 ==========
    
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    private Long tenantId;
}
```

---

## VO

```java
package com.robitcode.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * {中文名称}视图对象
 */
@Data
public class {EntityName}VO {
    
    private Long id;
    
    // ========== 业务字段 ==========
    private String name;
    private String code;
    private Integer sort;
    private Integer status;
    private String remark;
    
    // ========== 关联显示字段 ==========
    private Long {refEntity}Id;
    private String {refEntity}Name;  // 关联实体显示名称
    
    // ========== 标准字段 ==========
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

---

## DTO

```java
package com.robitcode.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * {中文名称}数据传输对象
 */
@Data
public class {EntityName}DTO {
    
    private Long id;

    @NotBlank(message = "{字段中文名}不能为空")
    private String name;

    private String code;
    
    private Integer sort;
    
    private Integer status;
    
    private String remark;
    
    @NotNull(message = "请选择{关联实体中文名}")
    private Long {refEntity}Id;
}
```

---

## Mapper

```java
package com.robitcode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.robitcode.entity.{EntityName};
import com.robitcode.vo.{EntityName}VO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * {中文名称}Mapper
 */
@Mapper
public interface {EntityName}Mapper extends BaseMapper<{EntityName}> {

    /**
     * 根据编码查询
     */
    {EntityName} selectByCode(@Param("code") String code);

    /**
     * 检查名称是否存在
     */
    boolean existsByName(@Param("name") String name, @Param("excludeId") Long excludeId);

    /**
     * 检查编码是否存在
     */
    boolean existsByCode(@Param("code") String code, @Param("excludeId") Long excludeId);

    /**
     * 批量查询
     */
    List<{EntityName}> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 分页查询（带关联）
     */
    IPage<{EntityName}VO> selectPageWithRelations(
        Page<{EntityName}VO> page,
        @Param("keyword") String keyword,
        @Param("status") Integer status,
        @Param("{refEntity}Id") Long {refEntity}Id
    );
}
```

---

## Mapper XML

文件路径：`service/src/main/resources/mapper/{EntityName}Mapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.robitcode.mapper.{EntityName}Mapper">

    <!-- 分页查询（带关联） -->
    <select id="selectPageWithRelations" resultType="com.robitcode.vo.{EntityName}VO">
        SELECT 
            e.*,
            r.name as {ref_entity}_name
        FROM sys_{table_name} e
        LEFT JOIN sys_{ref_table} r ON e.{ref_entity}_id = r.id AND r.deleted = 0
        WHERE e.deleted = 0
        <if test="keyword != null and keyword != ''">
            AND (e.name LIKE CONCAT('%', #{keyword}, '%'))
        </if>
        <if test="status != null">
            AND e.status = #{status}
        </if>
        <if test="{refEntity}Id != null">
            AND e.{ref_entity}_id = #{{refEntity}Id}
        </if>
        ORDER BY e.sort ASC, e.create_time DESC
    </select>

    <select id="selectByCode" resultType="com.robitcode.entity.{EntityName}">
        SELECT * FROM sys_{table_name} 
        WHERE code = #{code} AND deleted = 0 LIMIT 1
    </select>

    <select id="existsByName" resultType="boolean">
        SELECT COUNT(*) > 0 FROM sys_{table_name} 
        WHERE name = #{name} AND deleted = 0
        <if test="excludeId != null">AND id != #{excludeId}</if>
    </select>

    <select id="existsByCode" resultType="boolean">
        SELECT COUNT(*) > 0 FROM sys_{table_name} 
        WHERE code = #{code} AND deleted = 0
        <if test="excludeId != null">AND id != #{excludeId}</if>
    </select>

    <select id="selectByIds" resultType="com.robitcode.entity.{EntityName}">
        SELECT * FROM sys_{table_name} 
        WHERE id IN <foreach collection="ids" item="id" open="(" separator="," close=")">#{id}</foreach>
        AND deleted = 0 ORDER BY sort ASC, create_time DESC
    </select>

</mapper>
```

---

## Service

```java
package com.robitcode.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.robitcode.dto.{EntityName}DTO;
import com.robitcode.entity.{EntityName};
import com.robitcode.exception.BusinessException;
import com.robitcode.mapper.{EntityName}Mapper;
import com.robitcode.vo.{EntityName}VO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * {中文名称}服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class {EntityName}Service extends ServiceImpl<{EntityName}Mapper, {EntityName}> {

    private final {EntityName}Mapper {entityName}Mapper;
    // 如需校验关联实体，注入对应 Mapper
    // private final {RefEntity}Mapper {refEntity}Mapper;

    /**
     * 分页查询
     */
    public IPage<{EntityName}VO> getPage(Page<{EntityName}VO> page, String keyword, Integer status, Long {refEntity}Id) {
        return {entityName}Mapper.selectPageWithRelations(page, keyword, status, {refEntity}Id);
    }

    /**
     * 创建
     */
    @Transactional
    public {EntityName}VO create({EntityName}DTO dto) {
        // 校验名称唯一
        if ({entityName}Mapper.existsByName(dto.getName(), null)) {
            throw new BusinessException("{中文名称}已存在");
        }
        
        // 校验编码唯一
        if (dto.getCode() != null && {entityName}Mapper.existsByCode(dto.getCode(), null)) {
            throw new BusinessException("编码已存在");
        }

        {EntityName} entity = new {EntityName}();
        BeanUtils.copyProperties(dto, entity);
        {entityName}Mapper.insert(entity);
        
        log.info("{中文名称}创建成功: {}", entity.getName());
        return getDetail(entity.getId());
    }

    /**
     * 更新
     */
    @Transactional
    public {EntityName}VO update({EntityName}DTO dto) {
        {EntityName} existing = {entityName}Mapper.selectById(dto.getId());
        if (existing == null) {
            throw new BusinessException("{中文名称}不存在");
        }

        // 校验名称唯一
        if (!existing.getName().equals(dto.getName()) &&
            {entityName}Mapper.existsByName(dto.getName(), dto.getId())) {
            throw new BusinessException("{中文名称}已存在");
        }

        // 校验编码唯一
        if (dto.getCode() != null && !dto.getCode().equals(existing.getCode()) &&
            {entityName}Mapper.existsByCode(dto.getCode(), dto.getId())) {
            throw new BusinessException("编码已存在");
        }

        {EntityName} entity = new {EntityName}();
        BeanUtils.copyProperties(dto, entity);
        {entityName}Mapper.updateById(entity);
        
        log.info("{中文名称}更新成功: {}", entity.getName());
        return getDetail(entity.getId());
    }

    /**
     * 删除
     */
    @Transactional
    public void delete(Long id) {
        {EntityName} entity = {entityName}Mapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("{中文名称}不存在");
        }
        {entityName}Mapper.deleteById(id);
        log.info("{中文名称}删除成功: {}", entity.getName());
    }

    /**
     * 批量删除
     */
    @Transactional
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        ids.forEach(this::delete);
    }

    /**
     * 获取详情
     */
    public {EntityName}VO getDetail(Long id) {
        Page<{EntityName}VO> page = new Page<>(1, 1);
        LambdaQueryWrapper<{EntityName}> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq({EntityName}::getId, id).eq({EntityName}::getDeleted, 0);
        IPage<{EntityName}VO> result = {entityName}Mapper.selectPageWithRelations(page, null, null, null);
        if (result.getRecords().isEmpty()) {
            throw new BusinessException("{中文名称}不存在");
        }
        return result.getRecords().get(0);
    }

    /**
     * 获取下拉选项
     */
    public List<OptionVO> getOptions() {
        LambdaQueryWrapper<{EntityName}> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq({EntityName}::getDeleted, 0)
               .eq({EntityName}::getStatus, 1)
               .orderByAsc({EntityName}::getSort);
        List<{EntityName}> list = {entityName}Mapper.selectList(wrapper);
        return list.stream()
            .map(e -> new OptionVO(e.getId(), e.getName()))
            .collect(Collectors.toList());
    }
}
```

---

## Controller

```java
package com.robitcode.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.robitcode.common.Result;
import com.robitcode.dto.{EntityName}DTO;
import com.robitcode.service.{EntityName}Service;
import com.robitcode.vo.OptionVO;
import com.robitcode.vo.{EntityName}VO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {中文名称}管理控制器
 */
@Tag(name = "{中文名称}管理")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/{path}")
@RequiredArgsConstructor
public class {EntityName}Controller {

    private final {EntityName}Service {entityName}Service;

    @Operation(summary = "分页查询")
    @GetMapping("/page")
    public Result<IPage<{EntityName}VO>> getPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long {refEntity}Id) {
        Page<{EntityName}VO> pageParam = new Page<>(page, size);
        return Result.success({entityName}Service.getPage(pageParam, keyword, status, {refEntity}Id));
    }

    @Operation(summary = "获取详情")
    @GetMapping("/{id}")
    public Result<{EntityName}VO> getById(@PathVariable Long id) {
        return Result.success({entityName}Service.getDetail(id));
    }

    @Operation(summary = "创建")
    @PostMapping
    public Result<{EntityName}VO> create(@Valid @RequestBody {EntityName}DTO dto) {
        return Result.success("创建成功", {entityName}Service.create(dto));
    }

    @Operation(summary = "更新")
    @PutMapping("/{id}")
    public Result<{EntityName}VO> update(@PathVariable Long id, @Valid @RequestBody {EntityName}DTO dto) {
        dto.setId(id);
        return Result.success("更新成功", {entityName}Service.update(dto));
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        {entityName}Service.delete(id);
        return Result.success();
    }

    @Operation(summary = "批量删除")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        {entityName}Service.batchDelete(ids);
        return Result.success();
    }

    @Operation(summary = "获取下拉选项")
    @GetMapping("/options")
    public Result<List<OptionVO>> getOptions() {
        return Result.success({entityName}Service.getOptions());
    }
}
```
