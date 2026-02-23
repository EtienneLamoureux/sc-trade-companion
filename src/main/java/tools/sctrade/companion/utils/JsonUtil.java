package tools.sctrade.companion.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.ocr.WindowsOcr;
import tools.sctrade.companion.exceptions.JsonConversionException;
import tools.sctrade.companion.exceptions.JsonParsingException;

public class JsonUtil {
  private static final Logger logger = LoggerFactory.getLogger(WindowsOcr.class);
  private static final ObjectMapper objectMapper;

  static {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
  }

  public static <T> T parse(String json, Class<T> clazz) {
    try {
      return objectMapper.readValue(json, clazz);
    } catch (JsonProcessingException e) {
      logger.error("Could not parse json: " + json, e);
      throw new JsonParsingException(e);
    }
  }

  public static <T> List<T> parseList(String json, Class<T> clazz) {
    try {
      JavaType javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);

      return objectMapper.readValue(json, javaType);
    } catch (JsonProcessingException e) {
      logger.error("Could not parse json: " + json, e);
      throw new JsonParsingException(e);
    }
  }

  public static Object toJson(Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      logger.error("Could not convert to json: " + object, e);
      throw new JsonConversionException(e);
    }
  }
}
