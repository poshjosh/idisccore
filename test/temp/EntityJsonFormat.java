package temp;

import com.idisc.core.util.EntityMapBuilder;
import com.idisc.pu.entities.Comment;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Feeduser;
import com.idisc.pu.entities.Installation;
import com.idisc.pu.entities.Site;
import java.util.Map;

public class EntityJsonFormat extends com.bc.util.JsonFormat{
    
  private final EntityMapBuilder mapBuilder;
  
  public EntityJsonFormat() {  
    this(false, false, 1000);
  }
  
  public EntityJsonFormat(boolean tidyOutput, boolean plainTextOnly, int maxTextLength) {  
    super(tidyOutput);
    mapBuilder = new EntityMapBuilder(plainTextOnly, maxTextLength);
  }
  
  @Override
  public void appendJSONString(Object value, StringBuilder appendTo) {
    if ((value instanceof Feed)) {
      appendJsonString((Feed)value, appendTo);
    } else if ((value instanceof Installation)) {
      appendJsonString((Installation)value, appendTo);
    } else if ((value instanceof Comment)) {
      appendJsonString((Comment)value, appendTo);
    } else if ((value instanceof Site)) {
      appendJsonString((Site)value, appendTo);
    } else if ((value instanceof Feeduser)) {
      appendJsonString((Feeduser)value, appendTo);
    } else {
      super.appendJSONString(value, appendTo);
    }
  }
  
  public void appendJsonString(Feeduser user, StringBuilder appendTo) {
    Map map = mapBuilder.toMap(user);
    appendJSONString(map, appendTo);
  }
  
  public void appendJsonString(Feed feed, StringBuilder appendTo) {
    Map map = mapBuilder.toMap(feed);
    appendJSONString(map, appendTo);
  }
  
  public void appendJsonString(Comment comment, StringBuilder appendTo) {
    Map map = mapBuilder.toMap(comment);
    appendJSONString(map, appendTo);
  }
  
  public void appendJsonString(Installation installation, StringBuilder appendTo) {
    Map map = mapBuilder.toMap(installation);
    appendJSONString(map, appendTo);
  }
  
  public void appendJsonString(Site site, StringBuilder appendTo) {
    Map map = mapBuilder.toMap(site);
    appendJSONString(map, appendTo);
  }
}
