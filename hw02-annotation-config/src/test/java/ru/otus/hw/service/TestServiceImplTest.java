package ru.otus.hw.service;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static ru.otus.hw.service.TestServiceImpl.RETRY_MESSAGE;

@ExtendWith(MockitoExtension.class)
class TestServiceImplTest {
    private final Student student = new Student("Firstname", "Lastname");

    @Mock
    private IOService ioService;

    @Mock
    private QuestionDao questionDao;

    @InjectMocks
    private TestServiceImpl testService;

    @DisplayName("Should type expected questions with answers")
    @Test
    void shouldTypeExpectedQuestionsWithAnswers() {
        List<Question> questions = List.of(
                new Question("Is there life on Mars?", List.of(
                        new Answer("Science doesn't know this yet", true),
                        new Answer("Certainly. The red UFO is from Mars. And green is from Venus", false),
                        new Answer("Absolutely not", false)
                )),
                new Question("How should resources be loaded form jar in Java?", List.of(
                        new Answer("ClassLoader#geResourceAsStream or ClassPathResource#getInputStream", true),
                        new Answer("ClassLoader#geResource#getFile + FileReader", false)
                ))
        );
        given(questionDao.findAll()).willReturn(questions);

        answersShouldBe(1, 1);
        testService.executeTestFor(student);

        var inOrder = inOrder(ioService);
        List.of(
                "Is there life on Mars?",
                " 1) Science doesn't know this yet",
                " 2) Certainly. The red UFO is from Mars. And green is from Venus",
                " 3) Absolutely not",
                "How should resources be loaded form jar in Java?",
                " 1) ClassLoader#geResourceAsStream or ClassPathResource#getInputStream",
                " 2) ClassLoader#geResource#getFile + FileReader"
        ).forEach(s -> inOrder.verify(ioService).printLine(eq(s)));
    }

    private void answersShouldBe(int... answers) {
        var stub = given(ioService.readIntForRange(anyInt(), anyInt(), anyString()));
        for (var answer : answers) {
            stub = stub.willReturn(answer);
        }
    }

    @DisplayName("Should record the correct answer")
    @ParameterizedTest
    @CsvSource({"1,0", "3,0", "2,1"})
    void shouldRecordCorrectAnswer(int answerNumber, int rightAnswersCount) {
        var answer1 = new Answer("answer1", false);
        var answer2 = new Answer("answer2", true);
        var answer3 = new Answer("answer3", false);
        var answerList = List.of(answer1, answer2, answer3);
        var question = new Question("question", answerList);
        var questionList = List.of(question);
        given(questionDao.findAll()).willReturn(questionList);
        given(ioService.readIntForRange(1, answerList.size(), RETRY_MESSAGE))
                .willReturn(answerNumber);
        var testResult = testService.executeTestFor(student);
        assertEquals(student, testResult.getStudent());
        assertEquals(rightAnswersCount, testResult.getRightAnswersCount());
        assertEquals(questionList, testResult.getAnsweredQuestions());
    }
}