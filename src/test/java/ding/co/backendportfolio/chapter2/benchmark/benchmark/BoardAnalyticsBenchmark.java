package ding.co.backendportfolio.chapter2.benchmark.benchmark;

import ding.co.backendportfolio.chapter2.entity.*;
import ding.co.backendportfolio.chapter2.repository.BoardRepository;
import ding.co.backendportfolio.chapter2.repository.BoardTagRepository;
import ding.co.backendportfolio.chapter2.service.BoardAnalyticsService;
import ding.co.backendportfolio.chapter2.service.OptimizedBoardAnalyticsService;
import org.openjdk.jmh.annotations.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/*
플러그인 -> Jmh 설치해야 테스트를 수행 할 수 있음.
*/

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
//두번의 독립적인 JVM 프로세스에서 테스트를 수행한다는것을 의미
// Xms2G: 힙메모리는 최소 2기가, Xmx2G: 힙메모리는 최대 2기가
@Fork(value = 2, jvmArgs = {"-Xms2G", "-Xmx2G"})
//iteration을 두번 돌림으로써 웜업과정을 대비하겠다라는 의미
@Warmup(iterations = 2)
//iterations를 몇번이나 수행해서 평균 시간을 측정할건지
@Measurement(iterations = 5)
public class BoardAnalyticsBenchmark {
    private BoardRepository boardRepository;
    private BoardTagRepository boardTagRepository;
    private Board testBoard;
    private List<Board> testBoards;

    private BoardAnalyticsService originalBoardAnalyticsService;
    private OptimizedBoardAnalyticsService optimizedBoardAnalyticsService;

    private void setId(Object entity, Long id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID using reflection", e);
        }
    }

    @Setup
    public void setup() {
        boardRepository = mock(BoardRepository.class);
        boardTagRepository = mock(BoardTagRepository.class);

        // 테스트용 멤버 생성
        Member testMember = Member.builder()
                .email("test@test.com")
                .password("testpass")
                .nickname("tester")
                .build();
        setId(testMember, 1L);

        // 테스트용 태그들 생성
        List<Tag> tags = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Tag tag = new Tag("Tag" + i);
            setId(tag, (long) (i + 1));
            tags.add(tag);
        }

        // 메인 테스트 보드 생성
        testBoard = Board.builder()
                .title("Test Title")
                .content("Test content for benchmark with sufficient length for analysis")
                .category(Category.FREE)
                .member(testMember)
                .build();
        setId(testBoard, 1L);

        // 보드 태그 설정
        Set<BoardTag> boardTags = new HashSet<>();
        for (Tag tag : tags) {
            BoardTag boardTag = new BoardTag(testBoard, tag);
            setId(boardTag, tag.getId());
            boardTags.add(boardTag);
        }
        testBoard.setBoardTags(boardTags);

        // 테스트용 보드 리스트 생성
        testBoards = createTestBoards(100, testMember, tags);

        // Mock 설정
        when(boardRepository.findById(1L)).thenReturn(Optional.of(testBoard));
        when(boardRepository.findAll()).thenReturn(testBoards);
        when(boardTagRepository.findTagsByBoardId(1L)).thenReturn(tags);

        // 원본 서비스와 최적화된 서비스 초기화
        originalBoardAnalyticsService = new BoardAnalyticsService(boardRepository, boardTagRepository);
        optimizedBoardAnalyticsService = new OptimizedBoardAnalyticsService(boardRepository, boardTagRepository);
    }

    private List<Board> createTestBoards(int count, Member member, List<Tag> tags) {
        List<Board> boards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Board board = Board.builder()
                    .title("Test Title " + i)
                    .content("Test content " + i + " with some additional text for analysis")
                    .category(Category.FREE)
                    .member(member)
                    .build();
            setId(board, (long) (i + 2)); // ID 1은 이미 testBoard가 사용

            // 각 보드에 태그 추가
            Set<BoardTag> boardTags = new HashSet<>();
            for (Tag tag : tags.subList(0, Math.min(i % 3 + 1, tags.size()))) {
                BoardTag boardTag = new BoardTag(board, tag);
                setId(boardTag, (long) (boards.size() * tags.size() + tag.getId()));
                boardTags.add(boardTag);
            }
            board.setBoardTags(boardTags);

            boards.add(board);
        }
        return boards;
    }

    /*
    //음...해당 수행은 PC 사양마다 다를수 있겟구나.
    Benchmark                                                 Mode  Cnt   Score   Error  Units
    BoardAnalyticsBenchmark.benchmarkOptimizedImplementation  avgt   10   0.626 ± 0.441  ms/op
    BoardAnalyticsBenchmark.benchmarkOriginalImplementation   avgt   10  11.118 ± 0.294  ms/op
    */
    @Benchmark
    public Map<String, Object> benchmarkOriginalImplementation() {
        /*
        Result "ding.co.backendportfolio.chapter2.benchmark.benchmark.BoardAnalyticsBenchmark.benchmarkOriginalImplementation":
                11.118 ±(99.9%) 0.294 ms/op [Average]
                (min, avg, max) = (10.916, 11.118, 11.340), stdev = 0.194
                CI (99.9%): [10.824, 11.412] (assumes normal distribution)
        */
        return originalBoardAnalyticsService.generateBoardStatistics(1L);
    }

    @Benchmark
    public Map<String, Object> benchmarkOptimizedImplementation() {
        /*
        Result "ding.co.backendportfolio.chapter2.benchmark.benchmark.BoardAnalyticsBenchmark.benchmarkOptimizedImplementation":
                0.626 ±(99.9%) 0.441 ms/op [Average]
                (min, avg, max) = (0.213, 0.626, 0.962), stdev = 0.292
                CI (99.9%): [0.185, 1.067] (assumes normal distribution)
        */
        return optimizedBoardAnalyticsService.generateBoardStatistics(1L);
    }
} 