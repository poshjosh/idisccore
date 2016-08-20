package com.idisc.core.filters;

import com.scrapper.Filter;
import org.htmlparser.Attribute;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Tag;
import org.htmlparser.tags.ImageTag;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 3, 2016 10:08:59 AM
 */
public class ImagesFilter implements NodeFilter {
    
  private final Filter<String> imageSrcFilter;

  public ImagesFilter(String baseUrl) {
      this(new ImageSrcFilter(baseUrl));
  }
  
  public ImagesFilter(String baseUrl, String regexToAccept, String regexToReject) {
      this(new ImageSrcFilter(baseUrl, regexToAccept, regexToReject));
  }
  
  public ImagesFilter(Filter<String> imageSrcFilter) {
      this.imageSrcFilter = imageSrcFilter;
  }

  @Override
  public boolean accept(Node node) {
    if ((node instanceof Tag)) {
      Tag tag = (Tag)node;
      if (((tag instanceof ImageTag)) || ("IMG".equalsIgnoreCase(tag.getTagName()))) {
        Attribute attr = tag.getAttributeEx("src");
        if (attr == null) {
          return false;
        }
        String value = attr.getValue();
        if (value == null) {
          return false;
        }
        if(imageSrcFilter == null) {
            return true;
        }else{
            return imageSrcFilter.accept(value);
        }
      }
      return false;
    }

    return false;
  }
}
