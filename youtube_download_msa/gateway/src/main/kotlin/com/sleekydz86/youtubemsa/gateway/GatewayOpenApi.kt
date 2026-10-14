package com.sleekydz86.youtubemsa.gateway

import com.sleekydz86.youtubemsa.shared.DownloadTicket
import com.sleekydz86.youtubemsa.shared.ListedDownload
import com.sleekydz86.youtubemsa.shared.ListedPage
import com.sleekydz86.youtubemsa.shared.PasteBody
import com.sleekydz86.youtubemsa.shared.SimilarDownload
import com.sleekydz86.youtubemsa.shared.SimilarPage
import com.sleekydz86.youtubemsa.shared.SimilarQuery
import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.tags.Tag

fun gatewayOpenApi(): OpenAPI = OpenAPI()
    .info(gatewayInfo())
    .tags(gatewayTags())
    .components(gatewayComponents())
    .paths(gatewayPaths())

private fun gatewayInfo(): Info = Info()
    .title("youtube_download_msa API")
    .version("v1")
    .description("화면이 호출하는 /api/downloads 문서입니다. 게이트웨이가 다운로드 원장과 검색 서비스로 나눕니다.")

private fun gatewayTags(): List<Tag> = listOf(
    tag("01-다운로드 요청", "유튜브 주소를 다운로드 원장에 남깁니다."),
    tag("02-목록", "저장 상태를 페이지로 읽습니다."),
    tag("03-벡터 검색", "해시 임베딩과 코사인으로 비슷한 저장본을 찾습니다."),
    tag("04-저장본 검색", "제목, 파일명, 영상 ID로 저장본을 찾습니다."),
    tag("05-추천", "가까운 저장본 5개를 반환합니다."),
    tag("06-반응", "좋아요, 싫어요, 조회 수를 올립니다."),
)

private fun tag(name: String, description: String): Tag = Tag().name(name).description(description)

private fun gatewayComponents(): Components {
    val components = Components()
    schemaTypes().forEach { components.addSchemasOf(it) }
    return components
}

private fun schemaTypes(): List<Class<*>> = listOf(
    PasteBody::class.java,
    DownloadTicket::class.java,
    ListedPage::class.java,
    ListedDownload::class.java,
    SimilarQuery::class.java,
    SimilarPage::class.java,
    SimilarDownload::class.java,
    ApiError::class.java,
)

private fun Components.addSchemasOf(type: Class<*>): Components {
    ModelConverters.getInstance().readAll(AnnotatedType(type)).forEach { (name, schema) -> addSchemas(name, schema) }
    return this
}

private fun gatewayPaths(): Paths = Paths()
    .addPathItem("/api/downloads", PathItem().post(pasteOperation()).get(listOperation()))
    .addPathItem("/api/downloads/saved", PathItem().get(savedOperation()))
    .addPathItem("/api/downloads/search", PathItem().post(similarOperation()))
    .addPathItem("/api/downloads/recommendations", PathItem().get(recommendOperation()))
    .addPathItem("/api/downloads/{id}/recommendations", PathItem().get(recommendOneOperation()))
    .addPathItem("/api/downloads/{id}/likes", PathItem().post(reaction("likeDownload", "좋아요", "좋아요 수를 1 올립니다.")))
    .addPathItem("/api/downloads/{id}/dislikes", PathItem().post(reaction("dislikeDownload", "싫어요", "싫어요 수를 1 올립니다.")))
    .addPathItem("/api/downloads/{id}/views", PathItem().post(reaction("viewDownload", "조회", "조회 수를 1 올립니다.")))

private fun pasteOperation(): Operation = operation(
    tag = "01-다운로드 요청",
    operationId = "pasteDownload",
    summary = "영상 주소 등록",
    description = "유튜브 공유 주소를 받아 다운로드를 접수합니다. watch, shorts, youtu.be 주소를 받습니다.",
    response = "DownloadTicket",
    responseDescription = "접수된 다운로드",
).requestBody(jsonBody("PasteBody"))

private fun listOperation(): Operation = operation(
    tag = "02-목록",
    operationId = "listDownloads",
    summary = "다운로드 목록",
    description = "page는 0 이상 200 이하, size는 1 이상 50 이하입니다. 생략하면 0과 10입니다.",
    response = "ListedPage",
    responseDescription = "다운로드 페이지",
).parameters(pageParameters())

private fun savedOperation(): Operation = operation(
    tag = "04-저장본 검색",
    operationId = "searchSaved",
    summary = "저장본 검색",
    description = "제목, 파일명, 영상 ID에 검색어가 포함된 저장본을 페이지로 찾습니다.",
    response = "ListedPage",
    responseDescription = "검색된 저장본 페이지",
).parameters(listOf(queryString("q", "제목, 파일명, 영상 ID")) + pageParameters())

private fun similarOperation(): Operation = operation(
    tag = "03-벡터 검색",
    operationId = "searchSimilar",
    summary = "비슷한 저장본 검색",
    description = "검색어를 해시 임베딩해 코사인이 가까운 저장본을 찾습니다. k는 1 이상 20 이하이며 생략하면 5입니다.",
    response = "SimilarPage",
    responseDescription = "코사인 점수와 함께 정렬한 저장본",
).requestBody(jsonBody("SimilarQuery"))

private fun recommendOperation(): Operation = operation(
    tag = "05-추천",
    operationId = "recommendDownloads",
    summary = "추천 5개",
    description = "기준 영상 없이 가까운 저장본 5개를 반환합니다.",
    response = "SimilarPage",
    responseDescription = "추천 저장본",
)

private fun recommendOneOperation(): Operation = operation(
    tag = "05-추천",
    operationId = "recommendForDownload",
    summary = "이 영상과 비슷한 추천",
    description = "지정한 저장본과 가까운 저장본 5개를 반환합니다.",
    response = "SimilarPage",
    responseDescription = "기준 영상과 가까운 저장본",
    notFound = true,
).addParametersItem(idParameter())

private fun reaction(operationId: String, summary: String, description: String): Operation = operation(
    tag = "06-반응",
    operationId = operationId,
    summary = summary,
    description = "$description 서명 주소를 붙인 목록 항목을 반환합니다.",
    response = "ListedDownload",
    responseDescription = "반응을 반영한 항목",
    notFound = true,
).addParametersItem(idParameter())

private fun operation(
    tag: String,
    operationId: String,
    summary: String,
    description: String,
    response: String,
    responseDescription: String,
    notFound: Boolean = false,
): Operation = Operation()
    .addTagsItem(tag)
    .operationId(operationId)
    .summary(summary)
    .description(description)
    .responses(responses(response, responseDescription, notFound))

private fun responses(schema: String, description: String, notFound: Boolean): ApiResponses {
    val responses = ApiResponses()
        .addApiResponse("200", jsonResponse(description, schema))
        .addApiResponse("400", jsonResponse("요청 값이 올바르지 않습니다.", "ApiError"))
        .addApiResponse("502", jsonResponse("뒤 서비스에 연결하지 못했습니다.", "ApiError"))
    if (notFound) responses.addApiResponse("404", jsonResponse("저장된 영상을 찾지 못했습니다.", "ApiError"))
    return responses
}

private fun jsonResponse(description: String, schema: String): ApiResponse =
    ApiResponse().description(description).content(jsonContent(schema))

private fun jsonBody(schema: String): RequestBody = RequestBody().required(true).content(jsonContent(schema))

private fun jsonContent(schema: String): Content =
    Content().addMediaType("application/json", MediaType().schema(schemaRef(schema)))

private fun schemaRef(name: String): Schema<Any> {
    val schema = Schema<Any>()
    schema.`$ref` = "#/components/schemas/$name"
    return schema
}

private fun Operation.parameters(values: List<Parameter>): Operation {
    values.forEach { addParametersItem(it) }
    return this
}

private fun pageParameters(): List<Parameter> = listOf(
    queryInt("page", "0 이상 200 이하. 생략하면 0"),
    queryInt("size", "1 이상 50 이하. 생략하면 10"),
)

private fun queryInt(name: String, description: String): Parameter = query(name, description, IntegerSchema())

private fun queryString(name: String, description: String): Parameter = query(name, description, StringSchema())

private fun query(name: String, description: String, schema: Schema<*>): Parameter = Parameter()
    .name(name)
    .`in`("query")
    .required(false)
    .description(description)
    .schema(schema)

private fun idParameter(): Parameter = Parameter()
    .name("id")
    .`in`("path")
    .required(true)
    .description("다운로드 번호")
    .schema(IntegerSchema().format("int64"))
