package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Question;

@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public void executeTest() {
        ioService.printLine("...");
        ioService.printFormattedLine("Please answer the questions below%n");
        questionDao.findAll().forEach(
                this::typeQuestionWithAnswers
        );
    }

    private void typeQuestionWithAnswers(Question question) {
        ioService.printLine(question.text());
        var itemNum = 1;
        for (var answer : question.answers()) {
            ioService.printLine(" " + itemNum++ + ") " + answer.text());
        }
    }
}
