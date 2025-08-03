package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    protected static final String PROMPT = "Please answer the questions below%n";

    protected static final String RETRY_MESSAGE = "The response is out of range. Please try again";

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printFormattedLine(PROMPT);
        var questions = questionDao.findAll();
        var testResult = new TestResult(student);
        for (var question : questions) {
            ackQuestion(question);
            var answer = getAnswer(question);
            testResult.applyAnswer(question, answer.isCorrect());
        }
        return testResult;
    }

    private Answer getAnswer(Question question) {
        var minAnswerNumber = 1;
        var maxAnswerNumber = question.answers().size();
        var selectedNumber = ioService.readIntForRange(minAnswerNumber, maxAnswerNumber, RETRY_MESSAGE);
        return question.answers().get(selectedNumber - 1);
    }

    private void ackQuestion(Question question) {
        ioService.printLine(question.text());
        int itemNum = 1;
        for (var answer : question.answers()) {
            ioService.printLine(" " + itemNum++ + ") " + answer.text());
        }
    }
}
