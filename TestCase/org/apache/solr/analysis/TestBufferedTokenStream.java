/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.solr.analysis;

import junit.framework.TestCase;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;

import java.io.IOException;
import java.io.StringReader;

/**
 * Test that BufferedTokenStream behaves as advertized in subclasses.
 */
public class TestBufferedTokenStream extends TestCase {

  /** Example of a class implementing the rule "A" "B" => "Q" "B" */
  public static class AB_Q_Stream extends BufferedTokenStream {
    public AB_Q_Stream(TokenStream input) {super(input);}
    protected Token process(Token t) throws IOException {
      if ("A".equals(t.termText())) {
        Token t2 = read();
        if (t2!=null && "B".equals(t2.termText())) t.setTermText("Q");
        if (t2!=null) pushBack(t2);
      }
      return t;
    }
  }

  /** Example of a class implementing "A" "B" => "A" "A" "B" */
  public static class AB_AAB_Stream extends BufferedTokenStream {
    public AB_AAB_Stream(TokenStream input) {super(input);}
    protected Token process(Token t) throws IOException {
      if ("A".equals(t.termText()) && "B".equals(peek(1).termText()))
        write(t);
      return t;
    }
  }
  
  public static String tsToString(TokenStream in) throws IOException {
    StringBuffer out = new StringBuffer();
    Token t = in.next();
    if (null != t)
      out.append(t.termText());
    
    for (t = in.next(); null != t; t = in.next()) {
      out.append(" ").append(t.termText());
    }
    in.close();
    return out.toString();
  }
  
  public void testABQ() throws Exception {
    final String input = "How now A B brown A cow B like A B thing?";
    final String expected = "How now Q B brown A cow B like Q B thing?";
    TokenStream ts = new AB_Q_Stream
      (new WhitespaceTokenizer(new StringReader(input)));
    final String actual = tsToString(ts);
    //System.out.println(actual);
    assertEquals(expected, actual);
  }
  
  public void testABAAB() throws Exception {
    final String input = "How now A B brown A cow B like A B thing?";
    final String expected = "How now A A B brown A cow B like A A B thing?";
    TokenStream ts = new AB_AAB_Stream
      (new WhitespaceTokenizer(new StringReader(input)));
    final String actual = tsToString(ts);
    //System.out.println(actual);
    assertEquals(expected, actual);
  }
}