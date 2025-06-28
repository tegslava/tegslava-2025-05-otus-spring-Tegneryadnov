package ru.otus.hw.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.exceptions.QuestionReadException;

import static org.assertj.core.api.Assertions.*;
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

}

