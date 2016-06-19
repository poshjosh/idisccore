package com.idisc.core.util;

import com.bc.util.JsonBuilder;
import com.idisc.pu.entities.Comment;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Feeduser;
import com.idisc.pu.entities.Installation;
import com.idisc.pu.entities.Site;
import java.io.IOException;
import java.util.Map;

/**
 * @author Josh
 * @param <E>
 */
public class EntityJsonBuilder<E extends Appendable> extends JsonBuilder<E> {
    
    private final EntityMapBuilder mapBuilder;

    public EntityJsonBuilder(int columnMaxLength) { 
        super();
        mapBuilder = new EntityMapBuilder(false, columnMaxLength);
    }
    
    public EntityJsonBuilder(boolean plainTextOnly, int columnMaxLength) { 
        super();
        mapBuilder = new EntityMapBuilder(plainTextOnly, columnMaxLength);
    }

    public EntityJsonBuilder(boolean tidyOutput, boolean plainTextOnly, int columnMaxLength) {
        super(tidyOutput);
        mapBuilder = new EntityMapBuilder(plainTextOnly, columnMaxLength);
    }

    public EntityJsonBuilder(boolean tidyOutput, boolean escapeOutput, 
            boolean plainTextOnly, int columnMaxLength) {
        super(tidyOutput, escapeOutput);
        mapBuilder = new EntityMapBuilder(plainTextOnly, columnMaxLength);
    }

    public EntityJsonBuilder(boolean tidyOutput, boolean escapeOutput, 
            String indent, boolean plainTextOnly, int columnMaxLength) {
        super(tidyOutput, escapeOutput, indent);
        mapBuilder = new EntityMapBuilder(plainTextOnly, columnMaxLength);
    }

  @Override
  public void appendJSONString(Object value, E appendTo) throws IOException {
    if ((value instanceof Feed)) {
      appendJsonString((Feed)value, appendTo);
    } else if ((value instanceof Installation)) {
      appendJsonString((Installation)value, appendTo);
    } else if ((value instanceof Site)) {
      appendJsonString((Site)value, appendTo);
    } else if ((value instanceof Comment)) {
      appendJsonString((Comment)value, appendTo);
    } else if ((value instanceof Feeduser)) {
      appendJsonString((Feeduser)value, appendTo);
    } else {
      super.appendJSONString(value, appendTo);
    }
  }
  
  public void appendJsonString(Feeduser user, E appendTo) throws IOException {
    Map map = mapBuilder.toMap(user);
    appendJSONString(map, appendTo);
  }
  
  public void appendJsonString(Feed feed, E appendTo) throws IOException {
    Map map = mapBuilder.toMap(feed);
    appendJSONString(map, appendTo);
  }
  
  public void appendJsonString(Comment comment, E appendTo) throws IOException {
    Map map = mapBuilder.toMap(comment);
    appendJSONString(map, appendTo);
  }
  
  public void appendJsonString(Installation installation, E appendTo) throws IOException {
    Map map = mapBuilder.toMap(installation);
    appendJSONString(map, appendTo);
  }
  
  public void appendJsonString(Site site, E appendTo) throws IOException {
    Map map = mapBuilder.toMap(site);
    appendJSONString(map, appendTo);
  }
}
