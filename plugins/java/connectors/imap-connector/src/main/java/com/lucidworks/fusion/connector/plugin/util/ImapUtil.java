package com.lucidworks.fusion.connector.plugin.util;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import java.io.IOException;

public class ImapUtil {

  public static String getText(Part p) throws MessagingException, IOException {
    if (p.isMimeType("text/*")) {
      return (String) p.getContent();
    }

    if (p.isMimeType("multipart/alternative")) {
      Multipart mp = (Multipart) p.getContent();
      String text = null;
      for (int i = 0; i < mp.getCount(); i++) {
        Part bp = mp.getBodyPart(i);
        if (bp.isMimeType("text/plain")) {
          if (text == null)
            text = getText(bp);
          continue;
        } else if (bp.isMimeType("text/html")) {
          String s = getText(bp);
          if (s != null)
            return s;
        } else {
          return getText(bp);
        }
      }
      return text;
    }
    else if (p.isMimeType("multipart/*")) {
      Multipart mp = (Multipart) p.getContent();
      for (int i = 0; i < mp.getCount(); i++) {
        String s = getText(mp.getBodyPart(i));
        if (s != null)
          return s;
      }
    }

    return null;
  }
}
