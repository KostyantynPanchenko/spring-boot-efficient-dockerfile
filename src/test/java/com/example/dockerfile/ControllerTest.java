package com.example.dockerfile;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest
public class ControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void testControllerShouldReturnDefaultResponse() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/hello"))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().string("Hello stranger!"));
  }

  @Test
  void testControllerShouldReturnHelloAdam() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/hello?name=Adam"))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().string("Hello Adam!"));
  }
}
