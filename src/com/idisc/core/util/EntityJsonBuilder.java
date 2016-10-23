package com.idisc.core.util;

import com.bc.jpa.util.EntityMapBuilder;
import com.bc.util.JsonBuilder;
import com.bc.util.XLogger;
import com.idisc.core.util.mapbuildertransformers.TransformerService;
import com.idisc.core.util.mapbuildertransformers.TransformerServiceImpl;
import com.idisc.pu.entities.Comment;
import com.idisc.pu.entities.Country;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Feeduser;
import com.idisc.pu.entities.Installation;
import com.idisc.pu.entities.Site;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.persistence.Entity;

/**
 * @author Josh
 * @param <E>
 */
public class EntityJsonBuilder<E extends Appendable> extends JsonBuilder<E> {
    
  private final EntityMapBuilder mapBuilder;
  
  private final TransformerService transformerService;
  
  private final Map reusedMap;

  public EntityJsonBuilder(int columnMaxLength) { 
      
    this(false, columnMaxLength);
  }
    
  public EntityJsonBuilder(boolean plainTextOnly, int columnMaxLength) {
      
    this(false, plainTextOnly, columnMaxLength);
  }

  public EntityJsonBuilder(boolean tidyOutput, boolean plainTextOnly, int columnMaxLength) {
        
    this(tidyOutput, true, plainTextOnly, columnMaxLength);
  }

  public EntityJsonBuilder(boolean tidyOutput, boolean escapeOutput, 
      boolean plainTextOnly, int columnMaxLength) {
        
    this(tidyOutput, escapeOutput, "  ", 
            new DefaultEntityMapBuilder(), 
            new TransformerServiceImpl(plainTextOnly, columnMaxLength));
  }

  public EntityJsonBuilder(boolean tidyOutput, boolean escapeOutput, 
            String indent, boolean plainTextOnly, int columnMaxLength) {
      
    this(tidyOutput, escapeOutput, indent, 
            new DefaultEntityMapBuilder(), 
            new TransformerServiceImpl(plainTextOnly, columnMaxLength));
  }

  public EntityJsonBuilder(boolean tidyOutput, boolean escapeOutput, 
            String indent, EntityMapBuilder mapBuilder, TransformerService transformerService) {
      
    super(tidyOutput, escapeOutput, indent);
    
    this.mapBuilder = mapBuilder;
    
    this.transformerService = transformerService;
    
    this.reusedMap = new HashMap();
  }
  
  @Override
  public void appendJSONString(Object value, E appendTo) throws IOException {
      
XLogger.getInstance().log(Level.FINER, "#appendJSONString(Object, Appendable)", this.getClass());

    final Class entityType = this.getEntityType(value, null);
    
    if(entityType == null) {
        
      super.appendJSONString(value, appendTo);
        
    }else{
        
      this.reusedMap.clear();
    
XLogger.getInstance().log(Level.FINER, "Entity type: {0}, entity: {1}", 
        this.getClass(), entityType, value);

      this.mapBuilder.build(entityType, value, this.reusedMap, transformerService.get(entityType));
    
XLogger.getInstance().log(Level.FINER, "Entity map: {0}", this.getClass(), this.reusedMap);

      this.appendJSONString(this.reusedMap, appendTo);
    }
  }
  
  public Class getEntityType(Object value, Class outputIfNone) {
    Class output;
    if ((value instanceof Feed)) {
      output = Feed.class;
    } else if ((value instanceof Installation)) {
      output = Installation.class;
    } else if ((value instanceof Site)) {
      output = Site.class;
    } else if ((value instanceof Comment)) {
      output = Comment.class;
    } else if ((value instanceof Feeduser)) {
      output = Feeduser.class;
    } else if ((value instanceof Country)) {
      output = Country.class;
    } else {
      final Class valueType = value.getClass();
      if(valueType.getAnnotation(Entity.class) != null) {
        output = valueType;
      }else{
        output = outputIfNone;
      }
    }
    return output;
  }
}

