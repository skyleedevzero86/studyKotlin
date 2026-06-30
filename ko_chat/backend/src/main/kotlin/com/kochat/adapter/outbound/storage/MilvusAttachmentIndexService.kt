package com.kochat.adapter.outbound.storage

import com.kochat.adapter.outbound.persistence.chat.MessageAttachmentJpaEntity
import com.kochat.adapter.outbound.persistence.chat.MessageAttachmentJpaRepository
import com.kochat.global.config.MilvusProperties
import io.milvus.client.MilvusServiceClient
import io.milvus.grpc.DataType
import io.milvus.param.ConnectParam
import io.milvus.param.IndexType
import io.milvus.param.MetricType
import io.milvus.param.R
import io.milvus.param.collection.CreateCollectionParam
import io.milvus.param.collection.FieldType
import io.milvus.param.collection.HasCollectionParam
import io.milvus.param.dml.InsertParam
import io.milvus.param.index.CreateIndexParam
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.util.Collections

@Service
@ConditionalOnProperty(prefix = "app.milvus", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class MilvusAttachmentIndexService(
    private val properties: MilvusProperties,
    private val attachmentRepository: MessageAttachmentJpaRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var client: MilvusServiceClient? = null
    private var collectionReady = false

    @PostConstruct
    fun initialize() {
        try {
            client = MilvusServiceClient(
                ConnectParam.newBuilder()
                    .withHost(properties.host)
                    .withPort(properties.port)
                    .build(),
            )
            ensureCollection()
            collectionReady = true
            logger.info("Milvus 연결 완료: {}:{}", properties.host, properties.port)
        } catch (ex: Exception) {
            logger.warn("Milvus 초기화 실패 - 첨부파일 벡터 등록이 비활성화됩니다: {}", ex.message)
        }
    }

    fun indexAttachment(attachment: MessageAttachmentJpaEntity) {
        if (!collectionReady || client == null || attachment.milvusIndexed) {
            return
        }

        try {
            val vector = Collections.nCopies(properties.vectorDim, 0.0f)
            val fields = listOf(
                InsertParam.Field("attachment_id", listOf(attachment.id ?: 0L)),
                InsertParam.Field("message_id", listOf(attachment.messageId ?: 0L)),
                InsertParam.Field("chat_room_id", listOf(attachment.chatRoomId)),
                InsertParam.Field("object_key", listOf(attachment.objectKey)),
                InsertParam.Field("file_name", listOf(attachment.fileName)),
                InsertParam.Field("embedding", listOf(vector)),
            )

            val response = client!!.insert(
                InsertParam.newBuilder()
                    .withCollectionName(properties.collection)
                    .withFields(fields)
                    .build(),
            )

            if (response.status == R.Status.Success.code) {
                attachment.milvusIndexed = true
                attachmentRepository.save(attachment)
            }
        } catch (ex: Exception) {
            logger.warn("Milvus 첨부파일 등록 실패(objectKey={}): {}", attachment.objectKey, ex.message)
        }
    }

    private fun ensureCollection() {
        val milvus = client ?: return
        val hasCollection = milvus.hasCollection(
            HasCollectionParam.newBuilder().withCollectionName(properties.collection).build(),
        ).data

        if (hasCollection) {
            return
        }

        val fields = listOf(
            FieldType.newBuilder().withName("attachment_id").withDataType(DataType.Int64).withPrimaryKey(true).withAutoID(false).build(),
            FieldType.newBuilder().withName("message_id").withDataType(DataType.Int64).build(),
            FieldType.newBuilder().withName("chat_room_id").withDataType(DataType.Int64).build(),
            FieldType.newBuilder().withName("object_key").withDataType(DataType.VarChar).withMaxLength(500).build(),
            FieldType.newBuilder().withName("file_name").withDataType(DataType.VarChar).withMaxLength(255).build(),
            FieldType.newBuilder().withName("embedding").withDataType(DataType.FloatVector).withDimension(properties.vectorDim).build(),
        )

        milvus.createCollection(
            CreateCollectionParam.newBuilder()
                .withCollectionName(properties.collection)
                .withFieldTypes(fields)
                .build(),
        )

        milvus.createIndex(
            CreateIndexParam.newBuilder()
                .withCollectionName(properties.collection)
                .withFieldName("embedding")
                .withIndexType(IndexType.FLAT)
                .withMetricType(MetricType.L2)
                .build(),
        )
    }
}
