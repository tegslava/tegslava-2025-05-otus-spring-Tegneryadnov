package ru.otus.hw.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestServiceImplTest {

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
        testService.executeTest();
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
}