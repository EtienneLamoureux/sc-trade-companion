package tools.sctrade.companion.utils;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IncrementingIntTest {
  private IncrementingInt incrementingInt;

  @BeforeEach
  void setUp() {
    incrementingInt = new IncrementingInt();
  }

  @Test
  void whenGettingThenGetValue() {
    assertEquals(0, incrementingInt.get());
  }

  @Test
  void whenGettingAndIncrementingThenGetValue() {
    assertEquals(0, incrementingInt.getAndIncrement());
  }

  @Test
  void givenPreviousIncrementWhenGettingThenGetValue() {
    incrementingInt.getAndIncrement();

    assertEquals(1, incrementingInt.get());
  }
}
