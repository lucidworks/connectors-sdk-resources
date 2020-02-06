package com.lucidworks.connector.shared.generator;

public interface RandomContentGenerator {

  /**
   * Produces something that reads like a headline.
   *
   * @return the generated headline.
   */
  String makeHeadline();

  /**
   * Produces a sentence.
   *
   * @return the generated sentence.
   */
  String makeSentence();

  /**
   * Produces a random number of sentences in a the text.
   * <p>
   * The number of sentences is randomly generated between min and max arguments
   *
   * @param min minimum number of sentences the text is to contain.
   * @param max maximum number of sentences the text is to contain.
   * @return the generated text.
   */
  String makeRandomText(int min, int max);

  /**
   * Produces news article text.
   *
   * @param numSentences how many sentences the text is to contain.
   * @return the generated text.
   */
  String makeText(int numSentences);
}
