package com.azure.global.config

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler
import org.apache.ibatis.reflection.MetaObject
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.time.Instant

@Component
class MyMetaObjectConfig : MetaObjectHandler {
    override fun insertFill(metaObject: MetaObject) {
        val now = Timestamp.from(Instant.now())
        this.strictInsertFill(metaObject, "createDate", Timestamp::class.java, now)
        this.strictInsertFill(metaObject, "modifyDate", Timestamp::class.java, now)
    }

    override fun updateFill(metaObject: MetaObject) {
        val now = Timestamp.from(Instant.now())
        this.strictUpdateFill(metaObject, "modifyDate", Timestamp::class.java, now)
    }
}