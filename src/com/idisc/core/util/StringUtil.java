package com.idisc.core.util;

import java.io.StringWriter;
import java.util.HashMap;














public class StringUtil
{
  public static final String unescapeHtml3(String input)
  {
    StringWriter writer = null;
    int len = input.length();
    int i = 1;
    int st = 0;
    for (;;)
    {
      if ((i < len) && (input.charAt(i - 1) != '&')) {
        i++;
      } else { if (i >= len) {
          break;
        }
        
        int j = i;
        while ((j < len) && (j < i + 6 + 1) && (input.charAt(j) != ';'))
          j++;
        if ((j == len) || (j < i + 2) || (j == i + 6 + 1)) {
          i++;

        }
        else
        {
          if (input.charAt(i) == '#')
          {
            int k = i + 1;
            int radix = 10;
            
            char firstChar = input.charAt(k);
            if ((firstChar == 'x') || (firstChar == 'X')) {
              k++;
              radix = 16;
            }
            try
            {
              int entityValue = Integer.parseInt(input.substring(k, j), radix);
              
              if (writer == null)
                writer = new StringWriter(input.length());
              writer.append(input.substring(st, i - 1));
              
              if (entityValue > 65535) {
                char[] chrs = Character.toChars(entityValue);
                writer.write(chrs[0]);
                writer.write(chrs[1]);
              } else {
                writer.write(entityValue);
              }
            }
            catch (NumberFormatException ex) {
              i++; }
            continue;

          }
          else
          {
            CharSequence value = (CharSequence)lookupMap.get(input.substring(i, j));
            if (value == null) {
              i++;
              continue;
            }
            
            if (writer == null)
              writer = new StringWriter(input.length());
            writer.append(input.substring(st, i - 1));
            
            writer.append(value);
          }
          

          st = j + 1;
          i = st;
        }
      } }
    if (writer != null) {
      writer.append(input.substring(st, len));
      return writer.toString();
    }
    return input;
  }
  
  private static final String[][] ESCAPES = { { "\"", "quot" }, { "&", "amp" }, { "<", "lt" }, { ">", "gt" }, { " ", "nbsp" }, { "¡", "iexcl" }, { "¢", "cent" }, { "£", "pound" }, { "¤", "curren" }, { "¥", "yen" }, { "¦", "brvbar" }, { "§", "sect" }, { "¨", "uml" }, { "©", "copy" }, { "ª", "ordf" }, { "«", "laquo" }, { "¬", "not" }, { "­", "shy" }, { "®", "reg" }, { "¯", "macr" }, { "°", "deg" }, { "±", "plusmn" }, { "²", "sup2" }, { "³", "sup3" }, { "´", "acute" }, { "µ", "micro" }, { "¶", "para" }, { "·", "middot" }, { "¸", "cedil" }, { "¹", "sup1" }, { "º", "ordm" }, { "»", "raquo" }, { "¼", "frac14" }, { "½", "frac12" }, { "¾", "frac34" }, { "¿", "iquest" }, { "À", "Agrave" }, { "Á", "Aacute" }, { "Â", "Acirc" }, { "Ã", "Atilde" }, { "Ä", "Auml" }, { "Å", "Aring" }, { "Æ", "AElig" }, { "Ç", "Ccedil" }, { "È", "Egrave" }, { "É", "Eacute" }, { "Ê", "Ecirc" }, { "Ë", "Euml" }, { "Ì", "Igrave" }, { "Í", "Iacute" }, { "Î", "Icirc" }, { "Ï", "Iuml" }, { "Ð", "ETH" }, { "Ñ", "Ntilde" }, { "Ò", "Ograve" }, { "Ó", "Oacute" }, { "Ô", "Ocirc" }, { "Õ", "Otilde" }, { "Ö", "Ouml" }, { "×", "times" }, { "Ø", "Oslash" }, { "Ù", "Ugrave" }, { "Ú", "Uacute" }, { "Û", "Ucirc" }, { "Ü", "Uuml" }, { "Ý", "Yacute" }, { "Þ", "THORN" }, { "ß", "szlig" }, { "à", "agrave" }, { "á", "aacute" }, { "â", "acirc" }, { "ã", "atilde" }, { "ä", "auml" }, { "å", "aring" }, { "æ", "aelig" }, { "ç", "ccedil" }, { "è", "egrave" }, { "é", "eacute" }, { "ê", "ecirc" }, { "ë", "euml" }, { "ì", "igrave" }, { "í", "iacute" }, { "î", "icirc" }, { "ï", "iuml" }, { "ð", "eth" }, { "ñ", "ntilde" }, { "ò", "ograve" }, { "ó", "oacute" }, { "ô", "ocirc" }, { "õ", "otilde" }, { "ö", "ouml" }, { "÷", "divide" }, { "ø", "oslash" }, { "ù", "ugrave" }, { "ú", "uacute" }, { "û", "ucirc" }, { "ü", "uuml" }, { "ý", "yacute" }, { "þ", "thorn" }, { "ÿ", "yuml" } };
  



































  private static final int MIN_ESCAPE = 2;
  



































  private static final int MAX_ESCAPE = 6;
  


































  private static final HashMap<String, CharSequence> lookupMap = new HashMap();
  static { for (CharSequence[] seq : ESCAPES) {
      lookupMap.put(seq[1].toString(), seq[0]);
    }
  }
}
