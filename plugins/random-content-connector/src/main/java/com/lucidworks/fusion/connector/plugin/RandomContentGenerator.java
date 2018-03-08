package com.lucidworks.fusion.connector.plugin;

public interface RandomContentGenerator {

  String makeHeadline();

  String makeSentence(boolean isHeadline);

  String makeText(int numSentences);
}
