package com.books.global.base

import com.books.domain.entity.Book
import com.books.domain.repository.BookRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class DataInitializer(
    private val bookRepository: BookRepository
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        // 예시 데이터 준비
        val sampleBooks = listOf(
            Book("객체지향의 사실과 오해 역할, 책임, 협력 관점에서 본 객체지향", "프로그래밍", "조영호", LocalDate.of(2015, 6, 17), "9787115582247"),
            Book("요즘 우아한 AI 개발 머신러닝에서 GPT, LLM, 생성형 AI, MLOps까지, 배달의민족 실제 프로젝트로 엿보는 AI 활용 이야기", "프로그래밍", "우아한형제들", LocalDate.of(2025, 4, 1), "9787111641247"),
            Book("J랭체인 입문 RAG 챗봇부터 에이전트까지", "프로그래밍", "오승환", LocalDate.of(2025, 3, 1), "9787111213826"),
            Book("알고리즘 (4판)", "컴퓨터 과학", "Robert Sedgewick", LocalDate.of(2012, 10, 1), "9787115293800"),
            Book("교과서에 나오는 고사성어: 익힘책 1~3권 세트", "자기계발", "김광남", LocalDate.of(2025, 4, 29), "9781234567890"),
            Book("모조는 낮잠 잘 곳을 찾아요", "유아", "아델 벨린든", LocalDate.of(2025, 5, 25), "9789876543210"),
            Book("실리콘밸리 길들이기 폭주하는 빅테크 기업에 브레이크를 걸다", "마케팅", "개리 마커스, 김동환", LocalDate.of(2025, 4, 23), "9787111214748"),
            Book("39세 부자 아빠의 레버리지 ETF 투자 노트 불황에도 월급만으로 10배 불리는 고수익 복리 시스템", "경제 경영", "제이투", LocalDate.of(2025, 4, 18), "9787111464747"),
            Book("신세기 에반게리온 애장판 5", "만화", "사다모토 요시유키", LocalDate.of(2025, 3, 28), "9787115419378"),
            Book("BREATHE (격월간) : 2025년 no.72", "서양잡지", "BREATHE", LocalDate.of(2025, 4, 15), "9787123456789")
        )

        // 예시 데이터 저장
        bookRepository.saveAll(sampleBooks)

        println("데이터 초기화 완료, 총 ${sampleBooks.size}권의 도서가 등록되었습니다.")
    }
}