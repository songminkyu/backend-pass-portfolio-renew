package ding.co.backendportfolio.chapter6._1_practice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookService {

    private final BookRepository bookRepository;

    /**
     @Cacheable은 다음과 같은 순서로 동작함.
     1. 해당 메서드가 호출될 때 캐시 저장소(value)에 동일한 키가 존재 하는지 확인.
     2. 캐시에 데이터가 있으면 해당값을 반환하고 메서드는 실행 되지 않음.
     3. 캐시에 없으면 메서드를 실행 하고, 결과를 캐시에 저장한 후 반환.
    **/

    /**
     * - id 를 통해서 Book 을 로컬 캐싱합니다.
     */
    @Cacheable(value = "book")
    public Book findBookById(Long id) {
        log.info("findBookById 가 실행됩니다.");
        return bookRepository.findById(id).orElseThrow();
    }

    /**
     * - 같은 id 값을 통해서 조회하더라도, 반환 결과가 다르다면 다른 이름을 적용해야합니다.
     */
    @Cacheable(value = "bookName")
    public Book findBookNameById(Long id) {
        log.info("findBookNameById 가 실행됩니다.");
        return bookRepository.findById(id).orElse(null);
    }

    /**
     * - id 만을 캐시 키로 설정했기 때문에 name 이 달라도 같은 결과가 나온다.
     */
    @Cacheable(value = "bookIdAndName", key = "#id")
    public Book findBookByIdAndName(Long id, String name) {
        log.info("findBookByIdAndName 가 실행됩니다.");
        return bookRepository.findByIdAndName(id, name).orElseThrow();
    }
}
