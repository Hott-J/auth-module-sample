package com.example.domain

/**
 * 회사 표준 api 응답 모델 페이징 모델은 [ApiPageResult] 참고
 *
 * api 실패한 경우, [error] 필드에 값을 설정해야 하며 추가 데이터 응답이 필요하면 [data] 필드를 사용할 수 있다.
 */
data class ApiResult<R>(
    val data: R,
    val error: Error? = null
) {
    data class Error(
        val message: String,
        /** 에러 코드 */
        val code: String? = null,
        /** 개발용도 아님 */
        val exception: String? = null,
        /** 개발용도 아님 */
        val stacktrace: List<String>? = null
    )
}

data class ApiPageResult<E>(
    val data: List<E>,
    val totalItems: Long,
    val page: Int,
    val pageSize: Int
)

/**
 * Converts object to [ApiResult]
 * All api endpoints must return either [ApiResult] or [ApiPageResult].
 * It's also strongly recommended to use these classes in the controller class only.
 * If the receiver type is a collection, you can transform elements of the collection using a trailing lambda parameter.
 *
 * Example:
 * ```kotlin
 * T.toResult() // ApiResult<T>
 * T.toResult { R } // ApiListResult<R> or ApiPageResult<R>
 * ```
 */
fun <R> R.toResult() = ApiResult(this)

/**
 * Converts a Map to an [ApiResult] response.
 * If you want to return a Map, simply call [toResult] without the [transformer] parameter.
 *
 * Example:
 * ```kotlin
 * mapOf("0x1" to 1, "0x2" to 2).toResult { "${it.key}:${it.value}" } // ApiResult<List<String>>
 * mapOf("0x1" to 1, "0x2" to 2).toResult() // ApiResult<Map<String, Int>>
 * ```
 */
fun <K, V, R> Map<K, V>.toResult(transformer: (Map.Entry<K, V>) -> R) = ApiResult(map(transformer))
fun <E, R> Collection<E>.toResult(transformer: (E) -> R) = ApiResult(map(transformer))
fun <E, R> Sequence<E>.toResult(transformer: (E) -> R) = ApiResult(map(transformer).toList())

fun <E, R> org.springframework.data.domain.Page<E>.toResult(transformer: (E) -> R) = ApiPageResult(
    data = content.map(transformer),
    totalItems = totalElements,
    page = number,
    pageSize = size
)

fun <E, R> ApiResult<List<E>>.map(transformer: (E) -> R) = ApiResult(data.map(transformer))

fun <E, R> ApiPageResult<E>.map(transformer: (E) -> R) = ApiPageResult(
    data = data.map(transformer),
    totalItems = totalItems,
    page = page,
    pageSize = pageSize
)
