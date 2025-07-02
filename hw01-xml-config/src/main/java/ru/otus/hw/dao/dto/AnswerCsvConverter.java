package ru.otus.hw.dao.dto;

import com.opencsv.bean.AbstractCsvConverter;
import org.apache.commons.lang3.StringUtils;
import ru.otus.hw.domain.Answer;

public class AnswerCsvConverter extends AbstractCsvConverter {

    @Override
    public Object convertToRead(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        var valueArr = value.split("%");
        return new Answer(valueArr[0], Boolean.parseBoolean(valueArr[1]));
    }
}
