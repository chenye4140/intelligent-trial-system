package com.intelligent.trial.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 基础实体类 BaseEntity 单元测试
 */
class BaseEntityTest {

    /**
     * 测试用具体实体类
     */
    static class TestEntity extends BaseEntity {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @Test
    void testInheritance() {
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        entity.setCreateBy("admin");
        entity.setUpdateBy("admin");
        entity.setName("test");

        assertEquals(1L, entity.getId());
        assertNotNull(entity.getCreateTime());
        assertNotNull(entity.getUpdateTime());
        assertEquals("admin", entity.getCreateBy());
        assertEquals("admin", entity.getUpdateBy());
        assertEquals("test", entity.getName());
    }

    @Test
    void testIdType_isAssignId() throws NoSuchFieldException {
        Field idField = BaseEntity.class.getDeclaredField("id");
        TableId tableId = idField.getAnnotation(TableId.class);
        assertNotNull(tableId);
        assertEquals(IdType.ASSIGN_ID, tableId.type());
    }

    @Test
    void testCreateTime_fillInsert() throws NoSuchFieldException {
        Field field = BaseEntity.class.getDeclaredField("createTime");
        TableField tableField = field.getAnnotation(TableField.class);
        assertNotNull(tableField);
        assertEquals(FieldFill.INSERT, tableField.fill());
    }

    @Test
    void testUpdateTime_fillInsertUpdate() throws NoSuchFieldException {
        Field field = BaseEntity.class.getDeclaredField("updateTime");
        TableField tableField = field.getAnnotation(TableField.class);
        assertNotNull(tableField);
        assertEquals(FieldFill.INSERT_UPDATE, tableField.fill());
    }

    @Test
    void testCreateBy_fillInsert() throws NoSuchFieldException {
        Field field = BaseEntity.class.getDeclaredField("createBy");
        TableField tableField = field.getAnnotation(TableField.class);
        assertNotNull(tableField);
        assertEquals(FieldFill.INSERT, tableField.fill());
    }

    @Test
    void testUpdateBy_fillInsertUpdate() throws NoSuchFieldException {
        Field field = BaseEntity.class.getDeclaredField("updateBy");
        TableField tableField = field.getAnnotation(TableField.class);
        assertNotNull(tableField);
        assertEquals(FieldFill.INSERT_UPDATE, tableField.fill());
    }

    @Test
    void testSerializable() {
        TestEntity entity = new TestEntity();
        assertTrue(entity instanceof java.io.Serializable);
    }

    @Test
    void testAbstractClass() {
        // BaseEntity is abstract - verify it cannot be instantiated directly
        // This is a compile-time check, so we just verify the class is abstract
        assertTrue(java.lang.reflect.Modifier.isAbstract(BaseEntity.class.getModifiers()));
    }

    @Test
    void testAllFieldsPresent() throws NoSuchFieldException {
        // Verify all expected fields exist
        assertNotNull(BaseEntity.class.getDeclaredField("id"));
        assertNotNull(BaseEntity.class.getDeclaredField("createTime"));
        assertNotNull(BaseEntity.class.getDeclaredField("updateTime"));
        assertNotNull(BaseEntity.class.getDeclaredField("createBy"));
        assertNotNull(BaseEntity.class.getDeclaredField("updateBy"));
    }
}
