package cn.edu.ccst.sims.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 元对象处理器配置
 * 用于自动填充公共字段，如创建时间和更新时间。
 * 这在插入/更新实体时自动触发，减少手动代码，提高代码整洁性。
 */
@Configuration
public class MetaObjectHandlerConfig implements MetaObjectHandler {
    /**
     * 插入时自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        // 可添加其他字段，如status默认1
        if (metaObject.hasSetter("status")) {
            this.strictInsertFill(metaObject, "status", Integer.class, 1);
        }
    }

    /**
     * 更新时自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}