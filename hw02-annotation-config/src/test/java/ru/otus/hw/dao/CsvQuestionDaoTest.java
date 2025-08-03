package ru.otus.hw.dao;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.exceptions.QuestionReadException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class CsvQuestionDaoTest {
    @Mock
    TestFileNameProvider testFileNameProvider;

    @InjectMocks
    CsvQuestionDao csvQuestionDao;

    @DisplayName("Should throw expected exception when resource not exists")
    @Test
    void shouldThrowExpectedExceptionWhenResourceNotExists() {
        given(testFileNameProvider.getTestFileName()).willReturn("non-existing file");
        assertThatExceptionOfType(QuestionReadException.class)
                .isThrownBy(() -> csvQuestionDao.findAll());
    }

    @DisplayName("Should not throw exceptions for existing resource")
    @Test
    void shouldNotThrowExceptionsForExistingResource() {
        given(testFileNameProvider.getTestFileName()).willReturn("questions.csv");
        assertThatNoException().isThrownBy(() ->csvQuestionDao.findAll());
    }

    @DisplayName("Should read all questions from resources")
    @Test
    void findAllSuccess() {
        given(testFileNameProvider.getTestFileName()).willReturn("questions.csv");
        var questions = csvQuestionDao.findAll();
        assertEquals(4, questions.size());
        assertEquals("Is there life on Mars?", questions.get(0).text());
        assertEquals("How should resources be loaded form jar in Java?", questions.get(1).text());
        assertEquals("Which option is a good way to handle the exception?", questions.get(2).text());
        assertEquals("Can I use opencsv in my commercial applications?", questions.get(3).text());
    }
}

